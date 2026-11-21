#!/usr/bin/env bash
# Builds the signed Play bundle (.aab) and the release APK with an automatic build number (versionCode): the number of
# commits on main, never below min_build (Play refuses a code it has already seen; last code known when this script
# was added: none recorded (CI bundles use the run number, up to ~135)). VERSION_CODE=<n> overrides it: check the Play track's last code first.
# The version name stays in app/build.gradle.kts (change it through a PR); the script warns when it was already used.
# Then runs tools/smoke_release.sh on emulator-5554 when that emulator is connected (never on a real device).
# It only builds: uploading to Play stays the owner's call.
# Run from a clean, up-to-date main: tools/build_release.sh
set -euo pipefail
cd "$(dirname "$0")/.."
export PATH="$HOME/Library/Android/sdk/platform-tools:$PATH"
export JAVA_HOME="${JAVA_HOME:-/Applications/Android Studio.app/Contents/jbr/Contents/Home}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
min_build=1

branch=$(git rev-parse --abbrev-ref HEAD)
[ "$branch" = main ] || { echo "Switch to main first (on $branch)." >&2; exit 1; }
[ -z "$(git status --porcelain)" ] || { echo "Commit or stash your changes first." >&2; exit 1; }
git pull --ff-only --quiet
[ "$(git rev-parse HEAD)" = "$(git rev-parse origin/main)" ] || { echo "main differs from origin/main: push or pull first." >&2; exit 1; }
[ -f keystore.properties ] || [ -n "${BH_KEYSTORE_FILE:-}" ] || { echo "keystore.properties is missing: the release would be unsigned (see README, signing)." >&2; exit 1; }
[ -f app/google-services.json ] || { echo "app/google-services.json is missing: the release would ship without Firebase (copy it from the main checkout or the console)." >&2; exit 1; }
build=${VERSION_CODE:-$(git rev-list --count HEAD)}
[ "$build" -ge "$min_build" ] || build=$min_build
name=$(sed -n 's/^ *versionName = "\([^"]*\)".*/\1/p' app/build.gradle.kts | head -n 1)
if git tag --list "release/$name-*" | grep -q .; then
  echo "warning: versionName $name was already built ($(git tag --list "release/$name-*" | tr '\n' ' ')); bump it in app/build.gradle.kts through a PR if this is a new release." >&2
fi

tools/check.sh
./gradlew --quiet :app:bundleRelease :app:assembleRelease -PversionCode="$build"

out="app/build/outputs/bundle/release/betterhunter-$name-$build.aab"
cp app/build/outputs/bundle/release/app-release.aab "$out"
echo
echo "Built $out (version $name, build $build)."

if [ "$(adb -s emulator-5554 get-state 2>/dev/null)" = device ]; then
  ANDROID_SERIAL=emulator-5554 tools/smoke_release.sh app/build/outputs/apk/release/app-release.apk
else
  echo "warning: emulator-5554 is not running, so the release smoke test was SKIPPED. Start it and run" >&2
  echo "         tools/smoke_release.sh app/build/outputs/apk/release/app-release.apk before any Play upload." >&2
fi
git tag "release/$name-$build" 2>/dev/null || true
echo "Local tag release/$name-$build. Upload it in Play Console › BetterHunter › Test and release, only when the owner asks."
