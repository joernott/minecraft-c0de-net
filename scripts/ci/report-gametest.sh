#!/usr/bin/env bash
# Writes GitHub Actions error annotations for failed GameTests and errors found in a GameTest server log.
#
# Usage: scripts/ci/report-gametest.sh <log file>
set -uo pipefail
log="${1:?log file}"

if [ ! -s "$log" ]; then
    echo "::error::The GameTest server did not produce any output"
    exit 0
fi
# Failed tests, the summary line of the test runner and exceptions
grep -E "failed|required tests|Exception|Error:|BUILD FAILED" "$log" | grep -v "^> Task" | head -20 | while IFS= read -r line; do
    echo "::error title=GameTest::$(cut -c1-500 <<<"$line")"
done
