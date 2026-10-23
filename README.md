# BetterHunter for Android

*Chassez mieux. / Hunt better.* Native Android version of BetterHunter: practice for the Canadian firearms licence
(PAL/CFSC, CRFSC) and hunter education exams (Québec ICAF, Alberta), plus "What is it?", a photo game for
recognising Canada's game animals. Bilingual French/English, everything stays on the device.

The questions, exam recipes and photos come from the [hunt](https://github.com/kallistesoft/hunt) repo (the
Flutter proof of concept and the question pipeline); this app ships a copy of them.

Console setup still to do (Firebase, AdMob, Play): see [FOLLOW-UP.md](FOLLOW-UP.md). Product ideas (two parts,
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
core/designsystem    Theme (from the icon's green), shared components
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
| Update rules | `…/android/update-debug.json` | `…/android/update.json` |
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

On every launch and every return to the foreground, the app reads a small JSON file:

```json
{ "minVersionCode": 3 }
```

If the installed `versionCode` is below `minVersionCode`, the app shows only a blocking "Update required" screen.
Its button starts Google Play's **immediate in-app update** when Play offers one, and opens the Play Store listing
otherwise (sideloaded or debug builds). An update interrupted by leaving the app resumes when the user comes back.

- The last rules fetched are kept on the device, so a blocked version stays blocked offline.
- If the file can't be read (offline before the first fetch, 404, bad JSON), the app is never blocked.
- Unknown fields are ignored, so the file can carry more settings later.

Host the files on the web app's site (`hunt.kallistesoft.ca`), e.g. in the hunt repo at `app/web/android/update.json`
and `app/web/android/update-debug.json`; they are deployed with the site. To force everyone onto a new release:
publish it on Play, wait until it is available, then raise `minVersionCode` to its `versionCode`. Use the debug file to
try the flow on a debug build without affecting users.

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
  force-update rules and its feature-flag fetch interval.
- **No dp literals outside the theme.** Gaps come from `Spacing`, component sizes from `Sizes` (both in
  `core/designsystem/.../theme`), corners from `MaterialTheme.shapes`. A new size is added there first.

## UI components and screenshots

Reusable pieces live in two modules:

- `core:designsystem`, no app knowledge: theme, `Spacing` and `Sizes`, `BhCard`, `IconBadge`, `AnswerChoice` (idle, selected,
  correct, wrong), `AnswerResult`, `NoticeCard`, `HubCard`, `LinkRow`, `ProgressRing`, `SectionTitle`.
- `core:ui`, built on the app's models: `ExamCard`, `ExamListItem`, `ExamAvatar`, `ReadinessRing`, `QuestionPhoto`,
  `SourceLinks`, `BackTopAppBar`, plus `PreviewData` (sample exams and questions).

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

Questions and photos live in `core/data/src/main/assets/` and are checked by `BundledContentTest`. To update them
from the hunt repo:

```sh
scripts/sync-content.sh ../hunt
```

Image paths in the question banks keep the Flutter form (`assets/wildlife/…`); the parser drops the `assets/`
prefix.

## Not done yet

- AdMob: declared in Play Console, not integrated yet. The account and ad unit setup is in [FOLLOW-UP.md](FOLLOW-UP.md).
- Baseline profile (`profileinstaller` is in place; add a `:baselineprofile` module with a device in CI).
- Instrumented UI tests (the Compose test dependencies are set up; current tests are JVM unit tests: model rules,
  bundled content, force update, and every ViewModel's state stream with Turbine).
