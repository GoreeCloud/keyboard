#!/usr/bin/env bash
# A bounded, reversible USB installability check; no typing or default-IME change.
set -euo pipefail

if (( $# != 3 )); then
  echo "Usage: $0 <adb-serial> <verified-debug-apk> <expected-sha256>" >&2
  exit 64
fi

serial="$1"
apk="$2"
expected_sha="$3"
candidate="com.goreecloud.keyboard.florisbridge.debug"
system_package="com.goreecloud.keyboard"

test -s "$apk" || { echo "APK not found or empty" >&2; exit 2; }
command -v sha256sum >/dev/null || { echo "SHA-256 utility unavailable" >&2; exit 2; }
actual_sha="$(sha256sum "$apk" | cut -d " " -f 1)"
test "$actual_sha" = "$expected_sha" || { echo "APK SHA-256 does not match verified build" >&2; exit 2; }
command -v adb >/dev/null || { echo "ADB unavailable" >&2; exit 2; }
test "$(adb -s "$serial" get-state)" = "device" || { echo "Device not authorized" >&2; exit 2; }
test -n "$(adb -s "$serial" shell pm path "$system_package" | tr -d '\r')" || {
  echo "Existing system Keyboard was not found; refusing test" >&2
  exit 2
}
test -z "$(adb -s "$serial" shell pm path "$candidate" | tr -d '\r')" || {
  echo "Candidate already installed; will not overwrite or uninstall it" >&2
  exit 2
}

# Never reveal the user's existing default IME identifier in console logs.
original_default="$(adb -s "$serial" shell settings get secure default_input_method | tr -d '\r')"
installed_by_test=false
cleanup() {
  if [[ "$installed_by_test" == true ]]; then
    if ! adb -s "$serial" uninstall "$candidate" >/dev/null; then
      echo "ALERT: Unable to remove temporary candidate. Manual review required." >&2
      return 1
    fi
    if test -n "$(adb -s "$serial" shell pm path "$candidate" | tr -d '\r')"; then
      echo "ALERT: Temporary candidate still installed." >&2
      return 1
    fi
  fi
  if test "$(adb -s "$serial" shell settings get secure default_input_method | tr -d '\r')" != "$original_default"; then
    echo "ALERT: Default IME changed; review device state manually." >&2
    return 1
  fi
  test -n "$(adb -s "$serial" shell pm path "$system_package" | tr -d '\r')" || {
    echo "ALERT: Original GoreeCloud system Keyboard is missing." >&2
    return 1
  }
}
trap cleanup EXIT

# The APK must have passed exact-revision CI and its compiled-APK metadata gate.
installed_by_test=true
adb -s "$serial" install -t "$apk"

test -n "$(adb -s "$serial" shell pm path "$candidate" | tr -d '\r')" || {
  echo "Candidate package did not register" >&2
  exit 1
}
adb -s "$serial" shell ime list -s | tr -d '\r' | grep -F "$candidate" >/dev/null || {
  echo "Candidate input-method service did not register" >&2
  exit 1
}
test "$(adb -s "$serial" shell settings get secure default_input_method | tr -d '\r')" = "$original_default" || {
  echo "Default IME changed unexpectedly" >&2
  exit 1
}
echo "USB debug installability and IME registration passed; cleanup follows."
