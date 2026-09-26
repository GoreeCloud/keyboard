package com.goreecloud.keyboard

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeyboardClipboardRuntimeTest {
    @Test
    fun toolbarExposesRealAccessibleClipboardAction() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val view = KeyboardView(context)
        render(view)

        val target = view.accessibilityTargets()
            .firstOrNull { it.label == "Clipboard and Secure Paste" }
        assertNotNull("Clipboard must be exposed as a toolbar accessibility action", target)

        var openings = 0
        view.listener = object : KeyboardView.Listener {
            override fun onText(value: String) = Unit
            override fun onSwipe(keyPath: List<String>) = Unit
            override fun onSpace() = Unit
            override fun onBackspace() = Unit
            override fun onEnter() = Unit
            override fun onShift() = Unit
            override fun onSuggestion(value: String) = Unit
            override fun onLayerChanged(layer: KeyboardLayer) = Unit
            override fun onOpenClipboard() {
                openings += 1
            }
        }

        assertTrue(view.performAccessibilityTarget(target!!.id))
        assertEquals(1, openings)
    }

    @Test
    fun encryptedHistoryRoundTripsAndExpiresLocally() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val store = EncryptedClipboardHistoryStore(context)
        store.clearAll()

        val now = 5_000L
        val entry = store.upsert(
            text = "clipboard regression value",
            nowMillis = now,
            retentionMillis = 60_000L,
        )
        assertNotNull(entry)
        assertEquals(
            listOf("clipboard regression value"),
            store.load(nowMillis = now + 1_000L).map { it.text },
        )
        assertTrue(store.load(nowMillis = now + 60_001L).isEmpty())

        store.clearAll()
    }

    @Test
    fun clipboardPanelRendersAskAuthorizationWithoutPayloadDisclosure() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        var allowed = 0
        val panel = KeyboardClipboardPanelView(
            context = context,
            callbacks = KeyboardClipboardPanelView.Callbacks(
                onClose = {},
                onPaste = { _, _ -> },
                onPasteText = {},
                onTogglePin = {},
                onDelete = {},
                onEditSaved = { _, _ -> },
                onClearUnpinned = {},
                onHistoryEnabledChanged = {},
                onPolicyChanged = {},
                onAllowOnce = { allowed += 1 },
            ),
        )
        panel.render(
            KeyboardClipboardSnapshot(
                packageName = "example.app",
                policy = ClipboardAppPolicy.ASK,
                historyEnabled = false,
                retention = ClipboardRetention.TEN_MINUTES,
                entries = emptyList(),
                blockedReason = "This application is set to Ask.",
                requiresAuthorization = true,
            ),
        )

        assertTrue("Ask state should render a policy/prompt surface", panel.childCount >= 4)
        assertEquals(0, allowed)
    }

    private fun render(view: KeyboardView) {
        val width = 1080
        val height = 720
        view.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
    }
}
