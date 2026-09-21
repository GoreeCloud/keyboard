package com.goreecloud.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GlazeKeyboardTokensTest {
    @Test
    fun currentMappingPinsExactGlazeUiV16StableAuthority() {
        assertEquals("1.6.0", GlazeKeyboardTokens.TargetVersion)
        assertEquals(
            "a7180679ea851389e0f3004515f9a25f420e716d",
            GlazeKeyboardTokens.AcceptedReleaseSource
        )
        assertEquals(
            "c7509c79256b04b0aa67cb9dd0737d7588e0ae4a",
            GlazeKeyboardTokens.SourceQualificationAnchor
        )
        assertEquals("js/glaze-v1.6.0.mjs", GlazeKeyboardTokens.StableRuntimeEntrypoint)
        assertEquals("1.5.1", GlazeKeyboardTokens.RollbackBaseline)
        assertEquals(GlazeKeyboardTokens.AcceptedReleaseSource, GlazeKeyboardTokens.SourceRevision)
    }

    @Test
    fun v16AuthorityBoundaryRemainsPresentationOnlyAndFailClosed() {
        assertTrue(GlazeKeyboardTokens.PresentationOnly)
        assertFalse(GlazeKeyboardTokens.PermissionRequestAutomatic)
        assertFalse(GlazeKeyboardTokens.AuthorizationInferred)
        assertFalse(GlazeKeyboardTokens.ConsequentialExecutionAutomatic)
        assertFalse(GlazeKeyboardTokens.DownstreamConsumerAcceptanceAutomatic)
    }

    @Test
    fun inheritedGeometryAndInteractionFloorsRemainStable() {
        assertEquals(4f, GlazeKeyboardTokens.Space1Dp)
        assertEquals(8f, GlazeKeyboardTokens.Space2Dp)
        assertEquals(12f, GlazeKeyboardTokens.RadiusMediumDp)
        assertEquals(48f, GlazeKeyboardTokens.GeneralInteractionFloorDp)
        assertEquals(56f, GlazeKeyboardTokens.TouchAssistanceInteractionFloorDp)
        assertEquals(
            GlazeKeyboardTokens.GeneralInteractionFloorDp,
            GlazeKeyboardTokens.SuggestionStripHeightDp
        )
    }

    @Test
    fun v16MaterialRolesMapToBoundedNativeSurfaces() {
        val appearance = GlazeKeyboardTokens.Appearance.LIGHT
        assertEquals(
            GlazeKeyboardTokens.LightPalette.canvasArgb,
            GlazeKeyboardTokens.materialArgb(appearance, GlazeKeyboardTokens.MaterialRole.CANVAS)
        )
        assertEquals(
            GlazeKeyboardTokens.LightPalette.canvasArgb,
            GlazeKeyboardTokens.materialArgb(appearance, GlazeKeyboardTokens.MaterialRole.SOLID)
        )
        assertEquals(
            GlazeKeyboardTokens.LightPalette.surfaceArgb,
            GlazeKeyboardTokens.materialArgb(appearance, GlazeKeyboardTokens.MaterialRole.FUNCTIONAL_GLASS)
        )
        assertEquals(
            GlazeKeyboardTokens.LightPalette.surfaceArgb,
            GlazeKeyboardTokens.materialArgb(appearance, GlazeKeyboardTokens.MaterialRole.RAISED)
        )
    }

    @Test
    fun touchAssistanceRaisesInteractionFloorWithoutChangingNormalGeometry() {
        assertEquals(48f, GlazeKeyboardTokens.interactionFloorDp(touchAssistance = false))
        assertEquals(56f, GlazeKeyboardTokens.interactionFloorDp(touchAssistance = true))
    }

    @Test
    fun interactionStateCalibrationRemainsExplicit() {
        assertEquals(0.095f, GlazeKeyboardTokens.PressedOverlayOpacity)
        assertEquals(0.12f, GlazeKeyboardTokens.SelectedOverlayOpacity)
        assertEquals(3f, GlazeKeyboardTokens.FocusWidthDp)
        assertEquals(4f, GlazeKeyboardTokens.IncreasedContrastFocusWidthDp)
        assertEquals(
            0x18151A23,
            GlazeKeyboardTokens.stateOverlayArgb(
                GlazeKeyboardTokens.Appearance.LIGHT,
                GlazeKeyboardTokens.PressedOverlayOpacity,
            )
        )
    }

    @Test
    fun neutralFallbackPalettesRemainDistinctDuringSourceMigration() {
        val light = GlazeKeyboardTokens.palette(GlazeKeyboardTokens.Appearance.LIGHT)
        val dark = GlazeKeyboardTokens.palette(GlazeKeyboardTokens.Appearance.DARK)
        val deepDark = GlazeKeyboardTokens.palette(GlazeKeyboardTokens.Appearance.DEEP_DARK)
        assertNotEquals(light, dark)
        assertNotEquals(dark, deepDark)
    }

    @Test
    fun v16AtmosphereCannotManufactureMeaningOrObservation() {
        assertEquals(0f, GlazeKeyboardAtmosphere.DefaultMaterialTintContribution)
        assertTrue(GlazeKeyboardAtmosphere.ClarityOverridesTranslucency)
        assertFalse(GlazeKeyboardAtmosphere.BlurAloneMayProvideContrast)
        assertTrue(GlazeKeyboardAtmosphere.UnsupportedBackdropFallsBackSafely)
        assertFalse(GlazeKeyboardAtmosphere.BackgroundContentInspectionAllowed)
        assertFalse(GlazeKeyboardAtmosphere.BrandColorMayDefineSubstrate)
        assertFalse(GlazeKeyboardAtmosphere.SemanticColorMayDefineSubstrate)
        assertFalse(GlazeKeyboardAtmosphere.EnvironmentalColorMemoryEnabled)
        assertFalse(GlazeKeyboardAtmosphere.RemoteColorDerivationAllowed)
        assertFalse(GlazeKeyboardAtmosphere.PersistentSampleHistoryAllowed)
        assertFalse(GlazeKeyboardAtmosphere.SemanticInferenceAllowed)
        assertFalse(GlazeKeyboardAtmosphere.AnimatedAtmosphereEnabled)
    }
}
