"""Step 4b: blind-answer check. A separate call answers each question from the
source alone, without seeing the stored answer; disagreement flags the question."""
from concurrent.futures import ThreadPoolExecutor
from itertools import groupby

from .llm import GenerationError, ask, document_block
from .paths import DRAFTS, load_json, load_sources, save_json
from .schema import Question, VerdictOut

SYSTEM = """You are checking practice questions for a hunter and firearm-licence exam app against the attached official manual chapter.
Answer the question using only the manual.
- chosenIndex: 0-based index of the single correct choice, or -1 if the manual supports none of them.
- ambiguous: true if more than one choice could be defended, the wording is unclear, or the question tests something the manual doesn't state.
- issue: one short sentence describing any problem (factual error, ambiguity, poor translation, trick wording); empty string if none."""

WORKERS = 4


def _check(q: Question, lang: str, file: str, pages: tuple[int, int]) -> list[str]:
    b = getattr(q, lang)
    choices = "\n".join(f"{i}. {c}" for i, c in enumerate(b.choices))
    try:
        v = ask(
            SYSTEM,
            [document_block(file, *pages), {"type": "text", "text": f"Question: {b.q}\n{choices}"}],
            VerdictOut,
        )
    except GenerationError as e:
        return [f"blind-error:{lang}:{e}"]
    flags = []
    if v.chosenIndex != q.answerIndex:
        flags.append(f"blind-mismatch:{lang}:chose-{v.chosenIndex}")
    if v.ambiguous:
        flags.append(f"blind-ambiguous:{lang}")
    if v.issue.strip():
        flags.append(f"blind-issue:{lang}:{v.issue.strip()}")
    return flags


def run(scope: str, langs: tuple[str, ...] = ("fr", "en"), recheck: bool = False) -> list[Question]:
    source = load_sources()[scope]
    chapters = {c.id: c for c in source.chapters}
    path = DRAFTS / f"{scope}.json"
    qs = [Question.model_validate(q) for q in load_json(path, [])]
    todo = [q for q in qs if recheck or not any(f.startswith("blind") for f in q.flags)]

    for chapter, group in groupby(sorted(todo, key=lambda q: q.chapter), key=lambda q: q.chapter):
        pages = chapters[chapter].pages
        jobs = [(q, lang) for q in group for lang in langs]
        # First call alone writes the chapter's cache entry; the rest read it in parallel.
        results = [_check(*jobs[0], source.file, pages)]
        with ThreadPoolExecutor(WORKERS) as pool:
            results += pool.map(lambda j: _check(*j, source.file, pages), jobs[1:])

        by_id: dict[str, list[str]] = {}
        for (q, _), flags in zip(jobs, results):
            by_id.setdefault(q.id, []).extend(flags)
        for q in qs:
            if q.id in by_id:
                q.flags = [f for f in q.flags if not f.startswith("blind")] + (by_id[q.id] or ["blind-ok"])
        save_json(path, qs)
        bad = sum(1 for flags in by_id.values() if flags)
        print(f"  {scope}/{chapter}: {len(by_id)} checked, {bad} flagged")
    return qs
