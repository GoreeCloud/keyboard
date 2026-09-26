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
    fun physicalStatisticalGestureRecognizesIcon() {
        val centers = qwertyCenters()
        val labels = listOf("i", "c", "o", "n")
        val gesture = SwipeGesture(
            keyPath = labels,
            points = interpolate(labels.map { centers.getValue(it) }),
            keyCenters = centers,
        )

        val result = engine.decode(
            gesture = gesture,
            dictionary = listOf("iron", "icon", "into", "upon", "icons"),
            limit = 3,
        )

        assertEquals("icon", result.first())
    }

    @Test
    fun physicalStatisticalGestureHandlesRepeatedLettersWithoutLiteralLoop() {
        val centers = qwertyCenters()
        val labels = listOf("h", "e", "l", "o")
        val gesture = SwipeGesture(
            keyPath = labels,
            points = interpolate(labels.map { centers.getValue(it) }),
            keyCenters = centers,
        )

        val result = engine.decode(
            gesture = gesture,
            dictionary = listOf("help", "hello", "hero"),
            limit = 3,
        )

        assertEquals("hello", result.first())
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
    private fun qwertyCenters(): Map<String, SwipePoint> = buildMap {
        "qwertyuiop".forEachIndexed { index, c ->
            put(c.toString(), SwipePoint(index * 100f + 50f, 50f))
        }
        "asdfghjkl".forEachIndexed { index, c ->
            put(c.toString(), SwipePoint(index * 100f + 95f, 150f))
        }
        "zxcvbnm".forEachIndexed { index, c ->
            put(c.toString(), SwipePoint(index * 100f + 145f, 250f))
        }
    }

    private fun interpolate(
        anchors: List<SwipePoint>,
        samplesPerSegment: Int = 6,
    ): List<SwipePoint> {
        if (anchors.size < 2) return anchors
        val result = mutableListOf<SwipePoint>()
        anchors.zipWithNext().forEachIndexed { segmentIndex, (start, end) ->
            if (segmentIndex == 0) result += start
            for (step in 1..samplesPerSegment) {
                val t = step / samplesPerSegment.toFloat()
                result += SwipePoint(
                    x = start.x + (end.x - start.x) * t,
                    y = start.y + (end.y - start.y) * t,
                )
            }
        }
        return result
    }

}
