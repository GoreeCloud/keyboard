# GoreeCloud Keyboard — Feature Roadmap

**Status:** Active roadmap control  
**As of:** 2026-09-21  
**Authoritative project record:** Project Specification — Keyboard  
**Canonical repository:** GoreeCloud/keyboard  
**Drive control:** `GoreeCloud/Feature Roadmap/GoreeCloud Keyboard/FEATURE-ROADMAP.docx`

## Purpose

This file is the repository-side feature roadmap control for GoreeCloud Keyboard. It records verified Development progress and remaining stabilization/release obligations without replacing the authoritative project record, exact-revision evidence, or GoreeCloud Tasks Management.

## Current verified Development checkpoint

Authoritative `main` is `a60aeea165a1cc3de6ab69db65967389a836eea4` from merged PR #73. The accepted candidate consumed Android font scale, animator enablement, and touch-exploration state through a bounded GLAZE UI V1.6 presentation-context layer and applies the existing 56 dp touch-assistance floor to alternate-character targets. Exact candidate `da34a12d0759be058f28786ab3ceac9fb899569a` passed Platform Contract run `35630853814` and Android CI run `35630853196` before guarded merge.

PR #70 previously migrated the control plane to Platform Contract 0.4, all nine Integral Platform Systems, current repository identity, and current Stable GLAZE UI V1.6 authority. Keyboard remains Development/nonconformant. Its optical substrate still implements historical repository-local V1.2 / 1.2.0 semantics, so PR #73 is partial V1.6 runtime-context adoption rather than complete V1.6 application conformance.

## Roadmap

| ID | Feature / obligation | Priority | Current state |
| --- | --- | --- | --- |
| FR-001 | Keep repository and Drive roadmap controls synchronized with verified reality. | High | Ongoing control |
| FR-002 | Keep actionable Keyboard stabilization obligations in GoreeCloud Tasks Management without duplicating authority. | High | Ongoing control |
| FR-003 | Complete GLAZE UI V1.6 optical/component migration while preserving privacy-safe keyboard semantics. | High | Partial: V1.6 runtime presentation context is integrated; V1.2 optical substrate remains. |
| FR-004 | Complete whole-keyboard rendered, accessibility, large-text, RTL/Arabic, adaptive/form-factor, representative-device, latency/performance, and Human Visual Excellence acceptance. | High | Open |
| FR-005 | Preserve zero-environmental-memory privacy boundaries and complete Privacy Shield/Wardveil/Policy runtime acceptance. | High | Open |
| FR-006 | Complete Everkeep portability/recovery scope beyond the current bounded one-field preference format and prove clean-target recovery. | High | Open |
| FR-007 | Complete remaining applicable Manager, Mesh, Identity, Observability, and other nine-system runtime acceptance. | High | Open |
| FR-008 | Complete protected signing/distribution, rollback, Release Candidate, production, and Stable qualification. | High | Open |

## Maintenance and synchronization

This roadmap and the corresponding canonical Drive roadmap must remain materially synchronized with one another and with the authoritative project or service record. Update both copies whenever feature scope, priority, dependency, implementation status, cancellation, supersession, recommendation, or verification state materially changes.

No feature may be represented as complete or Stable solely because it appears in this roadmap. Completion and lifecycle claims require the applicable authoritative implementation, validation, review, release, production, and stabilization evidence.

## Reconciliation rule

At each material feature change, reconcile this roadmap against current authoritative repository state, the applicable platform-system requirements, and GoreeCloud Tasks Management. Missing obligations, stale status, duplicated work, roadmap drift, or undocumented disposition changes are defects to correct.
