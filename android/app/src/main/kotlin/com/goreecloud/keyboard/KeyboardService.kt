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

    // No active editor has granted ordinary-field behavior yet. Keep the process default fail-closed
    // until onStartInput/onStartInputView provide concrete EditorInfo for the current session.
    private var sensitiveInput = true
    private var editorSuppressesLanguageAssistance = true
    private var editorProhibitsPersonalizedLearning = true
    private var suggestionsSuppressed = true
    private var composingCaptureExhausted = false
    private var keyboardView: KeyboardView? = null
    private val suggestionEngine = SuggestionEngine()
    private val swipeTypingEngine = SwipeTypingEngine()
    private val settingsStore by lazy { KeyboardSettingsStore(this) }
    private val learningStore by lazy { KeyboardLearningStore(this) }
    private var typingSettings = KeyboardTypingSettings()
    private val composingWord = StringBuilder()
    private val committedHistory = mutableListOf<String>()
    private var composingStartsCapitalized = false
    private var sentenceStartPending = false
    private var presentedSuggestions: List<String> = emptyList()
    private var pendingSwipeCorrection: PendingSwipeCorrection? = null

    override fun onCreateInputView(): View {
        typingSettings = settingsStore.load()
        return KeyboardView(this).also { view ->
            keyboardView = view
            view.listener = this
            view.setLayer(KeyboardLayer.LETTERS)
            view.setShifted(shifted)
            view.setKeyHeightPreference(typingSettings.keyHeight)
            view.setToolbarStyle(typingSettings.toolbarStyle)
            view.setKeyPressHapticsEnabled(typingSettings.hapticFeedbackEnabled)
            view.setSwipeTypingEnabled(!sensitiveInput && typingSettings.swipeTypingEnabled)
            view.setGlazeV16PresentationSignals(currentGlazeV16PresentationSignals())
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
        keyboardView?.setLayer(KeyboardLayer.LETTERS)
        keyboardView?.setKeyHeightPreference(typingSettings.keyHeight)
        keyboardView?.setToolbarStyle(typingSettings.toolbarStyle)
        keyboardView?.setKeyPressHapticsEnabled(typingSettings.hapticFeedbackEnabled)
        keyboardView?.setGlazeV16PresentationSignals(currentGlazeV16PresentationSignals())
        refreshAutomaticShift()
        updateSuggestions()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        resetEditorSession()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        resetEditorSession()
    }

    override fun onDestroy() {
        composingWord.clear()
        composingStartsCapitalized = false
        composingCaptureExhausted = false
        committedHistory.clear()
        sentenceStartPending = false
        presentedSuggestions = emptyList()
        keyboardView = null
        super.onDestroy()
    }

    override fun onText(value: String) {
        if (value.isEmpty()) return
        pendingSwipeCorrection = null

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

        updateSuggestions()
        resetOneShotShift()
    }

    override fun onSpace() {
        pendingSwipeCorrection = null
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
            editorSuppressesLanguageAssistance ||
            !typingSettings.swipeTypingEnabled ||
            decodedCandidates.isEmpty()
        ) return

        val connection = currentInputConnection ?: return
        val candidates = SwipeCandidateRanker.rank(
            decoded = decodedCandidates,
            contextualPredictions = predictionCandidates(limit = SWIPE_DECODE_CANDIDATE_POOL),
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
            updateSuggestions()
            return
        }

        val beforeCursor = connection.getTextBeforeCursor(BACKSPACE_LOOKBEHIND_UTF16, 0)
        if (beforeCursor == null) {
            // Some editors do not implement bounded surrounding-text reads reliably. A standard
            // DEL key event is the most compatible fallback and requires no additional text read.
            sendFallbackBackspace(connection)
            clearPredictionContextAfterUnverifiedDeletion()
            updateSuggestions()
            return
        }

        val deleteCodePoints = TextDeletion.previousTextUnitCodePointCount(
            textBeforeCursor = beforeCursor,
            contextMayBeTruncated = beforeCursor.length >= BACKSPACE_LOOKBEHIND_UTF16,
        )

        if (deleteCodePoints <= 0) {
            sendFallbackBackspace(connection)
            clearPredictionContextAfterUnverifiedDeletion()
            updateSuggestions()
            return
        }

        val deleted = connection.deleteSurroundingTextInCodePoints(deleteCodePoints, 0)
        if (!deleted) {
            sendFallbackBackspace(connection)
            clearPredictionContextAfterUnverifiedDeletion()
            updateSuggestions()
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

        updateSuggestions()
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
        pendingSwipeCorrection = null
        shifted = false
        clearComposingBoundary()
        keyboardView?.setShifted(false)
        if (layer == KeyboardLayer.LETTERS) applyAutomaticShiftIfNeeded()
        updateSuggestions()
    }

    override fun onOpenSettings() {
        startActivity(
            Intent(this, KeyboardSettingsActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    private fun beginEditorSession(info: EditorInfo?) {
        typingSettings = settingsStore.load()
        shifted = false
        composingWord.clear()
        committedHistory.clear()
        composingStartsCapitalized = false
        sentenceStartPending = false
        composingCaptureExhausted = false
        presentedSuggestions = emptyList()
        pendingSwipeCorrection = null
        keyboardView?.setSuggestions(emptyList())
        keyboardView?.setKeyHeightPreference(typingSettings.keyHeight)
        keyboardView?.setSwipeTypingEnabled(false)

        if (info == null) {
            // Unknown editor metadata must not silently receive ordinary-field privileges. Treat it
            // as sensitive so backspace avoids surrounding-text inspection and suggestions remain
            // suppressed until Android provides a concrete EditorInfo for the active session.
            sensitiveInput = true
            editorSuppressesLanguageAssistance = true
            editorProhibitsPersonalizedLearning = true
            suggestionsSuppressed = true
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
        keyboardView?.setToolbarStyle(typingSettings.toolbarStyle)
        keyboardView?.setKeyPressHapticsEnabled(typingSettings.hapticFeedbackEnabled)
        keyboardView?.setSwipeTypingEnabled(
            !sensitiveInput &&
                !editorSuppressesLanguageAssistance &&
                typingSettings.swipeTypingEnabled,
        )
    }

    private fun resetEditorSession() {
        shifted = false
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
            val correction =
                QuillGrammarModel.boundaryCorrection(prefix, contextHistory)
                    ?: suggestionEngine.bestAutocorrection(
                        word = prefix,
                        dictionary = activeDictionary(),
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

    private fun formatPredictionCase(candidate: String): String =
        if (sentenceStartPending) {
            candidate.replaceFirstChar { first ->
                if (first.isLowerCase()) first.titlecase() else first.toString()
            }
        } else {
            candidate
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

        val contextHistory = transientContextHistory(
            excludeCurrentComposingWord = composingWord.isNotEmpty(),
        )

        presentedSuggestions = when {
            composingWord.isNotEmpty() && typingSettings.suggestionsEnabled ->
                suggestionEngine.suggest(
                    prefix = composingWord.toString(),
                    dictionary = activeDictionary(),
                    contextualPredictions = contextualPredictions(contextHistory, limit = 8),
                ).map(::formatCandidateCase)

            composingWord.isEmpty() && typingSettings.predictionsEnabled ->
                predictionCandidates(contextHistory = contextHistory).map(::formatPredictionCase)

            else -> emptyList()
        }.take(3)

        keyboardView?.setSuggestions(presentedSuggestions)
    }

    private fun activeDictionary(): List<String> {
        if (!personalizationAllowed()) return QuillLexicon.expandedEnglish
        return buildList {
            addAll(learningStore.learnedWords())
            addAll(QuillLexicon.expandedEnglish)
        }.distinctBy { it.lowercase() }
    }

    private fun activeSwipeDictionary(): List<String> {
        if (!personalizationAllowed()) return QuillLexicon.swipeEnglish
        return buildList {
            addAll(learningStore.learnedWords())
            addAll(QuillLexicon.swipeEnglish)
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
        return (learned + builtIn)
            .distinctBy { it.lowercase() }
            .take(limit)
    }

    private fun contextualPredictions(
        contextHistory: List<String>,
        limit: Int,
    ): List<String> =
        (
            QuillGrammarModel.predict(contextHistory, limit = maxOf(limit, 8)) +
                QuillPredictionModel.predict(contextHistory, limit = maxOf(limit, 8))
        )
            .distinctBy { it.lowercase() }
            .take(limit)

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
        const val MAX_CONTEXT_WORDS = 4
        const val MAX_PREDICTION_HISTORY_WORDS = 2
        const val SWIPE_DECODE_CANDIDATE_POOL = 12
        val AUTOCORRECT_BOUNDARIES = setOf(".", ",", "!", "?", ";", ":")
        val SENTENCE_ENDINGS = setOf(".", "!", "?")
    }
}
