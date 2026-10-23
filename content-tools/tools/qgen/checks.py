"""Step 4a: local, free checks on drafted questions. Sets `flags` on each question."""
from collections import Counter
from difflib import SequenceMatcher

from .paths import DRAFTS, load_json, save_json
from .schema import Question

BANNED = [
    "all of the above", "none of the above", "both a and b",
    "toutes ces réponses", "aucune de ces réponses", "toutes les réponses", "a et b",
]
DUPLICATE_RATIO = 0.85
LOCAL_FLAGS = ("format", "banned-phrase", "duplicate-of")


def problems(q: Question) -> list[str]:
    """Flags that need a reviewer's attention (`blind-ok` is a pass marker)."""
    return [f for f in q.flags if f != "blind-ok"]


def format_problems(q: Question) -> list[str]:
    found = []
    for lang in ("fr", "en"):
        b = getattr(q, lang)
        if not b.q.strip() or not b.explanation.strip():
            found.append(f"format:{lang}-empty")
        if len(b.choices) != 4:
            found.append(f"format:{lang}-{len(b.choices)}-choices")
        elif len({c.strip().lower() for c in b.choices}) != 4:
            found.append(f"format:{lang}-repeated-choice")
        text = " ".join([b.q, *b.choices]).lower()
        if any(p in text for p in BANNED):
            found.append(f"banned-phrase:{lang}")
    if not 0 <= q.answerIndex < 4:
        found.append("format:answer-index")
    return found


def run(scope: str) -> list[Question]:
    path = DRAFTS / f"{scope}.json"
    qs = [Question.model_validate(q) for q in load_json(path, [])]
    for q in qs:
        q.flags = [f for f in q.flags if not f.startswith(LOCAL_FLAGS)] + format_problems(q)

    # Near-duplicates (French text; O(n²) is fine for a few hundred questions).
    for i, a in enumerate(qs):
        for b in qs[i + 1:]:
            if SequenceMatcher(None, a.fr.q.lower(), b.fr.q.lower()).ratio() >= DUPLICATE_RATIO:
                b.flags.append(f"duplicate-of:{a.id}")

    save_json(path, qs)
    flagged = sum(1 for q in qs if problems(q))
    spread = Counter("ABCD"[q.answerIndex] for q in qs if 0 <= q.answerIndex < 4)
    print(f"  {scope}: {len(qs)} questions, {flagged} flagged, answer spread {dict(sorted(spread.items()))}")
    return qs
