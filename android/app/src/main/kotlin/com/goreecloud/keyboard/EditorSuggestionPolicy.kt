package com.goreecloud.keyboard

import android.text.InputType

/**
 * Determines whether GoreeCloud Keyboard may collect transient composing-word context or display
 * deterministic local suggestion candidates for the current editor.
 *
 * Password/sensitive fields remain governed by [InputPrivacyClassifier]. Ordinary text editors may
 * explicitly request no suggestions through Android's TYPE_TEXT_FLAG_NO_SUGGESTIONS flag.
 *
 * IME_FLAG_NO_PERSONALIZED_LEARNING is deliberately not a suggestion-suppression flag here. The
 * current Quill path does not persist a learned user model, so that editor request is honored by the
 * existing no-learning architecture without unnecessarily disabling transient, on-device
 * deterministic suggestions.
 */
object EditorSuggestionPolicy {
    fun shouldSuppress(inputType: Int, imeOptions: Int = 0): Boolean {
        if (InputPrivacyClassifier.isSensitive(inputType)) return true

        val inputClass = inputType and InputType.TYPE_MASK_CLASS
        val flags = inputType and InputType.TYPE_MASK_FLAGS
        return inputClass == InputType.TYPE_CLASS_TEXT &&
            flags and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS != 0
    }
}
