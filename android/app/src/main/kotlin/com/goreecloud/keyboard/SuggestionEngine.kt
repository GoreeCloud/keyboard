package com.goreecloud.keyboard

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.ln

/**
 * Local-only spelling, completion, and correction ranking for GoreeCloud Keyboard.
 *
 * The engine combines deterministic dictionary frequency, prefix quality, bounded Unicode-aware
 * edit distance, QWERTY proximity for likely tap slips, and optional transient next-word context.
 * It performs no network access and does not inspect or persist editor text itself.
 */
class SuggestionEngine {
    fun suggest(
        prefix: String,
        dictionary: Collection<String>,
        contextualPredictions: Collection<String> = emptyList(),
        limit: Int = 3,
    ): List<String> {
        if (prefix.isBlank() || limit <= 0) return emptyList()

        val effectiveLimit = limit.coerceAtMost(MAX_VISIBLE_SUGGESTIONS)
        val normalized = prefix.lowercase()
        val candidates = indexedCandidates(dictionary)
        val exact = candidates.firstOrNull { it.normalized == normalized }
        val contextRank = contextualPredictions
            .distinctBy { it.lowercase() }
            .mapIndexed { index, value -> value.lowercase() to index }
            .toMap()

        val scored = candidates.asSequence()
            .mapNotNull { candidate ->
                scoreCandidate(
                    typed = normalized,
                    candidate = candidate,
                    contextRank = contextRank,
                )
            }
            .sortedWith(
                compareBy<ScoredCandidate> { it.score }
                    .thenBy { it.candidate.rank }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.candidate.word },
            )
            .toList()

        val result = mutableListOf<String>()
        if (exact != null) result += exact.word

        scored.forEach { scoredCandidate ->
            if (result.size >= effectiveLimit) return@forEach
            val word = scoredCandidate.candidate.word
            if (result.none { it.equals(word, ignoreCase = true) }) result += word
        }

        if (
            exact == null &&
            result.size < effectiveLimit &&
            result.none { it.equals(prefix, ignoreCase = true) }
        ) {
            result += prefix
        }

        if (result.isEmpty()) result += prefix

        return result
            .distinctBy { it.lowercase() }
            .take(effectiveLimit)
    }

    /**
     * Returns a conservative local automatic correction for a completed word.
     *
     * The correction must be unknown, alphabetic, at least three code points, within a short
     * Damerau-Levenshtein radius, and clearly better than the runner-up. Neighbor-key substitutions
     * are treated as more plausible than distant substitutions, while ambiguous candidates remain
     * suggestions instead of being committed automatically.
     */
    fun bestAutocorrection(
        word: String,
        dictionary: Collection<String>,
        contextualPredictions: Collection<String> = emptyList(),
    ): String? {
        if (word.isBlank()) return null
        val normalized = word.lowercase()
        if (codePointCount(normalized) < MIN_CORRECTION_LENGTH) return null
        if (!normalized.codePoints().allMatch { Character.isLetter(it) }) return null

        val candidates = indexedCandidates(dictionary)
        if (candidates.any { it.normalized == normalized }) return null

        val contextRank = contextualPredictions
            .distinctBy { it.lowercase() }
            .mapIndexed { index, value -> value.lowercase() to index }
            .toMap()

        val corrections = candidates.asSequence()
            .mapNotNull { candidate ->
                scoreCorrectionCandidate(
                    typed = normalized,
                    candidate = candidate,
                    contextRank = contextRank,
                )
            }
            .sortedWith(
                compareBy<ScoredCandidate> { it.score }
                    .thenBy { it.candidate.rank }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.candidate.word },
            )
            .toList()

        val best = corrections.firstOrNull() ?: return null
        if (best.score > AUTOCORRECT_MAX_SCORE) return null

        val runnerUp = corrections.getOrNull(1)
        if (runnerUp != null && runnerUp.score - best.score < AUTOCORRECT_SCORE_MARGIN) {
            return null
        }
        return best.candidate.word
    }

    private fun scoreCandidate(
        typed: String,
        candidate: Candidate,
        contextRank: Map<String, Int>,
    ): ScoredCandidate? {
        if (candidate.normalized == typed) return null

        val completion = candidate.normalized.startsWith(typed)
        val maximumDistance = maximumCorrectionDistance(typed)
        val distance = if (completion) {
            0
        } else {
            damerauLevenshteinDistance(typed, candidate.normalized, maximumDistance)
        }

        if (!completion && distance !in 1..maximumDistance) return null

        val rankPenalty = frequencyPenalty(candidate.rank)
        val contextBonus = contextRank[candidate.normalized]
            ?.let { -CONTEXT_BONUS + it * CONTEXT_RANK_STEP }
            ?: 0.0

        val score = if (completion) {
            val extra = (codePointCount(candidate.normalized) - codePointCount(typed)).coerceAtLeast(0)
            COMPLETION_BASE_SCORE +
                extra * COMPLETION_LENGTH_WEIGHT +
                rankPenalty +
                contextBonus
        } else {
            correctionScore(
                typed = typed,
                candidate = candidate,
                distance = distance,
                rankPenalty = rankPenalty,
                contextBonus = contextBonus,
            )
        }

        return ScoredCandidate(candidate, score, distance)
    }

    private fun scoreCorrectionCandidate(
        typed: String,
        candidate: Candidate,
        contextRank: Map<String, Int>,
    ): ScoredCandidate? {
        val maximumDistance = maximumCorrectionDistance(typed)
        if (abs(codePointCount(candidate.normalized) - codePointCount(typed)) > maximumDistance) {
            return null
        }

        val distance = damerauLevenshteinDistance(
            typed,
            candidate.normalized,
            maximumDistance,
        )
        if (distance !in 1..maximumDistance) return null

        val firstTyped = typed.firstCodePointOrNull()
        val firstCandidate = candidate.normalized.firstCodePointOrNull()
        if (
            firstTyped != null &&
            firstCandidate != null &&
            firstTyped != firstCandidate &&
            !areNeighborKeys(firstTyped, firstCandidate)
        ) {
            return null
        }

        val contextBonus = contextRank[candidate.normalized]
            ?.let { -CONTEXT_BONUS + it * CONTEXT_RANK_STEP }
            ?: 0.0

        return ScoredCandidate(
            candidate = candidate,
            score = correctionScore(
                typed = typed,
                candidate = candidate,
                distance = distance,
                rankPenalty = frequencyPenalty(candidate.rank),
                contextBonus = contextBonus,
            ),
            distance = distance,
        )
    }

    private fun correctionScore(
        typed: String,
        candidate: Candidate,
        distance: Int,
        rankPenalty: Double,
        contextBonus: Double,
    ): Double {
        val commonPrefix = commonPrefixLength(typed, candidate.normalized)
        val lengthDelta = abs(codePointCount(candidate.normalized) - codePointCount(typed))
        val keyboardBonus = qwertyProximityBonus(typed, candidate.normalized)

        return CORRECTION_DISTANCE_WEIGHT * distance +
            lengthDelta * CORRECTION_LENGTH_WEIGHT -
            commonPrefix * COMMON_PREFIX_BONUS -
            keyboardBonus +
            rankPenalty +
            contextBonus
    }

    private fun indexedCandidates(dictionary: Collection<String>): List<Candidate> =
        dictionary.asSequence()
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .mapIndexed { rank, word ->
                Candidate(
                    word = word,
                    normalized = word.lowercase(),
                    rank = rank,
                )
            }
            .toList()

    private fun maximumCorrectionDistance(value: String): Int {
        val length = codePointCount(value)
        return when {
            length >= VERY_LONG_WORD_LENGTH -> 3
            length >= LONG_WORD_LENGTH -> 2
            else -> 1
        }
    }

    private fun qwertyProximityBonus(left: String, right: String): Double {
        if (left.length != right.length) return 0.0
        var bonus = 0.0
        left.zip(right).forEach { (a, b) ->
            if (a == b) return@forEach
            val p1 = KEY_POINTS[a] ?: return@forEach
            val p2 = KEY_POINTS[b] ?: return@forEach
            val distance = hypot(p1.first - p2.first, p1.second - p2.second)
            bonus += when {
                distance <= 1.05 -> NEIGHBOR_KEY_BONUS
                distance <= 1.6 -> NEAR_KEY_BONUS
                else -> 0.0
            }
        }
        return bonus
    }

    private fun areNeighborKeys(left: Int, right: Int): Boolean {
        if (left !in 'a'.code..'z'.code || right !in 'a'.code..'z'.code) return false
        val p1 = KEY_POINTS[left.toChar()] ?: return false
        val p2 = KEY_POINTS[right.toChar()] ?: return false
        return hypot(p1.first - p2.first, p1.second - p2.second) <= 1.25
    }

    private fun commonPrefixLength(left: String, right: String): Int {
        val limit = minOf(left.length, right.length)
        var index = 0
        while (index < limit && left[index] == right[index]) index += 1
        return index
    }

    private fun frequencyPenalty(rank: Int): Double =
        ln(rank.toDouble() + 2.0) * FREQUENCY_LOG_WEIGHT

    private fun damerauLevenshteinDistance(left: String, right: String, maxDistance: Int): Int {
        val a = left.codePoints().toArray()
        val b = right.codePoints().toArray()
        if (abs(a.size - b.size) > maxDistance) return maxDistance + 1

        val matrix = Array(a.size + 1) { IntArray(b.size + 1) }
        for (i in 0..a.size) matrix[i][0] = i
        for (j in 0..b.size) matrix[0][j] = j

        for (i in 1..a.size) {
            var rowMinimum = Int.MAX_VALUE
            for (j in 1..b.size) {
                val substitutionCost = if (a[i - 1] == b[j - 1]) 0 else 1
                var value = minOf(
                    matrix[i - 1][j] + 1,
                    matrix[i][j - 1] + 1,
                    matrix[i - 1][j - 1] + substitutionCost,
                )

                if (
                    i > 1 &&
                    j > 1 &&
                    a[i - 1] == b[j - 2] &&
                    a[i - 2] == b[j - 1]
                ) {
                    value = minOf(value, matrix[i - 2][j - 2] + 1)
                }

                matrix[i][j] = value
                rowMinimum = minOf(rowMinimum, value)
            }
            if (rowMinimum > maxDistance) return maxDistance + 1
        }
        return matrix[a.size][b.size]
    }

    private fun IntArray.firstCodePointOrNull(): Int? = firstOrNull()

    private fun String.firstCodePointOrNull(): Int? {
        if (isEmpty()) return null
        return codePointAt(0)
    }

    private fun codePointCount(value: String): Int = value.codePointCount(0, value.length)

    private data class Candidate(
        val word: String,
        val normalized: String,
        val rank: Int,
    )

    private data class ScoredCandidate(
        val candidate: Candidate,
        val score: Double,
        val distance: Int,
    )

    private companion object {
        const val MIN_CORRECTION_LENGTH = 3
        const val LONG_WORD_LENGTH = 6
        const val VERY_LONG_WORD_LENGTH = 10
        const val MAX_VISIBLE_SUGGESTIONS = 3

        const val COMPLETION_BASE_SCORE = 0.55
        const val COMPLETION_LENGTH_WEIGHT = 0.08
        const val CORRECTION_DISTANCE_WEIGHT = 1.55
        const val CORRECTION_LENGTH_WEIGHT = 0.18
        const val COMMON_PREFIX_BONUS = 0.07
        const val NEIGHBOR_KEY_BONUS = 0.42
        const val NEAR_KEY_BONUS = 0.18
        const val CONTEXT_BONUS = 1.15
        const val CONTEXT_RANK_STEP = 0.16
        const val FREQUENCY_LOG_WEIGHT = 0.055

        const val AUTOCORRECT_MAX_SCORE = 3.2
        const val AUTOCORRECT_SCORE_MARGIN = 0.28

        val KEY_POINTS = buildMap {
            "qwertyuiop".forEachIndexed { index, character ->
                put(character, index.toDouble() to 0.0)
            }
            "asdfghjkl".forEachIndexed { index, character ->
                put(character, index + 0.45 to 1.0)
            }
            "zxcvbnm".forEachIndexed { index, character ->
                put(character, index + 0.95 to 2.0)
            }
        }
    }
}
