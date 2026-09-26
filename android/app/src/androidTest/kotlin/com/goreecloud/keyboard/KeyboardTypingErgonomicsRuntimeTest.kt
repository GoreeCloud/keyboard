package com.goreecloud.keyboard

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeyboardTypingErgonomicsRuntimeTest {
    @Test
    fun navigationInsetKeepsBottomRowAboveSystemGestureArea() {
        val view = KeyboardView(
            ApplicationProvider.getApplicationContext<android.content.Context>(),
        )
        val bottomInset = 84
        ViewCompat.dispatchApplyWindowInsets(
            view,
            WindowInsetsCompat.Builder()
                .setInsets(
                    WindowInsetsCompat.Type.navigationBars(),
                    Insets.of(0, 0, 0, bottomInset),
                )
                .build(),
        )
        render(view)

        val enter = view.accessibilityTargets().first { it.label == "Enter" }
        val safeGap = GlazeKeyboardTokens.BottomSafeGapDp * view.resources.displayMetrics.density
        assertTrue(
            "Bottom-row controls must preserve the Glaze safe gap above navigation/gesture insets",
            enter.bounds.bottom <= view.height - bottomInset - safeGap + 1f,
        )
    }

    @Test
    fun lettersLayerExposesAlwaysVisibleNumberRow() {
        val view = createRenderedKeyboard()
        val labels = view.accessibilityTargets().map { it.label }.toSet()

        for (digit in "1234567890") {
            assertTrue("Letters layer must expose digit $digit", digit.toString() in labels)
        }
    }

    @Test
    fun lettersLayerExposesLanguageAwareSpaceSemantics() {
        val view = createRenderedKeyboard()
        val labels = view.accessibilityTargets().map { it.label }.toSet()

        assertTrue(
            "Letters layer must expose the current language through the space-key semantics",
            "Space, English (US)" in labels,
        )
    }

    @Test
    fun suggestionTargetsPreserveTheFullInteractionFloor() {
        val view = KeyboardView(
            ApplicationProvider.getApplicationContext<android.content.Context>(),
        )
        view.setSuggestions(listOf("hello", "help", "hero"))
        render(view)

        val minimumHeight =
            GlazeKeyboardTokens.GeneralInteractionFloorDp * view.resources.displayMetrics.density
        val suggestions = view.accessibilityTargets().filter { it.label.startsWith("Suggestion ") }

        assertEquals(3, suggestions.size)
        assertTrue(
            "Suggestion targets must preserve the full Glaze interaction floor even when the visual surface is inset",
            suggestions.all { it.bounds.height() + 1f >= minimumHeight },
        )
    }

    @Test
    fun lettersLayerExposesDirectPeriodAndCommaKeys() {
        val view = createRenderedKeyboard()
        val labels = view.accessibilityTargets().map { it.label }.toSet()

        assertTrue("Letters layer must expose a direct period key", "." in labels)
        assertTrue("Letters layer must expose a direct comma key", "," in labels)
    }

    @Test
    fun backspaceSmallReleaseDriftStillActivatesThePressedKey() {
        val view = createRenderedKeyboard()
        renderIntoExistingSize(view)
        val backspace = view.accessibilityTargets().first { it.label == "Backspace" }.bounds
        var deletions = 0

        view.listener = object : KeyboardView.Listener {
            override fun onText(value: String) = Unit
            override fun onSwipe(keyPath: List<String>) = Unit
            override fun onSpace() = Unit
            override fun onBackspace() {
                deletions += 1
            }
            override fun onEnter() = Unit
            override fun onShift() = Unit
            override fun onSuggestion(value: String) = Unit
            override fun onLayerChanged(layer: KeyboardLayer) = Unit
        }

        dispatch(
            view,
            MotionEvent.ACTION_DOWN,
            backspace.right - 2f,
            backspace.centerY(),
            eventTime = 0L,
        )
        dispatch(
            view,
            MotionEvent.ACTION_UP,
            backspace.right + 4f,
            backspace.centerY(),
            eventTime = 30L,
        )

        assertEquals(
            "A small release drift outside Backspace must still perform the deliberate key tap",
            1,
            deletions,
        )
    }

    @Test
    fun holdingBackspaceRepeatsUntilRelease() {
        val view = createRenderedKeyboard()
        renderIntoExistingSize(view)
        val backspace = view.accessibilityTargets().first { it.label == "Backspace" }.bounds
        val deletions = AtomicInteger(0)

        view.listener = object : KeyboardView.Listener {
            override fun onText(value: String) = Unit
            override fun onSwipe(keyPath: List<String>) = Unit
            override fun onSpace() = Unit
            override fun onBackspace() {
                deletions.incrementAndGet()
            }
            override fun onEnter() = Unit
            override fun onShift() = Unit
            override fun onSuggestion(value: String) = Unit
            override fun onLayerChanged(layer: KeyboardLayer) = Unit
        }

        dispatch(
            view,
            MotionEvent.ACTION_DOWN,
            backspace.centerX(),
            backspace.centerY(),
            eventTime = 0L,
        )
        assertEquals(
            "Backspace must respond immediately on press",
            1,
            deletions.get(),
        )

        SystemClock.sleep(520L)
        assertTrue(
            "Holding Backspace must repeatedly delete rather than waiting for release",
            deletions.get() >= 2,
        )

        dispatch(
            view,
            MotionEvent.ACTION_UP,
            backspace.centerX(),
            backspace.centerY(),
            eventTime = 540L,
        )
        val deletionsAtRelease = deletions.get()
        SystemClock.sleep(180L)

        assertEquals(
            "Backspace repeat must stop immediately after release",
            deletionsAtRelease,
            deletions.get(),
        )
    }

    @Test
    fun smallVisualGapNearMissStillActivatesNearestLetter() {
        val view = createRenderedKeyboard()
        renderIntoExistingSize(view)
        val targets = view.accessibilityTargets()
        val q = targets.first { it.label == "q" }.bounds
        val w = targets.first { it.label == "w" }.bounds
        val taps = mutableListOf<String>()

        view.listener = object : KeyboardView.Listener {
            override fun onText(value: String) {
                taps += value
            }
            override fun onSwipe(keyPath: List<String>) = Unit
            override fun onSpace() = Unit
            override fun onBackspace() = Unit
            override fun onEnter() = Unit
            override fun onShift() = Unit
            override fun onSuggestion(value: String) = Unit
            override fun onLayerChanged(layer: KeyboardLayer) = Unit
        }

        assertTrue("Expected a visual gap between Q and W hit bounds", w.left > q.right)
        val x = q.right + minOf(1f, (w.left - q.right) / 3f)
        val y = q.centerY()
        dispatch(view, MotionEvent.ACTION_DOWN, x, y, eventTime = 0L)
        dispatch(view, MotionEvent.ACTION_UP, x, y, eventTime = 20L)

        assertEquals(
            "A small touch in the visual key gap should resolve to the nearest key rather than disappear",
            listOf("q"),
            taps,
        )
    }

    @Test
    fun rapidLetterTapsAllCommitExactlyOnce() {
        val view = createRenderedKeyboard()
        renderIntoExistingSize(view)
        val targets = view.accessibilityTargets()
        val l = targets.first { it.label == "l" }.bounds
        val a = targets.first { it.label == "a" }.bounds
        val g = targets.first { it.label == "g" }.bounds
        val taps = mutableListOf<String>()

        view.listener = object : KeyboardView.Listener {
            override fun onText(value: String) {
                taps += value
            }
            override fun onSwipe(keyPath: List<String>) = Unit
            override fun onSpace() = Unit
            override fun onBackspace() = Unit
            override fun onEnter() = Unit
            override fun onShift() = Unit
            override fun onSuggestion(value: String) = Unit
            override fun onLayerChanged(layer: KeyboardLayer) = Unit
        }

        val sequence = listOf(
            "l" to l,
            "a" to a,
            "g" to g,
            "g" to g,
            "i" to targets.first { it.label == "i" }.bounds,
            "n" to targets.first { it.label == "n" }.bounds,
            "g" to g,
        )
        var eventTime = 0L
        repeat(4) {
            sequence.forEach { (_, bounds) ->
                dispatch(view, MotionEvent.ACTION_DOWN, bounds.centerX(), bounds.centerY(), eventTime)
                dispatch(view, MotionEvent.ACTION_UP, bounds.centerX(), bounds.centerY(), eventTime + 12L)
                eventTime += 24L
            }
        }

        assertEquals(
            "Rapid ordinary taps must never be dropped or duplicated by the KeyboardView touch path",
            sequence.flatMap { pair -> List(4) { pair.first } }.size,
            taps.size,
        )
        assertEquals(
            "lagging".repeat(4).toList().map { it.toString() },
            taps,
        )
    }

    @Test
    fun swipeGestureEmitsOrderedLetterTraceInsteadOfSingleTap() {
        val view = createRenderedKeyboard()
        view.setSwipeTypingEnabled(true)
        renderIntoExistingSize(view)

        val targets = view.accessibilityTargets()
        val h = targets.first { it.label == "h" }.bounds
        val e = targets.first { it.label == "e" }.bounds
        val l = targets.first { it.label == "l" }.bounds
        val o = targets.first { it.label == "o" }.bounds
        val traces = mutableListOf<List<String>>()
        val taps = mutableListOf<String>()

        view.listener = object : KeyboardView.Listener {
            override fun onText(value: String) {
                taps += value
            }

            override fun onSwipe(keyPath: List<String>) {
                traces += keyPath
            }

            override fun onSpace() = Unit
            override fun onBackspace() = Unit
            override fun onEnter() = Unit
            override fun onShift() = Unit
            override fun onSuggestion(value: String) = Unit
            override fun onLayerChanged(layer: KeyboardLayer) = Unit
        }

        dispatch(view, MotionEvent.ACTION_DOWN, h.centerX(), h.centerY(), eventTime = 0L)
        dispatch(view, MotionEvent.ACTION_MOVE, e.centerX(), e.centerY(), eventTime = 80L)
        dispatch(view, MotionEvent.ACTION_MOVE, l.centerX(), l.centerY(), eventTime = 145L)
        dispatch(view, MotionEvent.ACTION_UP, o.centerX(), o.centerY(), eventTime = 210L)

        assertEquals(listOf(listOf("h", "e", "l", "o")), traces)
        assertTrue("Swipe typing must not also commit the release key as a tap", taps.isEmpty())
    }

    @Test
    fun swipeGestureConsumesHistoricalMotionSamples() {
        val view = createRenderedKeyboard()
        view.setSwipeTypingEnabled(true)
        renderIntoExistingSize(view)

        val targets = view.accessibilityTargets()
        val h = targets.first { it.label == "h" }.bounds
        val e = targets.first { it.label == "e" }.bounds
        val l = targets.first { it.label == "l" }.bounds
        val o = targets.first { it.label == "o" }.bounds
        val gestures = mutableListOf<SwipeGesture>()

        view.listener = object : KeyboardView.Listener {
            override fun onText(value: String) = Unit
            override fun onSwipe(keyPath: List<String>) = Unit
            override fun onSwipeGesture(gesture: SwipeGesture) {
                gestures += gesture
            }
            override fun onSpace() = Unit
            override fun onBackspace() = Unit
            override fun onEnter() = Unit
            override fun onShift() = Unit
            override fun onSuggestion(value: String) = Unit
            override fun onLayerChanged(layer: KeyboardLayer) = Unit
        }

        dispatch(view, MotionEvent.ACTION_DOWN, h.centerX(), h.centerY(), eventTime = 0L)
        dispatchBatchedMove(
            view = view,
            firstTimeMs = 85L,
            firstX = e.centerX(),
            firstY = e.centerY(),
            secondTimeMs = 150L,
            secondX = l.centerX(),
            secondY = l.centerY(),
        )
        dispatch(view, MotionEvent.ACTION_UP, o.centerX(), o.centerY(), eventTime = 220L)

        assertEquals(1, gestures.size)
        assertEquals(
            "Historical Android motion samples must contribute crossed-key evidence to the swipe trace",
            listOf("h", "e", "l", "o"),
            gestures.single().keyPath,
        )
        assertTrue(
            "Historical Android motion samples must be preserved in the physical gesture geometry",
            gestures.single().points.size >= 4,
        )
    }

    @Test
    fun fastTwoKeySlipDoesNotBecomeSwipeTyping() {
        val view = createRenderedKeyboard()
        view.setSwipeTypingEnabled(true)
        renderIntoExistingSize(view)

        val targets = view.accessibilityTargets()
        val h = targets.first { it.label == "h" }.bounds
        val j = targets.first { it.label == "j" }.bounds
        val traces = mutableListOf<List<String>>()
        val taps = mutableListOf<String>()

        view.listener = object : KeyboardView.Listener {
            override fun onText(value: String) {
                taps += value
            }

            override fun onSwipe(keyPath: List<String>) {
                traces += keyPath
            }

            override fun onSpace() = Unit
            override fun onBackspace() = Unit
            override fun onEnter() = Unit
            override fun onShift() = Unit
            override fun onSuggestion(value: String) = Unit
            override fun onLayerChanged(layer: KeyboardLayer) = Unit
        }

        dispatch(view, MotionEvent.ACTION_DOWN, h.centerX(), h.centerY(), eventTime = 0L)
        dispatch(view, MotionEvent.ACTION_MOVE, j.centerX(), j.centerY(), eventTime = 20L)
        dispatch(view, MotionEvent.ACTION_UP, j.centerX(), j.centerY(), eventTime = 35L)

        assertTrue("A quick two-key slip must not trigger swipe decoding", traces.isEmpty())
        assertEquals(listOf("j"), taps)
    }

    private fun createRenderedKeyboard(): KeyboardView {
        val view = KeyboardView(
            ApplicationProvider.getApplicationContext<android.content.Context>(),
        )
        render(view)
        return view
    }

    private fun render(view: KeyboardView) {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        renderIntoExistingSize(view)
    }

    private fun renderIntoExistingSize(view: KeyboardView) {
        view.draw(
            Canvas(
                Bitmap.createBitmap(
                    view.measuredWidth,
                    view.measuredHeight,
                    Bitmap.Config.ARGB_8888,
                ),
            ),
        )
    }

    private fun dispatchBatchedMove(
        view: KeyboardView,
        firstTimeMs: Long,
        firstX: Float,
        firstY: Float,
        secondTimeMs: Long,
        secondX: Float,
        secondY: Float,
    ) {
        val event = MotionEvent.obtain(
            0L,
            firstTimeMs,
            MotionEvent.ACTION_MOVE,
            firstX,
            firstY,
            0,
        )
        try {
            event.addBatch(
                secondTimeMs,
                secondX,
                secondY,
                1f,
                1f,
                0,
            )
            view.dispatchTouchEvent(event)
        } finally {
            event.recycle()
        }
    }

    private fun dispatch(
        view: KeyboardView,
        action: Int,
        x: Float,
        y: Float,
        eventTime: Long = 0L,
    ) {
        val event = MotionEvent.obtain(0L, eventTime, action, x, y, 0)
        try {
            view.dispatchTouchEvent(event)
        } finally {
            event.recycle()
        }
    }
}
