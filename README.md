# GoreeCloud Keyboard

GoreeCloud Keyboard is an original, native GoreeCloud keyboard implementation. The current implemented platform is Android through a first-party input-method service and rendering surface. Apple-platform support remains product direction and is not claimed as currently implemented.

## Current Weave-stage Development foundation

The current Android source includes:

- Android `InputMethodService` integration and runtime registration checks through `InputMethodManager`.
- A first-party `KeyboardView` rendering, hit-testing, and pointer-input surface.
- Core QWERTY input, shift, backspace, space, enter, direct comma/period punctuation, and bottom safe-area handling for Android navigation/gesture insets.
- First-party letters-and-symbols switching with `?123` / `ABC`, plus a second `=\\<` symbol page for brackets, operators, currency marks, and common typographic symbols.
- A bounded first-party emoji layer that routes complete Unicode text through the existing String-safe input contract and includes deterministic Smileys, People, Nature, Food, Symbols, and Travel categories.
- A compact emoji category strip with complete accessibility announcements, a bounded local Recent category, explicit Clear behavior, and device-local private persistence for up to 24 exact emoji String values.
- Fully offline emoji search over only the packaged first-party catalog. Search state is transient, bounded, cleared on close, cannot be preloaded while closed, and is never sent through `InputConnection` or Quill composition.
- Bounded Unicode-aware backspace for common emoji modifiers, ZWJ-linked emoji, regional-indicator flags, keycaps, variation selectors, combining marks, and CRLF. Ambiguous/truncated ordinary-field look-behind fails closed; sensitive editors retain one-code-point deletion without text look-behind.
- Deterministic local long-press alternates for common Latin diacritics and punctuation, with viewport-bounded popup geometry, pointer selection, cancellation, haptic feedback, and fail-closed hit testing.
- A local GoreeCloud Quill suggestion boundary with frequency-ordered prefix candidates, a substantially broader packaged everyday-English dictionary, common derived forms and contractions, Unicode-aware typo correction, conservative automatic correction at word boundaries, bounded transient capture, starter predictions before typing, a one-to-three candidate contract, typed-token fallback, and suggestion commit authority bound to the exact candidates currently presented for the active editor session.
- A bounded local swipe-typing Development path using transient QWERTY gesture geometry and packaged-dictionary ranking rather than literal crossed-key text, with a visible gesture trail, a deliberate minimum travel/time/path threshold to reduce accidental swipes during fast tapping, tolerant-but-bounded endpoint drift for more realistic gestures, and sensitive-editor/touch-exploration suppression.
- The current 0.1.7 Development candidate uses the version-scoped package `com.goreecloud.keyboard.dev.v8` and visible name **GoreeCloud Keyboard Dev 0.1.7**, so it installs alongside a preinstalled `com.goreecloud.keyboard` package and earlier Development builds instead of attempting an incompatible signature update.
- A launcher-visible **GoreeCloud Keyboard** settings application with a Glaze card-based hierarchy plus an in-keyboard Settings control. Device-local settings currently cover swipe typing, suggestions, autocorrect, next-word predictions, automatic capitalization, and three visible key-height choices: Compact, Standard, and Tall. Full touch targets remain preserved behind the visual keycap sizes.
- A built-in read-only GoreeCloud dictionary derived from the canonical branding catalog plus a broader everyday-English supplement, canonical product/system names, and common English contractions. It does not learn from typed text.
- A bounded local Quill prediction/grammar layer that presents three starter predictions before typing, can present up to three next-word predictions from the current transient editor session, prioritizes likely spelling corrections in the candidate strip, applies conservative boundary autocorrection, restores common apostrophes, preserves canonical GoreeCloud casing, and automatically capitalizes sentence starts without persistent language-history storage.
- A dedicated real-action utility toolbar above the number row exposes Emoji, Symbols/Letters, Settings, and Hide Keyboard. Clipboard and GIF controls are intentionally not exposed as fake buttons while their governed providers/authority remain unimplemented.
- Backspace uses bounded Unicode-aware deletion when editor context is available and falls back to the standard Android DEL key event when an editor does not support the preferred deletion API, improving compatibility without expanding text-read authority.
- Sensitive-editor and host no-suggestions privacy gating that suppresses suggestion capture, display, and acceptance, resets fail-closed at authoritative editor-session lifecycle boundaries, and keeps missing/no-active-editor state conservative. Android no-personalized-learning requests remain compatible with deterministic transient suggestions because the current Quill path does not persist a learned user model.
- Privacy-by-default behavior with no Android network permission.
- A privacy-minimized `goreecloud-keyboard-preferences/1` format containing exactly the last explicitly selected emoji category, with validation/checksum integrity, explicit user-controlled Android Storage Access Framework import/export, import preview before write, export review/freeze before destination selection, and no generic preference serialization.
- Historical GLAZE UI V1.2 (`1.2.0`) Development source/material mapping pinned to reviewed source `f285b9145e27e6e7027b075c37299d101945c272`; current governed consumer target is GLAZE UI V1.6 / `1.6.0` at `a7180679ea851389e0f3004515f9a25f420e716d`.
- V1.2 neutral Frosted Neutral key material is consumed by the real native `KeyboardView`: neutral glass is the substrate and color remains accent/semantic rather than default material tint.
- V1.2 Light/Dark/Deep Dark source palettes, 4/8 dp spacing, 12 dp control radius role, 48/56 dp interaction floors, optical geometry references, and pressed/selected/focus state calibration are repository-local and test locked. Runtime appearance selection remains Android Light/Dark only until a separately reviewed Deep Dark policy exists.
- A V1.2 non-semantic atmosphere/material boundary that prohibits teal, green, aqua, amber, brand, or semantic color from defining the keyboard substrate and enables no editor/content sampling, remote derivation, persistence, semantic inference, telemetry, network lookup, or animated atmosphere.
- Explicit Wardveil Security, Privacy Shield, Everkeep, GoreeCloud Identity, GoreeCloud Mesh, and Manager acceptance boundaries.
- Android unit/build/governance and emulator validation infrastructure covering native registration, interaction, privacy lifecycle, emoji search, Unicode deletion, alternate popup geometry/hit testing, portability, and Glaze UI source boundaries.

The historical GLAZE UI V1.2 mapping remains migration evidence only. Current Official Stable consumer authority is V1.6 / `1.6.0`; Keyboard has not completed that migration. `KeyboardView` currently selects Light/Dark from Android night mode; it does not infer or auto-select Deep Dark. Complete current-Stable component/state/material-role migration across all surfaces, Reduced Motion/Transparency, Increased Contrast, forced-colors/native equivalents, 200% text/reflow, runtime Touch Assistance resolution, RTL/localization, TalkBack/Switch Access, representative phone/tablet/foldable and physical-device ergonomics, Human Visual Excellence, production signing/distribution, Seal qualification, production acceptance, and Anchor qualification remain separate gates.

## Product direction

GoreeCloud Keyboard is intended to become a beautiful, polished, feature-rich, privacy-first input platform while remaining fast, accessible, dependable, and native to supported platforms. Planned capability families include stronger gesture-recognition quality, stronger local correction and dictionaries, multilingual input and language switching, richer emoji/symbol discovery, GoreeCloud Secure Paste, privacy-approved voice input, one-handed and split layouts, tablet/foldable adaptation, user dictionaries, Quill-assisted writing, and explicitly governed personalization/continuity features.

Feature richness must remain substantive. A feature is not considered implemented merely because a button, label, placeholder, or visual treatment exists; behavior, privacy/security boundaries, accessibility, tests, lifecycle integration, and appropriate runtime acceptance are required.

Private on-device recents are a convenience cache, not a general learned-language or usage-profile system. Any broader persistence, synchronization, backup, personalization, downloadable model, clipboard, voice, or remote-content behavior requires separate Privacy Shield, Wardveil Security, Everkeep, Identity, Mesh, user-control, retention, and implementation acceptance as applicable.

## Documentation

- `SPECIFICATIONS.md` — canonical repository product/source specification and acceptance boundaries.
- `FEATURES.md` — implemented versus planned capability inventory.
- `BENEFITS.md` — product benefits grounded in current architecture.
- `COMPETITIVE-OBJECTIVES.md` — product-quality objectives and evidence discipline.
- `USER-MANUAL.md` — current Development user guidance.
- `docs/native-architecture.md` — native implementation architecture.
- `docs/glaze-ui-adoption.md` — historical V1.2 source mapping, current V1.6 migration authority, and remaining gates.
- `docs/glaze-motion-evaluation.md` — historical/test-only Experimental Glaze Motion evaluation.
- `docs/development/` — bounded Development evidence and implementation notes.

## Development model

This repository contains original GoreeCloud-owned application code. Third-party libraries may be used only as narrowly scoped supporting dependencies where justified; they must not become the primary keyboard implementation.

## Status

**Weave — native Android stabilization and hardening; deployment state: development.** Source or CI validation does not by itself establish production acceptance, signed release, representative physical-device acceptance, Seal status, or Anchor qualification.
