"""Offline tests: no API calls."""
from collections import Counter

import pytest

from qgen import paths, review
from qgen.checks import format_problems
from qgen.generate import _place_answer
from qgen.schema import LangBlock, Question, QuestionOut, VerdictOut, strict_schema


def make_q(qid="federal-ch01-001-q1", answer=1, **over):
    fr = LangBlock(q="Quelle est la première étape?", choices=["Pointer", "Vérifier", "Charger", "Tirer"], explanation="Parce que.")
    en = LangBlock(q="What is the first step?", choices=["Point", "Check", "Load", "Fire"], explanation="Because.")
    data = dict(id=qid, scope="federal", chapter="ch01", objectiveId="federal-ch01-001",
                fr=fr, en=en, answerIndex=answer, sourceRef="cfsc_fr.pdf p.3")
    data.update(over)
    return Question(**data)


def test_strict_schema_closes_nested_objects():
    schema = strict_schema(QuestionOut)
    lang = schema["$defs"]["LangBlock"]
    assert schema["additionalProperties"] is False and lang["additionalProperties"] is False
    assert set(lang["required"]) == {"q", "choices", "explanation"}
    assert strict_schema(VerdictOut)["required"] == ["chosenIndex", "ambiguous", "issue"]


def test_place_answer_keeps_languages_aligned_and_spreads_positions():
    positions = Counter()
    for n in range(200):
        q = make_q()
        out = _place_answer(QuestionOut(fr=q.fr, en=q.en, answerIndex=1), f"id-{n}")
        assert out.fr.choices[out.answerIndex] == "Vérifier"
        assert out.en.choices[out.answerIndex] == "Check"
        positions[out.answerIndex] += 1
    assert all(30 < positions[i] < 70 for i in range(4))


@pytest.mark.parametrize("change, flag", [
    (dict(answerIndex=4), "format:answer-index"),
    (dict(en=LangBlock(q="Q?", choices=["a", "b", "c"], explanation="x")), "format:en-3-choices"),
    (dict(fr=LangBlock(q="Q?", choices=["a", "b", "c", "Toutes ces réponses"], explanation="x")), "banned-phrase:fr"),
])
def test_format_problems(change, flag):
    assert flag in format_problems(make_q(**change))
    assert format_problems(make_q()) == []


def test_review_roundtrip_keeps_reviewer_edits(tmp_path, monkeypatch):
    for mod in (paths, review):
        monkeypatch.setattr(mod, "DRAFTS", tmp_path / "drafts", raising=False)
        monkeypatch.setattr(mod, "QUESTIONS", tmp_path / "questions", raising=False)
        monkeypatch.setattr(mod, "REVIEW_CSV", tmp_path / "review.csv", raising=False)
    paths.save_json(tmp_path / "drafts" / "federal.json", [make_q(), make_q("federal-ch01-001-q2")])

    review.export(["federal"])
    rows = review._read_csv()
    rows["federal-ch01-001-q1"].update(status="approved", fr_q="Question corrigée?")
    rows["federal-ch01-001-q2"].update(status="rejected")
    with review.REVIEW_CSV.open("w", encoding="utf-8", newline="") as f:
        import csv
        w = csv.DictWriter(f, fieldnames=review.COLUMNS)
        w.writeheader()
        w.writerows(rows.values())

    review.export(["federal"])  # re-export must not overwrite the edit
    review.bundle()
    bundled = paths.load_json(tmp_path / "questions" / "federal.json")
    assert [q["id"] for q in bundled] == ["federal-ch01-001-q1"]
    assert bundled[0]["fr"]["q"] == "Question corrigée?"
    assert bundled[0]["answerIndex"] == 1 and "flags" not in bundled[0]
