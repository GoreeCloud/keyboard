package com.goreecloud.keyboard

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeyboardSettingsRuntimeTest {
    @Test
    fun localTypingAppearancePrivacyAndHapticControlsPersist() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val store = KeyboardSettingsStore(context)

        try {
            KeyboardKeyHeight.values().forEach { height ->
                store.setKeyHeight(height)
                assertEquals(height, store.load().keyHeight)
            }
            KeyboardToolbarStyle.values().forEach { style ->
                store.setToolbarStyle(style)
                assertEquals(style, store.load().toolbarStyle)
            }

            store.setSwipeTypingEnabled(false)
            store.setSwipeTrailEnabled(false)
            store.setSuggestionsEnabled(false)
            store.setAutocorrectEnabled(false)
            store.setPredictionsEnabled(false)
            store.setAutoCapitalizeEnabled(false)
            store.setDoubleSpacePeriodEnabled(false)
            store.setNumberRowEnabled(false)
            store.setNumberRowInSensitiveFieldsEnabled(false)
            store.setEmojiToolbarEnabled(false)
            store.setLongPressHintsEnabled(false)
            store.setLongPressDelay(KeyboardLongPressDelay.FAST)
            store.setHapticFeedbackEnabled(false)
            store.setKeyPressSoundEnabled(true)
            store.setLearnFromTypingEnabled(true)

            val changed = store.load()
            assertFalse(changed.swipeTypingEnabled)
            assertFalse(changed.swipeTrailEnabled)
            assertFalse(changed.suggestionsEnabled)
            assertFalse(changed.autocorrectEnabled)
            assertFalse(changed.predictionsEnabled)
            assertFalse(changed.autoCapitalizeEnabled)
            assertFalse(changed.doubleSpacePeriodEnabled)
            assertFalse(changed.numberRowEnabled)
            assertFalse(changed.numberRowInSensitiveFieldsEnabled)
            assertFalse(changed.emojiToolbarEnabled)
            assertFalse(changed.longPressHintsEnabled)
            assertEquals(KeyboardLongPressDelay.FAST, changed.longPressDelay)
            assertFalse(changed.hapticFeedbackEnabled)
            assertTrue(changed.keyPressSoundEnabled)
            assertTrue(changed.learnFromTypingEnabled)
        } finally {
            // Restore Development defaults so this test cannot affect later IME runtime tests.
            store.setKeyHeight(KeyboardKeyHeight.COMPACT)
            store.setToolbarStyle(KeyboardToolbarStyle.ICONS_ONLY)
            store.setSwipeTypingEnabled(true)
            store.setSwipeTrailEnabled(true)
            store.setSuggestionsEnabled(true)
            store.setAutocorrectEnabled(true)
            store.setPredictionsEnabled(true)
            store.setAutoCapitalizeEnabled(true)
            store.setDoubleSpacePeriodEnabled(true)
            store.setNumberRowEnabled(true)
            store.setNumberRowInSensitiveFieldsEnabled(true)
            store.setEmojiToolbarEnabled(true)
            store.setLongPressHintsEnabled(true)
            store.setLongPressDelay(KeyboardLongPressDelay.SYSTEM)
            store.setHapticFeedbackEnabled(true)
            store.setKeyPressSoundEnabled(false)
            store.setLearnFromTypingEnabled(false)
        }

        val defaults = store.load()
        assertEquals(KeyboardKeyHeight.COMPACT, defaults.keyHeight)
        assertEquals(KeyboardToolbarStyle.ICONS_ONLY, defaults.toolbarStyle)
        assertTrue(defaults.swipeTypingEnabled)
        assertTrue(defaults.swipeTrailEnabled)
        assertTrue(defaults.suggestionsEnabled)
        assertTrue(defaults.autocorrectEnabled)
        assertTrue(defaults.predictionsEnabled)
        assertTrue(defaults.autoCapitalizeEnabled)
        assertTrue(defaults.doubleSpacePeriodEnabled)
        assertTrue(defaults.numberRowEnabled)
        assertTrue(defaults.numberRowInSensitiveFieldsEnabled)
        assertTrue(defaults.emojiToolbarEnabled)
        assertTrue(defaults.longPressHintsEnabled)
        assertEquals(KeyboardLongPressDelay.SYSTEM, defaults.longPressDelay)
        assertTrue("Key vibration is enabled by default", defaults.hapticFeedbackEnabled)
        assertFalse("Keypress sound remains opt-in", defaults.keyPressSoundEnabled)
        assertFalse(
            "Learning from typed words is privacy-sensitive and must be off by default",
            defaults.learnFromTypingEnabled,
        )
    }
}
