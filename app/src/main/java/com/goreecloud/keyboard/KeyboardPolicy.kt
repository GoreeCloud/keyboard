package com.goreecloud.keyboard

import android.text.InputType
import android.view.inputmethod.EditorInfo

/** Only editor metadata is inspected; never collect entered text or candidate history. */
enum class KeyboardKind { LETTERS, NUMBERS, PHONE, EMAIL, URL }

data class KeyboardContext(
    val kind: KeyboardKind,
    val sensitive: Boolean,
    val action: Int
)

object KeyboardPolicy {
    fun forEditor(inputType: Int, imeOptions: Int): KeyboardContext {
        val type = inputType and InputType.TYPE_MASK_CLASS
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        val isPassword = when (type) {
            InputType.TYPE_CLASS_TEXT -> variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
            InputType.TYPE_CLASS_NUMBER -> variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
            else -> false
        }
        val kind = when {
            type == InputType.TYPE_CLASS_PHONE -> KeyboardKind.PHONE
            type == InputType.TYPE_CLASS_NUMBER || type == InputType.TYPE_CLASS_DATETIME -> KeyboardKind.NUMBERS
            type == InputType.TYPE_CLASS_TEXT && (variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS) -> KeyboardKind.EMAIL
            type == InputType.TYPE_CLASS_TEXT && variation == InputType.TYPE_TEXT_VARIATION_URI -> KeyboardKind.URL
            else -> KeyboardKind.LETTERS
        }
        return KeyboardContext(
            kind = kind,
            sensitive = isPassword || (inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS != 0),
            action = imeOptions and EditorInfo.IME_MASK_ACTION
        )
    }
}
