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

internal enum class KeyboardLongPressDelay {
    FAST,
    SYSTEM,
    RELAXED,
}

internal data class KeyboardTypingSettings(
    val keyHeight: KeyboardKeyHeight = KeyboardKeyHeight.COMPACT,
    val toolbarStyle: KeyboardToolbarStyle = KeyboardToolbarStyle.ICONS_ONLY,
    val swipeTypingEnabled: Boolean = true,
    val swipeTrailEnabled: Boolean = true,
    val spacebarCursorControlEnabled: Boolean = true,
    val suggestionsEnabled: Boolean = true,
    val autocorrectEnabled: Boolean = true,
    val predictionsEnabled: Boolean = true,
    val autoCapitalizeEnabled: Boolean = true,
    val doubleSpacePeriodEnabled: Boolean = true,
    val numberRowEnabled: Boolean = true,
    val numberRowInSensitiveFieldsEnabled: Boolean = true,
    val emojiToolbarEnabled: Boolean = true,
    val longPressHintsEnabled: Boolean = true,
    val longPressDelay: KeyboardLongPressDelay = KeyboardLongPressDelay.SYSTEM,
    val hapticFeedbackEnabled: Boolean = true,
    val keyPressSoundEnabled: Boolean = false,
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
        swipeTrailEnabled = preferences.getBoolean(SWIPE_TRAIL, true),
        spacebarCursorControlEnabled = preferences.getBoolean(SPACEBAR_CURSOR_CONTROL, true),
        suggestionsEnabled = preferences.getBoolean(SUGGESTIONS, true),
        autocorrectEnabled = preferences.getBoolean(AUTOCORRECT, true),
        predictionsEnabled = preferences.getBoolean(PREDICTIONS, true),
        autoCapitalizeEnabled = preferences.getBoolean(AUTO_CAPITALIZE, true),
        doubleSpacePeriodEnabled = preferences.getBoolean(DOUBLE_SPACE_PERIOD, true),
        numberRowEnabled = preferences.getBoolean(NUMBER_ROW, true),
        numberRowInSensitiveFieldsEnabled = preferences.getBoolean(NUMBER_ROW_SENSITIVE, true),
        emojiToolbarEnabled = preferences.getBoolean(EMOJI_TOOLBAR, true),
        longPressHintsEnabled = preferences.getBoolean(LONG_PRESS_HINTS, true),
        longPressDelay = enumPreference(LONG_PRESS_DELAY, KeyboardLongPressDelay.SYSTEM),
        hapticFeedbackEnabled = preferences.getBoolean(HAPTIC_FEEDBACK, true),
        keyPressSoundEnabled = preferences.getBoolean(KEY_PRESS_SOUND, false),
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

    fun setSwipeTrailEnabled(value: Boolean) {
        preferences.edit().putBoolean(SWIPE_TRAIL, value).apply()
    }

    fun setSpacebarCursorControlEnabled(value: Boolean) {
        preferences.edit().putBoolean(SPACEBAR_CURSOR_CONTROL, value).apply()
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

    fun setDoubleSpacePeriodEnabled(value: Boolean) {
        preferences.edit().putBoolean(DOUBLE_SPACE_PERIOD, value).apply()
    }

    fun setNumberRowEnabled(value: Boolean) {
        preferences.edit().putBoolean(NUMBER_ROW, value).apply()
    }

    fun setNumberRowInSensitiveFieldsEnabled(value: Boolean) {
        preferences.edit().putBoolean(NUMBER_ROW_SENSITIVE, value).apply()
    }

    fun setEmojiToolbarEnabled(value: Boolean) {
        preferences.edit().putBoolean(EMOJI_TOOLBAR, value).apply()
    }

    fun setLongPressHintsEnabled(value: Boolean) {
        preferences.edit().putBoolean(LONG_PRESS_HINTS, value).apply()
    }

    fun setLongPressDelay(value: KeyboardLongPressDelay) {
        preferences.edit().putString(LONG_PRESS_DELAY, value.name).apply()
    }

    fun setHapticFeedbackEnabled(value: Boolean) {
        preferences.edit().putBoolean(HAPTIC_FEEDBACK, value).apply()
    }

    fun setKeyPressSoundEnabled(value: Boolean) {
        preferences.edit().putBoolean(KEY_PRESS_SOUND, value).apply()
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
        const val SWIPE_TRAIL = "swipe_trail"
        const val SPACEBAR_CURSOR_CONTROL = "spacebar_cursor_control"
        const val SUGGESTIONS = "suggestions"
        const val AUTOCORRECT = "autocorrect"
        const val PREDICTIONS = "predictions"
        const val AUTO_CAPITALIZE = "auto_capitalize"
        const val DOUBLE_SPACE_PERIOD = "double_space_period"
        const val NUMBER_ROW = "number_row"
        const val NUMBER_ROW_SENSITIVE = "number_row_sensitive"
        const val EMOJI_TOOLBAR = "emoji_toolbar"
        const val LONG_PRESS_HINTS = "long_press_hints"
        const val LONG_PRESS_DELAY = "long_press_delay"
        const val HAPTIC_FEEDBACK = "haptic_feedback"
        const val KEY_PRESS_SOUND = "key_press_sound"
        const val LEARN_FROM_TYPING = "learn_from_typing"
    }
}
