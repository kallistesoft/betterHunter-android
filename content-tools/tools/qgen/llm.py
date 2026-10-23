"""Thin wrapper around the Claude API: PDF page slices + structured JSON output."""
from __future__ import annotations

import base64
import io
from functools import lru_cache
from pathlib import Path

import anthropic
from pydantic import BaseModel
from pypdf import PdfReader, PdfWriter

from .paths import ROOT
from .schema import strict_schema

MODEL = "claude-opus-5-5"
FALLBACK_BETA = "server-side-fallback-2026-07-01"


@lru_cache(maxsize=1)
def _client() -> anthropic.Anthropic:
    return anthropic.Anthropic()


class GenerationError(RuntimeError):
    pass


@lru_cache(maxsize=32)
def pdf_slice_b64(file: str, first: int, last: int) -> str:
    """Base64 of pages first..last (1-indexed, inclusive) of a source PDF."""
    reader = PdfReader(ROOT / file)
    writer = PdfWriter()
    for i in range(first - 1, min(last, len(reader.pages))):
        writer.add_page(reader.pages[i])
    buf = io.BytesIO()
    writer.write(buf)
    return base64.b64encode(buf.getvalue()).decode()


def document_block(file: str, first: int, last: int) -> dict:
    """A cacheable PDF block. Keep it first in the message so every call on the
    same chapter reuses the cached prefix."""
    return {
        "type": "document",
        "source": {"type": "base64", "media_type": "application/pdf", "data": pdf_slice_b64(file, first, last)},
        "title": f"{Path(file).name} p.{first}-{last}",
        "cache_control": {"type": "ephemeral"},
    }


def ask(system: str, content: list[dict], out: type[BaseModel], effort: str = "high") -> BaseModel:
    resp = _client().beta.messages.create(
        model=MODEL,
        max_tokens=16000,
        betas=[FALLBACK_BETA],
        fallbacks="default",
        output_config={"effort": effort, "format": {"type": "json_schema", "schema": strict_schema(out)}},
        system=system,
        messages=[{"role": "user", "content": content}],
    )
    if resp.stop_reason == "refusal":
        raise GenerationError(f"refused: {resp.stop_details}")
    if resp.stop_reason == "max_tokens":
        raise GenerationError("output truncated (max_tokens)")
    text = next(b.text for b in resp.content if b.type == "text")
    return out.model_validate_json(text)
