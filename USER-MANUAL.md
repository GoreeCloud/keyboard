# GoreeCloud Keyboard User Manual

## Current availability

GoreeCloud Keyboard is currently a **Weave-stage** Android input-method implementation distributed as Development builds. This manual describes behavior present in the repository source and Development builds. It does not claim a public production release, Anchor qualification, representative physical-device acceptance, or current Apple-platform build.

## Install and enable the Development keyboard on Android

Current CI/debug physical-test builds install as **GoreeCloud Keyboard Dev 0.1.11** with package ID `com.goreecloud.keyboard.dev.v12`. This package is intentionally separate from the preinstalled/system package `com.goreecloud.keyboard`.

When installing the current Development APK, Android should offer to **install** GoreeCloud Keyboard Dev rather than **update** GoreeCloud Keyboard. If Android instead asks to update the preinstalled GoreeCloud Keyboard, that APK is an older Development artifact using the production package ID and should not be used for this test path.

After installation, use Android's system keyboard/input-method settings to enable **GoreeCloud Keyboard Dev 0.1.11**. Android may show a standard warning when enabling any third-party input method; review the system prompt and enable the keyboard only if you intend to use it.

Use Android's keyboard switcher or input-method selector to choose GoreeCloud Keyboard Dev 0.1.11 when a text field is active.

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

Backspace and Enter use first-party vector-style icons rather than generic text glyphs in the current Development candidate. Tap Backspace for one immediate deletion, or hold it to continue deleting at a controlled repeat rate until you lift your finger; dragging off the key cancels the repeat.

The temporary shift state resets after a shifted alphabetic character is entered. In ordinary text fields, the current Development candidate can also automatically shift at sentence starts when **Automatic capitalization** is enabled.

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

For ordinary text fields, the prediction bar can show up to three local starter predictions **before you type**. As you type, it switches to local completions and spelling candidates; after a committed word, it can show transient next-word predictions from the current editor session.

Candidates are ranked from the packaged local lexicon, broader everyday-English vocabulary, bounded spelling distance, QWERTY proximity, and current local context. The 0.1.11 candidate explicitly includes modern keyboard vocabulary such as **icon**, **icons**, **toolbar**, **emoji**, **grammar**, **prediction**, and **haptics**. Likely spelling corrections are presented ahead of a misspelled token when the local engine has a bounded correction; the word you are actively typing remains available as a fallback. Built-in context uses only a bounded ordinary-editor window and Keyboard's transient session history. If you explicitly enable **Learn from what you type**, bounded word and adjacent-word counters can also improve ranking locally across sessions.

Tap a suggestion to replace the current composing prefix with that suggestion followed by a space.

The current engine also performs conservative automatic correction at spaces and common punctuation boundaries when the local candidate is confident and the host text still matches the tracked prefix. In addition to missing-apostrophe forms such as **dont → don't**, **doesnt → doesn't**, and **im → I'm**, the deterministic grammar layer handles a bounded set of high-confidence common spelling repairs (for example **grammer → grammar**, **recieve → receive**, and **tommorow → tomorrow**) plus context-dependent repairs such as **should of → should have**. Canonical first-party names such as **GoreeCloud** and **Wardveil** retain their expected casing. Sensitive/no-suggestions editor policy remains authoritative. The current engine is local and bounded; it is not a cloud language model.

## Swipe typing

In ordinary non-sensitive text fields, you can slide across letter keys and release to submit a locally decoded word. The 0.1.11 candidate uses the actual transient pointer samples and rendered key centers rather than treating the crossed-key sequence as the whole gesture. Its local statistical classifier prunes by likely endpoints and path length, resamples the gesture, compares normalized shape and physical location against ideal word gestures, accounts for repeated-letter gesture variants and dictionary frequency, and then lets bounded local sentence context re-rank only candidates already accepted by the gesture classifier. When learning is explicitly enabled, learned local vocabulary may also participate. After a swipe, the prediction strip can show up to three local swipe candidates so you can replace the committed word with a better alternative when needed.

Swipe typing is disabled in sensitive editors and while touch-exploration/screen-reader optimized presentation is active. The gesture path is not persisted, learned from, transmitted, or combined with surrounding editor text.

The gesture-start threshold still requires deliberate travel, elapsed time, and movement across multiple letter keys before the keyboard switches from tapping to swipe decoding, reducing accidental swipes during fast typing. Endpoint selection allows nearby-key tolerance, while the statistical shape/location comparison is intended to distinguish gestures that cross similar keys but follow different routes. Recognition quality remains Weave-stage Development behavior and still requires representative physical-device acceptance.

## Sensitive text fields

For editor types classified as sensitive, GoreeCloud Keyboard suppresses suggestion collection, display, and acceptance and clears transient composing context at editor transitions. Backspace also avoids text look-behind in those sensitive editors.

This is a Development privacy boundary, not a claim that the keyboard can independently verify every application's semantic use of a text field. Android's editor metadata remains part of the classification signal.

## Network behavior

The current Android application foundation does **not** request Android network permission. Current Quill suggestions, emoji categories, and emoji recents are local-only.

Future network-backed capabilities, if implemented, require separate user-control, Privacy Shield, security, identity, and acceptance work and must be documented before they can be treated as current behavior.

## Keyboard settings and app shortcut

The Development package exposes a launcher shortcut named **GoreeCloud Keyboard Dev 0.1.11**. Opening it launches the first-party Keyboard settings screen. The same settings screen is reachable from the Settings glyph in the utility toolbar and from Android's input-method settings entry for GoreeCloud Keyboard. The settings UI uses Glaze-style cards, grouped controls, and segmented appearance controls, and its content root now applies Android system-bar insets so the status and navigation bars do not overlap the settings content.

Current device-local settings include:

- **Swipe typing**
- **Word suggestions**
- **Autocorrect**
- **Next-word predictions**
- **Automatic capitalization**
- **Key press vibration** — enabled by default
- **Learn from what you type** — disabled by default
- **Toolbar style:** Icons only or Icons + labels
- **Key height:** Compact, Standard, or Tall

Key-height choices change the visible keycap height while preserving the larger touch-target geometry used for dependable typing and accessibility.

## Key press vibration

Key press vibration is enabled by default. Taps on ordinary keys, suggestions, emoji controls, toolbar controls, and completed swipe gestures use Android keyboard haptic feedback. Turn **Key press vibration** off in GoreeCloud Keyboard Settings if you prefer a silent touch response. Android and device-level haptic policy still retain final authority over whether vibration is physically produced.

## Built-in GoreeCloud dictionary

The packaged local dictionary includes common English vocabulary, common derived/irregular forms, common contractions, and canonical GoreeCloud product/system terminology derived from the first-party branding catalog. It includes names such as **GoreeCloud**, **Glaze**, **Quill**, **Wardveil**, **Everkeep**, and the named GoreeCloud applications represented in that catalog.

The built-in dictionary itself is read-only. Optional **Learn from what you type** data is stored separately and is **off by default**. When enabled, Keyboard stores only bounded normalized word-frequency and adjacent-word-frequency counters in private app storage; it excludes sensitive editors, host no-suggestions editors, and editors requesting Android no-personalized-learning; it never stores full sentences or clipboard contents, does not synchronize or upload the data, and provides **Clear learned language data** in Settings. Disabling learning stops new collection; clearing removes the stored learned counters.

## First-run setup

Opening the GoreeCloud Keyboard app for the first time starts a guided setup flow instead of dropping directly into the full settings list.

The setup flow:

1. Shows whether the exact installed Development IME is enabled in Android and whether it is the currently selected keyboard.
2. Provides direct actions to open Android keyboard settings and the input-method picker.
3. Lets you choose initial typing, correction, prediction, swipe/trail, cursor-control, number-row, key-height, toolbar, Emoji, long-press, haptic/sound, and optional local-learning preferences.
4. Lets you decide whether to enable encrypted local Clipboard history and choose the temporary-clip expiration window.
5. Saves setup completion only when you finish the wizard.

Development candidates use version-scoped Android IME package names for side-by-side installation. Installing a new candidate therefore does not by itself guarantee that Android has enabled or selected that new IME. Use the first setup step to verify the exact candidate before testing.

The wizard can be run again later from Settings.

## Utility toolbar

A dedicated Glaze toolbar sits between the prediction bar and the number row. The default presentation is **icons only**; Settings can switch it to **Icons + labels**. The 0.1.21 Draft recovery candidate toolbar exposes three implemented actions:

- **Emoji** — the local emoji browser/search surface.
- **Clipboard** — a first-party clipboard glyph that opens the Keyboard-side clipboard panel.
- **Settings** — the first-party Settings action.

The redundant Hide Keyboard action has been removed. The **?123 / ABC** mode control remains on the bottom row instead of being duplicated in the toolbar. Emoji is toolbar-only on letter/symbol layers and is no longer duplicated on the bottom row. The actions share a single rounded Glaze toolbar surface instead of appearing as a row of unrelated utility keycaps.

GIF controls are not shown as placeholders. They remain separate capability work because GIF/provider behavior requires a real provider, network/retention policy, content-safety boundary, user controls, and privacy/security acceptance.

## Clipboard

The 0.1.21 Draft recovery candidate includes a real Keyboard-side Clipboard surface.

Open it with the **Clipboard** icon in the toolbar. The default per-app policy is **Ask**, so Keyboard presents an **Allow once** control before it reads the current clipboard for a new app session. You can change the current app's local policy to:

- **Allow** — current clipboard plus enabled local history are available through the panel.
- **Ask** — require **Allow once** before the current clipboard is read for that app session.
- **Paste only** — allow the deliberately opened current clipboard item without exposing saved history for that app.
- **Block** — do not read or present clipboard content to the Keyboard panel for that app.

**Clipboard history is off by default.** When you explicitly enable it, Keyboard stores recent non-sensitive text clips locally in encrypted form. Unpinned entries expire after the selected **10 min**, **1 hour**, or **24 hours** window.

The manager separates saved clips into **Pinned** and **Recent** collections. Tap a clip card to paste it. Long-press a clip to access management actions such as Pin/Unpin and Delete; saved history entries also expose **Edit**, which opens an IME-local editor and saves the revised text back to encrypted history. Pinned clips do not expire until you unpin or delete them.

For ordinary non-sensitive clips, local **smart content detection** can identify reusable fragments such as phone numbers, email addresses, web links, street addresses, dates, and times. Tapping a detected fragment pastes only that fragment. These detected values are computed locally from the displayed clip and are not separately persisted or added to prediction/learning.

**Paste** inserts the selected non-sensitive text and leaves its normal clipboard/history state intact. **Paste once** inserts the selected item, removes its Keyboard history copy, and clears the Android system clipboard only if it still contains the same selected value. Android-marked sensitive clips are never added to history and are available only through **Paste once**.

Clipboard payloads are not used for suggestions, autocorrect, prediction, learning, portable preference export, backup, synchronization, or telemetry. The current Keyboard-side controls are not the future privileged GoreeCloud Secure Paste Broker and cannot globally stop another Android app from using the platform clipboard APIs.

## Appearance

The governed consumer target is GLAZE UI V1.6 (`1.6.0`) at accepted source revision `a7180679ea851389e0f3004515f9a25f420e716d`. The current rendered key substrate still carries the inherited V1.2 Frosted Neutral optical/material implementation while the V1.6 presentation-context layer supplies current runtime accessibility and motion signals.

The live keyboard follows Android Light/Dark appearance. Deep Dark is defined in the inherited source mapping but is not automatically selected by the current IME runtime.

Complete V1.6 optical/component migration, Reduced Transparency/Motion acceptance, Increased Contrast/native equivalents, TalkBack/Switch Access, adaptive/form-factor validation, representative physical-device acceptance, Human Visual Excellence review, and production design acceptance remain incomplete.

## Current limitations

The Weave-stage implementation does not yet claim production-grade gesture recognition, multilingual input, privileged system-wide Secure Paste enforcement, voice input, one-handed/split layouts, full tablet/foldable adaptation, complete accessibility acceptance, user dictionary synchronization, complete Unicode grapheme segmentation for every script, signed production packaging, Seal qualification, production acceptance, or Anchor acceptance.

## Privacy and security expectations

Do not interpret the absence of network permission as proof that every future keyboard feature is automatically safe. New content sources, downloadable dictionaries/models, synchronization, clipboard access, voice adapters, account-backed personalization, or broader persisted usage history require their own GoreeCloud privacy, security, identity, continuity, and integration boundaries before production use.
