# FlorisBoard source provenance and migration boundary

**Repository:** `GoreeCloud/keyboard`  
**Imported upstream:** [florisboard/florisboard](https://github.com/florisboard/florisboard)  
**Pinned upstream revision:** `fe1241f4921b3eae923571ff7a3e113a7e10d677` (2026-09-30 UTC)  
**Method:** source snapshot imported onto a new GoreeCloud-owned development branch, rather than a GitHub network fork retaining upstream Git history  
**Status:** development baseline only; not yet reviewed, qualified, rebranded or released

## Imported material

The original FlorisBoard `app/`, `lib/`, `libnative/`, `utils/`, `gradle/` source/build structure and root Gradle scripts, Gradle wrapper for Unix, repository coding-format metadata, and upstream `LICENSE` are copied at the pinned revision. Original authorship and license headers in imported files must remain intact.

This initial import intentionally excludes upstream release automation and credentials-related distribution surfaces (`.github/`, `fastlane/`), the IDE workspace (`.idea/`), upstream brand README, upstream project roadmap, optional benchmark module, translation pipeline automation, and upstream-specific contribution/governance artifacts. Those exclusions require separate feature/preservation review where relevant. No claim is made that this staged snapshot is identical to a full upstream fork.

## Intellectual property and attribution

FlorisBoard's primary source license is **Apache License 2.0** (preserved in root `LICENSE`); other individual files, bundled third-party components, icons, data sets or documentation may carry additional attribution/license requirements. Audit and preserve every applicable notice before redistribution. GoreeCloud must not claim upstream contributions as its own work. Source import does not grant FlorisBoard trademarks or graphic identity for a final GoreeCloud product.

The independent repository's product goals are specified in [PROJECT-SPECIFICATIONS.md](PROJECT-SPECIFICATIONS.md). Upstream code is a temporary Fork-to-Native starting point, not permanent application identity or guaranteed long-term architecture.

## Unaccepted boundaries

The imported application and build configuration still use original FlorisBoard package identifiers, app labels and upstream service integrations. They are **not** an approved GoreeCloud application identity. Do not publish, sign, distribute or describe a build from this state as a GoreeCloud release.

Required before further promotion: audit all upstream source licenses and dependencies; verify reproducible builds; replace or disable network/upstream-provider paths until consent and GoreeCloud contracts are approved; establish GoreeCloud-native application identifiers, icons, onboarding and Glaze UI; verify input accuracy, sensitive-editor privacy, data protection, accessibility, migrations and the nine Integral Platform Systems.

## Evidence and traceability

- Upstream reference: `https://github.com/florisboard/florisboard/tree/fe1241f4921b3eae923571ff7a3e113a7e10d677`.
- Local/source-file import is not proof of successful Gradle build, CI, emulator operation, physical-device acceptance, license audit, upstream feature parity or release readiness.
- Track the engineering work and review gates in [GitHub issue #1](https://github.com/GoreeCloud/keyboard/issues/1).
