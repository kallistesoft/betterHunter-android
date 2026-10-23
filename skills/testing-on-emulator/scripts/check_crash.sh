#!/usr/bin/env bash
# Reports crashes and ANRs of the app since the last install.sh launch (which clears logcat).
#
#   check_crash.sh [debug|release]      default: debug
#
# Exit 0 and "OK" when the app is alive with no crash; exit 1 with the stack trace otherwise.
set -euo pipefail

SDK="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
ADB="$SDK/platform-tools/adb"
if [[ "${1:-debug}" == release ]]; then
    PKG=ca.kallistesoft.hunt
else
    PKG=ca.kallistesoft.hunt.debug
fi

status=0

crash="$("$ADB" logcat -d -b crash 2>/dev/null | grep -A 40 "Process: $PKG\b" || true)"
if [[ -n "$crash" ]]; then
    echo "CRASH:"
    echo "$crash"
    status=1
fi

anr="$("$ADB" logcat -d 2>/dev/null | grep -A 15 "ANR in $PKG\b" || true)"
if [[ -n "$anr" ]]; then
    echo "ANR:"
    echo "$anr"
    status=1
fi

# R8 strips classes that are only reached by reflection; release builds show it as these errors, not crashes.
r8="$("$ADB" logcat -d 2>/dev/null | grep -E "ClassNotFoundException|NoSuchMethodException|SerializationException" | head -20 || true)"
if [[ -n "$r8" ]]; then
    echo "REFLECTION/SERIALIZATION ERRORS (often R8 rules missing):"
    echo "$r8"
    status=1
fi

if [[ -z "$("$ADB" shell pidof "$PKG" | tr -d '\r')" ]]; then
    echo "App process $PKG is not running (crashed, killed or never launched)."
    status=1
fi

[[ $status -eq 0 ]] && echo "OK: $PKG running, no crash or ANR since launch."
exit $status
