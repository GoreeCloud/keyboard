# GoreeCloud Keyboard — Changelogs

**Record type:** Authoritative repository changelog index and current change history  
**Repository:** `GoreeCloud/keyboard`  
**Lifecycle:** Development / nonconformant  
**Migration state:** Candidate on `migration/repository-feature-records-20260922`; becomes authoritative only after accepted merge to `main`.  
**Current baseline:** `2dd42a70b2ca74e55fe3b3fcb3fca0547e06316a` (PR #77).  
**Governing standard:** Standard — Repository Feature Tracking and Changelog Governance v1.0.

## Authority and interpretation

This file becomes the repository-local changelog authority after accepted migration. Historical Keyboard chronology formerly stored in Google Drive is preserved in `docs/changelog-history/legacy-drive-keyboard-changelog.md`.

The historical archive preserves every non-empty paragraph from `Change Log — Keyboard.docx`. Historical Draft/candidate/lifecycle statements apply only to their original context and do not override current authoritative `main`.

Draft or unmerged pull requests are not accepted changes. PR #78 and older open stacked Drafts remain candidate-only.

## Historical Drive archive

Legacy source:
- `Change Log — Keyboard.docx`
- Drive file ID `1aSq_9oQxxyXr-YErNBFesLn5r4_h4N0t`
- migrated repository archive: [`docs/changelog-history/legacy-drive-keyboard-changelog.md`](docs/changelog-history/legacy-drive-keyboard-changelog.md)

The archive preserves the Drive chronology as historical provenance, including merged milestones, failed/corrected validation attempts, Draft candidate evidence, Glaze transitions, local input/privacy work, accessibility work, and release-boundary statements.

## Current repository changelog

### September 22, 2026 — Repository feature/changelog governance migration candidate
- Added root `IMPLEMENTED-FEATURES.md`, `PLANNED-FEATURES.md`, and `CHANGELOGS.md`.
- Preserved the complete non-empty legacy Drive changelog text in a repository historical-provenance file.
- Reconciled the former repository `FEATURE-ROADMAP.md` and Drive roadmap into evidence-backed implemented/open records.
- Explicitly dispositioned every legacy FR identifier.
- Corrected active governance to current repository identity `GoreeCloud/keyboard`; the stale `GoreeCloud/goreecloud-keyboard` identity is retained only where historically necessary.
- Added a repository-governance regression check requiring the new root records and rejecting the retired root roadmap filename.
- Reconciled current Development notes and CI provenance to accepted current state.
- No input behavior, Android permission, sensitive-editor policy, network behavior, platform-system runtime authority, Production, Release Candidate, or Stable state is changed by this governance migration.
- Drive sources remain migration sources until this candidate is accepted, authoritative `main` is read back, post-merge validation passes, and the deletion gate is satisfied.

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
See the complete historical archive linked above. The archive preserves all material legacy entries and their original status claims.

## Drive retirement gate

The mapped Drive roadmap and changelog must not be deleted until:
1. this migration is accepted through the repository workflow;
2. applicable exact-head checks pass;
3. the three root records and historical archive are read back from authoritative `main`;
4. retired root `FEATURE-ROADMAP.md` is confirmed absent;
5. current repository references/governance are reconciled; and
6. applicable post-merge validation passes on the accepted revision.

Only after those gates pass may these two mapped legacy Drive sources be permanently deleted:
- roadmap `FEATURE-ROADMAP.docx` — `1D6Y8Tjv3fcqF6KMmcMKugqG7QmvmnzMt`
- changelog `Change Log — Keyboard.docx` — `1aSq_9oQxxyXr-YErNBFesLn5r4_h4N0t`

After deletion, independently verify both IDs return not found, then record the retirement event in a narrow follow-up change.

## Maintenance rule

Record meaningful implementation, architecture, accessibility, privacy/security, migration, compatibility, lifecycle, deployment, recovery, release, and correction events here with exact evidence when relevant. Preserve historical facts; do not rewrite old evidence to match later architecture.
