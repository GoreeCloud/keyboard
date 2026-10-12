"""Source regression for pinned clipboard image retention.

These checks do not replace on-device image clipboard testing.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]
MANAGER = ROOT / "app/src/main/kotlin/dev/patrickgold/florisboard/ime/clipboard/ClipboardManager.kt"
DATABASE = ROOT / "app/src/main/kotlin/dev/patrickgold/florisboard/ime/clipboard/provider/ClipboardDatabase.kt"


class ClipboardPinnedCleanupTests(unittest.TestCase):
    def test_clear_unpinned_history_does_not_close_pinned_images(self):
        source = MANAGER.read_text(encoding="utf-8")
        clear_unpinned = source.split("fun clearHistory() {", 1)[1].split(
            "fun clearFullHistory() {", 1
        )[0]
        self.assertIn("for (item in currentHistory.unpinned)", clear_unpinned)
        self.assertIn("item.close(appContext)", clear_unpinned)
        self.assertIn("clipHistoryDao?.deleteAllUnpinned()", clear_unpinned)
        self.assertNotIn("currentHistory.all", clear_unpinned)

    def test_explicit_full_clear_still_releases_every_item(self):
        source = MANAGER.read_text(encoding="utf-8")
        clear_all = source.split("fun clearFullHistory() {", 1)[1].split(
            "fun restoreHistory(", 1
        )[0]
        self.assertIn("for (item in currentHistory.all)", clear_all)
        self.assertIn("clipHistoryDao?.deleteAll()", clear_all)

    def test_database_clear_only_deletes_unpinned(self):
        source = DATABASE.read_text(encoding="utf-8")
        self.assertIn('WHERE NOT isPinned', source)
        self.assertIn("fun deleteAllUnpinned()", source)


if __name__ == "__main__":
    unittest.main()
