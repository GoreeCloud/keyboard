# GoreeCloud Keyboard — Legacy Drive Changelog, Part 02

> Historical provenance migrated from Drive `Change Log — Keyboard.docx`. This part preserves non-empty source paragraphs 36–70 of 208; text is verbatim and only Word heading styles are normalized to Markdown. Historical Draft/candidate/lifecycle statements do not override current `main`.

Final exact head 997a3248dbf6d1d28b94a6fefdae7dbd5fdf6f5b passed Android CI run 33262971436, including source/build validation and the Android 15 native-interaction emulator job. Replacement pull request 9 was merged to authoritative main as de9563a9eda9510fd2d05bae6968b53e590914d5.

Physical-device typing and accessibility acceptance, supported Android/application matrix testing, tablet/foldable adaptation, complete Glaze UI rendered/native acceptance, signed production packaging, Apple-platform implementation, and Stable qualification remain separate gates.

### 2026-08-29 — Expanded native Android symbol input

Repository: GoreeCloud/goreecloud-keyboard

Pull request: #10 — Expand native Android symbol input

Validated candidate head: 977490892967f77807116c45a84028df9e6050d9

Validation: Android CI #28 / workflow run 33267288819 — success, including emulator interaction coverage for secondary-symbol navigation and rendered `[` key commit.

Merged authoritative main: 3f67b769e91f2e939321cd5312e9deb9f55afc15

Change: added a second first-party symbol page with brackets, operators, currency and typographic symbols plus direct navigation among letters/primary/secondary symbol layers; preserved letters-only shift and excluded symbols from local Quill composing context.

Acceptance boundary: Development input expansion only; emoji, multilingual layouts/language switching, complete TalkBack/switch-access and representative physical-device acceptance, signed release packaging, and Stable qualification remain unaccepted.

Unicode-Safe Native Key Input — Merged Development Milestone

Repository: GoreeCloud/goreecloud-keyboard

Pull request: #11 — Make native key input Unicode-safe

Validated candidate head: beaf282ca68cfec747c74a9fd64b007a4a865e9d

Validation: Android CI workflow run 33270909108 — success.

Merged authoritative main: 1c80c2f9e413ac8f3a07d5db89fb8d0e1a92ee3b

Change: replaced UTF-16 Char key payload assumptions with complete String text payloads across the native layout/view/service boundary, made deletion and suggestion replacement code-point aware, and kept suggestion composition limited to letter-only text. Validation/runtime checks were reconciled to the new listener contract before merge.

Acceptance boundary: this is Unicode correctness infrastructure only; emoji browsing, multilingual layouts, gesture/swipe typing, clipboard history, voice input, signed release, real-device acceptance, and Stable qualification remain unaccepted.

Bounded Native Emoji Layer — Merged Development Milestone

Merged PR #12 to main as d66ae1f74b01b918cb6e73141310700c48634bc5 after Android CI run 33273125941 completed successfully.

Added a first-party EMOJI layer using the complete-String native key contract, with navigation from letters and symbol layers and validation that supplementary Unicode emoji remain complete single-code-point keys rather than split UTF-16 units.

Acceptance boundary: this is a bounded single-code-point emoji surface, not a full searchable/categorized picker or proof of multi-code-point grapheme composition/deletion. Multilingual input, gesture typing, voice input, physical-device acceptance, release, and Stable qualification remain separate work.

Bounded Unicode Text-Unit Backspace — Merged Development Milestone

Merged commit: 18d1e0e1d2946b3c215851ecbe49848d32fb8726.

Added bounded Unicode-aware backspace for common emoji modifiers, ZWJ-linked emoji, regional-indicator flags, keycaps, variation selectors, combining marks, and CRLF. Ordinary input reads a bounded 64 UTF-16-code-unit look-behind and deletes the computed unit by code point. Sensitive editors deliberately skip look-behind inspection and retain one-code-point deletion.

This is not a claim of complete UAX #29 grapheme segmentation, a complete emoji picker, multilingual editing acceptance, release, physical-device acceptance, or Stable qualification.

### August 29, 2026 at 6:15 PM CDT — Bounded Composed Emoji Layer Merged

Change type or category: Native Android input development; Unicode/emoji correctness; bounded grapheme-like deletion compatibility; source validation; source-control merge.

Repository and pull request: GoreeCloud/goreecloud-keyboard; branch agent/composed-emoji-layer; Pull Request #14 — Add bounded composed emoji keys.

Exact accepted head: 6069023d7ff46040f23bea1c8683660dc24798ea.

Validation: Android CI workflow run 33281183703 completed successfully.

Implementation: Expanded the existing first-party EMOJI layer from the earlier single-code-point-only set to a bounded set that includes selected variation-selector, skin-tone modifier, ZWJ profession/family, rainbow-flag, and regional-indicator flag sequences while preserving the complete-String key commit path. Tests verify representative composed-sequence classes and require every currently exposed emoji key's full code-point count to equal TextDeletion.previousTextUnitCodePointCount(key), so each exposed key is compatible with one bounded backspace action.

Merge result: Pull Request #14 was squash-merged with expected-head protection to authoritative main as 5103615397de2d9f93fe033295659827f76ce377.

Acceptance boundary: This is still a deliberately bounded emoji surface. It does not implement categories, recents, search, comprehensive composed-sequence coverage, or claim globally complete Unicode UAX #29 grapheme segmentation. Multilingual input, gesture typing, voice input, representative physical-device acceptance, signed release packaging, and Stable qualification remain separate work

### August 29, 2026 — Direct Local Emoji Category Selection Merged
