package com.goreecloud.keyboard

/**
 * Local phrase-level spelling guidance for common hyphenated compounds.
 *
 * These are presented as explicit replacement suggestions rather than unconditional autocorrect.
 * English compounds can legitimately remain open in predicative contexts ("the app is up to date")
 * while taking hyphens attributively ("an up-to-date app"). Explicit suggestions avoid silently
 * damaging valid prose while still teaching and enabling the compound spelling.
 */
internal object HyphenatedCompoundModel {
    data class Rewrite(
        val sourceWords: List<String>,
        val replacement: String,
    )

    private val rewrites = listOf(
        Rewrite(listOf("up", "to", "date"), "up-to-date"),
        Rewrite(listOf("built", "in"), "built-in"),
        Rewrite(listOf("well", "known"), "well-known"),
        Rewrite(listOf("high", "quality"), "high-quality"),
        Rewrite(listOf("long", "term"), "long-term"),
        Rewrite(listOf("short", "term"), "short-term"),
        Rewrite(listOf("real", "time"), "real-time"),
        Rewrite(listOf("full", "time"), "full-time"),
        Rewrite(listOf("part", "time"), "part-time"),
        Rewrite(listOf("user", "friendly"), "user-friendly"),
        Rewrite(listOf("privacy", "focused"), "privacy-focused"),
        Rewrite(listOf("open", "source"), "open-source"),
        Rewrite(listOf("first", "party"), "first-party"),
        Rewrite(listOf("third", "party"), "third-party"),
        Rewrite(listOf("cross", "platform"), "cross-platform"),
        Rewrite(listOf("on", "device"), "on-device"),
        Rewrite(listOf("local", "first"), "local-first"),
        Rewrite(listOf("self", "hosted"), "self-hosted"),
        Rewrite(listOf("side", "by", "side"), "side-by-side"),
        Rewrite(listOf("end", "to", "end"), "end-to-end"),
    )

    fun rewriteForTail(history: List<String>): Rewrite? {
        val normalized = history.map { it.lowercase() }
        return rewrites
            .asSequence()
            .sortedByDescending { it.sourceWords.size }
            .firstOrNull { rewrite ->
                normalized.size >= rewrite.sourceWords.size &&
                    normalized.takeLast(rewrite.sourceWords.size) == rewrite.sourceWords
            }
    }
}
