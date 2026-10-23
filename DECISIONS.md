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
Daily goal 20 questions; the streak counts consecutive days with at least one answer. Photo of the day: the same photo
for everyone, chosen by the day number. Rules and tests: `core/model` (`QuizSession`, `UserProgress`).

**P6. Force update.** *Product.* A minimum build number (per platform) blocks older builds with an "Update required"
screen; the last value is cached so a blocked build stays blocked offline; never block when it was never fetched.
Android: Play immediate in-app update, else the store page. The minimum is a Firebase Remote Config Number parameter,
`min_version_code` (`min_version_code_debug` for debug builds). *Replaced:* a JSON file on hunt.kallistesoft.ca,
published from the hunt proof-of-concept repo, which is retired.

**P7. Feature flags with Firebase Remote Config.** *Product.* Boolean parameters with in-app defaults (`photo_of_the_day`,
`recognition_games`, `report_question`, all on), fetched on launch and foreground, plus real-time updates. The app
works without Firebase configured. *No Firebase Analytics.*

**P8. Ads (AdMob) are in the first release.** *Product.* The Play Console declarations (Ads, Advertising ID, AdMob
data types) were entered for that and stay; the `AD_ID` permission stays. Planned placement: one interstitial after a
finished test, never during a question. Before any release, check the store declarations against what the build really
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
quizzes: Québec, the Québec hunter course (no PAL); Alberta, the Alberta course and the PAL; elsewhere in Canada, the
PAL. Settings → My quizzes adds other provinces' quizzes or turns some off. "I passed the real exam" on a hunter
course is what turns a user into a New hunter.
ICAA (bow and crossbow) is shown as coming soon until it has questions. *App, not yet done:* the app still asks for exams in a
first-run picker, lists them firearms licence first (`ExamGroup` order) and has no province or weapon; the mockup
(design/) shows the target.

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

## Android engineering

**A1. Latest stable versions** at the time of writing (no RC or alpha): Kotlin 2.4.21, AGP 9.4.0 (built-in Kotlin),
Gradle 9.8.2, Compose BOM 2026.09.00, compile/target SDK 37, min SDK 26.

**A2. Architecture.** Modules `core/*` and `feature/*` with convention plugins (`build-logic`), MVVM with unidirectional
data flow (StateFlow UI state, stateless `…Content` composables), Hilt, Navigation 3, a single activity.

**A3. Storage: DataStore, not Room.** Progress is one small typed object (JSON); questions are read-only assets parsed
once. *Switch to Room when:* answer history is kept, or questions are downloaded and updated on the device.

**A4. No build-type branches in code.** No `BuildConfig.DEBUG`: what differs lives in `app/src/debug` and
`app/src/release` (`BuildTypeModule`), checked in CI.

**A5. Tests.** Pure logic tested on the JVM; every ViewModel tested with Turbine on fakes from `core:testing`; Roborazzi
screenshot tests verified in CI, references recorded by the "Record screenshots" workflow.
