# Android architecture and security boundaries

## Input and lifecycle

`GoreeKeyboardService` registers the Android `BIND_INPUT_METHOD` service,
bridges its lifecycle to a Compose input view, and sends keystrokes through
`InputConnection.commitText` or editor actions. `KeyboardPolicy` derives
a layout and sensitive-field classification from *editor metadata only*.
Compose keys hold transient shift/layer state and are reset on editor change.
No typed data enters persistent storage, analytics or logs.

A separate Compose `MainActivity` handles user-initiated keyboard
enablement and selection through Android's own system interfaces. Only
display accent and optional haptics are saved in private SharedPreferences.

## Design

`gc-ui-compose` is the canonical GoreeCloud UI Android Material 3
Expressive module's locally tracked source. It includes original generated
palette assets, roundness and motion API. Review
`third_party/GC-UI-REVISION` and token snapshot before each refresh.
The UI palette remains an alpha candidate pending device and founder review.
This is *actual source integration* but not a claim of broader ecosystem
release or visual acceptance.

## Trust boundaries

- The keyboard service requires system-only `BIND_INPUT_METHOD`.
- The APK requests no network or microphone permission.
- No text is written to disk and no backup of app preferences is permitted.
- Password/visible-password/web-password and number-password types are
  classified as sensitive. Suggestions/learning remain entirely absent.
- Backspace removes one Unicode code point, not always an entire perceived
  grapheme cluster (combining marks and ZWJ sequences need future tests).
- External editor apps and the Android IME framework remain independent trust
  boundaries; this project does not guarantee protection from a compromised
  device or application.
- USB, wireless, voice and clipboard ingestion are not integrated.

## Validation gates

Run unit tests, manifest and dependency audit, debug build, real device
typing on normal/email/phone/numeric/password fields, emoji and deletion
tests, TalkBack traversal, landscape/foldable configurations, cold start,
long-duration IME switching, battery/memory profiling, crash and network
inspection, and human visual acceptance before enabling general release.
No background service, notification, authentication or Mesh endpoint is
currently needed.
