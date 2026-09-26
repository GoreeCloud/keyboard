package com.goreecloud.keyboard

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeyboardLearningStoreRuntimeTest {
    @Test
    fun storesOnlyBoundedWordAndBigramCountersAndCanBeCleared() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val store = KeyboardLearningStore(context)

        try {
            store.clear()
            store.record("GoreeCloud", null)
            store.record("Keyboard", "GoreeCloud")
            store.record("Keyboard", "GoreeCloud")
            store.record("settings", "Keyboard")
            store.record("person@example.com", "settings")
            store.record("12345", "settings")

            assertTrue("Learned dictionary should include normalized ordinary words", "keyboard" in store.learnedWords())
            assertFalse("Email-like tokens must not be retained", "person@example.com" in store.learnedWords())
            assertFalse("Numeric tokens must not be retained", "12345" in store.learnedWords())
            assertEquals(
                listOf("keyboard"),
                store.predictNext(listOf("goreecloud"), limit = 1),
            )
            assertTrue(store.learnedWordCount() >= 3)
        } finally {
            store.clear()
        }

        assertEquals(0, store.learnedWordCount())
        assertEquals(emptyList<String>(), store.learnedWords())
    }
}
