#!/bin/sh
# Flow tests and the release smoke test on the CI emulator (run by the "integration" job, emulator already booted).
# Espresso's pressBack needs the app's window to have focus. On the slow software-rendered emulator a system
# "... isn't responding" dialog can hold it for the whole run, failing every test that presses Back, so error dialogs
# are hidden and any open one closed first. When tests fail, the focused window, a screenshot and logcat are saved in
# build/ci-diagnostics for the job's artifacts.
cd "$(dirname "$0")/.." || exit 1

if [ "${CI_KEEP_ERROR_DIALOGS:-}" != 1 ]; then
  adb shell settings put global hide_error_dialogs 1
  adb shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS > /dev/null
fi
adb logcat -c

./gradlew :app:connectedDebugAndroidTest :smoketest:connectedQaAndroidTest
status=$?

if [ $status -ne 0 ]; then
  out=build/ci-diagnostics
  mkdir -p $out
  adb shell dumpsys window | grep -E 'mCurrentFocus|mFocusedApp|mFocusedWindow' > $out/focus.txt
  adb exec-out screencap -p > $out/screen.png
  adb logcat -d > $out/logcat.txt
  cat $out/focus.txt
  grep -E 'ANR in|not responding' $out/logcat.txt | head -20
fi
exit $status
