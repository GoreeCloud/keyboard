# GoreeCloud Keyboard — Legacy Drive Changelog, Part 04

> Historical provenance migrated from Drive `Change Log — Keyboard.docx`. This part preserves non-empty source paragraphs 106–140 of 208; text is verbatim and only Word heading styles are normalized to Markdown. Historical Draft/candidate/lifecycle statements do not override current `main`.

Change: added a fully local emoji search model over only the emoji already packaged in KeyboardLayout. Search supports representative plain-language keywords, category names, exact emoji queries, multi-token AND matching, deterministic deduplication, and a hard 24-result cap. No network permission, telemetry, account lookup, cloud catalog, or remote personalization path was added.

Validation: Android CI run #61 completed successfully on the exact PR head, including the repository's Android validation path and new offline-search unit coverage.

Acceptance boundary: Development search-model foundation only. The IME does not yet render a search field or search-result surface, and physical-device/accessibility/rendered acceptance, signed release packaging, production qualification, and Stable promotion remain pending.

### 2026-08-31 — Transient Offline Emoji Search Session — Development

Draft PR #25, stacked on the bounded offline emoji-search foundation, reached exact head 00d84dbfe68c5a26190d06eb9ba50411fdf895fc. Added explicit transient search-session state with open/close behavior, a 48-Unicode-code-point query ceiling, complete-code-point backspace, no query collection while search is closed, and clearing of query/results on close. Result discovery delegates only to the existing packaged OfflineEmojiSearch model.

Validation: Android CI run #62 / workflow run 33408520598 completed successfully on the exact head.

Privacy and acceptance boundary: no query persistence, telemetry, account lookup, network request, remote catalog, cloud personalization, or synchronization path was introduced. KeyboardView still needs to render and route the search affordance; physical-device/accessibility acceptance, signed release packaging, production qualification, and Stable promotion remain separate gates.

Rendered Offline Emoji Search — Exact-Head Development Acceptance

Repository: GoreeCloud/goreecloud-keyboard. Draft PR #26 at exact head c14a932456d912559850f63224fa9352546cb105.

Change: Rendered fully offline emoji search inside the native Emoji layer. Search query text, spaces, and backspace are intercepted by the transient EmojiSearchSession and are never sent through InputConnection or Quill composing state. Local catalog results are bounded, the top strip presents a small result set, only a deliberately tapped emoji result is committed to the active editor, and recents record selected emoji rather than search queries. No network source, telemetry, account lookup, cloud personalization, or query persistence is introduced.

Validation: Android CI run #18 / workflow run 33412579571 completed successfully on the exact head. Unit tests and the debug APK build passed, and the Android 15 / API 35 interaction-emulator job completed successfully.

Acceptance boundary: Exact-head Development source/build/emulator acceptance only. Representative physical-device validation, broader accessibility and rendered Glaze UI 2.1 consumer acceptance, signed release packaging, production acceptance, and Stable qualification remain separate gates.

### August 31, 2026 at 4:16 PM CDT — Local Long-Press Key Alternates Foundation and Draft PR #28

Change type or category: Native Android keyboard development; long-press input foundation; privacy-preserving local lookup; unit testing; Development documentation.

Affected project and environment: GoreeCloud Keyboard; repository `GoreeCloud/goreecloud-keyboard`; branch `agent/local-key-alternates-foundation`; Draft PR #28; stacked on `agent/regional-indicator-deletion-parity`; no production runtime change.

Purpose and previous state: The preceding validated Development slice improved deletion parity for regional-indicator flag sequences and other bounded text units. This continuation adds a small local foundation for long-press character alternates without reading surrounding text or introducing predictive behavior.

Changes completed:

• Added `android/app/src/main/kotlin/com/goreecloud/keyboard/KeyAlternates.kt`.

• Added deterministic alternates for common Latin letters `a`, `c`, `e`, `i`, `n`, `o`, `u`, and `y`, including common diacritic forms and ligatures where included in the catalog.

• Added selected punctuation alternates for period, hyphen, apostrophe, quotation mark, question mark, and exclamation mark.

• Uppercase letter keys derive uppercase alternates deterministically with `Locale.ROOT`.

• Empty and unsupported labels return an empty alternate list rather than inventing context-dependent choices.

• Added `KeyAlternatesTest.kt` covering lower-case, upper-case, punctuation, empty, unsupported, and explicitly context-dependent labels.

• Added `docs/development/local-key-alternates-foundation.md` documenting the privacy boundary and the future native popup-composition step.

• Opened Draft PR #28, `Add local long-press key alternates foundation`, against the exact validated regional-indicator parent branch.

Validation and result: Exact head `acdb1bdd0f9b9df77e90d1f54f7a53c36d26825c` completed Android CI run 65 successfully. The new native foundation is source-validated on its current exact head.

Privacy and authority state: Alternate lookup uses only the visible key label supplied by the layout. It performs no network request, learning, personalization, clipboard read, surrounding-text inspection, persistence, language-model inference, sensitive-field classification, or production service integration. It does not bypass the existing IME text-commit authority path.

Current state and follow-up: Draft PR #28 remains Development work. The next bounded native slice can implement accessible long-press popup rendering, pointer movement, selection feedback, cancellation, and final alternate commit behavior using this catalog.

Development — Draft PR #29: native long-press key alternates

Wired the deterministic local KeyAlternates catalog into KeyboardView using the platform long-press timeout, a Glaze 48 dp alternate-cell floor, haptic feedback, slide selection, cancellation, and accessibility announcements. Alternate characters commit through the existing listener while KeyboardService remains the Shift/text-commit authority. After restoring the repository-required base-key listener path, the corrected exact head passes the Glaze UI/Motion boundary guard, unit tests, debug APK build, and Android 15 native interaction runtime emulator suite. No network lookup, learning, clipboard read, surrounding-text inspection, or persistence is added. Stable acceptance is not claimed.

Development — Draft PR #30: rendered bounded alternate popup geometry

Updated the native long-press alternate renderer to consume AlternatePopupLayout directly for viewport-bounded popup placement and per-item hit geometry. This removes duplicated renderer-side row/column calculations while retaining the existing deterministic local alternate catalog and IME commit authority.

The renderer now fails closed if no valid popup fits the current viewport by clearing item bounds and nulling selection, preventing an invisible alternate from being committed on release. No network lookup, surrounding-text inspection, clipboard read, learning, personalization, or persistence was introduced.

Validation: exact documented head 73209e69fc996f9e9a77c8bf1d07c87cddc0a776 passed Android CI #75 / workflow run 33594290017. Status remains Draft / Development; representative physical-device gesture, accessibility, compact-width, production-release, and Stable acceptance remain pending.

### September 2, 2026 — Bounded Alternate Popup Hit Testing — Development
