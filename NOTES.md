# GoreeCloud Keyboard — Development Notes

## Current stabilization context

- Authoritative `main` is `2dd42a70b2ca74e55fe3b3fcb3fca0547e06316a`, the merge of PR #77, **Honor Touch Assistance across Keyboard runtime sizing**.
- PR #77 exact candidate `f3e0b13c4b65e324c2b5f5ba040c536c8886e7fc` passed Android CI #260 / run `35643340747`; post-merge Android CI #261 / run `35662756227` passed on exact current `main`.
- The accepted sizing behavior applies the 56 dp Touch Assistance interaction floor to the suggestion strip and alternate popup and requests a 308 dp preferred four-row keyboard height when touch exploration is active, subject to Android/system parent constraints.
- Draft PR #78 is a separate candidate that attempts to preserve the 56 dp floor under constrained total IME height by compressing row gaps first. It is not authoritative current behavior unless accepted through the normal integration workflow.
- Repository lifecycle remains Development / nonconformant and is not Stable or production accepted.
- Current repository identity is `GoreeCloud/keyboard`.
- The implemented native Android optical presentation remains a historical repository-local GLAZE UI V1.2 / `1.2.0` mapping at reviewed source `f285b9145e27e6e7027b075c37299d101945c272`.
- Current Official Stable GLAZE UI consumer authority is V1.6 / `1.6.0` at exact release source `a7180679ea851389e0f3004515f9a25f420e716d`. Accepted PR #73 adds a bounded Android V1.6 presentation-context layer over the inherited V1.2 optical substrate; complete V1.6 runtime migration and downstream acceptance remain open.
- Platform Contract 0.4 and the nine Integral Platform Systems are the current machine-readable governance model. GoreeCloud Policy and GoreeCloud Observability are explicitly applicable-blocked rather than omitted.
- Typed text, composing/editor context, clipboard contents, learned input, suggestions, emoji search queries, credentials, and other sensitive input data remain outside the current one-field portable preference format and are not granted new observation authority by platform-contract or presentation work.

## Current Development evidence

- Native Android InputMethodService, first-party KeyboardView rendering, offline emoji/search, Unicode deletion, long-press alternates, bounded local Quill suggestions, privacy-sensitive editor handling, and Android virtual accessibility-node foundations are implemented Development source.
- The repository-local `goreecloud-keyboard-preferences/1` portability format remains limited to the explicitly selected emoji category with validation, import preview, export review/freeze, and user-controlled Storage Access Framework transfer.
- Existing V1.2 material/token evidence remains the optical implementation baseline. The accepted V1.6 context consumes Android font scale, animator enablement, and touch-exploration state; it does not relabel the optical substrate as V1.6-complete or establish Stable consumer conformance.
- The accepted Touch Assistance sizing path adds no editor-content, clipboard, telemetry, network, identity, or persistence authority.
- Historical Experimental Glaze Motion evaluation remains test-only and is not a production dependency.
- Repository-native feature/changelog migration is staged on `migration/repository-feature-records-20260922`. Until accepted on `main`, the new root records remain migration candidates and the legacy Drive sources must not be retired.

## Active stabilization gates

- Complete GLAZE UI V1.6 source migration and fresh whole-keyboard rendered, accessibility, adaptive/form-factor, representative-device, performance, Human Visual Excellence, rollback, and production acceptance.
- Accepted Privacy Shield and Wardveil Security integration appropriate to highly sensitive input processing.
- Approved Everkeep backup/clean-target recovery scope without silently persisting typed or usage-derived history.
- Accepted Manager, Mesh, Identity, Policy, and Observability integrations where architecturally applicable.
- Representative physical-device typing, IME ergonomics, latency, host-application compatibility, TalkBack/Switch Access, phone/tablet/foldable, and power/thermal acceptance.
- Production signing, distribution, release approval, Release Candidate qualification, and Stable qualification.

## Maintenance boundary

Keep repository documentation, the Platform Contract declaration, current Stable Glaze authority, and implementation claims aligned. Do not convert source/build/emulator success into representative-device, production, release, or Stable acceptance without the separately required evidence.
