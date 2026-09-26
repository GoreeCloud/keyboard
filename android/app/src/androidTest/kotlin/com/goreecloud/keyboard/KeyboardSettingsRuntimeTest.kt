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
            store.setSuggestionsEnabled(false)
            store.setAutocorrectEnabled(false)
            store.setPredictionsEnabled(false)
            store.setAutoCapitalizeEnabled(false)
            store.setHapticFeedbackEnabled(false)
            store.setLearnFromTypingEnabled(true)

            val changed = store.load()
            assertFalse(changed.swipeTypingEnabled)
            assertFalse(changed.suggestionsEnabled)
            assertFalse(changed.autocorrectEnabled)
            assertFalse(changed.predictionsEnabled)
            assertFalse(changed.autoCapitalizeEnabled)
            assertFalse(changed.hapticFeedbackEnabled)
            assertTrue(changed.learnFromTypingEnabled)
        } finally {
            // Restore Development defaults so this test cannot affect later IME runtime tests.
            store.setKeyHeight(KeyboardKeyHeight.COMPACT)
            store.setToolbarStyle(KeyboardToolbarStyle.ICONS_ONLY)
            store.setSwipeTypingEnabled(true)
            store.setSuggestionsEnabled(true)
            store.setAutocorrectEnabled(true)
            store.setPredictionsEnabled(true)
            store.setAutoCapitalizeEnabled(true)
            store.setHapticFeedbackEnabled(true)
            store.setLearnFromTypingEnabled(false)
        }

        val defaults = store.load()
        assertEquals(KeyboardKeyHeight.COMPACT, defaults.keyHeight)
        assertEquals(KeyboardToolbarStyle.ICONS_ONLY, defaults.toolbarStyle)
        assertTrue(defaults.swipeTypingEnabled)
        assertTrue(defaults.suggestionsEnabled)
        assertTrue(defaults.autocorrectEnabled)
        assertTrue(defaults.predictionsEnabled)
        assertTrue(defaults.autoCapitalizeEnabled)
        assertTrue("Key vibration is enabled by default", defaults.hapticFeedbackEnabled)
        assertFalse(
            "Learning from typed words is privacy-sensitive and must be off by default",
            defaults.learnFromTypingEnabled,
        )
    }
}
