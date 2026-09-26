package com.goreecloud.keyboard

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpacebarCursorRuntimeTest {
    @Test
    fun ordinarySpaceTapStillUsesNormalKeyPath() {
        val view = createRenderedView()
        var spaces = 0
        val cursorSteps = mutableListOf<Int>()
        view.listener = listener(onSpaceAction = { spaces += 1 })
        view.setOnTouchListener(
            SpacebarCursorTouchListener(
                keyboardView = view,
                isEnabled = { true },
                onCursorSteps = cursorSteps::add,
                activationDistancePx = 10f,
                stepDistancePx = 20f,
            ),
        )

        val bounds = spaceBounds(view)
        dispatch(view, MotionEvent.ACTION_DOWN, bounds.centerX(), bounds.centerY())
        dispatch(view, MotionEvent.ACTION_UP, bounds.centerX(), bounds.centerY())

        assertEquals(1, spaces)
        assertTrue(cursorSteps.isEmpty())
    }

    @Test
    fun horizontalSpacebarDragMovesCursorWithoutCommittingSpace() {
        val view = createRenderedView()
        var spaces = 0
        val cursorSteps = mutableListOf<Int>()
        view.listener = listener(onSpaceAction = { spaces += 1 })
        view.setOnTouchListener(
            SpacebarCursorTouchListener(
                keyboardView = view,
                isEnabled = { true },
                onCursorSteps = cursorSteps::add,
                activationDistancePx = 10f,
                stepDistancePx = 20f,
            ),
        )

        val bounds = spaceBounds(view)
        val x = bounds.centerX()
        val y = bounds.centerY()
        dispatch(view, MotionEvent.ACTION_DOWN, x, y)
        dispatch(view, MotionEvent.ACTION_MOVE, x + 35f, y + 1f)
        dispatch(view, MotionEvent.ACTION_UP, x + 35f, y + 1f)

        assertEquals(0, spaces)
        assertEquals(listOf(2), cursorSteps)
    }

    @Test
    fun verticalDominantSpacebarGestureCommitsNeitherSpaceNorCursorMove() {
        val view = createRenderedView()
        var spaces = 0
        val cursorSteps = mutableListOf<Int>()
        view.listener = listener(onSpaceAction = { spaces += 1 })
        view.setOnTouchListener(
            SpacebarCursorTouchListener(
                keyboardView = view,
                isEnabled = { true },
                onCursorSteps = cursorSteps::add,
                activationDistancePx = 10f,
                stepDistancePx = 20f,
            ),
        )

        val bounds = spaceBounds(view)
        val x = bounds.centerX()
        val y = bounds.centerY()
        dispatch(view, MotionEvent.ACTION_DOWN, x, y)
        dispatch(view, MotionEvent.ACTION_MOVE, x + 2f, y + 18f)
        dispatch(view, MotionEvent.ACTION_UP, x + 2f, y + 18f)

        assertEquals(0, spaces)
        assertTrue(cursorSteps.isEmpty())
    }

    @Test
    fun disabledCursorControlLeavesSpacebarOnNormalTapPath() {
        val view = createRenderedView()
        var spaces = 0
        val cursorSteps = mutableListOf<Int>()
        view.listener = listener(onSpaceAction = { spaces += 1 })
        view.setOnTouchListener(
            SpacebarCursorTouchListener(
                keyboardView = view,
                isEnabled = { false },
                onCursorSteps = cursorSteps::add,
                activationDistancePx = 10f,
                stepDistancePx = 20f,
            ),
        )

        val bounds = spaceBounds(view)
        dispatch(view, MotionEvent.ACTION_DOWN, bounds.centerX(), bounds.centerY())
        dispatch(view, MotionEvent.ACTION_UP, bounds.centerX(), bounds.centerY())

        assertEquals(1, spaces)
        assertTrue(cursorSteps.isEmpty())
    }

    private fun createRenderedView(): KeyboardView {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        return KeyboardView(context).apply {
            measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(720, View.MeasureSpec.EXACTLY),
            )
            layout(0, 0, measuredWidth, measuredHeight)
            draw(
                Canvas(
                    Bitmap.createBitmap(
                        measuredWidth,
                        measuredHeight,
                        Bitmap.Config.ARGB_8888,
                    ),
                ),
            )
        }
    }

    private fun spaceBounds(view: KeyboardView) =
        view.accessibilityTargets().first { it.label.startsWith("Space") }.bounds

    private fun dispatch(view: KeyboardView, action: Int, x: Float, y: Float) {
        val now = android.os.SystemClock.uptimeMillis()
        MotionEvent.obtain(now, now, action, x, y, 0).also { event ->
            view.dispatchTouchEvent(event)
            event.recycle()
        }
    }

    private fun listener(onSpaceAction: () -> Unit) = object : KeyboardView.Listener {
        override fun onText(value: String) = Unit
        override fun onSwipe(keyPath: List<String>) = Unit
        override fun onSpace() = onSpaceAction()
        override fun onBackspace() = Unit
        override fun onEnter() = Unit
        override fun onShift() = Unit
        override fun onSuggestion(value: String) = Unit
        override fun onLayerChanged(layer: KeyboardLayer) = Unit
    }
}
