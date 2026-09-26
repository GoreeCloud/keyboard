package com.goreecloud.keyboard

/**
 * Conservative first-party English grammar/context assistance.
 *
 * This is not a generative model. It supplies deterministic local phrase continuations and a small
 * set of high-confidence boundary repairs that can be explained and tested.
 */
internal object QuillGrammarModel {
    private val phrasePredictions = mapOf(
        "i don't" to listOf("know", "think", "need", "want"),
        "i do" to listOf("not", "have", "think", "want"),
        "i need" to listOf("to", "a", "the", "more"),
        "i want" to listOf("to", "a", "the", "you"),
        "i have" to listOf("a", "the", "been", "to"),
        "i am" to listOf("not", "going", "trying", "ready"),
        "i'm" to listOf("not", "going", "trying", "ready"),
        "i think" to listOf("the", "it", "this", "that"),
        "i know" to listOf("that", "the", "you", "it"),
        "i can" to listOf("see", "help", "do", "get"),
        "i will" to listOf("be", "have", "send", "check"),
        "i would" to listOf("like", "love", "prefer", "be"),
        "you are" to listOf("the", "a", "not", "going"),
        "you're" to listOf("the", "a", "not", "going"),
        "you can" to listOf("see", "use", "get", "try"),
        "you should" to listOf("be", "have", "try", "use"),
        "we are" to listOf("going", "not", "the", "a"),
        "we can" to listOf("use", "do", "make", "get"),
        "we need" to listOf("to", "a", "the", "more"),
        "they are" to listOf("the", "not", "going", "a"),
        "it is" to listOf("a", "the", "not", "very"),
        "it's" to listOf("a", "the", "not", "very"),
        "this is" to listOf("a", "the", "not", "what"),
        "that is" to listOf("a", "the", "not", "why"),
        "there is" to listOf("a", "the", "no", "more"),
        "there are" to listOf("a", "the", "no", "many"),
        "how can" to listOf("I", "you", "we", "this"),
        "how do" to listOf("I", "you", "we", "they"),
        "how does" to listOf("it", "this", "that", "the"),
        "what is" to listOf("the", "a", "this", "that"),
        "what are" to listOf("the", "you", "these", "those"),
        "where is" to listOf("the", "my", "your", "this"),
        "where are" to listOf("the", "you", "my", "your"),
        "when is" to listOf("the", "it", "this", "that"),
        "why is" to listOf("the", "it", "this", "that"),
        "why are" to listOf("you", "the", "we", "they"),
        "thank you" to listOf("for", "so", "very", "again"),
        "thanks for" to listOf("the", "your", "help", "everything"),
        "let me" to listOf("know", "see", "check", "try"),
        "please let" to listOf("me", "us", "them", "him"),
        "need to" to listOf("be", "get", "make", "use"),
        "want to" to listOf("be", "get", "see", "use"),
        "going to" to listOf("be", "the", "get", "have"),
        "have to" to listOf("be", "go", "get", "make"),
        "used to" to listOf("be", "have", "work", "use"),
        "the keyboard" to listOf("is", "should", "needs", "has"),
        "keyboard is" to listOf("not", "too", "a", "still"),
        "keyboard needs" to listOf("to", "a", "better", "more"),
        "the toolbar" to listOf("is", "should", "needs", "has"),
        "toolbar icon" to listOf("should", "is", "looks", "needs"),
        "word prediction" to listOf("is", "should", "needs", "can"),
        "word suggestions" to listOf("are", "should", "need", "can"),
        "swipe typing" to listOf("is", "should", "needs", "can"),
        "goreecloud keyboard" to listOf("settings", "suggestions", "typing", "dictionary"),
    )

    private val modalVerbs = setOf("can", "could", "may", "might", "must", "shall", "should", "will", "would")
    private val pluralSubjects = setOf("we", "you", "they")
    private val singularSubjects = setOf("he", "she", "it", "this", "that")
    private val determiners = setOf("a", "an", "the", "my", "your", "our", "his", "her", "their", "this", "that")
    private val prepositions = setOf("at", "by", "for", "from", "in", "into", "of", "on", "over", "to", "under", "with", "without")

    fun predict(history: List<String>, limit: Int = 6): List<String> {
        if (limit <= 0) return emptyList()
        val normalized = history
            .takeLast(3)
            .map { it.lowercase().replace('’', '\'') }
            .filter { it.isNotBlank() }

        if (normalized.isEmpty()) return listOf("I", "I'm", "The", "It", "This", "How").take(limit)

        val result = mutableListOf<String>()
        if (normalized.size >= 2) {
            phrasePredictions[normalized.takeLast(2).joinToString(" ")]?.let(result::addAll)
        }

        val last = normalized.last()
        result += when {
            last == "i" -> listOf("am", "have", "will", "can", "need", "want")
            last in pluralSubjects -> listOf("are", "have", "will", "can", "should", "were")
            last in singularSubjects -> listOf("is", "has", "will", "can", "was", "should")
            last in modalVerbs -> listOf("be", "have", "get", "use", "go", "make")
            last == "to" -> listOf("be", "have", "get", "make", "use", "see")
            last in setOf("is", "am", "are", "was", "were") ->
                listOf("not", "the", "a", "going", "still", "very")
            last in setOf("have", "has", "had") ->
                listOf("been", "a", "the", "to", "already", "not")
            last in determiners -> listOf("new", "same", "best", "keyboard", "app", "settings")
            last in prepositions -> listOf("the", "a", "my", "your", "this", "that")
            last in setOf("and", "but", "so", "because", "if", "while") ->
                listOf("I", "the", "it", "you", "we", "this")
            last.endsWith("ing") -> listOf("the", "to", "and", "a", "with", "for")
            else -> listOf("the", "to", "and", "a", "is", "that")
        }

        return result
            .distinctBy { it.lowercase() }
            .take(limit)
    }

    fun boundaryCorrection(word: String, history: List<String>): String? {
        val normalized = word.lowercase().replace('’', '\'')
        QuillPredictionModel.boundaryCorrection(normalized)?.let { return it }

        val previous = history.lastOrNull()?.lowercase()?.replace('’', '\'')
        if (
            normalized == "of" &&
            previous in setOf("could", "would", "should", "might", "must")
        ) {
            return "have"
        }

        return when (normalized) {
            "alot" -> "a lot"
            "thru" -> "through"
            else -> null
        }
    }
}
