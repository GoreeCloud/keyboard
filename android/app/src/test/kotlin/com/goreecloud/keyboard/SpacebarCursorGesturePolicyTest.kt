package com.goreecloud.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpacebarCursorGesturePolicyTest {
    @Test
    fun onlySinglePointerGesturesAreSupported() {
        assertTrue(SpacebarCursorGesturePolicy.supportsPointerCount(1))
        assertFalse(SpacebarCursorGesturePolicy.supportsPointerCount(0))
        assertFalse(SpacebarCursorGesturePolicy.supportsPointerCount(2))
        assertFalse(SpacebarCursorGesturePolicy.supportsPointerCount(Int.MAX_VALUE))
    }

    @Test
    fun movementInsideActivationWindowStaysPending() {
        assertEquals(
            SpacebarCursorGestureDecision(SpacebarCursorGestureMode.PENDING),
            SpacebarCursorGesturePolicy.evaluate(7f, 2f, 8f, 20f),
        )
    }

    @Test
    fun horizontalDragEmitsDirectionalCumulativeSteps() {
        assertEquals(
            SpacebarCursorGestureDecision(SpacebarCursorGestureMode.CURSOR, 1),
            SpacebarCursorGesturePolicy.evaluate(8f, 0f, 8f, 20f),
        )
        assertEquals(
            SpacebarCursorGestureDecision(SpacebarCursorGestureMode.CURSOR, 2),
            SpacebarCursorGesturePolicy.evaluate(29f, 1f, 8f, 20f),
        )
        assertEquals(
            SpacebarCursorGestureDecision(SpacebarCursorGestureMode.CURSOR, -3),
            SpacebarCursorGesturePolicy.evaluate(-49f, 2f, 8f, 20f),
        )
    }

    @Test
    fun extremeFiniteSamplesRemainBoundedAndDirectional() {
        assertEquals(
            SpacebarCursorGestureDecision(SpacebarCursorGestureMode.CURSOR, 4096),
            SpacebarCursorGesturePolicy.evaluate(Float.MAX_VALUE, 0f, 8f, 1f),
        )
        assertEquals(
            SpacebarCursorGestureDecision(SpacebarCursorGestureMode.CURSOR, -4096),
            SpacebarCursorGesturePolicy.evaluate(-Float.MAX_VALUE, 0f, 8f, 1f),
        )
    }

    @Test
    fun verticalOrEqualAxisMovementFailsClosed() {
        assertEquals(
            SpacebarCursorGestureMode.CANCEL,
            SpacebarCursorGesturePolicy.evaluate(9f, 12f, 8f, 20f).mode,
        )
        assertEquals(
            SpacebarCursorGestureMode.CANCEL,
            SpacebarCursorGesturePolicy.evaluate(10f, 10f, 8f, 20f).mode,
        )
    }

    @Test
    fun invalidGeometryFailsClosed() {
        assertEquals(
            SpacebarCursorGestureMode.CANCEL,
            SpacebarCursorGesturePolicy.evaluate(10f, 0f, 0f, 20f).mode,
        )
        assertEquals(
            SpacebarCursorGestureMode.CANCEL,
            SpacebarCursorGesturePolicy.evaluate(Float.NaN, 0f, 8f, 20f).mode,
        )
    }
}
