#!/usr/bin/env bash
# Writes a GitHub Actions error annotation for every failed test case of JUnit XML reports.
#
# Usage: scripts/ci/report-junit.sh <directory with TEST-*.xml>
set -uo pipefail
dir="${1:?directory with JUnit XML reports}"

found=0
for report in "$dir"/*.xml; do
    [ -e "$report" ] || continue
    # One line per test case, so failures can be matched to their test case
    tr -d '\n' < "$report" | sed 's/<testcase /\n<testcase /g' | grep -E '<(failure|error)' | while IFS= read -r testcase; do
        class=$(sed -E 's/.*classname="([^"]*)".*/\1/' <<<"$testcase")
        name=$(sed -E 's/<testcase name="([^"]*)".*/\1/' <<<"$testcase")
        message=$(sed -E 's/.*<(failure|error) message="([^"]*)".*/\2/' <<<"$testcase" | cut -c1-500)
        message=$(sed -e 's/&lt;/</g; s/&gt;/>/g; s/&quot;/"/g; s/&#10;/ /g; s/&amp;/\&/g' <<<"$message")
        echo "::error title=Test failed: $class.$name::$message"
    done
    found=1
done
[ "$found" = 1 ] || echo "::error::No JUnit reports found in $dir (the build failed before the tests ran)"
