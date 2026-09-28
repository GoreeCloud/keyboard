package com.goreecloud.keyboard

/**
 * Keeps audible key feedback out of password and other sensitive editors.
 *
 * Haptics remain governed by their user preference because they do not broadcast
 * keystroke timing into the surrounding environment in the same way key sounds do.
 */
internal object KeyboardKeyFeedbackPolicy {
    fun keyPressSoundEnabled(
        settings: KeyboardTypingSettings,
        sensitiveInput: Boolean,
    ): Boolean = settings.keyPressSoundEnabled && !sensitiveInput
}
