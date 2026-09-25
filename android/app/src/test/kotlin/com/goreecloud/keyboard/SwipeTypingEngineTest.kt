package com.goreecloud.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SwipeTypingEngineTest {
    private val engine = SwipeTypingEngine()

    @Test
    fun decodesCollapsedRepeatedLetters() {
        val result = engine.decode(
            keyPath = listOf("h", "g", "f", "r", "e", "t", "y", "u", "i", "k", "l", "o"),
            dictionary = listOf("help", "hello", "hero"),
        )

        assertEquals("hello", result.first())
    }

    @Test
    fun toleratesOneAccidentalCrossedKey() {
        val result = engine.decode(
            keyPath = listOf("t", "h", "g", "e"),
            dictionary = listOf("the", "there", "time"),
        )

        assertTrue(result.contains("the"))
    }

    @Test
    fun requiresSameGestureEndpoints() {
        val result = engine.decode(
            keyPath = listOf("h", "e", "l", "o"),
            dictionary = listOf("world", "yellow"),
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun remainsBoundedByRequestedLimit() {
        val result = engine.decode(
            keyPath = listOf("t", "h", "e"),
            dictionary = listOf("the", "tie", "tee", "time"),
            limit = 2,
        )

        assertEquals(2, result.size)
    }
}
