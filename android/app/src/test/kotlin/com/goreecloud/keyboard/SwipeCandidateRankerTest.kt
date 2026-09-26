package com.goreecloud.keyboard

import org.junit.Assert.assertEquals
import org.junit.Test

class SwipeCandidateRankerTest {
    @Test
    fun contextMayPromoteOnlyAlreadyDecodedCandidates() {
        assertEquals(
            listOf("three", "there", "these"),
            SwipeCandidateRanker.rank(
                decoded = listOf("three", "these", "there", "theme"),
                contextualPredictions = listOf("there", "they", "the"),
                limit = 3,
            ),
        )
    }

    @Test
    fun contextCannotPullWeakGeometryCandidateToTheTop() {
        assertEquals(
            listOf("hello", "help", "held"),
            SwipeCandidateRanker.rank(
                decoded = listOf("hello", "help", "held", "hero", "home", "how", "however"),
                contextualPredictions = listOf("however"),
                limit = 3,
            ),
        )
    }

    @Test
    fun decoderOrderRemainsWhenContextDoesNotMatch() {
        assertEquals(
            listOf("hello", "help", "held"),
            SwipeCandidateRanker.rank(
                decoded = listOf("hello", "help", "held"),
                contextualPredictions = listOf("there", "again", "today"),
            ),
        )
    }

    @Test
    fun resultRemainsBounded() {
        assertEquals(
            listOf("the", "there"),
            SwipeCandidateRanker.rank(
                decoded = listOf("there", "the", "then"),
                contextualPredictions = listOf("the"),
                limit = 2,
            ),
        )
    }
}
