# GoreeCloud Keyboard — Development Notes

## Current authoritative state

- Repository: `GoreeCloud/keyboard`
- Lifecycle: Development / nonconformant
- Platform Contract declaration: 0.4
- Current Official Stable GLAZE UI authority: V1.6 / `1.6.0` at `a7180679ea851389e0f3004515f9a25f420e716d`
- Implemented presentation source: repository-local V1.2 / `1.2.0` mapping at `f285b9145e27e6e7027b075c37299d101945c272`
- Glaze status: migration required; V1.6 runtime migration and application acceptance are not established

## Privacy and security boundary

Keyboard is a sensitive input surface. Current Development source keeps typing local, declares no Android network permission, treats missing or inactive editor metadata fail-closed, suppresses suggestion handling for sensitive/no-suggestions editors, and limits portable preference transfer to the explicitly reviewed emoji-category field. Typed text, editor content, clipboard payloads, suggestion history, learned input, credentials, and sensitive-editor contents are not authorized portability or observability payloads.

## Integral Platform Systems

All nine mandatory Integral Platform Systems are evaluated in `goreecloud.platform.yaml`: Manager, Privacy Shield, Wardveil Security, Everkeep, Glaze UI, Mesh, Identity, Policy, and Observability. Current runtime acceptance remains blocked where declared; Contract metadata does not manufacture integration evidence.

## Remaining stabilization gates

V1.6 source migration, rendered and accessibility acceptance, adaptive phone/tablet/foldable behavior, representative physical-device ergonomics, Privacy Shield/Wardveil/Everkeep/Manager/Mesh/Identity/Policy/Observability acceptance as applicable, recovery validation, protected signing/provenance, Release Candidate qualification, production acceptance, and Stable qualification remain open.
