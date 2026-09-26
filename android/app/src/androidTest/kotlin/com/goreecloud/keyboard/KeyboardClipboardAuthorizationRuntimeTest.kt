package com.goreecloud.keyboard

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeyboardClipboardAuthorizationRuntimeTest {
    @Test
    fun askGrantExpiresWhenPanelClosesAndInputViewHides() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val packageName = "example.ask.session"
        val preferences = KeyboardClipboardPreferences(context).apply {
            setHistoryEnabled(false)
            setPolicy(packageName, ClipboardAppPolicy.ASK)
        }
        val store = EncryptedClipboardHistoryStore(context)
        store.clearAll()

        val controller = KeyboardClipboardController(
            context = context,
            preferences = preferences,
            historyStore = store,
        )
        controller.updateEditor(packageName = packageName, sensitive = false)
        controller.onInputViewVisible()

        assertTrue(controller.snapshot().requiresAuthorization)

        controller.authorizeOnce()
        assertFalse(controller.snapshot().requiresAuthorization)

        controller.closePanel()
        assertTrue(controller.snapshot().requiresAuthorization)

        controller.authorizeOnce()
        assertFalse(controller.snapshot().requiresAuthorization)

        controller.onInputViewHidden()
        controller.onInputViewVisible()
        assertTrue(controller.snapshot().requiresAuthorization)

        controller.onInputViewHidden()
        store.clearAll()
    }
}
