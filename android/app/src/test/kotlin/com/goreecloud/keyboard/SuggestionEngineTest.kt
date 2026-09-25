package com.goreecloud.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SuggestionEngineTest {
    @Test
    fun returnsBoundedPrefixMatches() {
        val engine = SuggestionEngine()
        val result = engine.suggest(
            prefix = "go",
            dictionary = listOf("goreecloud", "good", "goal", "garden"),
            limit = 2
        )

        assertEquals(listOf("goal", "good"), result)
    }

    @Test
    fun addsSingleSubstitutionCorrection() {
        val engine = SuggestionEngine()
        val result = engine.suggest(
            prefix = "hellp",
            dictionary = listOf("hello", "hero", "world"),
            limit = 3
        )

        assertEquals(listOf("hello"), result)
    }

    @Test
    fun recognizesAdjacentTranspositionLocally() {
        val engine = SuggestionEngine()
        val result = engine.suggest(
            prefix = "teh",
            dictionary = listOf("the", "then", "them"),
            limit = 3
        )

        assertEquals(listOf("the"), result)
    }

    @Test
    fun doesNotRunCorrectionPassForVeryShortInput() {
        val engine = SuggestionEngine()
        val result = engine.suggest(
            prefix = "gi",
            dictionary = listOf("go", "hi", "git"),
            limit = 3
        )

        assertEquals(listOf("git"), result)
    }

    @Test
    fun keepsOneActionableCandidateWhenDictionaryHasNoUsefulMatch() {
        val engine = SuggestionEngine()
        val result = engine.suggest(
            prefix = "cloud",
            dictionary = listOf("clown", "could", "goreecloud"),
            limit = 3
        )

        assertEquals(listOf("cloud"), result)
    }

    @Test
    fun neverExposesMoreThanThreeSuggestions() {
        val engine = SuggestionEngine()
        val result = engine.suggest(
            prefix = "a",
            dictionary = listOf("a", "able", "about", "above", "after", "again"),
            limit = 20,
        )

        assertEquals(3, result.size)
        assertEquals(listOf("a", "able", "about"), result)
    }

    @Test
    fun uniqueOneEditCandidateMayAutocorrect() {
        val engine = SuggestionEngine()

        assertEquals(
            "hello",
            engine.autocorrection("hellp", listOf("hello", "help", "hero")),
        )
        assertEquals(
            "the",
            engine.autocorrection("teh", listOf("the", "then", "them")),
        )
    }

    @Test
    fun exactWordsPrefixCompletionsAndAmbiguousCorrectionsDoNotAutocorrect() {
        val engine = SuggestionEngine()

        assertNull(engine.autocorrection("hello", listOf("hello", "help")))
        assertNull(engine.autocorrection("hell", listOf("hello", "help")))
        assertNull(engine.autocorrection("cot", listOf("cat", "cut")))
    }

    @Test
    fun supplementaryCharacterInsertionCountsAsOneUnicodeEdit() {
        val engine = SuggestionEngine()
        val deseretSmallLongI = String(Character.toChars(0x10428))
        val result = engine.suggest(
            prefix = "abx",
            dictionary = listOf("ab${deseretSmallLongI}x"),
            limit = 3,
        )

        assertEquals(listOf("ab${deseretSmallLongI}x"), result)
    }

    @Test
    fun returnsNothingForNonPositiveLimit() {
        val engine = SuggestionEngine()
        assertEquals(emptyList<String>(), engine.suggest("go", listOf("good"), limit = 0))
    }
}
