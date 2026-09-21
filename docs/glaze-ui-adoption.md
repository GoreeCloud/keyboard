# GLAZE UI V1.5 Implemented Mapping / V1.6 Migration Requirement — GoreeCloud Keyboard

Status: **Migration in progress / Development**  
Current Stable target: **GLAZE UI V1.6 (`1.6.0`)**  
Canonical repository: `GoreeCloud/glaze-ui`  
Exact current Stable release source authority: `a7180679ea851389e0f3004515f9a25f420e716d`  
Implemented Development mapping: **GLAZE UI V1.5 (`1.5.0`)** at `b7fa8164bfdeaa1dc0acb21b770e7601120da04e`  
Reviewed V1.5 implementation anchor: `ee1032a0822ab8e103f8afe48e5c1859fde65cc9`  
Inherited optical/material baseline: **GLAZE UI V1.4.1 (`1.4.1`)** at `4fab9da0fad2e5c974e0e66ec88632c61745751c`  
Immediate rollback baseline: **V1.4.1 (`1.4.1`)**  
Production eligible on the Glaze UI gate: **no**

## Scope

Current Official Stable GLAZE UI authority is V1.6 / `1.6.0`, and GoreeCloud Keyboard must migrate to that target before current application conformance can be claimed. The implemented Development presentation remains the PR #68 V1.5 mapping: V1.5 capability-aware presentation semantics at exact Stable source `b7fa8164bfdeaa1dc0acb21b770e7601120da04e`, with the accepted V1.4.1 optical/material baseline. This record therefore distinguishes current required authority from implemented source instead of relabeling V1.5 code as V1.6. `GlazeKeyboardCapabilityV15` handles bounded authority-owned capability presentation, while `GlazeKeyboardTokens`, `GlazeKeyboardOptics`, and `GlazeKeyboardAtmosphere` retain the privacy-restricted native visual baseline.

This mapping remains **Development evidence only**. Shared Glaze Stable status does not establish complete Keyboard consumer conformance, rendered/accessibility/device acceptance, production approval, signed release, Release Candidate entry, or Stable Keyboard qualification.

The native surface remains first-party `KeyboardView`; no web runtime, remote UI layer, network permission, analytics, advertising, or Experimental Motion production dependency is introduced.

## Implemented V1.5 mapping

- `GlazeKeyboardCapabilityV15.TargetVersion` is `1.5.0`, with exact Stable source revision `b7fa8164bfdeaa1dc0acb21b770e7601120da04e` and reviewed implementation anchor `ee1032a0822ab8e103f8afe48e5c1859fde65cc9`.
- The inherited optical/material baseline is V1.4.1 at exact revision `4fab9da0fad2e5c974e0e66ec88632c61745751c`; `GlazeKeyboardTokens` and `GlazeKeyboardOptics` pin that baseline explicitly.
- V1.4.1 is the immediate rollback target for the V1.5 migration.
- V1.5 capability presentation accepts only explicit capability state already supplied by the authority that owns it.
- Missing capability state fails closed as `UNKNOWN`; duplicate capability ownership fails closed as `CONFLICT` instead of inventing provider precedence.
- `AVAILABLE` presentation never grants automatic execution, permission, navigation, privacy authorization, security authorization, or provider-selection authority.
- Typed text, composing text, surrounding text, suggestion content, clipboard state, application identity, emoji history, and key history are forbidden V1.5 capability/context inputs.
- Remote context and telemetry remain unauthorized and unnecessary.
- Governing material rule remains: **Neutral glass is the material. Color is an accent.**
- Inherited spacing consumed by the keyboard remains 4 dp and 8 dp.
- `RadiusMediumDp` remains the 12 dp control-role mapping.
- The suggestion strip and ordinary interaction floor remain 48 dp; the bounded map retains the 56 dp Touch Assistance / far-view floor without claiming platform preference resolution.
- Optical geometry references 8/16/24/32 dp plus capsule remain separate from structural radius and hit-target authority.
- Existing pressed, selected, focus, and increased-contrast focus calibration remains explicit and test locked.
- Light, Dark, and Deep Dark source palettes remain neutral Frosted material mappings. Runtime appearance selection remains Android Light/Dark only until a separately reviewed Deep Dark runtime policy exists.
- `GlazeKeyboardOptics` retains a deterministic V1.4.1 Optical Hardening resolver with maximum semantic protection for this highly sensitive input surface.
- Reduced Transparency and Forced Colors fail closed to `SOLID_ACCESSIBLE`, zero blur, zero decorative tint, and full semantic protection.
- Increased Contrast raises frost protection without enabling decorative color behavior.
- Environmental Color Memory influence remains intentionally **0%** for Keyboard even though the inherited shared optical baseline permits bounded memory on less-sensitive surfaces.
- Decorative environment tinting/aura remains disabled for Keyboard.
- Keyboard remains an **Application** surface. Local emoji search, suggestions, and alternate-character interaction remain input features, not Control Center or Universal Search.

## Capability and authority boundary

GLAZE UI remains presentation and interaction authority only. `GlazeKeyboardCapabilityV15` cannot create or replace Privacy Shield authorization, Wardveil Security evidence, GoreeCloud Identity state, Mesh ownership, Everkeep continuity state, Manager state, Android permissions, Keyboard typing policy, or application execution authority.

The V1.5 resolver is intentionally non-operational. Its outputs describe whether an already-defined action may be presented as enabled or disabled from explicit capability evidence. They do not execute the action and do not convert presentation state into permission.

For duplicate capability records, Keyboard fails closed rather than choosing a provider. Provider precedence must come from the authoritative system that owns the capability relationship, not from Glaze.

## Optical privacy boundary

Keyboard adopts a deliberately narrower optical policy than ordinary application surfaces.

The following data must never be used to select frost, tint, blur, warmth, depth, material color, capability presentation, or any other Glaze treatment:

- typed text;
- composing text;
- surrounding editor text;
- suggestions or correction candidates;
- clipboard contents;
- application/package identity;
- sensitive-editor classification details beyond the existing functional privacy gate;
- emoji search queries or recents;
- key history or typing cadence;
- learned language/profile data; or
- network/remote context.

Visual or capability adaptation therefore cannot create a new observation channel over the IME. `GlazeKeyboardOptics` consumes only explicitly supplied non-content presentation/accessibility state, and `GlazeKeyboardCapabilityV15` consumes only explicit authority-owned capability records. Neither performs collection.

## Accessibility precedence

For Keyboard, accessibility and semantic legibility outrank all optical expression:

1. Forced Colors / native equivalent.
2. Reduced Transparency.
3. Increased Contrast and focus visibility.
4. Key/suggestion/control semantic clarity and target size.
5. Neutral material expression.

No optical or capability mode may remove a required key label, suggestion, focus state, selected state, action, hierarchy, or interaction target. A restricted/unavailable action may remain visible when product semantics require explanation, but Glaze may not bypass its authority state.

## Governance and presentation boundary

`goreecloud.platform.yaml` keeps `platform_systems.glaze_ui.result` as `applicable-migration-required` and product `conformance.status` as `nonconformant` until complete Keyboard-specific acceptance exists.

Keyboard preserves the Glaze presentation principle: **Solid where users read or make explicit critical decisions. Glazed where users interact with transient navigation, command, search, control, or feedback chrome.** On an IME, legibility, input correctness, privacy, accessibility, and latency are always more important than optical richness.

Accent, material color, availability treatment, or capability presentation cannot imply privacy, security, protection, identity, recovery, synchronization, sensitive-editor state, focus, selection, or authorization beyond the applicable authoritative semantic contract.

## Existing privacy boundary remains unchanged

The V1.5 migration adds no new observation path and no new Android permission. In particular it adds no:

- typed/composing/surrounding-text read for visual or capability purposes;
- suggestion or learned-input persistence;
- clipboard observation;
- editor/content sampling for color or capability resolution;
- environmental sample persistence;
- telemetry or analytics;
- remote design/color/capability derivation;
- network permission;
- Identity or Mesh session; or
- background synchronization.

The existing one-field `goreecloud-keyboard-preferences/1` portability boundary remains unchanged and contains only the explicitly selected emoji category.

## Repository-local evidence

- `android/app/src/main/kotlin/com/goreecloud/keyboard/KeyboardView.kt` — first-party native rendering/input surface using the neutral token mapping.
- `android/app/src/main/kotlin/com/goreecloud/keyboard/GlazeKeyboardCapabilityV15.kt` — current V1.5 Stable capability-presentation provenance and fail-closed authority semantics.
- `android/app/src/main/kotlin/com/goreecloud/keyboard/GlazeKeyboardTokens.kt` — inherited V1.4.1 geometry, appearance, interaction, and neutral-material mapping.
- `android/app/src/main/kotlin/com/goreecloud/keyboard/GlazeKeyboardOptics.kt` — inherited V1.4.1 privacy-safe Optical Hardening policy.
- `android/app/src/main/kotlin/com/goreecloud/keyboard/GlazeKeyboardAtmosphere.kt` — non-semantic neutral-substrate and no-sensitive-observation boundary.
- `android/app/src/test/kotlin/com/goreecloud/keyboard/GlazeKeyboardCapabilityV15Test.kt` — current Stable authority, no-sensitive-context, fail-closed capability, and no-automatic-execution coverage.
- `android/app/src/test/kotlin/com/goreecloud/keyboard/GlazeKeyboardTokensTest.kt` — exact optical-baseline source/provenance/material/privacy contract coverage.
- `android/app/src/test/kotlin/com/goreecloud/keyboard/GlazeKeyboardOpticsTest.kt` — optical accessibility and sensitive-source prohibition coverage.
- `android/app/src/main/kotlin/com/goreecloud/keyboard/KeyboardAccessibilityDelegate.kt` — bounded Android virtual-view accessibility bridge.
- `scripts/check_glaze_motion_evaluation.py` — fail-closed Glaze/Motion/source-governance guard.
- Android manifest — no network permission.
- Android CI — governance, editor-privacy, accessibility, JVM tests, debug assembly, and emulator validation when green for the exact candidate revision.

## V1.5 shared-scope and V1.5.1 follow-up boundary

Shared GLAZE UI V1.5.0 is Stable for its bounded reviewed scope. Numeric Performance Budget v1.0 measurement and representative foldable/posture target-runtime qualification are explicitly deferred by Glaze governance to V1.5.1; Keyboard must not represent those shared follow-ups as V1.5.0 evidence.

Keyboard also retains its own stricter product-local acceptance requirements. Shared Glaze acceptance never substitutes for IME-specific privacy, accessibility, latency, ergonomics, or physical-device evidence.

## Acceptance still required

This source mapping still does not establish:

- a reviewed runtime policy for selecting Deep Dark, if Keyboard should expose one;
- complete component/state/material-role mapping across every keyboard/settings surface;
- actual wiring of Reduced Transparency, Increased Contrast, and forced-color/native-equivalent system preferences into every applicable runtime surface;
- selected/focus state runtime coverage for every applicable control;
- Reduced Motion behavior;
- 200% large-text/reflow within host IME constraints;
- platform Touch Assistance detection and 56 dp assisted geometry;
- RTL/localization expansion;
- representative TalkBack/Switch Access acceptance, including focus order, exploration behavior, host-IME interaction, long-press alternate discovery/activation, and physical-device ergonomics;
- representative phone/tablet/foldable and host-IME adaptation;
- representative physical-device long-press/slide/release ergonomics;
- representative physical-device performance, power, thermal, and latency acceptance;
- V1.5.1 shared performance/posture follow-up where applicable;
- Privacy Shield and Wardveil Security acceptance appropriate to sensitive input processing;
- Everkeep acceptance for any approved durable-state recovery scope;
- Mesh/Identity integration only where applicable and authorized;
- Manager visibility/administrative integration where required; or
- production signing, distribution, release approval, and Stable qualification.

Source/build/emulator success remains Development evidence only until those applicable runtime and release gates are satisfied.

## Glaze Motion boundary

Historical Glaze Motion 0.5 evaluation remains test-only. Motion is separately governed Experimental work and is not promoted by the V1.5 design-system migration. Its existing reviewed reference to the earlier V1.4 migration context remains historical evaluation provenance, not current design-system authority. Immediate pressed-state feedback in production `KeyboardView` does not activate the Experimental Motion subsystem.

## Rollback

V1.4.1 is the immediate known-good shared Glaze rollback baseline for this V1.5 migration. If the Keyboard V1.5 mapping itself regresses, revert the Keyboard V1.5 migration commits while preserving the canonical current Glaze lifecycle authority. A local rollback does not authorize relabeling an older Glaze release as current Stable.
