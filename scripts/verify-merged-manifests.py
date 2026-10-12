#!/usr/bin/env python3
"""Validate the final, merged debug IME manifest after Android Gradle assembly.

This catches permissions and service settings introduced by dependency or
build-variant manifest merging, which source-only checks cannot inspect.
"""
from pathlib import Path
import runpy
import sys

ROOT = Path(__file__).resolve().parents[1]
CHECKER = runpy.run_path(str(ROOT / "scripts" / "verify-ime-boundaries.py"))
verify_manifest = CHECKER["verify_manifest"]


def find_debug_manifests(root: Path) -> list[Path]:
    intermediates = root / "app" / "build" / "intermediates"
    if not intermediates.is_dir():
        return []
    return sorted(
        path for path in intermediates.rglob("AndroidManifest.xml")
        if "debug" in path.parts
        and ("merged_manifests" in path.parts or "merged_manifest" in path.parts)
    )


def audit(root: Path) -> list[str]:
    manifests = find_debug_manifests(root)
    if not manifests:
        return ["Missing debug merged AndroidManifest.xml; cannot verify final permissions."]
    errors: list[str] = []
    for manifest in manifests:
        errors.extend(f"{manifest}: {problem}" for problem in verify_manifest(manifest))
    return errors


def main() -> int:
    errors = audit(ROOT)
    if errors:
        print("Merged debug manifest verification FAILED:", file=sys.stderr)
        for error in errors:
            print(f"- {error}", file=sys.stderr)
        return 1
    print("Merged debug IME manifest verification PASSED.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
