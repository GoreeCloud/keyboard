package com.goreecloud.keyboard

import android.content.Context

internal enum class KeyboardKeyHeight {
    COMPACT,
    STANDARD,
    TALL,
}

internal enum class KeyboardToolbarStyle {
    ICONS_ONLY,
    ICONS_WITH_LABELS,
}

internal data class KeyboardTypingSettings(
    val keyHeight: KeyboardKeyHeight = KeyboardKeyHeight.COMPACT,
    val toolbarStyle: KeyboardToolbarStyle = KeyboardToolbarStyle.ICONS_ONLY,
    val swipeTypingEnabled: Boolean = true,
    val suggestionsEnabled: Boolean = true,
    val autocorrectEnabled: Boolean = true,
    val predictionsEnabled: Boolean = true,
    val autoCapitalizeEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val learnFromTypingEnabled: Boolean = false,
)

/**
 * Device-local, non-portable preferences for Keyboard interaction and presentation.
 *
 * These settings contain no typed text, editor contents, learned words, clipboard data,
 * account identifiers, telemetry, or network-derived state. Optional learned language data is
 * isolated in [KeyboardLearningStore] and is disabled by default.
 */
internal class KeyboardSettingsStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun load(): KeyboardTypingSettings = KeyboardTypingSettings(
        keyHeight = enumPreference(KEY_HEIGHT, KeyboardKeyHeight.COMPACT),
        toolbarStyle = enumPreference(TOOLBAR_STYLE, KeyboardToolbarStyle.ICONS_ONLY),
        swipeTypingEnabled = preferences.getBoolean(SWIPE_TYPING, true),
        suggestionsEnabled = preferences.getBoolean(SUGGESTIONS, true),
        autocorrectEnabled = preferences.getBoolean(AUTOCORRECT, true),
        predictionsEnabled = preferences.getBoolean(PREDICTIONS, true),
        autoCapitalizeEnabled = preferences.getBoolean(AUTO_CAPITALIZE, true),
        hapticFeedbackEnabled = preferences.getBoolean(HAPTIC_FEEDBACK, true),
        learnFromTypingEnabled = preferences.getBoolean(LEARN_FROM_TYPING, false),
    )

    fun setKeyHeight(value: KeyboardKeyHeight) {
        preferences.edit().putString(KEY_HEIGHT, value.name).apply()
    }

    fun setToolbarStyle(value: KeyboardToolbarStyle) {
        preferences.edit().putString(TOOLBAR_STYLE, value.name).apply()
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

    fun setHapticFeedbackEnabled(value: Boolean) {
        preferences.edit().putBoolean(HAPTIC_FEEDBACK, value).apply()
    }

    fun setLearnFromTypingEnabled(value: Boolean) {
        preferences.edit().putBoolean(LEARN_FROM_TYPING, value).apply()
    }

    private inline fun <reified T : Enum<T>> enumPreference(key: String, fallback: T): T =
        runCatching {
            enumValueOf<T>(preferences.getString(key, fallback.name) ?: fallback.name)
        }.getOrDefault(fallback)

    private companion object {
        const val PREFERENCES_NAME = "goreecloud_keyboard_runtime_settings"
        const val KEY_HEIGHT = "key_height"
        const val TOOLBAR_STYLE = "toolbar_style"
        const val SWIPE_TYPING = "swipe_typing"
        const val SUGGESTIONS = "suggestions"
        const val AUTOCORRECT = "autocorrect"
        const val PREDICTIONS = "predictions"
        const val AUTO_CAPITALIZE = "auto_capitalize"
        const val HAPTIC_FEEDBACK = "haptic_feedback"
        const val LEARN_FROM_TYPING = "learn_from_typing"
    }
}
