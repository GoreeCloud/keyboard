# GoreeCloud Keyboard — Project Specifications

> **Record:** Repository-authoritative project specification  
> **Repository:** `GoreeCloud/keyboard`  
> **Owner:** GoreeCloud  
> **State:** `main` remains documentation/governance only; [Draft PR #2](https://github.com/GoreeCloud/keyboard/pull/2) contains a build-verified upstream source candidate. Not merged, device-qualified or released  
> **Product direction:** Android-first, privacy-first, Fork-to-Native investigation using FlorisBoard as the proposed upstream starting point, with a GoreeCloud-native end state  
> **Last reviewed:** 2026-10-09  
> **Authority:** This file defines project-specific requirements. GoreeCloud governing Instructions, Policies, Standards, Rules, and applicable platform contracts take precedence. See [project record](PROJECT-RECORD.md).

## 1. Mission and ownership

Build a distinctively GoreeCloud, lightweight, secure, accessible, high-quality system keyboard with reliable offline text entry and opt-in advanced language, AI, creative, and cross-device capabilities. GoreeCloud owns product direction, system architecture, user experience, application identity, privacy boundaries, and acceptance decisions. The owner-directed complete [feature and capability catalog](../PLANNED-FEATURES.md) is the source for proposed product scope, not a claim of current availability.

**Verified repository baseline (2026-10-09):** the default branch contains `README.md` and `PLANNED-FEATURES.md` before these canonical records are added. No Android application code, builds, platform integration, acceptance tests, or release artifacts were present in that inspected tree. Historical implementation statements about a different repository must not be transferred into this repository as present-tense facts.

**Later verified candidate state (2026-10-09):** the default branch still has no Android application implementation, but [Draft PR #2](https://github.com/GoreeCloud/keyboard/pull/2) contains staged source code and build/security checks. The original bootstrap paragraph above is retained as historical evidence, not the current whole-repository state. See [Privacy and Security Review](PRIVACY-REVIEW.md) for the source-backed limitations and remaining acceptance gates.

## 2. Implementation strategy and source boundaries

- **Owner-directed starting direction:** investigate a controlled fork of FlorisBoard, preserve lawful license notices and provenance, inventory useful upstream functionality, and progressively rebuild product-defining components to GoreeCloud-native architecture and Glaze interaction standards.
- **Current fork status:** a pinned FlorisBoard source snapshot from upstream `fe1241f4921b3eae923571ff7a3e113a7e10d677` has been imported into [Draft PR #2](https://github.com/GoreeCloud/keyboard/pull/2), with source provenance recorded in the candidate. Android debug CI passed at candidate commit `f791fa05cca4be2ee84c5e84cb5b25066223a8e9` ([run](https://github.com/GoreeCloud/keyboard/actions/runs/37965243310)); more recent branch changes require their own validation. The import is **not accepted as native GoreeCloud implementation**. Full source/dependency, bundled asset, license, privacy, security and feature-preservation reviews remain open.
- **Mandatory decision gate:** audit FlorisBoard source, license and dependencies, current maintenance and security posture, Android compatibility, available tests, useful behaviors, accessibility, and the cost/risk of a fork versus native implementation. Document the concrete reason for any temporary Fork-to-Native approach and its bounded exit plan.
- **Feature/UI preservation gate:** before replacing inherited components, show that required functionality, user data, accessibility, input semantics, performance, privacy, and recovery are preserved or improved, with compatible regression evidence.
- **Product identity:** GoreeCloud application identifiers, terminology, visual identity, original icons and Glaze presentation must replace the upstream-facing shell as appropriate without misrepresenting provenance or violating license/trademark requirements.
- **Source of truth:** repository source, accepted commits, tests, build artifacts, and release records establish implementation facts; this specification establishes requirements. A prior internal Google Drive specification is a frozen migration input, not a parallel current authority.

## 3. Scope and capability taxonomy

All 41 categories and 772 enumerated feature entries are preserved in [PLANNED-FEATURES.md](../PLANNED-FEATURES.md). This file sets the engineering boundaries and acceptance gates for that catalog rather than duplicating its individual entries.

| Capability group | Catalog sections | Scope and dependency |
| --- | --- | --- |
| Typing, prediction, correction, composing | 1–5 | Offline input core first; AI features opt-in, reviewable and isolated |
| Language, translation, voice and handwriting | 6–9 | On-device where supported; explicit permission for remote processing and hardware APIs |
| Editing, clipboard, sync and productivity | 10–13 | Minimized data retention; clipboard and cross-device capabilities require scoped authorization |
| Emoji, media and personal expression | 14–18 | Local-first where feasible; online providers optional and disclosed |
| Toolbar, search, credentials and privacy | 19–23 | Platform credential APIs, contextual permission gates and secure-field suppression |
| Layouts, personalization, feedback and accessibility | 24–30 | Glaze-led, adaptive, screen-reader-compatible, testable on supported form factors |
| Dictionary, profiles, editing history and data controls | 31–37 | Explicit user ownership, export, reset, deletion, and safe persistence |
| Platform portability, performance and integrations | 38–41 | Android first; other operating systems and GoreeCloud services require platform-specific acceptance |

Capabilities described as “supported,” “available,” or “includes” in the owner-supplied vision shall be interpreted as **target product requirements** until the owning implementation and validation artifacts support narrower factual availability claims. Proposed cross-platform features must respect OS, device, credential, IME and accessibility API restrictions.

### Initial delivery boundary

The minimum useful Android milestone shall include correct key entry, backspace, cursor and composing behavior, letters/numbers/symbols, editor action keys, Unicode-safe handling, secure-field privacy, a comprehensible settings and activation experience, accessibility fundamentals, deterministic offline behavior, and robust core-input tests. Advanced AI, network services, clipboard history, stickers, GIF search, and cross-device synchronization must not delay or destabilize ordinary offline typing. No feature should be exposed as a working control until its behavior exists.

## 4. Proposed architecture — not implemented

Establish clear trust boundaries among:

1. **Android IME host:** `InputMethodService`, `InputConnection`, editor metadata and operating-system activation flow.
2. **Input core:** hit testing, gestures, compositing, cursor movement, deletion semantics, keyboard modes and language/layout contracts.
3. **Local language services:** word suggestions, dictionary and autocorrection with bounded, reviewable personalization.
4. **UI and Glaze adapter:** scalable, high-contrast keyboard surfaces, accessible semantics, adaptive layouts, preferences, and onboarding.
5. **Privacy/security services:** sensitive-editor classifier, permission mediation, retention and transmission policy, secure storage, and audit-safe diagnostics.
6. **Optional feature modules:** clipboard, translation, speech/handwriting, AI writing, media, credentials through platform providers, profile management and consent-based synchronization.
7. **Integrations:** versioned, contract-checked GoreeCloud and third-party APIs; no hidden transfer of typed text.

Keep core key input independent of the availability of external providers. Fail safely and preserve typing functionality when models, connectivity, accounts, peripherals, or services fail.

## 5. Privacy, security and data handling requirements

- Typing, prediction, basic correction and core editor navigation **must work offline** for the supported baseline.
- Never retain, upload, analyze, log or expose typed content without a justified, authorized, user-visible purpose. Do not embed typed text in crash reports, diagnostics, analytics or unexpected provider requests.
- Password and otherwise sensitive fields require fail-closed suppression of learning, history, AI, search, clipboard previews, suggestions and surrounding-text inspection unless an explicitly authorized safe use is documented.
- Personalized dictionaries, learned words, snippets, clips, profiles, AI requests, voice recordings, translation history and synchronized preferences need distinct retention, encryption, deletion, consent and export controls.
- Remote AI, search, GIF, speech, translation and sync providers are **off by default** unless a later approved product decision explicitly changes the feature-specific default. Preview and authorize remote submission of selected content, disclose destination and retention, and allow a local-only mode.
- Use supported Android credential/passkey/AutoFill APIs; the keyboard must never create a parallel plaintext credential vault or intercept secrets.
- Clipboard interception or system-wide secure paste must not be claimed beyond the privileges actually granted by the OS and approved GoreeCloud platform contracts.
- Enforce least privilege, secure dependency provenance, vulnerability review, signed releases, update continuity, secrets hygiene, and permission-scoped integrations.

## 6. GoreeCloud platform-system evaluation

Evaluate **all nine Integral Platform Systems** for applicability and acceptance. No system is presently claimed integrated in this documentation-only repository. `GoreeCloud Sync` remains separately governed.

| Integral Platform System | Keyboard evaluation scope | Evidence state |
| --- | --- | --- |
| GoreeCloud Manager | App lifecycle, version visibility and safe administration | Pending evaluation |
| Privacy Shield | Consent, sensitive input, retention, local/remote data boundaries | Pending evaluation |
| Wardveil Security | Threat controls, integrity, secure releases and hardening | Pending evaluation |
| Everkeep | Recovery, backed-up settings and user data portability | Pending evaluation |
| Glaze | Visual/interaction implementation, contrast, motion and form factors | Pending evaluation |
| GoreeCloud Mesh | Authenticated service connectivity where required | Pending evaluation |
| GoreeCloud Identity | Optional account and device authority, credential boundary | Pending evaluation |
| GoreeCloud Policy | Enforceable policy evaluation and user/admin settings | Pending evaluation |
| GoreeCloud Observability | Privacy-safe reliability signals, diagnostics and controls | Pending evaluation |

Each evaluation must result in **implemented and verified**, **not yet implemented**, **blocked**, or **not applicable with rationale**, with commit, test and runtime evidence when relevant. Do not call cosmetic conformance an integration.

## 7. Glaze and accessibility requirements

Apply the **current verified Glaze design contracts** at implementation time—not a hard-coded historical version number. Use high-quality icons and glyphs, appropriate typography, light/dark modes, accessible color contrast, reduced motion, large touch targets, user-controlled keyboard sizing, predictable focus, RTL/BiDi support and touch accommodations. Test TalkBack and Switch Access, external keyboards where supported, phone/tablet/foldable orientations, secure input, settings and onboarding. Visual polish must never obstruct key accuracy, keyboard latency, readability, accessibility or sensitive-editor safety.

## 8. Verification and release gates

A capability may be called **implemented** only when the source behavior exists in the owning repository and appropriate tests pass; **verified** only when the relevant runtime, accessibility, privacy, performance and device acceptance has been evidenced. Production or stable readiness additionally requires:

- Licensed upstream provenance, dependency audit, supply-chain and threat review.
- Functional and regression tests for editor contracts, Unicode/graphemes, backspace, language modes, suggestion/correction rejection, secure fields, restarts and offline operation.
- Compatibility testing on supported Android versions, physical devices, representative host apps, and tablet/foldable form factors where claimed.
- Accessibility and representative human usability testing; visual Glaze acceptance; appropriate nine-system assessments.
- Data deletion/export tests, network/permission audit, controlled AI/transmission consent, negative security tests, and recovery/rollback verification.
- Reproducible accepted build, exact-revision CI evidence, protected signing, tested upgrade path, provenance, and explicit release authorization.

Do not infer release, APK availability, production readiness or Anchor/Stable status from documentation, an imported upstream tree, an open PR, or CI green alone.

## 9. Delivery stages and maintenance

1. **Baseline and provenance:** inspect upstream, license, migration/dependency risks, and reference UX; establish tests, source control and explicit stage gates.
2. **Secure Android typing foundation:** validate IME core, keyboard layouts, Unicode/input contracts, privacy protections, activation and onboarding.
3. **Daily-driver input quality:** mature corrections, predictive text, gesture typing, languages, dictionaries, cursor control, and accessible adaptation.
4. **User-owned productivity:** deliberate clipboard/snippets, editing history, profiles, settings, text export and optional backup.
5. **Permissioned assistance and media:** AI, translation, voice, handwriting, search, stickers/GIF/emoji creation with modular provider controls.
6. **Ecosystem and platform expansion:** independently accepted integrations, optional encrypted sync and platform-specific companion implementations.
7. **Release qualification:** complete evidence matrix, device testing, privacy/security audits, updates, recovery and release approval.

Stages are ordering guidance, not claims that an individual stage has shipped or been accepted. Maintain regression coverage and upstream security review throughout any fork transition. Record material architecture and migration decisions in [PROJECT-RECORD.md](PROJECT-RECORD.md); track implementation obligations through the governed GoreeCloud task-management process.

## 10. Document control

- **Canonical requirement source:** `docs/PROJECT-SPECIFICATIONS.md`.
- **Detailed owner-supplied capability inventory:** `PLANNED-FEATURES.md`.
- **Historical decision and evidence record:** `docs/PROJECT-RECORD.md`.
- **Drive migration:** an older internal `Project Specification — Keyboard.docx` was identified. Full verified migration, confidentiality review and reconciliation of its extensive prior-repository history remain pending. Do not delete or publicly re-publish that source until the mandatory migration and protection gates are satisfied.
- **Change control:** update requirements and cross-references when implementation evolves; preserve factual history separately from current normative scope.
