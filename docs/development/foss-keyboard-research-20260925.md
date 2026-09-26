# GoreeCloud Keyboard — Open-Source Keyboard Research — 2026-09-25

**Status:** Development research / implementation provenance  
**Repository:** `GoreeCloud/keyboard`  
**Scope:** Swipe typing, suggestion/correction ranking, context handling, keyboard interaction quality  
**Integration mode:** Independent GoreeCloud reimplementation informed by compatible open-source behavior and architecture

## Purpose

This record captures the free/open-source keyboard implementations reviewed while stabilizing GoreeCloud Keyboard after representative physical-device feedback.

The current implementation does **not** vendor or mechanically translate these projects. GoreeCloud uses its own Kotlin architecture, rendering surface, privacy boundaries, data model, Glaze presentation, settings, and test contracts. The reviewed projects are engineering references for mature design patterns.

This approach preserves GoreeCloud-native architecture while avoiding unnecessary reinvention of well-understood keyboard techniques.

## Reviewed projects

### HeliBoard

- Upstream: `HeliBorg/HeliBoard`
- Reviewed revision: `dc28c24d272f0591575be0a84351e2f0c528b766`
- License: GPL-3.0
- Relevant areas reviewed:
  - dictionary-backed suggestion fusion;
  - n-gram context;
  - proximity-aware composition;
  - batch/gesture suggestion handling;
  - conservative auto-correction gating.
- GoreeCloud use: architectural reference only; no HeliBoard source copied into this candidate.

### FlorisBoard

- Upstream: `florisboard/florisboard`
- Reviewed revision: `c64826c2f143585cba5a15d74000bb238a18445a`
- License: Apache-2.0
- Relevant areas reviewed:
  - separation between dictionary management and NLP providers;
  - explicit spelling-result abstraction;
  - independent glide-gesture detection;
  - configurable smartbar/quick-action presentation.
- GoreeCloud use: architectural reference only; no FlorisBoard source copied into this candidate.

### AnySoftKeyboard

- Upstream: `AnySoftKeyboard/AnySoftKeyboard`
- Reviewed revision: `742bf8817aae1a808be6c5fa72bb10f0182ff659`
- License: Apache-2.0
- Relevant areas reviewed:
  - gesture-point sampling with minimum-distance filtering;
  - path-corner extraction;
  - start-key candidate pruning;
  - more lenient end-point handling;
  - direction-sensitive gesture-path comparison;
  - dictionary frequency as one ranking signal rather than the sole signal;
  - bounded next-word dictionaries and incognito/no-learning boundaries.
- GoreeCloud use: the broad techniques above were independently reimplemented in GoreeCloud-native code. No upstream class, method, data structure, or source block was copied verbatim.

### Android / LineageOS LatinIME

- Upstream mirror reviewed: `LineageOS/android_packages_inputmethods_LatinIME`
- Reviewed revision: `017c5587d7f8728bf7d67143e990d160efe4a08f`
- License: Apache-2.0 notices in the repository identify the Android Open Source Project.
- Relevant areas reviewed:
  - real pointer-point batch input;
  - gesture recognition thresholds designed to distinguish ordinary fast typing from deliberate gesture input;
  - proximity-aware word composition;
  - n-gram context;
  - confidence thresholds for automatic correction.
- GoreeCloud use: behavioral/architectural reference only; no LatinIME source copied into this candidate.

## GoreeCloud-native changes informed by this review

The reviewed patterns led to the following independent changes on the active Development candidate:

1. **Physical gesture geometry instead of crossed-key-only decoding**
   - GoreeCloud now captures sampled finger coordinates and actual rendered letter-key centers.
   - Gesture candidates are compared against the physical path rather than treating every crossed key as equally authoritative.

2. **Corner and direction awareness**
   - Dense swipe traces are reduced to meaningful bends.
   - Direction disagreement contributes to the candidate score so a word whose key path passes near the gesture but travels in the wrong direction is penalized.

3. **Asymmetric endpoints**
   - Swipe starts remain relatively strict.
   - Swipe endings allow additional drift because finger lift-off is naturally less precise.

4. **Context cannot overpower geometry**
   - Local word context may gently promote an already-plausible swipe candidate.
   - It cannot pull a weak geometry candidate from the bottom of the pool to the top.

5. **Fast-typing swipe guard**
   - Immediately after ordinary rapid letter taps, swipe activation requires a more deliberate movement/duration threshold.
   - This specifically targets the physical-device failure where fast tapping was misclassified as swiping.

6. **Suggestion ranking uses more than dictionary order**
   - GoreeCloud combines prefix quality, bounded Unicode edit distance, QWERTY-neighbor likelihood, dictionary frequency, and transient local context.
   - The typed token remains recoverable instead of being silently discarded.

7. **Broader transient context**
   - Ordinary non-sensitive editors can provide a small bounded window immediately before the cursor for local prediction and grammar context.
   - This context is transient and not persisted.
   - Sensitive, no-suggestions, and no-personalized-learning boundaries remain distinct and fail closed.

8. **Expanded built-in vocabulary**
   - Additional modern typing/interface vocabulary is packaged locally, including terms such as `icon`, `toolbar`, `glyph`, `grammar`, `prediction`, `gesture`, and `haptic`.
   - This does not depend on the optional learned-language store.

## Licensing and provenance boundary

No third-party runtime dependency or vendored third-party source tree is added by this research pass.

If GoreeCloud later imports source directly rather than independently reimplementing a technique, that change must separately preserve the upstream license, copyright notices, modification notices, attribution, patent terms, and any other redistribution obligations that apply.

The current GoreeCloud repository remains governed by its existing AGPL license and GoreeCloud-owned source architecture.

## Privacy boundary

The research pass adds no network permission, cloud language service, contacts access, clipboard authority, telemetry, or remote model dependency.

Swipe traces and editor context used for ranking are transient. Optional persistent learning remains off by default and subject to existing sensitive-editor and Android no-personalized-learning controls.

## Acceptance boundary

This research and source implementation do not by themselves establish production-quality swipe typing, production-quality language modeling, Seal qualification, or Anchor qualification.

Representative physical-device testing remains required because swipe recognition and word prediction quality depend on real touch behavior, device geometry, typing speed, and ordinary editor interaction.
