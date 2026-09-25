package com.goreecloud.keyboard

/**
 * Small first-party local prediction/grammar layer.
 *
 * It intentionally uses only the words committed by this Keyboard during the active editor session.
 * It does not inspect arbitrary surrounding editor text, persist history, learn a profile, or use
 * network/account/clipboard data.
 */
internal object QuillPredictionModel {
    private val phrasePredictions = mapOf(
        "does it" to listOf("feel", "work", "look"),
        "it feel" to listOf("better", "good", "right"),
        "feel better" to listOf("now", "today", "already"),
        "thank you" to listOf("for", "so", "very"),
        "can you" to listOf("please", "help", "send"),
        "would you" to listOf("like", "please", "be"),
        "i am" to listOf("going", "not", "ready"),
        "i need" to listOf("to", "a", "help"),
        "goreecloud keyboard" to listOf("settings", "suggestions", "typing"),
    )

    private val wordPredictions = mapOf(
        "i" to listOf("am", "have", "will"),
        "you" to listOf("are", "can", "will"),
        "we" to listOf("are", "can", "should"),
        "they" to listOf("are", "have", "will"),
        "he" to listOf("is", "was", "has"),
        "she" to listOf("is", "was", "has"),
        "it" to listOf("is", "was", "will"),
        "does" to listOf("it", "this", "that"),
        "this" to listOf("is", "will", "looks"),
        "that" to listOf("is", "was", "would"),
        "the" to listOf("keyboard", "app", "settings"),
        "my" to listOf("keyboard", "phone", "app"),
        "please" to listOf("let", "help", "send"),
        "goreecloud" to listOf("Keyboard", "Browser", "Notes"),
        "keyboard" to listOf("settings", "suggestions", "typing"),
        "privacy" to listOf("Shield", "settings", "controls"),
        "wardveil" to listOf("Security", "settings", "protection"),
        "glaze" to listOf("UI", "design", "settings"),
    )

    fun predict(committedHistory: List<String>, limit: Int = 3): List<String> {
        if (limit <= 0 || committedHistory.isEmpty()) return emptyList()

        val normalized = committedHistory
            .takeLast(2)
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
        if (normalized.isEmpty()) return emptyList()

        val phrase = if (normalized.size == 2) normalized.joinToString(" ") else null
        val source = phrase?.let(phrasePredictions::get)
            ?: wordPredictions[normalized.last()]
            ?: emptyList()

        return source.distinctBy { it.lowercase() }.take(limit)
    }

    fun boundaryCorrection(word: String): String? = when (word.lowercase()) {
        "goreecloud" -> "GoreeCloud"
        "wardveil" -> "Wardveil"
        "everkeep" -> "Everkeep"
        "i" -> "I"
        "im" -> "I'm"
        "ive" -> "I've"
        "dont" -> "don't"
        "doesnt" -> "doesn't"
        "cant" -> "can't"
        "wont" -> "won't"
        "isnt" -> "isn't"
        "arent" -> "aren't"
        "wasnt" -> "wasn't"
        "werent" -> "weren't"
        "shouldnt" -> "shouldn't"
        "wouldnt" -> "wouldn't"
        "couldnt" -> "couldn't"
        "youre" -> "you're"
        "theyre" -> "they're"
        else -> null
    }
}
