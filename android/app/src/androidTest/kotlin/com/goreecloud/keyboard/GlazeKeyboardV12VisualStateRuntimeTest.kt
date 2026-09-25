package com.goreecloud.keyboard

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GlazeKeyboardV12VisualStateRuntimeTest {
    @Test
    fun ordinaryKeyUsesV12PressedOverlayAndClearsItOnRelease() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val view = KeyboardView(context).apply {
            measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY),
            )
            layout(0, 0, measuredWidth, measuredHeight)
        }
        val committed = mutableListOf<String>()
        view.listener = listener(onText = { committed += it })

        val initial = render(view)
        initial.recycle()
        val qBounds = view.accessibilityTargets().first { it.label == "q" }.bounds
        val pressX = qBounds.centerX()
        val pressY = qBounds.centerY()
        val sampleX = pressX.toInt()
        val sampleY = pressY.toInt()

        val idle = render(view)
        val idleColor = idle.getPixel(sampleX, sampleY)
        idle.recycle()

        dispatch(view, MotionEvent.ACTION_DOWN, pressX, pressY)
        assertTrue("Press-down must not commit text", committed.isEmpty())

        val pressed = render(view)
        val pressedColor = pressed.getPixel(sampleX, sampleY)
        pressed.recycle()
        assertNotEquals(
            "The actual native key surface must visibly consume the V1.2 pressed overlay",
            idleColor,
            pressedColor,
        )

        dispatch(view, MotionEvent.ACTION_UP, pressX, pressY)
        assertEquals(listOf("q"), committed)

        val released = render(view)
        val releasedColor = released.getPixel(sampleX, sampleY)
        released.recycle()
        assertEquals(
            "Release must clear transient pressed presentation",
            idleColor,
            releasedColor,
        )
    }

    @Test
    fun selectedShiftHasAVisualStateDistinctFromIdleUtilityKeys() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val view = KeyboardView(context).apply {
            measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY),
            )
            layout(0, 0, measuredWidth, measuredHeight)
        }

        val initial = render(view)
        initial.recycle()
        val shiftBounds = view.accessibilityTargets().first { it.label == "Shift" }.bounds
        val sampleX = shiftBounds.centerX().toInt()
        val sampleY = shiftBounds.centerY().toInt()

        val idle = render(view)
        val idleColor = idle.getPixel(sampleX, sampleY)
        idle.recycle()

        view.setShifted(true)
        val selected = render(view)
        val selectedColor = selected.getPixel(sampleX, sampleY)
        selected.recycle()

        assertNotEquals(
            "Selected Shift must be visibly distinct from the ordinary utility-key treatment",
            idleColor,
            selectedColor,
        )
    }

    @Test
    fun cancelledPressClearsTransientPresentationWithoutCommitting() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val view = KeyboardView(context).apply {
            measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY),
            )
            layout(0, 0, measuredWidth, measuredHeight)
        }
        val committed = mutableListOf<String>()
        view.listener = listener(onText = { committed += it })

        val initial = render(view)
        initial.recycle()
        val qBounds = view.accessibilityTargets().first { it.label == "q" }.bounds
        val x = qBounds.centerX()
        val y = qBounds.centerY()
        val sampleX = x.toInt()
        val sampleY = y.toInt()

        val idle = render(view)
        val idleColor = idle.getPixel(sampleX, sampleY)
        idle.recycle()

        dispatch(view, MotionEvent.ACTION_DOWN, x, y)
        val pressed = render(view)
        val pressedColor = pressed.getPixel(sampleX, sampleY)
        pressed.recycle()
        assertNotEquals(idleColor, pressedColor)

        dispatch(view, MotionEvent.ACTION_CANCEL, x, y)
        assertTrue(committed.isEmpty())
        val cancelled = render(view)
        val cancelledColor = cancelled.getPixel(sampleX, sampleY)
        cancelled.recycle()
        assertEquals(idleColor, cancelledColor)
    }

    private fun render(view: KeyboardView): Bitmap =
        Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888).also {
            view.draw(Canvas(it))
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
    ) = object : KeyboardView.Listener {
        override fun onText(value: String) = onText(value)
        override fun onSwipe(keyPath: List<String>) = Unit
        override fun onSpace() = Unit
        override fun onBackspace() = Unit
        override fun onEnter() = Unit
        override fun onShift() = Unit
        override fun onSuggestion(value: String) = Unit
        override fun onLayerChanged(layer: KeyboardLayer) = Unit
    }
}
