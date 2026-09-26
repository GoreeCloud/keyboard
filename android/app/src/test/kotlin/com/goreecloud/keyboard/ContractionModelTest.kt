package com.goreecloud.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContractionModelTest {
    @Test
    fun restoresMissingApostrophe() {
        assertEquals("can't", ContractionModel.correction("cant"))
        assertEquals("don't", ContractionModel.correction("dont"))
        assertEquals("should've", ContractionModel.correction("shouldve"))
    }

    @Test
    fun repairsOneTypingErrorAndRestoresApostrophe() {
        assertEquals("can't", ContractionModel.correction("csnt"))
        assertEquals("doesn't", ContractionModel.correction("doesnt"))
    }

    @Test
    fun offersContractionCompletion() {
        assertEquals("can't", ContractionModel.suggestion("can"))
    }

    @Test
    fun shortAmbiguousNoiseIsRejected() {
        assertNull(ContractionModel.correction("is"))
    }
}
