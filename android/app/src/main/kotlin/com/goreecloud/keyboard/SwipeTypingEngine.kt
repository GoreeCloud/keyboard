package com.goreecloud.keyboard

import kotlin.math.abs

/**
 * Bounded local gesture decoder for Development swipe typing.
 *
 * It receives only the sequence of letter keys traversed by the current gesture and compares that
 * transient sequence against the packaged Quill lexicon. It performs no editor reads, learning,
 * persistence, telemetry, account access, or network access.
 */
internal class SwipeTypingEngine {
    fun decode(
        keyPath: List<String>,
        dictionary: Collection<String>,
        limit: Int = 3,
    ): List<String> {
        if (limit <= 0) return emptyList()

        val trace = normalizeTrace(keyPath)
        if (trace.size < MIN_TRACE_KEYS) return emptyList()

        val scored = dictionary.asSequence()
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .mapNotNull { word ->
                val normalizedWord = word.lowercase()
                val wordTrace = collapseConsecutive(
                    normalizedWord.codePoints()
                        .filter { Character.isLetter(it) }
                        .toArray()
                        .toList(),
                )
                if (wordTrace.size < MIN_TRACE_KEYS) return@mapNotNull null
                if (wordTrace.first() != trace.first() || wordTrace.last() != trace.last()) {
                    return@mapNotNull null
                }

                val distance = editDistance(trace, wordTrace)
                val allowance = maxOf(1, wordTrace.size / 3)
                if (distance > allowance) return@mapNotNull null

                Candidate(
                    word = word,
                    distance = distance,
                    traceLengthDelta = abs(trace.size - wordTrace.size),
                    wordLength = normalizedWord.codePointCount(0, normalizedWord.length),
                )
            }
            .sortedWith(
                compareBy<Candidate> { it.distance }
                    .thenBy { it.traceLengthDelta }
                    .thenBy { it.wordLength }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.word },
            )
            .take(limit)
            .map { it.word }
            .toList()

        return scored
    }

    private fun normalizeTrace(keyPath: List<String>): List<Int> =
        collapseConsecutive(
            keyPath.asSequence()
                .flatMap { label -> label.lowercase().codePoints().toArray().asSequence() }
                .filter { Character.isLetter(it) }
                .toList(),
        )

    private fun collapseConsecutive(values: List<Int>): List<Int> {
        if (values.isEmpty()) return emptyList()
        val result = ArrayList<Int>(values.size)
        for (value in values) {
            if (result.lastOrNull() != value) result += value
        }
        return result
    }

    private fun editDistance(left: List<Int>, right: List<Int>): Int {
        if (left.isEmpty()) return right.size
        if (right.isEmpty()) return left.size

        var previous = IntArray(right.size + 1) { it }
        for (leftIndex in left.indices) {
            val current = IntArray(right.size + 1)
            current[0] = leftIndex + 1
            for (rightIndex in right.indices) {
                val substitution = previous[rightIndex] +
                    if (left[leftIndex] == right[rightIndex]) 0 else 1
                current[rightIndex + 1] = minOf(
                    current[rightIndex] + 1,
                    previous[rightIndex + 1] + 1,
                    substitution,
                )
            }
            previous = current
        }
        return previous[right.size]
    }

    private data class Candidate(
        val word: String,
        val distance: Int,
        val traceLengthDelta: Int,
        val wordLength: Int,
    )

    private companion object {
        const val MIN_TRACE_KEYS = 2
    }
}
