# GoreeCloud Keyboard — Protected Development signing

**Scope:** Controlled, update-compatible Android Development builds of the unmerged
Fork-to-Native candidate. This is **not** production signing or approval.

## Identity and version continuity

- Existing system package `com.goreecloud.keyboard`: **do not replace or uninstall**.
- Disposable CI debug package `com.goreecloud.keyboard.florisbridge.debug`: temporary tests only; its key is not stable.
- Opt-in Development package `com.goreecloud.keyboard.florisbridge.dev`: repeatable testing **only** with a protected persistent keystore, approved key custody, and monotonically increasing versionCode.
- Reserved Development versionCode: `config/development-version-code.txt`, currently `120`; upstream baseline is 119. This code has **not** been issued to a test device or published. Before each subsequent Development distribution, increment the tracked code, validate it exceeds the prior installed and issued code, and review changes in source control. Never reset or reuse a code for a different release.

## Required local key custody

Keep the Development keystore **outside this repository and build artifacts**, in a private owner-controlled location with restrictive permissions, protected backup and a documented recovery custodian. Avoid transient CI/debug key identities. A new key generated at every build is not update-compatible. The owner or a provisioned secure build system must establish and preserve this key before the first Development distribution; no private signing material is committed here.

The opt-in Gradle build reads these environment variables securely from the approved local secret manager:

- `GOREECLOUD_KEYBOARD_DEV_KEYSTORE_PATH`
- `GOREECLOUD_KEYBOARD_DEV_STORE_PASSWORD`
- `GOREECLOUD_KEYBOARD_DEV_KEY_ALIAS`
- `GOREECLOUD_KEYBOARD_DEV_KEY_PASSWORD`

The keystore must already exist, not be a symlink, and be outside the repository. The build fails closed when required material is absent. The exact accepted Development signer certificate SHA-256 must be independently retained in the protected owner release record and supplied to the verifier via `GOREECLOUD_KEYBOARD_DEV_EXPECTED_CERT_SHA256`. Never pass passwords in a shell argument, a CI workflow, a plain text documentation file, or a repository commit. Avoid pasting them in chat.

## Explicit build

Once approved protected key custody is in place and the reserved versionCode has passed the upgrade gate, run from the repository root:

```bash
./gradlew :app:assembleDevelopment -PgoreecloudDevelopmentBuild=true --no-daemon
```

Without the opt-in Gradle property, **no Development build variant** is registered; ordinary PR/debug CI is unaffected. The Development package is distinct from both the existing system package and the disposable debug package. The Development build is not debuggable and is never signed with Gradle's default debug key.

## Bounded debug USB checks

The separate `scripts/usb-smoke.sh` harness accepts only the APK checksum specifically approved in `config/usb-smoke-approved-artifact.sha256`. The approved digest must be updated via reviewed source control when the exact CI artifact changes. The harness verifies its checksum before ADB installation, refuses to overwrite an existing test package, never selects the default input method, and removes its own temporary test package. This is an **ephemeral smoke check**, not a Development installation, release, or upgrade acceptance.

## Mandatory pre-distribution checks

1. Inspect the exact compiled APK's package ID, versionCode, certificate fingerprint, declared permissions, privacy/backup behavior, and APK signature schemes.
2. Run `python3 scripts/verify-development-apk.py` with the approved certificate fingerprint provided through `GOREECLOUD_KEYBOARD_DEV_EXPECTED_CERT_SHA256`. The verifier rejects a wrong package ID, versionCode, unsafe permissions, absent cryptographic verification, missing signer record or certificate mismatch. Compare the certificate against any already installed prior version. Confirm the new versionCode is strictly greater. Test an **upgrade in place** without uninstalling; preserve local data.
3. Test a clean installation separately from the upgrade path. Confirm the existing system keyboard is untouched and no default IME is changed implicitly.
4. Pass physical-device typing/security, Unicode/composing, accessibility, privacy, Glaze, third-party licensing, rollback and GoreeCloud platform acceptance; a passing build is not production approval.
5. Before providing any APK to the owner, copy the **exact artifact** into the authorized GoreeCloud Google Drive account and use that Drive link as the primary delivery path.

See [PROJECT-RECORD.md](PROJECT-RECORD.md) for verified history and [PRIVACY-REVIEW.md](PRIVACY-REVIEW.md) for outstanding blockers.
