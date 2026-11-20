# Research and upstream assessment (2026-11-20)

The target is a new GoreeCloud Android keyboard, not a renamed proprietary
keyboard or a promise of feature parity with commercial products.

| Reference | Observed architecture/strength | License or constraints | Decision |
| --- | --- | --- | --- |
| [FlorisBoard](https://github.com/florisboard/florisboard) | Kotlin Android IME, privacy-first, extension/layout orientation; current published project notes word suggestions/spell checking not included in releases | Apache-2.0; review exact files/third-party notices before any copying | Architectural reference; do not wholesale import or represent suggestions as complete |
| [HeliBoard](https://github.com/HeliBorg/HeliBoard) | AOSP/OpenBoard lineage, offline-oriented configurable keyboard, dictionary and suggestion functionality | GPL-3.0 (plus AOSP Apache notices); complete derivative obligations if copied | Feature reference only; no GPL code imported into this MIT baseline |
| [OpenBoard](https://github.com/openboard-team/openboard) | AOSP LatinIME derived Android keyboard, language/dictionary mechanisms | GPL-3.0 project with upstream AOSP components | Protocol and compatibility reference only; no imported source |
| [SwiftFloris](https://github.com/SysAdminDoc/SwiftFloris) | Extensive FlorisBoard-derived offline-first features and testing ideas | Confirm file-specific grants, supply chain, assets and models before use | Candidate for future source-level assessment, not integrated |
| [Android InputMethodService](https://developer.android.com/reference/android/inputmethodservice/InputMethodService) | Android platform standard for IME service; commitText through InputConnection | Android framework APIs | Implemented directly |
| [Jetpack Compose / Material 3](https://developer.android.com/compose) | Kotlin UI, accessibility semantics, dynamic layout | AndroidX/Jetpack open-source packages, selected pinned coordinates in build | Implemented |
| [GoreeCloud UI](https://github.com/GoreeCloud/gc-ui) | Canonical color, typography, shape, motion and expressive Compose adapter | MIT, alpha candidate, no released AAR; source provenance recorded | Source vendored in this repo as real Gradle module |

## Implementation choices and trade-offs

1. **Local-first default**: no network permission, account, cloud sync,
   telemetry, clipboard collection, or input logging. Code review and runtime
   inspection must still substantiate release privacy assurances.
2. **New minimal IME rather than a complete fork**: allows clean Kotlin/Compose
   architecture and direct UI integration without relicensing GPL components.
   The initial keyboard is deliberately feature-incomplete; it does **not**
   provide a commercial-class word engine or language corpus.
3. **Separate IME from setup**: an InputMethodService owns typing; a Compose
   activity owns system-guided enablement and preferences. No WebView or
   remote service substitutes for actual Android IME integration.
4. **Original source versus third-party**: GitHub repo keeps first-party
   source, build scripts, docs, the recorded in-repo UI adapter and license.
   Google/AndroidX dependencies are resolved from canonical Maven endpoints.
5. **Privileged data boundary**: typed text is transferred to the target editor
   only. Never use generic analytics, crash breadcrumbs, cloud text APIs or
   AI prompt injection across this boundary without explicit policies,
   data-flow review and user consent.

## Prioritized third-party roadmap

- **Typing**: evaluate license-compatible Unicode/grapheme segmentation,
  open lexicons/dictionaries with language-specific rights, finite-state
  transducers/trie models, and on-device autocorrection benchmarks.
  Do not import community wordlists without provenance and redistribution rights.
- **Gesture**: investigate self-contained, auditable offline path-decoding
  engines; test performance, accidental activation, accessibility, and source license.
- **Voice**: evaluate **sherpa-onnx** or **Vosk** for optional local recognition
  only after verifying APK size, model licenses, microphone consent, memory,
  latency and supported architectures. Neither integrated in this version.
- **Emoji/GIF/stickers**: retain ordinary Unicode emoji without an external
  asset service initially; a future optional GoreeCloud GIF integration must be
  explicit opt-in with offline fallback and no typed-text leakage.
- **Privacy/security**: threat model under GoreeShield and Wardveil separately;
  no Mesh or Identity dependency for offline typing. Implement network opt-in
  only for features with their own separate consent and audited flow.
- **Performance**: latency, memory and battery testing on real Android
  devices are mandatory before claiming high-quality native performance.

## Research verification limits

These findings reflect public upstream README/licensing documentation accessed
on November 20, 2026 and installed Android platform guidance. Upstream versions,
assets and licenses change. No FlorisBoard/HeliBoard/OpenBoard source, models
or dictionaries have been copied, built, executed or scanned here. The
first-party GoreeCloud UI module was included with an exact source revision
and successfully compiled locally as part of the Android debug build.
