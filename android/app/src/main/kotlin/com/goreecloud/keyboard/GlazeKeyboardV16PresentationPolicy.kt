package com.goreecloud.keyboard

/**
 * Bounded repository-local consumer mapping for current Stable GLAZE UI V1.6 / 1.6.0.
 *
 * This policy consumes only Android-owned presentation signals. It does not inspect editor
 * contents, typed text, suggestions, clipboard data, application identity, network state, or
 * any GoreeCloud privacy/security/identity authority.
 *
 * The legacy V1.2 material palette remains a separately identified implementation baseline
 * while Keyboard incrementally adopts V1.6 accessibility, motion, target-size, and continuity
 * semantics. This file does not establish complete V1.6 consumer conformance.
 */
internal object GlazeKeyboardV16PresentationPolicy {
    const val StableVersion = "1.6.0"
    const val StableSourceRevision = "a7180679ea851389e0f3004515f9a25f420e716d"
    const val LargeTextFontScale = 1.30f
    const val ExtraLargeTextFontScale = 1.60f

    enum class MotionMode {
        STANDARD,
        MINIMAL,
    }

    data class Signals(
        val fontScale: Float,
        val animationsEnabled: Boolean,
        val touchExplorationEnabled: Boolean,
    )

    data class Resolved(
        val motionMode: MotionMode,
        val largeText: Boolean,
        val extraLargeText: Boolean,
        val touchAssistance: Boolean,
        val screenReaderOptimized: Boolean,
        val interactionFloorDp: Float,
        val suggestionStripHeightDp: Float,
    )

    fun resolve(signals: Signals): Resolved {
        val touchAssistance = signals.touchExplorationEnabled
        return Resolved(
            motionMode = if (signals.animationsEnabled) MotionMode.STANDARD else MotionMode.MINIMAL,
            largeText = signals.fontScale >= LargeTextFontScale,
            extraLargeText = signals.fontScale >= ExtraLargeTextFontScale,
            touchAssistance = touchAssistance,
            screenReaderOptimized = touchAssistance,
            interactionFloorDp = GlazeKeyboardTokens.interactionFloorDp(touchAssistance),
            suggestionStripHeightDp = GlazeKeyboardTokens.interactionFloorDp(touchAssistance),
        )
    }
}
