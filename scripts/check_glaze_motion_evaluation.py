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
ATMOSPHERE = MAIN / "kotlin/com/goreecloud/keyboard/GlazeKeyboardAtmosphere.kt"
MOTION_REFERENCE_REVISION = "b386c793c047e2f5d5d92125732f142e7fdf32dc"
SOURCE_GLAZE_VERSION = "1.2.0"
GOVERNED_GLAZE_BASELINE = "1.6.0"
GLAZE_SOURCE_REVISION = "f285b9145e27e6e7027b075c37299d101945c272"\nGOVERNED_GLAZE_SOURCE_REVISION = "a7180679ea851389e0f3004515f9a25f420e716d"
MARKER = "GlazeMotionExperimental"


def fail(message: str) -> None:
    raise SystemExit(
        "Keyboard Glaze Development source / governed consumer boundary failed: " + message
    )


def require_all(label: str, text: str, markers: tuple[str, ...]) -> None:
    for marker in markers:
        if marker not in text:
            fail(f"{label} missing `{marker}`")


def main() -> None:
    for path in (DOC, ADOPTION, PLATFORM, TEST, KEYBOARD_VIEW, TOKENS, ATMOSPHERE):
        if not path.is_file():
            fail(f"missing required evidence: {path.relative_to(ROOT)}")

    doc_text = DOC.read_text(encoding="utf-8")
    adoption_text = ADOPTION.read_text(encoding="utf-8")
    platform_text = PLATFORM.read_text(encoding="utf-8")
    test_text = TEST.read_text(encoding="utf-8")
    view_text = KEYBOARD_VIEW.read_text(encoding="utf-8")
    token_text = TOKENS.read_text(encoding="utf-8")
    atmosphere_text = ATMOSPHERE.read_text(encoding="utf-8")

    require_all(
        "Motion boundary",
        doc_text,
        (
            "Lifecycle: **Experimental 0.5**",
            f"Reviewed canonical revision: `{MOTION_REFERENCE_REVISION}`",
            "Runtime compatibility baseline: **0.4.0**",
            "Evaluation mode: **native Android interaction mapping, test-only**",
            "Production dependency: **no**",
            "V1.2 is **not** the governed Stable application-consumer baseline",
            f"GLAZE UI V1.6 / `{GOVERNED_GLAZE_BASELINE}`",\n            f"`{GOVERNED_GLAZE_SOURCE_REVISION}`",
            "Motion remains separately Experimental",
            "insufficient for promotion by itself",
        ),
    )

    require_all(
        "Glaze adoption record",
        adoption_text,
        (
            "# GLAZE UI V1.2 Development Source Mapping — GoreeCloud Keyboard",
            "Status: **Migration in progress / Development**",
            f"Repository-local source target: **GLAZE UI V1.2 (`{SOURCE_GLAZE_VERSION}`)**",
            f"Current Official Stable consumer authority: **GLAZE UI V1.6 (`{GOVERNED_GLAZE_BASELINE}`)**",
            f"Reviewed V1.2 source reference: `{GLAZE_SOURCE_REVISION}`",
            "Production eligible on the Glaze UI gate: **no**",
            "applicable-migration-required",
            "Neutral glass is the material. Color is an accent.",
            "Deep Dark source material is explicitly defined",
            "editor/content sampling",
            "one-field `goreecloud-keyboard-preferences/1` portability boundary remains unchanged",
            "Glaze Motion 0.5 evaluation remains test-only",
        ),
    )

    require_all(
        "V1.2 source token mapping",
        token_text,
        (
            f'const val TargetVersion = "{SOURCE_GLAZE_VERSION}"',
            f'const val SourceRevision = "{GLAZE_SOURCE_REVISION}"',
            "enum class Appearance { LIGHT, DARK, DEEP_DARK }",
            "const val GeneralInteractionFloorDp = 48f",
            "const val TouchAssistanceInteractionFloorDp = 56f",
            "const val PressedOverlayOpacity = 0.095f",
            "const val FocusWidthDp = 3f",
            "const val IncreasedContrastFocusWidthDp = 4f",
            "Appearance.DEEP_DARK -> DeepDarkPalette",
            "fun stateOverlayArgb(",
        ),
    )

    require_all(
        "V1.2 atmosphere/material boundary",
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
            "const val EnvironmentalColorMemoryEnabled = false",
            "const val RemoteColorDerivationAllowed = false",
            "const val PersistentSampleHistoryAllowed = false",
            "const val SemanticInferenceAllowed = false",
            "const val AnimatedAtmosphereEnabled = false",
            "No editor/content",
        ),
    )

    require_all(
        "Platform Contract v0.4",
        platform_text,
        (
            'schema_version: "0.4"',
            "  id: goreecloud-keyboard",
            "  repository: GoreeCloud/keyboard",
            f'  glaze_ui:\n    result: applicable-migration-required\n    version: "{GOVERNED_GLAZE_BASELINE}"',
            f'  glaze_ui_required: "{GOVERNED_GLAZE_BASELINE}"',
            "goreecloud-platform-contract==0.4",
            f"glaze-ui=={GOVERNED_GLAZE_BASELINE}",
            "  policy:\n    result: applicable-blocked",
            "  observability:\n    result: applicable-blocked",
            "conformance:\n  status: nonconformant",
            f"exact release source {GOVERNED_GLAZE_SOURCE_REVISION}",
        ),
    )

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
        fail("KeyboardView must not infer or auto-select Deep Dark in this source-mapping slice")
    if "GlazeKeyboardAtmosphere" in view_text:
        fail("KeyboardView must not render optional atmosphere in this source-mapping slice")

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

    production_hits = []
    for path in MAIN.rglob("*.kt"):
        if MARKER in path.read_text(encoding="utf-8"):
            production_hits.append(str(path.relative_to(ROOT)))
    if production_hits:
        fail(
            "Experimental Motion escaped test quarantine into production source: "
            + ", ".join(production_hits)
        )

    active_records = adoption_text + "\n" + doc_text + "\n" + platform_text
    false_authority_claims = (
        "GLAZE UI V1.1 / `1.1.0` remains the governed Stable",
        "Current governed Stable consumer authority remains GLAZE UI V1.1",
        "GLAZE UI V1.2 (`1.2.0`) is the current Stable",
        "GLAZE UI V1.2 / `1.2.0` is the current Stable",
        "Exact Stable source authority",
        '  glaze_ui_required: "1.1.0"',
        "glaze-ui==1.1.0",
        'schema_version: "0.2"',
        "goreecloud-platform-contract==0.2",
        "Glaze UI 2.2.0 Stable is the production design-system authority.",
        "Required Stable baseline: **Glaze UI 2.2.0**",
        "stable_eligible: true",
    )
    for stale in false_authority_claims:
        if stale in active_records:
            fail(f"active evidence retains false or superseded authority claim `{stale}`")

    print(
        "Keyboard Glaze boundary passed: repository-local source target "
        f"{SOURCE_GLAZE_VERSION} at {GLAZE_SOURCE_REVISION}; governed consumer baseline "
        f"{GOVERNED_GLAZE_BASELINE} at {GOVERNED_GLAZE_SOURCE_REVISION}; Platform Contract 0.4 remains migration-required/nonconformant; "
        "Android runtime remains Light/Dark only; Experimental Motion remains quarantined; "
        "rendered/accessibility/device/release acceptance remains separate."
    )


if __name__ == "__main__":
    main()
