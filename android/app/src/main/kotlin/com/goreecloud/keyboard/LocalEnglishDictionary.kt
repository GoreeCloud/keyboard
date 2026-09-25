package com.goreecloud.keyboard

/**
 * Packaged, read-only English seed dictionary for local Development suggestions.
 *
 * This is static application data. It does not learn from typed text, persist user input,
 * synchronize, use network access, or create a usage profile.
 */
internal object LocalEnglishDictionary {
    val words: List<String> = listOf(
        "I", "a", "able", "about", "above", "after", "again", "against", "all", "also",
        "always", "am", "an", "and", "another", "any", "app", "are", "around", "as", "ask",
        "at", "away", "back", "be", "because", "been", "before", "being", "best", "better",
        "between", "both", "but", "by", "call", "can", "change", "check", "cloud", "come",
        "could", "day", "device", "did", "different", "do", "does", "done", "down", "each",
        "email", "even", "every", "family", "feel", "find", "first", "for", "from", "get",
        "give", "go", "goal", "good", "goreecloud", "great", "had", "has", "have", "he",
        "hello", "help", "her", "here", "him", "his", "home", "how", "I", "if", "in",
        "into", "is", "it", "its", "just", "keep", "keyboard", "know", "last", "like",
        "little", "long", "look", "love", "make", "many", "may", "me", "message", "more",
        "most", "much", "my", "native", "need", "never", "new", "next", "no", "not",
        "now", "of", "off", "on", "one", "only", "open", "or", "other", "our", "out",
        "over", "people", "please", "privacy", "question", "quick", "really", "right",
        "same", "say", "secure", "security", "see", "send", "should", "since", "so",
        "some", "something", "start", "still", "suggestion", "system", "take", "tell",
        "test", "than", "thank", "thanks", "that", "the", "their", "them", "then",
        "there", "these", "they", "thing", "think", "this", "time", "to", "today",
        "too", "type", "typing", "up", "use", "very", "want", "was", "way", "we",
        "well", "were", "what", "when", "where", "which", "who", "why", "will", "with",
        "word", "work", "world", "would", "write", "writing", "yes", "you", "your",
        "zero", "zone", "xray"
    ).distinctBy { it.lowercase() }
}
