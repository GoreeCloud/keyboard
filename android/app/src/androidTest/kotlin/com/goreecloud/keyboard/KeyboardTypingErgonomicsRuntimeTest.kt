package com.goreecloud.keyboard

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
