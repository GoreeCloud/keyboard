# Spacebar cursor control — Development

GoreeCloud Keyboard 0.1.13-dev introduced the bounded horizontal cursor-control implementation from the rendered Space key, and the 0.1.15-dev stabilization candidate retains it unchanged while focusing on dictionary ranking, rapid-typing responsiveness, swipe quality, and touch reliability. It is a GoreeCloud-owned reimplementation of the earlier Draft PR #63/#64 design adapted to the current PR #97 runtime and settings architecture.

## Behavior

- A normal Space tap continues through the existing `KeyboardView` path and commits one Space.
- A one-finger horizontal-dominant drag beginning on the rendered Space accessibility target activates cursor mode after Android touch slop.
- Cursor movement is emitted as bounded Android DPAD LEFT/RIGHT key events through the active `InputConnection`.
- Once cursor mode activates, the normal key gesture is cancelled and the remaining pointer stream is consumed so release cannot also commit Space or another key.
- Vertical-dominant or equal-axis movement beyond activation fails closed and produces neither cursor movement nor Space.
- Multi-pointer gestures fail closed.
- Cursor control is disabled in the local Emoji layer and while Android touch exploration is enabled.
- The device-local setting is ON by default and can be disabled independently of ordinary Space behavior.

## Privacy and editor-state boundary

The cursor gesture does not inspect surrounding editor text, read clipboard data, persist gesture history, use network services, emit telemetry, or add Android permissions.

After cursor movement, transient composing/prediction state is invalidated instead of reconstructing text around the new cursor position. Normal language assistance resumes only after a clean boundary re-establishes safe local context.

The gesture may operate in password/private editors because its movement path does not require surrounding-text reads. Existing sensitive-editor restrictions on suggestions, swipe decoding, learning, and text inspection remain authoritative.

## Validation boundary

Pure JVM tests cover:

- one-pointer enforcement;
- activation threshold behavior;
- left/right cumulative steps;
- extreme finite-coordinate bounding;
- vertical/equal-axis fail-closed behavior; and
- invalid geometry.

Android runtime tests cover the actual rendered Space accessibility target and verify:

- ordinary Space taps still commit normally;
- horizontal drags route to cursor steps without committing Space;
- vertical-dominant drags commit neither Space nor cursor movement; and
- disabling the setting leaves ordinary Space taps unchanged.

This remains Weave / Development work. Emulator success does not establish representative physical-device ergonomics, long-text/editor/OEM compatibility, RTL/BiDi behavior, accessibility acceptance, latency, production release, Seal qualification, or Anchor qualification.
