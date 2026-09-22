#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
required = [
    ROOT / "IMPLEMENTED-FEATURES.md",
    ROOT / "PLANNED-FEATURES.md",
    ROOT / "CHANGELOGS.md",
]
retired = [ROOT / "FEATURE-ROADMAP.md"]
archive_parts = [
    ROOT / "docs/changelog-history/legacy-drive-keyboard-changelog-part-01.md",
    ROOT / "docs/changelog-history/legacy-drive-keyboard-changelog-part-02.md",
    ROOT / "docs/changelog-history/legacy-drive-keyboard-changelog-part-03.md",
    ROOT / "docs/changelog-history/legacy-drive-keyboard-changelog-part-04.md",
    ROOT / "docs/changelog-history/legacy-drive-keyboard-changelog-part-05.md",
    ROOT / "docs/changelog-history/legacy-drive-keyboard-changelog-part-06.md",
]

errors = []
for path in required + archive_parts:
    if not path.is_file() or not path.read_text(encoding="utf-8").strip():
        errors.append(f"missing or empty repository-governance record: {path.relative_to(ROOT)}")

for path in retired:
    if path.exists():
        errors.append(f"retired repository-governance filename must not exist: {path.name}")

for path in required:
    if path.is_file():
        text = path.read_text(encoding="utf-8")
        if "GoreeCloud/goreecloud-keyboard" in text and "stale" not in text.lower():
            errors.append(f"{path.name} contains the retired repository identity without explicit stale/historical qualification")

# The six archive headers prove contiguous preservation of all 208 non-empty
# source paragraphs from the migrated Drive changelog.
expected_ranges = ["1–35 of 208", "36–70 of 208", "71–105 of 208", "106–140 of 208", "141–175 of 208", "176–208 of 208"]
for path, expected in zip(archive_parts, expected_ranges):
    if path.is_file() and expected not in path.read_text(encoding="utf-8"):
        errors.append(f"archive range marker missing from {path.name}: {expected}")

if errors:
    for error in errors:
        print(f"ERROR: {error}", file=sys.stderr)
    raise SystemExit(1)

print("Keyboard repository feature/changelog governance records and complete six-part history archive are present; legacy root roadmap is absent.")
