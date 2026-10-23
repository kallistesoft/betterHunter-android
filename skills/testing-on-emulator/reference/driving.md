# Driving the emulator

`ADB=~/Library/Android/sdk/platform-tools/adb` (adb isn't on the PATH). `PKG` is `ca.kallistesoft.hunt.debug`, or
`ca.kallistesoft.hunt` for the release build.

## Contents
- Reading the screen
- Input
- Device settings (language, dark mode, font size, rotation)
- App state (reset, process death)
- When things go wrong

## Reading the screen

```sh
android layout --flat 2>/dev/null | python3 -I $S/screen_text.py    # compact list: selector, actions, @x,y
android layout --flat --full -o build/emulator-test/layout.json      # everything, including hidden nodes
android screen capture -o build/emulator-test/<nn>-<screen>.png
android screen capture --annotate -o build/emulator-test/a.png       # numbered boxes, for elements without text
android screen resolve ...                                           # turns "#3" from an annotated shot into x,y
```

In Compose, a clickable card has no text; its label is a separate node inside it. Tapping the label taps the card.

## Input

```sh
android input tap "text=Continue" --wait=3000
android input tap "text=What is it?" --match=first      # label shown more than once
android input scroll "scrollable=true" --dir=down --until="text=Photo of the day"
android input text "deer" "clz=EditText"                 # type into a field
$ADB shell input keyevent KEYCODE_BACK                   # system back (tests predictive back / NavDisplay onBack)
$ADB shell input swipe 540 1800 540 600 300              # scroll by hand: up the screen over 300 ms
$ADB shell input tap 540 794                             # last resort: coordinates from screen_text.py
```

After a tap that changes screens, wait for the new screen with `--wait` on the next tap, or `sleep 1` before
`android layout`; Compose animations otherwise show the old screen.

## Device settings

| Check | Command | Undo |
|---|---|---|
| French UI | `$ADB shell cmd locale set-app-locales $PKG --locales fr-CA` | `--locales en-CA` |
| Dark theme | `$ADB shell cmd uimode night yes` | `night no` |
| Large text | `$ADB shell settings put system font_scale 1.3` | `1.0` |
| Landscape | `$ADB shell settings put system user_rotation 1` (with `accelerometer_rotation 0`) | `user_rotation 0` |

The app also has its own FR/EN toggle on the home screen; test it once by tapping, use the command for the rest.
Undo every setting before finishing so the next test starts from the default.

## App state

```sh
$ADB shell pm clear $PKG                       # wipe progress and chosen exams: first-run flow
# Process death: the back stack (Serializable NavKeys) and quiz state must survive it.
$ADB shell input keyevent KEYCODE_HOME
sleep 2                                        # am kill only kills a background app; right after HOME it does nothing
$ADB shell am kill $PKG
$ADB shell pidof $PKG || echo killed           # must print "killed", or the test proves nothing
$ADB shell am start -n $PKG/ca.kallistesoft.hunt.MainActivity    # should reopen on the same screen
```

## When things go wrong

| Symptom | Fix |
|---|---|
| `adb shell` or `android layout` hangs | Run `$S/emulator.sh` again: it detects an unresponsive emulator and cold-boots it |
| "No app window on screen" | App crashed or is in background: run `$S/check_crash.sh`, then relaunch with `install.sh --no-build` |
| "No matching elements" | Label is a content description (`desc=…`), is off screen (scroll to it), or the screen is in French |
| Tap succeeds but screen doesn't change | Element disabled (e.g. Continue before choosing an exam), or a dialog is on top: dump the layout |
| Debug app opens on "Update required" (`testTag` `force-update`) | Firebase Remote Config's `min_version_code_debug` is greater than the local build's `versionCode` (1): set it back to 1 in the Firebase console, or build with `-PversionCode=<n>` |
| `INSTALL_FAILED_*` | `install.sh` already handles signature mismatch; for anything else show the message to the user |
| `android` CLI missing | `brew install --cask android-commandlinetools`, or use `$ADB shell uiautomator dump` and `$ADB exec-out screencap -p > shot.png` |
