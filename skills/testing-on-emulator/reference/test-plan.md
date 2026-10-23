# What to test

Pick the flows that cover the change you are testing; run the whole smoke test before a release or when asked to
"test the app". Screens are in `app/src/main/kotlin/ca/kallistesoft/hunt/BetterHunterApp.kt` (navigation) and
`feature/*/`; if a label below no longer exists, the screen changed: find the new label in `strings.xml` and test
the screen as it is now.

## Contents
- Smoke test
- Checks for every screen
- Checks for a release build

## Smoke test

Start from `install.sh <type> --fresh`. Name screenshots `build/emulator-test/<nn>-<screen>[-fr|-dark].png`.

| # | Flow | Steps | Expect |
|---|---|---|---|
| 1 | First run | Fresh install → picker; tap Continue with nothing chosen, then pick an exam → Continue | Picker first; Continue does nothing until an exam is picked; then the dashboard with that exam under "My exams" |
| 2 | Dashboard | Read the screenshot top to bottom, scroll to "Photo of the day" | Today card (0 of 20), two hub cards, photo with credit, legal footer |
| 3 | Practice | "Practise now" → answer a question right and one wrong → Next | "Correct!" / "Incorrect", the "Why?" explanation and a source link after each answer |
| 4 | Practice to the end | Keep answering until the last question → Finish | Results: score ring, missed questions with your answer and the correct one; Home goes back to the dashboard |
| 5 | Progress kept | After 4, look at the dashboard | Daily goal counter went up; "Review my mistakes (n)" shows in Exam training |
| 6 | Mock exam | Exam training → an exam → "Mock exam" | Timer ("Time left"), no answer shown until the end, pass mark on results |
| 7 | Quit dialog | During a quiz, system back | "Quit this test?" dialog; Cancel stays, Quit leaves |
| 8 | Recognition | "What is it?" hub → start the game | Photo, four animals of the same family, fact sheet after answering |
| 9 | About | `desc=About` | Disclaimer, how it works, sources, contact, privacy, version |
| 10 | French | FR toggle on the dashboard, then visit 2, 3 and 9 | Every string in French ("PPA", never "PAL"); no English left, no clipped text |
| 11 | Dark theme | `cmd uimode night yes`, revisit 2, 3, 8 | Mockup's dark palette, readable contrast, photos not tinted |
| 12 | Process death | In a quiz: HOME, `am kill`, relaunch (see driving.md) | Reopens on the same screen, not the dashboard |
| 13 | Change exams | Dashboard "Change" → add or remove an exam → back | "My exams" updates |

Links (sources, privacy, fact sheets) open a Custom Tab: check it opens, then system back to return. Don't test
"Report a problem" past the mail chooser; it would send email.

## Checks for every screen

From the screenshot:
- Text: nothing clipped, overlapping or cut by the status/navigation bar (edge-to-edge); French strings are longer
- Colours, fonts and spacing match `design/mockup/` and `design/README.md`
- The disclaimer/footer is present where the design has it

From `screen_text.py`:
- Every tappable element has a label (text or desc): an unlabelled icon button is an accessibility bug

## Checks for a release build

Run the smoke test on `install.sh release --fresh` before any Play upload. R8 problems show up only here:
- Screens that load bundled content (questions, photos, exams JSON): kotlinx.serialization classes stripped by R8
  crash or show "No questions available."
- Progress saved in DataStore survives a relaunch (same serialization risk)
- `check_crash.sh release` after every flow; it also flags `ClassNotFoundException` and `SerializationException`
