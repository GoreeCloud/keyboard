# GoreeCloud Keyboard — Glaze Adoption and Native UX Acceptance

**Scope:** Android Input Method Editor and its Settings/onboarding surfaces  
**Reference authority:** [GoreeCloud/glaze](https://github.com/GoreeCloud/glaze) protected `main`, evaluated 2026-10-09  
**Current Glaze adoption target:** `1.7.0` (V1.7); **known-good rollback baseline:** `1.6.0` (V1.6)  
**Status:** Design/engineering acceptance plan only. Keyboard is **not** Glaze-conformant yet.

## Exact version and lifecycle boundary

The Glaze source documents `ADOPTION.md` and `CONFORMANCE.md` on its main branch require each consumer to validate V1.7 at its own exact source revision, environment and product surface. A shared Glaze Stable/Anchor status is not a substitute for Keyboard-specific native acceptance. The richer `1.7.1` development features are outside the accepted `1.7.0` consumer contract.

The current [Draft PR #2](https://github.com/GoreeCloud/keyboard/pull/2) imports FlorisBoard's Jetpack Compose and existing Snygg theme machinery. **These are upstream implementations, not evidence that Glaze tokens, assets, color semantics, accessibility or behavior have been implemented.** A dedicated native adaptation must preserve authorship and typing features while creating a distinct GoreeCloud identity.

## Acceptance scope

| Product surface | Required Glaze adaptation | Verification |
| --- | --- | --- |
| Keyboard keys and key popovers | Approved semantic colors, spacing, typography, elevation, iconography and meaningful pressed/focus feedback | Native light/dark, display size and contrast capture; key-hit geometry tests |
| Suggestions, smartbar, toolbar and voice/AI entry points | Distinct Glaze compositional hierarchy without obscuring core typing controls | Interaction acceptance, focus navigation and data-authority review |
| Settings / onboarding | Three discoverable peer categories: **Privacy, Security, Integrations**; accurate actual defaults and live control states | Fresh-install, returning-user and accessibility validation |
| Tablet and foldable input | Layout responsive to keyboard width, orientation, hinge and multi-window changes | Representative Android device or emulator matrix |
| Motor and accessibility needs | TalkBack semantics, keyboard and switch navigation, hit targets, legibility, reduced motion and clear feedback | Native screen-reader and alternative-input evidence |
| Locale and writing systems | RTL, Unicode composition, language switching, candidate ordering and secure-field suppression | Multilingual and sensitive-field regression suite |
| Privacy/security presentation | Fail-closed sensitive-field behavior; indicators based only on authoritative underlying system state | Runtime privacy and Wardveil/Privacy Shield boundary tests |

## Engineering workflow

1. Baseline all inherited upstream behavior and establish accessibility, typing accuracy, layout and performance measurements before visual replacement.
2. Map the accepted Glaze V1.7 design primitives to Android-native Compose implementations; use semantically named tokens, not purely cosmetic copy-and-paste styles.
3. Implement keyboard-native components incrementally behind regression coverage; preserve input latency, gestures, layout breadth, accessibility and user data.
4. Verify explicit native-state boundaries: Glaze governs presentation; Identity, Privacy Shield, Wardveil, Policy, Sync and other governing systems retain their own authority.
5. Record exact Keyboard and Glaze revisions, rendered and native evidence, relevant privacy/security decisions, rollback procedure and consumer-specific approval before claiming conformance.
6. Use V1.6 only as the governed rollback baseline; do not silently substitute V1.7.1 development behavior.

## Open blockers

- No accepted Glaze V1.7-native Keyboard components or production-use library integration have been established.
- No qualifying keyboard rendering, TalkBack/device, contrast, reduced-motion, RTL, foldable or end-to-end input evidence has been recorded.
- GoreeCloud-original icons, typography and complete product branding require design, rights and native review.
- The inherited Settings navigation does not yet satisfy all required GoreeCloud Privacy, Security and Integrations categories.
- A passing Android build is **not** Glaze V1.7 consumer conformance.

Track implementation and acceptance in [Issue #1](https://github.com/GoreeCloud/keyboard/issues/1), and follow [Project Specifications](PROJECT-SPECIFICATIONS.md) and the [Privacy Review](PRIVACY-REVIEW.md).
