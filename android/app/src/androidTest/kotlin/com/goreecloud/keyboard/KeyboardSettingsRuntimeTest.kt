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
    fun keyHeightAndTypingControlsPersistLocally() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val store = KeyboardSettingsStore(context)

        try {
            KeyboardKeyHeight.values().forEach { height ->
                store.setKeyHeight(height)
                assertEquals(height, store.load().keyHeight)
            }

            store.setSwipeTypingEnabled(false)
            store.setSuggestionsEnabled(false)
            store.setAutocorrectEnabled(false)
            store.setPredictionsEnabled(false)
            store.setAutoCapitalizeEnabled(false)

            val disabled = store.load()
            assertFalse(disabled.swipeTypingEnabled)
            assertFalse(disabled.suggestionsEnabled)
            assertFalse(disabled.autocorrectEnabled)
            assertFalse(disabled.predictionsEnabled)
            assertFalse(disabled.autoCapitalizeEnabled)
        } finally {
            // Restore Development defaults so this instrumentation test cannot affect later IME tests.
            store.setKeyHeight(KeyboardKeyHeight.COMPACT)
            store.setSwipeTypingEnabled(true)
            store.setSuggestionsEnabled(true)
            store.setAutocorrectEnabled(true)
            store.setPredictionsEnabled(true)
            store.setAutoCapitalizeEnabled(true)
        }

        val defaults = store.load()
        assertEquals(KeyboardKeyHeight.COMPACT, defaults.keyHeight)
        assertTrue(defaults.swipeTypingEnabled)
        assertTrue(defaults.suggestionsEnabled)
        assertTrue(defaults.autocorrectEnabled)
        assertTrue(defaults.predictionsEnabled)
        assertTrue(defaults.autoCapitalizeEnabled)
    }
}
