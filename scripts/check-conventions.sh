#!/bin/sh
# Project rules that lint doesn't enforce (see README, "Conventions"). Run by CI; run it before pushing.
cd "$(dirname "$0")/.." || exit 1
status=0

# 1. No build-type branches in code: debug code goes in src/debug, release code in src/release.
branches=$(grep -rnE --include='*.kt' --exclude-dir=build 'BuildConfig\.(DEBUG|BUILD_TYPE)' .)
if [ -n "$branches" ]; then
  echo "$branches"
  echo "error: don't branch on the build type; put the code in src/debug and src/release."
  status=1
fi

# 2. No dp literals outside the design system's theme: use Spacing, Sizes or MaterialTheme.shapes.
literals=$(grep -rnE --include='*.kt' --exclude-dir=build '[0-9]\.dp\b' . \
  | grep -v '/core/designsystem/src/main/kotlin/ca/kallistesoft/hunt/core/designsystem/theme/')
if [ -n "$literals" ]; then
  echo "$literals"
  echo "error: sizes come from the theme (Spacing, Sizes, MaterialTheme.shapes), not dp literals."
  status=1
fi

[ $status -eq 0 ] && echo "Conventions OK."
exit $status
