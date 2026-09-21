#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DOC = ROOT / "docs/glaze-motion-evaluation.md"
ADOPTION = ROOT / "docs/glaze-ui-adoption.md"
PLATFORM = ROOT / "goreecloud.platform.yaml"
TEST = ROOT / "android/app/src/androidTest/kotlin/com/goreecloud/keyboard/GlazeMotionExperimentalKeyboardRuntimeTest.kt"
MAIN = ROOT / "android/app/src/main"
KEYBOARD_VIEW = MAIN / "kotlin/com/goreecloud/keyboard/KeyboardView.kt"
TOKENS = MAIN / "kotlin/com/goreecloud/keyboard/GlazeKeyboardTokens.kt"
OPTICS = MAIN / "kotlin/com/goreecloud/keyboard/GlazeKeyboardOptics.kt"
CAPABILITY = MAIN / "kotlin/com/goreecloud/keyboard/GlazeKeyboardCapabilityV15.kt"
ATMOSPHERE = MAIN / "kotlin/com/goreecloud/keyboard/GlazeKeyboardAtmosphere.kt"
OPTICS_TEST = ROOT / "android/app/src/test/kotlin/com/goreecloud/keyboard/GlazeKeyboardOpticsTest.kt"
TOKENS_TEST = ROOT / "android/app/src/test/kotlin/com/goreecloud/keyboard/GlazeKeyboardTokensTest.kt"
CAPABILITY_TEST = ROOT / "android/app/src/test/kotlin/com/goreecloud/keyboard/GlazeKeyboardCapabilityV15Test.kt"

MOTION_REFERENCE_REVISION = "b386c793c047e2f5d5d92125732f142e7fdf32dc"
MOTION_GLAZE_VERSION = "1.4.0"
MOTION_GLAZE_REVISION = "84cb3db4884042f0fa25ed6d475a127fb110f596"
GLAZE_VERSION = "1.5.0"
GLAZE_SOURCE_REVISION = "b7fa8164bfdeaa1dc0acb21b770e7601120da04e"
GLAZE_REVIEWED_ANCHOR = "ee1032a0822ab8e103f8afe48e5c1859fde65cc9"
GLAZE_REQUIRED_VERSION = "1.6.0"
GLAZE_REQUIRED_SOURCE_REVISION = "a7180679ea851389e0f3004515f9a25f420e716d"
OPTICAL_BASELINE_VERSION = "1.4.1"
OPTICAL_BASELINE_REVISION = "4fab9da0fad2e5c974e0e66ec88632c61745751c"
MARKER = "GlazeMotionExperimental"


def fail(message: str) -> None:
    raise SystemExit("Keyboard GLAZE UI V1.5 implementation / V1.6 target / Motion boundary failed: " + message)


def require_all(label: str, text: str, markers: tuple[str, ...]) -> None:
    for marker in markers:
        if marker not in text:
            fail(f"{label} missing `{marker}`")


def main() -> None:
    for path in (
        DOC,
        ADOPTION,
        PLATFORM,
        TEST,
        KEYBOARD_VIEW,
        TOKENS,
        OPTICS,
        CAPABILITY,
        ATMOSPHERE,
        OPTICS_TEST,
        TOKENS_TEST,
        CAPABILITY_TEST,
    ):
        if not path.is_file():
            fail(f"missing required evidence: {path.relative_to(ROOT)}")

    doc_text = DOC.read_text(encoding="utf-8")
    adoption_text = ADOPTION.read_text(encoding="utf-8")
    platform_text = PLATFORM.read_text(encoding="utf-8")
    test_text = TEST.read_text(encoding="utf-8")
    view_text = KEYBOARD_VIEW.read_text(encoding="utf-8")
    token_text = TOKENS.read_text(encoding="utf-8")
    optics_text = OPTICS.read_text(encoding="utf-8")
    capability_text = CAPABILITY.read_text(encoding="utf-8")
    atmosphere_text = ATMOSPHERE.read_text(encoding="utf-8")
    optics_test_text = OPTICS_TEST.read_text(encoding="utf-8")
    tokens_test_text = TOKENS_TEST.read_text(encoding="utf-8")
    capability_test_text = CAPABILITY_TEST.read_text(encoding="utf-8")

    # The historical Experimental Motion evaluation keeps its original reviewed
    # Glaze context as provenance. It is not current Stable design-system authority.
    require_all(
        "Motion boundary",
        doc_text,
        (
            "Lifecycle: **Experimental 0.5**",
            f"Reviewed canonical revision: `{MOTION_REFERENCE_REVISION}`",
            "Runtime compatibility baseline: **0.4.0**",
            "Evaluation mode: **native Android interaction mapping, test-only**",
            "Production dependency: **no**",
            f"GLAZE UI V1.4 / `{MOTION_GLAZE_VERSION}`",
            f"`{MOTION_GLAZE_REVISION}`",
            "Motion remains separately Experimental",
            "insufficient for promotion by itself",
        ),
    )

    require_all(
        "Glaze adoption record",
        adoption_text,
        (
            "# GLAZE UI V1.5 Implemented Mapping / V1.6 Migration Requirement — GoreeCloud Keyboard",
            "Status: **Migration in progress / Development**",
            f"Current Stable target: **GLAZE UI V1.6 (`{GLAZE_REQUIRED_VERSION}`)**",
            f"Exact current Stable release source authority: `{GLAZE_REQUIRED_SOURCE_REVISION}`",
            f"Implemented Development mapping: **GLAZE UI V1.5 (`{GLAZE_VERSION}`)** at `{GLAZE_SOURCE_REVISION}`",
            f"Reviewed V1.5 implementation anchor: `{GLAZE_REVIEWED_ANCHOR}`",
            f"Inherited optical/material baseline: **GLAZE UI V1.4.1 (`{OPTICAL_BASELINE_VERSION}`)** at `{OPTICAL_BASELINE_REVISION}`",
            "Production eligible on the Glaze UI gate: **no**",
            "Neutral glass is the material. Color is an accent.",
            "Environmental Color Memory influence remains intentionally **0%**",
            "Typed text, composing text, surrounding text, suggestion content, clipboard state, application identity",
            "V1.5 shared-scope and V1.5.1 follow-up boundary",
            "Source/build/emulator success remains Development evidence only",
        ),
    )

    require_all(
        "V1.4.1 optical token baseline",
        token_text,
        (
            f'const val TargetVersion = "{OPTICAL_BASELINE_VERSION}"',
            f'const val SourceRevision = "{OPTICAL_BASELINE_REVISION}"',
            "enum class Appearance { LIGHT, DARK, DEEP_DARK }",
            "const val GeneralInteractionFloorDp = 48f",
            "const val TouchAssistanceInteractionFloorDp = 56f",
            "const val PressedOverlayOpacity = 0.095f",
            "const val FocusWidthDp = 3f",
            "const val IncreasedContrastFocusWidthDp = 4f",
            "Appearance.DEEP_DARK -> DeepDarkPalette",
            "fun stateOverlayArgb(",
            "Typed/editor content, suggestions",
        ),
    )

    require_all(
        "V1.4.1 Keyboard optical policy",
        optics_text,
        (
            f'const val TargetVersion = "{OPTICAL_BASELINE_VERSION}"',
            f'const val StableSourceRevision = "{OPTICAL_BASELINE_REVISION}"',
            "const val MaxEnvironmentalColorMemoryInfluence = 0f",
            "const val EditorContentMayDriveOptics = false",
            "const val ClipboardMayDriveOptics = false",
            "const val SuggestionContentMayDriveOptics = false",
            "const val AppIdentityMayDriveOptics = false",
            "const val RemoteContextAllowed = false",
            "const val TelemetryRequired = false",
            "SOLID_ACCESSIBLE",
            "semanticProtection = 1f",
            "decorativeTintAllowed = false",
            "environmentalColorMemoryInfluence = 0f",
        ),
    )

    require_all(
        "V1.5 capability presentation",
        capability_text,
        (
            f'const val TargetVersion = "{GLAZE_VERSION}"',
            f'const val StableSourceRevision = "{GLAZE_SOURCE_REVISION}"',
            f'const val ReviewedImplementationAnchor = "{GLAZE_REVIEWED_ANCHOR}"',
            f'const val OpticalBaselineVersion = "{OPTICAL_BASELINE_VERSION}"',
            f'const val OpticalBaselineRevision = "{OPTICAL_BASELINE_REVISION}"',
            "const val TypedContentMayDriveCapabilityPresentation = false",
            "const val ComposingContentMayDriveCapabilityPresentation = false",
            "const val SurroundingTextMayDriveCapabilityPresentation = false",
            "const val SuggestionContentMayDriveCapabilityPresentation = false",
            "const val ClipboardMayDriveCapabilityPresentation = false",
            "const val AppIdentityMayDriveCapabilityPresentation = false",
            "const val RemoteContextAllowed = false",
            "const val TelemetryRequired = false",
            "CapabilityState.CONFLICT",
            "automaticExecutionAllowed: Boolean = false",
            "authorizationInferred: Boolean = false",
            "providerPrecedenceInferred: Boolean = false",
        ),
    )

    require_all(
        "inherited atmosphere/material boundary",
        atmosphere_text,
        (
            "Neutral glass is the material. Color is an accent.",
            "const val DefaultMaterialTintContribution = 0f",
            "const val TealAsBaseMaterialAllowed = false",
            "const val GreenAsBaseMaterialAllowed = false",
            "const val AquaAsBaseMaterialAllowed = false",
            "const val AmberAsBaseMaterialAllowed = false",
            "const val BrandColorMayDefineSubstrate = false",
            "const val SemanticColorMayDefineSubstrate = false",
            "const val EnvironmentalAuraOptional = false",
            "const val EnvironmentalAuraMayPassThroughBackdrop = false",
            "const val EnvironmentalColorMemoryEnabled = false",
            "const val EnvironmentalColorMemoryMaxInfluence = 0f",
            "const val EditorContentSamplingAllowed = false",
            "const val SuggestionContentSamplingAllowed = false",
            "const val ClipboardSamplingAllowed = false",
            "const val ApplicationIdentitySamplingAllowed = false",
            "const val RemoteColorDerivationAllowed = false",
            "const val PersistentSampleHistoryAllowed = false",
            "const val SemanticInferenceAllowed = false",
            "const val AnimatedAtmosphereEnabled = false",
        ),
    )

    require_all(
        "optical tests",
        optics_test_text,
        (
            "ordinaryKeyboardOpticsRemainNeutralAndSemanticFirst",
            "increasedContrastRaisesFrostWithoutEnablingDecoration",
            "reducedTransparencyFailsClosedToSolidAccessible",
            "forcedColorsFailsClosedToSolidAccessible",
            "sensitiveInputSourcesCanNeverDriveOptics",
        ),
    )

    require_all(
        "token tests",
        tokens_test_text,
        (
            "currentOpticalMappingPinsExactGlazeUiV141StableAuthority",
            "inheritedAtmosphereCannotTintSubstrateOrEnableSensitiveObservation",
            f'assertEquals("{OPTICAL_BASELINE_VERSION}", GlazeKeyboardTokens.TargetVersion)',
            f'"{OPTICAL_BASELINE_REVISION}"',
            f'assertEquals("{GLAZE_VERSION}", GlazeKeyboardCapabilityV15.TargetVersion)',
        ),
    )

    require_all(
        "capability tests",
        capability_test_text,
        (
            "exactStableAuthorityAndInheritedOpticalBaselineArePinned",
            "sensitiveImeStateCannotDriveCapabilityPresentation",
            "availableCapabilityDoesNotAuthorizeAutomaticExecution",
            "missingCapabilityFailsClosed",
            "duplicateCapabilityOwnershipFailsClosedWithoutInventedPrecedence",
            "privacyRestrictedCapabilityRemainsDisabled",
        ),
    )

    require_all(
        "Platform Contract v0.4",
        platform_text,
        (
            'schema_version: "0.4"',
            "  id: goreecloud-keyboard",
            "  repository: GoreeCloud/keyboard",
            f'  glaze_ui:\n    result: applicable-migration-required\n    version: "{GLAZE_REQUIRED_VERSION}"',
            "  policy:",
            "  observability:",
            '  platform_contract: "0.4"',
            f'  glaze_ui_required: "{GLAZE_REQUIRED_VERSION}"',
            "goreecloud-platform-contract==0.4",
            f"glaze-ui=={GLAZE_REQUIRED_VERSION}",
            "GlazeKeyboardCapabilityV15.kt",
            "GlazeKeyboardCapabilityV15Test.kt",
            "GoreeCloud Sync change tracking, authorized replication, conflict reconciliation",
            "conformance:\n  status: nonconformant",
        ),
    )
    if "\n  sync:" in platform_text:
        fail("GoreeCloud Sync must remain separately governed and must not appear as an Integral Platform System")

    require_all(
        "representative Keyboard runtime",
        view_text,
        (
            "class KeyboardView",
            "Configuration.UI_MODE_NIGHT_MASK",
            "GlazeKeyboardTokens.Appearance.DARK",
            "GlazeKeyboardTokens.Appearance.LIGHT",
            "GlazeKeyboardTokens.palette(appearance)",
            "private val pressedKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG)",
            "private var pressedKeyBounds: RectF? = null",
            "GlazeKeyboardTokens.stateOverlayArgb(",
            "GlazeKeyboardTokens.PressedOverlayOpacity",
            "override fun onTouchEvent(event: MotionEvent)",
            "performClick()",
        ),
    )

    if "Appearance.DEEP_DARK" in view_text:
        fail("KeyboardView must not infer or auto-select Deep Dark until a governed runtime policy exists")
    if "GlazeKeyboardAtmosphere" in view_text or "GlazeKeyboardOptics" in view_text or "GlazeKeyboardCapabilityV15" in view_text:
        fail("KeyboardView must not gain implicit optical/capability/environment observation in this migration slice")

    require_all(
        "native test-only Motion evidence",
        test_text,
        (
            f'const val REFERENCE_REVISION = "{MOTION_REFERENCE_REVISION}"',
            'const val VERSION = "0.5.0"',
            'const val RUNTIME_BASELINE = "0.4.0"',
            "Settings.Global.ANIMATOR_DURATION_SCALE",
            "allowsOptionalSettling(",
            "KeyboardView(context)",
            "dispatchTouchEvent(event)",
        ),
    )

    # Sensitive-input presentation policy stays non-collecting. Keep direct
    # collection/network primitives out of optical, capability, and atmosphere files.
    presentation_authority = optics_text + "\n" + capability_text + "\n" + atmosphere_text
    for forbidden in (
        "InputConnection",
        "ClipboardManager",
        "EditorInfo",
        "getTextBeforeCursor",
        "getTextAfterCursor",
        "getSelectedText",
        "HttpURLConnection",
        "URLConnection",
        "Socket(",
        "WallpaperManager",
        "Camera",
    ):
        if forbidden in presentation_authority:
            fail(f"sensitive Glaze boundary contains forbidden observation/network primitive `{forbidden}`")

    production_hits = []
    for path in MAIN.rglob("*.kt"):
        if MARKER in path.read_text(encoding="utf-8"):
            production_hits.append(str(path.relative_to(ROOT)))
    if production_hits:
        fail(
            "Experimental Motion escaped test quarantine into production source: "
            + ", ".join(production_hits)
        )

    # Historical Motion evidence may retain its original V1.4 context. Active
    # current-authority records must not retain superseded target declarations.
    active_records = adoption_text + "\n" + platform_text + "\n" + token_text + "\n" + optics_text + "\n" + capability_text
    for stale in (
        "Current Stable target: **GLAZE UI V1.4 (`1.4.0`)**",
        "Current Stable target: **GLAZE UI V1.5 (`1.5.0`)**",
        '  glaze_ui_required: "1.4.0"',
        "glaze-ui==1.4.0",
        '  glaze_ui:\n    result: applicable-migration-required\n    version: "1.4.0"',
        'schema_version: "0.2"',
        'schema_version: "0.3"',
        '  platform_contract: "0.2"',
        '  platform_contract: "0.3"',
        "goreecloud-platform-contract==0.2",
        "goreecloud-platform-contract==0.3",
        "  repository: GoreeCloud/goreecloud-keyboard",
        "known immutable import-closure defect",
        "Glaze UI 2.2.0 Stable is the production design-system authority.",
        "stable_eligible: true",
    ):
        if stale in active_records:
            fail(f"active evidence retains stale/superseded Glaze authority `{stale}`")

    print(
        "Keyboard GLAZE UI boundary passed: "
        f"implemented V1.5 mapping {GLAZE_VERSION} at {GLAZE_SOURCE_REVISION}; required target "
        f"{GLAZE_REQUIRED_VERSION} at {GLAZE_REQUIRED_SOURCE_REVISION}; inherited optical baseline "
        f"{OPTICAL_BASELINE_VERSION} at {OPTICAL_BASELINE_REVISION}; Platform Contract 0.4 remains "
        "migration-required/nonconformant; GoreeCloud Sync remains separately blocked; "
        "Android runtime remains Light/Dark only; sensitive content cannot drive Glaze; "
        "Environmental Color Memory remains 0%; Experimental Motion remains quarantined; "
        "application acceptance stays separate."
    )


if __name__ == "__main__":
    main()
