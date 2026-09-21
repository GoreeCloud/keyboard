package com.goreecloud.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GlazeKeyboardV16PresentationPolicyTest {
    @Test
    fun exactStableV16AuthorityIsPinned() {
        assertEquals("1.6.0", GlazeKeyboardV16PresentationPolicy.StableVersion)
        assertEquals(
            "a7180679ea851389e0f3004515f9a25f420e716d",
            GlazeKeyboardV16PresentationPolicy.StableSourceRevision,
        )
    }

    @Test
    fun disabledAndroidAnimationsResolveMinimalMotionOnly() {
        val resolved = GlazeKeyboardV16PresentationPolicy.resolve(
            GlazeKeyboardV16PresentationPolicy.Signals(
                fontScale = 1.0f,
                animationsEnabled = false,
                touchExplorationEnabled = false,
            ),
        )

        assertEquals(GlazeKeyboardV16PresentationPolicy.MotionMode.MINIMAL, resolved.motionMode)
        assertFalse(resolved.touchAssistance)
        assertEquals(48f, resolved.interactionFloorDp)
    }

    @Test
    fun touchExplorationUsesTouchAssistanceTargetWithoutInventingOtherAuthority() {
        val resolved = GlazeKeyboardV16PresentationPolicy.resolve(
            GlazeKeyboardV16PresentationPolicy.Signals(
                fontScale = 1.0f,
                animationsEnabled = true,
                touchExplorationEnabled = true,
            ),
        )

        assertTrue(resolved.touchAssistance)
        assertTrue(resolved.screenReaderOptimized)
        assertEquals(56f, resolved.interactionFloorDp)
        assertEquals(56f, resolved.suggestionStripHeightDp)
    }

    @Test
    fun AndroidFontScaleMapsLargeAndExtraLargeTextThresholds() {
        val large = GlazeKeyboardV16PresentationPolicy.resolve(
            GlazeKeyboardV16PresentationPolicy.Signals(
                fontScale = 1.35f,
                animationsEnabled = true,
                touchExplorationEnabled = false,
            ),
        )
        val extraLarge = GlazeKeyboardV16PresentationPolicy.resolve(
            GlazeKeyboardV16PresentationPolicy.Signals(
                fontScale = 1.65f,
                animationsEnabled = true,
                touchExplorationEnabled = false,
            ),
        )

        assertTrue(large.largeText)
        assertFalse(large.extraLargeText)
        assertTrue(extraLarge.largeText)
        assertTrue(extraLarge.extraLargeText)
    }
}
