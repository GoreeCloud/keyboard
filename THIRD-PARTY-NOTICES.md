# Third-Party Notices and Implementation References

GoreeCloud Keyboard is distributed under the repository's GNU AGPL-3.0 license. This document records third-party open-source projects consulted or adapted during Keyboard development and the boundaries applied to source reuse.

## FlorisBoard statistical glide typing

- Project: FlorisBoard
- Repository: `florisboard/florisboard`
- Exact revision inspected: `c64826c2f143585cba5a15d74000bb238a18445a`
- Reference file: `app/src/main/kotlin/dev/patrickgold/florisboard/ime/text/gestures/StatisticalGlideTypingClassifier.kt`
- Upstream copyright notice in the inspected file: Copyright (C) 2025 The FlorisBoard Contributors
- License: Apache License 2.0
- GoreeCloud use: the physical swipe classifier adapts general statistical concepts including start/end pruning, ideal word gestures, uniform path resampling, normalized shape comparison, physical-location comparison, path-length filtering, and frequency-aware ranking. GoreeCloud retains its own implementation structure, thresholds, privacy controls, transient-context ranking, UI, settings, tests, and lifecycle.

## AnySoftKeyboard gesture typing and suggestion architecture

- Project: AnySoftKeyboard
- Repository: `AnySoftKeyboard/AnySoftKeyboard`
- Exact revision inspected: `742bf8817aae1a808be6c5fa72bb10f0182ff659`
- Reference files:
  - `ime/app/src/main/java/com/anysoftkeyboard/gesturetyping/GestureTypingDetector.java`
  - `ime/app/src/main/java/com/anysoftkeyboard/dictionaries/SUGGESTIONS.md`
- License: Apache License 2.0
- GoreeCloud use: the swipe implementation adapts general ideas around meaningful-point filtering, curvature/corner emphasis, start/end proximity, direction-aware route comparison, bounded candidate ranking, and dictionary-frequency weighting. The Quill work also uses AnySoftKeyboard's documented separation of dictionary sources, spelling/correction ranking, next-word prediction, and privacy/incognito learning controls as an architectural reference. No complete AnySoftKeyboard subsystem, UI, branding, or application architecture is imported.

The Apache-2.0 license text used by these references is preserved at `LICENSES/Apache-2.0.txt`.

## FrequencyWords English corpus-frequency supplement

- Project: FrequencyWords by Hermit Dave
- Source repository: `hermitdave/FrequencyWords`
- Exact source revision: `525f9b560de45753a5ea01069454e72e9aa541c6`
- Source file/blob: `content/2018/en/en_50k.txt` / `bbf5fa991058ff642732784b31af023f66462e1d`
- Upstream source corpus: OpenSubtitles 2018, as documented by the FrequencyWords project.
- License: the upstream repository states MIT for its code and CC BY-SA 4.0 for its content. GoreeCloud uses the word-frequency content only; the packaged derived word list remains attributable to FrequencyWords/OpenSubtitles under CC BY-SA 4.0.
- GoreeCloud transformation: preserve source frequency order while retaining only unique lowercase ASCII alphabetic entries of 2–24 characters. This produces 46,691 packaged words.
- Ranking boundary: GoreeCloud's first-party Quill vocabulary remains first. The FrequencyWords supplement follows it in corpus-frequency order so common fallback words are not accidentally ranked by alphabetic position.
- Privacy boundary: the bundled list is static local application data. It adds no network permission, telemetry, account dependency, remote lookup, or typed-text collection.

## Android Open Source Project LatinIME

- Project: AOSP LatinIME
- Public mirror inspected: `GrapheneOS/platform_packages_inputmethods_LatinIME`
- GoreeCloud use: architectural research for separating typed-word validity, correction candidates, prediction candidates, n-gram context, and gesture-vs-tap suggestion sessions. No AOSP UI, branding, binary dictionary, or native decoder is imported by this stabilization pass.

## Strong-copyleft keyboard research

- OpenBoard repository verified during this pass: `openboard-team/openboard`
- License: GPL-3.0
- GoreeCloud use: behavior/feature research only. No OpenBoard source code was copied into the current GoreeCloud Keyboard candidate.
- HeliBoard was also considered as a FOSS keyboard reference, but no HeliBoard source code was copied into this candidate.

## License boundary

Translating, porting, or mechanically rewriting source does not eliminate upstream license obligations. Direct or derivative work from permissively licensed sources is therefore recorded with provenance and license text. Stronger-copyleft projects are not copied merely to reproduce a general behavior when a GoreeCloud-native implementation is practical.

The product remains a GoreeCloud-native application: third-party implementation references do not transfer upstream branding, UI identity, telemetry assumptions, account models, privacy authority, or release state into GoreeCloud Keyboard.
