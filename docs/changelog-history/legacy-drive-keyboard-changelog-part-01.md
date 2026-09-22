# GoreeCloud Keyboard — Legacy Drive Changelog, Part 01

> Historical provenance migrated from Drive `Change Log — Keyboard.docx`. This part preserves non-empty source paragraphs 1–35 of 208; text is verbatim and only Word heading styles are normalized to Markdown. Historical Draft/candidate/lifecycle statements do not override current `main`.

## Change Log — Keyboard

### Purpose

I use this document as the ongoing chronological record of material changes to GoreeCloud Keyboard, including native input-method development, Glaze UI work, Quill integration, Wardveil Security, Privacy Shield, Everkeep integration, Android and Apple client work, testing, release engineering, and stabilization.

### Maintenance Guidelines

I will preserve prior entries, record implementation and validation state accurately, distinguish source/CI validation from real-device and production acceptance, and avoid recording typed content, clipboard payloads, credentials, private keys, tokens, signing material, or other sensitive values.

### Change Log Entries

### August 24, 2026 — Native Android Foundation CI Compile Defect Corrected

I inspected the active GoreeCloud Keyboard native-foundation pull request and its failing Android CI run. The workflow reached Kotlin compilation successfully but failed in KeyboardView.kt because android.graphics.Typeface.MEDIUM is not a valid Android Typeface style constant. I corrected the implementation on branch agent/native-foundation by importing Typeface and using the supported sans-serif-medium family with Typeface.NORMAL. The correction was committed as 9edbc80b078c9c19b5b5ee5c86bb0595f435a03c.

The Android foundation continues to provide the first-party InputMethodService, QWERTY rendering surface, shift/backspace/space/enter behavior, local suggestion boundary, no Android network permission, and explicit Glaze UI, Wardveil Security, Privacy Shield, Everkeep, and Quill integration boundaries. A new Android CI run was queued after the correction. No Stable, production, signed-APK, or real-device acceptance is claimed by this entry.

### August 24, 2026 — Native Android Foundation and Sensitive-Editor Privacy Gate Merged

I completed the pending native Android foundation validation. Android CI passed on exact pull-request head 9edbc80b078c9c19b5b5ee5c86bb0595f435a03c, I promoted pull request #1 from Draft, and I squash-merged it to main as 058f560a83e15a363161ae00b409c017fdc69148. The accepted foundation provides the first-party InputMethodService, native QWERTY surface, core key behavior, bounded local suggestions, no Android network permission, and explicit Glaze UI, Wardveil Security, Privacy Shield, Everkeep, and Quill integration boundaries.

I then opened pull request #2 to add a fail-closed private-input boundary for Android password editors. The Keyboard now classifies text-password, visible-password, web-password, and numeric-password variations as sensitive, clears composing context at editor transitions, does not add sensitive keystrokes to the in-memory suggestion prefix, suppresses suggestion presentation and acceptance in sensitive editors, and introduces no persistence, telemetry, analytics, advertising, sponsorship, or network behavior. The first PR #2 CI run failed only because the new test used kotlin.test instead of this Android project's established JUnit test harness. I corrected the test imports, Android CI passed on exact head 1d18a365a40517ef44fa3d343abe8f0baa86f1ad, and I squash-merged pull request #2 to main as 48c9d3396c367ee42375e0e5e4168203a41603ef. Real-device IME, signed-package, production, and Stable acceptance remain separate gates.

PR #3 — Bounded Local Typo-Correction Candidates Merged

I continued GoreeCloud Keyboard's local suggestion engine by adding a bounded one-edit correction pass for substitution, insertion/deletion, and adjacent transposition while retaining deterministic prefix-first ordering, short-input gating, case-insensitive deduplication, and safe nonpositive limits. Suggestions remain device-local; no network permission, persistence, telemetry, analytics, advertising, sponsorship, tracking, or sensitive-editor relaxation was introduced.

Exact accepted head 74ac9ce28e1c6bd32897411c423ece73ca8425ab passed Android CI #11. Pull request #3 was squash-merged to authoritative main as 7fe185aadad33db6d7d5595aeeba7dc755b05a3d. Real-device IME, signed-package, production, and Stable acceptance remain separate gates.

PR #4 — Glaze UI 1.5 Adoption Candidate and Glaze Motion 0.5 Native Evaluation Merged

I established GoreeCloud Keyboard as a Glaze UI 1.5.0 Adoption Candidate with production eligibility remaining false and added a second first-party Glaze Motion evaluation against the real native KeyboardView interaction surface. Experimental Motion is quarantined to documentation, repository validation, and Android test source; production Keyboard source remains free of the GlazeMotionExperimental marker and no Experimental runtime dependency was introduced.

The test-only mapping records Glaze Motion 0.5 with the 0.4.0 runtime compatibility baseline, validates the 90 ms micro timing role, 0.98 press scale, bounded optional-settling policy, real key release semantics, real suggestion hit-testing, and reduced-motion behavior on an Android 15 / API 35 x86_64 emulator with platform animation scales disabled.

Android CI #13 passed the build/governance job but the first emulator attempt failed because ValueAnimator.areAnimatorsEnabled() did not reliably represent the already-applied global animation-scale state inside the instrumentation process. I retained the emulator gate and corrected the test to read Android's authoritative Settings.Global.ANIMATOR_DURATION_SCALE directly. The semantic input, zero-duration, no-settling, key-release, and suggestion-selection assertions were not weakened.

Corrected exact head 80de7bd2dcff6d07b06b19f8250e37d20155d7ff passed both the build/governance job and the dedicated Android 15 emulator runtime job in Android CI #15. Pull request #4 was squash-merged with expected-head protection as c9c0500263b40640339cf7a46f1a029d9a2ac240.

Acceptance boundary: this establishes current-Stable adoption-candidate evidence plus a bounded native Android emulator Glaze Motion evaluation. It does not establish full phone/tablet native or rendered Glaze UI acceptance, TalkBack or switch-access acceptance, representative physical-device behavior, representative performance/power acceptance, production Glaze Motion activation, signed release acceptance, production acceptance, or Candidate/Stable Glaze Motion promotion.

### August 28, 2026 — Glaze UI 1.6 Adoption Candidate Migration Merged

GoreeCloud Keyboard migrated its current-Stable adoption evidence from Glaze UI 1.5.0 to Glaze UI 1.6.0 while preserving the first-party KeyboardView implementation, no-network privacy boundary, and Experimental Glaze Motion quarantine. The adoption record now references canonical Glaze UI Stable merge revision 9dcd39dad0ade79fb01dfb1b6b39f6bf2c167471 and explicitly keeps production eligibility false while phone/tablet rendered/native acceptance, applicable Adaptive Workspace acceptance, TalkBack/switch-access acceptance, representative physical-device acceptance, and release acceptance remain pending.

Exact pull-request head ed4703c53486c3d8eaae45cb281bdda6c4c15980 passed Android CI #18, including the 1.6 Stable/Motion governance validator, unit tests, debug APK assembly, and the Android 15 native interaction/reduced-motion emulator gate. PR #5 was squash-merged with expected-head protection to main as 3c82fff63d328bcc5f375b1b5a9bf9b692cd8c73. Post-merge Android CI #19 / run 33152961333 also passed on that exact main commit, including the Android 15 emulator job. Glaze Motion remains Experimental and is not a production dependency; this merge establishes 1.6 Adoption Candidate evidence only, not aligned-current-stable or production acceptance.

### August 28, 2026 — Glaze UI 2.0 Stable Adoption Candidate Merged

GoreeCloud Keyboard advanced from the superseded Glaze UI 1.6 adoption evidence to a bounded Glaze UI 2.0.0 Stable Adoption Candidate on its first-party Android KeyboardView. The implementation added application-owned GlazeKeyboardTokens for the reviewed 4/8 dp spacing subset, 12 dp utility radius, 48 dp general interaction floor, and Light foundation colors; raised the suggestion strip to the 48 dp interaction floor; added JVM token-lock tests; and hardened repository governance against stale 1.x evidence or Experimental Motion escaping into production source. Exact pull-request head 395ec59576287eb4371f504c333cc99e8372de37 passed Android CI #20. Pull request #6 was merged with expected-head protection to main as c69e7a51f619e51d7de83ddac692c293b9577a02.

Acceptance boundary: this merge establishes repository-level Glaze UI 2.0 Adoption Candidate source and CI evidence only. Dark/Deep Dark, complete material-role and expression-mode behavior, reduced-motion/adaptive/accessibility configurations, representative physical-device acceptance, signed release acceptance, and production acceptance remain separate gates. Experimental Glaze Motion remains test-only and is not a production dependency.

Aug 28, 2026 at 4:28 PM CDT — Current Glaze UI 2.0 Dark Appearance Mapping

Change type or category: Glaze UI 2.0 current-Stable migration; native Android appearance behavior; validation.

Updated GoreeCloud Keyboard against the current Glaze UI 2.0 Stable token map rather than the earlier promotion snapshot. The native token mapping now uses the current 14 dp radius.md value, typed Light and Dark palettes including canonical line colors, and draw-time Android night-mode selection in the first-party KeyboardView. Unit/governance coverage was updated to reject stale radius evidence and require Dark source wiring while keeping Experimental Glaze Motion test-only.

Validation and merge evidence: PR #7, exact head 5ee2469f02143eecbbc7c738efd9feb368358bc2; Android CI run #22 completed successfully; merged to main as cbfe94edc0f104987ba574e72f762440e8289834.

Acceptance boundary: this is source-level Light/Dark current-Stable migration evidence only. Deep Dark, complete Glaze material roles, rendered accessibility/device acceptance, signed release, and production acceptance remain separate gates.

### Source-Validated Development Update — Symbol Layer, Documentation, and Runtime Validation

GoreeCloud Keyboard added a first-party symbols layer with ?123 / ABC switching, digits and common punctuation, alphabetic-only shift behavior, and composing-context isolation so symbol input does not contaminate the local GoreeCloud Quill word context. The candidate also completed root repository specifications, features/benefits/objectives documentation, USER-MANUAL.md, and a source-current README while preserving sensitive-editor suggestion suppression and the no-network-permission boundary.

The first full Android CI run exposed a stale instrumentation test listener that did not implement the newly added onLayerChanged callback. The failure was a test-compilation compatibility defect, not an emulator/device behavior failure. The test double was corrected without changing production keyboard behavior.
