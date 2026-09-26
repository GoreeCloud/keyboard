package com.goreecloud.keyboard

import android.animation.ValueAnimator
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.view.inputmethod.EditorInfo

class KeyboardService : InputMethodService(), KeyboardView.Listener {
    private var shifted = false
    private var currentLayer = KeyboardLayer.LETTERS

    // No active editor has granted ordinary-field behavior yet. Keep the process default fail-closed
    // until onStartInput/onStartInputView provide concrete EditorInfo for the current session.
    private var sensitiveInput = true
    private var editorSuppressesLanguageAssistance = true
    private var editorProhibitsPersonalizedLearning = true
    private var suggestionsSuppressed = true
    private var composingCaptureExhausted = false
    private var keyboardView: KeyboardView? = null
    private val suggestionEngine = SuggestionEngine()
    private val runTogetherWordResolver = RunTogetherWordResolver()
    private var pendingPhraseRewrite: HyphenatedCompoundModel.Rewrite? = null
    private val swipeTypingEngine = SwipeTypingEngine()
    private val packagedEnglishDictionary by lazy { PackagedEnglishDictionary(this) }
    private val settingsStore by lazy { KeyboardSettingsStore(this) }
    private val learningStore by lazy { KeyboardLearningStore(this) }
    private val clipboardPreferences by lazy { KeyboardClipboardPreferences(this) }
    private val clipboardHistoryStore by lazy { EncryptedClipboardHistoryStore(this) }
    private val clipboardController by lazy {
        KeyboardClipboardController(
            context = this,
            preferences = clipboardPreferences,
            historyStore = clipboardHistoryStore,
        ).also { controller ->
            controller.onChanged = {
                clipboardPanelView?.render(controller.snapshot())
            }
        }
    }
    private var clipboardPanelView: KeyboardClipboardPanelView? = null
    private var typingSettings = KeyboardTypingSettings()
    private val composingWord = StringBuilder()
    private val committedHistory = mutableListOf<String>()
    private var composingStartsCapitalized = false
    private var sentenceStartPending = false
    private var presentedSuggestions: List<String> = emptyList()
    private var pendingSwipeCorrection: PendingSwipeCorrection? = null
    private var suggestionRefreshScheduled = false
    private val suggestionRefreshRunnable = Runnable {
        suggestionRefreshScheduled = false
        updateSuggestions()
    }

    override fun onCreateInputView(): View {
        typingSettings = settingsStore.load()
        packagedEnglishDictionary.preload()
        val builtInDictionary = packagedEnglishDictionary.words
        suggestionEngine.preload(builtInDictionary)
        swipeTypingEngine.preload(builtInDictionary)
        return KeyboardView(this).also { view ->
            keyboardView = view
            view.listener = this
            currentLayer = KeyboardLayer.LETTERS
            view.setLayer(currentLayer)
            view.setShifted(shifted)
            view.setKeyHeightPreference(typingSettings.keyHeight)
            view.setToolbarStyle(typingSettings.toolbarStyle)
            view.setKeyPressHapticsEnabled(typingSettings.hapticFeedbackEnabled)
            view.setKeyPressSoundEnabled(typingSettings.keyPressSoundEnabled)
            view.setEmojiToolbarEnabled(typingSettings.emojiToolbarEnabled)
            view.setLongPressHintsEnabled(typingSettings.longPressHintsEnabled)
            view.setLongPressDelay(typingSettings.longPressDelay)
            view.setSwipeTrailEnabled(typingSettings.swipeTrailEnabled)
            view.setNumberRowVisible(KeyboardNumberRowPolicy.isVisible(typingSettings, sensitiveInput))
            view.setSwipeTypingEnabled(!sensitiveInput && typingSettings.swipeTypingEnabled)
            view.setGlazeV16PresentationSignals(currentGlazeV16PresentationSignals())
            view.setOnTouchListener(
                SpacebarCursorTouchListener(
                    keyboardView = view,
                    isEnabled = {
                        settingsStore.load().spacebarCursorControlEnabled &&
                            currentLayer != KeyboardLayer.EMOJI &&
                            !touchExplorationEnabled()
                    },
                    onCursorSteps = ::moveCursorFromSpacebar,
                ),
            )
            updateSuggestions()
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        beginEditorSession(attribute)
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        beginEditorSession(info)
        clipboardController.onInputViewVisible()
        currentLayer = KeyboardLayer.LETTERS
        keyboardView?.setLayer(currentLayer)
        keyboardView?.setKeyHeightPreference(typingSettings.keyHeight)
        keyboardView?.setToolbarStyle(typingSettings.toolbarStyle)
        keyboardView?.setKeyPressHapticsEnabled(typingSettings.hapticFeedbackEnabled)
        keyboardView?.setKeyPressSoundEnabled(typingSettings.keyPressSoundEnabled)
        keyboardView?.setEmojiToolbarEnabled(typingSettings.emojiToolbarEnabled)
        keyboardView?.setLongPressHintsEnabled(typingSettings.longPressHintsEnabled)
        keyboardView?.setLongPressDelay(typingSettings.longPressDelay)
        keyboardView?.setSwipeTrailEnabled(typingSettings.swipeTrailEnabled)
        keyboardView?.setNumberRowVisible(KeyboardNumberRowPolicy.isVisible(typingSettings, sensitiveInput))
        keyboardView?.setGlazeV16PresentationSignals(currentGlazeV16PresentationSignals())
        refreshAutomaticShift()
        updateSuggestions()
    }

    override fun onFinishInput() {
        clipboardController.onInputViewHidden()
        closeClipboardPanel(restoreKeyboard = false)
        super.onFinishInput()
        resetEditorSession()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        clipboardController.onInputViewHidden()
        closeClipboardPanel(restoreKeyboard = false)
        super.onFinishInputView(finishingInput)
        resetEditorSession()
    }

    override fun onDestroy() {
        cancelScheduledSuggestionRefresh()
        composingWord.clear()
        composingStartsCapitalized = false
        composingCaptureExhausted = false
        committedHistory.clear()
        sentenceStartPending = false
        presentedSuggestions = emptyList()
        clipboardController.onInputViewHidden()
        closeClipboardPanel(restoreKeyboard = false)
        keyboardView = null
        super.onDestroy()
    }

    override fun onText(value: String) {
        if (value.isEmpty()) return
        pendingSwipeCorrection = null
        pendingPhraseRewrite = null

        val isLetterText = value.codePoints().allMatch { Character.isLetter(it) }
        val output = if (shifted && isLetterText) value.uppercase() else value

        if (!isLetterText && value in AUTOCORRECT_BOUNDARIES) {
            commitBoundary(value)
            return
        }

        currentInputConnection?.commitText(output, 1)

        if (languageCaptureAllowed()) {
            if (isLetterText) {
                sentenceStartPending = false
                if (composingWord.isEmpty()) {
                    composingStartsCapitalized =
                        output.codePointAt(0).let { Character.isUpperCase(it) }
                }

                val normalized = output.lowercase()
                if (!composingCaptureExhausted &&
                    SuggestionCapturePolicy.canAppend(composingWord.toString(), normalized)
                ) {
                    composingWord.append(normalized)
                } else {
                    composingWord.clear()
                    composingStartsCapitalized = false
                    composingCaptureExhausted = true
                }
            } else {
                clearComposingBoundary()
            }
        }

        scheduleSuggestionsUpdate()
        resetOneShotShift()
    }

    override fun onSpace() {
        pendingSwipeCorrection = null
        pendingPhraseRewrite = null
        if (tryCommitDoubleSpacePeriod()) return
        commitBoundary(" ")
    }

    override fun onSwipe(keyPath: List<String>) {
        val decodedCandidates = swipeTypingEngine.decode(
            keyPath = keyPath,
            dictionary = activeSwipeDictionary(),
            limit = SWIPE_DECODE_CANDIDATE_POOL,
        )
        commitDecodedSwipe(decodedCandidates)
    }

    override fun onSwipeGesture(gesture: SwipeGesture) {
        val decodedCandidates = swipeTypingEngine.decode(
            gesture = gesture,
            dictionary = activeSwipeDictionary(),
            limit = SWIPE_DECODE_CANDIDATE_POOL,
        )
        commitDecodedSwipe(decodedCandidates)
    }

    private fun commitDecodedSwipe(decodedCandidates: List<String>) {
        if (
            sensitiveInput ||
            !typingSettings.swipeTypingEnabled ||
            decodedCandidates.isEmpty()
        ) return

        val connection = currentInputConnection ?: return
        val candidates = SwipeCandidateRanker.rank(
            decoded = decodedCandidates,
            contextualPredictions = if (editorSuppressesLanguageAssistance) {
                emptyList()
            } else {
                predictionCandidates(limit = SWIPE_DECODE_CANDIDATE_POOL)
            },
            limit = 3,
        )
        if (candidates.isEmpty()) return

        fun format(value: String): String =
            if (shifted) {
                value.replaceFirstChar { first ->
                    if (first.isLowerCase()) first.titlecase() else first.toString()
                }
            } else {
                value
            }

        val formattedCandidates = candidates.map(::format)
        val output = formattedCandidates.first()

        connection.commitText("$output ", 1)
        recordCommittedWord(output, learn = false)
        sentenceStartPending = false
        clearComposingBoundary()
        resetOneShotShift()

        pendingSwipeCorrection = PendingSwipeCorrection(
            committedWord = output,
            candidates = formattedCandidates,
        )
        presentedSuggestions = formattedCandidates.take(3)
        keyboardView?.setSuggestions(presentedSuggestions)
    }

    override fun onBackspace() {
        pendingSwipeCorrection = null
        val connection = currentInputConnection ?: return

        if (sensitiveInput) {
            if (!connection.deleteSurroundingTextInCodePoints(1, 0)) {
                sendFallbackBackspace(connection)
            }
            clearPredictionContextAfterUnverifiedDeletion()
            scheduleSuggestionsUpdate()
            return
        }

        val beforeCursor = connection.getTextBeforeCursor(BACKSPACE_LOOKBEHIND_UTF16, 0)
        if (beforeCursor == null) {
            // Some editors do not implement bounded surrounding-text reads reliably. A standard
            // DEL key event is the most compatible fallback and requires no additional text read.
            sendFallbackBackspace(connection)
            clearPredictionContextAfterUnverifiedDeletion()
            scheduleSuggestionsUpdate()
            return
        }

        val deleteCodePoints = TextDeletion.previousTextUnitCodePointCount(
            textBeforeCursor = beforeCursor,
            contextMayBeTruncated = beforeCursor.length >= BACKSPACE_LOOKBEHIND_UTF16,
        )

        if (deleteCodePoints <= 0) {
            sendFallbackBackspace(connection)
            clearPredictionContextAfterUnverifiedDeletion()
            scheduleSuggestionsUpdate()
            return
        }

        val deleted = connection.deleteSurroundingTextInCodePoints(deleteCodePoints, 0)
        if (!deleted) {
            sendFallbackBackspace(connection)
            clearPredictionContextAfterUnverifiedDeletion()
            scheduleSuggestionsUpdate()
            return
        }

        if (languageCaptureAllowed() && !composingCaptureExhausted && composingWord.isNotEmpty()) {
            val lastCodePointStart = composingWord.offsetByCodePoints(composingWord.length, -1)
            composingWord.delete(lastCodePointStart, composingWord.length)
            if (composingWord.isEmpty()) composingStartsCapitalized = false
        } else if (composingWord.isEmpty()) {
            // Cursor edits outside the locally tracked word invalidate transient prediction context.
            committedHistory.clear()
            sentenceStartPending = false
        }

        scheduleSuggestionsUpdate()
    }

    override fun onEnter() {
        pendingSwipeCorrection = null
        val connection = currentInputConnection ?: return
        connection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        connection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        clearComposingBoundary()
        committedHistory.clear()
        sentenceStartPending = true
        applyAutomaticShiftIfNeeded()
        updateSuggestions()
    }

    override fun onShift() {
        shifted = !shifted
        keyboardView?.setShifted(shifted)
    }

    override fun onSuggestion(value: String) {
        if (tryCommitSwipeCorrection(value)) return
        if (tryCommitPhraseRewrite(value)) return

        if (
            editorSuppressesLanguageAssistance ||
            !typingSettings.suggestionsEnabled ||
            sensitiveInput ||
            composingCaptureExhausted
        ) return
        if (!SuggestionCommitPolicy.isPresentedCandidate(value, presentedSuggestions)) return

        val connection = currentInputConnection ?: return
        val prefix = composingWord.toString()
        val prefixCodePoints = prefix.codePointCount(0, prefix.length)

        if (prefixCodePoints > 0) {
            val beforeCursor = connection.getTextBeforeCursor(prefix.length, 0)
            if (!SuggestionCommitPolicy.matchesExpectedPrefix(prefix, beforeCursor)) {
                composingWord.clear()
                composingStartsCapitalized = false
                composingCaptureExhausted = true
                presentedSuggestions = emptyList()
                keyboardView?.setSuggestions(emptyList())
                return
            }
            connection.deleteSurroundingTextInCodePoints(prefixCodePoints, 0)
        }

        connection.commitText("$value ", 1)
        recordCommittedWord(value)
        sentenceStartPending = false
        clearComposingBoundary()
        resetOneShotShift()
        updateSuggestions()
    }

    override fun onLayerChanged(layer: KeyboardLayer) {
        currentLayer = layer
        pendingSwipeCorrection = null
        shifted = false
        clearComposingBoundary()
        keyboardView?.setShifted(false)
        if (layer == KeyboardLayer.LETTERS) applyAutomaticShiftIfNeeded()
        updateSuggestions()
    }

    override fun onOpenClipboard() {
        pendingSwipeCorrection = null
        pendingPhraseRewrite = null
        clearComposingBoundary()
        presentedSuggestions = emptyList()
        keyboardView?.setSuggestions(emptyList())

        val panel = KeyboardClipboardPanelView(
            context = this,
            callbacks = KeyboardClipboardPanelView.Callbacks(
                onClose = { closeClipboardPanel() },
                onPaste = ::pasteClipboardEntry,
                onTogglePin = { id ->
                    clipboardController.togglePin(id)
                    clipboardPanelView?.render(clipboardController.snapshot())
                },
                onDelete = { id ->
                    clipboardController.delete(id)
                    clipboardPanelView?.render(clipboardController.snapshot())
                },
                onClearUnpinned = {
                    clipboardController.clearUnpinned()
                    clipboardPanelView?.render(clipboardController.snapshot())
                },
                onHistoryEnabledChanged = { enabled ->
                    clipboardController.setHistoryEnabled(enabled)
                    clipboardPanelView?.render(clipboardController.openSnapshot())
                },
                onPolicyChanged = { policy ->
                    clipboardController.setCurrentAppPolicy(policy)
                    clipboardPanelView?.render(clipboardController.openSnapshot())
                },
            ),
        )
        clipboardPanelView = panel
        panel.render(clipboardController.openSnapshot())
        setInputView(panel)
    }

    private fun pasteClipboardEntry(id: String, pasteOnce: Boolean) {
        val text = clipboardController.consume(id, pasteOnce) ?: return
        currentInputConnection?.commitText(text, 1)

        // Clipboard payloads never enter learning, correction, prediction, or transient history.
        composingWord.clear()
        committedHistory.clear()
        composingStartsCapitalized = false
        composingCaptureExhausted = false
        sentenceStartPending = false
        presentedSuggestions = emptyList()
        pendingSwipeCorrection = null
        pendingPhraseRewrite = null
        clipboardPanelView?.render(clipboardController.snapshot())
    }

    private fun closeClipboardPanel(restoreKeyboard: Boolean = true) {
        val panel = clipboardPanelView ?: return
        clipboardPanelView = null
        clipboardController.closePanel()
        panel.removeAllViews()

        if (!restoreKeyboard) return
        keyboardView?.let { keyboard ->
            setInputView(keyboard)
            currentLayer = KeyboardLayer.LETTERS
            keyboard.setLayer(currentLayer)
            refreshAutomaticShift()
            updateSuggestions()
        }
    }

    override fun onOpenSettings() {
        startActivity(
            Intent(this, KeyboardSettingsActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    private fun moveCursorFromSpacebar(requestedSteps: Int) {
        if (requestedSteps == 0) return
        val connection = currentInputConnection ?: return
        val steps = requestedSteps.coerceIn(
            -MAX_CURSOR_STEPS_PER_CALLBACK,
            MAX_CURSOR_STEPS_PER_CALLBACK,
        )
        val keyCode =
            if (steps < 0) KeyEvent.KEYCODE_DPAD_LEFT else KeyEvent.KEYCODE_DPAD_RIGHT

        repeat(kotlin.math.abs(steps)) {
            connection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            connection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        }

        pendingSwipeCorrection = null
        composingWord.clear()
        composingStartsCapitalized = false
        composingCaptureExhausted = true
        committedHistory.clear()
        sentenceStartPending = false
        presentedSuggestions = emptyList()
        keyboardView?.setSuggestions(emptyList())
    }

    private fun touchExplorationEnabled(): Boolean {
        val accessibilityManager =
            getSystemService(ACCESSIBILITY_SERVICE) as? AccessibilityManager
        return accessibilityManager?.isTouchExplorationEnabled == true
    }

    private fun beginEditorSession(info: EditorInfo?) {
        typingSettings = settingsStore.load()
        shifted = false
        currentLayer = KeyboardLayer.LETTERS
        composingWord.clear()
        committedHistory.clear()
        composingStartsCapitalized = false
        sentenceStartPending = false
        composingCaptureExhausted = false
        presentedSuggestions = emptyList()
        pendingSwipeCorrection = null
        pendingPhraseRewrite = null
        keyboardView?.setSuggestions(emptyList())
        keyboardView?.setKeyHeightPreference(typingSettings.keyHeight)
        keyboardView?.setToolbarStyle(typingSettings.toolbarStyle)
        keyboardView?.setKeyPressHapticsEnabled(typingSettings.hapticFeedbackEnabled)
        keyboardView?.setKeyPressSoundEnabled(typingSettings.keyPressSoundEnabled)
        keyboardView?.setEmojiToolbarEnabled(typingSettings.emojiToolbarEnabled)
        keyboardView?.setLongPressHintsEnabled(typingSettings.longPressHintsEnabled)
        keyboardView?.setLongPressDelay(typingSettings.longPressDelay)
        keyboardView?.setSwipeTrailEnabled(typingSettings.swipeTrailEnabled)
        keyboardView?.setSwipeTypingEnabled(false)

        if (info == null) {
            // Unknown editor metadata must not silently receive ordinary-field privileges. Treat it
            // as sensitive so backspace avoids surrounding-text inspection and suggestions remain
            // suppressed until Android provides a concrete EditorInfo for the active session.
            sensitiveInput = true
            editorSuppressesLanguageAssistance = true
            editorProhibitsPersonalizedLearning = true
            suggestionsSuppressed = true
            clipboardController.updateEditor(packageName = null, sensitive = true)
            keyboardView?.setNumberRowVisible(
                KeyboardNumberRowPolicy.isVisible(typingSettings, sensitiveInput),
            )
            return
        }

        val inputType = info.inputType
        sensitiveInput = InputPrivacyClassifier.isSensitive(inputType)
        editorSuppressesLanguageAssistance =
            EditorSuggestionPolicy.shouldSuppress(inputType, info.imeOptions)
        editorProhibitsPersonalizedLearning =
            EditorSuggestionPolicy.prohibitsPersonalizedLearning(info.imeOptions)
        suggestionsSuppressed =
            editorSuppressesLanguageAssistance || !typingSettings.suggestionsEnabled
        keyboardView?.setNumberRowVisible(
            KeyboardNumberRowPolicy.isVisible(typingSettings, sensitiveInput),
        )
        keyboardView?.setSwipeTypingEnabled(
            !EditorSuggestionPolicy.shouldSuppressGestureTyping(inputType) &&
                typingSettings.swipeTypingEnabled,
        )
        clipboardController.updateEditor(
            packageName = info.packageName,
            sensitive = sensitiveInput,
        )
    }

    private fun resetEditorSession() {
        cancelScheduledSuggestionRefresh()
        shifted = false
        currentLayer = KeyboardLayer.LETTERS
        sensitiveInput = true
        editorSuppressesLanguageAssistance = true
        editorProhibitsPersonalizedLearning = true
        suggestionsSuppressed = true
        composingWord.clear()
        committedHistory.clear()
        composingStartsCapitalized = false
        sentenceStartPending = false
        composingCaptureExhausted = false
        presentedSuggestions = emptyList()
        pendingSwipeCorrection = null
        pendingPhraseRewrite = null
        clipboardController.updateEditor(packageName = null, sensitive = true)
        keyboardView?.setLayer(KeyboardLayer.LETTERS)
        keyboardView?.setShifted(false)
        keyboardView?.setSwipeTypingEnabled(false)
        keyboardView?.setSuggestions(emptyList())
    }

    private fun clearComposingBoundary() {
        composingWord.clear()
        composingStartsCapitalized = false
        composingCaptureExhausted = false
    }

    private fun tryCommitDoubleSpacePeriod(): Boolean {
        if (
            !typingSettings.doubleSpacePeriodEnabled ||
            sensitiveInput ||
            editorSuppressesLanguageAssistance
        ) return false

        val connection = currentInputConnection ?: return false
        val beforeCursor = connection.getTextBeforeCursor(DOUBLE_SPACE_LOOKBEHIND_UTF16, 0)
            ?: return false
        if (!DoubleSpacePeriodPolicy.shouldReplacePreviousSpace(beforeCursor)) return false
        if (!connection.deleteSurroundingTextInCodePoints(1, 0)) return false

        connection.commitText(". ", 1)
        clearComposingBoundary()
        committedHistory.clear()
        sentenceStartPending = true
        applyAutomaticShiftIfNeeded()
        updateSuggestions()
        return true
    }

    private fun commitBoundary(separator: String) {
        val connection = currentInputConnection ?: return
        val prefix = composingWord.toString()
        var committedCorrection = false
        var committedWord: String? = prefix.takeIf { it.isNotEmpty() }

        if (
            typingSettings.autocorrectEnabled &&
            !editorSuppressesLanguageAssistance &&
            !sensitiveInput &&
            !composingCaptureExhausted &&
            prefix.isNotEmpty()
        ) {
            val contextHistory = transientContextHistory(excludeCurrentComposingWord = true)
            val contextualPredictions = contextualPredictions(contextHistory, limit = 8)
            val dictionary = activeDictionary()
            val correction =
                QuillGrammarModel.boundaryCorrection(prefix, contextHistory)
                    ?: ContractionModel.correction(prefix)
                    ?: runTogetherWordResolver.resolve(prefix, dictionary)
                    ?: runTogetherWordResolver.resolveWithSingleEdit(prefix, dictionary)
                    ?: suggestionEngine.bestAutocorrection(
                        word = prefix,
                        dictionary = dictionary,
                        contextualPredictions = contextualPredictions,
                    )

            if (correction != null) {
                val beforeCursor = connection.getTextBeforeCursor(prefix.length, 0)
                if (SuggestionCommitPolicy.matchesExpectedPrefix(prefix, beforeCursor)) {
                    val prefixCodePoints = prefix.codePointCount(0, prefix.length)
                    connection.deleteSurroundingTextInCodePoints(prefixCodePoints, 0)
                    val formatted = formatCandidateCase(correction)
                    connection.commitText(formatted + separator, 1)
                    committedWord = formatted
                    committedCorrection = true
                } else {
                    composingCaptureExhausted = true
                }
            }
        }

        if (!committedCorrection) {
            connection.commitText(separator, 1)
        }

        committedWord?.let(::recordCommittedWord)

        if (separator in SENTENCE_ENDINGS) {
            committedHistory.clear()
            sentenceStartPending = true
            applyAutomaticShiftIfNeeded()
        } else if (separator == " ") {
            if (sentenceStartPending) {
                applyAutomaticShiftIfNeeded()
            } else {
                resetOneShotShift()
            }
        } else {
            sentenceStartPending = false
            resetOneShotShift()
        }

        clearComposingBoundary()
        updateSuggestions()
    }

    private fun formatCandidateCase(candidate: String): String =
        if (composingStartsCapitalized) {
            candidate.replaceFirstChar { first ->
                if (first.isLowerCase()) first.titlecase() else first.toString()
            }
        } else {
            candidate
        }

    private fun failClosedBackspaceContext() {
        composingWord.clear()
        committedHistory.clear()
        composingStartsCapitalized = false
        sentenceStartPending = false
        composingCaptureExhausted = true
        presentedSuggestions = emptyList()
        keyboardView?.setSuggestions(emptyList())
    }

    private fun sendFallbackBackspace(connection: android.view.inputmethod.InputConnection) {
        connection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
        connection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
    }

    private fun clearPredictionContextAfterUnverifiedDeletion() {
        composingWord.clear()
        committedHistory.clear()
        composingStartsCapitalized = false
        sentenceStartPending = false
        composingCaptureExhausted = false
        presentedSuggestions = emptyList()
    }

    private fun recordCommittedWord(word: String, learn: Boolean = true) {
        val normalizedWords = word
            .trim()
            .split(Regex("\\s+"))
            .map { it.lowercase() }
            .filter { it.isNotBlank() }
        if (normalizedWords.isEmpty()) return

        normalizedWords.forEach { normalized ->
            val previous = committedHistory.lastOrNull()
            if (learn && personalizationAllowed()) {
                learningStore.record(normalized, previous)
            }

            committedHistory += normalized
            while (committedHistory.size > MAX_PREDICTION_HISTORY_WORDS) {
                committedHistory.removeAt(0)
            }
        }
    }

    private fun tryCommitSwipeCorrection(value: String): Boolean {
        val pending = pendingSwipeCorrection ?: return false
        if (!pending.candidates.any { it.equals(value, ignoreCase = true) }) return false

        val connection = currentInputConnection ?: return false
        val expected = pending.committedWord + " "
        val beforeCursor = connection.getTextBeforeCursor(expected.length, 0)?.toString()
        if (beforeCursor != expected) {
            pendingSwipeCorrection = null
            updateSuggestions()
            return false
        }

        val codePoints = expected.codePointCount(0, expected.length)
        if (!connection.deleteSurroundingTextInCodePoints(codePoints, 0)) {
            pendingSwipeCorrection = null
            updateSuggestions()
            return false
        }

        connection.commitText("$value ", 1)
        if (committedHistory.isNotEmpty()) committedHistory.removeAt(committedHistory.lastIndex)
        recordCommittedWord(value)
        pendingSwipeCorrection = null
        clearComposingBoundary()
        updateSuggestions()
        return true
    }

    private fun tryCommitPhraseRewrite(value: String): Boolean {
        val rewrite = pendingPhraseRewrite ?: return false
        if (!rewrite.replacement.equals(value, ignoreCase = true)) return false

        val connection = currentInputConnection ?: return false
        val source = rewrite.sourceWords.joinToString(" ")
        val expectedLength = source.length + 1
        val beforeCursor = connection.getTextBeforeCursor(expectedLength, 0)?.toString() ?: return false
        if (!beforeCursor.equals("$source ", ignoreCase = true)) return false

        val replacement = if (beforeCursor.firstOrNull()?.isUpperCase() == true) {
            rewrite.replacement.replaceFirstChar { it.titlecase() }
        } else {
            rewrite.replacement
        }
        val codePoints = beforeCursor.codePointCount(0, beforeCursor.length)
        if (!connection.deleteSurroundingTextInCodePoints(codePoints, 0)) return false
        connection.commitText("$replacement ", 1)

        committedHistory.clear()
        recordCommittedWord(replacement, learn = false)
        pendingPhraseRewrite = null
        clearComposingBoundary()
        updateSuggestions()
        return true
    }

    private fun formatPredictionCase(candidate: String): String =
        if (sentenceStartPending) {
            candidate.replaceFirstChar { first ->
                if (first.isLowerCase()) first.titlecase() else first.toString()
            }
        } else {
            candidate
        }

    private fun synchronizeComposingWordFromEditor(): String {
        if (!languageCaptureAllowed()) return composingWord.toString()

        val beforeCursor = currentInputConnection
            ?.getTextBeforeCursor(CURRENT_WORD_LOOKBEHIND_UTF16, 0)
            ?: return composingWord.toString()
        if (beforeCursor.isEmpty()) return composingWord.toString()

        val editorWord = EditorContextParser.currentWordBeforeCursor(beforeCursor)
        if (editorWord == null) {
            composingWord.clear()
            composingStartsCapitalized = false
            return ""
        }

        val normalized = editorWord.lowercase().replace('’', '\'')
        if (normalized != composingWord.toString()) {
            composingWord.clear()
            composingWord.append(normalized)
            composingStartsCapitalized =
                editorWord.codePointAt(0).let { Character.isUpperCase(it) }
            composingCaptureExhausted = false
        }
        return composingWord.toString()
    }

    private fun scheduleSuggestionsUpdate() {
        if (
            sensitiveInput ||
            editorSuppressesLanguageAssistance ||
            composingCaptureExhausted
        ) {
            cancelScheduledSuggestionRefresh()
            updateSuggestions()
            return
        }

        val view = keyboardView ?: run {
            updateSuggestions()
            return
        }
        if (suggestionRefreshScheduled) return

        suggestionRefreshScheduled = true
        view.postDelayed(suggestionRefreshRunnable, SUGGESTION_REFRESH_COALESCE_MS)
    }

    private fun cancelScheduledSuggestionRefresh() {
        if (!suggestionRefreshScheduled) return
        keyboardView?.removeCallbacks(suggestionRefreshRunnable)
        suggestionRefreshScheduled = false
    }

    private fun updateSuggestions() {
        if (
            sensitiveInput ||
            editorSuppressesLanguageAssistance ||
            composingCaptureExhausted
        ) {
            presentedSuggestions = emptyList()
            keyboardView?.setSuggestions(emptyList())
            return
        }

        val synchronizedPrefix = synchronizeComposingWordFromEditor()
        val contextHistory = transientContextHistory(
            excludeCurrentComposingWord = synchronizedPrefix.isNotEmpty(),
        )

        presentedSuggestions = when {
            synchronizedPrefix.isNotEmpty() && typingSettings.suggestionsEnabled -> {
                pendingPhraseRewrite = null
                val dictionary = activeDictionary()
                val segmented =
                    runTogetherWordResolver.resolve(
                        token = synchronizedPrefix,
                        dictionary = dictionary,
                    ) ?: runTogetherWordResolver.resolveWithSingleEdit(
                        token = synchronizedPrefix,
                        dictionary = dictionary,
                    )
                val contraction = ContractionModel.suggestion(synchronizedPrefix)
                buildList<String> {
                    contraction?.let(::add)
                    segmented?.let(::add)
                    addAll(
                        suggestionEngine.suggest(
                            prefix = synchronizedPrefix,
                            dictionary = dictionary,
                            contextualPredictions = contextualPredictions(contextHistory, limit = 8),
                        ),
                    )
                }
                    .distinctBy { it.lowercase() }
                    .map(::formatCandidateCase)
            }

            composingWord.isEmpty() && typingSettings.predictionsEnabled -> {
                val rewrite = HyphenatedCompoundModel.rewriteForTail(contextHistory)
                pendingPhraseRewrite = rewrite
                buildList<String> {
                    rewrite?.replacement?.let(::add)
                    addAll(predictionCandidates(contextHistory = contextHistory))
                }
                    .distinctBy { it.lowercase() }
                    .map(::formatPredictionCase)
            }

            else -> {
                pendingPhraseRewrite = null
                emptyList()
            }
        }.take(3)

        keyboardView?.setSuggestions(presentedSuggestions)
    }

    private fun activeDictionary(): List<String> {
        val builtIn = packagedEnglishDictionary.words
        if (!personalizationAllowed()) return builtIn
        return buildList {
            addAll(builtIn)
            addAll(learningStore.learnedWords())
        }.distinctBy { it.lowercase() }
    }

    private fun activeSwipeDictionary(): List<String> {
        val builtIn = packagedEnglishDictionary.words
        if (!personalizationAllowed()) return builtIn
        return buildList {
            addAll(builtIn)
            addAll(learningStore.learnedWords())
        }.distinctBy { it.lowercase() }
    }

    private fun predictionCandidates(
        contextHistory: List<String> = transientContextHistory(),
        limit: Int = 3,
    ): List<String> {
        val learned = if (personalizationAllowed()) {
            learningStore.predictNext(contextHistory, limit = limit)
        } else {
            emptyList()
        }
        val builtIn = contextualPredictions(contextHistory, limit = maxOf(limit, 8))
        return (builtIn + learned)
            .distinctBy { it.lowercase() }
            .take(limit)
    }

    private fun contextualPredictions(
        contextHistory: List<String>,
        limit: Int,
    ): List<String> =
        QuillPredictionModel.predict(
            committedHistory = contextHistory,
            limit = limit,
        )

    private fun transientContextHistory(
        excludeCurrentComposingWord: Boolean = false,
    ): List<String> {
        if (!languageCaptureAllowed()) return committedHistory.takeLast(MAX_CONTEXT_WORDS)

        val beforeCursor = currentInputConnection
            ?.getTextBeforeCursor(EDITOR_CONTEXT_LOOKBEHIND_UTF16, 0)
        val parsed = EditorContextParser.wordsBeforeCursor(
            text = beforeCursor,
            limit = MAX_CONTEXT_WORDS + 1,
        ).toMutableList()

        if (
            excludeCurrentComposingWord &&
            composingWord.isNotEmpty() &&
            parsed.lastOrNull()?.equals(composingWord.toString(), ignoreCase = true) == true
        ) {
            parsed.removeAt(parsed.lastIndex)
        }

        return if (parsed.isNotEmpty()) parsed.takeLast(MAX_CONTEXT_WORDS)
        else committedHistory.takeLast(MAX_CONTEXT_WORDS)
    }

    private fun languageCaptureAllowed(): Boolean =
        !sensitiveInput && !editorSuppressesLanguageAssistance

    private fun personalizationAllowed(): Boolean =
        typingSettings.learnFromTypingEnabled &&
            languageCaptureAllowed() &&
            !editorProhibitsPersonalizedLearning

    private fun refreshAutomaticShift() {
        if (
            sensitiveInput ||
            !typingSettings.autoCapitalizeEnabled
        ) {
            shifted = false
            keyboardView?.setShifted(false)
            return
        }

        val capsMode = currentInputConnection?.getCursorCapsMode(TextUtils.CAP_MODE_SENTENCES) ?: 0
        shifted = capsMode and TextUtils.CAP_MODE_SENTENCES != 0
        sentenceStartPending = shifted
        keyboardView?.setShifted(shifted)
    }

    private fun applyAutomaticShiftIfNeeded() {
        if (sensitiveInput || !typingSettings.autoCapitalizeEnabled || !sentenceStartPending) return
        shifted = true
        keyboardView?.setShifted(true)
    }

    private fun resetOneShotShift() {
        if (!shifted) return
        shifted = false
        keyboardView?.setShifted(false)
    }

    private fun currentGlazeV16PresentationSignals(): GlazeKeyboardV16PresentationSignals {
        val accessibilityManager =
            getSystemService(ACCESSIBILITY_SERVICE) as? AccessibilityManager

        return GlazeKeyboardV16PresentationSignals(
            fontScale = resources.configuration.fontScale,
            animationsEnabled = ValueAnimator.areAnimatorsEnabled(),
            touchExplorationEnabled = accessibilityManager?.isTouchExplorationEnabled == true,
        )
    }

    private data class PendingSwipeCorrection(
        val committedWord: String,
        val candidates: List<String>,
    )

    private companion object {
        const val BACKSPACE_LOOKBEHIND_UTF16 = 64
        const val EDITOR_CONTEXT_LOOKBEHIND_UTF16 = 160
        const val CURRENT_WORD_LOOKBEHIND_UTF16 = 64
        const val DOUBLE_SPACE_LOOKBEHIND_UTF16 = 8
        const val MAX_CONTEXT_WORDS = 4
        const val MAX_PREDICTION_HISTORY_WORDS = 2
        const val SUGGESTION_REFRESH_COALESCE_MS = 24L
        const val MAX_CURSOR_STEPS_PER_CALLBACK = 24
        const val SWIPE_DECODE_CANDIDATE_POOL = 12
        val AUTOCORRECT_BOUNDARIES = setOf(".", ",", "!", "?", ";", ":")
        val SENTENCE_ENDINGS = setOf(".", "!", "?")
    }
}
