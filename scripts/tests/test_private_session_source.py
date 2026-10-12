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

    def test_providers_never_receive_hardcoded_public_session(self):
        src = NLP.read_text(encoding="utf-8")
        self.assertNotRegex(src, r"isPrivateSession\s*=\s*false\b")
        self.assertEqual(len(re.findall(r"^\s*isPrivateSession\s*=", src, re.M)), 4)
        self.assertIn("isPrivateSession = true,", src)
        self.assertEqual(src.count("isPrivateSession = isPrivateSession,"), 3)
        self.assertEqual(
            src.count("imeController.snapshotState().flags.isIncognitoMode"), 2
        )


if __name__ == "__main__":
    unittest.main()
