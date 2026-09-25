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

        val corrections = if (codePointCount(normalized) >= MIN_CORRECTION_LENGTH) {
            correctionCandidates(normalized, candidates).map { it.word }
        } else {
            emptyList()
        }

        val result = mutableListOf<String>()
        when {
            exact != null -> {
                result += exact.word
                result += completions
            }

            completions.isNotEmpty() -> {
                // Prefer real dictionary completions over echoing an incomplete token. Keep the
                // literal token available as a final fallback so the user can always preserve it.
                result += completions.take((limit - 1).coerceAtLeast(1))
                result += prefix
            }

            corrections.isNotEmpty() -> {
                // Put likely spelling corrections in front so a misspelling is visible rather than
                // presenting the misspelled token as though it were the best candidate.
                result += corrections.take((limit - 1).coerceAtLeast(1))
                result += prefix
            }

            else -> result += prefix
        }

        if (result.size < limit) {
            result += corrections.filterNot { candidate ->
                result.any { it.equals(candidate, ignoreCase = true) }
            }
        }

        return result
            .distinctBy { it.lowercase() }
            .take(limit.coerceAtMost(MAX_VISIBLE_SUGGESTIONS))
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
        const val MAX_VISIBLE_SUGGESTIONS = 3
    }
}
