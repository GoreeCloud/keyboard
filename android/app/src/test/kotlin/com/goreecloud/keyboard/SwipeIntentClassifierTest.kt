package com.goreecloud.keyboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SwipeIntentClassifierTest {
    @Test
    fun recentThreeKeyTapDriftDoesNotActivateSwipe() {
        assertFalse(
            SwipeIntentClassifier.shouldActivate(
                SwipeIntentClassifier.Evidence(
                    pathTravelDp = 70f,
                    netTravelDp = 50f,
                    elapsedMs = 126L,
                    distinctLetterCount = 3,
                    recentFastTyping = true,
                    touchSlopDp = 8f,
                ),
            ),
        )
    }

    @Test
    fun strongRecentThreeKeyGestureCanStillActivate() {
        assertTrue(
            SwipeIntentClassifier.shouldActivate(
                SwipeIntentClassifier.Evidence(
                    pathTravelDp = 108f,
                    netTravelDp = 58f,
                    elapsedMs = 175L,
                    distinctLetterCount = 3,
                    recentFastTyping = true,
                    touchSlopDp = 8f,
                ),
            ),
        )
    }

    @Test
    fun deliberateFourKeyGestureAfterTapStillActivates() {
        assertTrue(
            SwipeIntentClassifier.shouldActivate(
                SwipeIntentClassifier.Evidence(
                    pathTravelDp = 118f,
                    netTravelDp = 76f,
                    elapsedMs = 210L,
                    distinctLetterCount = 4,
                    recentFastTyping = true,
                    touchSlopDp = 8f,
                ),
            ),
        )
    }

    @Test
    fun ordinaryGestureWithoutRecentTypingUsesNormalThresholds() {
        assertTrue(
            SwipeIntentClassifier.shouldActivate(
                SwipeIntentClassifier.Evidence(
                    pathTravelDp = 58f,
                    netTravelDp = 42f,
                    elapsedMs = 105L,
                    distinctLetterCount = 3,
                    recentFastTyping = false,
                    touchSlopDp = 8f,
                ),
            ),
        )
    }

    @Test
    fun twoKeyGestureNeedsStrongEvidenceDuringRapidTyping() {
        assertFalse(
            SwipeIntentClassifier.shouldActivate(
                SwipeIntentClassifier.Evidence(
                    pathTravelDp = 88f,
                    netTravelDp = 48f,
                    elapsedMs = 145L,
                    distinctLetterCount = 2,
                    recentFastTyping = true,
                    touchSlopDp = 8f,
                ),
            ),
        )
        assertTrue(
            SwipeIntentClassifier.shouldActivate(
                SwipeIntentClassifier.Evidence(
                    pathTravelDp = 112f,
                    netTravelDp = 60f,
                    elapsedMs = 180L,
                    distinctLetterCount = 2,
                    recentFastTyping = true,
                    touchSlopDp = 8f,
                ),
            ),
        )
    }
}
