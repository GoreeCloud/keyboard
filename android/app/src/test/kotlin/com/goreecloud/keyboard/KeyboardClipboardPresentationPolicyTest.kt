package com.goreecloud.keyboard

import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardClipboardPresentationPolicyTest {
    @Test
    fun clipboardInteractionTargetMeetsGlazeFloor() {
        assertTrue(KeyboardClipboardPresentationPolicy.INTERACTION_TARGET_DP >= 48)
    }
}
