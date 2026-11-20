# GoreeCloud Keyboard

A **new, native, privacy-first Android input method** in Kotlin and Jetpack
Compose. This is **development-stage source**, not a published, signed, tested
production keyboard or a validated replacement for SwiftKey/Gboard.

## Current development scope

- Android `InputMethodService` with English (US) QWERTY typing, shift,
  delete, space, punctuation, symbol/emoji modes and editor actions.
- Context-sensitive basic numeric, telephone, email and URL key layouts.
- Native first-run activation screen and user-selectable UI accent.
- Material 3 Expressive implementation from **GoreeCloud/gc-ui**, included
  directly under `:gc-ui-compose` from the recorded canonical revision.
- No Internet/microphone/contacts permission, cloud service, account,
  telemetry, clipboard history, remote text sync or text-learning database.
- Android 8.0+ (`minSdk=26`), built against Android SDK 37.1; API-level
  and physical-device compatibility are **not yet accepted**.

Because keyboards may observe sensitive input, **do not install the dev
build for real passwords or confidential conversations before independent
security review and device testing**.

## Build locally

Requires a compatible JDK (17+), Android SDK with platform **37.1**, and
Android Gradle Plugin **9.2.0** with Gradle **9.5.0**. The wrapper downloads
the pinned Gradle distribution on first use if absent.

```sh
./gradlew :app:testDebugUnitTest :app:assembleDebug
# Unsigned debug-output location (debug-signed by Android build tooling):
# app/build/outputs/apk/debug/app-debug.apk
```

The APK is a development artifact. For user installation, review the APK,
signing certificate, version, permissions, input-method service registration,
privacy promises, compatibility and source commit.

## Enable on Android

1. Install an independently verified development APK and open GoreeCloud Keyboard.
2. Tap **Enable keyboard** and enable it in Android Settings. Android issues
   an appropriate system-wide third-party keyboard warning.
3. Tap **Choose keyboard** and select GoreeCloud Keyboard.
4. Use it in a test text field; switch back to the system keyboard if needed.

## Engineering layout

- `app/src/main/java/com/goreecloud/keyboard/`: IME service, Compose keys,
  field classification, preference manager and activation UI.
- `gc-ui-compose/`: exact-source first-party GoreeCloud UI Material 3
  Expressive adapter snapshot, built directly as an Android library.
- `third_party/`: source provenance, canonical UI token snapshot, license.
- `docs/FEATURES.md`, `docs/ROADMAP.md`, `docs/OBJECTIVES.md`: required
  product records and acceptance plan.
- `.github/workflows/android.yml`: Android build/test and permission checks.

## Security model and limitations

All entered text is sent only to the active app's Android
`InputConnection`. This code does not persist typed words, monitor the
clipboard, request Internet access or make external requests. Neither an
offline manifest nor a unit test is a formal security proof. Installed
packages and the host Android system must be trusted independently.

There is **no** learning, autocorrect, suggestions, glide input, voice
input, handwriting, expanded languages, GIF/sticker search or automatic
cross-GoreeCloud sharing in this version. Backspace is code-point based
(rather than full grapheme-cluster aware). The current English layout is
basic and is not a fully localized, accessibility/device-tested keyboard.

GoreeShield governs future consent/privacy; Wardveil governs threat
defense; GoreeCloud Mesh governs optional integrations; GoreeCloud
Identity governs authentication if ever necessary. All optional
integrations must preserve offline independence and user permission.

## License and provenance

New first-party keyboard source: **MIT**; see [LICENSE](LICENSE).
The copied GoreeCloud UI source is separately attributed under its MIT
license in [third_party/README.md](third_party/README.md). AndroidX,
Jetpack Compose and Material 3 are open-source external dependencies and
are not represented as GoreeCloud-authored source.

Architecture and feature research included FlorisBoard (Apache-2.0),
HeliBoard (GPLv3), and OpenBoard (GPLv3); **no source from these projects
has been imported**. SwiftKey and Gboard are competitive references only.

See [Features](docs/FEATURES.md), [Roadmap](docs/ROADMAP.md) and
[Objectives](docs/OBJECTIVES.md). No Stable release or signed APK
distribution is claimed.
