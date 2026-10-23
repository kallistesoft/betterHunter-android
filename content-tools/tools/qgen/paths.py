import json
from pathlib import Path

from pydantic import BaseModel

ROOT = Path(__file__).resolve().parents[2]  # content-tools/
CONTENT = ROOT / "content"
SYLLABUS = CONTENT / "syllabus"
DRAFTS = CONTENT / "drafts"
# Approved questions go straight into the Android app's bundled content.
QUESTIONS = ROOT.parent / "core" / "data" / "src" / "main" / "assets" / "content" / "questions"
REVIEW_CSV = CONTENT / "review.csv"


def load_json(path: Path, default=None):
    if not path.exists():
        return default
    return json.loads(path.read_text(encoding="utf-8"))


def save_json(path: Path, data) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    if isinstance(data, list):
        data = [d.model_dump() if isinstance(d, BaseModel) else d for d in data]
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def load_sources() -> dict:
    from .schema import Source

    raw = load_json(CONTENT / "sources.json", {})
    return {scope: Source.model_validate(s) for scope, s in raw.items() if not scope.startswith("_")}
