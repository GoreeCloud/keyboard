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

    def test_providers_never_receive_hardcoded_public_session(self):
        src = NLP.read_text(encoding="utf-8")
        self.assertNotRegex(src, r"isPrivateSession\s*=\s*false\b")
        self.assertEqual(len(re.findall(r"^\s*isPrivateSession\s*=", src, re.M)), 4)
        self.assertIn("isPrivateSession = true,", src)
        self.assertEqual(src.count("isPrivateSession = isPrivateSession,"), 3)
        self.assertEqual(src.count("val isPrivateSession = state.flags.isIncognitoMode"), 2)


if __name__ == "__main__":
    unittest.main()
