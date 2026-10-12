# GoreeCloud Keyboard — Privacy Review and Acceptance Gates

**Status:** Open; development candidate only. This checklist records repository-verifiable controls and outstanding runtime evidence. It does not certify that typed data is protected in every editor, device, or extension path.

## Verified source and CI controls

- The candidate Android manifest defines an input method service protected by `android.permission.BIND_INPUT_METHOD`, with explicit IME metadata and exported state.
- `scripts/verify-ime-boundaries.py` checks the source manifest for a set of high-risk permissions (including `INTERNET`) and enforces backup restrictions and service declarations.
- The Android candidate workflow executes source-boundary tests, then compiles and tests the candidate, verifies the merged manifest and checks APK metadata.
- On candidate commit `60df2ff7ef0b0ecd6ee20820b3c3edc5178b9c12`, GitHub Actions run [37973753809](https://github.com/GoreeCloud/keyboard/actions/runs/37973753809) completed successfully. This is **CI evidence only**, not a runtime data-flow audit.
- Candidate packages use `com.goreecloud.keyboard.florisbridge` and variant suffixes rather than replacing an installed `com.goreecloud.keyboard`.

## Sensitive-field stabilization on draft branch (2026-10-11)

The source changes classify numeric PIN/password editors as password input, force those editors into private mode even when the user preference says otherwise, and forward the captured incognito/private-session flag into suggestion providers. Standalone spell-check requests use a conservative private-session flag because the active IME editor may not be authoritative for that request. Source regression assertions cover these safeguards. **Android compilation at the new commit, provider-learning behavior and physical-device negative tests remain subject to fresh evidence.**

## Password and private-session suggestion isolation — follow-up candidate

The next candidate change suppresses autocomplete/suggestion candidates and auto-commit in password/PIN editors, prevents clipboard suggestion surfaces in incognito sessions or from source-marked sensitive clipboard entries, and rejects asynchronous NLP results from a previous editor after an editor switch. Static regression tests check these invariants. Build acceptance and on-device tests for editor switching, clipboard behavior and privacy-sensitive text are still required before claiming protection in real user workflows.

## Developer diagnostics minimization — follow-up candidate

Source hardening removes raw editor/surrounding/composition text and clipboard payloads from developer overlays and disables caching user words/spelling candidates in the in-memory debug overlay. Non-content selection, composition, IME and clipboard-presence indicators remain for troubleshooting. Source regression checks verify prohibited debug rendering/caching paths are absent; device screenshots, lifecycle behavior and a full log/diagnostics audit remain unverified.

## Input-editor lifecycle candidate isolation — follow-up candidate

Input-start, input-view start/finish, full input finish and extracting-view editor resets synchronously clear displayed suggestions and invalidate delayed provider results. An atomic request generation rejects responses from superseded text requests; candidate assembly checks its originating editor identity before publishing. Glide/direct suggestion insertion also checks password editor status. Static tests verify lifecycle hooks and invalidation; actual fast editor switches, keyboard visibility transitions, and rapid typing still require representative-device acceptance.

## Candidate assembly lifecycle protection — follow-up candidate

Asynchronous clipboard candidate assembly now uses one editor/content snapshot, captures the request generation, and verifies generation, editor identity, password-key variation, and incognito state under the suggestion lock immediately before publication. This blocks a stale assembly that completes after editor transition or privacy change from reappearing in the smartbar. Local source regression checks exercise these invariants; fast typing and input-switch runtime behavior remain unverified.

## Unverified or pending; blocks acceptance

- [ ] Threat-model keystroke, composing text, clipboard, suggestions, learning dictionaries, logs, caches, diagnostics, and crashes. Establish precise sensitive-editor classification.
- [ ] Audit every runtime outbound data path, extension/add-on integration, content provider and intent; independently verify no unexpected transmission of typed content. Static absence of `INTERNET` alone does not prove all possible data sharing is prevented.
- [ ] Test password, PIN, payment, OTP and incognito/private editor interactions with negative cases for prediction, personalization, logging, clipboard capture, and accessibility exposure.
- [ ] Perform clean-install and returning-user testing; verify keyboard selection remains explicitly user-controlled and the existing system keyboard is untouched.
- [ ] Validate offline typing, Unicode grapheme deletion, composition, RTL and multilingual switching on a representative physical device.
- [ ] Evaluate third-party resource licenses, app branding, all nine Integral Platform Systems, Glaze, accessibility, and backup/recovery before promotion beyond the draft import milestone.
- [ ] Establish protected persistent Development signing, version-code continuity, and exact certificate readback before any repeatable Development APK distribution.
- [ ] Copy any owner-delivered APK into the authorized GoreeCloud Google Drive account first; ephemeral CI artifacts are not update-compatible distributions.

## Evidence discipline

For each acceptance gate, record the exact commit, build variant, test environment, expected/observed behavior, negative cases and reviewer decision. Do not conflate source scans, successful CI, APK availability, USB installability, physical-device typing or production qualification.

See [Development signing](DEVELOPMENT-SIGNING.md), [project specifications](PROJECT-SPECIFICATIONS.md), and [upstream provenance](UPSTREAM-PROVENANCE.md). Track remaining implementation through [issue #1](https://github.com/GoreeCloud/keyboard/issues/1) and [draft PR #2](https://github.com/GoreeCloud/keyboard/pull/2).

## Archived baseline review (2026-10-09; superseded as to current CI)

### Earlier Keyboard privacy source review — 2026-10-09

**Review date:** 2026-10-09
**Evidence scope:** [Draft PR #2](https://github.com/GoreeCloud/keyboard/pull/2), candidate commit `f791fa05cca4be2ee84c5e84cb5b25066223a8e9`
**Classification:** Source-level findings and preliminary controls; **not** runtime security acceptance
**Release state:** Blocked pending further evidence

### 1. Data and trust boundaries

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

### 2. Verified static controls on the draft branch

- The candidate's **base source manifest** declares only vibration and notification permissions, not `INTERNET`, and its IME service requires Android's `BIND_INPUT_METHOD`. **Merged manifests and runtime network paths remain unverified.**
- Candidate app data has `android:allowBackup="false"`. Both old and Android 12+ data-extraction rule resources exclude the application root from automatic cloud backup and device transfer. The runtime effect and OEM behavior still need validation.
- The Python source verifier rejects high-risk permission additions, insecure IME binding, cleartext traffic, missing backup exclusions, missing backup resources and explicit backup inclusion.
- Ten regression tests exercise clean and unsafe manifest/backup configurations; the candidate CI invokes them before assembly.
- A new, distinct temporary Android application ID protects upstream FlorisBoard installations from collision: `com.goreecloud.keyboard.florisbridge`. This is **not** the approved release identity.

These controls are staged and **not yet merged into `main`**. The earlier initial-build checkpoint at `4908c82` passed [Android CI](https://github.com/GoreeCloud/keyboard/actions/runs/37963147776). The later changes need their own exact-head evidence.

### 3. Required next verification

1. Inspect **all merged Android manifests and APK permissions** for every supported build variant. A base source manifest is not the whole application.
2. Exercise credential/password fields, incognito/private editors, Unicode composing, editor-switch resets and host-app interaction on Android devices.
3. Verify clipboard default-off history, explicit opt-in, marked and unmarked sensitive content, import/restore, pinned clip retention, expiry and deletion, including older stored records.
4. Verify local storage, crash-report redaction, on-device processing, export controls, and that automatic backup/device migration remains fail-closed.
5. Validate exported activity intent inputs, MIME and URI handling, media providers and extension import behavior.
6. Audit upstream dependencies, bundled dictionaries/language packs and third-party licensing, plus the build/release supply chain.
7. Establish user-facing Privacy, Security and Integrations settings, current approved Glaze presentation and the applicable Privacy Shield and Wardveil Security contracts.
8. Obtain representative physical-device and accessibility evidence; establish protected development signing, upgrade continuity and a separate production release gate.

### 4. Decision

**Do not release, distribute or claim a production-ready GoreeCloud Keyboard.** Maintain Draft PR #2 until the applicable licensing, design, privacy, security, accessibility, performance, platform and signing acceptance gates are verified. Automatic backup is intentionally disabled for this candidate until a reviewed user-controlled recovery mechanism exists.
