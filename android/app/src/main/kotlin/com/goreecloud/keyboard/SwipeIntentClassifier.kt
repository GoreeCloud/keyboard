package com.goreecloud.keyboard

import kotlin.math.max

/**
 * Distinguishes deliberate gesture typing from ordinary tap drift.
 *
 * A recent completed letter tap is strong evidence that the user is in a rapid tap-typing burst.
 * During that burst, crossing extra key bounds is not sufficient by itself to become a swipe:
 * the gesture must show materially stronger duration and travel evidence. This keeps swipe typing
 * available without letting a hurried tap mutate into a gesture merely because the fingertip
 * crossed two neighboring keys before ACTION_UP.
 */
internal object SwipeIntentClassifier {
    data class Evidence(
        val pathTravelDp: Float,
        val netTravelDp: Float,
        val elapsedMs: Long,
        val distinctLetterCount: Int,
        val recentFastTyping: Boolean,
        val touchSlopDp: Float,
    )

    fun shouldActivate(evidence: Evidence): Boolean {
        if (evidence.distinctLetterCount < 2) return false
        if (evidence.elapsedMs < 0L) return false

        val normalPathFloor = max(
            evidence.touchSlopDp * TOUCH_SLOP_MULTIPLIER,
            NORMAL_MIN_PATH_DP,
        )

        if (!evidence.recentFastTyping) {
            return when (evidence.distinctLetterCount) {
                2 ->
                    evidence.pathTravelDp >= NORMAL_TWO_KEY_MIN_PATH_DP &&
                        evidence.netTravelDp >= NORMAL_TWO_KEY_MIN_NET_DP &&
                        evidence.elapsedMs >= NORMAL_TWO_KEY_MIN_MS
                3 ->
                    evidence.pathTravelDp >= max(normalPathFloor, NORMAL_THREE_KEY_MIN_PATH_DP) &&
                        evidence.elapsedMs >= NORMAL_THREE_KEY_MIN_MS
                else ->
                    evidence.pathTravelDp >= normalPathFloor &&
                        evidence.elapsedMs >= NORMAL_MIN_MS
            }
        }

        return when (evidence.distinctLetterCount) {
            2 ->
                evidence.pathTravelDp >= RECENT_TWO_KEY_MIN_PATH_DP &&
                    evidence.netTravelDp >= RECENT_TWO_KEY_MIN_NET_DP &&
                    evidence.elapsedMs >= RECENT_TWO_KEY_MIN_MS
            3 ->
                evidence.pathTravelDp >= RECENT_THREE_KEY_MIN_PATH_DP &&
                    evidence.netTravelDp >= RECENT_THREE_KEY_MIN_NET_DP &&
                    evidence.elapsedMs >= RECENT_THREE_KEY_MIN_MS
            else ->
                evidence.pathTravelDp >= RECENT_FOUR_PLUS_MIN_PATH_DP &&
                    evidence.elapsedMs >= RECENT_FOUR_PLUS_MIN_MS
        }
    }

    private const val TOUCH_SLOP_MULTIPLIER = 2.25f

    private const val NORMAL_MIN_PATH_DP = 34f
    private const val NORMAL_MIN_MS = 70L
    private const val NORMAL_THREE_KEY_MIN_PATH_DP = 42f
    private const val NORMAL_THREE_KEY_MIN_MS = 80L
    private const val NORMAL_TWO_KEY_MIN_PATH_DP = 68f
    private const val NORMAL_TWO_KEY_MIN_NET_DP = 34f
    private const val NORMAL_TWO_KEY_MIN_MS = 100L

    private const val RECENT_TWO_KEY_MIN_PATH_DP = 100f
    private const val RECENT_TWO_KEY_MIN_NET_DP = 52f
    private const val RECENT_TWO_KEY_MIN_MS = 165L
    private const val RECENT_THREE_KEY_MIN_PATH_DP = 92f
    private const val RECENT_THREE_KEY_MIN_NET_DP = 46f
    private const val RECENT_THREE_KEY_MIN_MS = 155L
    private const val RECENT_FOUR_PLUS_MIN_PATH_DP = 72f
    private const val RECENT_FOUR_PLUS_MIN_MS = 135L
}
