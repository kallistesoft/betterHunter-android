#!/usr/bin/env bash
# Builds the debug app (ca.kallistesoft.hunt.debug, "BetterHunter Dev", test ads) and installs it NEXT TO the Play version on every
# connected device except emulators, or on the serials given. Never uninstalls anything.
# Installing force-closes the running dev app: tell the owner first (it looks like a crash).
#   tools/install_dev.sh [SERIAL...]
set -euo pipefail
cd "$(dirname "$0")/.."
export PATH="$HOME/Library/Android/sdk/platform-tools:$PATH"
export JAVA_HOME="${JAVA_HOME:-/Applications/Android Studio.app/Contents/jbr/Contents/Home}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"

./gradlew --quiet :app:assembleDebug
apk=app/build/outputs/apk/debug/app-debug.apk
serials=("$@")
[ ${#serials[@]} -gt 0 ] || serials=($(adb devices | awk 'NR>1 && $2=="device" && $1 !~ /^emulator-/ {print $1}'))
[ ${#serials[@]} -gt 0 ] || { echo "No device connected (emulators are skipped unless named)." >&2; exit 1; }
for s in "${serials[@]}"; do
  echo "$s ($(adb -s "$s" shell getprop ro.product.model | tr -d '\r'))"
  adb -s "$s" install -r "$apk"
  adb -s "$s" shell monkey -p ca.kallistesoft.hunt.debug -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1 || true
done
