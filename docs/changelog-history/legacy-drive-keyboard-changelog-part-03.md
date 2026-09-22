# GoreeCloud Keyboard — Legacy Drive Changelog, Part 03

> Historical provenance migrated from Drive `Change Log — Keyboard.docx`. This part preserves non-empty source paragraphs 71–105 of 208; text is verbatim and only Word heading styles are normalized to Markdown. Historical Draft/candidate/lifecycle statements do not override current `main`.

Repository: GoreeCloud/goreecloud-keyboard. Pull Request #16 replaced cycle-through-only emoji category navigation with a direct local category strip exposing Smileys, People, and Symbols simultaneously. The selected category uses the existing Glaze key surface/stroke presentation, category state remains in-memory, and all emoji input continues through the existing complete-String commit and bounded Unicode text-unit deletion path.

Exact head bdb5749498c8e0b296466ca8741b6ef5090a476c passed Android CI workflow run 33287954889. PR #16 was squash-merged as 9ce05f56aaeab52ceea37e42e4a555d90e199b97.

Acceptance boundary: Development emoji navigation only; no network emoji search, recents/history persistence, GIF or sticker service, telemetry, broader UAX #29 guarantee, multilingual/device/accessibility production acceptance, voice input, signed release, or Stable qualification.

### August 30, 2026 — Process-Memory Emoji Recents Merged

Repository: GoreeCloud/goreecloud-keyboard. Pull Request #17 added a pure bounded emoji recency model and wired it into the existing Unicode-safe Android keyboard commit path. Recent values preserve complete String payloads, deduplicate by promoting reused emoji to the front, retain at most 24 entries, and expose a Recent category only when recents exist. Smileys, People, and Symbols remain directly selectable.

Privacy boundary: recents exist only in IME process memory. They are not persisted, synchronized, uploaded, sent to telemetry, or fetched from a network service; process-state loss clears them.

Validation history: initial exact head ac7cbe08b78548234c2d7c84a23ff0639f59bbff failed at the Glaze UI/Motion evaluation source check because that governance script still required the previous one-line Action.TEXT source pattern. The validator was strengthened to require the new Action.TEXT block, emojiRecents.record(hit.key.label), and the unchanged listener?.onText(hit.key.label) commit path. Repaired exact head 6033a4a15eb54cbe5ea45766a41c5ce404ff07b5 passed Android CI workflow run 33289202856, including emulator validation. PR #17 was squash-merged with expected-head protection as ca4e947d20d419bf96b972e44195044b33d25422.

Acceptance boundary: Development input behavior only. Complete emoji catalog/search, persistence/sync, GIF/sticker services, multilingual input, complete rendered Glaze/accessibility acceptance, physical-device acceptance, signed production packaging, release, and Stable qualification remain pending.

.

Explicit Local Emoji Recents Clearing Merged

PR #18 merged from candidate head 98833d44565bb04729aaa5842ad34164f82314f2 to main as c9085ac7202776ee53679bf41b9ef609aa035ab9 after Android CI run 33320368573 succeeded.

Added an explicit Clear operation for process-memory-only emoji recents and a visible Clear strip action only while recents exist. Clearing immediately exits Recent mode and returns to the selected local category. No persistence, sync, telemetry, network source, production, release, or Stable claim.

### 2026-08-30 — Expanded Offline Emoji Catalog — Development

Merged GoreeCloud/goreecloud-keyboard PR #19 to main as 2aedbfdc6e798d4572fedfbe4d0e83d215ecf4f7 from exact tested head 8fe9957b12ef0bd43a1382f917a7076c9eda2bc6. Android CI run #50 passed.

Expanded the first-party local emoji catalog to Smileys, People, Nature, Food, and Symbols. Every category remains bounded to three rows / 24 exact text units, deterministic cycling remains local-only, and tests continue to verify whole-text-unit deletion for composed emoji sequences.

Status: Development. The IME remains zero-network for this catalog; no telemetry, cloud personalization, remote emoji search, production distribution acceptance, or Stable qualification is introduced.

### 2026-08-30 — Compact Accessible Emoji Category Strip — Development

Merged GoreeCloud/goreecloud-keyboard PR #20 to main as 658fefa27a0d140518ad4243a1d2e9a04312a725 from exact tested head e2eb5af12af3e682cab9bd6bc86cca605bae415e. Android CI run #52 passed.

Replaced long visible emoji category labels with compact icon labels while retaining complete accessibility names. Local Recent and Clear controls appear only when recents exist, all first-party categories remain represented, and category/control actions are announced for accessibility.

Status: Development. The catalog remains local-only and this adds no network search, telemetry, cloud personalization, new input authority, production distribution acceptance, or Stable qualification.

### 2026-08-30 — Device-Local Persistent Emoji Recents — Development

Merged GoreeCloud Keyboard PR #21. Emoji recents now persist in Android private application preferences across IME process restarts, remain bounded to 24 exact emoji String values, and stay entirely device-local with no new network permission, telemetry, account dependency, learned-use profile, or synchronization path. Clear recent emoji removes persisted state as well as the current in-memory list.

The first candidate head aaa2402f9178649cdd04a2e042852b89055f44e0 failed Android CI #54 only because the new codec test used the wrong test-library imports; that head was not merged. JUnit 4 imports were corrected, producing replacement exact head 8c35e8175417fdb44be99c1e3f2db67615e72b07. Android CI #55 succeeded on that replacement head. Squash merge main SHA: 055cd378c617db2dd705172e510abd5c07e285a3.

Classification: Development. No cloud personalization, cross-device sync, account-backed state, signed release, or Stable claim.

### 2026-08-30 — Remembered Emoji Category — Development

Merged PR #22 from exact validated head a087a356e050385002cf97909cf5af1e306ec308 as squash commit 8eeb95dd601af766301cb0b8cd0f0d56f0ba09dc.

The Android keyboard now restores the user's last explicitly selected emoji category from private application preferences. Only a bounded EmojiCategory enum name is persisted. Invalid or unknown values fall back to Smileys. Category persistence remains independent from the existing local emoji-recents store.

Validation: Android CI run #57 completed successfully on the exact head, including unit tests, debug APK build, Glaze UI/Motion boundary checks, and Android 15 native interaction evaluation.

Acceptance boundary: Development only. No new permission, network path, telemetry, account dependency, cloud synchronization, learned-language/personality profile, backup acceptance, production qualification, or Stable claim is introduced.

### 2026-08-31 — Offline Travel Emoji Category — Development

Added a sixth fully offline Travel category to the native emoji catalog, bounded to three rows of eight entries and integrated with the compact category strip, whole-sequence deletion behavior, and remembered local category state.

Evidence: PR #23; exact tested head 7fa8a83f0da5d98a389ac7bf18295c59943a48ca; Android CI #59 success; merged main SHA be2e1b163d7881829a27dee454388365b245b514.

Status: Development. No remote catalog, telemetry, cloud personalization, production IME acceptance, or Stable claim.

### 2026-08-31 — Bounded Offline Emoji Search Foundation — Development

Repository: GoreeCloud/goreecloud-keyboard. Draft PR #24 on branch agent/offline-emoji-search at exact head 92d0ac14b7ccfef577c8d2e504276d11cf4f49d5.
