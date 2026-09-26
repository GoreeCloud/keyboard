package com.goreecloud.keyboard

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

/*
 * GoreeCloud modification/adaptation notice:
 *
 * Portions of the physical swipe-ranking design are a GoreeCloud-native adaptation of concepts
 * and permissively licensed implementation work from two Apache-2.0 keyboards:
 *
 * - FlorisBoard StatisticalGlideTypingClassifier, Copyright (C) 2025 The FlorisBoard Contributors:
 *   endpoint pruning, ideal word gestures, uniform resampling, shape-vs-location separation,
 *   path-length pruning, duplicate-letter gesture variants, and frequency-aware ranking.
 * - AnySoftKeyboard gesture-typing work: meaningful-point filtering, curvature/corner emphasis,
 *   start/end proximity handling, and direction-aware path comparison.
 *
 * This GoreeCloud file has been substantially modified/reimplemented for GoreeCloud's own data
 * structures, thresholds, privacy boundary, context reranking, UI, tests, and lifecycle. Exact
 * inspected upstream revisions and notices are recorded in THIRD-PARTY-NOTICES.md. The applicable
 * Apache License 2.0 text is preserved at LICENSES/Apache-2.0.txt.
 */

data class SwipePoint(val x: Float, val y: Float)

data class SwipeGesture(
    val keyPath: List<String>,
    val points: List<SwipePoint>,
    val keyCenters: Map<String, SwipePoint>,
)

/**
 * Local-only gesture decoder for GoreeCloud Keyboard.
 *
 * The physical path is never persisted or transmitted. Physical-device decoding uses the actual
 * pointer samples and rendered QWERTY key centers. The key-path overload remains as a deterministic
 * fallback and unit-test surface.
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
        if (centers.size < MIN_LAYOUT_KEYS) {
            return decode(gesture.keyPath, dictionary, limit)
        }

        val scale = keyboardScale(centers)
        if (!scale.isFinite() || scale <= 0.0) {
            return decode(gesture.keyPath, dictionary, limit)
        }

        val simplified = simplifyTrace(
            gesture.points.map { Point(it.x.toDouble(), it.y.toDouble()) },
            minimumSpacing = scale * MIN_TRACE_SAMPLE_SPACING,
        )
        if (simplified.size < MIN_TRACE_POINTS) {
            return decode(gesture.keyPath, dictionary, limit)
        }

        val observed = resamplePolyline(simplified, STATISTICAL_SAMPLE_POINTS)
        val normalizedObserved = normalizePolyline(observed)
        val observedCorners = extractCorners(simplified)
        val observedLength = polylineLength(simplified).coerceAtLeast(scale * 0.5)

        val likelyStarts = nearestKeyLabels(
            point = observed.first(),
            centers = centers,
            count = START_KEY_CANDIDATES,
        )
        val likelyEnds = nearestKeyLabels(
            point = observed.last(),
            centers = centers,
            count = END_KEY_CANDIDATES,
        )

        val physicalCandidates = dictionary.asSequence()
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
                        ln((idealLength / observedLength).coerceAtLeast(0.0001)),
                    )
                    if (lengthRatioPenalty > MAX_LOG_LENGTH_RATIO) continue

                    val ideal = resamplePolyline(variant, STATISTICAL_SAMPLE_POINTS)
                    val startDistance = distance(ideal.first(), observed.first()) / scale
                    val endDistance = distance(ideal.last(), observed.last()) / scale
                    if (
                        startDistance > MAX_PHYSICAL_START_DISTANCE ||
                        endDistance > MAX_PHYSICAL_END_DISTANCE
                    ) {
                        continue
                    }

                    val normalizedIdeal = normalizePolyline(ideal)
                    val shapeDistance = meanPointDistance(normalizedIdeal, normalizedObserved)
                    if (!shapeDistance.isFinite() || shapeDistance > MAX_NORMALIZED_SHAPE_DISTANCE) {
                        continue
                    }

                    val locationDistance = meanPointDistance(ideal, observed) / scale
                    val directionMismatch = directionMismatchCost(ideal, observed)
                    if (
                        !directionMismatch.isFinite() ||
                        directionMismatch > MAX_DIRECTION_MISMATCH
                    ) {
                        continue
                    }

                    val idealCorners = extractCorners(variant)
                    val cornerCoverage = if (
                        idealCorners.size >= 2 &&
                        observedCorners.size >= 2
                    ) {
                        orderedCoverageCost(idealCorners, observedCorners) / scale
                    } else {
                        0.0
                    }
                    if (!cornerCoverage.isFinite() || cornerCoverage > MAX_CORNER_COVERAGE) {
                        continue
                    }

                    val sequencePenalty =
                        sequenceDistance(traceLabels, wordLabels) * SEQUENCE_DISTANCE_WEIGHT
                    val rankPenalty =
                        ln(indexed.index.toDouble() + 2.0) * FREQUENCY_LOG_WEIGHT
                    val endpointPenalty =
                        startDistance * START_ENDPOINT_WEIGHT +
                            endDistance * END_ENDPOINT_WEIGHT

                    val score =
                        shapeDistance * SHAPE_WEIGHT +
                            locationDistance * LOCATION_WEIGHT +
                            directionMismatch * DIRECTION_WEIGHT +
                            cornerCoverage * CORNER_WEIGHT +
                            lengthRatioPenalty * LENGTH_WEIGHT +
                            endpointPenalty +
                            sequencePenalty +
                            rankPenalty

                    if (score < bestScore) bestScore = score
                }

                if (!bestScore.isFinite() || bestScore > MAX_TOTAL_SCORE) {
                    return@mapNotNull null
                }

                SwipeCandidate(
                    word = word,
                    score = bestScore,
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

        if (physicalCandidates.size >= limit) return physicalCandidates
        val fallbackCandidates = decode(
            keyPath = gesture.keyPath,
            dictionary = dictionary,
            limit = limit,
        )
        return (physicalCandidates + fallbackCandidates)
            .distinctBy { it.lowercase() }
            .take(limit)
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

        val likelyStarts = nearestKeyLabels(
            tracePoints.first(),
            KEY_POINTS,
            START_KEY_CANDIDATES,
        )
        val likelyEnds = nearestKeyLabels(
            tracePoints.last(),
            KEY_POINTS,
            END_KEY_CANDIDATES,
        )

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

                val wordPoints = wordLabels.mapNotNull(::pointFor)
                if (wordPoints.size != wordLabels.size) return@mapNotNull null

                val orderedCost = orderedCoverageCost(wordPoints, tracePoints)
                if (!orderedCost.isFinite() || orderedCost > FALLBACK_MAX_ORDERED_COST) {
                    return@mapNotNull null
                }

                val candidateCoverage = averageDistanceToPolyline(wordPoints, tracePoints)
                if (
                    !candidateCoverage.isFinite() ||
                    candidateCoverage > FALLBACK_MAX_CANDIDATE_COVERAGE
                ) {
                    return@mapNotNull null
                }

                val traceCoverage = averageDistanceToPolyline(tracePoints, wordPoints)
                val directionMismatch = directionMismatchCost(
                    resamplePolyline(wordPoints, FALLBACK_SAMPLE_POINTS),
                    resamplePolyline(tracePoints, FALLBACK_SAMPLE_POINTS),
                )
                val startDistance = distance(wordPoints.first(), tracePoints.first())
                val endDistance = distance(wordPoints.last(), tracePoints.last())
                val sequencePenalty =
                    sequenceDistance(traceLabels, wordLabels) * FALLBACK_SEQUENCE_WEIGHT
                val lengthPenalty =
                    abs(wordLabels.size - traceLabels.size) * FALLBACK_LENGTH_WEIGHT
                val frequencyPenalty =
                    ln(indexed.index.toDouble() + 2.0) * FALLBACK_FREQUENCY_WEIGHT

                SwipeCandidate(
                    word = word,
                    score =
                        orderedCost * FALLBACK_ORDERED_WEIGHT +
                            candidateCoverage * FALLBACK_CANDIDATE_COVERAGE_WEIGHT +
                            traceCoverage * FALLBACK_TRACE_COVERAGE_WEIGHT +
                            directionMismatch * FALLBACK_DIRECTION_WEIGHT +
                            (startDistance + endDistance) * FALLBACK_ENDPOINT_WEIGHT +
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
        keyPath.forEach { label ->
            val normalized = label.lowercase()
            if (normalized.length != 1 || normalized[0] !in 'a'..'z') return@forEach
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
        val duplicateAware = mutableListOf<Point>()
        var previous: String? = null
        var hasDuplicate = false

        for (character in word.lowercase()) {
            if (character !in 'a'..'z') return emptyList()
            val label = character.toString()
            val center = centers[label] ?: return emptyList()
            basic += center

            if (previous == label) {
                hasDuplicate = true
                val radius = scale * DUPLICATE_LETTER_LOOP_RADIUS
                duplicateAware += Point(center.x + radius, center.y)
                duplicateAware += Point(center.x, center.y - radius)
                duplicateAware += Point(center.x - radius, center.y)
                duplicateAware += Point(center.x, center.y + radius)
            }
            duplicateAware += center
            previous = label
        }

        return buildList {
            if (basic.size >= 2) add(basic)
            if (hasDuplicate && duplicateAware.size >= 2) add(duplicateAware)
        }
    }

    private fun nearestKeyLabels(
        point: Point,
        centers: Map<String, Point>,
        count: Int,
    ): Set<String> =
        centers.entries
            .asSequence()
            .filter { it.key.length == 1 && it.key[0] in 'a'..'z' }
            .sortedBy { (_, center) -> distance(point, center) }
            .take(count.coerceAtLeast(1))
            .mapTo(linkedSetOf()) { it.key }

    private fun simplifyTrace(
        points: List<Point>,
        minimumSpacing: Double,
    ): List<Point> {
        if (points.size <= 2) return points
        val result = mutableListOf(points.first())

        points.drop(1).dropLast(1).forEach { point ->
            if (distance(result.last(), point) >= minimumSpacing) result += point
            if (result.size >= MAX_TRACE_POINTS - 1) return@forEach
        }

        if (result.last() != points.last()) result += points.last()
        return result
    }

    private fun extractCorners(points: List<Point>): List<Point> {
        if (points.size <= 2) return points

        val result = mutableListOf(points.first())
        for (index in 1 until points.lastIndex) {
            val previous = points[index - 1]
            val current = points[index]
            val next = points[index + 1]

            val ax = current.x - previous.x
            val ay = current.y - previous.y
            val bx = next.x - current.x
            val by = next.y - current.y
            val aLength = hypot(ax, ay)
            val bLength = hypot(bx, by)
            if (aLength <= EPSILON || bLength <= EPSILON) continue

            val cosine = ((ax * bx + ay * by) / (aLength * bLength)).coerceIn(-1.0, 1.0)
            val turnStrength = 1.0 - cosine
            if (turnStrength >= CORNER_TURN_STRENGTH) result += current
        }

        result += points.last()
        return result
    }

    private fun resamplePolyline(
        points: List<Point>,
        count: Int,
    ): List<Point> {
        if (points.isEmpty() || count <= 0) return emptyList()
        if (points.size == 1 || count == 1) return List(count) { points.first() }

        val cumulative = DoubleArray(points.size)
        for (index in 1 until points.size) {
            cumulative[index] =
                cumulative[index - 1] + distance(points[index - 1], points[index])
        }

        val totalLength = cumulative.last()
        if (totalLength <= EPSILON) return List(count) { points.first() }

        val result = ArrayList<Point>(count)
        var segment = 0
        repeat(count) { sampleIndex ->
            val target =
                totalLength * sampleIndex.toDouble() / (count - 1).coerceAtLeast(1).toDouble()

            while (
                segment < points.lastIndex - 1 &&
                cumulative[segment + 1] < target
            ) {
                segment += 1
            }

            val start = points[segment]
            val end = points[min(segment + 1, points.lastIndex)]
            val segmentStart = cumulative[segment]
            val segmentEnd = cumulative[min(segment + 1, cumulative.lastIndex)]
            val segmentLength = (segmentEnd - segmentStart).coerceAtLeast(EPSILON)
            val t = ((target - segmentStart) / segmentLength).coerceIn(0.0, 1.0)

            result += Point(
                x = start.x + (end.x - start.x) * t,
                y = start.y + (end.y - start.y) * t,
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
        val side = max(maxX - minX, maxY - minY).coerceAtLeast(EPSILON)
        val centerX = (minX + maxX) / 2.0
        val centerY = (minY + maxY) / 2.0

        return points.map { point ->
            Point(
                x = (point.x - centerX) / side,
                y = (point.y - centerY) / side,
            )
        }
    }

    private fun meanPointDistance(
        left: List<Point>,
        right: List<Point>,
    ): Double {
        if (left.isEmpty() || left.size != right.size) return Double.POSITIVE_INFINITY

        var total = 0.0
        for (index in left.indices) {
            total += distance(left[index], right[index])
        }
        return total / left.size
    }

    private fun directionMismatchCost(
        candidate: List<Point>,
        observed: List<Point>,
    ): Double {
        if (candidate.size < 2 || candidate.size != observed.size) {
            return Double.POSITIVE_INFINITY
        }

        var total = 0.0
        var compared = 0
        for (index in 0 until candidate.lastIndex) {
            val candidateDx = candidate[index + 1].x - candidate[index].x
            val candidateDy = candidate[index + 1].y - candidate[index].y
            val observedDx = observed[index + 1].x - observed[index].x
            val observedDy = observed[index + 1].y - observed[index].y

            val candidateLength = hypot(candidateDx, candidateDy)
            val observedLength = hypot(observedDx, observedDy)
            if (candidateLength <= EPSILON || observedLength <= EPSILON) continue

            val cosine = (
                (candidateDx * observedDx + candidateDy * observedDy) /
                    (candidateLength * observedLength)
                ).coerceIn(-1.0, 1.0)

            total += (1.0 - cosine) * 0.5
            compared += 1
        }

        return if (compared == 0) Double.POSITIVE_INFINITY else total / compared
    }

    private fun orderedCoverageCost(
        candidate: List<Point>,
        observed: List<Point>,
    ): Double {
        if (candidate.isEmpty() || observed.isEmpty()) return Double.POSITIVE_INFINITY

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

    private fun averageDistanceToPolyline(
        points: List<Point>,
        polyline: List<Point>,
    ): Double {
        if (points.isEmpty() || polyline.isEmpty()) return Double.POSITIVE_INFINITY
        if (polyline.size == 1) {
            return points.sumOf { distance(it, polyline.first()) } / points.size
        }

        var total = 0.0
        points.forEach { point ->
            var best = Double.POSITIVE_INFINITY
            for (index in 0 until polyline.lastIndex) {
                best = min(
                    best,
                    distanceToSegment(point, polyline[index], polyline[index + 1]),
                )
            }
            total += best
        }
        return total / points.size
    }

    private fun distanceToSegment(
        point: Point,
        start: Point,
        end: Point,
    ): Double {
        val dx = end.x - start.x
        val dy = end.y - start.y
        if (abs(dx) <= EPSILON && abs(dy) <= EPSILON) return distance(point, start)

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

    private fun sequenceDistance(
        observed: List<String>,
        candidate: List<String>,
    ): Double {
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
            max(observed.size, candidate.size).toDouble()
    }

    private fun polylineLength(points: List<Point>): Double {
        if (points.size < 2) return 0.0
        var total = 0.0
        for (index in 0 until points.lastIndex) {
            total += distance(points[index], points[index + 1])
        }
        return total
    }

    private fun keyboardScale(centers: Map<String, Point>): Double {
        val pairs = listOf(
            "q" to "w",
            "w" to "e",
            "a" to "s",
            "s" to "d",
            "z" to "x",
            "x" to "c",
        )
        val distances = pairs.mapNotNull { (left, right) ->
            val a = centers[left] ?: return@mapNotNull null
            val b = centers[right] ?: return@mapNotNull null
            distance(a, b)
        }
        return distances.takeIf { it.isNotEmpty() }?.average() ?: Double.NaN
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
        const val EPSILON = 0.000001
        const val MIN_TRACE_KEYS = 2
        const val MIN_TRACE_POINTS = 3
        const val MIN_LAYOUT_KEYS = 20
        const val MAX_TRACE_POINTS = 96
        const val MIN_TRACE_SAMPLE_SPACING = 0.10

        const val STATISTICAL_SAMPLE_POINTS = 72
        const val START_KEY_CANDIDATES = 3
        const val END_KEY_CANDIDATES = 3
        const val DUPLICATE_LETTER_LOOP_RADIUS = 0.18

        const val MAX_LOG_LENGTH_RATIO = 0.82
        const val MAX_PHYSICAL_START_DISTANCE = 1.55
        const val MAX_PHYSICAL_END_DISTANCE = 2.15
        const val MAX_NORMALIZED_SHAPE_DISTANCE = 0.52
        const val MAX_DIRECTION_MISMATCH = 0.72
        const val MAX_CORNER_COVERAGE = 1.45
        const val MAX_TOTAL_SCORE = 5.80

        const val SHAPE_WEIGHT = 3.20
        const val LOCATION_WEIGHT = 1.05
        const val DIRECTION_WEIGHT = 1.55
        const val CORNER_WEIGHT = 0.80
        const val LENGTH_WEIGHT = 0.60
        const val START_ENDPOINT_WEIGHT = 1.15
        const val END_ENDPOINT_WEIGHT = 0.72
        const val SEQUENCE_DISTANCE_WEIGHT = 0.28
        const val FREQUENCY_LOG_WEIGHT = 0.008
        const val CORNER_TURN_STRENGTH = 0.035

        const val FALLBACK_SAMPLE_POINTS = 24
        const val FALLBACK_MAX_ORDERED_COST = 1.55
        const val FALLBACK_MAX_CANDIDATE_COVERAGE = 1.20
        const val FALLBACK_ORDERED_WEIGHT = 2.0
        const val FALLBACK_CANDIDATE_COVERAGE_WEIGHT = 1.45
        const val FALLBACK_TRACE_COVERAGE_WEIGHT = 0.30
        const val FALLBACK_DIRECTION_WEIGHT = 0.75
        const val FALLBACK_ENDPOINT_WEIGHT = 1.05
        const val FALLBACK_SEQUENCE_WEIGHT = 0.40
        const val FALLBACK_LENGTH_WEIGHT = 0.06
        const val FALLBACK_FREQUENCY_WEIGHT = 0.018

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
