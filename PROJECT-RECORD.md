# GoreeCloud Keyboard — Project Record

**Repository:** `GoreeCloud/keyboard`  
**Lifecycle:** Weave / nonconformant; deployment state: Development  
**Record purpose:** Significant project history, architecture and governance transitions, migration evidence, and acceptance evidence  
**Canonical authority:** This file is the repository-local project record once accepted on the default branch.  
**Migration source:** `Project Specification — Keyboard.docx`, Drive file `1UOFZJXwieJkHePHajVwCy2zkhKXFofYD`, internal version v1.0.

## Product establishment and native direction

GoreeCloud Keyboard is an original GoreeCloud-owned Android input-method application built around Android `InputMethodService` and a first-party rendering/input surface. Product direction emphasizes private local typing, sensitive-editor handling, accessible and responsive interaction, first-party Quill assistance, and strict separation between ordinary text entry and any future network-backed capability.

Apple-platform support remains product direction rather than an accepted implementation claim.

## Development foundation preserved from the Drive specification

The frozen project specification records the native Android foundation, including first-party QWERTY/symbol input, bounded local Quill suggestions, sensitive-editor suppression, no Android network permission, Glaze design-system requirements, privacy/security governance, and planned capability areas such as autocorrect, multilingual input, gesture typing, emoji/symbol expansion, clipboard tools, voice input, one-handed/split layouts, tablet/foldable adaptation, dictionaries, Quill-assisted writing, and optional synchronized preferences where separately approved.

It also records Secure Paste as a privacy-governed design direction. These requirements are preserved in `PROJECT-SPECIFICATIONS.md`; they do not become implementation claims merely because they were present in the Drive source.

## 2026-09-22 — Repository-native feature and changelog authority accepted

Repository-native feature/changelog governance was accepted on authoritative `main` through PR #79 as commit `6071bf3b7fddf36ddaa172b7ca858948f95ae6d1`.

The current `PLANNED-FEATURES.md` and `CHANGELOGS.md` state that the mapped legacy Drive roadmap/changelog sources were permanently retired and independently verified absent. Historical changelog chronology is preserved under `docs/changelog-history/`.

This project record does not duplicate that complete release/change chronology. `CHANGELOGS.md` remains the change-history authority; `IMPLEMENTED-FEATURES.md` and `PLANNED-FEATURES.md` remain feature-state authorities.

## Current repository baseline at migration

At the start of this project-specification migration, the repository-native governance baseline remains `6071bf3b7fddf36ddaa172b7ca858948f95ae6d1` (PR #79).

The current runtime-bearing capability baseline recorded by the repository is `64d5ed5b600e247630accceaa3f5ba8be26b3143` (PR #93), with exact-main Android CI #294 / run `36196662346` passed.

The repository's current specification baseline is newer than the frozen Drive v1.0 source and records the governed consumer target as GLAZE UI V1.6 / `1.6.0` at exact Stable release source `a7180679ea851389e0f3004515f9a25f420e716d`. Historical V1.3 and earlier statements from the Drive source remain provenance/requirement history and do not override the current repository target.

## 2026-09-27 — Project specification migration staged

A documentation-only branch, `docs/project-governance-migration-20260927`, was created from authoritative `main`.

The migration:
- creates root `PROJECT-SPECIFICATIONS.md`;
- creates root `PROJECT-RECORD.md`;
- incorporates the newer repository `SPECIFICATIONS.md` as the implementation-aware baseline;
- preserves the complete substantive Drive DOCX v1.0 specification as migrated requirement/provenance material;
- reconciles the historical repository name `GoreeCloud/goreecloud-keyboard` to `GoreeCloud/keyboard` where it is a current reference;
- updates repository navigation and related authority references; and
- retires the competing root `SPECIFICATIONS.md` after incorporation.

This documentation migration does not promote Keyboard beyond its current Development/Weave state and does not establish Glaze application conformance, Privacy Shield acceptance, Wardveil Security acceptance, Everkeep recovery acceptance, physical-device ergonomics, signed release, production deployment, or Stable qualification.

## Drive removal and archive boundary

Drive file `1UOFZJXwieJkHePHajVwCy2zkhKXFofYD` is a frozen migration input. It must not be edited, updated, re-saved, or maintained as the project specification.

Permanent Drive removal is required only after the repository migration is accepted, exact default-branch readback confirms the canonical files, applicable validation/review requirements are satisfied, and no unresolved discrepancy remains.

If a distinct historical specification artifact requires separate retention outside Git history, the archival destination is the appropriate Dropbox archive directory, not Google Drive.

## Ongoing record relationships

- `PROJECT-SPECIFICATIONS.md` — current project requirements and architecture.
- `IMPLEMENTED-FEATURES.md` — evidence-backed implemented capability inventory.
- `PLANNED-FEATURES.md` — open, partial, blocked, deferred, and acceptance-gated work.
- `CHANGELOGS.md` — repository/change chronology and migrated historical changelog index.
- `README.md` — project entry point and current Development orientation.

Update this record when significant architecture, repository identity, governance, security/privacy, recovery, production, lifecycle, migration, deprecation, or retirement events occur.
