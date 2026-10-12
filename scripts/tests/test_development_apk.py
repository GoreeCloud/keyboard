"""Fail-closed checks for protected Development APK metadata."""
from pathlib import Path
import runpy
import unittest

CHECKER = runpy.run_path(str(Path(__file__).resolve().parents[1] / "verify-development-apk.py"))
badging = CHECKER["inspect_badging"]
signer = CHECKER["inspect_signer"]
PACKAGE = CHECKER["PACKAGE"]
HASH = "a" * 64


def package_line(name=PACKAGE, code=120, permission=None):
    lines = [f"package: name='{name}' versionCode='{code}' versionName='0.6.0'"]
    if permission:
        lines.append(f"uses-permission: name='{permission}'")
    return "\n".join(lines)


def certificate_line(fingerprint=HASH):
    return f"Signer #1 certificate SHA-256 digest: {fingerprint}"


class DevelopmentApkTests(unittest.TestCase):
    def test_valid_package_and_signer(self):
        self.assertEqual(badging(package_line(), 120), [])
        self.assertEqual(signer(certificate_line(), HASH), [])

    def test_version_code_mismatch(self):
        self.assertTrue(any("versionCode" in x for x in badging(package_line(code=119), 120)))

    def test_debug_identity_must_not_pass(self):
        self.assertTrue(badging(package_line(name="com.goreecloud.keyboard.florisbridge.debug"), 120))

    def test_existing_system_identity_must_not_pass(self):
        self.assertTrue(badging(package_line(name="com.goreecloud.keyboard"), 120))

    def test_network_permission_must_fail(self):
        self.assertTrue(any("INTERNET" in x
                            for x in badging(package_line(permission="android.permission.INTERNET"), 120)))

    def test_unknown_certificate_must_fail(self):
        self.assertTrue(signer(certificate_line(), "b" * 64))

    def test_missing_certificate_record_fails(self):
        self.assertTrue(signer(certificate_line(), ""))

    def test_multiple_signers_fail(self):
        self.assertTrue(signer(certificate_line() + "\n" + certificate_line(), HASH))

    def test_unrecognized_certificate_format_fails(self):
        self.assertTrue(signer("invalid certificate details", HASH))


if __name__ == "__main__":
    unittest.main()
