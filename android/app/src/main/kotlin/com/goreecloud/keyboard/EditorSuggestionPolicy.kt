package com.goreecloud.keyboard

import android.text.InputType
import android.view.inputmethod.EditorInfo

/**
 * Determines whether GoreeCloud Keyboard may collect transient composing-word context or display
 * deterministic local suggestion candidates for the current editor.
 *
 * Password/sensitive fields remain governed by [InputPrivacyClassifier]. Ordinary text editors may
 * explicitly request no suggestions through Android's TYPE_TEXT_FLAG_NO_SUGGESTIONS flag.
 *
 * IME_FLAG_NO_PERSONALIZED_LEARNING does not suppress deterministic transient suggestions, but it
 * does prohibit both collection into and use of the optional persisted local personalization store.
 */
object EditorSuggestionPolicy {
    fun shouldSuppress(inputType: Int, imeOptions: Int = 0): Boolean {
        if (InputPrivacyClassifier.isSensitive(inputType)) return true

        val inputClass = inputType and InputType.TYPE_MASK_CLASS
        val flags = inputType and InputType.TYPE_MASK_FLAGS
        return inputClass == InputType.TYPE_CLASS_TEXT &&
            flags and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS != 0
    }

    /**
     * Gesture typing is direct user input, not a suggestion surface. An ordinary editor may request
     * TYPE_TEXT_FLAG_NO_SUGGESTIONS without losing deliberate local swipe entry. Sensitive fields
     * remain fail-closed and never receive gesture typing.
     */
    fun shouldSuppressGestureTyping(inputType: Int): Boolean =
        InputPrivacyClassifier.isSensitive(inputType)

    fun prohibitsPersonalizedLearning(imeOptions: Int): Boolean =
        imeOptions and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING != 0
}
