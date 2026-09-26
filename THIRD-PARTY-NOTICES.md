# Third-Party Notices and Implementation References

GoreeCloud Keyboard is distributed under the repository's GNU AGPL-3.0 license. This document records third-party open-source projects consulted or adapted during Keyboard development and the boundaries applied to source reuse.

## SwiftFloris / FlorisBoard statistical glide work

- Project: SwiftFloris, derived from FlorisBoard
- Repository: `SysAdminDoc/SwiftFloris`
- Reference file: `app/src/main/kotlin/dev/patrickgold/florisboard/ime/text/gestures/StatisticalGlideTypingClassifier.kt`
- Reference revision inspected during the September 25, 2026 stabilization pass: public `master` state fetched through GitHub
- Upstream copyright notice: Copyright (C) 2025 The FlorisBoard Contributors
- License: Apache License 2.0
- GoreeCloud use: GoreeCloud's physical swipe classifier is a product-specific rewrite of the statistical concepts: endpoint pruning, uniform path resampling, ideal word gestures, normalized shape comparison, physical-location comparison, path-length pruning, and frequency-aware ranking. GoreeCloud uses its own data structures, privacy boundary, rendering, candidate model, context reranking, tests, and UI.

The Apache-2.0 license text is preserved at `LICENSES/Apache-2.0.txt`.

## AnySoftKeyboard

- Project: AnySoftKeyboard
- Repository: `AnySoftKeyboard/AnySoftKeyboard`
- License: Apache License 2.0
- GoreeCloud use: architectural and behavioral reference for offline suggestions, next-word suggestions, configurable autocorrection, language dictionaries, and the statistical gesture-typing lineage referenced by FlorisBoard/SwiftFloris. No whole AnySoftKeyboard component was imported in this pass.

## Android Open Source Project LatinIME

- Project: AOSP LatinIME
- Repository: `platform/packages/inputmethods/LatinIME`
- License: Apache License 2.0
- GoreeCloud use: architectural reference for separating typed-word validity, correction candidates, prediction candidates, n-gram context, and gesture-vs-tap suggestion sessions. No AOSP UI, branding, binary dictionary, or native decoder was imported in this pass.

## HeliBoard

- Project: HeliBoard
- Repository: `HeliBorg/HeliBoard`
- License: GPL-3.0 (with inherited AOSP Apache-2.0 portions and separately licensed assets)
- GoreeCloud use: feature/behavior research only, including dictionary-pack concepts, offline-first behavior, toolbar ideas, and rapid-typing gesture cooldown. No GPL-3.0 HeliBoard source code was copied into GoreeCloud Keyboard during this pass.

## License boundary

Translating or mechanically rewriting source does not eliminate upstream license obligations. GoreeCloud therefore records direct or derivative permissive-source work explicitly and avoids copying stronger-copyleft source merely to reproduce a general behavior when a clean GoreeCloud-native implementation is practical.
