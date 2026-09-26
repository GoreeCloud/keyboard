package com.goreecloud.keyboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuillLexiconTest {
    @Test
    fun expandedDictionaryContainsCommonDerivedForms() {
        val words = QuillLexicon.expandedEnglish.map { it.lowercase() }.toSet()

        assertTrue("shows" in words)
        assertTrue("showing" in words)
        assertTrue("typed" in words)
        assertTrue("typing" in words)
        assertTrue("messages" in words)
        assertTrue("suggestions" in words)
        assertTrue("goreecloud" in words)
        assertTrue("wardveil" in words)
        assertTrue("everkeep" in words)
        assertTrue("don't" in words)
        assertTrue("doesn't" in words)
        assertTrue("okay" in words)
        assertTrue("tomorrow" in words)
        assertTrue("weekend" in words)
        assertTrue("clipboard" in words)
        assertTrue("wifi" in words)
        assertTrue("basic" in words)
        assertTrue("icon" in words)
        assertTrue("toolbar" in words)
        assertTrue("glyph" in words)
        assertTrue("grammar" in words)
        assertTrue("prediction" in words)
        assertTrue("gesture" in words)
        assertTrue("haptic" in words)
    }

    @Test
    fun expandedDictionaryRemainsLocalStaticVocabulary() {
        val words = QuillLexicon.expandedEnglish
        assertTrue(words.isNotEmpty())
        assertFalse(words.any { it.isBlank() })
        assertTrue(EverydayEnglishDictionary.terms.size >= 300)
        assertTrue(words.size > QuillLexicon.english.size + 300)
    }
}
