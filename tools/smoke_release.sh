#!/usr/bin/env bash
# Release smoke test: installs the R8 release APK on an EMULATOR, launches it, waits 15 s, then fails if the app died
# or logcat shows a FATAL EXCEPTION for it. Debug builds and green tests don't catch R8 startup crashes (Unarrow build
# 34 was rejected by Play for one), so run this before any Play upload. build_release.sh calls it.
#   tools/smoke_release.sh [path/to/app-release.apk]    (default: builds :app:assembleRelease first)
# ANDROID_SERIAL defaults to emulator-5554; any non-emulator serial is refused (never the owner's phone or tablet).
# Never uninstalls: if the installed app has another signature, the install fails and the script says so.
set -euo pipefail
cd "$(dirname "$0")/.."
export PATH="$HOME/Library/Android/sdk/platform-tools:$PATH"
export JAVA_HOME="${JAVA_HOME:-/Applications/Android Studio.app/Contents/jbr/Contents/Home}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
export ANDROID_SERIAL="${ANDROID_SERIAL:-emulator-5554}"
pkg=ca.kallistesoft.hunt

case "$ANDROID_SERIAL" in
  emulator-*) ;;
  *) echo "FAIL: $ANDROID_SERIAL is not an emulator; the smoke test only runs on emulators." >&2; exit 1 ;;
esac
[ "$(adb -s "$ANDROID_SERIAL" get-state 2>/dev/null)" = device ] || { echo "FAIL: $ANDROID_SERIAL is not connected." >&2; exit 1; }

apk="${1:-}"
if [ -z "$apk" ]; then
  ./gradlew --quiet :app:assembleRelease ${VERSION_CODE:+-PversionCode=$VERSION_CODE}
  apk=app/build/outputs/apk/release/app-release.apk
fi
[ -f "$apk" ] || { echo "FAIL: no signed release APK at $apk (keystore.properties missing?)." >&2; exit 1; }

adb install -r "$apk" >/dev/null || { echo "FAIL: install refused (another signature installed? never uninstall: use another emulator)." >&2; exit 1; }
adb logcat -c
adb shell monkey -p "$pkg" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
sleep 15

pid=$(adb shell pidof "$pkg" | tr -d '\r' || true)
crash=$(adb logcat -d | grep -A 3 'FATAL EXCEPTION' | grep -F "Process: $pkg" || true)
if [ -n "$crash" ] || [ -z "$pid" ]; then
  if [ -n "$crash" ]; then why="crashed"; else why="is not running"; fi
  echo "FAIL: $pkg $why 15 s after launch on $ANDROID_SERIAL."
  adb logcat -d | grep -A 30 'FATAL EXCEPTION' | head -60 || true
  exit 1
fi
echo "OK: $pkg running 15 s after launch on $ANDROID_SERIAL (pid $pid), no FATAL EXCEPTION."
