# Keyboard Privacy and Security Review — Import Candidate

**Review date:** 2026-10-09  
**Evidence scope:** [Draft PR #2](https://github.com/GoreeCloud/keyboard/pull/2), candidate commit `f791fa05cca4be2ee84c5e84cb5b25066223a8e9`  
**Classification:** Source-level findings and preliminary controls; **not** runtime security acceptance  
**Release state:** Blocked pending further evidence

## 1. Data and trust boundaries

A system keyboard handles inherently sensitive user-entered text and can observe editor metadata and clipboard state. Source-level controls alone are insufficient: Android service lifecycle, host editor behavior, data retention and user-visible permissions need runtime validation.

| Data surface | Observed imported behavior | Risk / remaining gate |
| --- | --- | --- |
| IME text and editor state | Uses Android `InputMethodService` and `InputConnection`; input method service requires `BIND_INPUT_METHOD` | Sensitive-editor classification, surrounding-text boundaries, lifecycle resets and data exposure require device tests |
| System clipboard | Source has a primary-clip listener and may maintain clipboard preview/state | Avoid silent capture, retained secrets and cross-app leakage; test Android platform behavior |
| Clipboard history | Optional persisted history exists; imported default `historyEnabled=false` | Unmarked sensitive clips may still be collected if history is enabled; consent, retention and deletion controls need acceptance |
| Clipboard sensitive marker | Candidate prevents items with `isSensitive=true` from entering ordinary history insertions | Source marker depends on app-supplied Android clipboard metadata; not all sensitive data is marked, and historical entries require review |
| Dictionary and preferences | Local preferences and user-dictionary database are present | Classify personal information, exports, deletion, storage encryption, backup/restore and migration |
| Diagnostics/crashes | Imported crash handling can write local diagnostic material | Prove typed text and editor context are not leaked into diagnostics or exported reports |
| Optional features and integrations | Upstream source contains optional editor, extensions and provider-related modules | Review all transmitted data, intents, provider authorities, permissions and retained assets before enabling integrations |

## 2. Verified static controls on the draft branch

- The candidate's **base source manifest** declares only vibration and notification permissions, not `INTERNET`, and its IME service requires Android's `BIND_INPUT_METHOD`. **Merged manifests and runtime network paths remain unverified.**
- Candidate app data has `android:allowBackup="false"`. Both old and Android 12+ data-extraction rule resources exclude the application root from automatic cloud backup and device transfer. The runtime effect and OEM behavior still need validation.
- The Python source verifier rejects high-risk permission additions, insecure IME binding, cleartext traffic, missing backup exclusions, missing backup resources and explicit backup inclusion.
- Ten regression tests exercise clean and unsafe manifest/backup configurations; the candidate CI invokes them before assembly.
- A new, distinct temporary Android application ID protects upstream FlorisBoard installations from collision: `com.goreecloud.keyboard.florisbridge`. This is **not** the approved release identity.

These controls are staged and **not yet merged into `main`**. The earlier initial-build checkpoint at `4908c82` passed [Android CI](https://github.com/GoreeCloud/keyboard/actions/runs/37963147776). The later changes need their own exact-head evidence.

## 3. Required next verification

1. Inspect **all merged Android manifests and APK permissions** for every supported build variant. A base source manifest is not the whole application.
2. Exercise credential/password fields, incognito/private editors, Unicode composing, editor-switch resets and host-app interaction on Android devices.
3. Verify clipboard default-off history, explicit opt-in, marked and unmarked sensitive content, import/restore, pinned clip retention, expiry and deletion, including older stored records.
4. Verify local storage, crash-report redaction, on-device processing, export controls, and that automatic backup/device migration remains fail-closed.
5. Validate exported activity intent inputs, MIME and URI handling, media providers and extension import behavior.
6. Audit upstream dependencies, bundled dictionaries/language packs and third-party licensing, plus the build/release supply chain.
7. Establish user-facing Privacy, Security and Integrations settings, current approved Glaze presentation and the applicable Privacy Shield and Wardveil Security contracts.
8. Obtain representative physical-device and accessibility evidence; establish protected development signing, upgrade continuity and a separate production release gate.

## 4. Decision

**Do not release, distribute or claim a production-ready GoreeCloud Keyboard.** Maintain Draft PR #2 until the applicable licensing, design, privacy, security, accessibility, performance, platform and signing acceptance gates are verified. Automatic backup is intentionally disabled for this candidate until a reviewed user-controlled recovery mechanism exists.
