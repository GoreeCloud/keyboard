package com.goreecloud.keyboard

/**
 * First-party local English contraction canonicalization.
 *
 * Apostrophes are punctuation inside a word, not evidence that the token should lose lexical
 * priority. The model compares the typed token against the letters-only form of packaged
 * contractions and accepts only an exact letters match or a unique one-edit repair.
 */
internal object ContractionModel {
    private data class Candidate(
        val value: String,
        val letters: String,
    )

    private val candidates = GoreeCloudDictionary.commonContractions
        .distinctBy { it.lowercase() }
        .map { Candidate(it, lettersOnly(it)) }

    fun correction(token: String): String? {
        val normalized = normalize(token)
        if (normalized.length < 2) return null
        val letters = lettersOnly(normalized)
        if (letters.length < 2) return null

        val ranked = candidates
            .map { it to damerauLevenshtein(letters, it.letters, maxDistance = 1) }
            .filter { (_, distance) -> distance in 0..1 }
            .sortedWith(
                compareBy<Pair<Candidate, Int>> { it.second }
                    .thenByDescending { it.first.letters.length }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.first.value },
            )

        val best = ranked.firstOrNull() ?: return null
        val runnerUp = ranked.getOrNull(1)
        if (runnerUp != null && runnerUp.second == best.second) return null
        if (best.second == 1 && letters.length < 4) return null
        return best.first.value
    }

    fun suggestion(prefix: String): String? {
        val normalized = normalize(prefix)
        if (normalized.length < 2) return null
        val letters = lettersOnly(normalized)
        if (letters.length < 2) return null

        val exactCompletion = candidates.firstOrNull { candidate ->
            candidate.letters.startsWith(letters) && candidate.letters != letters
        }
        correction(prefix)?.let { return it }
        return exactCompletion?.value
    }

    private fun normalize(value: String): String =
        value.trim().lowercase().replace('’', '\'')

    private fun lettersOnly(value: String): String =
        value.filter { it in 'a'..'z' }

    private fun damerauLevenshtein(left: String, right: String, maxDistance: Int): Int {
        if (kotlin.math.abs(left.length - right.length) > maxDistance) return maxDistance + 1
        val previousPrevious = IntArray(right.length + 1)
        var previous = IntArray(right.length + 1) { it }
        var current = IntArray(right.length + 1)

        left.indices.forEach { i ->
            current[0] = i + 1
            var rowMin = current[0]
            right.indices.forEach { j ->
                val substitution = previous[j] + if (left[i] == right[j]) 0 else 1
                val insertion = current[j] + 1
                val deletion = previous[j + 1] + 1
                var value = minOf(substitution, insertion, deletion)
                if (
                    i > 0 && j > 0 &&
                    left[i] == right[j - 1] &&
                    left[i - 1] == right[j]
                ) {
                    value = minOf(value, previousPrevious[j - 1] + 1)
                }
                current[j + 1] = value
                rowMin = minOf(rowMin, value)
            }
            if (rowMin > maxDistance) return maxDistance + 1
            previousPrevious.indices.forEach { previousPrevious[it] = previous[it] }
            val swap = previous
            previous = current
            current = swap
        }
        return previous[right.length]
    }
}
