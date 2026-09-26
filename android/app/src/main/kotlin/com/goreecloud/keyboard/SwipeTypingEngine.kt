package com.goreecloud.keyboard

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

/*
 * Physical swipe-ranking design is a GoreeCloud-native rewrite informed by the Apache-2.0
 * FlorisBoard/SwiftFloris StatisticalGlideTypingClassifier, which in turn credits the
 * AnySoftKeyboard statistical gesture-typing work. This file does not copy their UI, branding,
 * data model, or source layout; provenance and license references are recorded in
 * THIRD-PARTY-NOTICES.md.
 */

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
        if (centers.size < MIN_LAYOUT_KEYS) return decode(gesture.keyPath, dictionary, limit)

        val scale = keyboardScale(centers)
        if (!scale.isFinite() || scale <= 0.0) return decode(gesture.keyPath, dictionary, limit)

        val simplified = simplifyTrace(
            gesture.points.map { Point(it.x.toDouble(), it.y.toDouble()) },
            minimumSpacing = scale * MIN_TRACE_SAMPLE_SPACING,
        )
        if (simplified.size < MIN_TRACE_POINTS) return decode(gesture.keyPath, dictionary, limit)

        val observed = resamplePolyline(simplified, STATISTICAL_SAMPLE_POINTS)
        val normalizedObserved = normalizePolyline(observed)
        val observedLength = polylineLength(simplified).coerceAtLeast(scale * 0.5)

        val likelyStarts = nearestKeyLabels(observed.first(), centers, ENDPOINT_KEY_CANDIDATES)
        val likelyEnds = nearestKeyLabels(observed.last(), centers, ENDPOINT_KEY_CANDIDATES)

        return dictionary.asSequence()
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .withIndex()
            .mapNotNull { indexed ->
                val word = indexed.value
                val wordLabels = normalizedWordTrace(word)
                if (wordLabels.size < MIN_TRACE_KEYS) return@mapNotNull null
                if (wordLabels.first() !in likelyStarts || wordLabels.last() !in likelyEnds) {
                    return@mapNotNull null
                }

                val variants = idealGestureVariants(word, centers, scale)
                if (variants.isEmpty()) return@mapNotNull null

                var bestScore = Double.POSITIVE_INFINITY
                for (variant in variants) {
                    val idealLength = polylineLength(variant)
                    if (idealLength <= 0.0) continue

                    val lengthRatioPenalty = abs(
                        kotlin.math.ln((idealLength / observedLength).coerceAtLeast(0.0001)),
                    )
                    if (lengthRatioPenalty > MAX_LOG_LENGTH_RATIO) continue

                    val ideal = resamplePolyline(variant, STATISTICAL_SAMPLE_POINTS)
                    val normalizedIdeal = normalizePolyline(ideal)

                    val shapeDistance = meanPointDistance(normalizedIdeal, normalizedObserved)
                    val locationDistance = meanPointDistance(ideal, observed) / scale
                    val endpointDistance = (
                        distance(ideal.first(), observed.first()) +
                            distance(ideal.last(), observed.last())
                        ) / scale
                    val sequencePenalty =
                        sequenceDistance(traceLabels, wordLabels) * STATISTICAL_SEQUENCE_WEIGHT
                    val rankPenalty =
                        ln(indexed.index.toDouble() + 2.0) * STATISTICAL_FREQUENCY_LOG_WEIGHT

                    val score =
                        shapeDistance * STATISTICAL_SHAPE_WEIGHT +
                            locationDistance * STATISTICAL_LOCATION_WEIGHT +
                            endpointDistance * STATISTICAL_ENDPOINT_WEIGHT +
                            lengthRatioPenalty * STATISTICAL_LENGTH_WEIGHT +
                            sequencePenalty +
                            rankPenalty

                    if (score < bestScore) bestScore = score
                }

                if (!bestScore.isFinite() || bestScore > MAX_STATISTICAL_SCORE) {
                    return@mapNotNull null
                }

                SwipeCandidate(word = word, score = bestScore, rank = indexed.index)
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

    private fun idealGestureVariants(
        word: String,
        centers: Map<String, Point>,
        scale: Double,
    ): List<List<Point>> {
        val basic = mutableListOf<Point>()
        val looped = mutableListOf<Point>()
        var previous: String? = null
        var hasDuplicate = false

        for (character in word.lowercase()) {
            if (character !in 'a'..'z') return emptyList()
            val label = character.toString()
            val center = centers[label] ?: return emptyList()
            basic += center

            if (previous == label) {
                hasDuplicate = true
                val r = scale * DUPLICATE_LETTER_LOOP_RADIUS
                looped += Point(center.x + r, center.y + r)
                looped += Point(center.x + r, center.y - r)
                looped += Point(center.x - r, center.y - r)
                looped += Point(center.x - r, center.y + r)
            }
            looped += center
            previous = label
        }

        val variants = mutableListOf<List<Point>>()
        if (basic.size >= 2) variants += basic
        if (hasDuplicate && looped.size >= 2) variants += looped
        return variants
    }

    private fun nearestKeyLabels(
        point: Point,
        centers: Map<String, Point>,
        count: Int,
    ): Set<String> =
        centers.entries
            .sortedBy { (_, center) -> distance(point, center) }
            .take(count.coerceAtLeast(1))
            .mapTo(linkedSetOf()) { it.key }

    private fun resamplePolyline(points: List<Point>, count: Int): List<Point> {
        if (points.isEmpty() || count <= 0) return emptyList()
        if (points.size == 1 || count == 1) return List(count) { points.first() }

        val segmentLengths = DoubleArray(points.size - 1)
        var total = 0.0
        for (index in 0 until points.lastIndex) {
            val length = distance(points[index], points[index + 1])
            segmentLengths[index] = length
            total += length
        }
        if (total <= 0.000001) return List(count) { points.first() }

        val result = ArrayList<Point>(count)
        var segmentIndex = 0
        var segmentStartDistance = 0.0
        for (sample in 0 until count) {
            val target = if (count == 1) 0.0 else total * sample / (count - 1).toDouble()
            while (
                segmentIndex < segmentLengths.lastIndex &&
                segmentStartDistance + segmentLengths[segmentIndex] < target
            ) {
                segmentStartDistance += segmentLengths[segmentIndex]
                segmentIndex += 1
            }

            val startPoint = points[segmentIndex]
            val endPoint = points[segmentIndex + 1]
            val segmentLength = segmentLengths[segmentIndex]
            val t = if (segmentLength <= 0.000001) {
                0.0
            } else {
                ((target - segmentStartDistance) / segmentLength).coerceIn(0.0, 1.0)
            }
            result += Point(
                x = startPoint.x + (endPoint.x - startPoint.x) * t,
                y = startPoint.y + (endPoint.y - startPoint.y) * t,
            )
        }
        return result
    }

    private fun normalizePolyline(points: List<Point>): List<Point> {
        if (points.isEmpty()) return emptyList()
        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val minY = points.minOf { it.y }
        val maxY = points.maxOf { it.y }
        val side = max(maxX - minX, maxY - minY).coerceAtLeast(0.000001)
        val centerX = (minX + maxX) / 2.0
        val centerY = (minY + maxY) / 2.0
        return points.map { point ->
            Point(
                x = (point.x - centerX) / side,
                y = (point.y - centerY) / side,
            )
        }
    }

    private fun meanPointDistance(left: List<Point>, right: List<Point>): Double {
        if (left.isEmpty() || right.isEmpty() || left.size != right.size) {
            return Double.POSITIVE_INFINITY
        }
        var total = 0.0
        for (index in left.indices) {
            total += distance(left[index], right[index])
        }
        return total / left.size
    }

    private fun polylineLength(points: List<Point>): Double {
        if (points.size < 2) return 0.0
        var total = 0.0
        for (index in 0 until points.lastIndex) {
            total += distance(points[index], points[index + 1])
        }
        return total
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


    /**
     * Keeps the route endpoints plus meaningful bends. This follows the same broad principle used
     * by mature open-source gesture keyboards: dense pointer streams are less useful than a small
     * set of direction-changing points. This implementation is GoreeCloud-native and operates on
     * normalized rendered geometry rather than imported upstream structures.
     */
    private fun extractCorners(points: List<Point>): List<Point> {
        if (points.size <= 2) return points

        val corners = mutableListOf(points.first())
        for (index in 1 until points.lastIndex) {
            val previous = points[index - 1]
            val current = points[index]
            val next = points[index + 1]
            val incomingX = current.x - previous.x
            val incomingY = current.y - previous.y
            val outgoingX = next.x - current.x
            val outgoingY = next.y - current.y
            val incomingLength = hypot(incomingX, incomingY)
            val outgoingLength = hypot(outgoingX, outgoingY)
            if (incomingLength <= 0.000001 || outgoingLength <= 0.000001) continue

            val cosine = (
                incomingX * outgoingX + incomingY * outgoingY
            ) / (incomingLength * outgoingLength)
            val turnStrength = 1.0 - cosine.coerceIn(-1.0, 1.0)
            if (turnStrength >= CORNER_TURN_STRENGTH) corners += current
        }
        corners += points.last()
        return corners
    }

    /**
     * Compares route direction independently of absolute pointer position. A candidate that passes
     * near the same keys but travels through them in the wrong direction receives a strong penalty.
     */
    private fun directionalShapeCost(
        candidate: List<Point>,
        observed: List<Point>,
    ): Double {
        if (candidate.size < 2 || observed.size < 2) return Double.POSITIVE_INFINITY

        val sampleCount = maxOf(
            MIN_DIRECTION_SAMPLES,
            minOf(MAX_DIRECTION_SAMPLES, maxOf(candidate.size, observed.size) * 2),
        )
        val candidateSamples = resamplePolyline(candidate, sampleCount)
        val observedSamples = resamplePolyline(observed, sampleCount)
        if (candidateSamples.size != sampleCount || observedSamples.size != sampleCount) {
            return Double.POSITIVE_INFINITY
        }

        var total = 0.0
        var compared = 0
        for (index in 0 until sampleCount - 1) {
            val candidateDx = candidateSamples[index + 1].x - candidateSamples[index].x
            val candidateDy = candidateSamples[index + 1].y - candidateSamples[index].y
            val observedDx = observedSamples[index + 1].x - observedSamples[index].x
            val observedDy = observedSamples[index + 1].y - observedSamples[index].y

            val candidateLength = hypot(candidateDx, candidateDy)
            val observedLength = hypot(observedDx, observedDy)
            if (candidateLength <= 0.000001 || observedLength <= 0.000001) continue

            val cosine = (
                candidateDx * observedDx + candidateDy * observedDy
            ) / (candidateLength * observedLength)
            total += (1.0 - cosine.coerceIn(-1.0, 1.0)) * 0.5
            compared += 1
        }

        return if (compared == 0) Double.POSITIVE_INFINITY else total / compared
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

    private fun normalizedShapeCost(
        candidate: List<Point>,
        observed: List<Point>,
        scale: Double,
    ): Double {
        if (candidate.size < 2 || observed.size < 2 || scale <= 0.0) {
            return Double.POSITIVE_INFINITY
        }
        val candidateSamples = resamplePolyline(candidate, SHAPE_SAMPLE_COUNT)
        val observedSamples = resamplePolyline(observed, SHAPE_SAMPLE_COUNT)
        if (
            candidateSamples.size != SHAPE_SAMPLE_COUNT ||
            observedSamples.size != SHAPE_SAMPLE_COUNT
        ) {
            return Double.POSITIVE_INFINITY
        }

        return candidateSamples.zip(observedSamples)
            .sumOf { (left, right) -> distance(left, right) / scale } /
            SHAPE_SAMPLE_COUNT
    }

    private fun resamplePolyline(points: List<Point>, count: Int): List<Point> {
        if (points.isEmpty() || count <= 0) return emptyList()
        if (points.size == 1 || count == 1) return List(count) { points.first() }

        val cumulative = DoubleArray(points.size)
        for (index in 1 until points.size) {
            cumulative[index] =
                cumulative[index - 1] + distance(points[index - 1], points[index])
        }
        val totalLength = cumulative.last()
        if (totalLength <= 0.0) return List(count) { points.first() }

        val result = ArrayList<Point>(count)
        var segment = 0
        repeat(count) { sampleIndex ->
            val target =
                if (count == 1) 0.0
                else totalLength * sampleIndex.toDouble() / (count - 1).toDouble()

            while (
                segment < points.lastIndex - 1 &&
                cumulative[segment + 1] < target
            ) {
                segment += 1
            }

            val start = points[segment]
            val end = points[minOf(segment + 1, points.lastIndex)]
            val segmentStart = cumulative[segment]
            val segmentLength =
                (cumulative[minOf(segment + 1, cumulative.lastIndex)] - segmentStart)
                    .coerceAtLeast(0.000001)
            val t = ((target - segmentStart) / segmentLength).coerceIn(0.0, 1.0)
            result += Point(
                x = start.x + (end.x - start.x) * t,
                y = start.y + (end.y - start.y) * t,
            )
        }
        return result
    }

    private fun polylineLength(points: List<Point>): Double {
        if (points.size < 2) return 0.0
        var total = 0.0
        for (index in 0 until points.lastIndex) {
            total += distance(points[index], points[index + 1])
        }
        return total
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
        const val MIN_LAYOUT_KEYS = 20
        const val MAX_TRACE_POINTS = 72
        const val MIN_TRACE_SAMPLE_SPACING = 0.12

        // Statistical physical-gesture path, rewritten from permissively licensed FOSS concepts.
        const val STATISTICAL_SAMPLE_POINTS = 64
        const val ENDPOINT_KEY_CANDIDATES = 3
        const val DUPLICATE_LETTER_LOOP_RADIUS = 0.22
        const val MAX_LOG_LENGTH_RATIO = 1.05
        const val MAX_STATISTICAL_SCORE = 5.40
        const val STATISTICAL_SHAPE_WEIGHT = 2.65
        const val STATISTICAL_LOCATION_WEIGHT = 1.15
        const val STATISTICAL_ENDPOINT_WEIGHT = 0.90
        const val STATISTICAL_LENGTH_WEIGHT = 0.70
        const val STATISTICAL_SEQUENCE_WEIGHT = 0.35
        const val STATISTICAL_FREQUENCY_LOG_WEIGHT = 0.010

        const val MAX_PHYSICAL_START_DISTANCE = 1.45
        const val MAX_PHYSICAL_END_DISTANCE = 2.05
        const val MAX_PHYSICAL_ORDERED_COST = 1.65
        const val MAX_PHYSICAL_CANDIDATE_COVERAGE = 1.20
        const val PHYSICAL_ENDPOINT_WEIGHT = 1.30
        const val PHYSICAL_ORDERED_WEIGHT = 2.15
        const val PHYSICAL_CANDIDATE_COVERAGE_WEIGHT = 1.55
        const val PHYSICAL_TRACE_COVERAGE_WEIGHT = 0.24
        const val PHYSICAL_SHAPE_WEIGHT = 2.65
        const val PHYSICAL_DIRECTION_WEIGHT = 1.45
        const val PHYSICAL_ROUTE_LENGTH_WEIGHT = 0.45
        const val PHYSICAL_LENGTH_DELTA_WEIGHT = 0.045
        const val PHYSICAL_FREQUENCY_LOG_WEIGHT = 0.010
        const val MAX_PHYSICAL_SHAPE_COST = 1.35
        const val MAX_DIRECTION_COST = 0.82
        const val CORNER_TURN_STRENGTH = 0.12
        const val MIN_DIRECTION_SAMPLES = 8
        const val MAX_DIRECTION_SAMPLES = 20
        const val SHAPE_SAMPLE_COUNT = 24

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
