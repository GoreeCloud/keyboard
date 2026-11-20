# Feature inventory

## Implemented in source (subject to Android build and device acceptance)

- Android `InputMethodService` + manifest registration / user activation
- QWERTY English typing, shift, symbols, selection-aware backspace, space,
  keyboard switcher and editor action
- Simple number and phone-specific keyboard layouts
- Emoji entry mode using platform Unicode glyphs
- Compose-based setup/settings and Compose keyboard surface
- GoreeCloud UI Material 3 Expressive theme source integrated locally
- Offline-by-default manifest with no Internet or microphone permission
- Optional local keypress haptic feedback and selected GoreeCloud UI accent
- No per-key telemetry, clipboard collection, personal word learning or remote sync

## Planned (not yet implemented or accepted)

Suggestion/prediction engine, offline spell checking and autocorrect,
swipe typing, voice typing with truly on-device models, multilingual layouts,
user dictionaries, handwriting, advanced emoji/sticker/GIF search, clipboard
history with consent, split/floating keyboard, Android device and TalkBack
qualification, localized key labels, instrumented tests and signed releases.
