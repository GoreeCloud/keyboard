package com.goreecloud.keyboard

import android.content.Context

internal enum class KeyboardKeyHeight {
    COMPACT,
    STANDARD,
    TALL,
}

internal data class KeyboardTypingSettings(
    val keyHeight: KeyboardKeyHeight = KeyboardKeyHeight.COMPACT,
    val swipeTypingEnabled: Boolean = true,
    val suggestionsEnabled: Boolean = true,
    val autocorrectEnabled: Boolean = true,
    val predictionsEnabled: Boolean = true,
    val autoCapitalizeEnabled: Boolean = true,
)

/**
 * Device-local, non-portable preferences for Keyboard interaction and presentation.
 *
 * These settings contain no typed text, editor contents, learned words, clipboard data,
 * account identifiers, telemetry, or network-derived state.
 */
internal class KeyboardSettingsStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun load(): KeyboardTypingSettings = KeyboardTypingSettings(
        keyHeight = runCatching {
            KeyboardKeyHeight.valueOf(
                preferences.getString(KEY_HEIGHT, KeyboardKeyHeight.COMPACT.name)
                    ?: KeyboardKeyHeight.COMPACT.name,
            )
        }.getOrDefault(KeyboardKeyHeight.COMPACT),
        swipeTypingEnabled = preferences.getBoolean(SWIPE_TYPING, true),
        suggestionsEnabled = preferences.getBoolean(SUGGESTIONS, true),
        autocorrectEnabled = preferences.getBoolean(AUTOCORRECT, true),
        predictionsEnabled = preferences.getBoolean(PREDICTIONS, true),
        autoCapitalizeEnabled = preferences.getBoolean(AUTO_CAPITALIZE, true),
    )

    fun setKeyHeight(value: KeyboardKeyHeight) {
        preferences.edit().putString(KEY_HEIGHT, value.name).apply()
    }

    fun setSwipeTypingEnabled(value: Boolean) {
        preferences.edit().putBoolean(SWIPE_TYPING, value).apply()
    }

    fun setSuggestionsEnabled(value: Boolean) {
        preferences.edit().putBoolean(SUGGESTIONS, value).apply()
    }

    fun setAutocorrectEnabled(value: Boolean) {
        preferences.edit().putBoolean(AUTOCORRECT, value).apply()
    }

    fun setPredictionsEnabled(value: Boolean) {
        preferences.edit().putBoolean(PREDICTIONS, value).apply()
    }

    fun setAutoCapitalizeEnabled(value: Boolean) {
        preferences.edit().putBoolean(AUTO_CAPITALIZE, value).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "goreecloud_keyboard_runtime_settings"
        const val KEY_HEIGHT = "key_height"
        const val SWIPE_TYPING = "swipe_typing"
        const val SUGGESTIONS = "suggestions"
        const val AUTOCORRECT = "autocorrect"
        const val PREDICTIONS = "predictions"
        const val AUTO_CAPITALIZE = "auto_capitalize"
    }
}
