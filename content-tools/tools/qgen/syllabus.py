"""Step 2: split each source chapter into testable learning objectives."""
from pathlib import Path

from .llm import ask, document_block
from .paths import SYLLABUS, load_json, load_sources, save_json
from .schema import Objective, SyllabusOut

SYSTEM = """You help build an exam-preparation app for Canadian hunters and firearm licence candidates.
From the attached chapter of an official study manual, list the facts a candidate must know for the exam.

Rules:
- One objective = one testable fact, rule or procedure step, stated in your own words. Never copy sentences.
- Write each statement in the manual's language.
- Cover the whole chapter; prefer safety-critical and frequently examined content.
- Skip anecdotes, history and content that cannot be asked as a multiple-choice question.
- Set volatile=true when the fact depends on yearly regulations (dates, fees, bag limits, zones).
- page = the page number printed in the PDF slice where the fact appears (slice page 1 = first attached page)."""


def build(scope: str, target: int = 15) -> list[Objective]:
    source = load_sources()[scope]
    path = SYLLABUS / f"{scope}.json"
    existing = [Objective.model_validate(o) for o in load_json(path, [])]
    done = {o.chapter for o in existing}

    for ch in source.chapters:
        if ch.id in done:
            print(f"  {scope}/{ch.id}: already in syllabus, skipped")
            continue
        out = ask(
            SYSTEM,
            [
                document_block(source.file, *ch.pages),
                {"type": "text", "text": f"Chapter: {ch.title}\nList about {target} objectives."},
            ],
            SyllabusOut,
        )
        for n, o in enumerate(out.objectives, 1):
            existing.append(Objective(
                objectiveId=f"{scope}-{ch.id}-{n:03d}",
                scope=scope,
                chapter=ch.id,
                statement=o.statement,
                sourceRef=f"{Path(source.file).name} p.{ch.pages[0] + o.page - 1}",
                volatile=o.volatile,
            ))
        save_json(path, existing)
        print(f"  {scope}/{ch.id}: {len(out.objectives)} objectives")
    return existing
