package com.goreecloud.keyboard

import kotlin.math.abs
import kotlin.math.hypot

/**
 * Local gesture decoder for GoreeCloud Quill.
 *
 * The decoder treats the traversed QWERTY keys as an ordered geometric path rather than a literal
 * character sequence. Candidate words are ranked by how well their letter path follows the gesture,
 * by ordered letter coverage, by excess detour, and finally by packaged-dictionary frequency.
 *
 * Gesture traces remain transient. This engine performs no editor reads, persistence, learning,
 * telemetry, account/contact/clipboard access, or network access.
 */
internal class SwipeTypingEngine {
    fun decode(
        keyPath: List<String>,
        dictionary: Collection<String>,
        limit: Int = 3,
    ): List<String> {
        if (limit <= 0) return emptyList()

        val traceLabels = normalizeTrace(keyPath)
        if (traceLabels.size < MIN_TRACE_KEYS) return emptyList()
        val tracePoints = traceLabels.mapNotNull(::pointFor)
        if (tracePoints.size != traceLabels.size) return emptyList()

        val first = traceLabels.first()
        val last = traceLabels.last()

        return dictionary.asSequence()
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .withIndex()
            .mapNotNull { indexed ->
                val word = indexed.value
                val wordLabels = normalizedWordTrace(word)
                if (wordLabels.size < MIN_TRACE_KEYS) return@mapNotNull null
                if (wordLabels.first() != first || wordLabels.last() != last) return@mapNotNull null

                val wordPoints = wordLabels.mapNotNull(::pointFor)
                if (wordPoints.size != wordLabels.size) return@mapNotNull null

                val orderedCost = orderedCoverageCost(wordPoints, tracePoints)
                if (!orderedCost.isFinite() || orderedCost > MAX_ORDERED_COST) return@mapNotNull null

                val candidateCoverage = averageDistanceToPolyline(wordPoints, tracePoints)
                if (!candidateCoverage.isFinite() || candidateCoverage > MAX_CANDIDATE_COVERAGE) {
                    return@mapNotNull null
                }

                val traceCoverage = averageDistanceToPolyline(tracePoints, wordPoints)
                val lengthPenalty = abs(wordLabels.size - traceLabels.size) * LENGTH_DELTA_WEIGHT
                val frequencyPenalty = indexed.index * FREQUENCY_WEIGHT

                SwipeCandidate(
                    word = word,
                    score =
                        orderedCost * ORDERED_WEIGHT +
                        candidateCoverage * CANDIDATE_COVERAGE_WEIGHT +
                        traceCoverage * TRACE_COVERAGE_WEIGHT +
                        lengthPenalty +
                        frequencyPenalty,
                    rank = indexed.index,
                )
            }
            .sortedWith(
                compareBy<SwipeCandidate> { it.score }
                    .thenBy { it.rank }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.word },
            )
            .take(limit)
            .map { it.word }
            .toList()
    }

    private fun normalizeTrace(keyPath: List<String>): List<String> {
        val result = mutableListOf<String>()
        for (label in keyPath) {
            val normalized = label.lowercase()
            if (normalized.length != 1 || normalized[0] !in 'a'..'z') continue
            if (result.lastOrNull() != normalized) result += normalized
        }
        return result
    }

    private fun normalizedWordTrace(word: String): List<String> {
        val result = mutableListOf<String>()
        word.lowercase().forEach { character ->
            if (character !in 'a'..'z') return emptyList()
            val label = character.toString()
            if (result.lastOrNull() != label) result += label
        }
        return result
    }

    /**
     * Greedily matches each intended letter to the nearest remaining gesture point. This strongly
     * penalizes candidates whose letters appear along the right route but in the wrong order.
     */
    private fun orderedCoverageCost(candidate: List<Point>, observed: List<Point>): Double {
        var searchStart = 0
        var total = 0.0

        candidate.forEachIndexed { candidateIndex, target ->
            if (candidateIndex == 0) {
                total += distance(target, observed.first())
                return@forEachIndexed
            }
            if (candidateIndex == candidate.lastIndex) {
                total += distance(target, observed.last())
                return@forEachIndexed
            }

            var bestIndex = -1
            var bestDistance = Double.POSITIVE_INFINITY
            for (index in searchStart until observed.lastIndex) {
                val value = distance(target, observed[index])
                if (value < bestDistance) {
                    bestDistance = value
                    bestIndex = index
                }
            }
            if (bestIndex < 0) return Double.POSITIVE_INFINITY
            searchStart = bestIndex
            total += bestDistance
        }

        return total / candidate.size
    }

    private fun averageDistanceToPolyline(points: List<Point>, polyline: List<Point>): Double {
        if (points.isEmpty() || polyline.isEmpty()) return Double.POSITIVE_INFINITY
        if (polyline.size == 1) {
            return points.sumOf { distance(it, polyline.first()) } / points.size
        }

        var total = 0.0
        for (point in points) {
            var best = Double.POSITIVE_INFINITY
            for (index in 0 until polyline.lastIndex) {
                best = minOf(best, distanceToSegment(point, polyline[index], polyline[index + 1]))
            }
            total += best
        }
        return total / points.size
    }

    private fun distanceToSegment(point: Point, start: Point, end: Point): Double {
        val dx = end.x - start.x
        val dy = end.y - start.y
        if (dx == 0.0 && dy == 0.0) return distance(point, start)

        val projection = (
            (point.x - start.x) * dx +
                (point.y - start.y) * dy
            ) / (dx * dx + dy * dy)
        val t = projection.coerceIn(0.0, 1.0)
        return hypot(
            point.x - (start.x + t * dx),
            point.y - (start.y + t * dy),
        )
    }

    private fun distance(left: Point, right: Point): Double =
        hypot(left.x - right.x, left.y - right.y)

    private fun pointFor(label: String): Point? = KEY_POINTS[label]

    private data class Point(val x: Double, val y: Double)

    private data class SwipeCandidate(
        val word: String,
        val score: Double,
        val rank: Int,
    )

    private companion object {
        const val MIN_TRACE_KEYS = 2
        const val MAX_ORDERED_COST = 1.25
        const val MAX_CANDIDATE_COVERAGE = 0.95
        const val ORDERED_WEIGHT = 2.2
        const val CANDIDATE_COVERAGE_WEIGHT = 1.5
        const val TRACE_COVERAGE_WEIGHT = 0.35
        const val LENGTH_DELTA_WEIGHT = 0.07
        const val FREQUENCY_WEIGHT = 0.0008

        val KEY_POINTS = buildMap {
            "qwertyuiop".forEachIndexed { index, character ->
                put(character.toString(), Point(index.toDouble(), 0.0))
            }
            "asdfghjkl".forEachIndexed { index, character ->
                put(character.toString(), Point(index + 0.45, 1.0))
            }
            "zxcvbnm".forEachIndexed { index, character ->
                put(character.toString(), Point(index + 0.95, 2.0))
            }
        }
    }
}
