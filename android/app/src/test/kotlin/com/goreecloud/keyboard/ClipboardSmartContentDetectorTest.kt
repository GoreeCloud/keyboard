package com.goreecloud.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipboardSmartContentDetectorTest {
    @Test
    fun detectsReusableClipboardFragmentsLocally() {
        val input = """
            Contact admin@goreecloud.com or (205) 555-0199.
            Visit https://goreecloud.com/docs.
            Meet at 123 Cloud Avenue on September 26, 2026 at 3:30 PM.
        """.trimIndent()

        val detected = ClipboardSmartContentDetector.detect(input)

        assertTrue(detected.any {
            it.type == ClipboardSmartContentType.EMAIL && it.value == "admin@goreecloud.com"
        })
        assertTrue(detected.any {
            it.type == ClipboardSmartContentType.PHONE && it.value.contains("205")
        })
        assertTrue(detected.any {
            it.type == ClipboardSmartContentType.WEB_LINK &&
                it.value == "https://goreecloud.com/docs"
        })
        assertTrue(detected.any {
            it.type == ClipboardSmartContentType.ADDRESS &&
                it.value.equals("123 Cloud Avenue", ignoreCase = true)
        })
        assertTrue(detected.any {
            it.type == ClipboardSmartContentType.DATE &&
                it.value.contains("September 26")
        })
        assertTrue(detected.any {
            it.type == ClipboardSmartContentType.TIME &&
                it.value.equals("3:30 PM", ignoreCase = true)
        })
    }

    @Test
    fun overlappingAndRepeatedValuesAreNotDuplicated() {
        val detected = ClipboardSmartContentDetector.detect(
            "Email admin@goreecloud.com then admin@goreecloud.com",
        )
        assertEquals(
            1,
            detected.count { it.type == ClipboardSmartContentType.EMAIL },
        )
    }
}
