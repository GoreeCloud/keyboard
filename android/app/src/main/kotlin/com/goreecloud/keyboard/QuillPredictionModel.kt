package com.goreecloud.keyboard

/**
 * Small first-party local prediction/grammar layer.
 *
 * It intentionally uses only the words committed by this Keyboard during the active editor session.
 * It does not inspect arbitrary surrounding editor text, persist history, learn a profile, or use
 * network/account/clipboard data.
 */
internal object QuillPredictionModel {
    private val starterPredictions = listOf("I", "I'm", "The")

    private val phrasePredictions = mapOf(
        "does it" to listOf("feel", "work", "look"),
        "it feel" to listOf("better", "good", "right"),
        "feel better" to listOf("now", "today", "already"),
        "how can" to listOf("I", "you", "we"),
        "how are" to listOf("you", "they", "we"),
        "how do" to listOf("I", "you", "we"),
        "how does" to listOf("it", "this", "that"),
        "can you" to listOf("please", "help", "send"),
        "can i" to listOf("get", "have", "use"),
        "do you" to listOf("want", "know", "have"),
        "are you" to listOf("going", "ready", "sure"),
        "did you" to listOf("get", "see", "know"),
        "would you" to listOf("like", "please", "be"),
        "could you" to listOf("please", "help", "send"),
        "i am" to listOf("going", "not", "ready"),
        "i have" to listOf("a", "the", "been"),
        "i need" to listOf("to", "a", "help"),
        "i want" to listOf("to", "a", "the"),
        "i would" to listOf("like", "love", "prefer"),
        "i think" to listOf("the", "it", "this"),
        "i know" to listOf("that", "the", "you"),
        "i can" to listOf("see", "help", "do"),
        "i will" to listOf("be", "send", "check"),
        "i hope" to listOf("you", "it", "this"),
        "we can" to listOf("do", "use", "make"),
        "we need" to listOf("to", "a", "the"),
        "we should" to listOf("be", "use", "check"),
        "thank you" to listOf("for", "so", "very"),
        "thanks for" to listOf("the", "your", "help"),
        "please let" to listOf("me", "us", "them"),
        "let me" to listOf("know", "check", "see"),
        "it is" to listOf("a", "the", "not"),
        "it was" to listOf("a", "the", "really"),
        "this is" to listOf("a", "the", "not"),
        "that is" to listOf("a", "the", "not"),
        "the keyboard" to listOf("is", "settings", "needs"),
        "goreecloud keyboard" to listOf("settings", "suggestions", "typing"),
        "privacy shield" to listOf("settings", "policy", "controls"),
        "wardveil security" to listOf("settings", "policy", "controls"),
        "glaze ui" to listOf("design", "settings", "system"),
    )

    private val wordPredictions = mapOf(
        "i" to listOf("am", "have", "will"),
        "you" to listOf("are", "can", "will"),
        "we" to listOf("are", "can", "should"),
        "they" to listOf("are", "have", "will"),
        "he" to listOf("is", "was", "has"),
        "she" to listOf("is", "was", "has"),
        "it" to listOf("is", "was", "will"),
        "this" to listOf("is", "will", "looks"),
        "that" to listOf("is", "was", "would"),
        "these" to listOf("are", "can", "will"),
        "those" to listOf("are", "were", "look"),
        "how" to listOf("can", "are", "do"),
        "what" to listOf("is", "are", "do"),
        "where" to listOf("is", "are", "can"),
        "when" to listOf("is", "are", "will"),
        "why" to listOf("is", "are", "do"),
        "who" to listOf("is", "are", "can"),
        "can" to listOf("you", "I", "we"),
        "could" to listOf("you", "I", "we"),
        "would" to listOf("you", "like", "be"),
        "should" to listOf("be", "we", "I"),
        "will" to listOf("be", "you", "have"),
        "do" to listOf("you", "not", "it"),
        "does" to listOf("it", "this", "that"),
        "did" to listOf("you", "not", "it"),
        "are" to listOf("you", "the", "we"),
        "is" to listOf("the", "a", "not"),
        "was" to listOf("the", "a", "not"),
        "were" to listOf("the", "you", "not"),
        "have" to listOf("a", "the", "been"),
        "has" to listOf("a", "the", "been"),
        "had" to listOf("a", "the", "been"),
        "want" to listOf("to", "a", "the"),
        "need" to listOf("to", "a", "the"),
        "like" to listOf("to", "a", "the"),
        "love" to listOf("to", "the", "this"),
        "think" to listOf("the", "it", "this"),
        "know" to listOf("that", "the", "you"),
        "feel" to listOf("better", "like", "good"),
        "look" to listOf("at", "like", "good"),
        "going" to listOf("to", "home", "back"),
        "please" to listOf("let", "help", "send"),
        "thanks" to listOf("for", "so", "again"),
        "thank" to listOf("you", "the", "them"),
        "the" to listOf("keyboard", "app", "settings"),
        "a" to listOf("good", "new", "little"),
        "an" to listOf("app", "example", "issue"),
        "my" to listOf("keyboard", "phone", "app"),
        "your" to listOf("phone", "app", "settings"),
        "our" to listOf("app", "team", "system"),
        "goreecloud" to listOf("Keyboard", "Browser", "Notes"),
        "keyboard" to listOf("settings", "suggestions", "typing"),
        "privacy" to listOf("Shield", "settings", "controls"),
        "wardveil" to listOf("Security", "settings", "protection"),
        "glaze" to listOf("UI", "design", "settings"),
        "settings" to listOf("are", "for", "can"),
        "app" to listOf("is", "settings", "can"),
        "message" to listOf("to", "is", "was"),
        "today" to listOf("I", "is", "we"),
        "tomorrow" to listOf("I", "we", "is"),
        "now" to listOf("I", "we", "it"),
        "hello" to listOf("there", "everyone", "again"),
        "hey" to listOf("there", "how", "can"),
        "good" to listOf("morning", "idea", "luck"),
        "great" to listOf("job", "idea", "thanks"),
        "yes" to listOf("I", "that", "please"),
        "no" to listOf("I", "problem", "thanks"),
        "because" to listOf("I", "it", "the"),
        "if" to listOf("you", "I", "the"),
        "but" to listOf("I", "the", "it"),
        "and" to listOf("the", "I", "you"),
        "or" to listOf("the", "you", "I"),
        "to" to listOf("the", "be", "you"),
        "for" to listOf("the", "you", "a"),
        "with" to listOf("the", "you", "a"),
        "from" to listOf("the", "my", "your"),
        "at" to listOf("the", "home", "work"),
        "on" to listOf("the", "my", "your"),
        "in" to listOf("the", "a", "my"),
        "about" to listOf("the", "this", "it"),
        "not" to listOf("sure", "the", "a"),
        "very" to listOf("good", "nice", "important"),
        "really" to listOf("like", "good", "want"),
        "still" to listOf("have", "need", "want"),
        "already" to listOf("have", "done", "know"),
    )

    private val prepositions = setOf(
        "at", "by", "for", "from", "in", "into", "of", "on", "to", "with", "without",
    )
    private val auxiliaries = setOf(
        "am", "are", "can", "could", "did", "do", "does", "had", "has", "have", "is",
        "might", "must", "should", "was", "were", "will", "would",
    )
    private val determiners = setOf("a", "an", "the", "this", "that", "these", "those", "my", "your", "our")
    private val fallbackPredictions = listOf("the", "to", "and")

    private val boundaryCorrections = mapOf(
        "i" to "I",
        "im" to "I'm",
        "ive" to "I've",
        "dont" to "don't",
        "doesnt" to "doesn't",
        "didnt" to "didn't",
        "cant" to "can't",
        "wont" to "won't",
        "isnt" to "isn't",
        "arent" to "aren't",
        "wasnt" to "wasn't",
        "werent" to "weren't",
        "havent" to "haven't",
        "hasnt" to "hasn't",
        "hadnt" to "hadn't",
        "shouldnt" to "shouldn't",
        "wouldnt" to "wouldn't",
        "couldnt" to "couldn't",
        "youre" to "you're",
        "theyre" to "they're",
        "whats" to "what's",
        "thats" to "that's",
        "wheres" to "where's",
        "theres" to "there's",
        "couldve" to "could've",
        "wouldve" to "would've",
        "shouldve" to "should've",
    )

    private val canonicalCasing = mapOf(
        "goreecloud" to "GoreeCloud",
        "wardveil" to "Wardveil",
        "everkeep" to "Everkeep",
        "glaze" to "Glaze",
        "quill" to "Quill",
    )

    fun predict(committedHistory: List<String>, limit: Int = 3): List<String> {
        if (limit <= 0) return emptyList()

        val normalized = committedHistory
            .takeLast(2)
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }

        if (normalized.isEmpty()) {
            return starterPredictions.take(limit)
        }

        val phrase = if (normalized.size == 2) normalized.joinToString(" ") else null
        val last = normalized.last()
        val source = phrase?.let(phrasePredictions::get)
            ?: wordPredictions[last]
            ?: contextualFallback(last)

        return source.distinctBy { it.lowercase() }.take(limit)
    }

    private fun contextualFallback(last: String): List<String> = when {
        last in prepositions -> listOf("the", "a", "my")
        last in auxiliaries -> listOf("the", "you", "a")
        last in determiners -> listOf("new", "good", "same")
        last.endsWith("ing") -> listOf("the", "to", "and")
        else -> fallbackPredictions
    }

    fun boundaryCorrection(word: String): String? {
        val normalized = word.lowercase()
        return canonicalCasing[normalized] ?: boundaryCorrections[normalized]
    }
}
