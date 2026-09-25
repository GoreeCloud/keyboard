package com.goreecloud.keyboard

import org.junit.Assert.assertEquals
import org.junit.Test

class QuillPredictionModelTest {
    @Test
    fun predictsFromRecentLocalPhraseWithoutLearning() {
        assertEquals(
            listOf("feel", "work", "look"),
            QuillPredictionModel.predict(listOf("does", "it")),
        )
        assertEquals(
            listOf("settings", "suggestions", "typing"),
            QuillPredictionModel.predict(listOf("GoreeCloud", "Keyboard")),
        )
    }

    @Test
    fun appliesSafeBoundaryGrammarCorrections() {
        assertEquals("I", QuillPredictionModel.boundaryCorrection("i"))
        assertEquals("I'm", QuillPredictionModel.boundaryCorrection("im"))
        assertEquals("don't", QuillPredictionModel.boundaryCorrection("dont"))
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
