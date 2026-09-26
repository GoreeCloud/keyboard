package com.goreecloud.keyboard

import kotlin.math.abs
import kotlin.math.floor

internal enum class SpacebarCursorGestureMode {
    PENDING,
    CURSOR,
    CANCEL,
}

internal data class SpacebarCursorGestureDecision(
    val mode: SpacebarCursorGestureMode,
    val cumulativeSteps: Int = 0,
)

/**
 * Pure gesture policy for privacy-bounded spacebar cursor control.
 *
 * Horizontal movement must dominate before cursor mode activates. Once active, motion becomes
 * cumulative directional cursor steps. Invalid geometry and vertical/equal-axis activation fail
 * closed instead of turning into text input or another keyboard action.
 */
internal object SpacebarCursorGesturePolicy {
    fun supportsPointerCount(pointerCount: Int): Boolean = pointerCount == 1

    fun evaluate(
        deltaX: Float,
        deltaY: Float,
        activationDistancePx: Float,
        stepDistancePx: Float,
    ): SpacebarCursorGestureDecision {
        if (
            !activationDistancePx.isFinite() ||
            activationDistancePx <= 0f ||
            !stepDistancePx.isFinite() ||
            stepDistancePx <= 0f ||
            !deltaX.isFinite() ||
            !deltaY.isFinite()
        ) {
            return SpacebarCursorGestureDecision(SpacebarCursorGestureMode.CANCEL)
        }

        val horizontal = abs(deltaX)
        val vertical = abs(deltaY)
        if (horizontal < activationDistancePx && vertical < activationDistancePx) {
            return SpacebarCursorGestureDecision(SpacebarCursorGestureMode.PENDING)
        }
        if (horizontal <= vertical) {
            return SpacebarCursorGestureDecision(SpacebarCursorGestureMode.CANCEL)
        }

        val additionalSteps = floor((horizontal - activationDistancePx) / stepDistancePx)
            .toInt()
            .coerceIn(0, MAX_CUMULATIVE_STEPS - 1)
        val direction = if (deltaX < 0f) -1 else 1
        return SpacebarCursorGestureDecision(
            mode = SpacebarCursorGestureMode.CURSOR,
            cumulativeSteps = direction * (1 + additionalSteps),
        )
    }

    private const val MAX_CUMULATIVE_STEPS = 4096
}
