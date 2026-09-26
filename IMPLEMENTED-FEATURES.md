# GoreeCloud Keyboard — Implemented Features

**Record type:** Repository implemented-feature inventory  
**Repository:** `GoreeCloud/keyboard`  
**Lifecycle:** Weave / nonconformant; deployment state: development  
**Repository version:** `0.1.4-dev`  
**Migration state:** Complete on authoritative `main`; PR #79 merged as `6071bf3b7fddf36ddaa172b7ca858948f95ae6d1`, exact-main Android CI #264 passed, and the mapped legacy Drive roadmap/changelog sources were permanently retired and independently verified absent on September 22, 2026.  
**Current runtime-bearing baseline:** `64d5ed5b600e247630accceaa3f5ba8be26b3143`, merge of PR #93 on September 25, 2026; exact-main Android CI #294 / run `36196662346` passed.  
**Governing standard:** Standard — Repository Feature Tracking and Changelog Governance v1.0.

## Interpretation

This record describes capabilities accepted on authoritative `main`. It does not promote open Draft pull requests, historical Drive roadmap wording, or superseded Glaze adoption claims into current implementation state.

Authoritative `main` includes the repository-native governance migration at `6071bf3b7fddf36ddaa172b7ca858948f95ae6d1`; exact-main Android CI #264 / run `35758772834` passed on that revision. The current runtime-bearing capability baseline is `64d5ed5b600e247630accceaa3f5ba8be26b3143` from PR #93; exact-main Android CI #294 / run `36196662346` passed on that merge. Source/build/emulator success does not establish representative physical-device, production, Seal, or Anchor acceptance.

Draft PR #78 (`stabilize/touch-assistance-gap-compression-20260921`) and older open stacked Drafts remain candidate-only and are not represented as accepted behavior below.

## Implemented Weave-stage capabilities

### Native Android IME foundation
- First-party Android `InputMethodService` integration.
- First-party `KeyboardView` rendering, hit testing, pointer input, and Android input-method registration checks.
- Native QWERTY letters with an always-visible number row, Shift, Backspace, language-labeled Space, Enter, direct comma/period punctuation, and editor-action behavior.
- Android navigation-bar/system-gesture insets are reserved from the key interaction area so bottom-row controls do not extend beneath system navigation.
- CI/debug physical-test builds use a version-scoped Development package identity and the visible name **GoreeCloud Keyboard Dev**, so each CI-signed physical-test build can install alongside a preinstalled `com.goreecloud.keyboard` package and earlier Development builds.
- No Android network permission in the current application foundation.

### Local suggestions and gesture typing
- Packaged, local-only Quill English lexicon with frequency-ordered prefix suggestions, common derived forms, Unicode-aware typo correction, and conservative one-edit automatic correction at spaces and common punctuation boundaries.
- The strip stays empty at clean word boundaries instead of showing fixed generic starter words. Once a user types a non-empty ordinary-text prefix, Quill presents at least one and at most three candidates; the actively typed token remains visible as a fallback candidate even when the packaged dictionary has no better match. Sensitive/no-suggestion editor policy remains authoritative.
- First-party local swipe-typing gesture capture with a QWERTY geometry-aware on-device dictionary decoder and visible gesture trail.
- Swipe typing is disabled for sensitive editors and while touch exploration/screen-reader optimized presentation is active.
- Swipe decoding does not read surrounding editor text, persist gesture traces, learn from typing, use accounts/contacts/clipboard data, emit telemetry, or use network access.
- Suggestion candidates are presented in a single integrated Glaze-style candidate bar with one-to-three equal touch segments and subtle separators, avoiding oversized standalone pills.

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
- Sensitive-editor and host no-suggestions gating. Android no-personalized-learning requests remain non-learning while transient deterministic local candidates stay available.
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
- PR #77 historically accepted consistent Touch Assistance sizing for the earlier four-row surface. The current five-row letters layout requests a 320 dp ordinary preferred IME height and a 364 dp Touch Assistance preferred height while Android retains final measurement authority.
- Historical Experimental Glaze Motion evaluation remains test-only and is not a production dependency.

### Repository and platform control plane
- Platform Contract 2.0 declaration with current repository identity `GoreeCloud/keyboard`, lifecycle `weave`, deployment state `development`, qualification state `in-progress`, and next gate `seal`.
- Exactly nine Integral Platform Systems are declared: Manager, Privacy Shield, Wardveil Security, Everkeep, GLAZE UI, Mesh, Identity, Policy, and Observability.
- GLAZE UI is `applicable-migration-required`; the other currently applicable system integrations remain blocked pending accepted evidence.
- Android CI validates source/build/governance and an Android 15 emulator native-interaction/IME-activation path.

## Implemented-but-incomplete capability families

Draft PR #97 / 0.1.22-dev adds candidate-only recovery/onboarding after 0.1.21 was rejected on representative hardware because the launcher/setup app could stop immediately. The recovery adds forced first-launch ActivityScenario coverage, an Android 12/API 31 launch/process/IME lane, and fail-closed enabled/default IME status reads in addition to the Android 15 checks. It also carries forward the Keyboard-side Clipboard foundations. The existing launcher settings activity now provides a first-run wizard that verifies the exact installed Development IME enable/selection state and lets the user choose initial typing, gesture, layout, feedback, optional local-learning, Clipboard-history, and expiration preferences; setup completion is stored only after Finish and the wizard can be rerun later. The IME now keeps a stable `KeyboardInputSurfaceHost` root so Keyboard, Clipboard, and local edit surfaces swap inside one Android-owned input root instead of replacing it.

The candidate Clipboard surface includes tap-to-paste; Paste Once; dedicated Pinned and Recent collections; pin/unpin; long-press management; encrypted saved-clip editing; delete; clear-unpinned; 10-minute/1-hour/24-hour expiration; Android-sensitive non-persistence; and current-app Allow/Ask/Paste-only/Block policy. Local smart-content extraction can expose reusable phone numbers, email addresses, web links, street addresses, dates, and times without separately persisting the derived fragments. Clipboard payloads remain excluded from backup, portable preference export, synchronization, prediction, correction, personalization, and learning. This is not accepted on authoritative `main` and does not establish the privileged Secure Paste Broker or system-wide cross-app enforcement.

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
- the configurable utility-toolbar Draft stacks;
- accepted Arabic/multilingual layout work from Draft PR #65 or its stacked successors;
- system-wide GoreeCloud Secure Paste enforcement;
- privacy-approved voice input or translation;
- one-handed, floating, split, tablet, foldable, or posture-aware production layouts;
- broader Quill writing assistance beyond the local suggestion boundary;
- Apple-platform keyboard implementation;
- accepted runtime Privacy Shield, Wardveil Security, Everkeep, Manager, Mesh, Identity, Policy, or Observability integration;
- production acceptance, Seal qualification, or Anchor qualification.

## Maintenance rule

When an open capability becomes accepted on authoritative `main`, reconcile this file, `PLANNED-FEATURES.md`, and `CHANGELOGS.md` in the same evidence-backed workflow. Passing CI on an unmerged branch is not implementation authority.
