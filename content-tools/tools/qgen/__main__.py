"""Question pipeline CLI. Run from tools/: python -m qgen <command> ..."""
import argparse

from . import checks, review


def main() -> None:
    p = argparse.ArgumentParser(prog="qgen")
    sub = p.add_subparsers(dest="cmd", required=True)

    s = sub.add_parser("syllabus", help="extract learning objectives from the source PDF (API)")
    s.add_argument("scope")
    s.add_argument("--per-chapter", type=int, default=15)

    s = sub.add_parser("generate", help="draft questions for each objective (API)")
    s.add_argument("scope")
    s.add_argument("--per-objective", type=int, default=3)

    s = sub.add_parser("check", help="local format / duplicate checks (free)")
    s.add_argument("scope")

    s = sub.add_parser("verify", help="blind-answer check against the source (API)")
    s.add_argument("scope")
    s.add_argument("--lang", choices=["fr", "en", "both"], default="both")
    s.add_argument("--recheck", action="store_true", help="re-verify questions already checked")

    s = sub.add_parser("export", help="merge drafts into content/review.csv")
    s.add_argument("scopes", nargs="+")

    sub.add_parser("bundle", help="write approved questions to content/questions/")

    s = sub.add_parser("all", help="syllabus → generate → check → verify → export")
    s.add_argument("scope")

    a = p.parse_args()
    if a.cmd in ("syllabus", "generate", "verify", "all"):
        from . import generate, syllabus, verify  # needs API credentials
    if a.cmd == "syllabus":
        syllabus.build(a.scope, a.per_chapter)
    elif a.cmd == "generate":
        generate.generate(a.scope, a.per_objective)
    elif a.cmd == "check":
        checks.run(a.scope)
    elif a.cmd == "verify":
        verify.run(a.scope, ("fr", "en") if a.lang == "both" else (a.lang,), a.recheck)
    elif a.cmd == "export":
        review.export(a.scopes)
    elif a.cmd == "bundle":
        review.bundle()
    elif a.cmd == "all":
        syllabus.build(a.scope)
        generate.generate(a.scope)
        checks.run(a.scope)
        verify.run(a.scope)
        review.export([a.scope])


if __name__ == "__main__":
    main()
