# BetterHunter for Android

*Chassez mieux. / Hunt better.* Native Android version of BetterHunter: practice for the Canadian firearms licence
(PAL/CFSC) and hunter education exams (Québec ICAF, Alberta), plus "What is it?", a photo game for
recognising Canada's game animals. Bilingual French/English, everything stays on the device.

The questions, exam recipes and photos live in this repo (`core/data/src/main/assets/`) and ship inside the app.
They are edited here; the app takes no content from another repo.

Console setup still to do (Firebase, AdMob, Play): see [FOLLOW-UP.md](FOLLOW-UP.md). Play Store listing texts, images and
console declarations: [store/](store/). Product ideas (two parts,
monetization): [IDEAS.md](IDEAS.md). Decisions and why: [DECISIONS.md](DECISIONS.md).

> Independent study aid: not affiliated with or endorsed by the RCMP, the Canadian Firearms Program, the Government
> of Québec, FédéCP or any provincial agency.

## Stack

| | |
|---|---|
| Language | Kotlin 2.4.21 (AGP built-in Kotlin), Java 17 bytecode |
| Build | Gradle 9.8.2, Android Gradle Plugin 9.4.0, version catalog, convention plugins (`build-logic/`) |
| UI | Jetpack Compose (BOM 2026.09.00), Material 3, edge-to-edge, splash screen API |
| Navigation | Navigation 3 (1.2.0) with per-entry ViewModels and a saveable back stack |
| Architecture | MVVM + unidirectional data flow: repositories → `StateFlow` UI state → stateless composables |
| DI | Hilt 2.60.1 with KSP 2.3.12 |
| Storage | DataStore (progress as JSON; cached update rules as preferences) |
| Images | Coil 3 (bundled photos from assets) |
| Other | Firebase Remote Config (feature flags), per-app language (AppCompat), Custom Tabs, Play In-App Updates |
| SDK | compileSdk / targetSdk 37, minSdk 26 |

## Modules

```
app/                 Activity, Navigation 3 graph, Hilt app, build types and signing
core/model           Pure Kotlin: exams, questions, quiz session, progress rules, sources (JVM unit tests)
core/testing         Test doubles (fake repositories and flags, sample content), main dispatcher rule, Turbine helpers
core/data            Bundled content (assets/), progress repository (DataStore)
core/designsystem    Theme (the mockup's palette, Barlow fonts), shared components
core/flags           Feature flags: Firebase Remote Config, with in-app defaults
core/ui              Shared composables (exam card, question photo, source links) and all UI strings (EN/FR)
core/update          Force update: remote rules, cache, update screen with Play In-App Updates
feature/home         Dashboard: daily goal, streak, hubs, photo of the day
feature/exams        Exam training, exam picker, recognition games
feature/quiz         Practice, mock exam (timed), review of mistakes, results
feature/about        Disclaimer, how it works, sources, contact, privacy
```

Design mockup (visual reference for new screens, dark and light): [design/](design/README.md).

## Build types

| | `debug` | `release` |
|---|---|---|
| Application ID | `ca.kallistesoft.hunt.debug` (installs next to the Play app) | `ca.kallistesoft.hunt` |
| Name | BetterHunter Dev | BetterHunter |
| Version name | `1.0.0-debug` | `1.0.0` |
| Code shrinking | no | R8 minify + resource shrinking |
| Force update parameter | `min_version_code_debug` | `min_version_code` |
| Signing | debug key | release key if configured, unsigned otherwise |

```sh
./gradlew assembleDebug          # app/build/outputs/apk/debug/
./gradlew bundleRelease          # app/build/outputs/bundle/release/ (upload to Play Console)
./gradlew testDebugUnitTest :core:model:test
./gradlew :app:lintDebug
```

The `versionCode` is 1 unless `-PversionCode=<n>` is given (CI uses the run number). Every upload to Play needs a
higher one; bump `versionName` in `app/build.gradle.kts` for each release.

### Release signing

Create `keystore.properties` at the repo root (git-ignored):

```properties
storeFile=release.jks
storePassword=…
keyAlias=…
keyPassword=…
```

or set `BH_KEYSTORE_FILE`, `BH_KEYSTORE_PASSWORD`, `BH_KEY_ALIAS`, `BH_KEY_PASSWORD`. In CI, add the secrets
`BH_KEYSTORE_BASE64` (the keystore, base64), `BH_KEYSTORE_PASSWORD`, `BH_KEY_ALIAS` and `BH_KEY_PASSWORD`. The app uses
Play App Signing, so this is the upload key.

## Force update

On every launch and every return to the foreground, the app reads the minimum allowed `versionCode` from Firebase
Remote Config: the Number parameter `min_version_code` (release builds) or `min_version_code_debug` (debug builds).

If the installed `versionCode` is below it, the app shows only a blocking "Update required" screen. Its button starts
Google Play's **immediate in-app update** when Play offers one, and opens the Play Store listing otherwise (sideloaded
or debug builds). An update interrupted by leaving the app resumes when the user comes back.

- The last minimum fetched is kept on the device, so a blocked version stays blocked offline.
- If it was never fetched (offline on first launch, Firebase not configured), the app is never blocked.
- A parameter missing in the console counts as 0 (nobody blocked).

To force everyone onto a new release: publish it on Play, wait until it is available, then set `min_version_code` to
its `versionCode` in the Firebase console and publish. Use `min_version_code_debug` to try the flow on a debug build
without affecting users. Setup: [FOLLOW-UP.md](FOLLOW-UP.md), section 1.

## Feature flags (Firebase Remote Config)

Flags are Boolean Remote Config parameters, declared in `core/flags/.../FeatureFlag.kt` with their in-app default:

| Parameter | Default | Controls |
|---|---|---|
| `photo_of_the_day` | `true` | The photo of the day on the dashboard |
| `recognition_games` | `true` | The "Can you recognise them?" hub |
| `report_question` | `true` | "Report a problem" on each question |

ViewModels read them as a `Flow` (`FeatureFlags.isEnabled(flag)` or `values`), so screens update as soon as new values
are activated: at launch and on each return to the foreground (`fetchAndActivate`, at most hourly in release, every time
in debug), and live through Remote Config's real-time updates while the app is open. A flag keeps its default until
the console gives it a value, and when Firebase is offline or not configured.

To add a flag: add an entry to `FeatureFlag`, create the parameter in the Firebase console (Remote Config), and read it in
the ViewModel that needs it.

### Setup

Creating the Firebase project and the flags is described step by step in [FOLLOW-UP.md](FOLLOW-UP.md), along
with the AdMob and Play Console setup.

Without `google-services.json` the Google Services plugin isn't applied and the app runs with the defaults, so pull
requests and forks still build. Remote Config uses a Firebase installation ID (already covered by "Device or other IDs"
in the Play Console data safety form); no analytics are collected.

## Conventions

Checked by `scripts/check-conventions.sh` (CI runs it on every push):

- **No build-type branches in code.** Never `if (BuildConfig.DEBUG)`: code that differs goes in `app/src/debug` and
  `app/src/release`. Today each provides a `BuildTypeModule` (Hilt) with the build type's `BuildTypeConfig`: its
  force-update parameter and its Remote Config fetch interval.
- **No dp literals outside the theme.** Gaps come from `Spacing`, component sizes from `Sizes` (both in
  `core/designsystem/.../theme`), corners from `MaterialTheme.shapes`. A new size is added there first.

## UI components and screenshots

Reusable pieces live in two modules:

- `core:designsystem`, no app knowledge: theme (colours, Barlow typography, shapes), `Spacing` and `Sizes`, `BhCard`,
  `IconBadge`, `AnswerChoice` (idle, selected, correct, wrong; lettered row or two-by-two tile), `AnswerResult`,
  `ResultChip`, `NoticeCard`, `HubCard`, `LinkRow`, `ProgressRing`, `MeterBar`, `QuizProgress`, `PillToggle`,
  `PrimaryButton` / `SecondaryButton` / `LinkButton`, `SectionTitle` / `Overline`, `BuckLogo`.
- `core:ui`, built on the app's models: `ExamCard`, `ExamListItem`, `ExamAvatar`, `ReadinessRing`, `QuestionPhoto`
  (with `PhotoCaption`), `SourceLinks`, `BackTopAppBar`, plus `PreviewData` (sample exams and questions).

The look follows the design mockup ([design/](design/README.md)): forest greens, blaze orange for the one main action
and the current selection, green for progress and right answers, Barlow Condensed headings over Barlow text. The fonts
are bundled in `core/designsystem/src/main/res/font` (SIL Open Font License, copy in
`core/designsystem/src/main/assets/licenses/barlow-OFL.txt`).

Screens are split into a stateful entry point (ViewModel) and a stateless `…Content` composable. Every component and
screen has `@BhPreviews` previews: light in English and dark in French.

### Screenshot tests

Each preview is also a [Roborazzi](https://github.com/takahirom/roborazzi) screenshot test (Robolectric, no device),
in `*ScreenshotTest.kt` of its module. Reference images live in `src/test/screenshots/`.

```sh
./gradlew recordRoborazziDebug   # write or update the references
./gradlew verifyRoborazziDebug   # compare; diffs go to build/outputs/roborazzi
```

CI verifies on every push. After an intended UI change, run the **Record screenshots** workflow (Actions tab): it
records on CI's machine and commits the new references, so they match what CI renders.

## Content

This repo is the source of truth for the questions (DECISIONS.md, P4). Everything is in `core/data/src/main/assets/`:

| Path | Holds |
|---|---|
| `content/exams.json` | Exam recipes: name (FR/EN), the question banks (scopes) they draw from, question count, minutes, pass mark |
| `content/questions/<scope>.json` | One question bank per scope (`federal`, `hunter-common`, `QC`, `AB`, `wildlife-id`) |
| `wildlife/` | Photos of the "What is it?" questions (Wikimedia Commons, credited per question) |

Add or correct questions by editing these files in a pull request, or with the question pipeline and photo tools in
[content-tools/](content-tools/README.md) (manual → facts → bilingual questions → blind check → review sheet →
bundle), which write into these folders. `BundledContentTest` checks every bank (both languages, four choices, a valid
answer, photos present with their credit). Image paths in the banks are written
`assets/wildlife/…`; the parser drops the `assets/` prefix.

## Not done yet

- AdMob: declared in Play Console, not integrated yet. The account and ad unit setup is in [FOLLOW-UP.md](FOLLOW-UP.md).
- Baseline profile (`profileinstaller` is in place; add a `:baselineprofile` module with a device in CI).
- Instrumented UI tests (the Compose test dependencies are set up; current tests are JVM unit tests: model rules,
  bundled content, force update, and every ViewModel's state stream with Turbine).
