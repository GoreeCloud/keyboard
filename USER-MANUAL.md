# GoreeCloud Keyboard User Manual

## Current availability

GoreeCloud Keyboard is currently a **Weave-stage** Android input-method implementation distributed as Development builds. This manual describes behavior present in the repository source and Development builds. It does not claim a public production release, Anchor qualification, representative physical-device acceptance, or current Apple-platform build.

## Install and enable the Development keyboard on Android

Current CI/debug physical-test builds install as **GoreeCloud Keyboard Dev** with package ID `com.goreecloud.keyboard.dev.v5`. This package is intentionally separate from the preinstalled/system package `com.goreecloud.keyboard`.

When installing the current Development APK, Android should offer to **install** GoreeCloud Keyboard Dev rather than **update** GoreeCloud Keyboard. If Android instead asks to update the preinstalled GoreeCloud Keyboard, that APK is an older Development artifact using the production package ID and should not be used for this test path.

After installation, use Android's system keyboard/input-method settings to enable **GoreeCloud Keyboard Dev**. Android may show a standard warning when enabling any third-party input method; review the system prompt and enable the keyboard only if you intend to use it.

Use Android's keyboard switcher or input-method selector to choose GoreeCloud Keyboard Dev when a text field is active.

Exact settings labels vary by Android device and version.

## Type letters

The keyboard opens in its **letters** layer.

- Use the always-visible number row for digits without switching layers.
- Tap letter keys to enter text.
- Tap **⇧** to shift the next alphabetic character.
- Tap **⌫** to delete the preceding text unit supported by the current deletion model.
- Tap the direct **,** and **.** keys for common punctuation.
- Tap the **English (US)** spacebar to insert a space.
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

Tap **☺** from the letters or symbols layers to open the current bounded local emoji surface. The compact category strip exposes **Smileys**, **People**, **Nature**, **Food**, **Travel**, and **Symbols**, with fuller spoken accessibility labels than the visible icon labels. Emoji keys commit their complete Unicode String value, including supported multi-code-point sequences such as skin-tone variants, ZWJ sequences, flags, and variation-selector forms.

After you commit at least one emoji, a **Recent** control appears. Current recents behavior is intentionally privacy-bounded:

- the most recently committed emoji is promoted to the front;
- selecting an emoji already in the list promotes it rather than creating a duplicate;
- at most 24 exact emoji String values are retained;
- recents are stored only in Android private application preferences for GoreeCloud Keyboard;
- recents survive an IME process restart on the same Android app installation;
- recents are not synchronized, transmitted, logged, or used to build a learned-use profile; and
- the visible **Clear** control removes the stored local recents list as well as the current in-memory list.

The persistence format is a small bounded local list; it does not include surrounding typed text, editor contents, message drafts, timestamps, application identity, or usage telemetry.

The current picker is not a complete emoji catalog. Offline search over the packaged emoji catalog is implemented; cloud emoji lookup, GIF/sticker search, and synchronization are not implemented current behavior.

## Local GoreeCloud Quill suggestions

For ordinary text fields, the integrated candidate bar stays empty at a clean word boundary. Once you type a non-empty word prefix, GoreeCloud Quill shows **at least one and at most three** local candidates.

Candidates are frequency-ordered from the packaged local lexicon. If no dictionary candidate is stronger, the word you are actively typing remains available as the fallback candidate instead of letting the strip drop to zero.

Tap a suggestion to replace the current composing prefix with that suggestion followed by a space.

The current engine also performs conservative one-edit automatic correction at spaces and common punctuation boundaries when the local candidate is confident and the host text still matches the tracked prefix. Sensitive/no-suggestions editor policy remains authoritative. The current engine is local and bounded; it is not a cloud language model.

## Swipe typing

In ordinary non-sensitive text fields, you can slide across letter keys and release to submit a locally decoded word. The current Development implementation uses only the transient key path from that gesture and the packaged local Quill lexicon.

Swipe typing is disabled in sensitive editors and while touch-exploration/screen-reader optimized presentation is active. The gesture path is not persisted, learned from, transmitted, or combined with surrounding editor text.

Recognition quality remains Weave-stage Development behavior and is still subject to physical-device refinement.

## Sensitive text fields

For editor types classified as sensitive, GoreeCloud Keyboard suppresses suggestion collection, display, and acceptance and clears transient composing context at editor transitions. Backspace also avoids text look-behind in those sensitive editors.

This is a Development privacy boundary, not a claim that the keyboard can independently verify every application's semantic use of a text field. Android's editor metadata remains part of the classification signal.

## Network behavior

The current Android application foundation does **not** request Android network permission. Current Quill suggestions, emoji categories, and emoji recents are local-only.

Future network-backed capabilities, if implemented, require separate user-control, Privacy Shield, security, identity, and acceptance work and must be documented before they can be treated as current behavior.

## Appearance

The governed consumer target is GLAZE UI V1.6 (`1.6.0`) at accepted source revision `a7180679ea851389e0f3004515f9a25f420e716d`. The current rendered key substrate still carries the inherited V1.2 Frosted Neutral optical/material implementation while the V1.6 presentation-context layer supplies current runtime accessibility and motion signals.

The live keyboard follows Android Light/Dark appearance. Deep Dark is defined in the inherited source mapping but is not automatically selected by the current IME runtime.

Complete V1.6 optical/component migration, Reduced Transparency/Motion acceptance, Increased Contrast/native equivalents, TalkBack/Switch Access, adaptive/form-factor validation, representative physical-device acceptance, Human Visual Excellence review, and production design acceptance remain incomplete.

## Current limitations

The Weave-stage implementation does not yet claim production-grade gesture recognition, multilingual input, clipboard tools, voice input, one-handed/split layouts, full tablet/foldable adaptation, complete accessibility acceptance, user dictionary synchronization, complete Unicode grapheme segmentation for every script, signed production packaging, Seal qualification, production acceptance, or Anchor acceptance.

## Privacy and security expectations

Do not interpret the absence of network permission as proof that every future keyboard feature is automatically safe. New content sources, downloadable dictionaries/models, synchronization, clipboard access, voice adapters, account-backed personalization, or broader persisted usage history require their own GoreeCloud privacy, security, identity, continuity, and integration boundaries before production use.
