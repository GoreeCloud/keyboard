package com.goreecloud.keyboard

import android.content.Context
import java.util.Locale

/**
 * Device-local packaged English lexicon used by suggestions, correction, and swipe decoding.
 *
 * The first-party frequency-ordered Quill vocabulary stays at the front of the list. The bundled
 * FrequencyWords English supplement preserves corpus-frequency order and extends lexical coverage
 * without adding network access, telemetry, account state, or typed-text collection.
 */
internal class PackagedEnglishDictionary(
    context: Context,
) {
    private val applicationContext = context.applicationContext

    val words: List<String> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        val ordered = LinkedHashMap<String, String>()

        fun add(word: String) {
            val trimmed = word.trim()
            if (trimmed.isEmpty()) return
            val normalized = trimmed.lowercase(Locale.ROOT)
            if (normalized.length > MAX_WORD_LENGTH) return
            if (!normalized.codePoints().allMatch { Character.isLetter(it) || it == '\''.code }) return
            ordered.putIfAbsent(normalized, trimmed)
        }

        QuillLexicon.expandedEnglish.forEach(::add)

        applicationContext.assets
            .open(ASSET_PATH)
            .bufferedReader(Charsets.UTF_8)
            .useLines { lines -> lines.forEach(::add) }

        ordered.values.toList()
    }

    fun preload() {
        words.size
    }

    companion object {
        const val ASSET_PATH = "dictionaries/en_us_frequency_common.txt"
        const val SOURCE_PROJECT = "FrequencyWords / OpenSubtitles 2018 English"
        const val SOURCE_REVISION = "525f9b560de45753a5ea01069454e72e9aa541c6"
        const val SOURCE_BLOB = "bbf5fa991058ff642732784b31af023f66462e1d"
        const val EXPECTED_FALLBACK_WORDS = 46_691
        private const val MAX_WORD_LENGTH = 48
    }
}
