# GoreeCloud Keyboard — Planned Features

**Record type:** Repository planned/open feature inventory  
**Repository:** `GoreeCloud/keyboard`  
**Lifecycle:** Weave / nonconformant; deployment state: development  
**Repository version:** `0.1.20-dev` Draft candidate; authoritative `main` remains separately governed.  
**Migration state:** Complete on authoritative `main`; PR #79 merged as `6071bf3b7fddf36ddaa172b7ca858948f95ae6d1`, exact-main Android CI #264 passed, and the mapped legacy Drive roadmap/changelog sources were permanently retired and independently verified absent on September 22, 2026.  
**Evidence baseline:** repository-native governance accepted on `main` at `6071bf3b7fddf36ddaa172b7ca858948f95ae6d1`; current runtime-bearing capability baseline is `64d5ed5b600e247630accceaa3f5ba8be26b3143` (PR #93), with exact-main Android CI #294 / run `36196662346` passed.  
**Governing standard:** Standard — Repository Feature Tracking and Changelog Governance v1.0.

## Purpose and migration sources

This file carries forward material planned, partial, blocked, deferred, acceptance-gated, or future capability obligations from:
- the retired former root `FEATURE-ROADMAP.md` preserved through Git history;
- retired legacy Drive `FEATURE-ROADMAP.docx` (`1D6Y8Tjv3fcqF6KMmcMKugqG7QmvmnzMt`), whose migrated dispositions are preserved below;
- current repository `README.md`, `FEATURES.md`, `SPECIFICATIONS.md`, `NOTES.md`, and Platform Contract evidence; and
- verified authoritative `main`.

The retired Drive roadmap's repository name `GoreeCloud/goreecloud-keyboard` was stale. Current repository identity is `GoreeCloud/keyboard`, as verified by live GitHub state and the accepted Platform Contract 2.0 declaration.

Open Draft pull requests remain candidate-only. In particular, PR #78 and the older stacked Draft feature lines are not implementation authority.

## Priority open obligations

### P0 — Sensitive-input privacy, security, and platform acceptance
- Complete accepted Privacy Shield integration appropriate to typed/editor data, sensitive fields, purpose limitation, minimization, and any future disclosure or persistence.
- Complete accepted Wardveil Security integration for any external content, models, dictionaries, clipboard/security flows, or other executable/unsafe input surfaces.
- Complete Everkeep backup/clean-target recovery scope without silently including typed or usage-derived history.
- Complete applicable Manager, Mesh, Identity, Policy, and Observability integration without expanding Keyboard input-data authority.
- Preserve no-network/local-first behavior unless a separately governed capability explicitly requires network access and passes privacy/security review.

### P0 — GLAZE UI V1.6 completion and accessibility
- Complete the remaining V1.6 optical/material/component migration; the accepted V1.6 presentation-context layer does not relabel the historical V1.2 optical substrate as V1.6-complete.
- Complete Reduced Motion, Reduced Transparency, Increased Contrast, forced-colors/native-equivalent behavior, large-text/reflow, Touch Assistance, RTL/localization, TalkBack, Switch Access, Voice Access where claimed, adaptive form factors, and Human Visual Excellence.
- Complete representative physical-device keyboard ergonomics and interaction acceptance across supported devices/hosts.

### P0 — Release and runtime acceptance
- Complete representative Android host/editor/OEM/version testing.
- Complete physical-device typing, long-press/slide/release, accessibility, latency, performance, power, and thermal acceptance.
- Complete controlled production signing and key custody, provenance, distribution/update, rollback/recovery, Seal qualification, production approval, and Anchor qualification on exact revisions.

## Product capability backlog

### Local typing quality and correction
Continue Unicode-safe input/deletion, Unicode-normalized matching, correction confidence, context-aware prediction, user dictionaries, broader language dictionaries, and privacy-minimized personalization without retaining typed content by default. Draft PR #97 now includes a 0.1.21 recovery candidate. It retains the frequency-ranked 46,691-word FrequencyWords/OpenSubtitles supplement, indexed suggestion/swipe lookup, coalesced rapid-typing suggestion refreshes, small-release-drift preservation, bounded key-gap recovery, and the unified functional icon system, while adding contraction-aware typo repair, one-edit missed-space recovery, canonical first-party dictionary priority, and explicit common hyphenated-compound suggestions. It still adds no network permission, remote dictionary lookup, Contacts access, voice, telemetry, or external-dictionary runtime authority. This remains candidate-only until accepted on authoritative main.

### Spacebar cursor control
The privacy-bounded cursor-control work represented by legacy FR-011 and Draft PRs #63/#64 is not accepted on current `main`. Draft PR #97 carries the 0.1.13 reimplementation, retained in the 0.1.21 candidate adapted to the current Keyboard runtime: one-finger horizontal Space drags emit bounded DPAD cursor steps without surrounding-text reads, ordinary Space taps are preserved, vertical/equal-axis and multi-pointer gestures fail closed, Emoji-layer and touch-exploration presentation disable the gesture, and a device-local default-on setting controls it. Pure policy and rendered Android runtime tests are included. The capability remains candidate-only until accepted on authoritative `main`, and representative physical-device, RTL/BiDi, accessibility, editor/OEM, and latency acceptance remain open.

### Multilingual input, language switching, and RTL/BiDi
Implement first-party multilingual layouts, explicit language switching, locale-aware typing, script-appropriate editing, and RTL/BiDi correctness. Draft PR #65 and its stacked successors remain candidate history rather than accepted current behavior.

### Gesture/swipe typing
A Weave-stage Development foundation now provides bounded local gesture capture and rendered-QWERTY geometry-aware packaged-dictionary decoding. Draft PR #97 further reworks physical swipe recognition using GoreeCloud-native adaptations of permissively licensed FlorisBoard and AnySoftKeyboard concepts, with exact provenance recorded in THIRD-PARTY-NOTICES.md. Gesture traces remain transient and non-networked; sensitive editors and touch-exploration presentation disable the path. Continue improving recognition quality, multilingual models, ambiguity handling, accessibility behavior, performance, physical-device ergonomics, and representative acceptance before any broader maturity claim.

### Emoji, symbols, alternates, and discovery
Expand the accepted local emoji/symbol/alternate foundations with broader Unicode/grapheme correctness, richer discovery, complete catalog/search/composition behavior, accessibility, and representative device acceptance.

### Configurable utility toolbar
A toolbar must expose only real underlying actions. The old Draft PR #60 stack is not current implementation authority. Draft PR #97 now carries a candidate toolbar exposing only real **Emoji**, **Clipboard**, and **Settings** actions; Symbols/Letters remain on the bottom-row mode key. The candidate uses a quiet Glaze toolbar surface with first-party rounded-outline glyphs, defaults to icons-only, preserves 48 dp toolbar interaction targets behind compact visual containers, and offers optional Icons + labels presentation. It remains candidate-only until accepted on authoritative main.

The Clipboard action now has a real Keyboard-side implementation rather than a placeholder: tap-to-paste, Paste Once, opt-in encrypted local history, dedicated Pinned and Recent collections, pin/unpin, long-press management, encrypted saved-clip editing, delete, clear-unpinned, 10-minute/1-hour/24-hour retention, Android-sensitive non-persistence, current-app Allow/Ask/Paste-only/Block policy, and local smart-content extraction for phone numbers, email addresses, web links, street addresses, dates, and times. Derived smart fragments are not separately persisted. The 0.1.21 recovery candidate also keeps the IME on a stable root surface after Clipboard/edit use and adds a first-run wizard for enabling/selecting the exact Development IME and choosing initial preferences. GIF/media insertion remains future work and still requires an explicit provider, network/retention behavior, search/content-safety boundary, user controls, and privacy/security acceptance.

### GoreeCloud Secure Paste
Draft PR #97 now implements the **Keyboard-side** portion of the intended Secure Paste experience, including an intentional paste surface and per-app local policy. The remaining system-wide obligation is still blocked on Privacy Shield authorization and a privileged GoreeCloud Secure Paste Broker capable of controlling cross-application clipboard authority outside the IME. A normal Android IME cannot revoke another application's clipboard API authority. The current encrypted history feature is separately governed, opt-in, device-local, excluded from backup/sync/learning, and must not be misrepresented as privileged Secure Paste enforcement.

### Voice input and translation
Add voice input and translation only with explicit provider/model authority, data-flow and retention rules, consent, offline/degraded behavior, privacy/security controls, and user control.

### Adaptive form factors
Add one-handed, floating, split, tablet, foldable, posture-aware, compact-width, orientation, and large-display layouts while preserving touch accuracy, reachability, accessibility, and native IME semantics.

### Broader GoreeCloud Quill assistance
Expand beyond the accepted local suggestion boundary only when editor-data scope, sensitive-field behavior, user control, privacy authorization, model/provider authority, retention, and degraded behavior are explicit and accepted.

### Portability, backup, and recovery
Preserve `goreecloud-keyboard-preferences/1` as an explicitly privacy-minimized format. Any schema expansion must be separately reviewed. Complete backup/restore/export, validation-before-write, review-before-destination, clean-target recovery, rollback, and Everkeep acceptance without silently adding typed content, surrounding context, learned input, emoji history, clipboard data, telemetry identifiers, credentials, or secrets.

### Apple-platform support
Treat Apple-platform keyboard support as product direction only until a separate native implementation, privacy model, packaging path, platform constraints, integrations, testing, and acceptance evidence exist.

## Legacy roadmap migration disposition

Every legacy Drive roadmap identifier is accounted for below.

| Legacy ID | Disposition under repository-native governance |
| --- | --- |
| FR-001 | Superseded as an active roadmap-control row by the maintenance rules in the three repository-native records; the obligation to keep feature state current remains. |
| FR-002 | Continues as GoreeCloud Tasks Management governance where work is actionable; it is not a product feature. |
| FR-003 | The evidence-backed lifecycle rule remains. Its repository/Drive synchronization requirement is superseded: Drive roadmap/changelog mirroring is prohibited after migration. |
| FR-010 | Implemented Weave-stage foundations are recorded in `IMPLEMENTED-FEATURES.md`; platform/privacy/production acceptance remains open here. |
| FR-011 | Remains open. Legacy Draft cursor-control candidates are not accepted current behavior. |
| FR-012 | Partial Weave-stage foundation implemented; broader local typing/correction quality remains open. |
| FR-013 | Remains open; Draft multilingual/Arabic work is candidate-only. |
| FR-014 | Remains planned. |
| FR-015 | Partial Development foundation implemented; richer discovery/correctness/acceptance remains open. |
| FR-016 | Remains open; the Draft toolbar stack is not accepted current behavior. |
| FR-017 | Partial Draft candidate implementation: Keyboard-side intentional paste, per-app local policy, Paste Once, encrypted temporary history, Pinned/Recent collections, saved-clip editing, and local smart-content extraction exist in PR #97; privileged Secure Paste Broker, Privacy Shield authorization, Wardveil acceptance, and system-wide enforcement remain blocked. |
| FR-018 | Remains planned. |
| FR-019 | Remains planned. |
| FR-020 | Partial local Quill foundation implemented; broader assistance remains open. |
| FR-021 | One-field portability foundation implemented; broader backup/recovery/Everkeep acceptance remains open. |
| FR-022 | Updated from the stale V1.3 target to current V1.6 authority. Partial V1.6 presentation context is implemented; full migration/acceptance remains open. |
| FR-023 | Updated from the stale smaller platform-system list to the current nine-system Platform Contract 2.0 model. GLAZE UI remains migration-required and the other applicable system integrations remain blocked. |
| FR-024 | Remains product direction only; no Apple implementation is claimed. |
| FR-025 | Remains the exact-revision representative-test/signing/recovery/release/production/Anchor gate. |

## Repository-governance obligations

- Do not recreate `FEATURE-ROADMAP.md`.
- Do not recreate, synchronize, mirror, back up, or maintain a Keyboard roadmap or changelog in Google Drive.
- Keep `IMPLEMENTED-FEATURES.md`, this file, and `CHANGELOGS.md` current in the same governed change when lifecycle state materially changes.
- Preserve historical Draft/candidate evidence without promoting it to accepted implementation.
- Keep repository identity `GoreeCloud/keyboard` current in active records and automation.

## Maintenance rule

A capability remains here until its defined implementation and acceptance scope is complete. When accepted on authoritative `main`, update `IMPLEMENTED-FEATURES.md`, reconcile/remove the corresponding active obligation here, and record the meaningful event in `CHANGELOGS.md`.
