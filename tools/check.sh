#!/usr/bin/env bash
# Local gate before every push (the global pre-push hook runs it, so failures show before CI does).
# Same steps as the "Android CI" workflow, minus the release build: conventions, unit tests, Kover (90 % of lines
# for every class) and the app's lint.
#   tools/check.sh
set -euo pipefail
cd "$(dirname "$0")/.."
export JAVA_HOME="${JAVA_HOME:-/Applications/Android Studio.app/Contents/jbr/Contents/Home}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"

scripts/check-conventions.sh
./gradlew --quiet testDebugUnitTest :core:model:test :koverVerifyUnit :app:lintDebug
echo "check: OK"
