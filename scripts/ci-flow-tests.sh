#!/bin/sh
# Flow tests and the release smoke test on the CI emulator (run by the "integration" job, emulator already booted).
# Espresso's pressBack needs the app's window to have focus. On the slow software-rendered emulator another app's
# "Application Not Responding" dialog (seen: the Pixel launcher, already open when the tests start) can hold it for the
# whole run, failing every test that presses Back. Error dialogs are hidden, and any ANR dialog that has the focus,
# before or during the tests, is closed by stopping its app. When tests fail, the focused window, a screenshot and
# logcat are saved in build/ci-diagnostics for the job's artifacts.
cd "$(dirname "$0")/.." || exit 1

# Stops the app whose ANR dialog has the focus, which closes the dialog. Prints the app, or nothing.
close_anr_dialog() {
  app=$(adb shell dumpsys window | sed -n 's/.*mCurrentFocus=.*Application Not Responding: \([^ }]*\).*/\1/p')
  if [ -n "$app" ]; then
    adb shell am force-stop "$app"
    echo "Closed the ANR dialog of $app"
  fi
}

if [ "${CI_KEEP_ERROR_DIALOGS:-}" != 1 ]; then
  adb shell settings put global hide_error_dialogs 1
  adb shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS > /dev/null
  close_anr_dialog
  # One may open during the run too.
  while sleep 5; do close_anr_dialog; done &
  watcher=$!
fi
adb logcat -c

./gradlew :app:connectedDebugAndroidTest :smoketest:connectedQaAndroidTest
status=$?
[ -n "${watcher:-}" ] && kill "$watcher"

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
