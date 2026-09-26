# GoreeCloud Keyboard — Changelogs

**Record type:** Authoritative repository changelog index and current change history  
**Repository:** `GoreeCloud/keyboard`  
**Lifecycle:** Weave / nonconformant; deployment state: development  
**Migration state:** Complete on authoritative `main`; PR #79 merged as `6071bf3b7fddf36ddaa172b7ca858948f95ae6d1`, exact-main Android CI #264 passed, and the mapped legacy Drive roadmap/changelog sources were permanently retired and independently verified absent on September 22, 2026.  
**Current governance baseline:** `6071bf3b7fddf36ddaa172b7ca858948f95ae6d1` (PR #79).  
**Current runtime-bearing baseline:** `64d5ed5b600e247630accceaa3f5ba8be26b3143` (PR #93); exact-main Android CI #294 / run `36196662346` passed.  
**Governing standard:** Standard — Repository Feature Tracking and Changelog Governance v1.0.

## Authority and interpretation

This file is the repository-local changelog authority. Historical Keyboard chronology formerly stored in Google Drive is preserved in six contiguous files under `docs/changelog-history/`.

The historical archive preserves all 208 non-empty paragraphs from the retired `Change Log — Keyboard.docx`, verbatim except for Markdown normalization of Word heading styles. Historical Draft/candidate/lifecycle statements apply only to their original context and do not override current authoritative `main`.

Draft or unmerged pull requests are not accepted changes. PR #78 and older open stacked Drafts remain candidate-only.

## Historical Drive archive

Retired legacy source:
- `Change Log — Keyboard.docx`
- former Drive file ID `1aSq_9oQxxyXr-YErNBFesLn5r4_h4N0t`
- permanently deleted September 22, 2026 after verified migration; independent metadata readback returned `404 Not Found`

Migrated repository archive:
- [`legacy-drive-keyboard-changelog-part-01.md`](docs/changelog-history/legacy-drive-keyboard-changelog-part-01.md) — source non-empty paragraphs 1–35 of 208
- [`legacy-drive-keyboard-changelog-part-02.md`](docs/changelog-history/legacy-drive-keyboard-changelog-part-02.md) — paragraphs 36–70
- [`legacy-drive-keyboard-changelog-part-03.md`](docs/changelog-history/legacy-drive-keyboard-changelog-part-03.md) — paragraphs 71–105
- [`legacy-drive-keyboard-changelog-part-04.md`](docs/changelog-history/legacy-drive-keyboard-changelog-part-04.md) — paragraphs 106–140
- [`legacy-drive-keyboard-changelog-part-05.md`](docs/changelog-history/legacy-drive-keyboard-changelog-part-05.md) — paragraphs 141–175
- [`legacy-drive-keyboard-changelog-part-06.md`](docs/changelog-history/legacy-drive-keyboard-changelog-part-06.md) — paragraphs 176–208

Together these six files preserve the complete non-empty Drive chronology, including merged milestones, failed/corrected validation attempts, Draft candidate evidence, Glaze transitions, local input/privacy work, accessibility work, and release-boundary statements.

## Current repository changelog

### September 26, 2026 — Physical-device IME recovery, first-run setup, and expanded Clipboard / 0.1.21-dev
- Records representative-device rejection of the 0.1.20 candidate because the installed keyboard could stop appearing; the prior 0.1.20 exact-head green CI evidence is retained as automated evidence only and is not physical-device acceptance.
- Replaces auxiliary `InputMethodService.setInputView(...)` root swaps with a stable `KeyboardInputSurfaceHost`. Keyboard, Clipboard, and Clipboard-edit surfaces now swap inside one Android-owned IME root, and every new editor session explicitly restores the Keyboard surface.
- Adds Android runtime coverage for returning from an auxiliary surface to the Keyboard surface.
- Adds a launcher first-run setup wizard. It checks whether the exact version-scoped Development IME is enabled and selected, links to Android keyboard settings/input-method selection, and lets the user choose initial correction, prediction, capitalization, double-space, swipe/trail, cursor-control, number-row, key-height, toolbar, Emoji, long-press, haptic/sound, optional local-learning, Clipboard-history, and Clipboard-retention preferences.
- Adds persistent local setup-completion state and a Settings action for rerunning the setup flow.
- Expands the Keyboard-side Clipboard manager with dedicated **Pinned** and **Recent** sections, tap-to-paste cards, long-press Pin/Unpin/Delete management, encrypted saved-clip editing, and an IME-local edit surface whose unfinished buffer is discarded on lifecycle exit.
- Adds local smart-content extraction for phone numbers, email addresses, web links, street addresses, dates, and times. Detected fragments are computed in memory from the displayed clip, can be pasted independently, are not separately persisted, and never enter prediction/learning.
- Adds edit-retention regression coverage for pinned and temporary clips, setup-completion persistence coverage, smart-content detector coverage, and the stable input-surface regression.
- Advances the side-by-side Development package to `versionCode 22` / `0.1.21-dev` / `com.goreecloud.keyboard.dev.v22`.
- This remains Draft / Weave Development work. Fresh exact-head Android CI, Android 15 IME/runtime validation, and new representative physical-device acceptance are required before any merge or maturity claim.

### September 26, 2026 — Draft PR #97 Keyboard-side Clipboard, contraction repair, and compound guidance / 0.1.20-dev
- Adds a real first-party **Clipboard** toolbar action and rounded-outline Clipboard glyph; the toolbar now exposes only implemented Emoji, Clipboard, and Settings actions.
- Adds an IME-local Clipboard panel with user-mediated direct-text paste, **Paste Once**, pin/unpin, delete, clear-unpinned history, and explicit current-app **Allow / Ask / Paste only / Block** controls. The default Ask state requires **Allow once** before Keyboard reads the current clipboard for that app session.
- Adds opt-in device-local clipboard history bounded to 24 non-sensitive text entries. Persisted payloads are AES-GCM encrypted with an Android Keystore key; unpinned entries expire after the selected 10-minute, 1-hour, or 24-hour window. Android-marked sensitive clips are never persisted and are exposed only through Paste Once.
- Keeps clipboard payloads outside Quill suggestions/correction/prediction, optional language learning, portable preference export, Android backup, synchronization, Identity/Mesh state, telemetry, and network access. Direct Clipboard reads accept text only and do not coerce content URIs or launch intents.
- Documents the authority boundary: this Keyboard-side implementation does **not** provide the future privileged GoreeCloud Secure Paste Broker and cannot revoke another application's Android clipboard API authority.
- Records independent open-source research of HeliBoard, FlorisBoard, and AnySoftKeyboard at pinned revisions in `OPEN-SOURCE-RESEARCH.md`; no third-party clipboard source or assets were copied into the implementation.
- Strengthens local language quality with broader contractions, keyboard-proximity disambiguation for contraction typos such as `csnt → can't`, one-edit missed-space recovery such as `helpcme → help me`, canonical first-party dictionary priority, and explicit common hyphenated-compound suggestions such as `up-to-date` and `built-in`.
- Adds unit/runtime coverage for contraction repair, one-edit run-together recovery, compound-tail recognition, clipboard history retention/pinning, the accessible Clipboard toolbar action, Android-Keystore encrypted history round-trip/expiration, and Ask-state rendering.
- Advances the side-by-side Development package to `versionCode 21` / `0.1.20-dev` / `com.goreecloud.keyboard.dev.v21`.
- This remains Draft / Weave Development work. Exact-head CI, Android 15 IME/runtime validation, and representative physical-device clipboard/typing acceptance are still required before any merge or maturity claim.

### September 26, 2026 — Draft PR #97 rapid-tap isolation and missed-space recovery / 0.1.19-dev
- Records representative-device 0.1.18 feedback: the version-bound Development label now renders correctly and company/brand swipe recognition improved, but sustained fast tap typing can still be promoted into swipe mode and run-together words remain difficult to recover.
- Extracts swipe activation into a pure `SwipeIntentClassifier` so tap-versus-gesture evidence is independently regression-tested instead of being embedded only in `KeyboardView` thresholds.
- Extends the rapid-typing guard window and requires materially stronger path/duration evidence for two- and three-key gestures, and stronger evidence for four-plus-key gestures, when a gesture begins immediately after a completed letter tap.
- Adds recent-fast-tap recovery: if a hurried touch crosses neighboring key bounds but does not satisfy deliberate swipe intent, the keyboard preserves the ACTION_DOWN letter instead of committing the release key.
- Adds Android runtime regressions with swipe enabled that reproduce a rapid tap drifting across three letter keys and verify it remains a tap, while a deliberate longer swipe immediately after a tap still emits a swipe gesture.
- Adds a bounded local `RunTogetherWordResolver` for missed spaces. It splits an unknown 5–32-letter token into at most four exact local dictionary words, rejects ambiguous near-ties, preserves known whole words, and allows long exact curated brand terms without granting them artificial frequency priority.
- Integrates high-confidence run-together recovery into the suggestion strip and explicit boundary/autocorrect path, so examples such as `helpme` or `helpmeunderstandyou` can become `help me` / `help me understand you` without remote inference or typed-text upload.
- Advances the side-by-side Development package to `versionCode 20` / `0.1.19-dev` / `com.goreecloud.keyboard.dev.v20`; the visible Development label remains generated from the same Gradle version source.
- No network permission, telemetry, Contacts access, remote language model, clipboard authority, or new typed-text persistence was introduced.
- This remains Draft / Weave Development work. Fresh exact-head CI and representative physical-device retesting are required before acceptance.

### September 26, 2026 — Draft PR #97 cross-app gesture reliability, swipe precision, and brand vocabulary / 0.1.18-dev
- Treats the latest representative-device screenshots and testing as continuing Weave-stage evidence: swipe entry can be disabled in some ordinary host editors such as note fields, rapid tap typing can still be promoted into swipe intent, gesture decoding can choose an unintended word, and common technology/company vocabulary remains incomplete.
- Decouples Android `TYPE_TEXT_FLAG_NO_SUGGESTIONS` from gesture typing. Ordinary non-sensitive editors may suppress suggestion/autocorrect surfaces while still accepting deliberate local swipe input; sensitive/password fields remain fail-closed.
- Strengthens tap-versus-swipe classification using accumulated gesture-path travel, a longer recent-fast-typing guard window, and stronger two-key path/net-travel/duration evidence. Curved or returning deliberate swipes remain eligible because classification no longer depends only on endpoint displacement.
- Pins the physical decoder's best geometry candidate as the committed swipe winner. Predictive context may refine alternate candidates but can no longer replace the strongest traced result.
- Improves swipe precision with stronger ordered-key sequence weighting, ordered key-coverage evidence, and less frequency bias while retaining the four-key endpoint tolerance required by neighboring-endpoint regression coverage.
- Adds a curated static local brand/company/product vocabulary including `Samsung`, `Ryzen`, `MIUI`, `OnePlus`, `Nvidia`, `Snapdragon`, `GitHub`, `OpenAI`, and other common technology terms, preserving familiar display casing where the packaged word is authoritative.
- Advances the side-by-side Development package to `versionCode 19` / `0.1.18-dev` / `com.goreecloud.keyboard.dev.v19`.
- Generates the Development launcher and IME labels from the same Gradle candidate-version source, so Android shows **GoreeCloud Keyboard Dev 0.1.18** and the visible identity cannot drift to an older candidate number.
- No network permission, remote language model, telemetry, Contacts access, clipboard authority, or new typed-text persistence was introduced.
- This remains Draft / Weave Development work. Fresh exact-head CI and representative physical-device retesting remain required.

### September 26, 2026 — Draft PR #97 physical typing feedback correction / 0.1.17-dev
- Treats the latest representative-device feedback as a continuing Weave-stage quality gate: swipe typing is improved but still inconsistent, fast ordinary typing can still be misclassified as swipe intent, common-word recognition/autocorrect can miss obvious neighbor-key slips such as `bjt → but`, and local grammar assistance remains too limited.
- Advanced the side-by-side Development package to `versionCode 18` / `0.1.17-dev` / `com.goreecloud.keyboard.dev.v18` so this CI-signed device-test build can install beside the earlier 0.1.16 candidate.
- Refined `SuggestionEngine` autocorrect confidence so the normal ambiguity margin remains conservative, while a one-key QWERTY-neighbor substitution may use a smaller margin only when the winning word is genuinely high-frequency and has a large packaged-dictionary rank advantage over the runner-up. This explicitly covers the reported `bjt → but` class without globally relaxing autocorrect.
- Added regressions requiring the real first-party dictionary to correct `bjt` to `but` while preserving suggestion-only behavior when competing neighbor-key words have close ranks.
- Expanded the deterministic local grammar boundary with narrow standard-English subject/auxiliary agreement repairs such as `I is → I am`, `they is → they are`, `she are → she is`, `you has → you have`, and `it have → it has`.
- Strengthened rapid-typing/swipe separation: a gesture started within the existing fast-typing guard window now requires at least 40 dp travel and 96 ms duration, and a two-letter path additionally requires a strong 64 dp travel signal. Deliberate longer two-key swipes remain eligible.
- Modestly widened swipe start/end candidate neighborhoods from three to four keys and increased ordered key-sequence influence in the physical statistical scorer, retaining shape/location/direction/corner scoring and the bounded indexed dictionary path.
- No network permission, remote language model, telemetry, Contacts access, clipboard authority, or new typed-text persistence was introduced.
- This remains Draft / Weave Development work. Fresh exact-head source/build/emulator validation and representative physical-device retesting remain required before acceptance.

### September 26, 2026 — Draft PR #97 unified functional iconography / 0.1.16-dev
- Advanced the side-by-side Development package to `versionCode 17` / `0.1.16-dev` / `com.goreecloud.keyboard.dev.v17`; corrected the stale debug launcher label so the installed development app identifies itself as **GoreeCloud Keyboard Dev 0.1.16**.
- Implemented a first-party shared functional glyph system for Shift, Backspace, Enter, Emoji, and Settings instead of mixing text symbols and unrelated custom icon styles.
- Standardized the default glyph stroke family at 1.85 dp with round caps/joins, compact optical geometry, monochrome default presentation, and selected-state accent treatment.
- Replaced the toolbar's previous settings-slider glyph with a compact first-party gear and refined the Emoji face to the same optical/stroke family.
- Changed the default icons-only toolbar so controls sit directly on the quiet Glaze toolbar surface rather than each receiving a permanent filled pill; pressed state remains a temporary soft surface treatment.
- Preserved the full 48 dp interaction row behind compact 40 dp visual icon containers so the lighter visual treatment does not shrink touch or accessibility targets.
- Added a dedicated repository design record at `docs/development/functional-iconography.md`, grounded in the owner-provided contemporary Android keyboard reference and current GLAZE UI V1.6 inherited functional-icon rules.
- Added Android runtime coverage for functional-glyph geometry and toolbar interaction-floor preservation.
- No inactive Clipboard, GIF, voice, translation, handwriting, or plugin controls were added. The toolbar continues to expose only real implemented Emoji and Settings actions.
- This remains Draft / Weave Development work pending fresh exact-head source/build/emulator validation and representative physical-device visual/ergonomic review.

### September 26, 2026 — Draft PR #97 frequency ranking and rapid-typing responsiveness / 0.1.15-dev
- Advanced the side-by-side Development package to `versionCode 16` / `0.1.15-dev` / `com.goreecloud.keyboard.dev.v16`.
- Replaced the interim 0.1.14 alphabetical Moby fallback with a 46,691-word supplement derived from FrequencyWords `content/2018/en/en_50k.txt` at exact revision `525f9b560de45753a5ea01069454e72e9aa541c6` / blob `bbf5fa991058ff642732784b31af023f66462e1d`.
- The FrequencyWords content is documented upstream as CC BY-SA 4.0; GoreeCloud preserves attribution/provenance in `THIRD-PARTY-NOTICES.md`, keeps the first-party Quill lexicon first, and preserves the supplement's corpus-frequency order instead of accidentally treating alphabetic position as usage frequency.
- The packaged supplement contains `jump`, `lag`, `lagging`, `typing`, `suggestion`, and `suggestions`; the source ranks `jump` at 1,094 and `hill` at 1,837 before GoreeCloud's first-party lexical overrides and geometry/context scoring.
- Ordinary letter input now coalesces suggestion refresh work to a bounded 24 ms cadence. Text commit remains immediate, while repeated rapid taps do not synchronously invoke editor-context reads and suggestion scoring after every single letter.
- Normal Backspace-driven suggestion refreshes use the same coalescing path; sensitive/suppressed editors continue to clear language assistance immediately.
- Small touch-up drift within Android tap slop now preserves the key that received ACTION_DOWN rather than switching to an adjacent key solely because the release coordinate crossed a visual gap.
- Existing bounded key-gap near-miss recovery, frequency/suggestion indexes, swipe endpoint indexes, 0.1.11 swipe safeguards, 0.1.12 settings, and 0.1.13 cursor control remain in place.
- Added/updated runtime coverage for frequency-asset order/source identity and small release-drift key preservation. Fresh exact-head source/build/emulator validation and representative physical-device retesting remain required.

### September 25, 2026 — Draft PR #97 physical-device dictionary, swipe, and tap-reliability correction / 0.1.14-dev
- Treats representative physical-device 0.1.13 feedback as a failed typing-quality gate: swiping `jump` repeatedly decoded as `hill`, the local dictionary did not recognize common words such as `lagging`, and some ordinary taps appeared to produce no key input.
- Root-cause readback confirmed that `jump`, `lag`, and `lagging` were absent from the packaged 0.1.13 vocabulary while `hill` was present, so the decoder could not select the intended missing word regardless of geometry quality.
- Advanced the side-by-side Development package to `versionCode 15` / `0.1.14-dev` / `com.goreecloud.keyboard.dev.v15`.
- Added a static local English fallback asset derived from Moby Words II `common.txt` at exact public source revision `b84076e29e6a4c686e36c259df5ffe10bafbdaed` / blob `c5ce15eb98035e4d2ea05aefac10c499c1260d37`. The deterministic GoreeCloud import keeps 46,855 unique lowercase ASCII alphabetic entries of 2–24 characters. The original Moby content is public domain and the source mirror dedicates its formatting-only changes under CC0 1.0; provenance is recorded in `THIRD-PARTY-NOTICES.md`.
- Keeps the first-party Quill frequency lexicon first in candidate order, then uses the packaged fallback for missing ordinary English words. The expanded dictionary remains read-only local application data and adds no Android network permission, remote lookup, telemetry, account dependency, or typed-text collection.
- Reworked `SuggestionEngine` to cache a stable dictionary index and prune spelling/correction work by first-key neighborhood and bounded word length instead of rescoring every dictionary entry for every keystroke.
- Reworked `SwipeTypingEngine` to cache normalized dictionary traces in start/end endpoint buckets before physical or ordered-route scoring, preventing the much larger fallback lexicon from turning each swipe into an unrestricted whole-dictionary geometry pass.
- Added bounded nearest-key recovery for small touches landing in the visual gap immediately beside rendered keys; exact hit targets remain authoritative and recovery is capped by Android touch slop and a 10 dp maximum.
- Added regressions for `laging → lagging`, a physical `j → u → m → p` swipe preferring `jump` over `hill`, 40,000+ packaged-word coverage, rapid ordinary taps, and visual-gap near-miss activation.
- Fresh exact-head CI and representative physical-device retesting are required before the correction can be accepted. The candidate remains Draft / Weave Development work.

### September 25, 2026 — Draft PR #97 privacy-bounded spacebar cursor-control candidate / 0.1.13-dev
- Advanced the side-by-side Development package to `versionCode 14` / `0.1.13-dev` / `com.goreecloud.keyboard.dev.v14`.
- Reimplemented the earlier Draft PR #63/#64 spacebar cursor-control concept against the current PR #97 runtime instead of merging the stale stacked branch.
- A one-finger horizontal-dominant drag beginning on the rendered Space key now emits bounded Android DPAD LEFT/RIGHT cursor movement through the active `InputConnection`; ordinary Space taps remain on the normal key path.
- Vertical-dominant/equal-axis movement and multi-pointer interaction fail closed. Cursor mode consumes the remainder of the gesture so release cannot also insert Space.
- The gesture performs no surrounding-text reconstruction, clipboard read, persistence, telemetry, network request, or new permission. After movement, transient Quill composing/prediction state is invalidated rather than reconstructed from host text.
- Cursor control is disabled in the local Emoji layer and while Android touch exploration is active.
- Added a default-on device-local **Spacebar cursor control** setting under Gesture typing.
- Added pure gesture-policy tests and rendered Android runtime tests covering normal Space taps, horizontal cursor drags, vertical fail-closed behavior, and the disabled preference path.
- Added a current development design record at `docs/development/spacebar-cursor-control.md`.
- This remains Draft / Weave Development work pending fresh exact-head CI and representative physical-device cursor, RTL/BiDi, accessibility, editor/OEM, and latency validation.

### September 25, 2026 — Draft PR #97 expanded local settings candidate / 0.1.12-dev
- Advanced the side-by-side Development package to `versionCode 13` / `0.1.12-dev` / `com.goreecloud.keyboard.dev.v13`.
- Expanded the first-party settings surface with functional, device-local controls for the ordinary number row, password-field number-row override, double-space period, swipe-trail visibility, Emoji-toolbar visibility, long-press alternate hints, Fast/System/Relaxed long-press delay, and opt-in Android key-click sound.
- Preserved existing defaults where changing them could surprise physical testers: the ordinary number row, password number row, swipe trail, Emoji shortcut, long-press hints, haptics, suggestions, autocorrection, predictions, automatic capitalization, and double-space period are enabled; key-click sound and learned language remain opt-in.
- Number-row policy remains editor-local, double-space period is suppressed in sensitive or language-assistance-suppressed fields, and the new settings add no Android permission, network authority, contacts access, clipboard access, voice provider, telemetry, or external dictionary.
- Corrected the swipe setting description so it no longer says three crossed keys are required; the 0.1.11 recognizer already permits a qualifying two-key sampled trace.
- Added settings persistence/runtime coverage plus pure policy tests for sensitive-field number-row behavior and double-space-period qualification.
- The candidate remains Draft / Weave Development work pending fresh exact-head source, build, Android runtime, and representative physical-device validation.

### September 25, 2026 — Draft PR #97 physical-device swipe and language-quality correction / 0.1.11-dev
- Advanced the side-by-side Development package to `versionCode 12` / `0.1.11-dev` / `com.goreecloud.keyboard.dev.v12`.
- Treats the 0.1.10 representative physical-device report as a failed acceptance gate: deliberate swiping could drop out, and the suggestion strip could drift into a malformed concatenated token.
- Re-evaluates swipe activation on the final finger-up sample so sparse/coalesced Android motion delivery cannot lose a deliberate gesture merely because the last MOVE arrived before the activation duration.
- Removes the minimum swipe-velocity gate while retaining movement-distance, duration, recent-fast-typing, and different-letter safeguards, and allows a qualifying two-key sampled trace to reach decoding.
- Keeps physical statistical swipe candidates primary but falls back to the ordered key trace when strict physical filtering rejects all or too few candidates, preventing an otherwise valid gesture from silently producing no word.
- Reconciles the locally tracked composing prefix against the bounded ordinary-editor token before suggestion ranking so stale internal state cannot accumulate across word boundaries.
- Removes duplicate grammar injection from contextual prediction, prefers built-in vocabulary/context before optional learned candidates, lets real suggestions fill available strip slots before an unknown raw token, and broadens suggestion-only typo distance without weakening automatic-correction thresholds.
- Expands the packaged local everyday vocabulary with additional ordinary conversational forms and adds common failure/status phrase continuations.
- Strengthens suggestion-ranking unit coverage. The candidate remains Development/Weave work pending fresh exact-head CI and representative physical-device retest.

### September 25, 2026 — Draft PR #97 continuous Backspace repeat candidate / 0.1.10-dev
- Advanced the side-by-side Development package to `versionCode 11` / `0.1.10-dev` / `com.goreecloud.keyboard.dev.v11`.
- Changed Backspace press behavior so one deletion occurs immediately on press, then deletion repeats continuously after a bounded hold delay until the finger is released.
- Cancels held deletion on touch cancellation or when the finger moves outside the Backspace target plus normal touch slop, preventing runaway deletion after a drag-away.
- Keeps each repeat on the existing Unicode-aware deletion path, so ordinary text deletes progressively while supported emoji/grapheme clusters continue to use the bounded safe deletion logic.
- Added Android runtime coverage that verifies immediate Backspace response, repeated deletion while held, and prompt cessation after release.
- Retains the 0.1.9 statistical swipe, prediction, grammar, dictionary, toolbar, settings, privacy, and FOSS-provenance improvements; representative physical-device acceptance remains required.
- This entry describes the open Draft PR #97 candidate and is not accepted authoritative-main implementation until governed merge/readback and fresh exact-main validation occur.

### September 25, 2026 — Draft PR #97 FOSS-informed typing-quality candidate / 0.1.9-dev
- Advanced the side-by-side Development package to `versionCode 10` / `0.1.9-dev` / `com.goreecloud.keyboard.dev.v10`.
- Researched current free/open-source keyboard implementations including FlorisBoard/SwiftFloris, AnySoftKeyboard, AOSP LatinIME, and HeliBoard, and recorded source-reuse/license boundaries in `THIRD-PARTY-NOTICES.md`.
- Reworked the physical swipe decoder into a GoreeCloud-native statistical classifier informed by Apache-2.0 FlorisBoard/SwiftFloris and AnySoftKeyboard ideas: endpoint pruning, sampled-path resampling, normalized shape comparison, physical-location comparison, path-length filtering, duplicate-letter ideal-gesture variants, frequency ranking, and bounded contextual re-ranking.
- Added Android `MotionEvent` historical-sample consumption so batched pointer coordinates contribute to both the physical gesture geometry and crossed-key trace; this reduces lost bends/keys when the platform coalesces MOVE events under load.
- Kept HeliBoard GPL-3.0 work at behavior/reference level for this pass rather than copying its source; the repository remains AGPL-3.0 and preserves the Apache-2.0 reference license text under `LICENSES/Apache-2.0.txt`.
- Added a curated swipe vocabulary separate from mechanically generated inflections so synthetic low-value forms do not dominate gesture ambiguity.
- Expanded the packaged everyday-English dictionary with modern keyboard/input vocabulary including `icon`, `icons`, `toolbar`, `emoji`, `grammar`, `prediction`, `autocorrect`, `haptics`, `gesture`, `editor`, and related common terms.
- Strengthened deterministic local grammar/context assistance with bounded ordinary-editor context, phrase continuations, common spelling repairs such as `grammer → grammar`, `recieve → receive`, and `tommorow → tomorrow`, plus context-dependent `should of → should have`-style modal repair.
- Redesigned the utility toolbar into one rounded Glaze surface with first-party drawn emoji-face and settings-slider glyphs; removed the redundant Hide Keyboard action while keeping Emoji and Settings.
- Added Android system-bar inset handling to Keyboard Settings and portable-preference screens so content no longer intentionally occupies status/navigation bar space.
- Added/updated tests for modern vocabulary, contextual suggestion ranking, grammar repairs, statistical physical swipe recognition, toolbar action boundaries, and system behavior.
- Preserved the existing privacy model: no Android network permission, gesture traces are transient, optional learning remains off by default, and sensitive/no-suggestions/no-personalized-learning editor boundaries remain fail-closed.
- This entry describes the open Draft PR #97 candidate and is not accepted authoritative-main implementation until governed merge/readback and fresh exact-main validation occur.

### September 25, 2026 — Draft PR #97 physical-device feedback candidate / 0.1.8-dev
- Advanced the side-by-side Development package to `versionCode 9` / `0.1.8-dev` / `com.goreecloud.keyboard.dev.v9`.
- Added **Learn from what you type** as an explicit privacy-sensitive control that is OFF by default. When enabled, Keyboard stores only bounded normalized word-frequency and adjacent-word-frequency counters in app-private local storage, excludes sensitive editors, host no-suggestions editors, and Android no-personalized-learning editors from collection and learned-data use, exposes local deletion, and adds no network/sync/telemetry authority.
- Added key-press haptic feedback enabled by default with a user setting to disable it.
- Added toolbar style control: icons-only by default, optional Icons + labels.
- Removed Emoji from the ordinary/symbol bottom rows, removed the duplicated Symbols mode action, and removed the redundant Hide Keyboard action. The candidate toolbar now exposes only Emoji and Settings, uses a unified Glaze surface, and draws first-party face/settings glyphs rather than literal emoji or generic gear characters; Clipboard/GIF remain unimplemented rather than decorative.
- Replaced generic Backspace/Enter text glyph rendering with first-party vector-style icon geometry.
- Expanded built-in conversational prediction context and combined opt-in learned word/bigram ranking with the existing static GoreeCloud/everyday-English lexicon.
- Reworked swipe recognition using a GoreeCloud-native adaptation of permissively licensed FlorisBoard and AnySoftKeyboard gesture-classification concepts: rendered-key geometry, endpoint pruning, meaningful-point filtering, uniform resampling, normalized-shape/location comparison, direction and corner scoring, route-length filtering, frequency weighting, bounded transient-context re-ranking, and up to three post-swipe alternatives. Exact upstream revisions and license provenance are recorded in THIRD-PARTY-NOTICES.md.
- Expanded packaged everyday-English vocabulary with common words and UI/typing terms including icon/icons, toolbar, grammar, prediction, gesture, editor, and related terms; added regression coverage so simple vocabulary cannot silently disappear.
- Added Android system-bar inset handling to the main Keyboard Settings and portable-preference activities so content does not render beneath the status/navigation bars.
- Preserved version-scoped debug install isolation, no Android network permission, sensitive-field suppression, and Development/Weave nonconformant status.
- This entry describes the open Draft PR #97 candidate and is not accepted authoritative-main implementation until governed merge/readback and fresh exact-main validation occur.


### September 25, 2026 — Platform Contract 2.0 Weave control-plane migration
- Migrated the repository manifest from legacy Platform Contract 0.4 lifecycle vocabulary to Contract 2.0.
- Classified the current Keyboard line as **Weave** because integration, stabilization, accessibility hardening, physical-device acceptance, platform-system migration, and release-completeness work now dominate over initial construction.
- Kept deployment state **development**, qualification state **in-progress**, next gate **Seal**, and conformance **nonconformant**.
- Reconciled the manifest version to the implemented `0.1.4-dev` Android line.
- Pinned Platform Contract validation to current central authority `32cfe6395f6e4bc4872a99e8d0c666ea0b1ed7b8`.
- Corrected the active Development install identity in user-facing documentation to `com.goreecloud.keyboard.dev.v5`.
- This control-plane migration does not itself establish physical-device acceptance, production acceptance, Seal qualification, or Anchor qualification.


### September 25, 2026 — Visual keyboard polish and one-to-three suggestions / 0.1.4-dev
- Added an always-visible number row to the ordinary letters layout and rebalanced the five-row geometry around the existing Glaze interaction floors.
- Changed the spacebar's visible label to **English (US)**, matching conventional mobile-keyboard language affordances while preserving Space accessibility semantics.
- Added subtle differentiated utility-key material for Shift, Backspace, Enter, Symbols, and Emoji while retaining neutral Glaze surfaces for ordinary text keys.
- Replaced oversized standalone suggestion pills with a single integrated Glaze candidate bar using one-to-three equal touch segments and subtle separators.
- Changed Quill candidate generation so every non-empty ordinary-text prefix exposes at least one and at most three visible candidates. The actively typed token becomes the fallback candidate when the packaged dictionary has no stronger match.
- Preserved sensitive-editor and host no-suggestions suppression boundaries; the one-to-three contract applies only while ordinary suggestion presentation is authorized.
- Increased preferred IME sizing for the five-row letters surface to 320 dp ordinarily and 364 dp under Touch Assistance, while Android retains final measurement authority.
- Incremented the Development package to `versionCode 5` / `0.1.4-dev` and the side-by-side CI test package identity to `com.goreecloud.keyboard.dev.v5`.
- Added unit/runtime coverage for the one-to-three candidate contract and always-visible number row.
- PR #93 merged to authoritative `main` as `64d5ed5b600e247630accceaa3f5ba8be26b3143`; exact-main Android CI #294 / run `36196662346` passed, including unit tests, APK build/provenance, Android 15 runtime instrumentation, and IME activation.
- These changes remain Development behavior and do not establish physical-device, Production, Release Candidate, or Stable acceptance.


### September 25, 2026 — Quill typing-quality stabilization / 0.1.3-dev
- Replaced fixed `the / I / to` starter candidates with an empty clean-boundary strip so suggestions are driven by the word actually being typed.
- Changed candidate ordering from shortest/alphabetical preference to packaged-dictionary frequency order, while keeping exact typed words first.
- Expanded the built-in local Quill English dictionary with common inflected and irregular forms without learning from typed text.
- Added conservative automatic correction on space and common punctuation boundaries for confident one-edit misspellings, with host-prefix validation before replacement.
- Kept deterministic transient suggestions available when an editor requests `IME_FLAG_NO_PERSONALIZED_LEARNING`; the current Quill path still persists no learned typing model. Sensitive fields and `TYPE_TEXT_FLAG_NO_SUGGESTIONS` remain suppressing boundaries.
- Replaced literal crossed-key swipe matching with QWERTY geometry-aware route scoring, ordered letter coverage, detour scoring, and packaged-dictionary frequency ranking.
- Added unit coverage for frequency-ranked suggestions, autocorrect, expanded dictionary forms, and geometry-aware swipe decoding.
- Incremented the Development package to `versionCode 4` / `0.1.3-dev` and the side-by-side test package identity to `com.goreecloud.keyboard.dev.v4` so this CI-signed build does not need to replace earlier Development or preinstalled system copies.
- PR #91 merged to authoritative `main` as `cb57224dd904a54e35f0e01146d4109b357d6eb7`; exact-main Android CI #288 / run `36194656484` passed, including unit tests, APK build/provenance, Android 15 runtime instrumentation, and IME activation.
- These changes remain Development behavior and do not establish physical-device, Production, Release Candidate, or Stable acceptance.


### September 25, 2026 — Development APK installability isolation
- Changed the CI/debug physical-test package identity to `com.goreecloud.keyboard.dev.v3` and the visible label to **GoreeCloud Keyboard Dev**.
- This prevents Android from treating a CI debug-signed APK as an update to a preinstalled `com.goreecloud.keyboard` system package whose signing certificate may differ.
- Incremented the Development package to `versionCode 3` / `0.1.2-dev`.
- Updated emulator IME activation checks and artifact provenance for the Development package identity.
- CI debug signing remains test-only and is not Production, system-image, Release Candidate, or Stable signing authority.


### September 25, 2026 — Development package version 0.1.1-dev
- Incremented Android `versionCode` from 1 to 2 and `versionName` from `0.1.0-dev` to `0.1.1-dev` so the stabilized typing build is distinguishable from earlier Development installs.
- Updated CI artifact provenance to report `0.1.1-dev`.
- No input behavior, privacy authority, platform maturity, production signing, Release Candidate, or Stable qualification is changed by the version increment.


### September 25, 2026 — Android typing ergonomics stabilization
- Reserved Android navigation/system-gesture insets from the interactive keyboard area so the bottom row no longer occupies the gesture-navigation region.
- Added direct comma and period keys to the letters-layer bottom row and centered the QWERTY home row for more conventional key geometry.
- Reworked the Quill suggestion strip into bounded Glaze-style candidate surfaces and expanded the packaged local lexicon so ordinary typing produces useful completions across a much broader everyday vocabulary.
- Added general starter candidates for ordinary editors while retaining sensitive-field, no-suggestions, and no-personalized-learning suppression boundaries.
- Added a bounded local swipe-typing gesture path, visible gesture trail, crossed-key-tolerant packaged-lexicon decoding, and runtime suppression for sensitive editors and touch-exploration presentation.
- Added Android runtime coverage for bottom safe-area geometry, punctuation exposure, and swipe gesture emission plus local decoder unit coverage.
- These changes remain Development behavior; they do not establish physical-device, Production, Release Candidate, or Stable acceptance.
- PR #85 merged to authoritative `main` as `7313b5981fe309efbc4241bae5d79e61c630c751`; exact-main Android CI #275 / run `36186867768` passed, including unit tests, APK build/provenance, Android 15 runtime instrumentation, and IME activation.


### September 22, 2026 — Drive retirement completed and independently verified
- Repository migration PR #79 merged to authoritative `main` as `6071bf3b7fddf36ddaa172b7ca858948f95ae6d1`.
- Exact candidate Android CI #263 passed on `249d252efe1da5fa1a8694044d751b940ed4403a`, including the repository feature/changelog governance guard, unit tests, APK build/provenance, and Android 15 native interaction/IME activation.
- Exact-main Android CI #264 / run `35758772834` passed on `6071bf3b7fddf36ddaa172b7ca858948f95ae6d1`, including the same repository governance guard and Android 15 emulator lane.
- Authoritative `main` was read back with root `IMPLEMENTED-FEATURES.md`, `PLANNED-FEATURES.md`, and `CHANGELOGS.md` present, all six contiguous historical archive files present, and root `FEATURE-ROADMAP.md` absent.
- Legacy Drive roadmap `FEATURE-ROADMAP.docx` (`1D6Y8Tjv3fcqF6KMmcMKugqG7QmvmnzMt`) was permanently deleted only after those gates passed; independent metadata readback returned `404 Not Found`.
- Legacy Drive changelog `Change Log — Keyboard.docx` (`1aSq_9oQxxyXr-YErNBFesLn5r4_h4N0t`) was permanently deleted only after those gates passed; independent metadata readback returned `404 Not Found`.
- The repository-native records and Git history are now the sole authorized Keyboard feature/changelog authority under the active standard.
- This retirement changes documentation authority only; it does not alter Android input behavior, permissions, editor privacy rules, network behavior, platform-system runtime integration, Production Acceptance, Release Candidate qualification, or Stable qualification.

### September 22, 2026 — Repository feature/changelog governance migration candidate
- Added root `IMPLEMENTED-FEATURES.md`, `PLANNED-FEATURES.md`, and `CHANGELOGS.md`.
- Preserved all 208 non-empty legacy Drive changelog paragraphs across six contiguous repository historical-provenance files.
- Reconciled the former repository `FEATURE-ROADMAP.md` and Drive roadmap into evidence-backed implemented/open records.
- Explicitly dispositioned every legacy FR identifier.
- Corrected active governance and Android artifact provenance to current repository identity `GoreeCloud/keyboard`; the stale `GoreeCloud/goreecloud-keyboard` identity is retained only inside historical source text or explicitly labeled stale context.
- Added a repository-governance regression check requiring the new root records, all six archive parts, their contiguous source-range markers, and absence of the retired root roadmap filename.
- Reconciled current Development notes/features/specification to the accepted PR #77 and V1.6 governance baseline.
- No input behavior, Android permission, sensitive-editor policy, network behavior, platform-system runtime authority, Production, Release Candidate, or Stable state is changed by this governance migration.
- At this candidate checkpoint, Drive sources remained migration sources pending accepted merge, authoritative-main readback, post-merge validation, and deletion verification; those conditions were later satisfied by the completion entry above.

### September 21, 2026 — PR #77: Touch Assistance runtime sizing
- Merged PR #77 to authoritative `main` as `2dd42a70b2ca74e55fe3b3fcb3fca0547e06316a`.
- Applied the existing 56 dp Touch Assistance interaction floor consistently to the suggestion strip and alternate-character targets.
- Requested a 308 dp preferred four-row IME height when touch exploration is active, while preserving Android/system parent measurement authority.
- Exact candidate Android CI #260 / run `35643340747` passed.
- Exact-main Android CI #261 / run `35662756227` passed on the merged revision.
- Keyboard remains Development / nonconformant; representative device/accessibility and release gates remain open.

### September 21, 2026 — PR #73: bounded GLAZE UI V1.6 runtime context
- Merged PR #73 as `a60aeea165a1cc3de6ab69db65967389a836eea4`.
- Added a bounded V1.6 presentation policy using Android font scale, animator enablement, and touch-exploration state.
- Preserved the historical V1.2-derived optical substrate rather than relabeling it as a completed V1.6 migration.
- Added no typed-text, clipboard, network, telemetry, account, or new authorization authority.

### September 21, 2026 — PR #70: Platform Contract 0.4 and current repository identity
- Merged PR #70 as `21715abcb4fe8112c6322f167dfa68de8f7c9df4`.
- Migrated the machine-readable control plane to Platform Contract 0.4.
- Corrected current repository identity to `GoreeCloud/keyboard`.
- Declared the nine Integral Platform Systems and added Policy/Observability as explicit applicable-blocked systems.
- Set current Official Stable GLAZE UI target to V1.6 / `1.6.0` while retaining the historical V1.2 implementation as migration evidence.
- Added root `NOTES.md`.
- This was governance/documentation/control-plane reconciliation; it did not establish platform-system runtime acceptance or production state.

### September 9, 2026 and earlier — migrated Drive chronology
See the six-part complete historical archive linked above. The archive preserves the source chronology and original status/evidence language.

## Drive retirement completion

Keyboard's mapped Drive roadmap/changelog retirement is complete and verified.

Verified completion conditions:
1. PR #79 was accepted through the repository workflow and merged to authoritative `main`.
2. Exact candidate Android CI #263 passed.
3. Root `IMPLEMENTED-FEATURES.md`, `PLANNED-FEATURES.md`, and `CHANGELOGS.md` plus all six historical archive parts were read back from authoritative `main`.
4. Root `FEATURE-ROADMAP.md` was confirmed absent from authoritative `main`.
5. Active repository identity/governance references were reconciled to `GoreeCloud/keyboard` and the repository-native authority model.
6. Exact-main Android CI #264 / run `35758772834` passed on merge `6071bf3b7fddf36ddaa172b7ca858948f95ae6d1`.
7. Drive roadmap ID `1D6Y8Tjv3fcqF6KMmcMKugqG7QmvmnzMt` was permanently deleted and independently verified `404 Not Found`.
8. Drive changelog ID `1aSq_9oQxxyXr-YErNBFesLn5r4_h4N0t` was permanently deleted and independently verified `404 Not Found`.

Do not recreate, synchronize, mirror, back up, or retain active Keyboard feature-roadmap or changelog copies in Google Drive.

## Maintenance rule

Record meaningful implementation, architecture, accessibility, privacy/security, migration, compatibility, lifecycle, deployment, recovery, release, and correction events here with exact evidence when relevant. Preserve historical facts; do not rewrite old evidence to match later architecture.
