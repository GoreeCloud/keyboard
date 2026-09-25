package com.goreecloud.keyboard

import kotlin.math.abs

/**
 * Local-only suggestion and correction boundary for GoreeCloud Quill.
 *
 * Candidate order is treated as a lightweight frequency signal: earlier dictionary entries are
 * preferred over later entries. Suggestions remain deterministic, in-process, non-learning, and
 * independent of network, telemetry, contacts, clipboard data, or surrounding editor text.
 *
 * Edit distance is measured in Unicode code points and supports adjacent transposition so common
 * typing errors such as "teh" can resolve to "the" without splitting supplementary characters.
 */
class SuggestionEngine {
    fun suggest(prefix: String, dictionary: Collection<String>, limit: Int = 3): List<String> {
        if (prefix.isBlank() || limit <= 0) return emptyList()

        val normalized = prefix.lowercase()
        val candidates = indexedCandidates(dictionary)
        val exact = candidates.firstOrNull { it.normalized == normalized }

        val completions = candidates
            .asSequence()
            .filter { it.normalized != normalized && it.normalized.startsWith(normalized) }
            .sortedWith(
                compareBy<Candidate> { it.rank }
                    .thenBy { codePointCount(it.word) - codePointCount(normalized) }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.word },
            )
            .map { it.word }
            .toList()

        val result = mutableListOf<String>()
        // Keep the actively typed token visible even when it is not in the packaged dictionary.
        // This gives ordinary typing a stable primary candidate while still allowing Quill
        // completions/corrections to occupy the remaining slots.
        result += exact?.word ?: prefix
        result += completions
            .asSequence()
            .filterNot { it.equals(result.first(), ignoreCase = true) }
            .take((limit - result.size).coerceAtLeast(0))
        if (result.size >= limit) return result.take(limit)

        if (codePointCount(normalized) < MIN_CORRECTION_LENGTH) {
            return result.take(limit)
        }

        val corrections = correctionCandidates(normalized, candidates)
            .asSequence()
            .filterNot { candidate -> result.any { it.equals(candidate.word, ignoreCase = true) } }
            .map { it.word }
            .take(limit - result.size)
            .toList()

        result += corrections
        return result.take(limit)
    }

    /**
     * Returns a conservative automatic correction for a completed typed word.
     *
     * Automatic replacement is intentionally stricter than the suggestion strip: the typed token
     * must not already be known, must contain letters only, must be at least three code points, and
     * the replacement must be exactly one bounded Damerau-Levenshtein edit away while retaining the
     * first letter. Ambiguous one-edit candidates are resolved by dictionary rank, but a close
     * runner-up suppresses automatic replacement rather than guessing.
     */
    fun bestAutocorrection(word: String, dictionary: Collection<String>): String? {
        if (word.isBlank()) return null
        val normalized = word.lowercase()
        if (codePointCount(normalized) < MIN_CORRECTION_LENGTH) return null
        if (!normalized.codePoints().allMatch { Character.isLetter(it) }) return null

        val candidates = indexedCandidates(dictionary)
        if (candidates.any { it.normalized == normalized }) return null

        val corrections = correctionCandidates(normalized, candidates)
            .filter { it.distance == 1 }
            .filter { it.normalized.firstCodePointOrNull() == normalized.firstCodePointOrNull() }

        val best = corrections.firstOrNull() ?: return null
        val runnerUp = corrections.getOrNull(1)
        if (runnerUp != null && runnerUp.rank - best.rank < AUTOCORRECT_RANK_GAP) {
            return null
        }
        return best.word
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
                    distance = 0,
                )
            }
            .toList()

    private fun correctionCandidates(
        normalized: String,
        candidates: List<Candidate>,
    ): List<Candidate> {
        val normalizedLength = codePointCount(normalized)
        val maximumDistance = if (normalizedLength >= LONG_WORD_LENGTH) 2 else 1

        return candidates.asSequence()
            .filterNot { it.normalized.startsWith(normalized) }
            .filter {
                abs(codePointCount(it.normalized) - normalizedLength) <= maximumDistance
            }
            .mapNotNull { candidate ->
                val distance = damerauLevenshteinDistance(
                    normalized,
                    candidate.normalized,
                    maximumDistance,
                )
                if (distance in 1..maximumDistance) candidate.copy(distance = distance) else null
            }
            .sortedWith(
                compareBy<Candidate> { it.distance }
                    .thenBy { it.rank }
                    .thenBy { abs(codePointCount(it.word) - normalizedLength) }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.word },
            )
            .toList()
    }

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
        val distance: Int,
    )

    private companion object {
        const val MIN_CORRECTION_LENGTH = 3
        const val LONG_WORD_LENGTH = 6
        const val AUTOCORRECT_RANK_GAP = 8
    }
}
