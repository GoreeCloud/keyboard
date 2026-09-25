package com.goreecloud.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SuggestionEngineTest {
    private val engine = SuggestionEngine()

    @Test
    fun ranksPrefixMatchesByDictionaryFrequencyOrder() {
        val result = engine.suggest(
            prefix = "go",
            dictionary = listOf("good", "goal", "goreecloud", "garden"),
            limit = 2,
        )

        assertEquals(listOf("good", "go"), result)
    }

    @Test
    fun alwaysReturnsBetweenOneAndThreeCandidatesForTypedPrefix() {
        val unmatched = engine.suggest(
            prefix = "zxqv",
            dictionary = listOf("the", "there", "keyboard"),
            limit = 3,
        )
        val crowded = engine.suggest(
            prefix = "a",
            dictionary = listOf("a", "about", "after", "again", "always", "another"),
            limit = 3,
        )

        assertEquals(listOf("zxqv"), unmatched)
        assertEquals(3, crowded.size)
    }

    @Test
    fun exactTypedWordStaysFirst() {
        val result = engine.suggest(
            prefix = "how",
            dictionary = listOf("however", "how", "house"),
            limit = 3,
        )

        assertEquals("how", result.first())
    }

    @Test
    fun addsSingleSubstitutionCorrection() {
        val result = engine.suggest(
            prefix = "hellp",
            dictionary = listOf("hello", "hero", "world"),
            limit = 3,
        )

        assertEquals(listOf("hello", "hellp"), result)
    }

    @Test
    fun recognizesAdjacentTranspositionLocally() {
        val result = engine.suggest(
            prefix = "teh",
            dictionary = listOf("the", "then", "them"),
            limit = 3,
        )

        assertEquals(listOf("the", "teh"), result)
    }

    @Test
    fun conservativeAutocorrectFixesOneEditTypos() {
        assertEquals(
            "shows",
            engine.bestAutocorrection(
                word = "shws",
                dictionary = listOf("shows", "shoes", "show"),
            ),
        )
        assertEquals(
            "the",
            engine.bestAutocorrection(
                word = "teh",
                dictionary = listOf("the", "then", "them"),
            ),
        )
    }

    @Test
    fun autocorrectDoesNotReplaceKnownWords() {
        assertNull(
            engine.bestAutocorrection(
                word = "show",
                dictionary = listOf("show", "shows", "shoes"),
            ),
        )
    }

    @Test
    fun doesNotAutocorrectVeryShortTokens() {
        assertNull(
            engine.bestAutocorrection(
                word = "gi",
                dictionary = listOf("go", "hi", "git"),
            ),
        )
    }

    @Test
    fun supplementaryCharacterInsertionCountsAsOneUnicodeEdit() {
        val deseretSmallLongI = String(Character.toChars(0x10428))
        val result = engine.suggest(
            prefix = "abx",
            dictionary = listOf("ab${deseretSmallLongI}x"),
            limit = 3,
        )

        assertEquals(listOf("ab${deseretSmallLongI}x", "abx"), result)
    }

    @Test
    fun returnsNothingForNonPositiveLimit() {
        assertEquals(emptyList<String>(), engine.suggest("go", listOf("good"), limit = 0))
    }
}
