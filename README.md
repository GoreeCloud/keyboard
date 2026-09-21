# GoreeCloud Keyboard

GoreeCloud Keyboard is an original, native GoreeCloud keyboard implementation. The current implemented platform is Android through a first-party input-method service and rendering surface. Apple-platform support remains product direction and is not claimed as currently implemented.

## Current Development foundation

The current Android source includes:

- Android `InputMethodService` integration and runtime registration checks through `InputMethodManager`.
- A first-party `KeyboardView` rendering, hit-testing, and pointer-input surface.
- Core QWERTY input, shift, backspace, space, and enter actions.
- First-party letters-and-symbols switching with `?123` / `ABC`, plus a second `=\\<` symbol page for brackets, operators, currency marks, and common typographic symbols.
- A bounded first-party emoji layer that routes complete Unicode text through the existing String-safe input contract and includes deterministic Smileys, People, Nature, Food, Symbols, and Travel categories.
- A compact emoji category strip with complete accessibility announcements, a bounded local Recent category, explicit Clear behavior, and device-local private persistence for up to 24 exact emoji String values.
- Fully offline emoji search over only the packaged first-party catalog. Search state is transient, bounded, cleared on close, cannot be preloaded while closed, and is never sent through `InputConnection` or Quill composition.
- Bounded Unicode-aware backspace for common emoji modifiers, ZWJ-linked emoji, regional-indicator flags, keycaps, variation selectors, combining marks, and CRLF. Ambiguous/truncated ordinary-field look-behind fails closed; sensitive editors retain one-code-point deletion without text look-behind.
- Deterministic local long-press alternates for common Latin diacritics and punctuation, with viewport-bounded popup geometry, pointer selection, cancellation, haptic feedback, and fail-closed hit testing.
- A local GoreeCloud Quill suggestion boundary with deterministic prefix candidates, bounded typo correction, Unicode-code-point-aware one-edit correction, bounded transient capture, and suggestion commit authority bound to the exact candidates currently presented for the active editor session.
- Sensitive-editor and host no-suggestions privacy gating that suppresses suggestion capture, display, and acceptance, resets fail-closed at authoritative editor-session lifecycle boundaries, and keeps missing/no-active-editor state conservative.
- Privacy-by-default behavior with no Android network permission.
- A privacy-minimized `goreecloud-keyboard-preferences/1` format containing exactly the last explicitly selected emoji category, with validation/checksum integrity, explicit user-controlled Android Storage Access Framework import/export, import preview before write, export review/freeze before destination selection, and no generic preference serialization.
- Repository-local GLAZE UI V1.2 (`1.2.0`) Development source/material mapping retained as historical implementation evidence at `f285b9145e27e6e7027b075c37299d101945c272`; current Official Stable consumer authority is GLAZE UI V1.6 (`1.6.0`) at `a7180679ea851389e0f3004515f9a25f420e716d`, so runtime migration remains required.
- V1.2 neutral Frosted Neutral key material is consumed by the real native `KeyboardView`: neutral glass is the substrate and color remains accent/semantic rather than default material tint.
- V1.2 Light/Dark/Deep Dark source palettes, 4/8 dp spacing, 12 dp control radius role, 48/56 dp interaction floors, optical geometry references, and pressed/selected/focus state calibration are repository-local and test locked. Runtime appearance selection remains Android Light/Dark only until a separately reviewed Deep Dark policy exists.
- A V1.2 non-semantic atmosphere/material boundary that prohibits teal, green, aqua, amber, brand, or semantic color from defining the keyboard substrate and enables no editor/content sampling, remote derivation, persistence, semantic inference, telemetry, network lookup, or animated atmosphere.
- Explicit Manager, Privacy Shield, Wardveil Security, Everkeep, GoreeCloud Mesh, GoreeCloud Identity, GoreeCloud Policy, and GoreeCloud Observability acceptance boundaries.
- Android unit/build/governance and emulator validation infrastructure covering native registration, interaction, privacy lifecycle, emoji search, Unicode deletion, alternate popup geometry/hit testing, portability, and Glaze UI source boundaries.

The repository-local GLAZE UI V1.2 mapping remains historical Development/migration evidence only. Current Official Stable consumer authority is GLAZE UI V1.6 / `1.6.0`; Keyboard has not yet migrated its native presentation implementation to that line. `KeyboardView` currently selects Light/Dark from Android night mode; it does not infer or auto-select Deep Dark. V1.6 source migration plus complete component/state/material-role review, Reduced Motion/Transparency, Increased Contrast, forced-colors/native equivalents, 200% text/reflow, runtime Touch Assistance resolution, RTL/localization, TalkBack/Switch Access, representative phone/tablet/foldable and physical-device ergonomics, Human Visual Excellence, production signing/distribution, release, and Stable qualification remain separate gates.

## Product direction

GoreeCloud Keyboard is intended to become a beautiful, polished, feature-rich, privacy-first input platform while remaining fast, accessible, dependable, and native to supported platforms. Planned capability families include gesture typing, stronger local correction and dictionaries, multilingual input and language switching, richer emoji/symbol discovery, GoreeCloud Secure Paste, privacy-approved voice input, one-handed and split layouts, tablet/foldable adaptation, user dictionaries, Quill-assisted writing, and explicitly governed personalization/continuity features.

Feature richness must remain substantive. A feature is not considered implemented merely because a button, label, placeholder, or visual treatment exists; behavior, privacy/security boundaries, accessibility, tests, lifecycle integration, and appropriate runtime acceptance are required.

Private on-device recents are a convenience cache, not a general learned-language or usage-profile system. Any broader persistence, synchronization, backup, personalization, downloadable model, clipboard, voice, or remote-content behavior requires separate Privacy Shield, Wardveil Security, Everkeep, Identity, Mesh, user-control, retention, and implementation acceptance as applicable.

## Documentation

- `SPECIFICATIONS.md` — canonical repository product/source specification and acceptance boundaries.
- `FEATURES.md` — implemented versus planned capability inventory.
- `BENEFITS.md` — product benefits grounded in current architecture.
- `COMPETITIVE-OBJECTIVES.md` — product-quality objectives and evidence discipline.
- `USER-MANUAL.md` — current Development user guidance.
- `docs/native-architecture.md` — native implementation architecture.
- `docs/glaze-ui-adoption.md` — current GLAZE UI V1.2 source/material migration evidence and remaining gates.
- `docs/glaze-motion-evaluation.md` — historical/test-only Experimental Glaze Motion evaluation.
- `docs/development/` — bounded Development evidence and implementation notes.

## Development model

This repository contains original GoreeCloud-owned application code. Third-party libraries may be used only as narrowly scoped supporting dependencies where justified; they must not become the primary keyboard implementation.

## Status

**Development — native Android stabilization.** Source or CI validation does not by itself establish production acceptance, signed release, representative physical-device acceptance, Release Candidate status, or Stable qualification.
