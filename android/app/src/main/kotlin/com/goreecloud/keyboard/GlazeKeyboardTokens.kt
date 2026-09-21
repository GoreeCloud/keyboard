package com.goreecloud.keyboard

import kotlin.math.roundToInt

/**
 * Bounded native source baseline for current Official Stable GLAZE UI V1.6.
 *
 * The accepted shared release source is pinned exactly. Keyboard consumes V1.6 semantic
 * material roles and presentation-only authority boundaries while preserving the existing
 * neutral fallback palette until rendered/accessibility/device qualification is completed.
 *
 * This source baseline does not establish downstream consumer acceptance, production
 * acceptance, or Stable qualification for GoreeCloud Keyboard.
 */
internal object GlazeKeyboardTokens {
    const val TargetVersion = "1.6.0"
    const val AcceptedReleaseSource = "a7180679ea851389e0f3004515f9a25f420e716d"
    const val SourceQualificationAnchor = "c7509c79256b04b0aa67cb9dd0737d7588e0ae4a"
    const val StableRuntimeEntrypoint = "js/glaze-v1.6.0.mjs"
    const val RollbackBaseline = "1.5.1"

    // Compatibility name retained for existing repository checks.
    const val SourceRevision = AcceptedReleaseSource

    const val PresentationOnly = true
    const val PermissionRequestAutomatic = false
    const val AuthorizationInferred = false
    const val ConsequentialExecutionAutomatic = false
    const val DownstreamConsumerAcceptanceAutomatic = false

    enum class Appearance { LIGHT, DARK, DEEP_DARK }

    enum class MaterialRole {
        CANVAS,
        SOLID,
        RAISED,
        FUNCTIONAL_GLASS,
        CLEAR_GLASS,
        OVERLAY,
    }

    data class Palette(
        val canvasArgb: Int,
        val surfaceArgb: Int,
        val onSurfaceArgb: Int,
        val onSurfaceMutedArgb: Int,
        val lineArgb: Int,
    )

    const val Space1Dp = 4f
    const val Space2Dp = 8f
    const val RadiusMediumDp = 12f
    const val GeneralInteractionFloorDp = 48f
    const val TouchAssistanceInteractionFloorDp = 56f
    const val SuggestionStripHeightDp = GeneralInteractionFloorDp

    const val OpticalMicroDp = 8f
    const val OpticalControlDp = 16f
    const val OpticalContainerDp = 24f
    const val OpticalHeroDp = 32f
    const val OpticalCapsuleDp = 999f

    const val PressedOverlayOpacity = 0.095f
    const val SelectedOverlayOpacity = 0.12f
    const val FocusWidthDp = 3f
    const val IncreasedContrastFocusWidthDp = 4f

    // Existing neutral fallback palette retained during the bounded V1.6 source-baseline tranche.
    val LightPalette = Palette(
        canvasArgb = 0xFFF5F7FA.toInt(),
        surfaceArgb = 0x94FFFFFF.toInt(),
        onSurfaceArgb = 0xFF151A23.toInt(),
        onSurfaceMutedArgb = 0xFF5D6675.toInt(),
        lineArgb = 0x1A505050,
    )

    val DarkPalette = Palette(
        canvasArgb = 0xFF0B0D11.toInt(),
        surfaceArgb = 0x9E19191B.toInt(),
        onSurfaceArgb = 0xFFF5F7FA.toInt(),
        onSurfaceMutedArgb = 0xFFB0B7C3.toInt(),
        lineArgb = 0x1AFFFFFF,
    )

    val DeepDarkPalette = Palette(
        canvasArgb = 0xFF05070A.toInt(),
        surfaceArgb = 0xB80E0E10.toInt(),
        onSurfaceArgb = 0xFFF5F7FA.toInt(),
        onSurfaceMutedArgb = 0xFFABB4C2.toInt(),
        lineArgb = 0x17FFFFFF,
    )

    fun interactionFloorDp(touchAssistance: Boolean): Float =
        if (touchAssistance) TouchAssistanceInteractionFloorDp else GeneralInteractionFloorDp

    fun palette(appearance: Appearance): Palette = when (appearance) {
        Appearance.LIGHT -> LightPalette
        Appearance.DARK -> DarkPalette
        Appearance.DEEP_DARK -> DeepDarkPalette
    }

    fun materialArgb(appearance: Appearance, role: MaterialRole): Int {
        val palette = palette(appearance)
        return when (role) {
            MaterialRole.CANVAS,
            MaterialRole.SOLID,
            MaterialRole.OVERLAY -> palette.canvasArgb
            MaterialRole.RAISED,
            MaterialRole.FUNCTIONAL_GLASS,
            MaterialRole.CLEAR_GLASS -> palette.surfaceArgb
        }
    }

    fun stateOverlayArgb(appearance: Appearance, opacity: Float): Int {
        val boundedOpacity = opacity.coerceIn(0f, 1f)
        val alpha = (boundedOpacity * 255f).roundToInt()
        return (alpha shl 24) or (palette(appearance).onSurfaceArgb and 0x00FFFFFF)
    }
}
