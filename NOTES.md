# GoreeCloud Keyboard — Development Notes

## Current stabilization context

- Current V1.6 follow-up sizing applies the 56 dp Touch Assistance floor to the suggestion strip and alternate popup and requests a 308 dp preferred four-row keyboard height when touch exploration is active. This remains Development source behavior pending exact-head CI and physical-device acceptance.

- Repository lifecycle remains Development and is not Stable or production accepted.
- Current repository identity is `GoreeCloud/keyboard`.
- The implemented native Android presentation remains a historical repository-local GLAZE UI V1.2 / `1.2.0` mapping at reviewed source `f285b9145e27e6e7027b075c37299d101945c272`.
- Current Official Stable GLAZE UI consumer authority is V1.6 / `1.6.0` at exact release source `a7180679ea851389e0f3004515f9a25f420e716d`. The current candidate adds a bounded Android V1.6 presentation-context layer over the inherited V1.2 optical substrate; complete runtime migration and downstream acceptance remain open.
- Platform Contract 0.4 and the nine Integral Platform Systems are the current machine-readable governance target. GoreeCloud Policy and GoreeCloud Observability are explicitly applicable-blocked rather than omitted.
- Typed text, composing/editor context, clipboard contents, learned input, suggestions, emoji search queries, credentials, and other sensitive input data remain outside the current one-field portable preference format and are not granted new observation authority by platform-contract or presentation work.

## Current Development evidence

- Native Android InputMethodService, first-party KeyboardView rendering, offline emoji/search, Unicode deletion, long-press alternates, bounded local Quill suggestions, privacy-sensitive editor handling, and Android virtual accessibility-node foundations are implemented Development source.
- The repository-local `goreecloud-keyboard-preferences/1` portability format remains limited to the explicitly selected emoji category with validation, import preview, export review/freeze, and user-controlled Storage Access Framework transfer.
- Existing V1.2 material/token evidence remains the current optical implementation baseline. The new V1.6 context consumes only Android font scale, animator enablement, and touch-exploration state; it does not relabel the optical substrate as V1.6-complete or establish Stable consumer conformance.
- The V1.6 context raises alternate-character interaction targets through the existing 56 dp touch-assistance floor when Android touch exploration is active. It adds no editor-content, clipboard, telemetry, network, identity, or persistence authority.
- Historical Experimental Glaze Motion evaluation remains test-only and is not a production dependency.

## Active stabilization gates

- Complete GLAZE UI V1.6 source migration and fresh whole-keyboard rendered, accessibility, adaptive/form-factor, representative-device, performance, Human Visual Excellence, rollback, and production acceptance.
- Accepted Privacy Shield and Wardveil Security integration appropriate to highly sensitive input processing.
- Approved Everkeep backup/clean-target recovery scope without silently persisting typed or usage-derived history.
- Accepted Manager, Mesh, Identity, Policy, and Observability integrations where architecturally applicable.
- Representative physical-device typing, IME ergonomics, latency, host-application compatibility, TalkBack/Switch Access, phone/tablet/foldable, and power/thermal acceptance.
- Production signing, distribution, release approval, Release Candidate qualification, and Stable qualification.

## Maintenance boundary

Keep repository documentation, the Platform Contract declaration, current Stable Glaze authority, and implementation claims aligned. Do not convert source/build/emulator success into representative-device, production, release, or Stable acceptance without the separately required evidence.
