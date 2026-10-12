# GoreeCloud Keyboard

GoreeCloud Keyboard is a privacy-first input-platform project owned by GoreeCloud. The owner-directed product direction is an Android-first keyboard with reliable offline typing, optional intelligent and creative tools, accessible Glaze design, and eventual compatible cross-device experiences.

## Project documentation

- [Project specifications](docs/PROJECT-SPECIFICATIONS.md) — canonical requirements, scope, architecture targets, privacy/security boundaries, nine platform-system evaluation and acceptance gates.
- [Project record](docs/PROJECT-RECORD.md) — dated decisions, repository evidence, history, and outstanding verification boundaries.
- [Complete Features and Capabilities](PLANNED-FEATURES.md) — all 41 product areas and 772 enumerated feature entries from the owner-directed catalog.

## Current state

**Android candidate on draft branch (2026-10-11); not Stable or released.** The `main` checkpoint on 2026-10-09 documented the earlier pre-import state. The draft branch `feat/florisboard-import-20261009` now includes a pinned FlorisBoard-derived Android IME source tree, source privacy gates, and a passing Android CI assemble/unit-test run at commit `487bc56313316fda5a880b526c72e5e9c93cfa33`. The current stabilization work strengthens sensitive-field privacy propagation. These are **candidate/source-level and CI facts only**; device typing, protected Development signing, upgrade, full privacy, accessibility and release acceptance remain pending.

The owner's Fork-to-Native direction calls for a controlled FlorisBoard investigation, preservation of valuable functionality, independent GoreeCloud architecture and Glaze presentation, and verification of applicable privacy, security, accessibility and platform requirements. Upstream provenance and license compliance must be audited before source import.

Implementation, testing, platform integration, APK distribution, and lifecycle qualification must be verified against the authoritative GitHub source, CI, runtime and approved release evidence. Do not infer production or Stable status from written plans.
