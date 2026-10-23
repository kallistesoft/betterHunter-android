"""Step 5: human review through content/review.csv, then bundle approved questions.

review.csv is the source of truth once a row is reviewed: edits made in the
spreadsheet (wording, choices, answer letter) are what gets bundled.
status: pending | approved | fix | rejected
"""
import csv

from .checks import format_problems, problems
from .paths import DRAFTS, QUESTIONS, REVIEW_CSV, load_json, save_json
from .schema import LangBlock, Question

LETTERS = "ABCD"
COLUMNS = [
    "status", "notes", "id", "scope", "chapter", "flags", "answer",
    "fr_q", *(f"fr_{l}" for l in LETTERS), "fr_explanation",
    "en_q", *(f"en_{l}" for l in LETTERS), "en_explanation",
    "sourceRef", "sourceUrl", "reviewYear", "objectiveId",
]
REVIEWED = {"approved", "fix", "rejected"}


def _read_csv() -> dict[str, dict]:
    if not REVIEW_CSV.exists():
        return {}
    with REVIEW_CSV.open(encoding="utf-8", newline="") as f:
        return {row["id"]: row for row in csv.DictReader(f)}


def _to_row(q: Question) -> dict:
    row = {
        "status": "pending", "notes": "", "id": q.id, "scope": q.scope, "chapter": q.chapter,
        "flags": "; ".join(problems(q)), "answer": LETTERS[q.answerIndex] if 0 <= q.answerIndex < 4 else "?",
        "sourceRef": q.sourceRef, "sourceUrl": q.sourceUrl or "", "reviewYear": q.reviewYear or "",
        "objectiveId": q.objectiveId,
    }
    for lang in ("fr", "en"):
        b = getattr(q, lang)
        row[f"{lang}_q"] = b.q
        row[f"{lang}_explanation"] = b.explanation
        for l, c in zip(LETTERS, b.choices):
            row[f"{lang}_{l}"] = c
    return row


def _from_row(row: dict) -> Question:
    def block(lang):
        return LangBlock(
            q=row[f"{lang}_q"].strip(),
            choices=[row[f"{lang}_{l}"].strip() for l in LETTERS if row.get(f"{lang}_{l}", "").strip()],
            explanation=row[f"{lang}_explanation"].strip(),
        )

    return Question(
        id=row["id"], scope=row["scope"], chapter=row["chapter"], objectiveId=row["objectiveId"],
        reviewYear=int(row["reviewYear"]) if row["reviewYear"] else None,
        fr=block("fr"), en=block("en"),
        answerIndex=LETTERS.find(row["answer"].strip().upper()),
        sourceRef=row["sourceRef"],
        sourceUrl=row.get("sourceUrl", "").strip() or None,
    )


def export(scopes: list[str]) -> None:
    rows = _read_csv()
    for scope in scopes:
        for q in (Question.model_validate(d) for d in load_json(DRAFTS / f"{scope}.json", [])):
            old = rows.get(q.id)
            if old and old["status"] in REVIEWED:
                old["flags"] = "; ".join(problems(q))  # keep the reviewer's edits
            else:
                rows[q.id] = _to_row(q)

    # Pending + flagged first, so the reviewer starts with the risky ones.
    ordered = sorted(rows.values(), key=lambda r: (r["status"] in REVIEWED, not r["flags"], r["id"]))
    with REVIEW_CSV.open("w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=COLUMNS, extrasaction="ignore")
        w.writeheader()
        w.writerows(ordered)
    pending = sum(r["status"] not in REVIEWED for r in ordered)
    print(f"  review.csv: {len(ordered)} rows, {pending} awaiting review")


def bundle() -> None:
    by_scope: dict[str, list[dict]] = {}
    for row in _read_csv().values():
        if row["status"] != "approved":
            continue
        q = _from_row(row)
        if bad := format_problems(q):
            print(f"  {q.id}: approved but invalid ({', '.join(bad)}), skipped")
            continue
        by_scope.setdefault(q.scope, []).append(q.model_dump(exclude={"flags"}))
    for scope, qs in sorted(by_scope.items()):
        save_json(QUESTIONS / f"{scope}.json", sorted(qs, key=lambda q: q["id"]))
        print(f"  questions/{scope}.json: {len(qs)} approved questions")
