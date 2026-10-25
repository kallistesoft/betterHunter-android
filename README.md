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
core/testing         Test doubles (fake repositories, flags and ads, sample content), main dispatcher rule, Turbine helpers
core/ads             AdMob: consent (UMP), the interstitial after a finished test, behind InterstitialAds / AdConsent
core/data            Bundled content (assets/), progress repository (DataStore)
core/designsystem    Theme (the mockup's palette, Barlow fonts), shared components
core/flags           Feature flags: Firebase Remote Config, with in-app defaults
core/ui              Shared composables (exam card, question photo, source links) and all UI strings (EN/FR)
core/update          Force update: remote rules, cache, update screen with Play In-App Updates
feature/onboarding   First launch: what kind of hunter, what they hunt with, where (the province sets the quizzes)
feature/home         Dashboard: daily goal, streak, hubs, photo of the day (new-hunter version after a real exam)
feature/exams        Exam training, My provinces, "I passed the real exam", recognition games
feature/quiz         Practice, mock exam (timed), review of mistakes, results
feature/settings     Settings: profile (hunter type, weapons, where you hunt, my provinces), language, daily goal
feature/about        Disclaimer, how it works, sources
```

Design mockup (visual reference for new screens, dark and light): [design/](design/README.md).

## Build types

| | `debug` | `release` |
|---|---|---|
| Application ID | `ca.kallistesoft.hunt.debug` (installs next to the Play app) | `ca.kallistesoft.hunt` |
| Name | BetterHunter Dev | BetterHunter |
| Icon | slate, with an orange "DEV" tag (`app/src/debug/res`) | the green buck (`app/src/main/res`) |
| Version name | `1.0.0-debug` | `1.0.0` |
| Code shrinking | no | R8 minify + resource shrinking |
| Force update parameter | `min_version_code_debug` | `min_version_code` |
| Signing | debug key | release key if configured, unsigned otherwise |

A third build type, `qa`, is the release build signed with the debug key, only for the release smoke test (see
"Integration tests"); it is never uploaded.

```sh
./gradlew assembleDebug          # app/build/outputs/apk/debug/
./gradlew bundleRelease          # app/build/outputs/bundle/release/ (upload to Play Console)
./gradlew testDebugUnitTest :core:model:test
./gradlew koverVerifyUnit        # unit test coverage: every class needs 90% of its lines (see below)
./gradlew :app:lintDebug
```

Coverage is measured by [Kover](https://github.com/Kotlin/kotlinx-kover) (`KoverConventionPlugin` in build-logic) over
the unit tests of every module except `:core:testing` and `:smoketest`, merged at the root, so a class may be covered
by another module's tests. `./gradlew koverHtmlReportUnit` writes `build/reports/kover/htmlUnit/` with the missed
lines. Only generated code (Hilt, Dagger, Compose singletons, `BuildConfig`) and `@Preview` functions are left out.

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

## Ads (AdMob)

One interstitial after a finished test (practice, mock exam, review, recognition game), before its results, never
during a question (DECISIONS.md, P8), and a rewarded ad the user chooses to unlock a mock exam retake or "Reset all"
(P14). `:core:ads` holds the rules (`AdsManager`) behind three interfaces: `InterstitialAds` (the quiz calls
`showBeforeResults()` after the last answer, except on a rewarded run), `RewardedAds` (Mock exams calls `preload()`
when it opens and `showForReward()` on "Watch ad") and `AdConsent` (consent at launch, "Privacy options" in
Settings). Tests use `FakeInterstitialAds`, `FakeRewardedAds` and `FakeAdConsent` from `:core:testing`.

- **Consent first.** At each launch `MainActivity` asks Google's User Messaging Platform (UMP) for the consent
  information and shows its form where required (EEA, UK, Switzerland, some US states). Mobile Ads is initialised and
  the first ad loaded only once `canRequestAds()` is true. Settings shows **Privacy options** while UMP says the
  privacy options are `REQUIRED`.
- **Preloaded.** The interstitial is loaded in advance; if none is ready (no fill, offline, still loading) the results
  show at once and the next one loads. A loaded ad older than an hour is dropped.
- **Capped.** At most one every 3 minutes in the app (the AdMob ad unit has the same frequency cap).
- **Rewarded: no network, no reward.** `showForReward()` returns `OFFLINE` without a validated network (the retake
  doesn't start), `NOT_EARNED` when the ad is closed early, `EARNED` when watched, and `NO_ADS` (the retake starts)
  only when the user can't cause it: ads off, no rewarded unit, no consent possible, or no ad while online. It waits
  up to 8 s for an ad still loading.
- **Switch.** `ads_enabled` (Remote Config) turns ads off without a release (retakes are then free).
- **Not in instrumented tests.** Under the flow tests and the release smoke test (AndroidX Test loaded in the app's
  process) ads are off, consent request included: an interstitial would cover the screens they drive.

**IDs.** In `app/build.gradle.kts` (`admobRelease`, `admobTest`): the AdMob App ID goes in the manifest through the
`admobAppId` placeholder, the ad units in `BuildConfig.ADMOB_INTERSTITIAL_ID` and `ADMOB_REWARDED_ID` (release's
rewarded unit is blank until it's created in AdMob, FOLLOW-UP.md, section 2). Only `release` uses the real IDs;
`debug` and `qa` (emulators, smoke tests) always use Google's sample IDs, which only serve test ads. Never tap a real
ad in the app.

## Feature flags (Firebase Remote Config)

Flags are Boolean Remote Config parameters, declared in `core/flags/.../FeatureFlag.kt` with their in-app default:

| Parameter | Default | Controls |
|---|---|---|
| `photo_of_the_day` | `true` | The photo of the day on the dashboard |
| `recognition_games` | `true` | The "Can you recognise them?" hub |
| `report_question` | `true` | "Report a problem" on each question |
| `ads_enabled` | `true` | The interstitial after a finished test (off: no ad loaded or shown) |

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
- **No `init` in a ViewModel.** State is derived with `combine` + `stateIn`, or loaded in
  `_uiState.asStateFlow().onStart { }` + `stateIn`, so it starts when the screen collects it. See
  [.claude/skills/viewmodel-state](.claude/skills/viewmodel-state/SKILL.md).

## UI components and screenshots

Reusable pieces live in two modules:

- `core:designsystem`, no app knowledge: theme (colours, Barlow typography, shapes), `Spacing` and `Sizes`, `BhCard`,
  `IconBadge`, `AnswerChoice` (idle, selected, correct, wrong; lettered row or two-by-two tile), `AnswerResult`,
  `ResultChip`, `NoticeCard`, `HubCard`, `ChoiceCard`, `SettingRow` / `SettingsCard`, `LinkRow`, `ProgressRing`, `MeterBar`, `QuizProgress`, `PillToggle`,
  `PrimaryButton` / `SecondaryButton` / `LinkButton`, `SectionTitle` / `Overline`, `BuckLogo`.
- `core:ui`, built on the app's models: `ExamCard`, `ExamListItem`, `ExamAvatar`, `ReadinessRing`, `QuestionPhoto`
  (with `PhotoCaption`), `SourceLinks`, `BackTopAppBar`, `BrandTopAppBar`, `HuntIcons`, `ChoiceQuestion`, plus `PreviewData` (sample exams and questions).

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

CI verifies on every push. After an intended UI change, push the branch and run `scripts/record-screenshots.sh`: it
starts the **Record screenshots** workflow, which records the references on CI's machine (so they match what CI
renders) and uploads them, then downloads them, commits them as you and pushes. The workflow never commits itself: a
push by the Actions bot would leave the PR's CI waiting for an approval.

## Integration tests

Two suites run on a device or emulator (CI runs both on an API 35 emulator, in the `integration` job):

| Suite | Where | What |
|---|---|---|
| Flow tests | `app/src/androidTest` | The real app (Hilt, DataStore, bundled questions), driven with Compose test APIs: onboarding, practice to the results, review, leaving a mock exam, other provinces, "I passed the real exam", Settings |
| Release smoke test | `:smoketest` | The `qa` build (the release build: R8, shrinking, release code, signed with the debug key) driven from outside with UiAutomator: first launch to a question, then a restart that must open Home |

```sh
./gradlew :app:connectedDebugAndroidTest       # flow tests
./gradlew :smoketest:connectedQaAndroidTest    # release smoke test
```

Every flow test starts from a fresh install: the Android Test Orchestrator runs each test in its own process with
the app's data cleared (`clearPackageData`). They clear the app's data on the device, so with a phone plugged in,
pick the emulator with `ANDROID_SERIAL=emulator-5554`. Tests find screens by test tag and by the app's own strings,
so they follow the device language; the smoke test sets English for its app. Run them without `google-services.json`
(as CI does) so Firebase can't change flags or force an update during a test.

Add a flow test for each new flow: a class per area in `app/src/androidTest`, using `AppRobot` (`onboard()`,
`clickTag()`, `waitForText()`...).

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

- Baseline profile (`profileinstaller` is in place; add a `:baselineprofile` module with a device in CI).
