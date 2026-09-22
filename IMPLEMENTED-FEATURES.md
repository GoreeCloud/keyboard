# GoreeCloud Keyboard — Implemented Features

**Record type:** Repository implemented-feature inventory  
**Repository:** `GoreeCloud/keyboard`  
**Lifecycle:** Development / nonconformant  
**Repository version:** `0.1.0-dev`  
**Migration state:** Complete on authoritative `main`; PR #79 merged as `6071bf3b7fddf36ddaa172b7ca858948f95ae6d1`, exact-main Android CI #264 passed, and the mapped legacy Drive roadmap/changelog sources were permanently retired and independently verified absent on September 22, 2026.  
**Current runtime-bearing baseline:** `2dd42a70b2ca74e55fe3b3fcb3fca0547e06316a`, merge of PR #77 on September 21, 2026.  
**Governing standard:** Standard — Repository Feature Tracking and Changelog Governance v1.0.

## Interpretation

This record describes capabilities accepted on authoritative `main`. It does not promote open Draft pull requests, historical Drive roadmap wording, or superseded Glaze adoption claims into current implementation state.

Authoritative `main` includes the repository-native governance migration at `6071bf3b7fddf36ddaa172b7ca858948f95ae6d1`; exact-main Android CI #264 / run `35758772834` passed on that revision. The current runtime-bearing capability baseline remains `2dd42a70b2ca74e55fe3b3fcb3fca0547e06316a` from PR #77. Source/build/emulator success does not establish representative physical-device, Production, Release Candidate, or Stable acceptance.

Draft PR #78 (`stabilize/touch-assistance-gap-compression-20260921`) and older open stacked Drafts remain candidate-only and are not represented as accepted behavior below.

## Implemented Development capabilities

### Native Android IME foundation
- First-party Android `InputMethodService` integration.
- First-party `KeyboardView` rendering, hit testing, pointer input, and Android input-method registration checks.
- Native QWERTY letters with Shift, Backspace, Space, Enter, and editor-action behavior already accepted on `main`.
- No Android network permission in the current application foundation.

### Symbols and Unicode-safe text input
- First-party letters/symbols switching with `?123`, `ABC`, and a secondary `=\\<` symbol page.
- Complete `String` payloads across native key input rather than UTF-16 `Char` assumptions.
- Bounded Unicode-aware backspace for common combining marks, variation selectors, emoji modifiers, keycaps, tag sequences, ZWJ-linked emoji, regional-indicator flags, and CRLF.
- Fail-closed handling when ordinary bounded look-behind is ambiguous; sensitive editors retain the stricter no-look-behind path.

### Emoji input and discovery
- First-party packaged emoji catalog with Smileys, People, Nature, Food, Symbols, and Travel categories.
- Selected composed variation-selector, skin-tone, ZWJ, family, rainbow-flag, and regional-indicator sequences.
- Accessible compact category controls.
- Device-local bounded Recent emoji state with explicit Clear behavior.
- Fully offline transient emoji search over the packaged catalog; query state is session-local and is not sent to the active editor unless a result is deliberately selected.

### Local Quill suggestion boundary
- Deterministic local prefix suggestions.
- Bounded one-edit typo-correction candidates using Unicode code points.
- Bounded transient suggestion capture.
- Suggestion commit authority restricted to the exact candidates currently presented for the active editor session.
- Sensitive-editor, host no-suggestions, and host no-personalized-learning gating.
- Fail-closed editor lifecycle handling across authoritative start/finish transitions and no-active-editor state.

### Long-press alternates and accessibility foundations
- Deterministic local long-press alternates for common Latin diacritics and punctuation.
- Viewport-bounded popup geometry with shared render/hit-test geometry.
- Pointer movement selection, cancellation, haptic feedback, and accessibility announcements.
- Android virtual accessibility-node foundations and bounded alternate-character accessibility actions accepted on `main`.

### Privacy-minimized portability
- `goreecloud-keyboard-preferences/1` portable format containing exactly the last explicitly selected emoji category.
- Strict schema/validation/checksum handling.
- Explicit user-controlled Android Storage Access Framework import/export.
- Import preview before mutation and export review/freeze before destination selection.
- No generic preference serialization.
- Typed text, composing/surrounding editor context, learned input, suggestions, emoji recents/history, emoji search queries, clipboard payloads, key history, credentials, secrets, and sensitive-editor contents are outside the current portable format.

### GLAZE UI and presentation state
- Historical repository-local V1.2 / `1.2.0` Frosted Neutral optical/material substrate remains implemented Development provenance at reviewed source `f285b9145e27e6e7027b075c37299d101945c272`.
- Current governed consumer target is GLAZE UI V1.6 / `1.6.0` at exact Stable release source `a7180679ea851389e0f3004515f9a25f420e716d`.
- PR #73 accepted a bounded V1.6 Android presentation-context layer consuming Android font scale, animator enablement, and touch-exploration state while preserving the V1.2-derived optical substrate.
- PR #77 accepted consistent Touch Assistance sizing for the suggestion strip and alternate targets, with a 56 dp interaction floor and a 308 dp preferred four-row IME height when touch exploration is active and Android grants the requested size.
- Historical Experimental Glaze Motion evaluation remains test-only and is not a production dependency.

### Repository and platform control plane
- Platform Contract 0.4 declaration with current repository identity `GoreeCloud/keyboard`.
- Exactly nine Integral Platform Systems are declared: Manager, Privacy Shield, Wardveil Security, Everkeep, GLAZE UI, Mesh, Identity, Policy, and Observability.
- GLAZE UI is `applicable-migration-required`; the other currently applicable system integrations remain blocked pending accepted evidence.
- Android CI validates source/build/governance and an Android 15 emulator native-interaction/IME-activation path.

## Implemented-but-incomplete capability families

These foundations are implemented only for the bounded scope described above and retain open obligations in `PLANNED-FEATURES.md`:
- local typing quality and correction;
- emoji/symbol/alternate discovery;
- one-field portability and recovery preparation;
- V1.6 presentation-context adoption;
- accessibility foundations;
- platform-system declarations and local safeguards.

## Explicitly not implemented on current `main`

Current authoritative `main` does not establish:
- accepted spacebar cursor-control behavior from Draft PRs #63/#64 or Draft PR #78;
- the Draft number-row or configurable utility-toolbar stacks;
- accepted Arabic/multilingual layout work from Draft PR #65 or its stacked successors;
- gesture/swipe typing;
- system-wide GoreeCloud Secure Paste enforcement;
- privacy-approved voice input or translation;
- one-handed, floating, split, tablet, foldable, or posture-aware production layouts;
- broader Quill writing assistance beyond the local suggestion boundary;
- Apple-platform keyboard implementation;
- accepted runtime Privacy Shield, Wardveil Security, Everkeep, Manager, Mesh, Identity, Policy, or Observability integration;
- Production Acceptance, Release Candidate qualification, or Stable qualification.

## Maintenance rule

When an open capability becomes accepted on authoritative `main`, reconcile this file, `PLANNED-FEATURES.md`, and `CHANGELOGS.md` in the same evidence-backed workflow. Passing CI on an unmerged branch is not implementation authority.
