# GoreeCloud Keyboard Specifications

## Product status

**Development — native Android stabilization.**

GoreeCloud Keyboard is an original GoreeCloud-owned keyboard implementation. The current implemented platform is Android through `InputMethodService` and a first-party rendering/input surface. Apple-platform support remains product direction and is not claimed as currently implemented.

GoreeCloud Keyboard must be beautiful, polished, cohesive, responsive, feature-rich, fast, private, accessible, and dependable. It must be built and designed against the latest current Stable GLAZE UI release while preserving native platform semantics, privacy/security authority boundaries, and truthful implementation status.

## Current implemented scope

- Native Android `InputMethodService` integration and declarative/runtime input-method registration checks.
- First-party `KeyboardView` rendering and pointer-input handling.
- QWERTY letters with shift, backspace, space, and enter.
- First-party letters/symbols mode switching with `?123`, `ABC`, and `=\\<` controls.
- Primary symbol page with digits and common punctuation plus a secondary first-party page with brackets, operators, currency marks, and common typographic symbols.
- Bounded first-party emoji input using complete Unicode `String` payloads, deterministic Smileys, People, Nature, Food, Symbols, and Travel categories, local Recent state, explicit Clear behavior, and bounded device-local private persistence.
- Fully offline emoji search over only the packaged first-party catalog. Search query state is transient, bounded, cleared on close, cannot be mutated while closed, and is not sent through the active editor or Quill composing context.
- Deterministic local long-press key alternates with viewport-bounded popup layout, exact shared render/hit-test geometry, pointer movement selection, cancellation, haptic feedback, and accessibility announcements.
- Local-only GoreeCloud Quill suggestion boundary with deterministic prefix suggestions, bounded typo-correction candidates, Unicode-code-point-aware correction, bounded transient capture, and commit authority restricted to the exact candidates currently presented for the active editor session.
- Sensitive-editor classification, host no-suggestions policy, and fail-closed editor lifecycle handling that clear composing/suggestion state and prevent ordinary-field authority from leaking across editor transitions or no-active-editor states.
- Bounded Unicode-aware backspace for common emoji modifiers, ZWJ-linked emoji, regional-indicator flags, keycaps, variation selectors, combining marks, and CRLF. Ambiguous truncated ordinary-field look-behind fails closed; sensitive editors retain one-code-point deletion without text look-behind.
- No Android network permission in the current application foundation.
- A privacy-minimized `goreecloud-keyboard-preferences/1` format containing exactly the last explicitly selected emoji category, with strict validation/checksum integrity, explicit user-controlled Storage Access Framework import/export, preview-before-write import, review/freeze-before-destination export, and no generic preference serialization.
- Historical GLAZE UI V1.2 (`1.2.0`) Development source/material mapping pinned to reviewed source `f285b9145e27e6e7027b075c37299d101945c272`; current governed consumer target is GLAZE UI V1.6 / `1.6.0` at exact Stable release source `a7180679ea851389e0f3004515f9a25f420e716d`.
- A bounded V1.6 Android presentation-context resolver now maps Android-owned font scale, animator enablement, and touch-exploration state into large-text, Reduced/Minimal Motion, screen-reader/touch-assistance, and 48/56 dp target behavior without observing editor content or expanding input-data authority.
- V1.2 Frosted Neutral key material is consumed by the native `KeyboardView`; neutral glass is the material and color remains an accent rather than a default substrate tint.
- V1.2 Light/Dark/Deep Dark source palettes, 4/8 dp spacing, 12 dp control radius, 48/56 dp target floors, optical geometry references, and pressed/selected/focus state calibration are repository-local and unit-tested. Runtime appearance selection remains Light/Dark from Android night mode only.
- A V1.2 non-semantic atmosphere/material boundary prohibits chromatic, brand, or semantic color from defining the keyboard substrate and enables no editor/content sampling, remote color derivation, persistent sample history, semantic inference, telemetry, network lookup, or animated atmosphere.
- Experimental Glaze Motion evaluation remains historical/test-only and is not a production dependency or V1.2 acceptance source.
- Android unit/build/governance and emulator validation infrastructure covers registration, native interaction, editor privacy lifecycle, emoji search, Unicode deletion, suggestion authority, portable preferences, Glaze UI mapping, and alternate-popup geometry/hit testing.

## Native input behavior

### Letters layer

The default layer exposes QWERTY alphabetic rows. Shift affects alphabetic output only and automatically returns to the unshifted state after a shifted character is committed.

### Primary symbols layer

Tap `?123` from letters to open the primary symbol page. It exposes digits and common punctuation. `ABC` returns directly to letters, while `=\\<` opens the secondary symbol page.

### Secondary symbols layer

The secondary page exposes additional brackets, mathematical/operator punctuation, path/separator characters, currency marks, and common typographic symbols. `ABC` returns directly to letters and `?123` returns directly to the primary symbol page.

Entering any non-letter layer clears the current suggestion composition buffer. Non-letter symbol input does not become Quill suggestion context.

### Emoji layer

Emoji input uses complete `String` payloads. The current local catalog is packaged with the application and requires no network lookup. Category navigation, recents, search, and deletion behavior remain first-party and local. Search query text is input-navigation state rather than editor text and is never committed unless the user explicitly chooses an emoji result.

### Long-press alternates

Eligible letter and punctuation keys expose deterministic local alternates after the platform long-press timeout. Popup placement and active pointer selection share one viewport-bounded geometry result. Invalid layouts, gaps, outside points, non-finite coordinates, unused cells, and stale/missing layout state fail closed to no alternate commit.

### Editor transitions

Authoritative Android editor-session start/finish callbacks reset transient privacy/suggestion state. Missing or no-active-editor metadata remains sensitive and suggestions-suppressed. Ordinary editors can relax that policy only through a concrete editor transition. Host `TYPE_TEXT_FLAG_NO_SUGGESTIONS` is honored without misclassifying the field as a password.

### Suggestion authority

Transient suggestion capture is bounded. A capture that becomes incomplete or ambiguous remains suppressed until a clean word/editor boundary rather than beginning a misleading mid-word context. Suggestion callbacks are not accepted by value alone: the selected value must be one of the exact candidates currently presented for the active editor and must still satisfy host-prefix validation before replacement.

## Privacy and security boundaries

### Privacy Shield / Privacy Center

Keyboard input is highly sensitive. Current source minimizes observation, keeps ordinary suggestion work local, treats sensitive/no-active-editor contexts fail closed, and excludes typed text, composing/surrounding editor context, suggestions/learned input, emoji recents/frequency history, emoji search queries, clipboard data, key history, sensitive-editor contents, telemetry, Identity data, credentials, and cryptographic secrets from the portable preference format.

GLAZE UI presentation adds no observation authority. No production claim is made for future cloud-assisted input, clipboard history, voice input, synchronization, or account-backed personalization until explicit Privacy Shield policy, consent, retention, user-control, implementation, and runtime acceptance exist.

### GoreeCloud Secure Paste — planned

GoreeCloud Keyboard is the planned primary user-facing IME surface for GoreeCloud Secure Paste. The intended architecture replaces passive cross-application clipboard reads with explicit user-mediated paste through a privileged Android/framework Secure Paste Broker governed by Privacy Shield. A normal Android IME cannot globally revoke another application's clipboard API authority and must not be represented as doing so.

Planned behavior includes a dedicated Paste action, per-application clipboard policy, optional expiration and Paste Once, stricter sensitive-item handling, minimized non-content evidence, Wardveil review for dangerous context-dependent payloads, and no clipboard payload backup/synchronization/Identity association by default. This capability remains Planned / Proposed until the privileged system enforcement path and applicable acceptance evidence exist.

### Wardveil Security / Security Center

The application does not treat UI state as security authority. Future downloadable dictionaries, models, themes, clipboard integrations, voice/input adapters, or other external content require explicit Wardveil validation appropriate to the object and execution boundary before production use. Secure Paste should allow context-aware protection against dangerous clipboard payloads without silently rewriting ordinary user text outside a documented security rule.

### Everkeep / Continuity Center

The current one-field portable preference codec plus bounded category-only local export/apply seams are Development portability primitives only. They are not complete Keyboard backup, clean-target recovery, synchronization, or Everkeep acceptance. Emoji recents and other usage-derived history are not silently included in portability or recovery scope.

### GoreeCloud Identity / Identity Center

The current native keyboard foundation does not require a GoreeCloud account to type or use the local one-field transfer flow. Future account-backed personalization or synchronization must use GoreeCloud Identity for authentication/authorization and must not expose credential material to the input surface.

### GoreeCloud Mesh / Mesh Center

Current local typing does not depend on Mesh availability. Future cross-application/service capabilities must use governed GoreeCloud integration contracts rather than hidden coupling. Clipboard payloads, typed text, suggestion context, and other sensitive input data must not be transported over Mesh merely to establish integration or dashboard state.

### GoreeCloud Manager / Management Center

Keyboard may publish minimized operational/version/capability state to Manager when an accepted integration exists. Manager does not gain typed-text, editor-content, clipboard-payload, suggestion-history, or other sensitive input authority merely because it manages application lifecycle/configuration.

## GLAZE UI / Design Center

Current Official Stable GoreeCloud design-system authority is GLAZE UI V1.6 / `1.6.0` at exact release source `a7180679ea851389e0f3004515f9a25f420e716d`. Keyboard now contains a bounded V1.6 Android presentation-context resolver and consumes it in the native `KeyboardView` for Android-owned accessibility/motion signals and interaction sizing. The underlying Frosted Neutral material/color/token mapping remains historical V1.2 / `1.2.0` at reviewed source `f285b9145e27e6e7027b075c37299d101945c272`; this partial migration is Development evidence only and is not complete V1.6 consumer acceptance.

The governing V1.2 material rule is **Neutral glass is the material. Color is an accent.** `KeyboardView` now consumes neutral V1.2 base-glass surfaces for Light/Dark runtime rendering. Deep Dark is source-defined but not automatically selected from ordinary Android dark mode. The source also records V1.2 state calibration for pressed/selected/focus behavior without representing that source map as complete rendered conformance.

Accessibility directives outrank cosmetic material behavior. Reduced Transparency, Reduced Motion, Increased Contrast, forced-colors/native equivalents, focus visibility, content legibility, target size, and task completion must remain valid even when blur/translucency/advanced effects are unavailable. The keyboard must not become dependent on nested backdrop blur or atmosphere for input correctness.

Local emoji search remains an application-local input-navigation capability and is not GoreeCloud Universal Search. Long-press alternates are transient application interaction, not Control Center. Glaze presentation semantics do not grant Universal Search, Control Center, security, privacy, identity, recovery, or other platform authority to Keyboard.

Historical Glaze Motion 0.5 evaluation remains test-only and provides no current V1.2 production/conformance evidence.

## Product-quality direction

GoreeCloud Keyboard must evolve as a complete first-party input product rather than a minimal demo. Subject to separate implementation and acceptance, target capability families include:

- stronger local autocorrect, prediction, and user/language dictionaries;
- multilingual layouts and explicit language switching;
- gesture/swipe typing;
- richer emoji, symbol, kaomoji, and specialized input discovery;
- GoreeCloud Secure Paste and privacy-governed clipboard tools;
- privacy-approved voice input/adapters;
- one-handed and split layouts;
- tablet/foldable/posture-aware layouts;
- hardware-keyboard and accessibility-aware workflows where applicable;
- Quill-assisted writing through privacy-preserving boundaries;
- user-controlled appearance and input preferences;
- explicitly governed portability, backup, recovery, and optional synchronization.

Feature richness must remain substantive. Buttons, placeholders, labels, decorative surfaces, or roadmap statements are not implementation evidence. Each capability requires functional behavior, native lifecycle integration, privacy/security boundaries, accessibility, testing, and appropriate runtime/release acceptance.

## Production and Stable acceptance gates

Development source or passing CI is not equivalent to production acceptance or Stable qualification. Production promotion requires evidence appropriate to the shipped platform, including:

- exact-revision source/build/test validation;
- complete GLAZE UI V1.6 consumer migration for applicable keyboard and settings surfaces beyond the bounded runtime-context foundation;
- Reduced Transparency, Reduced Motion, Increased Contrast, forced-colors/native equivalents, large text/reflow, RTL/localization, and Touch Assistance behavior;
- TalkBack, Switch Access, and other claimed assistive-input acceptance;
- representative Android host-editor compatibility;
- representative phone/tablet/foldable layout and ergonomics acceptance;
- representative physical-device typing, long-press/slide/release, latency, performance, power, and thermal acceptance;
- Human Visual Excellence review of the actual Keyboard consumer;
- Privacy Shield and Wardveil Security acceptance appropriate to a sensitive input surface;
- approved Everkeep continuity/recovery scope and clean-target recovery evidence where required;
- applicable Manager/Mesh/Identity integrations without expanding input-data authority;
- protected signing, provenance, distribution, update, rollback, and recovery procedures;
- governed Release Candidate validation; and
- explicit production/Stable approval.

Until those gates are satisfied, GoreeCloud Keyboard remains Development and must not be represented as Stable.
