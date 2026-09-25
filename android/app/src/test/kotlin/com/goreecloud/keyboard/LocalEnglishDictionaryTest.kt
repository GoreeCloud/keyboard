package com.goreecloud.keyboard

import org.junit.Assert.assertTrue
import org.junit.Test

class LocalEnglishDictionaryTest {
    @Test
    fun packagedDictionaryContainsCommonAndGoreeCloudTerms() {
        val normalized = LocalEnglishDictionary.words.map { it.lowercase() }.toSet()

        for (word in listOf(
            "the", "hello", "message", "keyboard", "privacy", "security",
            "goreecloud", "question", "quick", "zero", "zone", "xray"
        )) {
            assertTrue("expected packaged dictionary word $word", word in normalized)
        }
        assertTrue(
            "packaged dictionary should be materially broader than the old bootstrap list",
            normalized.size >= 150,
        )
    }
}
