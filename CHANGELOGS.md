# GoreeCloud Keyboard — Changelogs

**Record type:** Authoritative repository changelog index and current change history  
**Repository:** `GoreeCloud/keyboard`  
**Lifecycle:** Development / nonconformant  
**Migration state:** Complete on authoritative `main`; PR #79 merged as `6071bf3b7fddf36ddaa172b7ca858948f95ae6d1`, exact-main Android CI #264 passed, and the mapped legacy Drive roadmap/changelog sources were permanently retired and independently verified absent on September 22, 2026.  
**Current governance baseline:** `6071bf3b7fddf36ddaa172b7ca858948f95ae6d1` (PR #79).  
**Current runtime-bearing baseline:** `7313b5981fe309efbc4241bae5d79e61c630c751` (PR #85); exact-main Android CI #275 / run `36186867768` passed.  
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

### September 25, 2026 — Side-by-side Development APK installability
- The debug/Development APK now uses application ID `com.goreecloud.keyboard.dev` and the visible name **GoreeCloud Keyboard Dev**.
- This prevents the CI debug-signed test APK from attempting to replace a preinstalled/system GoreeCloud Keyboard package with application ID `com.goreecloud.keyboard`, which can be rejected when the system copy uses a different signing certificate.
- CI provenance and Android 15 IME activation checks now validate the Development package identity explicitly.
- The production/system package identity remains `com.goreecloud.keyboard`; no production signing authority is implied.


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
