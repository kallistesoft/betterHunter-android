"""Data models for the question pipeline.

The model-facing schemas (`*Out`) avoid JSON-schema constraints that structured
outputs don't support (min/max lengths, etc.); those rules are enforced locally
in checks.py instead.
"""
from __future__ import annotations

from typing import Literal, Optional

from pydantic import BaseModel

Lang = Literal["fr", "en"]


# ---- source manifest (content/sources.json) ----

class Chapter(BaseModel):
    id: str
    title: str
    pages: tuple[int, int]  # 1-indexed, inclusive


class Source(BaseModel):
    file: str  # path relative to repo root, under sources/ (gitignored)
    lang: Lang
    chapters: list[Chapter]


# ---- syllabus ----

class ObjectiveOut(BaseModel):
    statement: str  # the testable fact, paraphrased, never copied
    page: int
    volatile: bool  # depends on yearly regulations (dates, fees, bag limits, ...)


class SyllabusOut(BaseModel):
    objectives: list[ObjectiveOut]


class Objective(BaseModel):
    objectiveId: str
    scope: str
    chapter: str
    statement: str
    sourceRef: str  # "<source file name> p.<page>"
    volatile: bool


# ---- questions ----

class LangBlock(BaseModel):
    q: str
    choices: list[str]
    explanation: str


class QuestionOut(BaseModel):
    fr: LangBlock
    en: LangBlock
    answerIndex: int


class QuestionsOut(BaseModel):
    questions: list[QuestionOut]


class Question(BaseModel):
    id: str
    scope: str
    chapter: str
    objectiveId: str
    reviewYear: Optional[int] = None
    fr: LangBlock
    en: LangBlock
    answerIndex: int
    sourceRef: str
    sourceUrl: Optional[str] = None  # official page confirming a regulation fact (law, quebec.ca, rcmp.ca)
    flags: list[str] = []


# ---- blind verification ----

class VerdictOut(BaseModel):
    chosenIndex: int  # -1 if no choice is supported by the source
    ambiguous: bool
    issue: str  # empty when the question is fine


def strict_schema(model: type[BaseModel]) -> dict:
    """JSON schema for structured outputs: every object closed and fully required."""
    schema = model.model_json_schema()

    def close(node):
        if isinstance(node, dict):
            if node.get("type") == "object" and "properties" in node:
                node["additionalProperties"] = False
                node["required"] = list(node["properties"])
            for v in node.values():
                close(v)
        elif isinstance(node, list):
            for v in node:
                close(v)

    close(schema)
    return schema
