package com.goreecloud.keyboard

import kotlin.math.roundToInt

/**
 * Bounded native mapping of the GLAZE UI V1.2 foundation, Frosted Neutral material,
 * appearance, geometry, target-size, and interaction-state subset consumed by
 * GoreeCloud Keyboard's first-party Android surface.
 *
 * Current authority is machine version 1.2.0 at exact Stable release revision
 * f285b9145e27e6e7027b075c37299d101945c272. V1.2 preserves inherited V1 semantic,
 * accessibility, component, System Shell, and truth boundaries while promoting the
 * Frosted Neutral + Living Frosted visual system.
 *
 * Governing rule: Neutral glass is the material. Color is an accent.
 *
 * The existing [RadiusMediumDp] property name remains a source-compatible alias for the
 * V1.2 12 dp control role. Optical geometry references remain separate from structural
 * radii and interaction hit targets.
 *
 * Android night mode remains a binary Light/Dark signal in KeyboardView. Deep Dark is
 * defined here from the V1.2 appearance contract but is not inferred from ordinary
 * Android dark mode and is not auto-selected by the current IME runtime.
 *
 * This mapping changes the actual key material consumed by KeyboardView from the older
 * chromatic V1.1 source line to V1.2 neutral frosted surfaces. It does not by itself
 * establish complete rendered/accessibility/device/release acceptance.
 */
internal object GlazeKeyboardTokens {
    const val TargetVersion = "1.2.0"
    const val SourceRevision = "f285b9145e27e6e7027b075c37299d101945c272"

    enum class Appearance { LIGHT, DARK, DEEP_DARK }

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
    const val BottomSafeGapDp = Space1Dp

    // V1.2 optical geometry references remain separate from structural radii.
    const val OpticalMicroDp = 8f
    const val OpticalControlDp = 16f
    const val OpticalContainerDp = 24f
    const val OpticalHeroDp = 32f
    const val OpticalCapsuleDp = 999f

    // V1.2 interaction-state calibration.
    const val UtilityOverlayOpacity = 0.055f
    const val PressedOverlayOpacity = 0.095f
    const val SelectedOverlayOpacity = 0.12f
    const val FocusWidthDp = 3f
    const val IncreasedContrastFocusWidthDp = 4f

    /**
     * V1.2 Frosted Neutral base-glass mapping. Canvas/text roles remain inherited V1
     * structural roles while the interactive key substrate now uses the promoted neutral
     * material values rather than V1.1 chromatic atmosphere.
     */
    val LightPalette = Palette(
        canvasArgb = 0xFFF5F7FA.toInt(),
        surfaceArgb = 0x94FFFFFF.toInt(), // rgba(255,255,255,0.58)
        onSurfaceArgb = 0xFF151A23.toInt(),
        onSurfaceMutedArgb = 0xFF5D6675.toInt(),
        lineArgb = 0x1A505050,
    )

    val DarkPalette = Palette(
        canvasArgb = 0xFF0B0D11.toInt(),
        surfaceArgb = 0x9E19191B.toInt(), // rgba(25,25,27,0.62)
        onSurfaceArgb = 0xFFF5F7FA.toInt(),
        onSurfaceMutedArgb = 0xFFB0B7C3.toInt(),
        lineArgb = 0x1AFFFFFF,
    )

    val DeepDarkPalette = Palette(
        canvasArgb = 0xFF05070A.toInt(),
        surfaceArgb = 0xB80E0E10.toInt(), // rgba(14,14,16,0.72)
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

    fun stateOverlayArgb(appearance: Appearance, opacity: Float): Int {
        val boundedOpacity = opacity.coerceIn(0f, 1f)
        val alpha = (boundedOpacity * 255f).roundToInt()
        return (alpha shl 24) or (palette(appearance).onSurfaceArgb and 0x00FFFFFF)
    }
}
