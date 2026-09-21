# GLAZE UI V1.6 Development Source Baseline — GoreeCloud Keyboard

Status: **Migration in progress / Development**  
Repository-local source target: **GLAZE UI V1.6 (`1.6.0`)**  
Governed Stable consumer baseline: **GLAZE UI V1.6 (`1.6.0`)**  
Accepted release source: `a7180679ea851389e0f3004515f9a25f420e716d`  
Source qualification anchor: `c7509c79256b04b0aa67cb9dd0737d7588e0ae4a`  
Stable runtime entrypoint: `js/glaze-v1.6.0.mjs`  
Known-good rollback baseline: **1.5.1**  
Production eligible on the Glaze UI gate: **no**

## Scope

This tranche replaces Keyboard's historical V1.2 source identity with a bounded V1.6 native source baseline. It pins the exact accepted Stable release source, adopts V1.6 semantic material roles in the actual `KeyboardView` rendering path, and records V1.6 presentation-only authority boundaries.

It does **not** establish complete Keyboard consumer migration or downstream acceptance. `goreecloud.platform.yaml` therefore remains `applicable-migration-required` and overall conformance remains nonconformant until whole-keyboard rendered, accessibility, adaptive/form-factor, representative-device, performance, Human Visual Excellence, rollback, release, and production evidence is accepted.

The existing neutral Light/Dark/Deep Dark palette values are retained as a bounded fallback during this first source-baseline tranche. They are not represented as newly qualified V1.6 visual-token evidence.

## Implemented V1.6 source baseline

- `GlazeKeyboardTokens.TargetVersion` is `1.6.0`.
- `AcceptedReleaseSource` pins `a7180679ea851389e0f3004515f9a25f420e716d`.
- `SourceQualificationAnchor` pins `c7509c79256b04b0aa67cb9dd0737d7588e0ae4a`.
- `StableRuntimeEntrypoint` records `js/glaze-v1.6.0.mjs`.
- Material roles are explicit: Canvas, Solid, Raised, Functional Glass, Clear Glass, and Overlay.
- `KeyboardView` consumes those semantic roles for the actual Canvas, key substrate, popup, and selected-popup surfaces.
- V1.6 presentation remains presentation-only: it cannot request permission automatically, infer authorization, execute consequential actions automatically, or establish downstream consumer acceptance.
- Clarity overrides translucency, blur alone cannot provide contrast, unsupported backdrops must fall back safely, and Keyboard does not inspect editor/background content to manufacture material or semantic state.
- Existing 48 dp ordinary and 56 dp Touch Assistance interaction floors remain preserved.
- Existing pressed/selected/focus calibration remains explicit during this bounded migration.
- Android night mode continues to select only Light/Dark; Deep Dark is not inferred automatically.
- The one-field `goreecloud-keyboard-preferences/1` portability boundary remains unchanged.
- Glaze Motion 0.5 evaluation remains test-only and is not a production dependency.

## Privacy and authority boundary

This source migration adds no typed-text observation, editor-content sampling, clipboard access, learned-input persistence, remote color derivation, telemetry, analytics, network permission, Identity session, Mesh session, or automatic synchronization.

Glaze presentation grants no Privacy Shield, Wardveil Security, Identity, Everkeep, Manager, Mesh, Policy, Observability, Universal Search, or Control Center authority.

## Repository-local evidence

- `GlazeKeyboardTokens.kt` — exact V1.6 source identity, material roles, authority boundary, and bounded fallback palette.
- `GlazeKeyboardAtmosphere.kt` — V1.6 clarity/translucency and no-observation boundary.
- `KeyboardView.kt` — actual native consumption of V1.6 semantic material roles.
- `GlazeKeyboardTokensTest.kt` — exact release-source, authority, material-role, geometry, state, and no-observation regression coverage.
- `scripts/check_glaze_motion_evaluation.py` — fail-closed source/governance guard.
- Android CI — exact-head repository/source checks, JVM tests, build, and applicable emulator validation.

## Acceptance still required

This source baseline does not by itself establish complete V1.6 consumer migration. Remaining gates include complete component/state/material-role coverage, Reduced Transparency, Reduced Motion, Increased Contrast, large text/reflow, RTL/localization, Touch Assistance resolution, TalkBack/Switch Access, representative host-editor compatibility, phone/tablet/foldable behavior, physical-device ergonomics/latency/power/thermal evidence, Human Visual Excellence, applicable Integral Platform System acceptance, signing/distribution, recovery/rollback, Release Candidate, production, and Stable qualification.

## Rollback

If this source-baseline tranche regresses Keyboard, revert the exact candidate/merge and return to the previously verified main revision. The shared Glaze UI rollback baseline remains 1.5.1; downstream Keyboard rollback does not alter shared Glaze lifecycle authority.
