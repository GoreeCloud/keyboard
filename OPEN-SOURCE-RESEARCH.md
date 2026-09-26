# GoreeCloud Keyboard — Open-Source Research

## Scope

This record documents research used to improve GoreeCloud Keyboard's clipboard product behavior.
The resulting implementation is original GoreeCloud source. No third-party clipboard source code
or assets were copied into this repository.

Research method: **Research → Compare → Understand → Extract Requirements → Redesign → Reforge → Improve → Validate → Document**.

## HeliBoard

- Project: `HeliBorg/HeliBoard`
- Revision examined: `dc28c24d272f0591575be0a84351e2f0c528b766`
- Date researched: 2026-09-26
- License: GNU GPL v3
- Use: research only; no HeliBoard source incorporated.
- Capabilities studied: clipboard-history affordances, retention, pin/unpin, removal, toolbar access,
  and privacy-conscious local keyboard operation.
- Lesson: clipboard history needs explicit lifetime controls and direct management rather than only
  a one-shot Paste key.

## FlorisBoard

- Project: `florisboard/florisboard`
- Revision examined: `c64826c2f143585cba5a15d74000bb238a18445a`
- Date researched: 2026-09-26
- License: Apache License 2.0
- Use: research only; no FlorisBoard clipboard source incorporated.
- Capabilities studied: integrated clipboard management/history and keyboard-local clipboard UX.
- Lesson: clipboard controls should be a coherent keyboard surface, not an external-app workflow.

## AnySoftKeyboard

- Project: `AnySoftKeyboard/AnySoftKeyboard`
- Revision examined: `742bf8817aae1a808be6c5fa72bb10f0182ff659`
- Date researched: 2026-09-26
- License: Apache License 2.0
- Use: research only; no AnySoftKeyboard clipboard source incorporated.
- Capabilities studied: paste/history access, pinning, multi-item management, and keeping clipboard
  management close to the keyboard.
- Lesson: management actions should not unexpectedly dismiss the clipboard surface.

## GoreeCloud redesign

The Keyboard-side Development design is deliberately more privacy-bounded:

- history defaults off;
- persisted text is encrypted with an Android Keystore AES-GCM key;
- Android-marked sensitive clips are never persisted;
- clipboard payloads are excluded from backup, synchronization, Identity association, prediction,
  correction, personalization, and learning;
- per-application Allow / Ask / Paste only / Block policy is user-controlled, with Ask requiring an explicit per-session Allow once action before the current clipboard is read;
- Paste Once removes the matching local history entry and clears the system clipboard only when its
  current value still matches the item the user explicitly selected;
- content URIs and intents are not coerced or opened by the clipboard surface;
- Keyboard never claims that an ordinary IME can revoke another application's clipboard API access.

The future privileged **GoreeCloud Secure Paste Broker** remains a separate platform dependency for
system-level cross-application enforcement, broker-enforced lifetime/revocation, Privacy Shield
authorization, and accepted Wardveil contextual payload review.
