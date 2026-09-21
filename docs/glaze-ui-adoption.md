# GLAZE UI V1.2 Development Source Mapping — GoreeCloud Keyboard

Status: **Migration in progress / Development**  
Repository-local source target: **GLAZE UI V1.2 (`1.2.0`)**  
Governed Stable consumer baseline: **GLAZE UI V1.6 (`1.6.0`)**  
Canonical repository: `GoreeCloud/glaze-ui`  
Reviewed V1.2 source reference: `f285b9145e27e6e7027b075c37299d101945c272`  
Current Stable V1.6 source reference: `a7180679ea851389e0f3004515f9a25f420e716d`  
Production eligible on the Glaze UI gate: **no**

## Scope

This repository currently carries a Development source/material mapping derived from historical GLAZE UI V1.2 / `1.2.0`. That mapping is **not** the current governed Stable application-consumer authority. Current Official Stable consumer authority is GLAZE UI V1.6 / `1.6.0` at exact release source `a7180679ea851389e0f3004515f9a25f420e716d`. Keyboard therefore remains `applicable-migration-required` because its implemented local source tokens still model V1.2 Frosted Neutral material and interaction states rather than a completed V1.6 consumer migration.

The repository-local mapping covers applicable V1.2 foundation, Frosted Neutral material, appearance, geometry, target-size, interaction-state, and bounded native accessibility behavior in GoreeCloud Keyboard's first-party Android surface. A new bounded V1.6 runtime presentation-context foundation now consumes Android-owned font scale, animator enablement, and touch-exploration state, but the underlying material/token implementation remains V1.2-derived. This does **not** establish complete V1.6 consumer conformance, governed release adoption, production acceptance, representative-device acceptance, signed release, Release Candidate entry, or Stable qualification.

The native surface remains first-party `KeyboardView`; no web runtime, remote UI layer, network permission, analytics, advertising, or Experimental Motion production dependency is introduced.

## Implemented V1.2 Development source mapping

- `GlazeKeyboardV16PresentationPolicy` now pins current Stable V1.6 / `1.6.0` release source `a7180679ea851389e0f3004515f9a25f420e716d` and maps only Android-owned presentation signals.
- `KeyboardView` consumes that bounded V1.6 context for Reduced/Minimal motion classification, large/extra-large text classification, screen-reader/touch-assistance state, the 48/56 dp interaction floor, suggestion-strip sizing, and long-press alternate target sizing.
- No editor text, composing state, clipboard data, suggestion content, app identity, network state, privacy state, security state, or authorization truth is consumed by this presentation resolver.
- This is a partial V1.6 runtime-context migration foundation only; the native material/color/token mapping below remains explicitly historical V1.2-derived implementation until separately migrated and accepted.

- `GlazeKeyboardTokens.TargetVersion` is `1.2.0` and `SourceRevision` records reviewed V1.2 source reference `f285b9145e27e6e7027b075c37299d101945c272`.
- Governing material rule: **Neutral glass is the material. Color is an accent.**
- Inherited spacing consumed by the keyboard remains 4 dp and 8 dp.
- Existing `RadiusMediumDp` remains a source-compatible alias for the V1.2 12 dp control radius role.
- The suggestion strip and ordinary interaction floor remain 48 dp; the bounded map retains the 56 dp Touch Assistance / far-view floor without claiming platform preference resolution.
- Optical geometry references 8/16/24/32 dp plus capsule remain separate from structural radius and hit-target authority.
- V1.2 pressed, selected, focus, and increased-contrast focus calibration is represented explicitly in repository-local tokens.
- Ordinary rendered keys consume the V1.2 pressed overlay (`0.095`) during an active pointer press. The overlay follows the currently touched key, clears when the pointer leaves all key bounds, clears when a long-press alternate popup takes over interaction, and clears on release or cancellation without introducing animation or Experimental Motion runtime authority.
- Light keys consume the V1.2 neutral base-glass material `rgba(255,255,255,0.58)` rather than the older V1.1 source mapping.
- Dark keys consume the V1.2 neutral base-glass material `rgba(25,25,27,0.62)`.
- Deep Dark source material is explicitly defined as `rgba(14,14,16,0.72)` with the V1.2 structural border family.
- `KeyboardView` continues to select only Light/Dark from Android night mode at draw time. Ordinary Android dark mode is not silently treated as Deep Dark. No new user appearance preference is added by this slice.
- `GlazeKeyboardAtmosphere` records the V1.2 neutral-substrate boundary: default chromatic material tint contribution is zero; teal, green, aqua, amber, brand, and semantic color cannot define the keyboard substrate.
- Environmental aura remains optional and external to the substrate. Environmental Color Memory, editor/content sampling, remote color derivation, persistent sample history, semantic inference, telemetry, network lookup, and animated atmosphere remain disabled/not authorized.
- Keyboard remains an **Application** surface. Long-press alternates and local emoji search remain local input interactions, not Control Center or Universal Search.
- Existing Quill suggestions, sensitive-editor gating, typo correction, emoji, alternate-character, deletion, and key-release semantics remain first-party and on-device.
- The custom-drawn native keyboard has a bounded Android virtual-view accessibility foundation. `KeyboardAccessibilityDelegate` uses `ExploreByTouchHelper` to expose rendered keys, visible Quill suggestions, emoji category controls, and visible local emoji-search results as actionable virtual button nodes. Virtual activation delegates to the same first-party semantic handlers used by touch input rather than creating a second text-commit authority.
- Virtual nodes expose meaningful control labels and selected state where applicable, including Shift and the selected emoji category. Hover exploration is delegated through the Android accessibility helper, and structural changes invalidate the virtual root so assistive technology can refresh visible controls.

## Governance and presentation boundary

Repository-local V1.2 source values may be used as historical Development implementation evidence, but they cannot be represented as the current Stable consumer contract or as accepted downstream conformance. `goreecloud.platform.yaml` therefore keeps `platform_systems.glaze_ui.result` as `applicable-migration-required`, records current governed target `1.6.0`, and requires `1.6.0` in `compatibility.glaze_ui_required`. The new V1.6 Android presentation-context resolver is bounded migration progress, while the material/token implementation remains V1.2-derived and complete V1.6 consumer acceptance is still open.

Keyboard follows the mapped material and interaction hierarchy while retaining the principle: **Solid where users read or make explicit critical decisions. Glazed where users interact with transient navigation, command, search, control, or feedback chrome.** For an IME, key labels, suggestion content, selection/focus indication, and sensitive-input behavior are higher priority than optical effects.

Producer-authoritative protected meaning and accessibility resolution precede optical presentation. Removing blur, translucency, aura, or other advanced effects must never remove content, actions, focus, semantic state, hierarchy, or target size.

Accent color cannot mean privacy, security, protection, identity, recovery, synchronization, availability, sensitive-editor state, focus, or selection unless the applicable authoritative semantic contract explicitly grants that meaning. Glaze presentation grants no clipboard, editor-observation, learning, network, Identity, Mesh, Everkeep, Privacy Shield, or Wardveil Security authority.

## Privacy boundary

The V1.2 Development source mapping adds no new observation path. In particular it adds no:

- typed/composing/surrounding text read;
- suggestion or learned-input persistence;
- emoji-recents or emoji-search export;
- clipboard access;
- editor/content sampling for color;
- telemetry or analytics;
- remote design or color derivation;
- network permission;
- Identity or Mesh session; or
- background synchronization.

Pressed-state rendering consumes only pointer geometry already required by the native key interaction path. It does not inspect editor content, clipboard state, suggestions, language state, application identity, or network data.

The virtual accessibility delegate consumes only the already-rendered interaction geometry and visible control labels supplied by `KeyboardView`. It does not open or inspect an `InputConnection`, read surrounding editor text, access the clipboard, read or write preference state, persist exploration history, perform network access, or create a new semantic commit path. The keyboard service remains authoritative for Shift-aware text commit behavior.

The existing one-field `goreecloud-keyboard-preferences/1` portability boundary remains unchanged and still contains only the explicitly selected emoji category.

## Repository-local evidence

- `android/app/src/main/kotlin/com/goreecloud/keyboard/KeyboardView.kt` — first-party rendering/pointer-input surface; consumes neutral V1.2 Development key material and native pressed-state feedback while retaining Android Light/Dark runtime selection; publishes current rendered controls to the accessibility delegate and routes virtual activation through shared semantic handlers.
- `android/app/src/main/kotlin/com/goreecloud/keyboard/KeyboardAccessibilityDelegate.kt` — bounded `ExploreByTouchHelper` virtual-node bridge for custom-drawn keys, suggestions, emoji category controls, and visible local emoji-search results.
- `android/app/src/main/kotlin/com/goreecloud/keyboard/GlazeKeyboardTokens.kt` — bounded V1.2 structural/material/state source mapping including explicit Deep Dark source values.
- `android/app/src/main/kotlin/com/goreecloud/keyboard/GlazeKeyboardAtmosphere.kt` — V1.2 neutral-substrate and non-semantic atmosphere boundary.
- `android/app/src/main/kotlin/com/goreecloud/keyboard/GlazeKeyboardV16PresentationPolicy.kt` — bounded V1.6 Android presentation-context resolver pinned to the exact Stable release source.
- `GlazeKeyboardV16PresentationPolicyTest` — unit evidence for exact V1.6 authority, Reduced/Minimal motion resolution, Android font-scale thresholds, and 48/56 dp touch-assistance target behavior.
- `android/app/src/main/kotlin/com/goreecloud/keyboard/AlternatePopupLayout.kt` — viewport-bounded long-press geometry/hit-test authority.
- `GlazeKeyboardTokensTest` — exact repository source provenance, inherited geometry, neutral Light/Dark/Deep Dark materials, state calibration, interaction floors, and atmosphere-observation/tinting assertions.
- `GlazeKeyboardV12VisualStateRuntimeTest` — native emulator rendering evidence that an ordinary key changes visually on press, returns to idle presentation on release/cancel, and retains release-only semantic commit behavior.
- `KeyboardAccessibilityRuntimeTest` — Android instrumentation evidence that rendered custom controls become virtual button nodes, virtual activation reaches the ordinary listener path, suggestions remain actionable, emoji search controls remain discoverable, and selected-state presentation is surfaced.
- `scripts/check_keyboard_accessibility.py` — fail-closed repository guard for the virtual-node integration and its minimized data-authority boundary.
- `AlternatePopupLayoutTest` — normal, edge, compact multi-row, failure, gap/unused-cell, outside-point, and non-finite hit-test behavior.
- Android manifest — no network permission.
- Android CI — repository Glaze/Motion governance, editor-privacy and accessibility source guards, JVM tests, debug assembly, and native emulator interaction validation.

## Acceptance still required

This source mapping still does not establish:

- complete migration from the historical V1.2 material/token mapping to current Stable GLAZE UI V1.6 / `1.6.0` authority beyond the bounded Android presentation-context foundation;
- a reviewed runtime policy for selecting Deep Dark, if Keyboard should expose one;
- complete governed component/state/material-role mapping across every keyboard/settings surface;
- selected/focus state runtime coverage for every applicable control surface;
- Reduced Transparency / solid fallback acceptance;
- Increased Contrast and forced-colors/native-equivalent acceptance;
- Reduced Motion;
- 200% large-text/reflow within host IME constraints;
- platform Touch Assistance detection and 56 dp assisted geometry;
- RTL/localization expansion;
- representative TalkBack/Switch Access acceptance, including focus order, exploration behavior, host-IME interaction, long-press alternate discovery/activation, and physical-device ergonomics;
- representative phone/tablet/foldable and host-IME adaptation;
- representative physical-device long-press/slide/release ergonomics;
- representative physical-device performance, power, thermal, and latency acceptance;
- Human Visual Excellence review of the actual Keyboard consumer;
- Privacy Shield and Wardveil Security acceptance appropriate to sensitive input processing;
- Everkeep acceptance for any approved durable-state recovery scope;
- Mesh/Identity integration only where applicable and authorized;
- Manager visibility/administrative integration where required; or
- production signing, distribution, release approval, and Stable qualification.

Source/build/emulator success remains Development evidence only until those applicable runtime and release gates are satisfied.

## Glaze Motion boundary

Historical Glaze Motion 0.5 evaluation remains test-only. Glaze Motion is separately governed Experimental work and is not promoted by this V1.2 Development source mapping. The pressed-state feedback in production `KeyboardView` is an immediate deterministic state overlay and does not activate the Experimental Motion subsystem. Glaze Motion is not a production dependency and cannot establish consumer acceptance.

## Rollback and reconciliation

If the repository-local V1.2 source/material/accessibility mapping causes a regression, revert the exact Keyboard mapping commit/merge to the prior validated Keyboard source revision. Do not relabel an ungoverned or superseded source line as current Stable merely to preserve a consumer implementation. Glaze consumer authority must follow the governed Stable release line, and Keyboard must explicitly migrate, re-pin, and revalidate whenever the current governed Stable consumer authority changes.
