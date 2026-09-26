package com.goreecloud.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RunTogetherWordResolverTest {
    private val resolver = RunTogetherWordResolver()

    @Test
    fun repairsTwoCommonWordsWithMissingSpace() {
        assertEquals(
            "help me",
            resolver.resolve(
                token = "helpme",
                dictionary = listOf("the", "you", "me", "help", "understand"),
            ),
        )
    }

    @Test
    fun repairsSeveralBunchedCommonWordsLocally() {
        assertEquals(
            "help me understand you",
            resolver.resolve(
                token = "helpmeunderstandyou",
                dictionary = listOf("the", "you", "me", "help", "understand", "stand"),
            ),
        )
    }

    @Test
    fun keepsKnownWholeWordIntact() {
        assertNull(
            resolver.resolve(
                token = "oneplus",
                dictionary = listOf("one", "plus", "OnePlus"),
            ),
        )
        assertNull(
            resolver.resolve(
                token = "understand",
                dictionary = listOf("under", "stand", "understand"),
            ),
        )
    }

    @Test
    fun exactLongBrandWordsCanBeSeparatedWithoutFrequencyPrivilege() {
        val dictionary = buildList {
            repeat(7_000) { index -> add("common$index") }
            add("Samsung")
            add("Ryzen")
        }

        assertEquals(
            "Samsung Ryzen",
            resolver.resolve(
                token = "samsungryzen",
                dictionary = dictionary,
            ),
        )
    }

    @Test
    fun repairsBunchedWordsWithOneNeighborTypingError() {
        assertEquals(
            "help me",
            resolver.resolveWithSingleEdit(
                token = "helpcme",
                dictionary = listOf("the", "you", "me", "help", "understand"),
            ),
        )
    }

    @Test
    fun oneEditRecoveryStillRejectsArbitraryUnknownText() {
        assertNull(
            resolver.resolveWithSingleEdit(
                token = "zxqvplmokn",
                dictionary = listOf("the", "you", "me", "help", "keyboard"),
            ),
        )
    }

    @Test
    fun arbitraryUnknownTextIsNotInventedIntoWords() {
        assertNull(
            resolver.resolve(
                token = "zxqvplmokn",
                dictionary = listOf("the", "you", "me", "help", "keyboard"),
            ),
        )
    }
}
