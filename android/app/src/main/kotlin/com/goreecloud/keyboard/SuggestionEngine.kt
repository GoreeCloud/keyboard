package com.goreecloud.keyboard

/**
 * Local-only suggestion boundary for GoreeCloud Quill.
 *
 * The engine performs deterministic prefix matching and a deliberately bounded
 * one-edit correction pass. It exposes no network, persistence, telemetry, or
 * unrestricted language-model transport.
 *
 * Correction distance is measured in Unicode code points rather than UTF-16 code units so a
 * supplementary character cannot be split into two artificial edits.
 */
class SuggestionEngine {
    fun suggest(prefix: String, dictionary: Collection<String>, limit: Int = 3): List<String> {
        if (prefix.isBlank() || limit <= 0) return emptyList()

        val boundedLimit = limit.coerceAtMost(MAX_VISIBLE_SUGGESTIONS)
        val normalized = prefix.lowercase()
        val normalizedCodePointCount = codePointCount(normalized)
        val candidates = normalizedCandidates(dictionary)

        val prefixMatches = candidates
            .asSequence()
            .filter { it.startsWith(normalized, ignoreCase = true) }
            .sortedWith(
                compareBy<String> { codePointCount(it) }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it },
            )
            .toList()

        val corrections = if (
            prefixMatches.size < boundedLimit &&
            normalizedCodePointCount >= MIN_CORRECTION_LENGTH
        ) {
            correctionCandidates(normalized, candidates)
        } else {
            emptyList()
        }

        val suggestions = (prefixMatches + corrections)
            .distinctBy { it.lowercase() }
            .take(boundedLimit)

        // While the user is actively typing an ordinary word, keep one actionable item visible
        // even when the packaged dictionary has no useful match. The local prefix itself is a
        // no-network, no-learning "keep what I typed" candidate and preserves the 1..3 strip rule.
        return if (suggestions.isNotEmpty()) suggestions else listOf(prefix).take(boundedLimit)
    }

    /**
     * Returns a conservative automatic correction only when exactly one packaged dictionary word
     * is one Unicode edit away. Prefix completions are deliberately excluded so Space never turns
     * an incomplete word into an unsolicited completion.
     */
    fun autocorrection(prefix: String, dictionary: Collection<String>): String? {
        if (prefix.isBlank() || codePointCount(prefix) < MIN_CORRECTION_LENGTH) return null

        val normalized = prefix.lowercase()
        val candidates = normalizedCandidates(dictionary)
        if (candidates.any { it.equals(normalized, ignoreCase = true) }) return null

        return correctionCandidates(normalized, candidates).singleOrNull()
    }

    private fun normalizedCandidates(dictionary: Collection<String>): List<String> =
        dictionary
            .asSequence()
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .toList()

    private fun correctionCandidates(normalized: String, candidates: Collection<String>): List<String> {
        val normalizedCodePointCount = codePointCount(normalized)
        return candidates
            .asSequence()
            .filterNot { it.startsWith(normalized, ignoreCase = true) }
            .filter { isSingleEditAway(normalized, it.lowercase()) }
            .sortedWith(
                compareBy<String> {
                    kotlin.math.abs(codePointCount(it) - normalizedCodePointCount)
                }
                    .thenBy { codePointCount(it) }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it },
            )
            .toList()
    }

    private fun isSingleEditAway(left: String, right: String): Boolean {
        val leftCodePoints = left.codePoints().toArray()
        val rightCodePoints = right.codePoints().toArray()
        val lengthDifference = kotlin.math.abs(leftCodePoints.size - rightCodePoints.size)
        if (lengthDifference > 1) return false

        if (leftCodePoints.size == rightCodePoints.size) {
            val mismatches = leftCodePoints.indices.filter {
                leftCodePoints[it] != rightCodePoints[it]
            }
            return when (mismatches.size) {
                1 -> true
                2 -> {
                    val first = mismatches[0]
                    val second = mismatches[1]
                    second == first + 1 &&
                        leftCodePoints[first] == rightCodePoints[second] &&
                        leftCodePoints[second] == rightCodePoints[first]
                }
                else -> false
            }
        }

        val shorter = if (leftCodePoints.size < rightCodePoints.size) leftCodePoints else rightCodePoints
        val longer = if (leftCodePoints.size < rightCodePoints.size) rightCodePoints else leftCodePoints
        var shortIndex = 0
        var longIndex = 0
        var skipped = false

        while (shortIndex < shorter.size && longIndex < longer.size) {
            if (shorter[shortIndex] == longer[longIndex]) {
                shortIndex++
                longIndex++
            } else {
                if (skipped) return false
                skipped = true
                longIndex++
            }
        }
        return true
    }

    private fun codePointCount(value: String): Int = value.codePointCount(0, value.length)

    private companion object {
        const val MIN_CORRECTION_LENGTH = 3
        const val MAX_VISIBLE_SUGGESTIONS = 3
    }
}
