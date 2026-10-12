"""Static regression checks for sensitive-editor and NLP privacy propagation.

These assertions cannot replace device instrumentation or data-flow inspection.
"""
from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[2]
CONTROLLER = ROOT / "app/src/main/kotlin/dev/patrickgold/florisboard/ime/keyboard3/ImeController.kt"
NLP = ROOT / "app/src/main/kotlin/dev/patrickgold/florisboard/ime/nlp/NlpManager.kt"


class PrivateSessionSourceTests(unittest.TestCase):
    def test_hardware_delete_repeats_on_key_down_without_double_deleting_on_release(self):
        src = CONTROLLER.read_text(encoding="utf-8")
        down = src.split("fun onHardwareKeyDown(", 1)[1].split(
            "fun onHardwareKeyUp(", 1
        )[0]
        up = src.split("fun onHardwareKeyUp(", 1)[1].split(
            "fun snapshotState(", 1
        )[0]
        self.assertIn("KeyEvent.KEYCODE_DEL -> {", down)
        self.assertIn("KeyEvent.KEYCODE_FORWARD_DEL -> {", down)
        self.assertIn("emitBackspace()", down)
        self.assertIn("emitForwardDelete()", down)
        self.assertIn("return handledHardwareDeleteKeys.remove(keyCode)", up)
        self.assertIn("handledHardwareDeleteKeys.add(keyCode)", down)
        self.assertNotIn("event.isCtrlPressed", up)
        self.assertNotIn("emitBackspace()", up)
        self.assertNotIn("emitForwardDelete()", up)
        self.assertIn("event.isCtrlPressed || event.isAltPressed || event.isMetaPressed", down)

    def test_modifier_repeat_releases_hardware_key_ownership(self):
        src = CONTROLLER.read_text(encoding="utf-8")
        down = src.split("fun onHardwareKeyDown(", 1)[1].split(
            "fun onHardwareKeyUp(", 1
        )[0]
        self.assertIn("if (event.isCtrlPressed || event.isAltPressed || event.isMetaPressed) {", down)
        self.assertIn("handledHardwareDeleteKeys.remove(keyCode)", down)
        self.assertIn("return false", down)
        self.assertIn("handledHardwareDeleteKeys.add(keyCode)", down)

    def test_nlp_provider_teardown_releases_active_state(self):
        src = NLP.read_text(encoding="utf-8")
        wrapper = src.split("private class ProviderInstanceWrapper", 1)[1]
        create = wrapper.split("suspend fun createIfNecessary()", 1)[1].split("suspend fun preload(", 1)[0]
        destroy = wrapper.split("suspend fun destroyIfNecessary()", 1)[1].split("inner class ClipboardSuggestionProvider", 1)[0]
        self.assertIn("lifecycleMutex.withLock {", create)
        self.assertIn("provider.create()", create)
        self.assertLess(create.index("provider.create()"), create.index("isInstanceAlive = true"))
        self.assertIn("lifecycleMutex.withLock {", destroy)
        self.assertIn("provider.destroy()", destroy)
        self.assertLess(destroy.index("provider.destroy()"), destroy.index("isInstanceAlive = false"))
        self.assertNotIn("getAndSet(", create + destroy)
        self.assertIn("private val lifecycleMutex = Mutex()", wrapper)

    def test_hardware_delete_release_tracking_is_reset_on_editor_lifecycle(self):
        src = CONTROLLER.read_text(encoding="utf-8")
        service = (ROOT / "app/src/main/kotlin/dev/patrickgold/florisboard/FlorisImeService.kt").read_text()
        self.assertIn("private val handledHardwareDeleteKeys = mutableSetOf<Int>()", src)
        self.assertIn("fun clearHardwareDeleteTracking()", src)
        self.assertIn("handledHardwareDeleteKeys.clear()", src)
        self.assertGreaterEqual(service.count("imeController.clearHardwareDeleteTracking()"), 4)

    def test_forward_delete_preserves_unicode_surrogate_pairs(self):
        src = CONTROLLER.read_text(encoding="utf-8")
        delete = src.split("fun emitForwardDelete() {", 1)[1].split(
            "override fun", 1
        )[0]
        self.assertIn("Character.charCount(Character.codePointAt(afterCursor, 0))", delete)
        self.assertIn("afterCursor.substring(charsToDelete)", delete)
        self.assertIn("charsAfter = charsToDelete", delete)
        self.assertNotIn("text.substring(1)", delete)

    def test_numeric_password_field_is_private_input(self):
        src = CONTROLLER.read_text(encoding="utf-8")
        number_branch = src.split("InputAttributes.Type.NUMBER -> {", 1)[1].split(
            "InputAttributes.Type.PHONE -> {", 1
        )[0]
        self.assertIn("InputAttributes.Variation.PASSWORD", number_branch)
        self.assertIn("KeyVariation.PASSWORD", number_branch)

    def test_password_overrides_force_off(self):
        src = CONTROLLER.read_text(encoding="utf-8")
        self.assertRegex(
            src,
            r"\.withIncognitoMode\(\s*(?://[^\n]*\n\s*)?"
            r"keyVariation == KeyVariation\.PASSWORD\s*\|\|\s*when",
        )

    def test_password_fields_do_not_expose_suggestions(self):
        src = NLP.read_text(encoding="utf-8")
        self.assertIn("state.flags.keyVariation == KeyVariation.PASSWORD", src)
        self.assertIn("isPasswordEditor -> emptyList()", src)
        self.assertIn("if (isPrivateSession || !prefs.clipboard.suggestionEnabled.get())", src)
        self.assertIn("if (currentItem.isSensitive) return emptyList()", src)
        self.assertIn("if (imeController.snapshotState().flags.keyVariation == KeyVariation.PASSWORD) return null", src)

    def test_late_suggestions_do_not_cross_editor_boundaries(self):
        src = NLP.read_text(encoding="utf-8")
        self.assertIn("val requestEditor = state.editor", src)
        self.assertEqual(src.count("editor !== requestEditor"), 2)

    def test_ime_service_does_not_log_editor_metadata_or_pressed_key_payloads(self):
        service = (ROOT / "app/src/main/kotlin/dev/patrickgold/florisboard/FlorisImeService.kt").read_text()
        self.assertNotIn("info?.debugSummarize()", service)
        self.assertNotIn("keyCode=$keyCode", service)
        self.assertNotIn("event=$event", service)
        self.assertIn('flogDebug { "Hardware key down" }', service)
        self.assertIn('flogDebug { "Hardware key up" }', service)

    def test_devtools_never_displays_raw_editor_or_clipboard_content(self):
        overlay = (
            ROOT / "app/src/main/kotlin/dev/patrickgold/florisboard/app/devtools/DevtoolsOverlay.kt"
        ).read_text(encoding="utf-8")
        self.assertNotIn("primaryClip.toString()", overlay)
        self.assertNotIn("${content.surroundingText.", overlay)
        self.assertNotIn("${content.compositionText}", overlay)
        self.assertNotIn("wordInfoPair", overlay)
        self.assertIn("content redacted", overlay)
        self.assertIn("Spelling overlay", overlay)

    def test_spellcheck_words_not_cached_for_diagnostics(self):
        src = NLP.read_text(encoding="utf-8")
        self.assertNotIn("debugOverlaySuggestionsInfos.put(", src)
        self.assertIn("fun addToDebugOverlay(", src)

    def test_editor_lifecycle_resets_pending_suggestions(self):
        service = (
            ROOT / "app/src/main/kotlin/dev/patrickgold/florisboard/FlorisImeService.kt"
        ).read_text(encoding="utf-8")
        self.assertIn("override fun onStartInput(", service)
        self.assertIn("override fun onStartInputView(", service)
        self.assertIn("override fun onFinishInputView(", service)
        self.assertIn("override fun onFinishInput()", service)
        self.assertGreaterEqual(service.count("nlpManager.clearSuggestions()"), 5)

    def test_outdated_async_request_cannot_restore_old_candidate(self):
        src = NLP.read_text(encoding="utf-8")
        self.assertIn("private val suggestionRequestVersion = AtomicLong()", src)
        self.assertIn("val requestVersion = suggestionRequestVersion.incrementAndGet()", src)
        self.assertIn("suggestionRequestVersion.get() != requestVersion", src)
        self.assertIn("suggestionRequestVersion.incrementAndGet()", src)
        self.assertIn("activeCandidates = emptyList()", src)
        self.assertIn("currentState.editor !== state.editor", src)

    def test_candidate_assembly_guards_request_generation_and_privacy(self):
        src = NLP.read_text(encoding="utf-8")
        assembly = src.split("private fun assembleCandidates() {", 1)[1].split(
            "fun autoExpandCollapseSmartbarActions(", 1
        )[0]
        self.assertIn("val assemblyVersion = suggestionRequestVersion.get()", assembly)
        self.assertIn("content = state.content,", assembly)
        self.assertIn("suggestionRequestVersion.get() != assemblyVersion", assembly)
        self.assertIn("currentState.editor !== state.editor", assembly)
        self.assertIn("currentState.flags.keyVariation != state.flags.keyVariation", assembly)
        self.assertIn("currentState.flags.isIncognitoMode != isPrivateSession", assembly)
        self.assertIn("internalSuggestionsGuard.withLock {", assembly)

    def test_providers_never_receive_hardcoded_public_session(self):
        src = NLP.read_text(encoding="utf-8")
        self.assertNotRegex(src, r"isPrivateSession\s*=\s*false\b")
        self.assertEqual(len(re.findall(r"^\s*isPrivateSession\s*=", src, re.M)), 4)
        self.assertIn("isPrivateSession = true,", src)
        self.assertEqual(src.count("isPrivateSession = isPrivateSession,"), 3)
        self.assertEqual(src.count("val isPrivateSession = state.flags.isIncognitoMode"), 2)


if __name__ == "__main__":
    unittest.main()
