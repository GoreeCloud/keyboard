package com.goreecloud.keyboard

import android.animation.ValueAnimator
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.view.inputmethod.EditorInfo

class KeyboardService : InputMethodService(), KeyboardView.Listener {
    private var shifted = false

    // No active editor has granted ordinary-field behavior yet. Keep the process default fail-closed
    // until onStartInput/onStartInputView provide concrete EditorInfo for the current session.
    private var sensitiveInput = true
    private var suggestionsSuppressed = true
    private var composingCaptureExhausted = false
    private var keyboardView: KeyboardView? = null
    private val suggestionEngine = SuggestionEngine()
    private val swipeTypingEngine = SwipeTypingEngine()
    private val composingWord = StringBuilder()
    private var composingStartsCapitalized = false
    private var presentedSuggestions: List<String> = emptyList()

    override fun onCreateInputView(): View {
        return KeyboardView(this).also { view ->
            keyboardView = view
            view.listener = this
            view.setLayer(KeyboardLayer.LETTERS)
            view.setShifted(shifted)
            view.setSwipeTypingEnabled(!sensitiveInput)
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
        keyboardView?.setShifted(false)
        keyboardView?.setGlazeV16PresentationSignals(currentGlazeV16PresentationSignals())
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
        presentedSuggestions = emptyList()
        keyboardView = null
        super.onDestroy()
    }

    override fun onText(value: String) {
        if (value.isEmpty()) return

        val isLetterText = value.codePoints().allMatch { Character.isLetter(it) }
        val output = if (shifted && isLetterText) value.uppercase() else value

        if (!isLetterText && value in AUTOCORRECT_BOUNDARIES) {
            commitBoundary(value)
            resetOneShotShift()
            return
        }

        currentInputConnection?.commitText(output, 1)

        if (!suggestionsSuppressed) {
            if (isLetterText) {
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
        commitBoundary(" ")
        resetOneShotShift()
    }

    override fun onSwipe(keyPath: List<String>) {
        if (sensitiveInput || keyPath.isEmpty()) return
        val connection = currentInputConnection ?: return

        val candidate = swipeTypingEngine.decode(
            keyPath = keyPath,
            dictionary = QuillLexicon.expandedEnglish,
            limit = 1,
        ).firstOrNull() ?: return

        val output = if (shifted) {
            candidate.replaceFirstChar { first ->
                if (first.isLowerCase()) first.titlecase() else first.toString()
            }
        } else {
            candidate
        }

        connection.commitText("$output ", 1)
        clearComposingBoundary()
        resetOneShotShift()
        updateSuggestions()
    }

    override fun onBackspace() {
        val connection = currentInputConnection ?: return
        val deleteCodePoints = if (sensitiveInput) {
            1
        } else {
            val beforeCursor = connection.getTextBeforeCursor(BACKSPACE_LOOKBEHIND_UTF16, 0)
                ?: return failClosedBackspaceContext()
            TextDeletion.previousTextUnitCodePointCount(
                textBeforeCursor = beforeCursor,
                contextMayBeTruncated = beforeCursor.length >= BACKSPACE_LOOKBEHIND_UTF16,
            )
        }

        if (deleteCodePoints <= 0) {
            failClosedBackspaceContext()
            return
        }

        connection.deleteSurroundingTextInCodePoints(deleteCodePoints, 0)

        if (!suggestionsSuppressed && !composingCaptureExhausted && composingWord.isNotEmpty()) {
            val lastCodePointStart = composingWord.offsetByCodePoints(composingWord.length, -1)
            composingWord.delete(lastCodePointStart, composingWord.length)
            if (composingWord.isEmpty()) composingStartsCapitalized = false
        }

        updateSuggestions()
    }

    override fun onEnter() {
        val connection = currentInputConnection ?: return
        connection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        connection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        clearComposingBoundary()
        updateSuggestions()
    }

    override fun onShift() {
        shifted = !shifted
        keyboardView?.setShifted(shifted)
    }

    override fun onSuggestion(value: String) {
        if (suggestionsSuppressed || sensitiveInput || composingCaptureExhausted) return
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
        clearComposingBoundary()
        resetOneShotShift()
        updateSuggestions()
    }

    override fun onLayerChanged(layer: KeyboardLayer) {
        shifted = false
        clearComposingBoundary()
        keyboardView?.setShifted(false)
        updateSuggestions()
    }

    private fun beginEditorSession(info: EditorInfo?) {
        shifted = false
        composingWord.clear()
        composingStartsCapitalized = false
        composingCaptureExhausted = false
        presentedSuggestions = emptyList()
        keyboardView?.setSuggestions(emptyList())
        keyboardView?.setSwipeTypingEnabled(false)

        if (info == null) {
            // Unknown editor metadata must not silently receive ordinary-field privileges. Treat it
            // as sensitive so backspace avoids surrounding-text inspection and suggestions remain
            // suppressed until Android provides a concrete EditorInfo for the active session.
            sensitiveInput = true
            suggestionsSuppressed = true
            return
        }

        val inputType = info.inputType
        sensitiveInput = InputPrivacyClassifier.isSensitive(inputType)
        suggestionsSuppressed = EditorSuggestionPolicy.shouldSuppress(inputType, info.imeOptions)
        keyboardView?.setSwipeTypingEnabled(!sensitiveInput)
    }

    private fun resetEditorSession() {
        shifted = false
        sensitiveInput = true
        suggestionsSuppressed = true
        composingWord.clear()
        composingStartsCapitalized = false
        composingCaptureExhausted = false
        presentedSuggestions = emptyList()
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
        var committedCorrection = false

        if (!suggestionsSuppressed &&
            !sensitiveInput &&
            !composingCaptureExhausted &&
            composingWord.isNotEmpty()
        ) {
            val prefix = composingWord.toString()
            val correction = suggestionEngine.bestAutocorrection(
                word = prefix,
                dictionary = QuillLexicon.expandedEnglish,
            )

            if (correction != null) {
                val beforeCursor = connection.getTextBeforeCursor(prefix.length, 0)
                if (SuggestionCommitPolicy.matchesExpectedPrefix(prefix, beforeCursor)) {
                    val prefixCodePoints = prefix.codePointCount(0, prefix.length)
                    connection.deleteSurroundingTextInCodePoints(prefixCodePoints, 0)
                    connection.commitText(formatCandidateCase(correction) + separator, 1)
                    committedCorrection = true
                } else {
                    composingCaptureExhausted = true
                }
            }
        }

        if (!committedCorrection) {
            connection.commitText(separator, 1)
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
        composingStartsCapitalized = false
        composingCaptureExhausted = true
        presentedSuggestions = emptyList()
        keyboardView?.setSuggestions(emptyList())
    }

    private fun updateSuggestions() {
        if (suggestionsSuppressed || composingCaptureExhausted || composingWord.isEmpty()) {
            presentedSuggestions = emptyList()
            keyboardView?.setSuggestions(emptyList())
            return
        }

        presentedSuggestions = suggestionEngine.suggest(
            prefix = composingWord.toString(),
            dictionary = QuillLexicon.expandedEnglish,
        ).map(::formatCandidateCase).take(3)

        keyboardView?.setSuggestions(presentedSuggestions)
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

    private companion object {
        const val BACKSPACE_LOOKBEHIND_UTF16 = 64
        val AUTOCORRECT_BOUNDARIES = setOf(".", ",", "!", "?", ";", ":")
    }
}
