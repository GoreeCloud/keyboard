package com.goreecloud.keyboard

import android.graphics.Bitmap
import android.graphics.Canvas
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

private object GlazeMotionExperimental {
    const val VERSION = "0.5.0"
    const val REFERENCE_REVISION = "b386c793c047e2f5d5d92125732f142e7fdf32dc"
    const val RUNTIME_BASELINE = "0.4.0"
    const val MICRO_DURATION_MS = 90L
    const val REDUCED_MOTION_DURATION_MS = 0L
    const val PRESS_SCALE = 0.98f
    const val MAXIMUM_CONCURRENT_SETTLING = 12

    fun durationMs(reducedMotion: Boolean): Long =
        if (reducedMotion) REDUCED_MOTION_DURATION_MS else MICRO_DURATION_MS

    fun allowsOptionalSettling(reducedMotion: Boolean, activeSettling: Int): Boolean =
        !reducedMotion && activeSettling < MAXIMUM_CONCURRENT_SETTLING
}

@RunWith(AndroidJUnit4::class)
class GlazeMotionExperimentalKeyboardRuntimeTest {
    @Test
    fun disabledPlatformAnimationsCollapseOptionalMotionWithoutBlockingKeyCommit() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val animatorScale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        )
        assertEquals("CI must disable the Android animator duration scale", 0f, animatorScale, 0f)
        assertEquals(
            "Reduced-motion semantic duration must collapse",
            0L,
            GlazeMotionExperimental.durationMs(reducedMotion = true)
        )
        assertFalse(
            "Optional settling must be rejected under reduced motion",
            GlazeMotionExperimental.allowsOptionalSettling(
                reducedMotion = true,
                activeSettling = 0
            )
        )

        val view = createRenderedKeyboard()
        val events = mutableListOf<String>()
        view.listener = listener(onText = { events += it })

        val qBounds = view.accessibilityTargets().first { it.label == "q" }.bounds
        val x = qBounds.centerX()
        val y = qBounds.centerY()

        dispatch(view, MotionEvent.ACTION_DOWN, x, y)
        assertTrue("Press-down must not commit semantic input", events.isEmpty())
        dispatch(view, MotionEvent.ACTION_UP, x, y)
        assertEquals("Release must commit the real q key exactly once", listOf("q"), events)
    }

    @Test
    fun realSuggestionHitTestingRemainsSemanticStateSource() {
        val view = createRenderedKeyboard(listOf("hello", "help", "hero"))
        val selected = mutableListOf<String>()
        view.listener = listener(onSuggestion = { selected += it })

        val helloBounds = view.accessibilityTargets()
            .first { it.label == "Suggestion hello" }
            .bounds
        dispatch(view, MotionEvent.ACTION_UP, helloBounds.centerX(), helloBounds.centerY())

        assertEquals("Suggestion hit-testing must remain authoritative", listOf("hello"), selected)
        assertEquals("0.5.0", GlazeMotionExperimental.VERSION)
        assertEquals("0.4.0", GlazeMotionExperimental.RUNTIME_BASELINE)
        assertEquals(90L, GlazeMotionExperimental.durationMs(reducedMotion = false))
        assertTrue(GlazeMotionExperimental.PRESS_SCALE < 1f)
    }

    @Test
    fun primarySymbolPageNavigatesToSecondaryPageAndCommitsRenderedText() {
        val view = createRenderedKeyboard()
        view.setLayer(KeyboardLayer.SYMBOLS)
        render(view)
        val layers = mutableListOf<KeyboardLayer>()
        val text = mutableListOf<String>()
        view.listener = listener(
            onText = { text += it },
            onLayerChanged = { layers += it }
        )

        val density = view.resources.displayMetrics.density
        val horizontalPadding = GlazeKeyboardTokens.Space2Dp * density
        val gap = GlazeKeyboardTokens.Space1Dp * density
        val keyboardTop = (GlazeKeyboardTokens.SuggestionStripHeightDp + GlazeKeyboardTokens.Space2Dp) * density
        val rowHeight = (view.height - keyboardTop - gap * 5f) / 4f
        val bottomY = keyboardTop + 3f * (rowHeight + gap) + rowHeight / 2f

        val bottomAvailable = view.width - horizontalPadding * 2f - gap * 3f
        val modeWidth = bottomAvailable * (1.2f / 7.9f)
        val moreModeX = horizontalPadding + modeWidth + gap + modeWidth / 2f
        dispatch(view, MotionEvent.ACTION_UP, moreModeX, bottomY)

        assertEquals(listOf(KeyboardLayer.SYMBOLS_MORE), layers)

        render(view)
        val topRowAvailable = view.width - horizontalPadding * 2f - gap * 9f
        val firstTextX = horizontalPadding + topRowAvailable / 10f / 2f
        val firstTextY = keyboardTop + rowHeight / 2f
        dispatch(view, MotionEvent.ACTION_UP, firstTextX, firstTextY)

        assertEquals("Secondary symbol page must commit the rendered [ key", listOf("["), text)
    }

    private fun createRenderedKeyboard(suggestions: List<String> = emptyList()): KeyboardView {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        return KeyboardView(context).apply {
            setSuggestions(suggestions)
            measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY)
            )
            layout(0, 0, measuredWidth, measuredHeight)
            render(this)
        }
    }

    private fun render(view: KeyboardView) {
        view.draw(Canvas(Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)))
    }

    private fun dispatch(view: KeyboardView, action: Int, x: Float, y: Float) {
        val event = MotionEvent.obtain(0L, 0L, action, x, y, 0)
        try {
            view.dispatchTouchEvent(event)
        } finally {
            event.recycle()
        }
    }

    private fun listener(
        onText: (String) -> Unit = {},
        onSuggestion: (String) -> Unit = {},
        onLayerChanged: (KeyboardLayer) -> Unit = {}
    ) = object : KeyboardView.Listener {
        override fun onText(value: String) = onText(value)
        override fun onSwipe(keyPath: List<String>) = Unit
        override fun onSpace() = Unit
        override fun onBackspace() = Unit
        override fun onEnter() = Unit
        override fun onShift() = Unit
        override fun onSuggestion(value: String) = onSuggestion(value)
        override fun onLayerChanged(layer: KeyboardLayer) = onLayerChanged(layer)
    }
}
