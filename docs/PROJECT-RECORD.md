# GoreeCloud Keyboard — Project Record

**Owner:** GoreeCloud  
**Repository:** `GoreeCloud/keyboard`  
**Record class:** Significant project history and evidence  
**Updated:** 2026-10-09

## Verification boundary

A 2026-10-09 GitHub tree inspection found two files in the default branch: `README.md` and `PLANNED-FEATURES.md`. No Android source code, upstream fork, runtime tests, nine-system integration, release APK, or completed product was present in that inspected tree.

This record describes **this repository**. Implementation claims associated with earlier work elsewhere are not automatically attributed to it.

## 2026-10-09 — Feature catalog

The owner-directed GoreeCloud Keyboard product catalog was stored in [PLANNED-FEATURES.md](../PLANNED-FEATURES.md), retaining 41 categories and 772 feature entries.

- Commit: `1cddfc873e10551c609ecfaf5f13f77cc6d8d622`
- Default-branch readback: completed
- Classification: product vision and planned scope, **not implemented features**

## 2026-10-09 — README routing

The [README](../README.md) was updated to link the capability inventory and distinguish planned functionality from source-verified functionality.

- Commit: `9c39605c8d4195012710c4949a45aaffd0e0a979`
- Default-branch readback: completed
- Classification: documentation only

## 2026-10-09 — Canonical project specification

The repository's [project specification](PROJECT-SPECIFICATIONS.md) was created with Android-first scope, owner-directed consideration of FlorisBoard for a controlled Fork-to-Native transition, privacy and security requirements, Glaze and accessibility requirements, and acceptance evaluation of the nine Integral Platform Systems.

- Commit: `e6e3351ac65b25f03e2af8e6cd69f634df9a9cba`
- Classification: normative and architectural targets, not runtime acceptance

## Open history and migration boundaries

Existing historical project documentation requires separate review before its contents can be safely reconciled with this public repository. Such documentation is not a competing current implementation source and must not be removed before the required migration, validation and confidentiality gates. No completed migration or disposal is claimed here.

## Pending engineering acceptance

| Area | Verified state at baseline | Required completion evidence |
| --- | --- | --- |
| Upstream licensing and provenance | Not evidenced | Audited upstream revision, dependency inventory and transition decision |
| Input method implementation | Not evidenced | Source, correct behavior and approved tests |
| Glaze and nine platform systems | Not evidenced | Applicability and implementation evidence |
| Data protection and offline operation | Not evidenced | Permission, privacy, security and runtime tests |
| Device support and accessibility | Not evidenced | Physical-device and accessibility acceptance |
| Release | Not evidenced | Exact-revision build, protected signing, upgrade/recovery and release authorization |

Update this ledger for significant verified changes, decisions, migrations, incidents and acceptance events. Use `CHANGELOGS.md` for release-facing changes when applicable. Never treat a documented target as a shipped feature.

## 2026-10-09 — Bootstrap work tracked

The initial implementation, provenance, security, accessibility, and migration obligations are tracked in [GitHub issue #1](https://github.com/GoreeCloud/keyboard/issues/1). The issue was verified **open**, not completed. It does not establish an accepted fork, tested Android build or release.

## 2026-10-09 — Android candidate build milestone

[Draft PR #2](https://github.com/GoreeCloud/keyboard/pull/2) contains the pinned FlorisBoard source candidate. It remains unmerged. Exact-head [CI run 37963147776](https://github.com/GoreeCloud/keyboard/actions/runs/37963147776) passed Android candidate debug assembly and initial IME manifest checks at commit `4908c82`. Further backup privacy hardening and tests are being evaluated in Draft PR #2; release and platform acceptance remain pending.

## 2026-10-09 — Merged manifest and current privacy gates

- [Draft PR #2](https://github.com/GoreeCloud/keyboard/pull/2) remains **open, Draft and unmerged**. The default branch continues to hold governance/docs only, not the app source.
- At revision `15e7bf0c8ea231ff28df03622958606866e57e33`, [CI run 37966844864](https://github.com/GoreeCloud/keyboard/actions/runs/37966844864) **passed** debug assembly, regression tests, Android source permissions, backup rules, and the *merged debug manifest* privacy gate. The candidate avoids unnecessary cloned clipboard media when history is off or the item is marked sensitive.
- Candidate revision `06f63f422ee8ab7ee473bab51252064dd8b112c3` added compiled APK package/permissions verification. Exact-commit [CI run 37967783880](https://github.com/GoreeCloud/keyboard/actions/runs/37967783880) **passed**, covering 21 Python regression tests, assembly, final merged manifest and compiled APK checks. Later revision `408e1a39efb7de17e30f8e9695dbf361bed0a807` adds a required Android/Kotlin JVM unit-test step; [CI run 37968659085](https://github.com/GoreeCloud/keyboard/actions/runs/37968659085) is pending fresh exact-head validation.
- [Glaze V1.7 Keyboard adoption](GLAZE-ADOPTION.md) now records the accepted v1.7.0 consumer target and v1.6.0 rollback baseline, while explicitly leaving native conformance unverified.
- Physical-device qualification, Kotlin/Android instrumentation, complete clipboard privacy, license and dependency audit, platform integration, protected Development signing, recovery/upgrade behavior and release acceptance remain **open**.


## 2026-10-09 — Bounded USB installability verification

- **Environment:** Owner-authorized ARM64 smartphone reporting Android 16 (API 36); OnePlus N200-class hardware. ADB reported an authorized USB connection. Hardware serial, default keyboard identifier, user content, screenshots and personal device logs were not recorded.
- **Existing app preservation:** The existing `com.goreecloud.keyboard` is a system app on the phone's product partition and was not altered.
- **Source and build:** [Draft PR #2](https://github.com/GoreeCloud/keyboard/pull/2) at head `d03e11e6e7ecb2649e61c315dfa7c3a7454c4fb7` passed [GitHub Actions run 37969186933](https://github.com/GoreeCloud/keyboard/actions/runs/37969186933): Kotlin/JVM unit tests, 21 Python regression tests, APK assembly, source and merged manifest checks, compiled APK identity/permission checks, and short-lived debug artifact creation.
- **Artifact provenance:** Debug artifact was produced from the pull-request merge revision `b3bf721912642363dff63c5cb76261c41c7f8f74`; local extracted `app-debug.apk` SHA-256: `e76bf74c325f7785fa7b727d9e947c09a8b9bba86f268871377111d9b2f9761a`. This was **ephemerally CI debug-signed**, not an approved Development/update signing identity.
- **Physical result:** `adb install -t` reported **Success** for isolated `com.goreecloud.keyboard.florisbridge.debug`. Android's package registry recognized the installed app and its `android.view.InputMethod` service. The candidate was **not selected as the default keyboard**. `adb uninstall` then reported **Success**, and readback confirmed the temporary candidate is absent while the existing system GoreeCloud Keyboard remains.
- **Acceptance boundary:** This verifies a one-time APK installability/IME-registration smoke test only. Real typing, orientation changes, TalkBack, composing text/Unicode, password-field behavior, clipboard persistence on device, input latency, battery/performance, reboot/upgrade continuity, protected signing and Glaze-native presentation were **not tested**. No APK was published as an owner Development release, and the imported source PR remains Draft and unmerged.
