# GoreeCloud Keyboard Specifications

## Product status

**Development — native Android stabilization.**

GoreeCloud Keyboard is an original GoreeCloud-owned keyboard implementation. The current implemented platform is Android through `InputMethodService` and a first-party rendering/input surface. Apple-platform support remains product direction and is not claimed as currently implemented.

GoreeCloud Keyboard must be beautiful, polished, cohesive, responsive, feature-rich, fast, private, accessible, and dependable. It must be built and designed against the latest applicable Stable GLAZE UI authority while preserving native platform semantics, privacy/security authority boundaries, and truthful implementation status.

## Current implemented scope

- Native Android `InputMethodService` integration and declarative/runtime input-method registration checks.
- First-party `KeyboardView` rendering and pointer-input handling.
- QWERTY letters with shift, backspace, space, and adaptive editor action.
- A dedicated 1–0 number row above QWERTY on the letters layer. It is enabled by default and can be disabled/re-enabled from the Android IME settings surface through a device-local Boolean presentation preference.
- A configurable utility toolbar rendered above the suggestion/navigation strip. The bounded current toolbar exposes only implemented real actions: Emoji, Symbols, and Keyboard Settings. Its master visibility and each action are controlled by device-local Boolean presentation preferences. A configuration with no enabled actions collapses the toolbar rather than rendering empty or placeholder controls.
- Toolbar Emoji and Symbols actions reuse the existing layer-switching authority; Settings delegates through `KeyboardView.Listener` to `KeyboardService`, which opens the existing explicit `KeyboardPortablePreferencesActivity`. The view does not gain activity, editor, clipboard, storage, account, or network authority merely because the toolbar can request Settings.
- Adaptive action-key presentation for ordinary Enter plus Android Go, Search, Send, Next, Done, and Previous editor semantics. Explicit host actions use `InputConnection.performEditorAction()`; ordinary Enter remains the fallback when no explicit action applies or the host does not handle it.
- First-party letters/symbols mode switching with `?123`, `ABC`, and `=\\<` controls.
- Primary symbol page with digits and common punctuation plus a secondary first-party page with brackets, operators, currency marks, and common typographic symbols.
- Bounded first-party emoji input using complete Unicode `String` payloads, deterministic Smileys, People, Nature, Food, Symbols, and Travel categories, local Recent state, explicit Clear behavior, bounded device-local private persistence, and fully offline emoji search over the packaged catalog.
- Deterministic local long-press key alternates with viewport-bounded popup layout, exact shared render/hit-test geometry, pointer movement selection, cancellation, haptic feedback, and accessibility semantics.
- Local-only GoreeCloud Quill suggestion boundary with deterministic prefix suggestions, bounded typo-correction candidates, Unicode-code-point-aware correction, bounded transient capture, and commit authority restricted to exact candidates presented for the active editor session.
- Sensitive-editor classification, host no-suggestions policy, and fail-closed editor lifecycle handling that clear composing/suggestion state and prevent ordinary-field authority from leaking across editor transitions or no-active-editor states.
- Bounded Unicode-aware backspace for common emoji modifiers, ZWJ-linked emoji, regional-indicator flags, keycaps, variation selectors, combining marks, and CRLF. Ambiguous truncated ordinary-field look-behind fails closed; sensitive editors retain one-code-point deletion without text look-behind.
- No Android network permission in the current application foundation.
- A privacy-minimized `goreecloud-keyboard-preferences/1` format containing exactly the last explicitly selected emoji category, with strict validation/checksum integrity, explicit user-controlled Storage Access Framework import/export, preview-before-write import, review/freeze-before-destination export, and no generic preference serialization.
- A separate device-local presentation store containing only the number-row Boolean and utility-toolbar visibility/action Booleans. It is intentionally outside the portable preference format and must not contain typed text, composing context, suggestions, clipboard contents, credentials, editor content, or usage-derived history.
- GLAZE UI V1.3 / `1.3.0` Development source/material mapping pinned to exact Stable integration revision `fc7cc91d2eace8da2371371c2855c24cbcb326a1` through the current stacked migration work.
- Inherited Frosted Neutral Light/Dark/Deep Dark source palettes, 4/8 dp spacing, 12 dp control-radius role, 48/56 dp interaction floors, optical geometry references, and deterministic pressed/selected/focus behavior. Runtime appearance selection remains Android Light/Dark only until separately accepted policy exists for additional modes.
- V1.3 Adaptive Resonance authority boundaries that keep typed/editor content outside color authority and enable no environmental memory, remote derivation, persistent sample history, semantic inference, telemetry, network lookup, or animated atmosphere.
- Experimental Glaze Motion evaluation remains historical/test-only and is not a production dependency or current Stable acceptance source.
- Android unit/build/governance and emulator validation infrastructure covers registration, native interaction, editor privacy lifecycle, emoji search, Unicode deletion, suggestion authority, portable preferences, accessibility semantics, Glaze UI mapping, alternate-popup geometry/hit testing, the dedicated number row, configurable utility toolbar, and adaptive action presentation.

## Native input behavior

### Letters layer

The default layer exposes an optional dedicated number row followed by QWERTY alphabetic rows. The number row defaults to enabled. Shift affects alphabetic output only and automatically returns to the unshifted state after a shifted alphabetic character is committed.

The number-row preference is a presentation choice. It does not grant additional editor observation authority and is not included in the current portable preference format.

### Configurable utility toolbar

When enabled with at least one selected action, the utility toolbar renders a separate Glaze-sized interaction row above the existing suggestion/navigation strip. The row is available across the current keyboard layers so its geometry does not disappear merely because the user switches from letters to symbols or emoji.

The current bounded action set is deliberately limited to capabilities that already exist:

- **Emoji** routes through the existing first-party emoji layer and closes a transient emoji-search query before returning to normal emoji navigation when necessary.
- **Symbols** routes through the existing primary symbols layer.
- **Keyboard Settings** requests the existing native settings activity through the IME service.

Emoji and Symbols expose selected-state semantics when their corresponding layer is active. All toolbar controls use the same native virtual-accessibility-control system as the rest of the custom-drawn keyboard, with resource-backed spoken labels. No toolbar action is a placeholder for Secure Paste, GIF/sticker content, voice, translation, Launcher, Search, or another capability that has not yet been implemented under its governing authority.

The toolbar's master visibility plus its three current action-visibility choices are low-sensitivity device-local presentation state. They are not included in `goreecloud-keyboard-preferences/1`, do not record toolbar use history, and do not authorize editor-content inspection, clipboard access, telemetry, account state, synchronization, or network access.

### Editor action key

The right-side action key reflects host `EditorInfo.imeOptions` when Android supplies a supported explicit action. Supported semantic presentations are Go, Search, Send, Next, Done, and Previous. Fields that do not supply an applicable explicit action use ordinary Enter.

The visual label and accessibility label are separate where useful; for example, Search may use a compact search glyph while the virtual accessibility control is labeled “Search.” `IME_FLAG_NO_ENTER_ACTION` forces the ordinary Enter path rather than manufacturing an explicit action.

### Primary symbols layer

Tap `?123` from letters or the utility toolbar's Symbols action to open the primary symbol page. It exposes digits and common punctuation. `ABC` returns directly to letters, while `=\\<` opens the secondary symbol page.

### Secondary symbols layer

The secondary page exposes additional brackets, mathematical/operator punctuation, path/separator characters, currency marks, and common typographic symbols. `ABC` returns directly to letters and `?123` returns directly to the primary symbol page.

Entering any non-letter layer clears the current suggestion composition buffer. Non-letter symbol input does not become Quill suggestion context.

### Emoji layer

Emoji input uses complete `String` payloads. The current local catalog is packaged with the application and requires no network lookup. Category navigation, recents, search, and deletion behavior remain first-party and local. Search query text is input-navigation state rather than editor text and is never committed unless the user explicitly chooses an emoji result.

### Long-press alternates

Eligible letter and punctuation keys expose deterministic local alternates after the platform long-press timeout. Popup placement and active pointer selection share one viewport-bounded geometry result. Invalid layouts, gaps, outside points, non-finite coordinates, unused cells, and stale/missing layout state fail closed to no alternate commit.

### Editor transitions

Authoritative Android editor-session start/finish callbacks reset transient privacy/suggestion state and the adaptive action presentation. Missing or no-active-editor metadata remains sensitive and suggestions-suppressed. Ordinary editors can relax that policy only through a concrete editor transition. Host `TYPE_TEXT_FLAG_NO_SUGGESTIONS` is honored without misclassifying the field as a password.

### Suggestion authority

Transient suggestion capture is bounded. A capture that becomes incomplete or ambiguous remains suppressed until a clean word/editor boundary rather than beginning a misleading mid-word context. Suggestion callbacks are not accepted by value alone: the selected value must be one of the exact candidates currently presented for the active editor and must still satisfy host-prefix validation before replacement.

## Inspiration-driven product direction

The Android-keyboard references supplied and approved on 2026-09-08 are inspiration inputs, not third-party designs to reproduce. GoreeCloud Keyboard must remain an original first-party Glaze-native experience.

The approved direction includes:

- spacious rounded keys and strong touch targets;
- a suggestion strip above the key field;
- optional dedicated number row;
- a configurable real-action utility toolbar;
- adaptive action key;
- direct symbols, emoji, and long-press alternates;
- Light/Dark system-adaptive presentation;
- future Secure Paste/clipboard tools, GIF/sticker discovery, voice, translation, gesture typing, cursor/selection gestures, multilingual layouts, richer correction/dictionaries, manual appearance controls, one-handed/floating/split/adaptive form-factor layouts, broader Quill assistance, and additional governed GoreeCloud ecosystem actions.

The dedicated number row, adaptive action key, and bounded configurable utility toolbar are implemented in the current Development stack. Existing local suggestions, Glaze key geometry, Android Light/Dark adaptation, symbols, emoji, and long-press alternates also support the direction. The remaining items are active planned obligations and must not be represented as implemented through placeholder toolbar buttons or decorative surfaces.

## Privacy and security boundaries

### Privacy Shield / Privacy Center

Keyboard input is highly sensitive. Current source minimizes observation, keeps ordinary suggestion work local, treats sensitive/no-active-editor contexts fail closed, and excludes typed text, composing/surrounding editor context, suggestions/learned input, emoji recents/frequency history, emoji search queries, clipboard data, key history, sensitive-editor contents, telemetry, Identity data, credentials, and cryptographic secrets from the portable preference format.

The number-row and utility-toolbar preferences are low-sensitivity device-local presentation state. They add no content observation and are not synchronized or exported by the current portability flow. Toolbar activation history is not retained.

GLAZE UI presentation adds no observation authority. No production claim is made for future cloud-assisted input, clipboard history, voice input, translation, GIF/sticker content, synchronization, or account-backed personalization until explicit Privacy Shield policy, consent, retention, user-control, implementation, and runtime acceptance exist.

### GoreeCloud Secure Paste — planned

GoreeCloud Keyboard is the planned primary user-facing IME surface for GoreeCloud Secure Paste. The intended architecture replaces passive cross-application clipboard reads with explicit user-mediated paste through a privileged Android/framework Secure Paste Broker governed by Privacy Shield. A normal Android IME cannot globally revoke another application's clipboard API authority and must not be represented as doing so.

Planned behavior includes a dedicated Paste action, per-application clipboard policy, optional expiration and Paste Once, stricter sensitive-item handling, minimized non-content evidence, Wardveil review for dangerous context-dependent payloads, and no clipboard payload backup/synchronization/Identity association by default. This capability remains Planned / Proposed until the privileged system enforcement path and applicable acceptance evidence exist. No Secure Paste control is added to the current toolbar merely to imply implementation.

### Wardveil Security / Security Center

The application does not treat UI state as security authority. Future downloadable dictionaries, models, themes, clipboard integrations, GIF/sticker providers, voice/input adapters, translation services, or other external content require explicit Wardveil validation appropriate to the object and execution boundary before production use.

### Everkeep / Continuity Center

The current one-field portable preference codec plus bounded category-only local export/apply seams are Development portability primitives only. They are not complete Keyboard backup, clean-target recovery, synchronization, or Everkeep acceptance. The number-row preference, utility-toolbar preferences, emoji recents, and other usage/presentation state are not silently included in portability or recovery scope.

### GoreeCloud Identity / Identity Center

The current native keyboard foundation does not require a GoreeCloud account to type, configure the number row or utility toolbar, or use the local one-field transfer flow. Future account-backed personalization or synchronization must use GoreeCloud Identity for authentication/authorization and must not expose credential material to the input surface.

### GoreeCloud Mesh / Mesh Center

Current local typing and utility-toolbar navigation do not depend on Mesh availability. Future cross-application/service capabilities must use governed GoreeCloud integration contracts rather than hidden coupling. Clipboard payloads, typed text, suggestion context, and other sensitive input data must not be transported over Mesh merely to establish integration or dashboard state.

### GoreeCloud Manager / Management Center

Keyboard may publish minimized operational/version/capability state to Manager when an accepted integration exists. Manager does not gain typed-text, editor-content, clipboard-payload, suggestion-history, toolbar-use history, or other sensitive input authority merely because it manages application lifecycle/configuration.

## GLAZE UI / Design Center

Current Official Stable GLAZE UI authority is V1.6 / `1.6.0` at exact release source `a7180679ea851389e0f3004515f9a25f420e716d`. The active Keyboard Development stack has not yet migrated to V1.6: PR #68 implements a V1.5 capability-presentation mapping at `b7fa8164bfdeaa1dc0acb21b770e7601120da04e` with reviewed implementation anchor `ee1032a0822ab8e103f8afe48e5c1859fde65cc9`, while native optical/material behavior remains pinned to the accepted V1.4.1 baseline `4fab9da0fad2e5c974e0e66ec88632c61745751c`. Keyboard therefore remains migration-required and nonconformant until fresh V1.6 source adoption and application-specific acceptance are completed.

The native `KeyboardView` consumes the inherited Frosted Neutral material foundation with Android Light/Dark runtime selection. The configurable toolbar uses the existing key surface/stroke/state treatment and the 48 dp general interaction floor rather than introducing an unrelated visual language. Neither the implemented V1.5 mapping nor the required V1.6 migration authorizes editor/typed-content color sampling. Environmental memory, remote color derivation, persistent sample history, semantic inference, telemetry, and animated atmosphere remain disabled in the Keyboard consumer.

Accessibility directives outrank cosmetic material behavior. Reduced Transparency, Reduced Motion, Increased Contrast, forced-colors/native equivalents, focus visibility, content legibility, target size, and task completion must remain valid even when blur/translucency/advanced effects are unavailable. The five-row number-row layout plus toolbar must preserve usable interaction geometry rather than shrinking controls solely to fit added chrome.

Local emoji search remains an application-local input-navigation capability and is not GoreeCloud Universal Search. Long-press alternates and the utility toolbar are application interaction, not Control Center. The toolbar's Settings action opens the local Keyboard settings surface; it does not become a general Settings, Launcher, Search, privacy, security, identity, recovery, or management authority. Glaze presentation semantics grant none of those authorities.

Historical Glaze Motion evaluation remains test-only and provides no current production/conformance evidence.

## Product-quality direction

GoreeCloud Keyboard must evolve as a complete first-party input product rather than a minimal demo. Remaining active capability families include:

- stronger local autocorrect, prediction, and user/language dictionaries;
- multilingual layouts and explicit language switching;
- gesture/swipe typing and cursor/text-selection gestures;
- expansion of the configurable utility toolbar only when additional underlying actions are actually implemented and governed;
- richer emoji, symbol, kaomoji, GIF, sticker, and specialized input discovery through approved boundaries;
- GoreeCloud Secure Paste and privacy-governed clipboard history/pinned snippets;
- privacy-approved voice input/adapters and translation;
- one-handed, floating, split, tablet/foldable/posture-aware layouts;
- hardware-keyboard and accessibility-aware workflows where applicable;
- Quill-assisted writing through privacy-preserving boundaries;
- user-controlled manual appearance and input preferences beyond the currently implemented local presentation choices;
- additional governed Launcher/Search/other GoreeCloud ecosystem actions; and
- explicitly governed portability, backup, recovery, and optional synchronization.

Feature richness must remain substantive. Buttons, placeholders, labels, decorative surfaces, or roadmap statements are not implementation evidence. Each capability requires functional behavior, native lifecycle integration, privacy/security boundaries, accessibility, testing, and appropriate runtime/release acceptance.

## Production and Stable acceptance gates

Development source or passing CI is not equivalent to production acceptance or Stable qualification. Production promotion requires evidence appropriate to the shipped platform, including:

- exact-revision source/build/test validation;
- complete GLAZE UI V1.3 consumer mapping for applicable keyboard and settings surfaces, including the toolbar;
- Reduced Transparency, Reduced Motion, Increased Contrast, forced-colors/native equivalents, large text/reflow, RTL/localization, and Touch Assistance behavior;
- TalkBack, Switch Access, Voice Access, and other claimed assistive-input acceptance across keys, suggestions, emoji, alternates, toolbar, and settings;
- representative Android host-editor compatibility including adaptive action semantics;
- representative phone/tablet/foldable layout and ergonomics acceptance, including the five-row number-row configuration plus toolbar;
- representative physical-device typing, long-press/slide/release, toolbar reachability, latency, performance, power, and thermal acceptance;
- Human Visual Excellence review of the actual Keyboard consumer;
- Privacy Shield and Wardveil Security acceptance appropriate to a sensitive input surface;
- approved Everkeep continuity/recovery scope and clean-target recovery evidence where required;
- applicable Manager/Mesh/Identity integrations without expanding input-data authority;
- protected signing, provenance, distribution, update, rollback, and recovery procedures;
- governed Release Candidate validation; and
- explicit production/Stable approval.

Until those gates are satisfied, GoreeCloud Keyboard remains Development and must not be represented as Stable.
