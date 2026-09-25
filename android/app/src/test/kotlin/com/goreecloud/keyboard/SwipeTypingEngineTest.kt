package com.goreecloud.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SwipeTypingEngineTest {
    private val engine = SwipeTypingEngine()

    @Test
    fun decodesARealisticCrossedKeyHelloGesture() {
        val result = engine.decode(
            keyPath = listOf("h", "g", "f", "r", "e", "t", "y", "u", "i", "k", "l", "o"),
            dictionary = listOf("help", "hello", "hero"),
        )

        assertEquals("hello", result.first())
    }

    @Test
    fun geometricRouteDistinguishesShowsFromNearbyShoes() {
        val result = engine.decode(
            keyPath = listOf("s", "d", "f", "g", "h", "y", "u", "i", "o", "w", "e", "d", "s"),
            dictionary = listOf("shows", "shoes", "says"),
        )

        assertEquals("shows", result.first())
    }

    @Test
    fun toleratesOneAccidentalCrossedKey() {
        val result = engine.decode(
            keyPath = listOf("t", "h", "g", "e"),
            dictionary = listOf("the", "there", "time"),
        )

        assertEquals("the", result.first())
    }

    @Test
    fun toleratesNeighboringEndpointDrift() {
        val result = engine.decode(
            keyPath = listOf("g", "e", "l", "p"),
            dictionary = listOf("hello", "help", "world"),
        )

        assertTrue("Nearby start/end drift should still produce useful local candidates", result.isNotEmpty())
        assertTrue(result.first() in setOf("hello", "help"))
    }

    @Test
    fun rejectsDistantGestureEndpoints() {
        val result = engine.decode(
            keyPath = listOf("h", "e", "l", "o"),
            dictionary = listOf("world", "yellow"),
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun remainsBoundedByRequestedLimit() {
        val result = engine.decode(
            keyPath = listOf("t", "h", "g", "e"),
            dictionary = listOf("the", "there", "time"),
            limit = 2,
        )

        assertEquals(2, result.size)
    }
}
