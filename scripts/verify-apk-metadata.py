#!/usr/bin/env python3
"""Fail-closed identity and permission audit of a compiled debug APK."""
from pathlib import Path
import os
import re
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
DEBUG_APK = ROOT / "app/build/outputs/apk/debug/app-debug.apk"
EXPECTED_PACKAGE = "com.goreecloud.keyboard.florisbridge.debug"
FORBIDDEN_PERMISSIONS = frozenset({
    "android.permission.INTERNET",
    "android.permission.RECORD_AUDIO",
    "android.permission.CAMERA",
    "android.permission.READ_CONTACTS",
    "android.permission.READ_SMS",
    "android.permission.ACCESS_FINE_LOCATION",
    "android.permission.QUERY_ALL_PACKAGES",
    "android.permission.MANAGE_EXTERNAL_STORAGE",
})


def audit_badging(output: str) -> list[str]:
    errors = []
    package_lines = [line for line in output.splitlines() if line.startswith("package: ")]
    if len(package_lines) != 1:
        return ["Exactly one compiled APK package identity is required."]
    name = re.search(r"\bname='([^']+)'", package_lines[0])
    version = re.search(r"\bversionCode='(\d+)'", package_lines[0])
    if name is None or name.group(1) != EXPECTED_PACKAGE:
        errors.append("Debug APK package must use the GoreeCloud transitional identity.")
    if version is None or int(version.group(1)) <= 0:
        errors.append("Debug APK must have a positive Android versionCode.")
    permissions = set()
    for line in output.splitlines():
        if line.startswith("uses-permission"):
            match = re.search(r"\bname='([^']+)'", line)
            if match is None:
                errors.append("A compiled permission could not be parsed.")
            else:
                permissions.add(match.group(1))
    for permission in sorted(permissions & FORBIDDEN_PERMISSIONS):
        errors.append(f"Forbidden compiled APK permission: {permission}")
    return errors


def find_aapt() -> Path | None:
    sdk = os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT")
    if not sdk:
        return None
    candidates = list((Path(sdk) / "build-tools").glob("*/aapt"))
    return sorted(candidates)[-1] if candidates else None


def main() -> int:
    aapt = find_aapt()
    if aapt is None or not DEBUG_APK.is_file() or DEBUG_APK.stat().st_size == 0:
        print("Missing aapt or compiled debug APK; cannot inspect built identity.", file=sys.stderr)
        return 1
    try:
        result = subprocess.run([str(aapt), "dump", "badging", str(DEBUG_APK)],
                                capture_output=True, text=True, check=True)
    except (OSError, subprocess.CalledProcessError) as exc:
        print(f"Cannot inspect APK metadata: {exc}", file=sys.stderr)
        return 1
    errors = audit_badging(result.stdout)
    if errors:
        for error in errors:
            print(error, file=sys.stderr)
        return 1
    print("Compiled debug APK package and permissions verified.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())