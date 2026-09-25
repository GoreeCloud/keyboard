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
        // Editor authority changes before the input view is necessarily shown. Reset the privacy
        // policy and transient composing state here so an editor switch cannot temporarily retain
        // the previous field's sensitive/no-suggestions decision while the IME UI is hidden.
        beginEditorSession(attribute)
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        // Android can show/restart the input view after onStartInput. Re-evaluate from the current
        // EditorInfo rather than trusting cached policy from a previous visible field.
        beginEditorSession(info)
        keyboardView?.setLayer(KeyboardLayer.LETTERS)
        keyboardView?.setShifted(false)
        keyboardView?.setGlazeV16PresentationSignals(currentGlazeV16PresentationSignals())
        updateSuggestions()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        // onFinishInput is Android's editor-session boundary. Do not rely only on the input view
        // being hidden to clear transient composing context or the previous editor's policy state.
        resetEditorSession()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        // The view can finish independently of the editor session. Clear the same transient state
        // here as a defense-in-depth UI boundary; onStartInputView will reapply current policy.
        resetEditorSession()
    }

    override fun onDestroy() {
        composingWord.clear()
        composingCaptureExhausted = false
        presentedSuggestions = emptyList()
        keyboardView = null
        super.onDestroy()
    }

    override fun onText(value: String) {
        if (value.isEmpty()) return
        val isLetterText = value.codePoints().allMatch { Character.isLetter(it) }
        val output = if (shifted && isLetterText) value.uppercase() else value
        currentInputConnection?.commitText(output, 1)
        if (!suggestionsSuppressed) {
            if (isLetterText) {
                val normalized = output.lowercase()
                if (!composingCaptureExhausted &&
                    SuggestionCapturePolicy.canAppend(composingWord.toString(), normalized)
                ) {
                    composingWord.append(normalized)
                } else {
                    // Keep committed typing authoritative in the host, but stop retaining local
                    // mid-word context once the bounded suggestion observation window is exhausted.
                    composingWord.clear()
                    composingCaptureExhausted = true
                }
            } else {
                clearComposingBoundary()
            }
        }
        updateSuggestions()

        if (shifted) {
            shifted = false
            keyboardView?.setShifted(false)
        }
    }

    override fun onSpace() {
        currentInputConnection?.commitText(" ", 1)
        clearComposingBoundary()
        updateSuggestions()
    }

    override fun onSwipe(keyPath: List<String>) {
        if (sensitiveInput || keyPath.isEmpty()) return
        val connection = currentInputConnection ?: return
        val candidate = swipeTypingEngine.decode(
            keyPath = keyPath,
            dictionary = QuillLexicon.english,
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
        shifted = false
        keyboardView?.setShifted(false)
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
            // The bounded lookbehind did not establish a safe complete prior text unit. Do not
            // partially delete host text or guess a replacement composing prefix.
            failClosedBackspaceContext()
            return
        }
        connection.deleteSurroundingTextInCodePoints(deleteCodePoints, 0)

        if (!suggestionsSuppressed && !composingCaptureExhausted && composingWord.isNotEmpty()) {
            val lastCodePointStart = composingWord.offsetByCodePoints(composingWord.length, -1)
            composingWord.delete(lastCodePointStart, composingWord.length)
        }
        // If capture was exhausted, do not guess that backspace reconstructed a complete prefix.
        // Stay suppressed until a word/editor boundary provides a clean local observation start.
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
        // Keep a second sensitive-input check here so a future policy regression cannot turn
        // candidate acceptance into surrounding-text access for a protected editor.
        if (suggestionsSuppressed || sensitiveInput || composingCaptureExhausted) return
        // The callback value itself is not replacement authority. It must still be one of the exact
        // candidates presented for this editor session; a stale/forged callback cannot delete text.
        if (!SuggestionCommitPolicy.isPresentedCandidate(value, presentedSuggestions)) return
        val connection = currentInputConnection ?: return
        val prefix = composingWord.toString()
        val prefixCodePoints = prefix.codePointCount(0, prefix.length)
        if (prefixCodePoints > 0) {
            val beforeCursor = connection.getTextBeforeCursor(prefix.length, 0)
            if (!SuggestionCommitPolicy.matchesExpectedPrefix(prefix, beforeCursor)) {
                // The host editor is authoritative for cursor/text state. If it no longer matches
                // the local candidate prefix, do not delete or commit against stale context, and do
                // not start a new mid-word prefix until the user reaches a clean boundary.
                composingWord.clear()
                composingCaptureExhausted = true
                presentedSuggestions = emptyList()
                keyboardView?.setSuggestions(emptyList())
                return
            }
            connection.deleteSurroundingTextInCodePoints(prefixCodePoints, 0)
        }
        connection.commitText("$value ", 1)
        clearComposingBoundary()
        shifted = false
        keyboardView?.setShifted(false)
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
        composingCaptureExhausted = false
        // Presentation belongs to the previous editor until this exact session is evaluated. Clear
        // both the service-side acceptance set and any visible strip before granting new candidates.
        presentedSuggestions = emptyList()
        keyboardView?.setSuggestions(emptyList())
        if (info == null) {
            // Unknown editor metadata must not silently receive ordinary-field privileges. Treat it
            // as sensitive so backspace avoids surrounding-text inspection and suggestions remain
            // suppressed until Android provides a concrete EditorInfo for the active session.
            sensitiveInput = true
            suggestionsSuppressed = true
            keyboardView?.setSwipeTypingEnabled(false)
            return
        }

        val inputType = info.inputType
        sensitiveInput = InputPrivacyClassifier.isSensitive(inputType)
        suggestionsSuppressed = EditorSuggestionPolicy.shouldSuppress(inputType, info.imeOptions)
        keyboardView?.setSwipeTypingEnabled(!sensitiveInput)
    }

    private fun resetEditorSession() {
        shifted = false
        // With no active editor, retain the most restrictive transient policy. A subsequent concrete
        // EditorInfo is the only authority that may enable ordinary-field composing/surrounding text.
        sensitiveInput = true
        suggestionsSuppressed = true
        composingWord.clear()
        composingCaptureExhausted = false
        presentedSuggestions = emptyList()
        keyboardView?.setLayer(KeyboardLayer.LETTERS)
        keyboardView?.setShifted(false)
        keyboardView?.setSwipeTypingEnabled(false)
        // No active editor owns suggestion presentation after teardown. Clear the visible strip
        // rather than repopulating bootstrap candidates until a subsequent editor session starts.
        keyboardView?.setSuggestions(emptyList())
    }

    private fun clearComposingBoundary() {
        composingWord.clear()
        composingCaptureExhausted = false
    }

    private fun failClosedBackspaceContext() {
        composingWord.clear()
        composingCaptureExhausted = true
        presentedSuggestions = emptyList()
        keyboardView?.setSuggestions(emptyList())
    }

    private fun updateSuggestions() {
        if (suggestionsSuppressed || composingCaptureExhausted) {
            presentedSuggestions = emptyList()
            keyboardView?.setSuggestions(emptyList())
            return
        }
        presentedSuggestions = if (composingWord.isEmpty()) {
            QuillLexicon.starterSuggestions
        } else {
            suggestionEngine.suggest(
                prefix = composingWord.toString(),
                dictionary = QuillLexicon.english,
            )
        }.take(3)
        keyboardView?.setSuggestions(presentedSuggestions)
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
    }
}
