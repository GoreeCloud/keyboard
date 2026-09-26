package com.goreecloud.keyboard

/**
 * Extracts a tiny transient language context from ordinary editor text immediately before the
 * cursor. The caller controls whether the editor is eligible. This parser never persists content.
 */
internal object EditorContextParser {
    fun wordsBeforeCursor(text: CharSequence?, limit: Int = 4): List<String> {
        if (text == null || limit <= 0) return emptyList()

        val normalized = text.toString()
            .replace('’', '\'')
            .lowercase()

        return WORD.findAll(normalized)
            .map { it.value }
            .toList()
            .takeLast(limit)
    }

    fun currentWordBeforeCursor(text: CharSequence?): String? {
        val value = text?.toString()
            ?.replace('’', '\'')
            ?: return null
        if (value.isEmpty()) return null
        return CURRENT_WORD.find(value)?.value
    }

    fun isSentenceBoundary(text: CharSequence?): Boolean {
        val value = text?.toString()?.trimEnd().orEmpty()
        if (value.isEmpty()) return true
        return value.last() in setOf('.', '!', '?', '\n')
    }

    private val WORD = Regex("[\\p{L}]+(?:['-][\\p{L}]+)*")
    private val CURRENT_WORD = Regex("[\\p{L}]+(?:['-][\\p{L}]*)*$")
}
