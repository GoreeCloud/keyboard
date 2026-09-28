package com.goreecloud.keyboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardKeyFeedbackPolicyTest {
    @Test
    fun keySoundFollowsPreferenceInOrdinaryEditors() {
        assertTrue(
            KeyboardKeyFeedbackPolicy.keyPressSoundEnabled(
                KeyboardTypingSettings(keyPressSoundEnabled = true),
                sensitiveInput = false,
            ),
        )
        assertFalse(
            KeyboardKeyFeedbackPolicy.keyPressSoundEnabled(
                KeyboardTypingSettings(keyPressSoundEnabled = false),
                sensitiveInput = false,
            ),
        )
    }

    @Test
    fun sensitiveEditorsSuppressKeySoundsEvenWhenPreferenceIsEnabled() {
        assertFalse(
            KeyboardKeyFeedbackPolicy.keyPressSoundEnabled(
                KeyboardTypingSettings(keyPressSoundEnabled = true),
                sensitiveInput = true,
            ),
        )
    }
}
