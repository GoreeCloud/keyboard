package com.goreecloud.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HyphenatedCompoundModelTest {
    @Test
    fun recognizesRequestedCompoundSpellings() {
        assertEquals(
            "up-to-date",
            HyphenatedCompoundModel.rewriteForTail(listOf("this", "is", "up", "to", "date"))?.replacement,
        )
        assertEquals(
            "built-in",
            HyphenatedCompoundModel.rewriteForTail(listOf("a", "built", "in"))?.replacement,
        )
    }

    @Test
    fun includesBroaderCommonCompoundSet() {
        assertEquals(
            "open-source",
            HyphenatedCompoundModel.rewriteForTail(listOf("open", "source"))?.replacement,
        )
        assertEquals(
            "end-to-end",
            HyphenatedCompoundModel.rewriteForTail(listOf("end", "to", "end"))?.replacement,
        )
    }

    @Test
    fun unrelatedTailHasNoRewrite() {
        assertNull(HyphenatedCompoundModel.rewriteForTail(listOf("help", "me")))
    }
}
