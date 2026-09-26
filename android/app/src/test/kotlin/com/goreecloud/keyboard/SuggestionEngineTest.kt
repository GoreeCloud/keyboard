package com.goreecloud.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

        assertEquals(listOf("good", "goal"), result)
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
    fun highFrequencyNeighborKeySlipAutocorrectsBjtToBut() {
        assertEquals(
            "but",
            engine.bestAutocorrection(
                word = "bjt",
                dictionary = QuillLexicon.expandedEnglish,
            ),
        )
    }

    @Test
    fun closeRankNeighborKeyAlternativesRemainSuggestionOnly() {
        assertNull(
            engine.bestAutocorrection(
                word = "bjt",
                dictionary = listOf("but", "bit", "bat"),
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
    fun builtInDictionaryKnowsModernKeyboardVocabulary() {
        val result = engine.suggest(
            prefix = "ico",
            dictionary = QuillLexicon.expandedEnglish,
            limit = 3,
        )

        assertEquals("icon", result.first())
    }

    @Test
    fun contextCanPromoteARelevantCompletion() {
        val result = engine.suggest(
            prefix = "set",
            dictionary = listOf("set", "setting", "settings", "settle"),
            contextualPredictions = listOf("settings"),
            limit = 3,
        )

        assertEquals("set", result.first())
        assertEquals("settings", result[1])
    }

    @Test
    fun commonIconPrefixProducesBuiltInIconSuggestion() {
        val result = engine.suggest(
            prefix = "ico",
            dictionary = QuillLexicon.expandedEnglish,
            limit = 3,
        )

        assertEquals("icon", result.first())
    }

    @Test
    fun transientContextCanPromoteARelevantCompletion() {
        val result = engine.suggest(
            prefix = "se",
            dictionary = listOf("second", "send", "settings", "see"),
            contextualPredictions = listOf("settings"),
            limit = 3,
        )

        assertEquals("settings", result.first())
    }

    @Test
    fun fillsSuggestionStripWithRealCandidatesBeforeRawUnknownPrefix() {
        val result = engine.suggest(
            prefix = "predic",
            dictionary = listOf("prediction", "predict", "predictive", "predicate"),
            limit = 3,
        )

        assertEquals(3, result.size)
        assertEquals("predict", result.first())
        assertTrue("prediction" in result)
        assertTrue("predic" !in result)
    }

    @Test
    fun longerUnknownTokensCanReceiveUsefulSuggestionOnlyCorrections() {
        val result = engine.suggest(
            prefix = "sugestions",
            dictionary = listOf("suggestions", "suggestion", "settings"),
            limit = 3,
        )

        assertEquals("suggestions", result.first())
    }

    @Test
    fun returnsNothingForNonPositiveLimit() {
        assertEquals(emptyList<String>(), engine.suggest("go", listOf("good"), limit = 0))
    }
    @Test
    fun commonMissingLetterTypoOffersLaggingBeforeUnrelatedWords() {
        val result = engine.suggest(
            prefix = "laging",
            dictionary = listOf("larding", "lasting", "landing", "lagging"),
            limit = 3,
        )

        assertEquals("lagging", result.first())
    }

    @Test
    fun largeDictionaryCacheDoesNotChangeResultsAcrossRepeatedQueries() {
        val dictionary = buildList {
            addAll(listOf("lag", "lagging", "jump", "hill"))
            repeat(5_000) { index -> add("word$index") }
        }

        assertEquals(
            engine.suggest("laging", dictionary, limit = 3),
            engine.suggest("laging", dictionary, limit = 3),
        )
    }

}
