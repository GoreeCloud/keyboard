package com.goreecloud.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuillGrammarModelTest {
    @Test
    fun keyboardAndToolbarContextProduceUsefulContinuations() {
        assertEquals(
            listOf("to", "a", "better"),
            QuillGrammarModel.predict(listOf("keyboard", "needs"), limit = 3),
        )
        assertEquals(
            listOf("should", "is", "looks"),
            QuillGrammarModel.predict(listOf("toolbar", "icon"), limit = 3),
        )
    }

    @Test
    fun modalOfIsCorrectedToHave() {
        assertEquals(
            "have",
            QuillGrammarModel.boundaryCorrection("of", listOf("should")),
        )
    }

    @Test
    fun repairsHighConfidenceSubjectAuxiliaryAgreement() {
        assertEquals("am", QuillGrammarModel.boundaryCorrection("is", listOf("I")))
        assertEquals("are", QuillGrammarModel.boundaryCorrection("is", listOf("they")))
        assertEquals("is", QuillGrammarModel.boundaryCorrection("are", listOf("she")))
        assertEquals("have", QuillGrammarModel.boundaryCorrection("has", listOf("you")))
        assertEquals("has", QuillGrammarModel.boundaryCorrection("have", listOf("it")))
    }

    @Test
    fun commonCollapsedPhraseCorrectionCanReturnMultipleWords() {
        assertEquals(
            "a lot",
            QuillGrammarModel.boundaryCorrection("alot", emptyList()),
        )
    }

    @Test
    fun starterPredictionsAreMeaningfulBeforeTyping() {
        val result = QuillGrammarModel.predict(emptyList(), limit = 3)
        assertEquals(3, result.size)
        assertTrue("I" in result)
    }
}
