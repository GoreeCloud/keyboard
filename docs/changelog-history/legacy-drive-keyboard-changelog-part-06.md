# GoreeCloud Keyboard — Legacy Drive Changelog, Part 06

> Historical provenance migrated from Drive `Change Log — Keyboard.docx`. This part preserves non-empty source paragraphs 176–208 of 208; text is verbatim and only Word heading styles are normalized to Markdown. Historical Draft/candidate/lifecycle statements do not override current `main`.

The active V1.3 Keyboard branch's remaining automated Platform Contract failure was traced to its reusable workflow caller still pinning the central V1.2-only validator. The caller now pins exact tested V1.3-compatible candidate 3204afc4f603f3b29a456f01cbb28755d7f7bd00; the Keyboard V1.3 manifest and native mapping were not weakened.

Exact head e77ee4513c7e1d8d9baa493922e4c457b3251a5a passed Platform Contract run 34281931501 and Android CI run 34281930293. The central candidate remains Draft and no production, Release Candidate, or Stable application claim was made.

### September 8, 2026 — Native accessibility state-description hardening — Development

Opened draft PR #56 from current V1.3 migration head e77ee4513c7e1d8d9baa493922e4c457b3251a5a. Exact candidate head da77a9ae4c39ee4dbb4ee1fafac5d9f17adf3410 adds localized On, Off, and Selected accessibility-state labels, exposes Shift stateDescription=On/Off on its native virtual button, exposes Selected for the current emoji category, and adds Android platform-node assertions without widening editor-content or hidden-input authority.

Android CI run 34284735494 passed completely. The build/source job passed the existing Glaze/Motion, editor-privacy, and keyboard-accessibility validators, unit tests, APK build, and artifact provenance steps. The Android 15 emulator job then passed connectedDebugAndroidTest and IME activation, exercising the new state-description assertions through the platform accessibility provider.

PR #56 remains Draft and unmerged. This change is accessibility source/runtime hardening, not TalkBack, Switch Access, Voice Access, Touch Assistance, localization, multi-form-factor, physical-device, Privacy Shield, Wardveil Security, Release Candidate, or Stable acceptance. The Platform Contract workflow did not rerun because neither the manifest nor its workflow changed; parent PR #54 remains the controlling V1.3 Platform Contract candidate evidence.

### September 8, 2026 — Resource-backed alternate accessibility actions — Development

- Opened Draft PR #57 on `agent/keyboard-accessibility-localized-actions`, stacked on Draft PR #56.

- Current exact head: `9f5dd2e3e360af79899e250e78922a953366cbf7`.

- Moved alternate-character accessibility discovery, custom-action, unavailable-control, and announcement copy behind Android string resources.

- Updated Android instrumentation to verify platform-node hint/action/state text through resource IDs.

- Updated the repository accessibility checker to require the resource-backed contract while preserving forbidden editor/clipboard/network-authority checks.

- Exact-head Android CI run `34286201823` passed build/source validation, unit tests, APK/provenance staging, and the Android 15 connected instrumentation plus IME activation job.

- Platform Contract did not rerun because this slice does not modify the platform manifest or Platform Contract workflow; parent-stack evidence remains controlling.

- Status remains Development. Translated resources, RTL, human assistive-technology validation, representative physical-device evidence, security/privacy acceptance, release signing, RC, and Stable qualification remain open.

### September 8, 2026 — Keyboard security and repository editor baselines — Development

Draft PR #58 on `agent/security-editorconfig-baseline-20260908`, stacked on Draft PR #57, adds the missing root `.editorconfig` and a substantive `SECURITY.md` documenting current typed/editor-data minimization, fail-closed editor privacy, local portability, accessibility, Secure Paste authority, Platform-System, dependency, vulnerability-handling, and release boundaries. It does not widen IME permissions, editor inspection, clipboard authority, persistence, network behavior, learning, synchronization, or text-input authority.

Exact head `1d9d6090b84017338671e4eccc730f7577619eaa` passed Android CI run `34290823455`. The build job passed exact-source verification, Glaze UI/Motion validation, editor-privacy lifecycle validation, keyboard-accessibility validation, JVM unit tests, debug APK assembly, and Development artifact staging/upload. The dependent Android 15 motion-runtime-emulator job also passed native interaction and IME activation on the same exact head.

PR #58 remains Draft and unmerged. Privacy Shield, Wardveil Security, Everkeep, applicable Identity, Mesh, Manager, human assistive-technology, representative physical-device, signing, release, production, and Stable acceptance remain separate gates.

### September 9, 2026 — Configurable real-action utility toolbar — Development

I continued the approved Keyboard inspiration roadmap by opening Draft PR #60 on `agent/keyboard-configurable-utility-toolbar`, stacked directly on the exact validated PR #59 input-surface head. The native KeyboardView now renders a configurable Glaze-sized toolbar above the existing suggestion/navigation strip with only real implemented actions: Emoji, Symbols, and Keyboard Settings. Emoji and Symbols reuse the established layer-routing path; Settings delegates through the KeyboardView listener to KeyboardService, which opens the existing explicit Keyboard settings activity. The toolbar collapses when disabled or when no real actions are selected, so unimplemented roadmap items are not represented by decorative placeholders.

The Android Keyboard settings surface now exposes a toolbar master switch plus individual Emoji, Symbols, and Settings visibility controls. These values are private device-local Boolean presentation preferences and are intentionally excluded from `goreecloud-keyboard-preferences/1`; no toolbar-use history is retained. Native virtual accessibility nodes expose resource-backed toolbar labels, click semantics, and selected state for active Emoji/Symbols layers. The toolbar adds no typed/editor-content, clipboard, learned-input, network, account, telemetry, synchronization, Identity, Mesh, Everkeep, Privacy Shield, or Wardveil authority.

Exact head `1d0630e430e4967ff3cd772331808b9cfafbad59` passed Android CI #214 / workflow run `34295579417`. Exact-source, Glaze/Motion, editor-privacy, keyboard-accessibility, JVM unit-test, debug APK/provenance, Android 15 connected runtime/native-interaction, and exact IME-activation gates all succeeded.

PR #60 remains Draft and unmerged behind PR #59. This checkpoint is Development source/build/emulator evidence only; representative physical-device ergonomics/performance, Human Visual Excellence, TalkBack/Switch Access/Voice Access/Touch Assistance, large-text/reflow, RTL/localization, tablet/foldable acceptance, applicable Privacy Shield/Wardveil/Everkeep acceptance, protected signing/distribution, Release Candidate qualification, production approval, and Stable qualification remain open.

### September 8, 2026 CDT / September 9 UTC — Unicode-normalized local Quill suggestions — Development

Draft PR #61 on `feature/keyboard-unicode-normalized-suggestions`, stacked on the exact validated configurable-toolbar candidate, advances the device-local Quill suggestion boundary by normalizing input and dictionary matching keys to Unicode NFC before case-insensitive prefix/correction comparison. Canonically equivalent dictionary forms are deduplicated by normalized key while the original dictionary spelling is preserved as the value returned to the editor. Existing Unicode code-point correction distance, sensitive-editor protections, and local-only behavior are preserved.

Exact head `104803fe42aaffe7de7e73a9f05993a638a60718` passed Android CI. This change adds no network, telemetry, clipboard, persistence, account, synchronization, or unrestricted language-model authority. Multilingual layouts, language switching/ranking, gesture typing, spacebar cursor control, production Secure Paste, representative physical-device ergonomics/accessibility, signing, Release Candidate, production, and Stable qualification remain separate gates.

### September 8, 2026 CDT / September 9 UTC — Privacy-Bounded Spacebar Cursor Control — Development

Draft PR #63 on `feature/keyboard-spacebar-cursor-control`, stacked on the exact validated PR #61 Unicode-normalized suggestion head, adds bounded horizontal cursor movement from the existing rendered Space key. Exact candidate head: `ffc32c7c352a81c6c07615767fb68c5eef2ca184`.

Normal Space taps retain the existing KeyboardView key path. A horizontal-dominant Space drag must cross Android touch slop before cursor mode activates; cursor movement is emitted only through bounded DPAD LEFT/RIGHT key events on the active InputConnection. Activation cancels the normal key gesture and consumes the remainder of that pointer stream so release cannot also commit Space or another key. Vertical-dominant or equal-axis movement past activation fails closed. Cursor dragging is disabled in the local Emoji layer.

The privacy boundary is unchanged. The gesture does not inspect surrounding editor text, read clipboard contents, persist gesture history, use a network service, emit telemetry, add permissions, or create remote input authority. Cursor movement invalidates the locally observed Quill prefix instead of attempting to reconstruct it from host text, preserving the existing private/password editor protections.

Validation: Android CI #219 / workflow run `34304287153` passed on exact head `ffc32c7c352a81c6c07615767fb68c5eef2ca184`. The build job passed exact-source, Glaze UI/Motion, editor-privacy, accessibility, JVM unit-test, APK/provenance, and artifact gates. The Android 15 runtime job passed the unfiltered native interaction and exact IME activation suite, including rendered Space tap/drag behavior.

PR #63 remains Draft/open/unmerged Development work. Representative physical-device cursor ergonomics, long-text/editor/OEM compatibility, RTL/BiDi, TalkBack/Switch Access/Voice Access, Touch Assistance, broader text-selection gestures, multilingual layouts, swipe typing, production Secure Paste, performance/power, platform-system acceptance, signing/distribution, Release Candidate approval, production release, and Stable qualification remain open.
