# GoreeCloud Keyboard — Functional Iconography

**Status:** Development candidate — 0.1.20-dev  
**Applies to:** GoreeCloud Keyboard toolbar controls and special-key glyphs  
**Current design-system authority:** GLAZE UI V1.6 / 1.6.0, with inherited V1 functional-icon rules  
**Glaze source reviewed:** `GoreeCloud/glaze-ui` main `d07a2843589bbca6156fb716b732bd71e2ff14d8`

## Purpose

This document defines the default functional-icon direction for GoreeCloud Keyboard.

The owner supplied a contemporary Android keyboard reference showing a quiet toolbar, compact rounded outline icons, restrained monochrome presentation, and visually consistent special-key symbols. GoreeCloud Keyboard adopts that interaction and visual direction without copying another keyboard's artwork, proprietary assets, or inactive controls.

The implementation remains first-party and custom-drawn. The active 0.1.20 Development candidate applies this system directly to the native Android KeyboardView.

## Governing Glaze principles

Functional icons are semantic controls, not decoration.

The Keyboard implementation therefore follows these inherited Glaze requirements:

- preserve recognizable meaning at compact sizes;
- use accessible names for interactive icon-only controls;
- keep directional meaning correct;
- do not use color as the only state indicator;
- keep one coherent stroke family;
- preserve visual center, terminal style, corner character, and negative space;
- use outlined presentation for available/inactive controls;
- use surrounding surface treatment and/or an accent for selected or active state;
- avoid inconsistent stroke weight, unnecessary gloss, and decorative complexity.

## Default visual grammar

### Stroke family

- Default stroke width: **1.85 dp**.
- Stroke caps: **round**.
- Stroke joins: **round**.
- Default icon color: the current Glaze on-surface color at restrained opacity.
- Selected/active functional icon: current Android/GoreeCloud accent color where an accent helps identify state.
- No icon is recolored merely to imply importance.

### Toolbar

The default toolbar style is **Icons only**.

- Minimum interaction target: **48 dp**.
- Default icon visual container: **40 dp** centered inside the target.
- Icons sit directly on the quiet toolbar surface by default rather than each receiving a permanent filled pill.
- Pressed state may receive a temporary soft surface treatment.
- The toolbar must not expose a control whose real capability is not implemented.
- Emoji, Clipboard, and Keyboard Settings are the current implemented default actions.
- GIF, voice-provider, translation, handwriting, or other reference-image controls must not be added as decorative placeholders.

### Special keys

Shift, Backspace, and Enter use the same functional icon family as toolbar controls.

- **Shift:** compact outlined upward arrow with a stable silhouette. Selected Shift preserves the same symbol and uses selected surface treatment plus restrained accent.
- **Backspace:** outlined erase-key silhouette with a simple internal cross.
- **Enter:** compact return-arrow geometry with round terminals.
- Text-based mode keys such as `?123`, `ABC`, and `=\\<` remain text controls until a governed symbol would improve recognition without ambiguity.

### Toolbar controls

- **Emoji:** simple outlined face with compact filled eyes and an outlined smile.
- **Clipboard:** compact rounded clipboard shell with a restrained top clip and two internal text lines, matching the shared 1.85 dp rounded-stroke grammar.
- **Settings:** compact gear geometry rather than the previous slider-style icon, matching the widely understood Settings convention while remaining first-party geometry.

## Interaction and accessibility

Visual simplification must not reduce usability.

- Icon-only controls retain full semantic accessibility labels.
- The visual icon may be smaller than its touch target.
- Selected state remains available through accessibility semantics, not color alone.
- Press, selected, disabled, and unavailable presentation must remain distinguishable.
- Toolbar hit targets retain the full interaction row even though the visible icon surface is inset.
- Reduced Motion has no effect on static glyph recognition.

## Implementation boundary

This tranche changes presentation only for implemented controls.

It does **not** authorize or imply implementation of:

- privileged system-wide Secure Paste enforcement beyond the implemented Keyboard-side clipboard surface;
- GIF search/providers;
- voice input providers;
- handwriting;
- translation;
- external toolbar plugins;
- network-backed actions.

Those capabilities require their own governed implementation, privacy/security review, and acceptance evidence before their icons may appear.

## Validation requirements

Before this iconography can be treated as accepted Keyboard presentation, validate:

- light appearance;
- dark appearance;
- selected Shift;
- toolbar pressed state;
- monochrome readability;
- high-contrast behavior;
- accessibility labels and activation;
- 48 dp toolbar interaction targets;
- phone-scale physical-device appearance;
- no clipping at Compact, Standard, and Tall key-height settings;
- no regression in hit testing, swipe typing, long-press alternates, or navigation/system-gesture spacing.

Source and emulator validation do not replace representative physical-device visual acceptance.
