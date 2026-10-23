"""Step 3: draft bilingual multiple-choice questions for each objective."""
import datetime
import hashlib
import json

from .llm import GenerationError, ask, document_block
from .paths import CONTENT, DRAFTS, SYLLABUS, load_json, load_sources, save_json
from .schema import Objective, Question, QuestionOut, QuestionsOut

SYSTEM = """You write practice questions for a bilingual (French/English) Canadian hunter and firearm-licence exam-prep app.
Use only facts supported by the attached manual chapter.

Each question:
- Exactly 4 choices, exactly one correct. Wrong choices must be plausible to a beginner but clearly wrong per the manual.
- No "all of the above", "none of the above", "toutes ces réponses" or "aucune de ces réponses".
- No trick wording, no double negatives, no questions that depend on the current year.
- Original wording: never copy sentences or questions from the manual.
- explanation: 1-2 sentences saying why the answer is correct, in plain language.
- Write French first (Québec French, official terms), then the English version with the same meaning and the same choice order.
- Use the glossary's official term pairs.
- Vary the style across questions: direct recall, short scenario ("You are..."), and procedure order."""


def _place_answer(q: QuestionOut, qid: str) -> QuestionOut:
    """Move the correct choice to a position derived from the id, so answers are
    spread evenly across A-D whatever the model's own bias."""
    target = int(hashlib.sha1(qid.encode()).hexdigest(), 16) % 4
    for block in (q.fr, q.en):
        if len(block.choices) == 4:
            correct = block.choices.pop(q.answerIndex)
            block.choices.insert(target, correct)
    if len(q.fr.choices) == 4 and len(q.en.choices) == 4:
        q.answerIndex = target
    return q


def generate(scope: str, per_objective: int = 3) -> list[Question]:
    source = load_sources()[scope]
    chapters = {c.id: c for c in source.chapters}
    objectives = [Objective.model_validate(o) for o in load_json(SYLLABUS / f"{scope}.json", [])]
    glossary = json.dumps(load_json(CONTENT / "glossary.json", {}), ensure_ascii=False)
    path = DRAFTS / f"{scope}.json"
    drafts = [Question.model_validate(q) for q in load_json(path, [])]
    done = {q.objectiveId for q in drafts}
    year = datetime.date.today().year

    # Sorted by chapter so consecutive calls share the cached PDF prefix.
    for obj in sorted(objectives, key=lambda o: o.objectiveId):
        if obj.objectiveId in done:
            continue
        ch = chapters[obj.chapter]
        try:
            out = ask(
                SYSTEM,
                [
                    document_block(source.file, *ch.pages),
                    {"type": "text", "text": (
                        f"Glossary (fr ↔ en): {glossary}\n\n"
                        f"Chapter: {ch.title}\nObjective: {obj.statement}\nSource: {obj.sourceRef}\n\n"
                        f"Write {per_objective} different questions testing this objective."
                    )},
                ],
                QuestionsOut,
            )
        except GenerationError as e:
            print(f"  {obj.objectiveId}: {e}")
            continue
        for n, q in enumerate(out.questions, 1):
            qid = f"{obj.objectiveId}-q{n}"
            q = _place_answer(q, qid)
            drafts.append(Question(
                id=qid,
                scope=scope,
                chapter=obj.chapter,
                objectiveId=obj.objectiveId,
                reviewYear=year if obj.volatile else None,
                fr=q.fr,
                en=q.en,
                answerIndex=q.answerIndex,
                sourceRef=obj.sourceRef,
            ))
        save_json(path, drafts)
        print(f"  {obj.objectiveId}: {len(out.questions)} questions")
    return drafts
