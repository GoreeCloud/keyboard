#!/usr/bin/env python3
"""Verify a Development APK's identity, version, permissions and signer.

The expected certificate fingerprint comes only from the authorized owner key
custody, never from a repository default. This does not replace the required
device upgrade test and production acceptance.
"""
from pathlib import Path
import os
import re
import shutil
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
APK = ROOT / "app/build/outputs/apk/development/app-development.apk"
RESERVED_CODE = ROOT / "config/development-version-code.txt"
PACKAGE = "com.goreecloud.keyboard.florisbridge.dev"
FORBIDDEN = {
    "android.permission.INTERNET",
    "android.permission.RECORD_AUDIO",
    "android.permission.CAMERA",
    "android.permission.READ_CONTACTS",
    "android.permission.READ_SMS",
    "android.permission.ACCESS_FINE_LOCATION",
    "android.permission.QUERY_ALL_PACKAGES",
    "android.permission.MANAGE_EXTERNAL_STORAGE",
}


def inspect_badging(output: str, expected_code: int) -> list[str]:
    errors = []
    package_lines = [line for line in output.splitlines() if line.startswith("package: ")]
    if len(package_lines) != 1:
        return ["Expected exactly one Android package header."]
    m = re.search(r"\bname='([^']+)'", package_lines[0])
    v = re.search(r"\bversionCode='(\d+)'", package_lines[0])
    if m is None or m.group(1) != PACKAGE:
        errors.append("Development APK package identity mismatch.")
    if v is None or int(v.group(1)) != expected_code:
        errors.append("Development APK versionCode differs from the reserved ledger.")
    for line in output.splitlines():
        if line.startswith("uses-permission"):
            m = re.search(r"\bname='([^']+)'", line)
            if m is None:
                errors.append("Could not parse a declared APK permission.")
            elif m.group(1) in FORBIDDEN:
                errors.append(f"Prohibited Development APK permission: {m.group(1)}")
    return errors


def inspect_signer(output: str, expected_digest: str) -> list[str]:
    if not re.fullmatch(r"[a-fA-F0-9]{64}", expected_digest or ""):
        return ["Expected Development certificate SHA-256 fingerprint is missing or invalid."]
    digests = re.findall(
        r"Signer #\d+ certificate SHA-256 digest:\s*([0-9a-fA-F]{64})",
        output,
    )
    if len(digests) != 1:
        return ["Expected exactly one verified APK signer certificate."]
    if digests[0].lower() != expected_digest.lower():
        return ["APK signer certificate differs from the approved Development identity."]
    return []


def find_build_tool(executable: str) -> str | None:
    sdk = os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT")
    if sdk:
        candidates = sorted((Path(sdk) / "build-tools").glob(f"*/{executable}"))
        if candidates:
            return str(candidates[-1])
    return shutil.which(executable)


def main() -> int:
    expected_digest = os.environ.get("GOREECLOUD_KEYBOARD_DEV_EXPECTED_CERT_SHA256", "")
    if not re.fullmatch(r"[a-fA-F0-9]{64}", expected_digest):
        print("Approved certificate fingerprint is not configured.", file=sys.stderr)
        return 2
    if not APK.is_file() or APK.stat().st_size == 0:
        print("Development APK is missing or empty.", file=sys.stderr)
        return 2
    try:
        code = int(RESERVED_CODE.read_text(encoding="utf-8").strip())
    except (OSError, ValueError):
        print("Development version ledger cannot be read.", file=sys.stderr)
        return 2
    if code <= 119:
        print("Development versionCode must exceed the source baseline.", file=sys.stderr)
        return 2
    aapt, apksigner = find_build_tool("aapt"), find_build_tool("apksigner")
    if not aapt or not apksigner:
        print("Android SDK build-tools are required.", file=sys.stderr)
        return 2
    commands = ([aapt, "dump", "badging", str(APK)],
                [apksigner, "verify", "--print-certs", str(APK)])
    try:
        manifest, signature = [
            subprocess.run(cmd, capture_output=True, text=True, check=True).stdout
            for cmd in commands
        ]
    except (OSError, subprocess.CalledProcessError):
        print("APK metadata or cryptographic signature verification failed.", file=sys.stderr)
        return 1
    errors = inspect_badging(manifest, code) + inspect_signer(signature, expected_digest)
    if errors:
        for error in errors:
            print(error, file=sys.stderr)
        return 1
    print("Development APK identity, version, permissions and signature verified.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
