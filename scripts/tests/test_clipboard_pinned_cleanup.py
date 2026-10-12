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

    def test_conditional_delete_only_releases_image_after_row_is_removed(self):
        source = MANAGER.read_text(encoding="utf-8")
        block = source.split("fun deleteClip(", 1)[1].split("fun pinClip(", 1)[0]
        self.assertIn("val dao = clipHistoryDao ?: return@launch", block)
        self.assertIn("dao.deleteIfUnpinned(item.id)", block)
        self.assertIn("dao.delete(item.id)", block)
        self.assertIn("if (removedRows <= 0) return@launch", block)
        self.assertIn("appContext.contentResolver.delete(uri, null, null)", block)
        self.assertLess(
            block.index("if (removedRows <= 0) return@launch"),
            block.index("appContext.contentResolver.delete(uri, null, null)"),
        )

    def test_database_reports_actual_deletion_count(self):
        source = DATABASE.read_text(encoding="utf-8")
        self.assertIn("fun deleteIfUnpinned(id: Long): Int", source)
        self.assertIn("fun delete(id: Long): Int", source)
        self.assertIn("AND not isPinned", source)


if __name__ == "__main__":
    unittest.main()
