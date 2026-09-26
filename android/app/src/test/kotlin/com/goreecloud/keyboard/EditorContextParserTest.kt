package com.goreecloud.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorContextParserTest {
    @Test
    fun extractsOnlyRecentWords() {
        assertEquals(
            listOf("this", "toolbar", "icon"),
            EditorContextParser.wordsBeforeCursor(
                "Earlier text. this toolbar icon",
                limit = 3,
            ),
        )
    }

    @Test
    fun preservesContractionsAsSingleWords() {
        assertEquals(
            listOf("i'm", "testing", "it's"),
            EditorContextParser.wordsBeforeCursor("I'm testing it’s", limit = 3),
        )
    }

    @Test
    fun recognizesCleanSentenceBoundaries() {
        assertTrue(EditorContextParser.isSentenceBoundary(""))
        assertTrue(EditorContextParser.isSentenceBoundary("Done.   "))
        assertTrue(EditorContextParser.isSentenceBoundary("Really?"))
    }
}
