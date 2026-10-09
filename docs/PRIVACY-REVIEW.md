# GoreeCloud Keyboard — Privacy Review and Acceptance Gates

**Status:** Open; development candidate only. This checklist records repository-verifiable controls and outstanding runtime evidence. It does not certify that typed data is protected in every editor, device, or extension path.

## Verified source and CI controls

- The candidate Android manifest defines an input method service protected by `android.permission.BIND_INPUT_METHOD`, with explicit IME metadata and exported state.
- `scripts/verify-ime-boundaries.py` checks the source manifest for a set of high-risk permissions (including `INTERNET`) and enforces backup restrictions and service declarations.
- The Android candidate workflow executes source-boundary tests, then compiles and tests the candidate, verifies the merged manifest and checks APK metadata.
- On candidate commit `60df2ff7ef0b0ecd6ee20820b3c3edc5178b9c12`, GitHub Actions run [37973753809](https://github.com/GoreeCloud/keyboard/actions/runs/37973753809) completed successfully. This is **CI evidence only**, not a runtime data-flow audit.
- Candidate packages use `com.goreecloud.keyboard.florisbridge` and variant suffixes rather than replacing an installed `com.goreecloud.keyboard`.

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
