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
echo "Synced $(ls "$DEST/content/questions" | wc -l) question banks and $(ls "$DEST/wildlife" | wc -l) photos."
