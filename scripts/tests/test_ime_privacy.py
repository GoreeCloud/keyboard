"""Regression tests for GoreeCloud Keyboard static Android privacy gates."""
from pathlib import Path
import importlib.util
import tempfile
import unittest

VERIFIER = Path(__file__).resolve().parents[1] / "verify-ime-boundaries.py"
spec = importlib.util.spec_from_file_location("keyboard_ime_boundaries", VERIFIER)
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)

A = 'http://schemas.android.com/apk/res/android'


def manifest_xml(allow_backup='false', permission=''):
    extra = f'<uses-permission android:name="{permission}"/>' if permission else ''
    return f"""<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="{A}">
  {extra}
  <application android:allowBackup="{allow_backup}">
    <service android:name=".KeyboardService"
      android:exported="true"
      android:permission="android.permission.BIND_INPUT_METHOD">
      <intent-filter><action android:name="android.view.InputMethod"/></intent-filter>
      <meta-data android:name="android.view.im" android:resource="@xml/method"/>
    </service>
  </application>
</manifest>
"""


class ManifestPrivacyTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.path = Path(self.tmp.name) / 'AndroidManifest.xml'

    def check(self, xml):
        self.path.write_text(xml, encoding='utf-8')
        return module.verify_manifest(self.path)

    def test_minimal_ime_passes(self):
        self.assertEqual(self.check(manifest_xml()), [])

    def test_automatic_backup_fails_closed(self):
        self.assertTrue(any('backup' in x.lower()
                            for x in self.check(manifest_xml(allow_backup='true'))))

    def test_implicit_backup_setting_fails_closed(self):
        self.assertTrue(any('backup' in x.lower()
                            for x in self.check(manifest_xml().replace(' android:allowBackup="false"', ''))))

    def test_network_permission_fails_closed(self):
        self.assertTrue(any('INTERNET' in x
                            for x in self.check(manifest_xml(permission='android.permission.INTERNET'))))

    def test_cleartext_traffic_rejected(self):
        xml = manifest_xml().replace('android:allowBackup="false"', 'android:allowBackup="false" android:usesCleartextTraffic="true"')
        self.assertTrue(any('Cleartext' in error for error in self.check(xml)))

    def test_sdk23_network_permission_rejected(self):
        xml = manifest_xml().replace('<application', '<uses-permission-sdk-23 android:name="android.permission.INTERNET"/><application')
        self.assertTrue(any('INTERNET' in error for error in self.check(xml)))

    def test_second_ime_service_rejected(self):
        xml = manifest_xml()
        service = xml.split('<service', 1)[1].split('</service>', 1)[0]
        second = '<service' + service.replace('.KeyboardService', '.SecondKeyboardService') + '</service>'
        xml = xml.replace('</application>', second + '</application>')
        self.assertTrue(any('found 2' in error for error in self.check(xml)))

    def test_ime_binding_is_mandatory(self):
        xml = manifest_xml().replace('android.permission.BIND_INPUT_METHOD', 'android.permission.NORMAL')
        self.assertTrue(any('BIND_INPUT_METHOD' in x for x in self.check(xml)))


class BackupPrivacyTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name)
        self.old = self.root / 'app/src/main/res/xml/backup_rules.xml'
        self.new = self.root / 'app/src/main/res/xml-v31/backup_rules.xml'
        self.old.parent.mkdir(parents=True)
        self.new.parent.mkdir(parents=True)
        self.old.write_text('<full-backup-content><exclude domain="root" path="."/></full-backup-content>', encoding='utf-8')
        self.new.write_text('<data-extraction-rules><cloud-backup><exclude domain="root" path="."/></cloud-backup><device-transfer><exclude domain="root" path="."/></device-transfer></data-extraction-rules>', encoding='utf-8')

    def check(self):
        return module.verify_backup_rules(self.root)

    def test_explicit_exclusions_pass(self):
        self.assertEqual(self.check(), [])

    def test_root_include_is_rejected(self):
        self.old.write_text('<full-backup-content><include domain="root" path="."/></full-backup-content>', encoding='utf-8')
        self.assertTrue(any('inclusion' in x for x in self.check()))

    def test_device_transfer_must_exist(self):
        self.new.write_text('<data-extraction-rules><cloud-backup><exclude domain="root" path="."/></cloud-backup></data-extraction-rules>', encoding='utf-8')
        self.assertTrue(any('device-transfer' in x for x in self.check()))

    def test_partial_cloud_exclusion_is_rejected(self):
        self.new.write_text('<data-extraction-rules><cloud-backup><exclude domain="file" path="cache"/></cloud-backup><device-transfer><exclude domain="root" path="."/></device-transfer></data-extraction-rules>', encoding='utf-8')
        self.assertTrue(any('cloud-backup' in x for x in self.check()))

    def test_missing_rule_file_fails_closed(self):
        self.new.unlink()
        self.assertTrue(any('invalid backup rules' in x for x in self.check()))


if __name__ == '__main__':
    unittest.main()
