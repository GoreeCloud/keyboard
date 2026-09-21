package com.goreecloud.keyboard

/**
 * GLAZE UI V1.6 presentation-only material boundary for GoreeCloud Keyboard.
 *
 * V1.6 makes clarity authoritative over translucency and treats blur as a bounded
 * presentation resource. Keyboard must never inspect editor/content state to manufacture
 * material, semantic, privacy, security, identity, or authorization meaning.
 */
internal object GlazeKeyboardAtmosphere {
    const val DefaultMaterialTintContribution = 0f

    const val ClarityOverridesTranslucency = true
    const val BlurAloneMayProvideContrast = false
    const val UnsupportedBackdropFallsBackSafely = true
    const val BackgroundContentInspectionAllowed = false

    const val TealAsBaseMaterialAllowed = false
    const val GreenAsBaseMaterialAllowed = false
    const val AquaAsBaseMaterialAllowed = false
    const val AmberAsBaseMaterialAllowed = false
    const val BrandColorMayDefineSubstrate = false
    const val SemanticColorMayDefineSubstrate = false

    const val EnvironmentalAuraOptional = true
    const val EnvironmentalAuraMayPassThroughBackdrop = true
    const val EnvironmentalAuraMustRemainOutsideSubstrate = true

    const val EnvironmentalColorMemoryEnabled = false
    const val RemoteColorDerivationAllowed = false
    const val PersistentSampleHistoryAllowed = false
    const val SemanticInferenceAllowed = false
    const val AnimatedAtmosphereEnabled = false
}
