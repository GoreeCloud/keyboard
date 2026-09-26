package com.goreecloud.keyboard

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.ln

internal data class SwipePoint(val x: Float, val y: Float)

internal data class SwipeGesture(
    val keyPath: List<String>,
    val points: List<SwipePoint>,
    val keyCenters: Map<String, SwipePoint>,
)

/**
 * Local gesture decoder for GoreeCloud Keyboard.
 *
 * Physical-device decoding prefers the actual sampled finger path plus the current rendered key
 * centers. The legacy key-path entrypoint remains for deterministic unit tests and fallback. No
 * gesture trace is persisted or transmitted.
 */
internal class SwipeTypingEngine {
    fun decode(
        gesture: SwipeGesture,
        dictionary: Collection<String>,
        limit: Int = 3,
    ): List<String> {
        if (limit <= 0) return emptyList()

        val traceLabels = normalizeTrace(gesture.keyPath)
        if (traceLabels.size < MIN_TRACE_KEYS) return emptyList()

        val centers = gesture.keyCenters
            .mapKeys { it.key.lowercase() }
            .mapValues { Point(it.value.x.toDouble(), it.value.y.toDouble()) }

        val scale = keyboardScale(centers)
        val sampledTrace = simplifyTrace(
            gesture.points.map { Point(it.x.toDouble(), it.y.toDouble()) },
            minimumSpacing = scale * MIN_TRACE_SAMPLE_SPACING,
        )

        if (
            sampledTrace.size < MIN_TRACE_POINTS ||
            !scale.isFinite() ||
            scale <= 0.0
        ) {
            return decode(gesture.keyPath, dictionary, limit)
        }

        return dictionary.asSequence()
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .withIndex()
            .mapNotNull { indexed ->
                val word = indexed.value
                val wordLabels = normalizedWordTrace(word)
                if (wordLabels.size < MIN_TRACE_KEYS) return@mapNotNull null

                val wordPoints = wordLabels.mapNotNull(centers::get)
                if (wordPoints.size != wordLabels.size) return@mapNotNull null

                val startDistance = distance(wordPoints.first(), sampledTrace.first()) / scale
                val endDistance = distance(wordPoints.last(), sampledTrace.last()) / scale
                if (
                    startDistance > MAX_PHYSICAL_ENDPOINT_DISTANCE ||
                    endDistance > MAX_PHYSICAL_ENDPOINT_DISTANCE
                ) return@mapNotNull null

                val orderedCost = orderedCoverageCost(wordPoints, sampledTrace) / scale
                if (!orderedCost.isFinite() || orderedCost > MAX_PHYSICAL_ORDERED_COST) {
                    return@mapNotNull null
                }

                val candidateCoverage =
                    averageDistanceToPolyline(wordPoints, sampledTrace) / scale
                if (
                    !candidateCoverage.isFinite() ||
                    candidateCoverage > MAX_PHYSICAL_CANDIDATE_COVERAGE
                ) return@mapNotNull null

                val traceCoverage =
                    averageDistanceToPolyline(sampledTrace, wordPoints) / scale
                val sequencePenalty =
                    sequenceDistance(traceLabels, wordLabels) * SEQUENCE_DISTANCE_WEIGHT
                val lengthPenalty =
                    abs(wordLabels.size - traceLabels.size) * PHYSICAL_LENGTH_DELTA_WEIGHT
                val endpointPenalty =
                    (startDistance + endDistance) * PHYSICAL_ENDPOINT_WEIGHT
                val frequencyPenalty =
                    ln(indexed.index.toDouble() + 2.0) * PHYSICAL_FREQUENCY_LOG_WEIGHT

                SwipeCandidate(
                    word = word,
                    score =
                        endpointPenalty +
                        orderedCost * PHYSICAL_ORDERED_WEIGHT +
                        candidateCoverage * PHYSICAL_CANDIDATE_COVERAGE_WEIGHT +
                        traceCoverage * PHYSICAL_TRACE_COVERAGE_WEIGHT +
                        sequencePenalty +
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

        return dictionary.asSequence()
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .withIndex()
            .mapNotNull { indexed ->
                val word = indexed.value
                val wordLabels = normalizedWordTrace(word)
                if (wordLabels.size < MIN_TRACE_KEYS) return@mapNotNull null

                val wordPoints = wordLabels.mapNotNull(::pointFor)
                if (wordPoints.size != wordLabels.size) return@mapNotNull null

                val startDistance = distance(wordPoints.first(), tracePoints.first())
                val endDistance = distance(wordPoints.last(), tracePoints.last())
                if (
                    startDistance > MAX_ENDPOINT_DISTANCE ||
                    endDistance > MAX_ENDPOINT_DISTANCE
                ) return@mapNotNull null

                val orderedCost = orderedCoverageCost(wordPoints, tracePoints)
                if (!orderedCost.isFinite() || orderedCost > MAX_ORDERED_COST) return@mapNotNull null

                val candidateCoverage = averageDistanceToPolyline(wordPoints, tracePoints)
                if (!candidateCoverage.isFinite() || candidateCoverage > MAX_CANDIDATE_COVERAGE) {
                    return@mapNotNull null
                }

                val traceCoverage = averageDistanceToPolyline(tracePoints, wordPoints)
                val sequencePenalty =
                    sequenceDistance(traceLabels, wordLabels) * SEQUENCE_DISTANCE_WEIGHT
                val lengthPenalty = abs(wordLabels.size - traceLabels.size) * LENGTH_DELTA_WEIGHT
                val endpointPenalty = (startDistance + endDistance) * ENDPOINT_WEIGHT
                val frequencyPenalty = ln(indexed.index.toDouble() + 2.0) * FREQUENCY_LOG_WEIGHT

                SwipeCandidate(
                    word = word,
                    score =
                        endpointPenalty +
                        orderedCost * ORDERED_WEIGHT +
                        candidateCoverage * CANDIDATE_COVERAGE_WEIGHT +
                        traceCoverage * TRACE_COVERAGE_WEIGHT +
                        sequencePenalty +
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

    private fun simplifyTrace(points: List<Point>, minimumSpacing: Double): List<Point> {
        if (points.size <= 2) return points
        val result = mutableListOf(points.first())
        for (point in points.drop(1).dropLast(1)) {
            if (distance(result.last(), point) >= minimumSpacing) result += point
            if (result.size >= MAX_TRACE_POINTS - 1) break
        }
        if (result.last() != points.last()) result += points.last()
        return result
    }

    private fun keyboardScale(centers: Map<String, Point>): Double {
        val neighborPairs = listOf(
            "q" to "w", "w" to "e", "a" to "s", "s" to "d", "z" to "x", "x" to "c",
        )
        val values = neighborPairs.mapNotNull { (left, right) ->
            val a = centers[left] ?: return@mapNotNull null
            val b = centers[right] ?: return@mapNotNull null
            distance(a, b)
        }
        if (values.isEmpty()) return Double.NaN
        return values.average()
    }

    /**
     * Greedily matches each intended letter to the nearest remaining gesture point, preserving
     * order so routes that pass near the right letters in the wrong sequence are penalized.
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

    private fun sequenceDistance(observed: List<String>, candidate: List<String>): Double {
        if (observed.isEmpty() || candidate.isEmpty()) return 1.0
        val matrix = Array(observed.size + 1) { IntArray(candidate.size + 1) }
        for (i in observed.indices) matrix[i + 1][0] = i + 1
        for (j in candidate.indices) matrix[0][j + 1] = j + 1
        for (i in observed.indices) {
            for (j in candidate.indices) {
                val substitution = if (observed[i] == candidate[j]) 0 else 1
                matrix[i + 1][j + 1] = minOf(
                    matrix[i][j + 1] + 1,
                    matrix[i + 1][j] + 1,
                    matrix[i][j] + substitution,
                )
            }
        }
        return matrix[observed.size][candidate.size].toDouble() /
            maxOf(observed.size, candidate.size)
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
        const val MIN_TRACE_POINTS = 3
        const val MAX_TRACE_POINTS = 72
        const val MIN_TRACE_SAMPLE_SPACING = 0.16

        const val MAX_PHYSICAL_ENDPOINT_DISTANCE = 1.75
        const val MAX_PHYSICAL_ORDERED_COST = 1.65
        const val MAX_PHYSICAL_CANDIDATE_COVERAGE = 1.20
        const val PHYSICAL_ENDPOINT_WEIGHT = 1.30
        const val PHYSICAL_ORDERED_WEIGHT = 2.15
        const val PHYSICAL_CANDIDATE_COVERAGE_WEIGHT = 1.55
        const val PHYSICAL_TRACE_COVERAGE_WEIGHT = 0.28
        const val PHYSICAL_LENGTH_DELTA_WEIGHT = 0.055
        const val PHYSICAL_FREQUENCY_LOG_WEIGHT = 0.012

        const val MAX_ENDPOINT_DISTANCE = 1.45
        const val MAX_ORDERED_COST = 1.45
        const val MAX_CANDIDATE_COVERAGE = 1.10
        const val ENDPOINT_WEIGHT = 1.25
        const val ORDERED_WEIGHT = 2.0
        const val CANDIDATE_COVERAGE_WEIGHT = 1.5
        const val TRACE_COVERAGE_WEIGHT = 0.35
        const val LENGTH_DELTA_WEIGHT = 0.07
        const val FREQUENCY_LOG_WEIGHT = 0.02
        const val SEQUENCE_DISTANCE_WEIGHT = 0.55

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
