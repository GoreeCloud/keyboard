# GoreeCloud Keyboard User Manual

## Current availability

GoreeCloud Keyboard is currently a **Forge-stage** Android input-method implementation distributed only as Development builds. This manual describes behavior present in the repository source and Development builds. It does not claim a public production release, Anchor qualification, representative physical-device acceptance, or current Apple-platform build.

## Enable the keyboard on Android

Development APKs use the separate package `com.goreecloud.keyboard.dev` and the visible name **GoreeCloud Keyboard Dev**. This intentionally allows a Development build to install beside an older OEM/system-preinstalled GoreeCloud Keyboard without attempting to replace the system-signed package.

After installing a Development build, use Android's system keyboard/input-method settings to enable **GoreeCloud Keyboard Dev**. Android may show a standard warning when enabling any third-party input method; review the system prompt and enable the development keyboard only if you intend to test it.

Use Android's keyboard switcher or input-method selector to choose **GoreeCloud Keyboard Dev** when a text field is active. The preinstalled/system GoreeCloud Keyboard remains installed separately.

Exact settings labels vary by Android device and version.

## Type letters

The keyboard opens in its **letters** layer.

- Tap letter keys to enter text.
- Tap **⇧** to shift the next alphabetic character.
- Tap **⌫** to delete the preceding text unit supported by the current deletion model.
- Tap **space** to insert a space. In ordinary non-sensitive fields, Space may also apply a conservative local typo correction when exactly one packaged dictionary word is one Unicode edit away and the host text still matches the locally tracked prefix.
- Tap **.** for a direct period key on the letters layout.
- Tap **↵** to send the Android Enter key action to the active editor.

The temporary shift state resets after a shifted alphabetic character is entered.

## Type numbers and symbols

Tap **?123** from the letters layer to open the primary symbols page. It includes digits and common punctuation.

From the primary symbols page:

- tap **ABC** to return directly to letters;
- tap **=\\<** to open the secondary symbols page.

The secondary page adds brackets, operators and separators, currency marks, and common typographic symbols. From that page, tap **ABC** to return directly to letters or **?123** to return to the primary symbols page.

Changing layers clears the temporary word context used for local suggestions. Symbol input is not added to that composing-word context.

## Emoji

Tap **☺** from the letters or symbols layers to open the current bounded local emoji surface. The compact category strip exposes **Smileys**, **People**, **Nature**, **Food**, **Symbols**, and **Travel**, with fuller spoken accessibility labels than the visible icon labels. Emoji keys commit their complete Unicode String value, including supported multi-code-point sequences such as skin-tone variants, ZWJ sequences, flags, and variation-selector forms.

After you commit at least one emoji, a **Recent** control appears. Current recents behavior is intentionally privacy-bounded:

- the most recently committed emoji is promoted to the front;
- selecting an emoji already in the list promotes it rather than creating a duplicate;
- at most 24 exact emoji String values are retained;
- recents are stored only in Android private application preferences for GoreeCloud Keyboard;
- recents survive an IME process restart on the same Android app installation;
- recents are not synchronized, transmitted, logged, or used to build a learned-use profile; and
- the visible **Clear** control removes the stored local recents list as well as the current in-memory list.

The persistence format is a small bounded local list; it does not include surrounding typed text, editor contents, message drafts, timestamps, application identity, or usage telemetry.

The current picker is not a complete emoji catalog. It includes bounded, fully offline search over the packaged first-party emoji catalog; search text is transient, local-only, cleared on close, and is not sent through the active editor unless you deliberately select a result. Cloud emoji lookup, GIF/sticker search, and synchronization are not implemented current behavior.

## Local GoreeCloud Quill suggestions

For ordinary text fields, the suggestion strip shows **at least one and no more than three** local candidates while a word is actively being typed. Candidates come from a packaged, read-only English seed dictionary plus the current local prefix. The dictionary does not learn from typed text, synchronize, or use network access.

Tap a suggestion to replace the current composing prefix with that suggestion followed by a space.

Space now performs a conservative local autocorrection only when exactly one packaged dictionary word is one Unicode edit away, the typed token is not already an exact packaged word, and Android's host text still matches the locally tracked prefix. Prefix completions and ambiguous corrections are not automatically committed.

The current suggestion engine remains intentionally bounded. It provides deterministic prefix candidates, limited typo-correction candidates, and conservative Space-triggered autocorrection; it is not a claim of a complete language model or cloud writing service.

## Sensitive text fields

For editor types classified as sensitive, GoreeCloud Keyboard suppresses suggestion collection, display, and acceptance and clears transient composing context at editor transitions. Backspace also avoids text look-behind in those sensitive editors.

This is a Development privacy boundary, not a claim that the keyboard can independently verify every application's semantic use of a text field. Android's editor metadata remains part of the classification signal.

## Network behavior

The current Android application foundation does **not** request Android network permission. Current Quill suggestions, emoji categories, and emoji recents are local-only.

Future network-backed capabilities, if implemented, require separate user-control, Privacy Shield, security, identity, and acceptance work and must be documented before they can be treated as current behavior.

## Appearance

The current governed consumer target is **GLAZE UI V1.6 (`1.6.0`)** at exact Stable source `a7180679ea851389e0f3004515f9a25f420e716d`. The live keyboard already consumes bounded V1.6 Android presentation signals for font scale, animation enablement, and touch exploration, while its optical/material substrate still maps the previously reviewed V1.2 (`1.2.0`) implementation at `f285b9145e27e6e7027b075c37299d101945c272`.

The IME continues to follow Android Light/Dark night-mode state and does **not** automatically select Deep Dark. In this device-test candidate, Touch Assistance requests a 308 dp four-row keyboard height and compresses vertical row gaps before shrinking ordinary key rows, preserving the 56 dp interaction floor whenever the system grants sufficient height.

Complete V1.6 optical/component migration, Reduced Transparency/Motion, Increased Contrast/native equivalents, TalkBack/Switch Access, adaptive/form-factor validation, representative physical-device acceptance, Human Visual Excellence review, and production design acceptance remain incomplete.

## Current limitations

The Forge-stage implementation does not yet claim complete gesture typing, multilingual input, clipboard tools, voice input, one-handed/split layouts, full tablet/foldable adaptation, complete accessibility acceptance, user dictionary synchronization, complete Unicode grapheme segmentation for every script, signed production packaging, Seal qualification, or Anchor acceptance.

## Privacy and security expectations

Do not interpret the absence of network permission as proof that every future keyboard feature is automatically safe. New content sources, downloadable dictionaries/models, synchronization, clipboard access, voice adapters, account-backed personalization, or broader persisted usage history require their own GoreeCloud privacy, security, identity, continuity, and integration boundaries before production use.
