# GoreeCloud Keyboard — Legacy Drive Changelog, Part 05

> Historical provenance migrated from Drive `Change Log — Keyboard.docx`. This part preserves non-empty source paragraphs 141–175 of 208; text is verbatim and only Word heading styles are normalized to Markdown. Historical Draft/candidate/lifecycle statements do not override current `main`.

GoreeCloud Keyboard Draft PR #30 advanced to exact head `3aff5d7390dae5f1518bc96b0a80301873af4a9b`. The rendered native alternate popup already consumes the viewport-bounded placement/cell geometry policy and fails closed when a valid popup cannot fit. This continuation adds deterministic geometry-level `hitTest(...)` behavior that selects only real alternate cells and rejects gaps, unused final-row cells, non-finite coordinates, and outside points. Android CI #78 / run `33596686074` completed successfully on the documentation-complete exact head.

Authority remains unchanged: this path consumes geometry only and adds no surrounding-text inspection, clipboard access, learning, persistence, personalization, or network behavior. Direct pointer-path adoption of the helper, representative-device gesture/compact-width/accessibility acceptance, complete current Stable Glaze UI conformance, release, and Stable qualification remain pending

### September 2, 2026 — Glaze UI 2.2 Stable Adoption Candidate — Development

Draft PR #31 advanced Keyboard's repository-local Glaze authority to current Glaze UI 2.2.0 Stable at exact head `c7b425d28f96d2da20129843f71e3d54cc232e2f`. The bounded native mapping preserves compatible 4/8 dp spacing, 14 dp radius, 48 dp normal and 56 dp Touch Assistance floors, and Light/Dark values; classifies the keyboard as an Application surface under the 2.2 System Shell hierarchy; leaves local emoji search local; and updates governance to reject superseded active 2.1/2.0/1.x authority while keeping Experimental Glaze Motion test-only. Android CI #83 / run `33598082008` passed on that exact head.

Acceptance boundary: Adoption Candidate / Development only. No Deep Dark, complete 2.2 component/state/System Glaze acceptance, full accessibility resolution, representative-device/Human Visual Excellence acceptance, production packaging, release, or Stable qualification is implied.

.

### September 2, 2026 — Alternate Popup Unified Geometry and Pointer Hit Authority — Development

GoreeCloud Keyboard Draft PR #30 advanced to exact head `e322b8e92d438446170fc55a6f1c512bf5a768b8`. The long-press alternate-character popup now retains the pure `AlternatePopupLayoutResult` generated for rendering and uses that same result for pointer-move selection through `hitTest(x, y)`. The former second mutable renderer-populated Android `RectF` hit-target list is no longer gesture-selection authority.

The layout remains viewport-bounded, supports compact multi-row placement, prefers above-key placement when valid, and fails closed when a popup cannot fit. Pointer hit testing also fails closed for missing/failed layouts, non-finite coordinates, outside points, inter-cell gaps, and unused final-row cells. Pointer-up can commit only a valid selected alternate index. The path consumes source-key, viewport, and pointer geometry only and adds no surrounding-text, clipboard, language-inference, learning, personalization, persistence, or network authority. Existing IME text-commit authority is unchanged.

Exact-head validation: Android CI #85 / run `33607866002` passed at `e322b8e92d438446170fc55a6f1c512bf5a768b8`, including unit tests, debug APK assembly, and the repository Android interaction evaluation.

Classification remains Development. Representative physical-device long-press/slide/release gesture, compact-width behavior, TalkBack/switch-access and other accessibility acceptance, and complete current-Stable Glaze UI 2.2 application acceptance remain separate gates. Glaze UI 2.2 adoption remains the parallel Draft PR #31 line and is not implied by this interaction slice.

### September 2, 2026 — Glaze UI 2.2 and Alternate Popup Lines Unified — Development

Draft PR #31 advanced to exact combined head `e56ec4a01dde4024aa5ef54b3d13fd681ef8ada7`. The current Glaze UI 2.2.0 Adoption Candidate mapping and the validated long-press alternate-popup geometry/pointer-selection work now coexist on one branch. `AlternatePopupLayout` is the viewport-bounded placement and exact hit-test authority consumed by `KeyboardView`; rendered cells and pointer movement share the same retained `AlternatePopupLayoutResult`, with fail-closed handling for impossible layouts, gaps, unused cells, outside/non-finite coordinates, and missing layout state. The former second mutable renderer `RectF` hit-target authority is removed.

Validation: Android CI #86 / run `33613668324` passed the repository Glaze UI 2.2 / Experimental Motion governance boundary, JVM unit tests, debug APK assembly, and Android 15 / API 35 native interaction emulator evaluation on the exact head. The earlier popup-only head `e322b8e92d438446170fc55a6f1c512bf5a768b8` remains historical Development evidence and is superseded as the current interaction line by this combined head.

Privacy/authority remains unchanged: geometry uses only source-key, viewport, and pointer coordinates and adds no surrounding-text, clipboard, language-inference, learning, personalization, persistence, network, Universal Search, Control Center, or text-commit authority. Classification remains Adoption Candidate / Development; representative physical-device gesture/compact-width/accessibility acceptance and the remaining complete Glaze UI 2.2/release/Stable gates remain pending.

### September 6, 2026 — Secure Paste Architecture Documented — Planned / Proposed

The canonical GoreeCloud Keyboard project specification now defines the planned GoreeCloud Secure Paste architecture. GoreeCloud Keyboard is designated as the primary user-facing IME surface, Privacy Shield as the privacy-policy and data-use authorization authority, and a future privileged Android/framework Secure Paste Broker as the required system-level enforcement component for restricting passive cross-application clipboard reads while preserving explicit user-mediated paste.

The documented requirements include a dedicated Keyboard Paste action; per-application clipboard policy; Secure Paste controls; optional expiration and Paste Once behavior; same-app compatibility where appropriate; sensitive-data handling; local-first and minimized processing; no clipboard payload telemetry; no Everkeep backup, GoreeCloud Mesh synchronization, or GoreeCloud Identity association by default; minimized non-content Privacy Receipt evidence; Wardveil review for dangerous context-dependent payloads; Glaze UI and accessibility requirements; and explicit compatibility handling for applications that bypass the standard Android selection toolbar.

Implementation status remains Planned / Proposed. The documentation explicitly states that a normal Android IME cannot globally revoke another application's operating-system clipboard API authority and that production or Stable claims require a privileged system component, Privacy Shield integration, runtime enforcement evidence, accessibility/device validation, Wardveil review, Glaze UI acceptance, release packaging, and explicit production approval.

### September 6, 2026 — Keyboard Product Experience and Current Glaze UI Requirement Documented

The canonical GoreeCloud Keyboard specification now defines beauty, polish, cohesion, responsiveness, and substantive feature richness as explicit product requirements rather than optional presentation goals. The requirement covers key geometry, spacing, typography, optical alignment, surfaces, interaction states, haptics where appropriate, suggestions, emoji/symbol surfaces, popups, toolbar actions, settings, onboarding, error/empty states, and adaptive layouts while preserving privacy, security, accessibility, performance, and native Android IME behavior.

The active design-system target was reconciled to the canonical current Stable GLAZE UI V1.2 / 1.2.0 authority. GoreeCloud Keyboard is required to track the latest current Stable Glaze UI release through deliberate migration, exact-revision evidence, regression testing, rendered/native accessibility validation, representative-device ergonomics, and Human Visual Excellence rather than by token copying or version-label changes alone.

The feature-rich direction now explicitly includes high-quality typing and correction, multilingual input, gesture typing where approved, emoji and symbol discovery, long-press alternates, clipboard/Secure Paste integration, privacy-approved voice input, one-handed and split layouts, tablet/foldable adaptation, user dictionaries, Quill-assisted writing, accessibility, and other useful native capabilities. This documentation change does not claim those planned capabilities are already implemented or Stable

### September 6, 2026 — Stabilization Stack Consolidated to Main and GLAZE UI V1.2 Native Material Migration Merged

I consolidated the active GoreeCloud Keyboard Development stack through integration pull request #47 and squash-merged exact validated head `861f81c0e50708fee2ffc3b293d52d996c042d03` to authoritative `main` as `a2fd37e792dd7f2120542bacd88ace41d67afa70`. The merge brings the previously stacked work onto one authoritative line, including offline emoji search and closed-session state boundaries, Unicode and regional-indicator deletion correctness, deterministic local long-press alternates with shared viewport-bounded render/hit-test geometry, privacy-minimized category-only preference portability with import-preview/export-review safeguards, fail-closed editor-session privacy lifecycle handling, bounded suggestion capture, presentation-bound suggestion commits, and Unicode-code-point typo correction.

The merged source also advances the actual native Keyboard surface to current Stable GLAZE UI V1.2 / `1.2.0` source/material authority `f285b9145e27e6e7027b075c37299d101945c272`. `KeyboardView` now consumes V1.2 Frosted Neutral key materials under the rule “Neutral glass is the material. Color is an accent.” The repository defines Light, Dark, and Deep Dark source material values, preserves 4/8 dp spacing, the 12 dp control-radius role and 48/56 dp interaction floors, and records V1.2 pressed/selected/focus calibration. Runtime appearance remains Android Light/Dark only; no unreviewed Deep Dark auto-selection or chromatic substrate is introduced.

Validation before merge: Platform Contract #61 / run `34072047052` passed using V1.2-aligned canonical Platform Contract authority `c941ce1d8d1eff3c9df994d1e16f83147eadae00`. Android CI #162 / run `34072046778` passed the Glaze/Motion governance guard, editor privacy lifecycle guard, JVM unit tests, debug APK assembly, and Android 15 / API 35 native interaction and IME activation emulator gate. The squash-merged tree is identical to the validated integration tree.

Classification remains Development. Complete rendered/native V1.2 consumer acceptance, accessibility/resilience modes, TalkBack/Switch Access, RTL/localization, adaptive phone/tablet/foldable ergonomics, representative physical-device typing/gesture/performance acceptance, Human Visual Excellence, applicable Privacy Shield/Wardveil Security/Everkeep/Identity/Mesh/Manager acceptance, protected signing/provenance, governed Release Candidate validation, production approval, and Stable qualification remain separate gates.

### September 6, 2026 — Native Virtual Accessibility Controls Merged — Development

I completed the first bounded native accessibility-node milestone for GoreeCloud Keyboard. Pull request #49, `Expose native keyboard controls to accessibility`, was validated at exact head `03bb00e323e09c0d8341ad7df282a31cf7e89070` and squash-merged to authoritative `main` as `a25bb1c55dfe87e4a90ce3a7802fd845a33bfe43`.

The custom-drawn Android KeyboardView now publishes rendered keys, visible Quill suggestions, emoji category controls, and visible local emoji-search results as virtual Android button nodes using `ExploreByTouchHelper`. The virtual layer carries meaningful labels and selected state, supports hover exploration, and routes accessibility ACTION_CLICK activation through the same semantic handlers used by the normal keyboard interaction path. It therefore does not create separate text-commit authority.

The accessibility delegate is deliberately data-minimized. It consumes rendered geometry and visible control labels only; it does not inspect `InputConnection` or surrounding editor text, read clipboard contents, access persistent usage history, use the network, or gain telemetry, synchronization, learning, Identity, or Mesh authority. Android CI now includes a fail-closed accessibility source-boundary validator.

Validation history: earlier PR #49 emulator attempts exposed a test-harness problem in how the instrumentation test queried an AndroidX compat wrapper rather than Android's real platform accessibility provider. The diagnostic assertion isolated that issue. The final test now exercises `view.accessibilityNodeProvider`, matching the platform bridge used by accessibility services. Android CI #170 / workflow run `34074312708` then passed the complete build job and Android 15 emulator job, including Glaze/Motion governance, editor-privacy governance, the new accessibility guard, JVM tests, debug APK assembly, platform virtual-node creation/click routing, native interaction checks, and IME activation.

Classification remains Development. Representative TalkBack/Switch Access operation, accessible long-press alternate discovery/activation, representative physical-device accessibility and ergonomics, complete GLAZE UI V1.2 accessibility acceptance, adaptive form-factor acceptance, signed production packaging, Release Candidate approval, production release, and Stable qualification remain pending.

### September 8, 2026 — GLAZE UI V1.3 Platform Contract validation repair — Development
