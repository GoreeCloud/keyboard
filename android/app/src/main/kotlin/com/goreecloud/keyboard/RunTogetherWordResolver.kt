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

    /**
     * Recovers a missed-space token that also contains one ordinary typing error.
     *
     * The exact resolver remains authoritative. This fallback explores only one deletion,
     * adjacent transposition, or QWERTY-neighbor substitution, and then requires the resulting
     * token to satisfy the same exact-dictionary segmentation confidence rules. Ambiguous phrases
     * are rejected.
     */
    fun resolveWithSingleEdit(
        token: String,
        dictionary: Collection<String>,
    ): String? {
        resolve(token, dictionary)?.let { return it }

        val normalized = token.lowercase()
        if (normalized.length !in MIN_TOKEN_LENGTH..MAX_TOKEN_LENGTH) return null
        if (normalized.any { it !in 'a'..'z' }) return null
        if (indexFor(dictionary).containsKey(normalized)) return null

        val phrases = LinkedHashMap<String, Double>()

        fun consider(candidate: String, editCost: Double) {
            if (candidate.length !in MIN_TOKEN_LENGTH..MAX_TOKEN_LENGTH) return
            val phrase = resolve(candidate, dictionary) ?: return
            val current = phrases[phrase]
            if (current == null || editCost < current) phrases[phrase] = editCost
        }

        normalized.indices.forEach { index ->
            consider(normalized.removeRange(index, index + 1), 1.0)

            if (index + 1 < normalized.length && normalized[index] != normalized[index + 1]) {
                val swapped = normalized.toCharArray().also { chars ->
                    val value = chars[index]
                    chars[index] = chars[index + 1]
                    chars[index + 1] = value
                }.concatToString()
                consider(swapped, 0.90)
            }

            qwertyNeighbors[normalized[index]].orEmpty().forEach { replacement ->
                val replaced = normalized.toCharArray().also { chars ->
                    chars[index] = replacement
                }.concatToString()
                consider(replaced, 0.85)
            }
        }

        val ranked = phrases.entries.sortedWith(
            compareBy<Map.Entry<String, Double>> { it.value }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.key },
        )
        val best = ranked.firstOrNull() ?: return null
        val runnerUp = ranked.getOrNull(1)
        if (runnerUp != null && runnerUp.value - best.value < SINGLE_EDIT_WIN_MARGIN) return null
        return best.key
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
        const val SINGLE_EDIT_WIN_MARGIN = 0.10

        val SINGLE_LETTER_WORDS = setOf("a", "i")

        val qwertyNeighbors = mapOf(
            'q' to "wa", 'w' to "qeas", 'e' to "wrsd", 'r' to "etdf", 't' to "ryfg",
            'y' to "tugh", 'u' to "yihj", 'i' to "uojk", 'o' to "ipkl", 'p' to "ol",
            'a' to "qwsz", 's' to "wedxza", 'd' to "erfcxs", 'f' to "rtgcvd",
            'g' to "tyhbvf", 'h' to "yujnbg", 'j' to "uikmnh", 'k' to "iolmj",
            'l' to "opk", 'z' to "asx", 'x' to "sdc z".replace(" ", ""),
            'c' to "dfvx", 'v' to "fgbc", 'b' to "ghnv", 'n' to "hjmb", 'm' to "jkn",
        )
    }
}
