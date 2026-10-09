"""Tests that final merged Android manifests remain mandatory privacy gates."""
from contextlib import redirect_stderr
from io import StringIO
from pathlib import Path
import runpy
import tempfile
import unittest

MODULE = runpy.run_path(str(Path(__file__).resolve().parents[1] / "verify-merged-manifests.py"))
audit = MODULE["audit"]
find_debug_manifests = MODULE["find_debug_manifests"]
A = "http://schemas.android.com/apk/res/android"


def merged_xml(permission=""):
    requested = f'<uses-permission android:name="{permission}"/>' if permission else ""
    return f'''<manifest xmlns:android="{A}">
        {requested}
        <application android:allowBackup="false">
            <service android:name=".KeyboardService" android:exported="true"
              android:permission="android.permission.BIND_INPUT_METHOD">
                <intent-filter><action android:name="android.view.InputMethod"/></intent-filter>
                <meta-data android:name="android.view.im" android:resource="@xml/method"/>
            </service>
        </application>
    </manifest>'''


class MergedManifestChecks(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name)
        self.manifest = self.root / "app/build/intermediates/merged_manifests/debug/processDebugManifest/AndroidManifest.xml"

    def test_missing_build_fails_closed(self):
        self.assertTrue(any("Missing debug merged" in error for error in audit(self.root)))

    def test_merged_manifest_passes(self):
        self.manifest.parent.mkdir(parents=True)
        self.manifest.write_text(merged_xml(), encoding="utf-8")
        self.assertEqual(find_debug_manifests(self.root), [self.manifest])
        self.assertEqual(audit(self.root), [])

    def test_network_permission_from_dependency_fails(self):
        self.manifest.parent.mkdir(parents=True)
        self.manifest.write_text(merged_xml("android.permission.INTERNET"), encoding="utf-8")
        self.assertTrue(any("INTERNET" in error for error in audit(self.root)))

    def test_other_variants_do_not_replace_debug_proof(self):
        self.manifest.parent.mkdir(parents=True)
        release = Path(str(self.manifest).replace("/debug/", "/release/"))
        release.parent.mkdir(parents=True)
        release.write_text(merged_xml(), encoding="utf-8")
        self.assertTrue(audit(self.root))


if __name__ == "__main__":
    unittest.main()
