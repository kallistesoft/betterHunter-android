#!/bin/sh
# Copy the question banks, exam recipes and wildlife photos from the hunt repo (the content source of truth)
# into this app's assets. Usage: scripts/sync-content.sh [path/to/hunt]   (default: ../hunt)
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
HUNT="${1:-$ROOT/../hunt}"
[ -f "$HUNT/tools/sync_app_content.sh" ] || { echo "hunt repo not found at $HUNT" >&2; exit 1; }

# Let the hunt repo pick approved questions over samples and build the photo bank first.
"$HUNT/tools/sync_app_content.sh"

DEST="$ROOT/core/data/src/main/assets"
rm -rf "$DEST/content" "$DEST/wildlife"
mkdir -p "$DEST/content/questions" "$DEST/wildlife"
cp "$HUNT/app/assets/content/exams.json" "$DEST/content/"
cp "$HUNT/app/assets/content/questions/"*.json "$DEST/content/questions/"
cp "$HUNT/app/assets/wildlife/"* "$DEST/wildlife/"

# Not in this app: the RPAL exam (CRFSC, restricted firearms), which hunters don't need (DECISIONS.md, P9).
rm -f "$DEST/content/questions/federal-restricted.json"
python3 - "$DEST/content/exams.json" <<'PY'
import json, sys
path = sys.argv[1]
exams = json.load(open(path, encoding="utf-8"))
exams.pop("CRFSC", None)
with open(path, "w", encoding="utf-8") as f:
    json.dump(exams, f, ensure_ascii=False, indent=2)
    f.write("\n")
PY
echo "Synced $(ls "$DEST/content/questions" | wc -l) question banks and $(ls "$DEST/wildlife" | wc -l) photos."
