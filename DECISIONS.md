# Decisions

What was decided for BetterHunter, why, and what was rejected, so later work (on Android, and the iOS app to come)
doesn't undo it or reopen it by accident. One entry per decision; add a line when something is decided, and mark an
entry *Replaced* rather than deleting it.

**Platform tags:** *Product* applies to every platform (the iOS app must behave the same); *Android* is this app's
implementation choice (iOS picks its own equivalent).

## Product

**P1. Native apps, Android first, then the same app on iOS.** *Product.* The Flutter prototype (hunt repo) proved the
concept; the store apps are native (Kotlin/Compose here, Swift/SwiftUI on iOS). The app's questions live in this repo
(P4). *Why:* best platform fit and tooling. *Rejected:* shipping the Flutter app.

**P2. Bilingual FR/EN.** *Product.* French for any `fr` locale, English otherwise; the FR/EN toggle sets a per-app
language (shown in system settings), not an app preference. Questions carry both languages.

**P3. No user accounts.** *Product.* Purchases are tied to the store account (Play or App Store restore them);
progress stays on the device and follows the user through the OS backup. *Why:* no sign-in friction, no personal data,
no account-deletion obligations. *Revisit only for:* live sync across devices, purchases shared across platforms,
sharing with an instructor or group.

**P4. Questions ship inside the app, and this repo owns them.** *Product.* The exam recipes, question banks and photos
live in this repo (`core/data/src/main/assets/`) and are edited and reviewed here, by pull request; the app takes no
content from another repo. The tools behind them (question pipeline, review sheet, species cards and photos) are in
`content-tools/` and write straight into the app's assets. *Replaced:* copying approved questions from the hunt repo
(a proof of concept, retired) with a sync script.
*Next step, not started:* a static API on the GoDaddy site (versioned content packs + manifest, synced and cached on
the device), see FOLLOW-UP.md section 4. *Rejected for now:* a database and backend (GoDaddy PHP/MySQL or Firestore):
only worth it for an admin page, accounts or server-side features.

**P5. Quiz rules.** *Product.* Practice and review: 20 questions, answer final, explanation at once, recorded right
away. Mock exam: the exam's question count, timer and pass mark from `exams.json`; answers can change and are recorded
at the end; time up ends it. Review uses only the user's mistakes; a mistake clears after 2 correct answers in a row.
Daily goal 20 questions by default (10, 20 or 30 in Settings); the streak counts consecutive days with at least one answer. Photo of the day: the same photo
for everyone, chosen by the day number. Rules and tests: `core/model` (`QuizSession`, `UserProgress`).

**P6. Force update.** *Product.* A minimum build number (per platform) blocks older builds with an "Update required"
screen; the last value is cached so a blocked build stays blocked offline; never block when it was never fetched.
Android: Play immediate in-app update, else the store page. The minimum is a Firebase Remote Config Number parameter,
`min_version_code` (`min_version_code_debug` for debug builds). *Replaced:* a JSON file on hunt.kallistesoft.ca,
published from the hunt proof-of-concept repo, which is retired.

**P7. Feature flags with Firebase Remote Config.** *Product.* Boolean parameters with in-app defaults (`photo_of_the_day`,
`recognition_games`, `report_question`, `ads_enabled`, all on), fetched on launch and foreground, plus real-time updates. The app
works without Firebase configured. *No Firebase Analytics.*

**P8. Ads (AdMob) are in the first release.** *Product.* The Play Console declarations (Ads, Advertising ID, AdMob
data types) were entered for that and stay; the `AD_ID` permission stays. Placement: one interstitial after a
finished test, never during a question. *In the app (2026-10-23):* after the last answer of an exam-training quiz
(practice, mock exam, review, recognition game), the preloaded interstitial shows, then the results; none loaded means
the results at once. At most one every 3 minutes (in the app and as the AdMob frequency cap). Hunters' field
runs (P11: daily questions, skills, season check) show no ad for now. Consent with Google's UMP at launch, "Privacy
options" in Settings where required; nothing is loaded before consent allows it. `ads_enabled` (Remote Config) turns
ads off. Real ad IDs only in release builds; debug and qa (emulator smoke tests) use Google's test IDs. Before any release, check the store declarations against what the build really
does (FOLLOW-UP.md, section 3). Monetization beyond that (rewarded ads, Pro unlock, two-part app) is only explored, in
IDEAS.md.

**P9. Privacy.** *Product.* Everything stays on the device; the app contacts only the update-rules file, Firebase
Remote Config (installation ID) and, with ads, AdMob. No location, no analytics, no crash reporting yet. "Report a
problem" opens the user's mail app.

**P10. Unofficial study aid.** *Product.* The disclaimer (not affiliated with the RCMP, the Canadian Firearms Program,
Québec, FédéCP…) is always reachable (dashboard footer, About), and every explanation links its source (manual page,
official rule or species fact sheet).

## Identity and stores

**S1. Android application ID `ca.kallistesoft.hunt`** (debug: `.debug`). *Why:* it's the app already created in Play
Console, and a package name can't change. *Replaced:* `ca.kallistesoft.betterhunter` (tried, reverted the same day).
iOS chooses its bundle ID when its App Store Connect app is created.

**S2. Release signing.** Play App Signing; the developer keeps only an upload key (keystore.properties locally, CI
secrets). CI builds an unsigned release without the secrets. `versionCode` = CI run number.

**P9. No RPAL (CRFSC) exam.** *Product.* The app prepares for hunting: the PAL (non-restricted firearms) and provincial
hunter education. The RPAL covers restricted firearms (mainly handguns), which hunters don't need, so its exam and
question bank are left out.

**P10. The hunter course makes the hunter; the PAL is optional.** *Product.* To hunt you need your province's hunter
education: in Québec, ICAF to hunt with firearms (without a PAL you hunt near a PAL holder) or ICAA for bow and
crossbow; in Alberta, Conservation and Hunter Education. The PAL (CFSC) is the federal licence to own or buy
firearms, not a requirement to be a hunter. Onboarding asks where the user hunts and the province sets the
quizzes: Québec, the Québec hunter course and the PAL; Alberta, the Alberta course and the PAL; elsewhere in Canada,
the PAL (Québec added the PAL on 2026-10-24: many Québec hunters also want their firearms licence). The question itself names no exam ("Québec hunting rules and game", 2026-10-24); the quizzes show in Exam
training and My provinces. Settings → My provinces switches other provinces on (adding their quizzes) or off. "I passed the real exam" on a hunter
course is what turns a user into a Hunter (P12).
ICAA (bow and crossbow) is shown as coming soon until it has questions. *In the app (2026-10-23):* onboarding asks the three questions (no exam picker), the profile and the
provinces are in `UserProgress` (`myExamIds`: home province first), hunter courses come before the PAL (`ExamGroup`
order), My provinces is reached from Exam training, and "Yes, I passed" makes a Hunter with Home's hunter
version. Settings changes each answer (hunter type, weapons, where you hunt), My provinces and the daily goal.

**P11. Hunters practise field skills, not exams.** *Product.* A Hunter (P12) is done with exams: their Home and
Field skills never show a course, a pass mark or a mock exam. To keep them up to date, Home asks 6 questions a day: 3
random species questions and 3 random general questions. Skills are practised by topic; a skill at 100% is
mastered, right answers count for 3 months (then the question counts as unanswered again), each mix keeps 1 or 2
questions from mastered skills, "Keep your edge" replaces the mix once every skill is mastered, and Home offers a season
check before the season. The app never states legal season dates (zone, weapon, year): the hunter enters their own
first hunting day and Home counts down to it; without a date, the season check is offered from late August.
Reminders are local notifications, opt-in at two moments (daily questions done, season date saved), at most one a day,
never between 9 pm and 8 am, at the hunter's usual playing time. *In the app (2026-10-23):* `FieldSkill` maps
each skill to chapters of the banks (a first pass, to check against the questions; safe firearm handling only for
firearm hunters), `FieldTraining` draws the runs, `SeasonStatus` and `Reminder` hold the season and reminder rules
(core:model, tested on the JVM). Every Hunter gets this Home; one who passed the exam in the app also keeps Exam
training, as a row below the tiles (2026-10-24). Reminders are one WorkManager job, rescheduled whenever the progress changes and
when the time zone or the time changes or the phone restarts; a late one still shows that day, before 9 pm (#8). The
usual time is the median time of the first answer on the last 14 days played (7 pm until 5 days). "Not now" asks again a
week later; a refused system dialog stops asking. After 3 daily reminders ignored in a row they come every other day,
until the app is opened. Without a date for the coming season, one "Hunting season is coming" on August 25. A season
reminder opens the season check.

**P12. Two profiles: Working on it and Hunter.** *Product.* "What kind of hunter are you?" has two answers: Working on
it (preparing for the licence or the hunter course) and Hunter (licensed, whatever the number of seasons). A
first-season hunter and a thirty-season one can do the same things, so the app doesn't split them. "Yes, I passed"
makes a Hunter, on the same Home as one from onboarding (P11), with a welcome banner and Exam training kept as a row.
For hunters who already know the basics, the first Home will offer an optional skill check ("Already hunt? Skip what
you know": the skills answered right start as mastered), and today's species questions will include look-alikes
(white-tailed or mule deer, sex and age); both are in the mockup, not built yet (FOLLOW-UP.md, section 5).
*Replaced (2026-10-24):* three profiles, New hunter and Experienced hunter, mocked as two flows. *In the app:*
`HunterType` is `WORKING_ON_IT` or `HUNTER`; progress saved as `NEW_HUNTER` or `EXPERIENCED` reads back as `HUNTER`
(`@JsonNames`), so nothing is lost on update.

**P13. No provincial course names in the app.** *Product.* To stay clear of any issue with governments and course
publishers, the app never names a provincial hunter course or its publisher (ICAF, ICAA, Alberta Conservation and
Hunter Education, FédéCP…). A Working-on-it user sees the PAL (PPA in French), which is named, and a "Hunting exam"
whose questions come from their province's bank ("Hunting exam · Québec", "Hunting exam · Alberta"; the province name
stays). "I passed the real exam" asks "Did you pass your hunting exam?". The disclaimer names no one ("not affiliated
with … any government, police force or hunting organization"), in the app and on the Play listing. *Exception:* source
citations keep the manual's title and link ("ICAF manual (FédéCP, 2020), p. 61", About → Sources). *In the app
(2026-10-24):* exam ids (`ICAF`, `ABHE`) are internal only; `exams.json` gives each exam a `name` and a `short` name;
`BundledContentTest` and `core/ui` `NoCourseNamesTest` fail on any course or publisher name in strings, exam names or
question text.

## Design

**D1. Material 3 with a fixed brand palette.** *Replaced by D6.* Colour scheme generated (tonal spot) from the icon's
green `#2F5D3A`, light and dark; *no dynamic colour*, so the app always matches its icon and store listing. Default M3
type scale. *Declined for now:* Material Symbols instead of material-icons-extended, a custom typography, M3 Expressive.

**D2. Design tokens only.** Sizes come from `Spacing` and `Sizes`, corners from the theme's shapes; no literal sizes in
screens (checked in CI). iOS should reuse the same token values.

**D3. Shared components.** Reusable pieces live in the design system (`AnswerChoice`, `IconBadge`, `HubCard`,
`NoticeCard`, `BottomActionBar`…) and `core:ui`; screens don't redraw their own versions.

**D4. Edge to edge.** Every screen sits in a Scaffold; lists scroll under the transparent navigation bar; bottom
actions sit in an opaque bar.

**D5. Visual references.** Every component and screen has previews in two variants (light in English, dark in French)
and a screenshot test; the reference images (`*/src/test/screenshots`) are the visual target, iOS included.

**D6. The "field guide" look of the design mockup.** *Product.* The mockup in `design/` (claude.ai Design canvas)
sets the look: dark forest greens / warm cream in light mode, blaze orange only for the one main action of a screen and
the current selection, green for progress, links and right answers; Barlow Condensed headings over Barlow text
(bundled, SIL OFL); 22 dp cards, 16 dp answers, 14 dp buttons, 44 dp touch targets. Still Material 3 underneath, with
the mockup's colours mapped onto its roles (primary = orange, secondary = green; see `Color.kt`), and still no dynamic
colour. "What is it?" answers show the name in both languages; an answered photo shows the animal's card instead of the
choices. iOS uses the same token values.

**D7. No Compose Styles API.** *Android.* Components keep their parameters and Material 3 theming; they aren't moved to
`Style` (`.claude/skills/styles`). *Why:* the API is still `@ExperimentalFoundationStyleApi`; most design-system components wrap
Material 3 components, which Styles doesn't cover (`Buttons`, `Cards`, `LinkRow`, `AnswerChoice`, `ChoiceCard`,
`PillToggle`, `IconBadge`, `ProgressRing`); and the look is fixed (D6), so there's little for it to add. The
prerequisites are met (foundation 1.12.1, compile SDK 37). *Revisit when:* the API is stable or Material 3 supports it.
A pilot would add the opt-in to the `betterhunter.android.compose` convention plugin, a `ComponentStyles.kt`, and move
`NoticeCard` alone, its Roborazzi screenshot checking that nothing changed.

## Android engineering

**A1. Latest stable versions** at the time of writing (no RC or alpha): Kotlin 2.4.21, AGP 9.4.0 (built-in Kotlin),
Gradle 9.8.2, Compose BOM 2026.09.00, compile/target SDK 37, min SDK 26.

**A2. Architecture.** Modules `core/*` and `feature/*` with convention plugins (`build-logic`), MVVM with unidirectional
data flow (StateFlow UI state, stateless `…Content` composables), Hilt, Navigation 3, a single activity.

**A2b. ViewModel state starts when collected.** No `init` in a ViewModel: `uiState` is derived with `combine` +
`stateIn(WhileSubscribed(5_000))`, or a one-time setup runs in `_uiState.asStateFlow().onStart { }` guarded by the
loading state. *Why:* nothing runs for a ViewModel no screen shows, no race with the first collection, state always
follows the repositories. Checked in CI (`check-conventions.sh`); how-to in `.claude/skills/viewmodel-state`.

**A3. Storage: DataStore, not Room.** Progress is one small typed object (JSON); questions are read-only assets parsed
once. *Switch to Room when:* answer history is kept, or questions are downloaded and updated on the device.

**A4. No build-type branches in code.** No `BuildConfig.DEBUG`: what differs lives in `app/src/debug` and
`app/src/release` (`BuildTypeModule`), checked in CI.

**A5. Tests.** Pure logic tested on the JVM; every ViewModel tested with Turbine on fakes from `core:testing`; Roborazzi
screenshot tests verified in CI, references recorded by the "Record screenshots" workflow. Integration tests on an
emulator in CI: flow tests on the real app (no fakes; the Test Orchestrator clears the app's data before each test)
and a UiAutomator smoke test of the release build (`qa` build type: release, debug-signed), so R8 problems show up
before a Play upload. *Rejected:* Hilt test modules replacing storage or Firebase in flow tests (a fresh process and
no `google-services.json` give the same isolation with the real wiring).
