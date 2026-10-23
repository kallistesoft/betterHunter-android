# BetterHunter design mockup

The visual reference for BetterHunter screens (Android, and iOS through the shared token values). The Android app
implements it in `core/designsystem` (theme, components) and the feature screens; see DECISIONS.md, D6.

- **Live canvas (source of truth):** https://claude.ai/artifact/NSq8SRajcbQ8UmAv5cJaer (a claude.ai Design canvas,
  private to its owner). Add new screens there, then copy the files back here.
- **Backup copy:** `mockup/`: the canvas index (`canvas.json`) and one `.dc.html` file per screen, as published.

## Screens

| Screen | Dark | Light |
|---|---|---|
| Splash screen (the native Android splash: icon on the green, nothing else; taps through to onboarding) | `Splash.dc.html` | `SplashLight.dc.html` |
| Home | `Main.dc.html` | `MainLight.dc.html` |
| What is it? (question) | `PhotoQuiz.dc.html` | `PhotoQuizLight.dc.html` |
| What is it? (answer) | `PhotoAnswer.dc.html` | `PhotoAnswerLight.dc.html` |
| Mock exam (question) | `ExamQuestion.dc.html` | `ExamQuestionLight.dc.html` |
| Onboarding: what kind of hunter? (first launch, after the splash screen) | `HunterProfile.dc.html` | `HunterProfileLight.dc.html` |
| Onboarding: what are you preparing for? (only after "Working on it") | `ExamPicker.dc.html` | `ExamPickerLight.dc.html` |

Phone frames are 390×844. Dark screens are the top row of the canvas, light screens the row below, in the order of
the flow: splash, onboarding, home, then the quizzes. Screens link to each other (click through them on the canvas).

**Onboarding (not built yet).** First launch, after the splash: "What kind of hunter are you?" (Working on it:
preparing for the licence or hunter course; New hunter: licensed, a few seasons; Experienced hunter: many seasons),
saved on the device. Only "Working on it" goes on to "What are you preparing for?", the exam picker (PAL, Québec
ICAF, Alberta; "More provinces coming"), then Home; new and experienced hunters go straight to Home. A province
question ("Where do you hunt?") can come later, when more provinces are covered.

## Logo concepts

`Logos.dc.html` (one 1280×1000 board, below the light row) compares four directions for a new logo, each as the app
icon, on dark, on light and at 48/32/20 px: A *Blaze buck* (flat buck head, orange antlers), B *Track* (deer hoof
print), C *Tine monogram* (condensed B with an antler), D *Field patch* (today's antler mark in a round crest). None is
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
