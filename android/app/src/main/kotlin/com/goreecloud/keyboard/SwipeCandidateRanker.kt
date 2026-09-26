package com.goreecloud.keyboard

/**
 * Re-ranks already-decoded swipe candidates using local next-word context.
 *
 * Geometry remains dominant. Context may gently promote an already-plausible candidate, but cannot
 * pull a weak geometric match from the bottom of the decoder pool to the top. This avoids a common
 * failure mode where language context overwhelms what the finger actually traced.
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
            .distinctBy { it.lowercase() }
            .withIndex()
            .sortedWith(
                compareBy<IndexedValue<String>> { candidate ->
                    val geometryRank = candidate.index.toDouble()
                    val contextIndex = contextRank[candidate.value.lowercase()]
                    geometryRank - contextBoost(contextIndex)
                }.thenBy { it.index },
            )
            .map { it.value }
            .take(limit)
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
