package com.goreecloud.keyboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardSettingPoliciesTest {
    @Test
    fun numberRowPolicyPreservesGeneralAndSensitiveOverrides() {
        val defaults = KeyboardTypingSettings()
        assertTrue(KeyboardNumberRowPolicy.isVisible(defaults, sensitiveInput = false))
        assertTrue(KeyboardNumberRowPolicy.isVisible(defaults, sensitiveInput = true))

        val ordinaryHidden = defaults.copy(numberRowEnabled = false)
        assertFalse(KeyboardNumberRowPolicy.isVisible(ordinaryHidden, sensitiveInput = false))
        assertTrue(KeyboardNumberRowPolicy.isVisible(ordinaryHidden, sensitiveInput = true))

        val fullyHidden = ordinaryHidden.copy(numberRowInSensitiveFieldsEnabled = false)
        assertFalse(KeyboardNumberRowPolicy.isVisible(fullyHidden, sensitiveInput = false))
        assertFalse(KeyboardNumberRowPolicy.isVisible(fullyHidden, sensitiveInput = true))
    }

    @Test
    fun doubleSpacePeriodRequiresOrdinaryWordCharacterBeforeExistingSpace() {
        assertTrue(DoubleSpacePeriodPolicy.shouldReplacePreviousSpace("hello "))
        assertTrue(DoubleSpacePeriodPolicy.shouldReplacePreviousSpace("test7 "))
        assertFalse(DoubleSpacePeriodPolicy.shouldReplacePreviousSpace(null))
        assertFalse(DoubleSpacePeriodPolicy.shouldReplacePreviousSpace(""))
        assertFalse(DoubleSpacePeriodPolicy.shouldReplacePreviousSpace(" "))
        assertFalse(DoubleSpacePeriodPolicy.shouldReplacePreviousSpace("hello"))
        assertFalse(DoubleSpacePeriodPolicy.shouldReplacePreviousSpace("hello. "))
        assertFalse(DoubleSpacePeriodPolicy.shouldReplacePreviousSpace("hello  "))
        assertFalse(DoubleSpacePeriodPolicy.shouldReplacePreviousSpace("🙂 "))
    }
}
