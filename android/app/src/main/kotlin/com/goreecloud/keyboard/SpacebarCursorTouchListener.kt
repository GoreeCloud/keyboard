package com.goreecloud.keyboard

import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration

/**
 * Observes only one-finger gestures that begin on the rendered Space accessibility target.
 *
 * Normal Space taps stay on KeyboardView's existing path. Once cursor mode activates, the normal
 * key gesture is cancelled and the remainder of the pointer stream is consumed so release cannot
 * also commit a Space. This listener has no editor-text, clipboard, persistence, network, or
 * telemetry authority.
 */
internal class SpacebarCursorTouchListener(
    private val keyboardView: KeyboardView,
    private val isEnabled: () -> Boolean,
    private val onCursorSteps: (Int) -> Unit,
    private val activationDistancePx: Float =
        ViewConfiguration.get(keyboardView.context).scaledTouchSlop.toFloat(),
    private val stepDistancePx: Float = 24f * keyboardView.resources.displayMetrics.density,
) : View.OnTouchListener {
    private var spaceBounds: RectF? = null
    private var startX = 0f
    private var startY = 0f
    private var emittedSteps = 0
    private var cursorMode = false
    private var consumeUntilUp = false

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (
            spaceBounds != null &&
            !SpacebarCursorGesturePolicy.supportsPointerCount(event.pointerCount)
        ) {
            cancelNormalKeyboardTouch(event)
            spaceBounds = null
            cursorMode = false
            consumeUntilUp = true
            return true
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                reset()
                if (!isEnabled()) return false
                val target = keyboardView.accessibilityTargets()
                    .firstOrNull {
                        isSpaceAccessibilityLabel(it.label) &&
                            it.bounds.contains(event.x, event.y)
                    }
                    ?: return false
                spaceBounds = RectF(target.bounds)
                startX = event.x
                startY = event.y
                return false
            }

            MotionEvent.ACTION_MOVE -> {
                if (spaceBounds == null) return consumeUntilUp
                val decision = SpacebarCursorGesturePolicy.evaluate(
                    deltaX = event.x - startX,
                    deltaY = if (cursorMode) 0f else event.y - startY,
                    activationDistancePx = activationDistancePx,
                    stepDistancePx = stepDistancePx,
                )

                return when (decision.mode) {
                    SpacebarCursorGestureMode.PENDING -> false
                    SpacebarCursorGestureMode.CANCEL -> {
                        cancelNormalKeyboardTouch(event)
                        spaceBounds = null
                        consumeUntilUp = true
                        true
                    }
                    SpacebarCursorGestureMode.CURSOR -> {
                        if (!cursorMode) {
                            cursorMode = true
                            consumeUntilUp = true
                            cancelNormalKeyboardTouch(event)
                        }
                        val stepDelta = decision.cumulativeSteps - emittedSteps
                        if (stepDelta != 0) {
                            emittedSteps = decision.cumulativeSteps
                            onCursorSteps(stepDelta)
                        }
                        true
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                val consume = cursorMode || consumeUntilUp
                reset()
                return consume
            }

            MotionEvent.ACTION_CANCEL -> {
                val consume = cursorMode || consumeUntilUp
                reset()
                return consume
            }
        }
        return cursorMode || consumeUntilUp
    }

    private fun cancelNormalKeyboardTouch(source: MotionEvent) {
        val cancel = MotionEvent.obtain(source)
        cancel.action = MotionEvent.ACTION_CANCEL
        keyboardView.onTouchEvent(cancel)
        cancel.recycle()
    }

    private fun reset() {
        spaceBounds = null
        startX = 0f
        startY = 0f
        emittedSteps = 0
        cursorMode = false
        consumeUntilUp = false
    }

    private fun isSpaceAccessibilityLabel(label: String): Boolean =
        label == "Space" || label.startsWith("Space,")

}
