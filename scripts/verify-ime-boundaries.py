#!/usr/bin/env python3
"""Fail-closed source-manifest checks for the GoreeCloud Keyboard Android candidate.

This is a *static input* check, not a substitute for inspecting the merged
manifest in each build variant, auditing runtime network paths, or reviewing
privacy and Android platform permissions.
"""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "app" / "src" / "main" / "AndroidManifest.xml"
A = "{http://schemas.android.com/apk/res/android}"

# These authorities need an explicit, separately reviewed privacy design and
# permission gate before they may appear in the system keyboard manifest.
FORBIDDEN = frozenset(
    {
        "android.permission.INTERNET",
        "android.permission.READ_CONTACTS",
        "android.permission.READ_CALL_LOG",
        "android.permission.READ_SMS",
        "android.permission.RECORD_AUDIO",
        "android.permission.CAMERA",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.READ_PHONE_STATE",
        "android.permission.QUERY_ALL_PACKAGES",
        "android.permission.MANAGE_EXTERNAL_STORAGE",
    }
)


def verify_manifest(path: Path) -> list[str]:
    errors: list[str] = []
    try:
        manifest = ET.parse(path).getroot()
    except (OSError, ET.ParseError) as exc:
        return [f"Manifest cannot be read: {exc}"]

    if manifest.tag != "manifest":
        errors.append("Root element must be <manifest>.")

    requested = {
        element.attrib.get(A + "name")
        for element in manifest
        if element.tag in {"uses-permission", "uses-permission-sdk-23", "uses-permission-sdk-m"}
    }
    for permission in sorted(FORBIDDEN & requested):
        errors.append(f"Unreviewed high-risk permission: {permission}")

    application = manifest.find("application")
    if application is None:
        return errors + ["No <application> element."]
    if application.attrib.get(A + "allowBackup") != "false":
        errors.append("Automatic Android backup must explicitly be disabled for this IME candidate.")
    if application.attrib.get(A + "usesCleartextTraffic") == "true":
        errors.append("Cleartext application traffic must not be enabled.")
    if A + "sharedUserId" in manifest.attrib:
        errors.append("Deprecated shared user IDs are forbidden.")

    ime_services = []
    for service in application.findall("service"):
        actions = [
            action.attrib.get(A + "name")
            for intent_filter in service.findall("intent-filter")
            for action in intent_filter.findall("action")
        ]
        if "android.view.InputMethod" in actions:
            ime_services.append(service)

    if len(ime_services) != 1:
        errors.append(f"Expected one Android input-method service, found {len(ime_services)}.")
    for service in ime_services:
        if service.attrib.get(A + "permission") != "android.permission.BIND_INPUT_METHOD":
            errors.append("Input-method service must require BIND_INPUT_METHOD.")
        if service.attrib.get(A + "exported") != "true":
            errors.append("Input-method service must have explicit exported=true.")
        if not any(
            item.attrib.get(A + "name") == "android.view.im"
            for item in service.findall("meta-data")
        ):
            errors.append("Input-method service must declare android.view.im metadata.")
    return errors



def verify_backup_rules(root: Path) -> list[str]:
    """Require explicit exclusion from both cloud and device-transfer backups.

    Manifest allowBackup=false alone is not sufficient on all Android 12+
    device-to-device transfer implementations. These checks intentionally
    fail closed until an approved GoreeCloud-controlled recovery path exists.
    """
    errors: list[str] = []
    configurations = (
        ("app/src/main/res/xml/backup_rules.xml", "full-backup-content", ("full-backup-content",)),
        ("app/src/main/res/xml-v31/backup_rules.xml", "data-extraction-rules", ("cloud-backup", "device-transfer")),
    )
    for relative_path, expected_root, sections in configurations:
        path = root / relative_path
        try:
            document = ET.parse(path).getroot()
        except (OSError, ET.ParseError) as exc:
            errors.append(f"{relative_path}: invalid backup rules: {exc}")
            continue
        if document.tag != expected_root:
            errors.append(f"{relative_path}: expected <{expected_root}> root.")
            continue
        if document.findall(".//include"):
            errors.append(f"{relative_path}: automatic inclusion in backups is forbidden.")
        for section_name in sections:
            section = document if section_name == expected_root else document.find(section_name)
            if section is None:
                errors.append(f"{relative_path}: missing <{section_name}>.")
                continue
            exclusions = section.findall("exclude")
            if not any(
                node.attrib.get("domain") == "root" and node.attrib.get("path") == "."
                for node in exclusions
            ):
                errors.append(f"{relative_path}: <{section_name}> must exclude the full root.")
    return errors


def main() -> int:
    errors = verify_manifest(MANIFEST)
    errors.extend(verify_backup_rules(ROOT))
    if errors:
        print("Android IME static boundary check FAILED:", file=sys.stderr)
        for error in errors:
            print(f"- {error}", file=sys.stderr)
        return 1
    print("Android IME static boundary check PASSED (manifest and backup rules only).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
