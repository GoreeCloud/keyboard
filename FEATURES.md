# GoreeCloud Keyboard Features

## Implemented in Development source

- Native Android input method using `InputMethodService`.
- Original first-party keyboard rendering, hit-testing, and pointer-input surface.
- Runtime input-method registration checks through Android `InputMethodManager`.
- QWERTY letter entry.
- Shift, backspace, space, and enter controls.
- Letters/symbols mode switching with a primary digit/common-punctuation page and a second first-party symbol page for brackets, operators, currency marks, and common typographic symbols.
- Direct `ABC`, `?123`, and `=\\<` navigation between implemented input layers.
- A bounded first-party emoji layer with Smileys, People, Nature, Food, Symbols, and Travel categories plus selected composed variation-selector, skin-tone, ZWJ, family, rainbow-flag, and regional-indicator sequences.
- Compact accessible emoji-category controls with separate spoken accessibility labels.
- Bounded emoji recents that persist only in Android private app preferences, restore after an IME process restart, and can be explicitly cleared from the emoji strip.
- Fully offline transient emoji search over the packaged first-party emoji catalog; query text stays inside the IME search session, cannot be preloaded while the session is closed, and only a deliberately selected result is committed to the editor.
- Complete-String emoji commit behavior and bounded text-unit backspace tests that require every currently exposed emoji key to delete coherently.
- Bounded Unicode backspace handling for combining marks, variation selectors, emoji modifiers, keycaps, tag sequences, common ZWJ emoji, CRLF, and regional-indicator runs using pair-from-the-start flag parity.
- Fail-closed handling when bounded ordinary-field look-behind may begin inside a larger text unit; sensitive fields retain the stricter no-look-behind path.
- Local-only GoreeCloud Quill suggestion boundary.
- Frequency-ordered prefix suggestions, expanded packaged English inflections, Unicode-aware typo-correction candidates, and conservative automatic correction when a completed token is a confident one-edit misspelling.
- Bounded transient suggestion capture that suppresses misleading mid-word recapture when complete context is no longer known.
- Suggestion commit authority bound to the exact candidates currently presented for the active editor session rather than accepting arbitrary callback values.
- Sensitive-editor privacy gating for suggestion capture/display/acceptance and text look-behind deletion logic.
- Host `TYPE_TEXT_FLAG_NO_SUGGESTIONS` requests suppress local candidates. `IME_FLAG_NO_PERSONALIZED_LEARNING` remains honored by the non-learning architecture without unnecessarily disabling transient deterministic suggestions.
- Fail-closed editor lifecycle behavior across authoritative Android start/finish callbacks, including conservative no-active-editor and missing-editor-metadata state.
- Deterministic local long-press alternates for common Latin diacritics and punctuation.
- Viewport-bounded long-press popup placement and exact shared render/hit-test geometry with fail-closed gap/outside/non-finite/unused-cell handling, pointer movement selection, cancellation, haptic feedback, and accessibility announcements.
- Android virtual accessibility-node foundations and bounded alternate-character accessibility actions for the custom-drawn input surface.
- A privacy-minimized `goreecloud-keyboard-preferences/1` format containing exactly the last explicitly selected emoji category.
- Strict portable-preference validation/checksum integrity, category-only reader/writer seams, explicit user-controlled Android Storage Access Framework transfer, import preview before mutation, and export review/freeze before destination selection.
- Historical GLAZE UI V1.2 (`1.2.0`) Development optical/material mapping pinned to reviewed source `f285b9145e27e6e7027b075c37299d101945c272`.
- V1.2 neutral Frosted Neutral key surfaces consumed by the actual native `KeyboardView`, with Light/Dark runtime selection, explicit Deep Dark source values, 48/56 dp interaction floors, control geometry, and state calibration.
- A V1.2 non-semantic material/atmosphere boundary that prohibits chromatic, brand, or semantic color from becoming the keyboard substrate and authorizes no editor/content sampling, remote derivation, persistence, semantic inference, telemetry, network lookup, or animated atmosphere.
- A bounded GLAZE UI V1.6 presentation-context layer accepted through PR #73, using Android font scale, animator enablement, and touch-exploration signals without relabeling the V1.2 optical substrate as V1.6-complete.
- Touch Assistance runtime sizing accepted through PR #77: the suggestion strip and alternate targets use the 56 dp interaction floor, and the four-row layout requests 308 dp preferred height when touch exploration is active and Android grants that size.
- Platform Contract 0.4 declaration with current repository identity `GoreeCloud/keyboard` and all nine Integral Platform Systems represented.
- No Android network permission in the current foundation; emoji recents, emoji search, suggestions, alternates, and portable preferences do not synchronize or emit telemetry.
- Unit, build, governance, and Android emulator validation paths covering registration, privacy lifecycle, Unicode deletion, suggestion authority, emoji search, portable preference boundaries, Glaze UI mapping/context, accessibility, and native interaction.

## Development / acceptance work still required

- Complete optical/material/component migration and rendered/native consumer acceptance against current Official Stable GLAZE UI V1.6 across all keyboard and settings surfaces; the V1.2 optical substrate remains historical Development implementation evidence only.
- Reduced Transparency / solid fallback, Reduced Motion, Increased Contrast, forced-colors/native equivalents, 200% text/reflow, complete Touch Assistance behavior, RTL/localization, and Deep Dark runtime policy where applicable.
- Representative physical-device IME acceptance across supported Android/editor combinations.
- Complete TalkBack, Switch Access, Voice Access where claimed, and other assistive-input acceptance.
- Representative phone/tablet/foldable ergonomics, including one-handed/split/adaptive layouts where implemented.
- Representative physical-device latency, performance, power, and thermal acceptance.
- Human Visual Excellence review of the actual Keyboard consumer.
- Broader composed-sequence coverage, complete grapheme segmentation, and additional language/locale input modes.
- Privacy Shield, Wardveil Security, Everkeep, Manager, Mesh, Identity, Policy, and Observability acceptance where applicable without expanding input-data authority.
- Complete approved backup/clean-target recovery scope beyond the current one-field portability primitive.
- Protected signing/provenance, release packaging, distribution, update/rollback, and explicit Stable approval.

## Planned product capabilities — not current implementation claims

- Privacy-bounded spacebar cursor control after accepted integration and representative ergonomics/RTL/accessibility validation; open Draft candidates are not current implementation authority.
- Higher-order swipe recognition quality, multilingual gesture models, and representative physical-device acceptance beyond the current QWERTY shape-aware Development decoder.
- Stronger local prediction/correction and user/language dictionaries.
- Multilingual input and language switching.
- User-controlled personalization and learned-language features where separately approved.
- GoreeCloud Secure Paste and other clipboard tools with explicit Privacy Shield and privileged-platform enforcement boundaries.
- Optional voice input/adapters where platform, privacy, and security policies permit.
- One-handed, split, tablet, foldable, and posture-aware keyboard experiences.
- Richer emoji, symbol, kaomoji, and specialized input discovery.
- GoreeCloud Quill writing assistance beyond the current local suggestion boundary.
- Governed synchronization, backup/recovery, and portability where explicitly implemented.
- Apple-platform keyboard support through a separate native implementation and acceptance path.

All planned capabilities remain subject to GoreeCloud privacy, security, identity, continuity, integration, design, accessibility, and release acceptance requirements. Buttons, labels, placeholders, Draft PRs, or decorative surfaces do not count as implementation evidence by themselves.
