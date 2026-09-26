package com.goreecloud.keyboard

/**
 * Re-ranks already-decoded swipe candidates using local next-word context.
 *
 * Geometry remains authoritative for which words are eligible. Context may only promote candidates
 * that the swipe decoder already accepted, so prediction never manufactures an unrelated swipe
 * result. Both inputs are local and bounded.
 */
internal object SwipeCandidateRanker {
    fun rank(
        decoded: List<String>,
        contextualPredictions: List<String>,
        limit: Int = 3,
    ): List<String> {
        if (limit <= 0 || decoded.isEmpty()) return emptyList()

        val contextRank = contextualPredictions
            .distinctBy { it.lowercase() }
            .mapIndexed { index, value -> value.lowercase() to index }
            .toMap()

        return decoded
            .withIndex()
            .sortedWith(
                compareBy<IndexedValue<String>> {
                    contextRank[it.value.lowercase()] ?: Int.MAX_VALUE
                }.thenBy { it.index },
            )
            .map { it.value }
            .distinctBy { it.lowercase() }
            .take(limit)
    }
}
