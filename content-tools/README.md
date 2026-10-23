# Content tools

How BetterHunter's questions and "What is it?" photos are made. The results go straight into the app's bundled
content, `core/data/src/main/assets/` (see the main README, "Content"); this folder holds what's behind them. It came
from the hunt proof of concept, which is retired.

**Rule:** questions are written in original wording from the manuals' facts. Never copy official exam questions or
manual text.

## Layout

```
content/
  sources.json      source manual + chapter page ranges per scope
  glossary.json     official FR ↔ EN term pairs
  syllabus/         generated learning objectives per scope
  drafts/           generated questions, before review
  review.csv        human review sheet (source of truth once reviewed)
  wildlife-id/      species cards (species.json) and original photos for "What is it?"
sources/            official PDFs (copyrighted, gitignored)
tools/qgen/         question generation pipeline (Python + Claude API)
tools/wildlife_id.py  photo ID bank builder
tools/tests/        pipeline tests (offline)
```

The exam recipes (`exams.json`) live only in the app, in `core/data/src/main/assets/content/`.

## Question scopes

Each question has one scope: `federal` (PAL, the same everywhere in Canada), `hunter-common` (shared by all provincial
hunter-ed courses), or a province code (`QC`, `AB`, …). An exam mixes scopes, e.g. `ICAF = hunter-common + QC`.
Questions about yearly regulations carry a `reviewYear` and are re-checked each season. There is no RPAL scope
(DECISIONS.md, P9).

## Question pipeline

Setup (Python 3.10+), from `content-tools/`:

```sh
python3 -m venv .venv && .venv/bin/pip install -r tools/requirements.txt
cd tools
```

Claude API credentials come from `ANTHROPIC_API_KEY` or an `ant auth login` profile.

1. Put the manual in `sources/` and fill in its chapters in `content/sources.json`.
2. `../.venv/bin/python -m qgen syllabus federal`: extract the testable facts, one call per chapter.
3. `../.venv/bin/python -m qgen generate federal`: 3 bilingual questions per fact. Correct answers are spread evenly
   across A–D.
4. `../.venv/bin/python -m qgen check federal`: free local checks (format, banned phrasings, near-duplicates).
5. `../.venv/bin/python -m qgen verify federal`: blind-answer check. Claude answers each question from the manual
   without seeing the stored answer, and any disagreement is flagged.
6. `../.venv/bin/python -m qgen export federal`: writes `content/review.csv`, flagged questions first. Set `status` to
   `approved` / `fix` / `rejected` and edit text directly in the sheet.
7. `../.venv/bin/python -m qgen bundle`: only `approved` rows are written, one bank per scope, to the app's
   `core/data/src/main/assets/content/questions/`.

`python -m qgen all federal` runs steps 2–6. Every step is resumable: work already done is skipped. A new scope also
needs its exam recipe in the app's `exams.json`.

Each chapter's PDF is sent as a prompt-cached block, so repeated calls on the same chapter are cheap.

Tests (offline, no API calls): `../.venv/bin/python -m pytest tests`

## Photo ID quiz ("What is it?")

`content/wildlife-id/species.json` holds one card per species (names, 3 same-family look-alikes, how to recognise it,
where it lives, protected look-alike warning, fact-sheet link). Photos in `content/wildlife-id/photos/` come from
Wikimedia Commons under CC0 / public domain / CC BY / CC BY-SA; the app shows each photo's author and licence.
`tools/wildlife_id.py` fetches photos (`fetch`, `candidates`, `preview`, `use`) and builds the bank (`build`), which
writes `wildlife-id.json` and the photos into the app's assets. On macOS it compresses the photos with `sips`
(640 px); elsewhere it keeps the app's existing compressed copies and copies only new ones.

After a bundle or build, run the app's tests (`./gradlew :core:data:testDebugUnitTest` runs `BundledContentTest`) and
open a pull request.
