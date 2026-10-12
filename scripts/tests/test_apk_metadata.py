"""Compiled APK metadata acceptance and rejection tests."""
from pathlib import Path
import runpy
import unittest

MOD = runpy.run_path(str(Path(__file__).resolve().parents[1] / "verify-apk-metadata.py"))
audit = MOD["audit_badging"]
PACKAGE = MOD["EXPECTED_PACKAGE"]


def badging(package=PACKAGE, version=119, permission=None):
    lines = [f"package: name='{package}' versionCode='{version}' versionName='0.6.0'"]
    if permission is not None:
        lines.append(f"uses-permission: name='{permission}'")
    return "\n".join(lines)


class ApkMetadataTests(unittest.TestCase):
    def test_expected_transitional_package(self):
        self.assertEqual(audit(badging()), [])

    def test_original_upstream_id_is_not_accepted(self):
        self.assertTrue(any("identity" in x for x in audit(badging("dev.patrickgold.florisboard.debug"))))

    def test_unknown_package_is_rejected(self):
        self.assertTrue(audit(badging("com.example.keyboard")))

    def test_network_permission_rejected_from_compiled_apk(self):
        self.assertTrue(any("INTERNET" in x
                            for x in audit(badging(permission="android.permission.INTERNET"))))

    def test_user_visible_notification_permission_allowed(self):
        self.assertEqual(audit(badging(permission="android.permission.POST_NOTIFICATIONS")), [])

    def test_missing_package_is_rejected(self):
        self.assertTrue(audit(""))

    def test_zero_version_is_rejected(self):
        self.assertTrue(any("versionCode" in x for x in audit(badging(version=0))))


if __name__ == "__main__":
    unittest.main()