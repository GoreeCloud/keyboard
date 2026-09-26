package com.goreecloud.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuillGrammarModelTest {
    @Test
    fun repairsCommonHighConfidenceMisspellings() {
        assertEquals("grammar", QuillGrammarModel.boundaryCorrection("grammer", emptyList()))
        assertEquals("receive", QuillGrammarModel.boundaryCorrection("recieve", emptyList()))
        assertEquals("tomorrow", QuillGrammarModel.boundaryCorrection("tommorow", emptyList()))
    }

    @Test
    fun repairsModalOfToHaveOnlyWithSupportingContext() {
        assertEquals("have", QuillGrammarModel.boundaryCorrection("of", listOf("should")))
        assertNull(QuillGrammarModel.boundaryCorrection("of", listOf("piece")))
    }

    @Test
    fun predictsKeyboardContextInsteadOfOnlyGenericFillers() {
        val predictions = QuillGrammarModel.predict(listOf("toolbar", "icon"), limit = 4)
        assertTrue("should" in predictions)
        assertTrue("looks" in predictions)
    }
}
