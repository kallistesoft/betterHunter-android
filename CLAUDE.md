# Notes for Claude

BetterHunter, native Android (Kotlin, Compose, Hilt, Nav3), package `ca.kallistesoft.hunt` (debug: `.debug`,
"BetterHunter Dev"). Not the Flutter app in kallistesoft/hunt (~/Sources/Repos/HuntExamPrep): that one is the retired
proof of concept. To-dos live in `FOLLOW-UP.md` and `IDEAS.md`; product decisions in `DECISIONS.md`, which also has
the table of non-blocking choices Claude made with its default for the owner to validate later (ask only when
blocked). Never show provincial course names in the UI (DECISIONS P13; guard tests enforce it).

## Branch model

- Feature branch → push → PR against `main`; the owner merges. Never stacked PRs.
- `main` has the "Protect main" ruleset (PR + 1 approval + `build` and `integration` checks). The owner merges with
  his admin bypass; never run `gh pr merge --admin` or bypass it any other way. No bot commits, no "approve
  workflow" step. Report CI on the PR; never suggest merging with red CI.

## Build, check, release

- Local builds: prefix Gradle with
  `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ANDROID_HOME=~/Library/Android/sdk`.
- `tools/check.sh` (conventions, unit tests, Kover 90 % per class, `:app:lintDebug`) runs before every push (global
  pre-push hook). Screenshot verification fails locally on macOS for 4 references even on main: Linux CI is the
  reference for those (`scripts/record-screenshots.sh` records them).
- `tools/build_release.sh` (clean, up-to-date `main` only; needs `keystore.properties`): automatic versionCode
  (commits on main; check the Play track's last code, `VERSION_CODE=<n>` overrides), signed AAB + release APK, then
  `tools/smoke_release.sh` on `emulator-5554`. Never send a build to Play without that R8 smoke test passing.
  Building is fine; Play releases and uploads only when the owner asks.
- `tools/install_dev.sh [SERIAL]` installs the debug app next to the Play app (never uninstall the store app);
  announce installs on the owner's devices first.
- `connectedAndroidTest` (or any connected* task) only with `ANDROID_SERIAL=emulator-5554`: it once ran on the
  owner's Pixel and uninstalled BetterHunter with its progress.

## Design mockup

The BetterHunter design mockup (dark and light screens) is a claude.ai Design canvas:
https://claude.ai/artifact/NSq8SRajcbQ8UmAv5cJaer

To add or change screens, read the canvas with the Artifact tool (`action: "read"`, then its `project/canvas.json` and
the screens' `project/*.dc.html` files) and publish to that same URL; don't create a new canvas. Follow the palette,
fonts and patterns in `design/README.md`, and give every new screen a dark and a light version. After publishing,
copy the changed files into `design/mockup/` and update `design/README.md`.

## Skills

Repo skills live in `.claude/skills/`. Read the skill's `SKILL.md` and follow it when the task matches:

- [testing-on-emulator](.claude/skills/testing-on-emulator/SKILL.md): build, install and test the app on the Android
  emulator (smoke test, a change, a release/R8 build before a Play upload)
- [viewmodel-state](.claude/skills/viewmodel-state/SKILL.md): how ViewModels build their state (no `init`; `combine` +
  `stateIn`, or `asStateFlow().onStart { }` + `stateIn`), enforced by `scripts/check-conventions.sh`
- [unit-testing](.claude/skills/unit-testing/SKILL.md): unit tests (JVM, Robolectric Compose) and Kover coverage, 90% of
  lines for every class (`./gradlew :koverVerifyUnit`); use with any code change and when the Coverage step fails
