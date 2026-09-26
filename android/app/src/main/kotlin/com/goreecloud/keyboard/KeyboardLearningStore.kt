package com.goreecloud.keyboard

import android.content.Context

/**
 * Optional device-local language adaptation for Keyboard.
 *
 * Privacy properties:
 * - disabled by default through [KeyboardSettingsStore];
 * - never records full editor text, sentences, clipboard content, account data, or field identity;
 * - stores only normalized word-frequency and adjacent-word-frequency counters;
 * - callers must exclude sensitive and no-suggestions editors;
 * - storage is bounded and user-clearable;
 * - no network, synchronization, telemetry, backup export, or cross-app identifier is used.
 */
internal class KeyboardLearningStore(context: Context) {
    private val preferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    @Synchronized
    fun record(word: String, previousWord: String?) {
        val normalized = normalize(word) ?: return

        val words = parseWords().toMutableMap()
        words[normalized] = (words[normalized] ?: 0).plus(1).coerceAtMost(MAX_COUNT)
        persistWords(words)

        val previous = previousWord?.let(::normalize) ?: return
        val bigrams = parseBigrams().toMutableMap()
        val key = Bigram(previous, normalized)
        bigrams[key] = (bigrams[key] ?: 0).plus(1).coerceAtMost(MAX_COUNT)
        persistBigrams(bigrams)
    }

    @Synchronized
    fun learnedWords(limit: Int = MAX_WORDS): List<String> =
        parseWords()
            .entries
            .sortedWith(
                compareByDescending<Map.Entry<String, Int>> { it.value }
                    .thenBy { it.key },
            )
            .take(limit.coerceAtLeast(0))
            .map { it.key }

    @Synchronized
    fun predictNext(history: List<String>, limit: Int = 3): List<String> {
        if (limit <= 0) return emptyList()
        val previous = history.lastOrNull()?.let(::normalize) ?: return starterWords(limit)

        return parseBigrams()
            .asSequence()
            .filter { it.key.previous == previous }
            .sortedWith(
                compareByDescending<Map.Entry<Bigram, Int>> { it.value }
                    .thenBy { it.key.next },
            )
            .map { it.key.next }
            .distinct()
            .take(limit)
            .toList()
    }

    @Synchronized
    fun starterWords(limit: Int = 3): List<String> =
        parseWords()
            .entries
            .asSequence()
            .filter { it.value >= MIN_STARTER_COUNT }
            .sortedWith(
                compareByDescending<Map.Entry<String, Int>> { it.value }
                    .thenBy { it.key },
            )
            .map { it.key }
            .take(limit.coerceAtLeast(0))
            .toList()

    @Synchronized
    fun learnedWordCount(): Int = parseWords().size

    @Synchronized
    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun persistWords(values: Map<String, Int>) {
        val bounded = values.entries
            .sortedWith(
                compareByDescending<Map.Entry<String, Int>> { it.value }
                    .thenBy { it.key },
            )
            .take(MAX_WORDS)
            .joinToString("\n") { "${it.key}\t${it.value}" }

        preferences.edit().putString(KEY_WORDS, bounded).apply()
    }

    private fun persistBigrams(values: Map<Bigram, Int>) {
        val bounded = values.entries
            .sortedWith(
                compareByDescending<Map.Entry<Bigram, Int>> { it.value }
                    .thenBy { it.key.previous }
                    .thenBy { it.key.next },
            )
            .take(MAX_BIGRAMS)
            .joinToString("\n") {
                "${it.key.previous}\t${it.key.next}\t${it.value}"
            }

        preferences.edit().putString(KEY_BIGRAMS, bounded).apply()
    }

    private fun parseWords(): Map<String, Int> =
        preferences.getString(KEY_WORDS, null)
            ?.lineSequence()
            ?.mapNotNull { line ->
                val parts = line.split('\t')
                val word = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val count = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(1, MAX_COUNT)
                    ?: return@mapNotNull null
                word to count
            }
            ?.toMap()
            ?: emptyMap()

    private fun parseBigrams(): Map<Bigram, Int> =
        preferences.getString(KEY_BIGRAMS, null)
            ?.lineSequence()
            ?.mapNotNull { line ->
                val parts = line.split('\t')
                val previous = parts.getOrNull(0)?.takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null
                val next = parts.getOrNull(1)?.takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null
                val count = parts.getOrNull(2)?.toIntOrNull()?.coerceIn(1, MAX_COUNT)
                    ?: return@mapNotNull null
                Bigram(previous, next) to count
            }
            ?.toMap()
            ?: emptyMap()

    private fun normalize(value: String): String? {
        val normalized = value
            .trim()
            .lowercase()
            .replace('’', '\'')
        val codePointCount = normalized.codePointCount(0, normalized.length)
        if (codePointCount !in MIN_WORD_LENGTH..MAX_WORD_LENGTH) return null
        if (!WORD_PATTERN.matches(normalized)) return null
        return normalized
    }

    private data class Bigram(val previous: String, val next: String)

    private companion object {
        const val PREFERENCES_NAME = "goreecloud_keyboard_local_learning"
        const val KEY_WORDS = "learned_words_v1"
        const val KEY_BIGRAMS = "learned_bigrams_v1"
        const val MAX_WORDS = 512
        const val MAX_BIGRAMS = 1024
        const val MAX_COUNT = 9999
        const val MIN_WORD_LENGTH = 2
        const val MAX_WORD_LENGTH = 32
        const val MIN_STARTER_COUNT = 2

        val WORD_PATTERN = Regex("^[\\p{L}]+(?:['-][\\p{L}]+)*$")
    }
}
