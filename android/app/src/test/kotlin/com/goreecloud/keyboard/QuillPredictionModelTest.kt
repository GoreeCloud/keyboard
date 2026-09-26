package com.goreecloud.keyboard

import org.junit.Assert.assertEquals
import org.junit.Test

class QuillPredictionModelTest {
    @Test
    fun predictsBeforeTypingWithoutReadingEditorText() {
        assertEquals(
            listOf("I", "I'm", "The"),
            QuillPredictionModel.predict(emptyList()),
        )
    }

    @Test
    fun predictsFromRecentLocalPhraseWithoutLearning() {
        assertEquals(
            listOf("feel", "work", "look"),
            QuillPredictionModel.predict(listOf("does", "it")),
        )
        assertEquals(
            listOf("I", "you", "we"),
            QuillPredictionModel.predict(listOf("How", "can")),
        )
        assertEquals(
            listOf("settings", "suggestions", "typing"),
            QuillPredictionModel.predict(listOf("GoreeCloud", "Keyboard")),
        )
    }

    @Test
    fun commonFunctionWordsReceiveUsefulFallbackContext() {
        assertEquals(
            listOf("the", "you"),
            QuillPredictionModel.predict(listOf("for"), limit = 2),
        )
        assertEquals(
            listOf("be", "have"),
            QuillPredictionModel.predict(listOf("might"), limit = 2),
        )
    }

    @Test
    fun appliesSafeBoundaryGrammarCorrections() {
        assertEquals("I", QuillPredictionModel.boundaryCorrection("i"))
        assertEquals("I'm", QuillPredictionModel.boundaryCorrection("im"))
        assertEquals("don't", QuillPredictionModel.boundaryCorrection("dont"))
        assertEquals("didn't", QuillPredictionModel.boundaryCorrection("didnt"))
        assertEquals("what's", QuillPredictionModel.boundaryCorrection("whats"))
        assertEquals("GoreeCloud", QuillPredictionModel.boundaryCorrection("goreecloud"))
        assertEquals("Wardveil", QuillPredictionModel.boundaryCorrection("wardveil"))
    }

    @Test
    fun predictionCountIsBounded() {
        assertEquals(
            listOf("am", "have"),
            QuillPredictionModel.predict(listOf("i"), limit = 2),
        )
        assertEquals(emptyList<String>(), QuillPredictionModel.predict(listOf("i"), limit = 0))
    }
}
