package com.goreecloud.keyboard

internal object KeyboardNumberRowPolicy {
    fun isVisible(settings: KeyboardTypingSettings, sensitiveInput: Boolean): Boolean =
        settings.numberRowEnabled ||
            (sensitiveInput && settings.numberRowInSensitiveFieldsEnabled)
}

internal object DoubleSpacePeriodPolicy {
    /**
     * The first Space has already been committed when this policy runs. Replace it only when the
     * character before that Space is an ordinary letter or digit. Punctuation, emoji, whitespace,
     * and unknown/truncated context fail closed to normal Space behavior.
     */
    fun shouldReplacePreviousSpace(textBeforeCursor: CharSequence?): Boolean {
        val value = textBeforeCursor?.toString() ?: return false
        if (value.length < 2 || value.last() != ' ') return false
        return value[value.length - 2].isLetterOrDigit()
    }
}
