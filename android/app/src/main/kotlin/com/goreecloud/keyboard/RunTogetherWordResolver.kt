package com.goreecloud.keyboard

import kotlin.math.ln

/**
 * Finds a conservative local word-boundary repair for a token whose spaces were missed while
 * typing. Only exact dictionary words participate; the resolver never invents a word and never
 * runs for a token that is already known as one word.
 *
 * The search is deliberately bounded to four segments and 32 ASCII letters. Short fragments must
 * be high-frequency dictionary entries, while longer exact words (including curated brand names)
 * may participate even when their packaged rank is lower. Ambiguous near-ties are rejected.
 */
internal class RunTogetherWordResolver {
    private data class Entry(
        val word: String,
        val normalized: String,
        val rank: Int,
    )

    private data class Path(
        val parts: List<Entry>,
        val score: Double,
    ) {
        val phrase: String get() = parts.joinToString(" ") { it.word }
    }

    private var cachedDictionary: Collection<String>? = null
    private var cachedIndex: Map<String, Entry>? = null

    fun resolve(
        token: String,
        dictionary: Collection<String>,
    ): String? {
        val normalized = token.lowercase()
        if (normalized.length !in MIN_TOKEN_LENGTH..MAX_TOKEN_LENGTH) return null
        if (normalized.any { it !in 'a'..'z' }) return null

        val index = indexFor(dictionary)
        if (index.containsKey(normalized)) return null

        val completed = mutableListOf<Path>()

        fun search(offset: Int, parts: List<Entry>, score: Double) {
            if (offset == normalized.length) {
                if (parts.size >= 2) completed += Path(parts, score)
                return
            }
            if (parts.size >= MAX_SEGMENTS) return

            for (end in offset + 1..normalized.length) {
                val piece = normalized.substring(offset, end)
                val entry = index[piece] ?: continue
                if (!eligibleSegment(entry)) continue

                val remaining = normalized.length - end
                if (remaining > 0 && parts.size + 1 >= MAX_SEGMENTS) continue

                val boundaryPenalty = if (parts.isEmpty()) 0.0 else SPLIT_PENALTY
                search(
                    offset = end,
                    parts = parts + entry,
                    score = score + segmentScore(entry) + boundaryPenalty,
                )
            }
        }

        search(offset = 0, parts = emptyList(), score = 0.0)

        val ranked = completed
            .asSequence()
            .filter(::confidentPath)
            .sortedWith(
                compareBy<Path> { it.score }
                    .thenBy { it.parts.size }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.phrase },
            )
            .toList()

        val best = ranked.firstOrNull() ?: return null
        val runnerUp = ranked.getOrNull(1)
        if (
            runnerUp != null &&
            !runnerUp.phrase.equals(best.phrase, ignoreCase = true) &&
            runnerUp.score - best.score < MIN_WIN_MARGIN
        ) {
            return null
        }
        return best.phrase
    }

    private fun indexFor(dictionary: Collection<String>): Map<String, Entry> {
        if (cachedDictionary === dictionary) {
            cachedIndex?.let { return it }
        }

        val index = LinkedHashMap<String, Entry>()
        dictionary.forEachIndexed { rank, word ->
            val normalized = word.lowercase()
            if (normalized.isNotBlank()) {
                index.putIfAbsent(
                    normalized,
                    Entry(word = word, normalized = normalized, rank = rank),
                )
            }
        }
        cachedDictionary = dictionary
        cachedIndex = index
        return index
    }

    private fun eligibleSegment(entry: Entry): Boolean {
        val length = entry.normalized.length
        return when (length) {
            1 -> entry.normalized in SINGLE_LETTER_WORDS && entry.rank <= SINGLE_LETTER_MAX_RANK
            2 -> entry.rank <= TWO_LETTER_MAX_RANK
            3 -> entry.rank <= THREE_LETTER_MAX_RANK
            else -> entry.rank <= COMMON_SEGMENT_MAX_RANK || length >= LONG_EXACT_WORD_LENGTH
        }
    }

    private fun confidentPath(path: Path): Boolean {
        if (path.parts.size !in 2..MAX_SEGMENTS) return false
        val hasCommonAnchor = path.parts.any { it.rank <= COMMON_ANCHOR_MAX_RANK }
        val allSubstantial = path.parts.all { it.normalized.length >= 4 }
        return hasCommonAnchor || allSubstantial
    }

    private fun segmentScore(entry: Entry): Double {
        val shortPenalty = when (entry.normalized.length) {
            1 -> 1.25
            2 -> 0.70
            3 -> 0.30
            else -> 0.0
        }
        return ln(entry.rank + 2.0) + shortPenalty
    }

    private companion object {
        const val MIN_TOKEN_LENGTH = 5
        const val MAX_TOKEN_LENGTH = 32
        const val MAX_SEGMENTS = 4

        const val SINGLE_LETTER_MAX_RANK = 300
        const val TWO_LETTER_MAX_RANK = 1_200
        const val THREE_LETTER_MAX_RANK = 3_000
        const val COMMON_SEGMENT_MAX_RANK = 6_000
        const val COMMON_ANCHOR_MAX_RANK = 1_500
        const val LONG_EXACT_WORD_LENGTH = 5

        const val SPLIT_PENALTY = 0.85
        const val MIN_WIN_MARGIN = 0.60

        val SINGLE_LETTER_WORDS = setOf("a", "i")
    }
}
