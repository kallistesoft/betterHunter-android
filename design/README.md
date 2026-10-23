# BetterHunter design mockup

The visual reference for BetterHunter screens (Android, and iOS through the shared token values). The Android app
implements it in `core/designsystem` (theme, components) and the feature screens; see DECISIONS.md, D6.

- **Live canvas (source of truth):** https://claude.ai/artifact/NSq8SRajcbQ8UmAv5cJaer (a claude.ai Design canvas,
  private to its owner). Add new screens there, then copy the files back here.
- **Backup copy:** `mockup/`: the canvas index (`canvas.json`) and one `.dc.html` file per screen, as published.

## Screens

The whole app, in the order of the flow (left to right on the canvas; dark row on top, light row below). "Built" means
the screen exists in the app; "restyle" means its layout in the app still differs from the mockup.

| Screen | Dark | Light | In the app |
|---|---|---|---|
| Splash screen: the native Android splash, icon on the green | `Splash.dc.html` | `SplashLight.dc.html` | built |
| Onboarding: what kind of hunter? (first launch) | `HunterProfile.dc.html` | `HunterProfileLight.dc.html` | built |
| Onboarding: what do you hunt with? (firearm, bow, crossbow) | `HuntWith.dc.html` | `HuntWithLight.dc.html` | built |
| Onboarding: where do you hunt? (Québec, Alberta, elsewhere in Canada) | `HuntWhere.dc.html` | `HuntWhereLight.dc.html` | built |
| Home | `Main.dc.html` | `MainLight.dc.html` | built |
| Settings: hunter type, hunting with, where you hunt, my provinces, language, daily goal; About inside | `Settings.dc.html` | `SettingsLight.dc.html` | built (each profile row reopens its onboarding question with Save) |
| My provinces: one switch per province (Québec; Alberta; elsewhere in Canada, the PAL) adds or hides its quizzes | `Provinces.dc.html` | `ProvincesLight.dc.html` | built (from Settings and Exam training) |
| About | `About.dc.html` | `AboutLight.dc.html` | built |
| Exam training | `ExamTraining.dc.html` | `ExamTrainingLight.dc.html` | built |
| Practice: question answered (text question, wrong answer) | `PracticeQuestion.dc.html` | `PracticeQuestionLight.dc.html` | built |
| Practice: results | `PracticeResults.dc.html` | `PracticeResultsLight.dc.html` | built |
| Review my mistakes: nothing to review | `NothingToReview.dc.html` | `NothingToReviewLight.dc.html` | built |
| Mock exam: question | `ExamQuestion.dc.html` | `ExamQuestionLight.dc.html` | built |
| Mock exam: quit this test? (dialog) | `QuitDialog.dc.html` | `QuitDialogLight.dc.html` | built |
| Mock exam: results | `ExamResults.dc.html` | `ExamResultsLight.dc.html` | built |
| I passed the real exam: confirm (from Exam training) | `PassedExam.dc.html` | `PassedExamLight.dc.html` | built |
| Home, new hunter (after passing the real exam) | `MainNewHunter.dc.html` | `MainNewHunterLight.dc.html` | built |
| Onboarding as a new hunter: what kind of hunter? → New hunter, then the same two questions | `HunterProfileNewHunter.dc.html`, `HuntWithNewHunter.dc.html`, `HuntWhereNewHunter.dc.html` | `…Light.dc.html` | built, except the new-hunter wording of "Where do you hunt?" |
| Home, new hunter (from onboarding): today's 3 species + 3 general questions, Field skills instead of Exam training | `HomeNewHunter.dc.html` | `HomeNewHunterLight.dc.html` | no |
| Today: a general question (3 a day) | `DailyQuestion.dc.html` | `DailyQuestionLight.dc.html` | no |
| Field skills: today's mix, readiness per skill | `FieldSkills.dc.html` | `FieldSkillsLight.dc.html` | no |
| Field skills: question (one skill, 10 questions) | `SkillQuestion.dc.html` | `SkillQuestionLight.dc.html` | no |
| Field skills: results (the skill's progress) | `SkillResults.dc.html` | `SkillResultsLight.dc.html` | no |
| Field skills: skill mastered (results at 100%) | `SkillMastered.dc.html` | `SkillMasteredLight.dc.html` | no |
| Field skills: mastered skills, one faded | `FieldSkillsMastered.dc.html` | `FieldSkillsMasteredLight.dc.html` | no |
| Field skills: all mastered, "Keep your edge" | `FieldSkillsAllMastered.dc.html` | `FieldSkillsAllMasteredLight.dc.html` | no |
| Home, new hunter, season coming (no date set: from late August) | `HomeSeasonGeneric.dc.html` | `HomeSeasonGenericLight.dc.html` | no |
| When does your season start? (the hunter's own date) | `SeasonDate.dc.html` | `SeasonDateLight.dc.html` | no |
| Home, new hunter, 3 weeks before the hunter's date | `HomeSeasonCheck.dc.html` | `HomeSeasonCheckLight.dc.html` | no |
| Season check: question (15, all skills) | `SeasonCheckQuestion.dc.html` | `SeasonCheckQuestionLight.dc.html` | no |
| Can you recognise them? (recognition games) | `Recognition.dc.html` | `RecognitionLight.dc.html` | built, restyle; "Tracks" and "Calls" are ideas |
| What is it?: question | `PhotoQuiz.dc.html` | `PhotoQuizLight.dc.html` | built |
| What is it?: photo full screen | `PhotoZoom.dc.html` | `PhotoZoomLight.dc.html` | built |
| What is it?: answer | `PhotoAnswer.dc.html` | `PhotoAnswerLight.dc.html` | built |
| Update required (force update) | `UpdateRequired.dc.html` | `UpdateRequiredLight.dc.html` | built, restyle |

Phone frames are 390×844. Screens link to each other: click through them on the canvas.

The canvas has one **page per flow** (dark row on top, light row below, screens numbered in the order of the flow):

| Page | Screens |
|---|---|
| 1 · Working on it: exam prep (opens first) | 1 Splash → 2 What kind of hunter? → 3 What do you hunt with? → 4 Where do you hunt? → 5 Home → 6 Exam training → 7 Practice question → 8 Practice results (8b nothing to review) → 9 Mock exam question (9b quit) → 10 Mock exam results → 11 I passed the real exam → 12 Home, new hunter |
| 2 · New hunter: first seasons | 1 What kind of hunter? → New hunter → 2 What do you hunt with? → 3 Where do you hunt? → 4 Home, new hunter (4b today's general question) → 5 Field skills → 6 Skill question → 7 Skill results → 8 Skill mastered → 9 Mastered and faded skills → 10 All mastered → 11 Home, season coming (no date) → 11b Season start date → 11c Home, your season in 3 weeks → 12 Season check question |
| 3 · What is it? | 1 Recognition hub → 2 Photo question → 3 Photo full screen → 4 Answer |
| 4 · Settings & About | 1 Settings → 2 My provinces → 3 About |
| 5 · System | 1 Update required |
| 6 · Logo | Logo concepts, buck variants |

The screens added for the full app are generated: `generator/screens.py` (shared themes, header, cards, buttons and
icons; `python3 generator/screens.py <out_dir>` writes both themes), `generator/buck.py` and `generator/logos.py` (the
logo boards). Edit the generator, regenerate, then publish the changed files to the canvas and copy them here.

**Onboarding (built).** First launch, after the splash: "What kind of hunter are you?" (Working on it,
New hunter, Experienced hunter), "What do you hunt with?" (firearm, bow, crossbow; pick all that apply) and "Where do
you hunt?" (Québec, Alberta, elsewhere in Canada), saved on the device. The province sets the quizzes of a "Working on
it" user: Québec, the Québec hunter course (ICAF; ICAA for bow and crossbow once it has questions); Alberta, the Alberta
course and the PAL; elsewhere, the PAL. Settings → My provinces switches other provinces on (adding their quizzes) or off. There is
no exam list in onboarding.

**Passed the real exam (built).** On an exam card in Exam training, "I passed the real exam" opens a
confirmation on the hunter course ("Did you pass your hunter course (ICAF)?", what changes, "Yes, I passed" / "Not yet"). Confirming makes the user a
New hunter (changeable back in Settings) and lands on Home's new-hunter version: a congratulations banner, the daily
goal played on "What is it?", and Exam training kept as a tile ("ICAF passed") for refreshers. The example user is a Québec hunter preparing ICAF
(main exam) and the PAL (optional). The app takes the user's
word; nothing is verified.

**New hunter: first seasons (mockup, not built).** A user who picks "New hunter" at onboarding is done with exams:
nothing in the flow mentions a course, a pass mark or a mock exam. The goal is to keep them up to date, so Home leads
with **today's 6 questions**: 3 random species questions (a photo to name, as in "What is it?") and 3 random general
questions (rules, safety, shot placement, tracking), each with its own progress and one "Continue" button; new
questions every day, and they keep the streak. Below them, Home swaps the Exam training tile for **Field skills**, which practises hunting know-how one skill at a time: shot placement, after the
shot (tracking, recovery, tagging), safe firearm handling, rules in the field (hours, tags, transport), survival and
first aid. "Today's mix" draws 10 questions from the weakest skills; a skill's results show its readiness before and
after. The questions can come from the hunter-course banks (grouped by chapter), still citing their source. The skill
list and its grouping of chapters are placeholders to check. Flow 1's Home as a new hunter (after passing the real
exam) still shows Exam training, as built.

**Mastery (mockup, not built).** A skill is **mastered** at 100%: its results celebrate it and its row shows a check
instead of a percentage. Right answers count for **3 months**: after that a question counts as unanswered again, so
mastered skills fade back before the next season ("Was mastered · 2 answers expired"). Each mix keeps 1 or 2
questions from mastered skills; a miss takes the skill below 100% until it is won back. With every skill mastered, the
mix becomes **Keep your edge** (10 questions across all skills, oldest answers first). Before the season, Home offers a
**season check** (15 questions across all skills). The app doesn't know legal season dates (they depend on the zone,
the weapon and the year), so the **hunter sets their own date**: "When does your season start?" (from the Home card or
Settings; it says to check the official regulations), and Home counts down to it ("Your season starts in 3 weeks ·
Nov 14, the date you set"). Without a date, Home shows "Hunting season is coming" from late August, with "Set my season
start date". The date is asked again each year.

## Logo concepts

`Logos.dc.html` (one 1280×1000 board, below the light row) compares four directions for a new logo, each as the app
icon, on dark, on light and at 48/32/20 px: A *Blaze buck* (flat buck head, orange antlers), B *Track* (deer hoof
print), C *Tine monogram* (condensed B with an antler), D *Field patch* (today's antler mark in a round crest).
**Concept A, Blaze buck, is chosen**: it is the app icon (`app/src/main/res/drawable/ic_launcher_foreground.xml`,
with a monochrome version for themed icons), the splash icon and the dashboard header (`BuckLogo` in
`core/designsystem`), drawn from the same 64-unit shapes. The app icon background is `#1F4229`. Exports in `logo/`: `buck.svg` (the mark), `app-icon.svg`
(adaptive icon layout) and `play-store-icon.svg` / `play-store-icon-512.png` (the Play Console's 512 px icon).

`BuckVariants.dc.html` (beside it) tries three signature details to make the buck more distinctive: *Blaze
patch* (orange triangle on the forehead), *Uneven rack* (3 points one side, 4 the other) and *First light* (orange sun
between the antlers), plus *Blaze + uneven rack*, the mix of the first two (highlighted). Not chosen yet.

## Photos

The screens reference photos uploaded to the canvas (`/_blob/<id>` URLs), the same Wikimedia Commons photos the app bundles in `core/data/src/main/assets/wildlife/` (and the former app
icon's deer):

| Blob id | Photo |
|---|---|
| `876ea656307e18bff50cd003ae123e81` | `white-tailed-deer.jpg` |
| `1d7af3f152d0eb25bf4d048cbff96b7f` | the former app icon's deer (no longer used: the screens draw the buck logo inline) |
| `38c860d8b4e20555914ef4c9812cbd55` | `mule-deer.jpg` |
| `ed3939a5023b68655218c7345bb481dd` | `moose.jpg` |

To restore the canvas from this copy, re-upload these photos and replace the ids.

## Look

Fonts: **Barlow Condensed** 600/700 for headings, **Barlow** 400/500/600 for body text.
Corners: 22 px large cards, 16–20 px tiles and choices, 14 px buttons. Touch targets at least 44 px.

| Role | Dark | Light |
|---|---|---|
| Splash background (the icon's green) | `#1F4229` | `#2D5B38` |
| Background | `#0E1611` | `#F5F2EA` |
| Card | `#17221A` | `#FFFFFF` |
| Card border / divider | `#24352A` | `#E6E1D5` |
| Track (empty progress) | `#24352A` | `#E2DCCD` |
| Text | `#EEF1EA` | `#17201A` |
| Secondary text | `#A9B5AB` | `#5B6659` |
| Main action (blaze orange) | `#F27B2B`, text `#1A0E05` | `#E8691E`, text `#1A0E05` |
| Orange label text | `#F7A06A` | `#B4500F` |
| Selected answer | bg `#2A1C11`, border orange | bg `#FDEBDD`, border orange |
| "Don't confuse it" card | bg `#2A1C11`, border `#4A2F1B` | bg `#FDEBDD`, border `#F3C9A6` |
| Green (correct, progress, links) | `#8FD19E` | `#2F6B3E` (bars `#3F8A52`) |
| Green tint (icon tiles, chips) | `#22382A` | `#E3EFE4` |

Orange is for the one main action per screen and the current selection; green is for progress and right answers.
Text over photos sits on a dark fade so it stays readable in both modes.
