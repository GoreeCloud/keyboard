package com.goreecloud.keyboard

/**
 * Re-ranks already-decoded swipe candidates using local next-word context.
 *
 * The decoder's first result is pinned because it represents the strongest physical gesture match.
 * Context may refine only alternate slots; prediction must never replace what the finger most
 * strongly traced.
 */
internal object SwipeCandidateRanker {
    fun rank(
        decoded: List<String>,
        contextualPredictions: List<String>,
        limit: Int = 3,
    ): List<String> {
        if (limit <= 0 || decoded.isEmpty()) return emptyList()

        val unique = decoded.distinctBy { it.lowercase() }
        val geometryWinner = unique.first()
        if (limit == 1 || unique.size == 1) return listOf(geometryWinner)

        val contextRank = contextualPredictions
            .distinctBy { it.lowercase() }
            .mapIndexed { index, value -> value.lowercase() to index }
            .toMap()

        val alternates = unique.drop(1).withIndex()
            .sortedWith(
                compareBy<IndexedValue<String>> { candidate ->
                    val geometryRank = candidate.index.toDouble() + 1.0
                    geometryRank - contextBoost(contextRank[candidate.value.lowercase()])
                }.thenBy { it.index },
            )
            .map { it.value }

        return (listOf(geometryWinner) + alternates).take(limit)
    }

    private fun contextBoost(contextIndex: Int?): Double = when (contextIndex) {
        0 -> 1.35
        1 -> 0.90
        2 -> 0.55
        3 -> 0.30
        null -> 0.0
        else -> 0.15
    }
}
