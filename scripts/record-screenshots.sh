#!/bin/sh
# Records the screenshot references for the current branch on CI's machine (the "Record screenshots" workflow), then
# commits and pushes them as you, so the PR's CI starts like for any push (see README, "Screenshot tests").
# Needs the GitHub CLI (gh), a clean working tree and the branch pushed.
set -e
cd "$(dirname "$0")/.."

branch=$(git rev-parse --abbrev-ref HEAD)
head=$(git rev-parse HEAD)
if [ -n "$(git status --porcelain)" ]; then
  echo "error: commit or stash your changes first."
  exit 1
fi
git fetch -q origin "$branch"
if [ "$head" != "$(git rev-parse "origin/$branch")" ]; then
  echo "error: push $branch first: the workflow records what is on GitHub."
  exit 1
fi

gh workflow run screenshots.yml --ref "$branch"
echo "Waiting for the Record screenshots run on $head..."
run=""
while [ -z "$run" ]; do
  sleep 5
  run=$(gh run list --workflow screenshots.yml --branch "$branch" --event workflow_dispatch --limit 5 \
    --json databaseId,headSha --jq "[.[] | select(.headSha == \"$head\")][0].databaseId // empty")
done
gh run watch "$run" --exit-status > /dev/null

images=$(mktemp -d)
gh run download "$run" --name screenshots --dir "$images"
(cd "$images" && find . -name '*.png') | while read -r file; do
  mkdir -p "$(dirname "$file")"
  cp "$images/$file" "$file"
done
rm -rf "$images"

git add -- '*/src/test/screenshots/*.png'
if git diff --cached --quiet; then
  echo "Screenshots unchanged."
else
  git commit -q -m "Update screenshot references"
  git push -q
  echo "Committed and pushed the new references: CI runs on them."
fi
