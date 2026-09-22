package com.goreecloud.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GlazeKeyboardV16PresentationPolicyTest {
    @Test
    fun pinsCurrentStableAuthorityWithoutRelabelingInheritedOptics() {
        assertEquals("1.6.0", GlazeKeyboardV16PresentationPolicy.StableVersion)
        assertEquals(
            "a7180679ea851389e0f3004515f9a25f420e716d",
            GlazeKeyboardV16PresentationPolicy.StableSourceRevision,
        )
        assertEquals("1.2.0", GlazeKeyboardV16PresentationPolicy.InheritedOpticalVersion)
        assertEquals(
            "f285b9145e27e6e7027b075c37299d101945c272",
            GlazeKeyboardV16PresentationPolicy.InheritedOpticalSourceRevision,
        )
    }

    @Test
    fun androidSignalsMapOnlyPresentationState() {
        val context = GlazeKeyboardV16AndroidPresentationContext.resolve(
            GlazeKeyboardV16PresentationSignals(
                fontScale = 1.65f,
                animationsEnabled = false,
                touchExplorationEnabled = true,
            ),
        )

        assertTrue(context.reducedMotion)
        assertTrue(context.largeText)
        assertTrue(context.extraLargeText)
        assertTrue(context.touchAssistance)
        assertTrue(context.screenReaderOptimized)
        assertEquals(56f, GlazeKeyboardV16PresentationPolicy.interactionFloorDp(context))
        assertEquals(308f, GlazeKeyboardV16PresentationPolicy.preferredImeHeightDp(context))
        assertEquals(
            GlazeKeyboardV16MotionMode.MINIMAL,
            GlazeKeyboardV16PresentationPolicy.motionMode(context),
        )
    }

    @Test
    fun touchAssistanceCompressesGapsBeforeShrinkingOrdinaryRows() {
        val context = GlazeKeyboardV16PresentationContext(touchAssistance = true)

        assertEquals(
            2.4f,
            GlazeKeyboardV16PresentationPolicy.verticalGapDp(
                totalHeightDp = 300f,
                rowCount = 4,
                context = context,
            ),
            0.001f,
        )
        assertEquals(
            GlazeKeyboardTokens.Space1Dp,
            GlazeKeyboardV16PresentationPolicy.verticalGapDp(
                totalHeightDp = 308f,
                rowCount = 4,
                context = context,
            ),
            0.001f,
        )
    }

    @Test
    fun neutralSignalsPreserveOrdinaryTargetAndMotion() {
        val context = GlazeKeyboardV16AndroidPresentationContext.resolve(
            GlazeKeyboardV16PresentationSignals(
                fontScale = 1.0f,
                animationsEnabled = true,
                touchExplorationEnabled = false,
            ),
        )

        assertFalse(context.reducedMotion)
        assertFalse(context.largeText)
        assertFalse(context.touchAssistance)
        assertEquals(48f, GlazeKeyboardV16PresentationPolicy.interactionFloorDp(context))
        assertEquals(300f, GlazeKeyboardV16PresentationPolicy.preferredImeHeightDp(context))
        assertEquals(
            GlazeKeyboardV16MotionMode.STANDARD,
            GlazeKeyboardV16PresentationPolicy.motionMode(context),
        )
    }
}
