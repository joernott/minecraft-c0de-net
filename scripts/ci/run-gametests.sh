#!/usr/bin/env bash
# Runs the GameTests of a node and watches the server: if it doesn't stop within STOP_SECONDS after the tests
# have finished, the threads of the Java processes are printed and the run is stopped. A hanging server would
# otherwise block the job until GitHub cancels it, without showing where it hangs.
#
# Usage: scripts/ci/run-gametests.sh <gradle task> <log file>
set -uo pipefail

TASK="$1"
LOG="$2"
STOP_SECONDS=${STOP_SECONDS:-120}

./gradlew "$TASK" --console=plain >"$LOG" 2>&1 &
gradle=$!
tail -f "$LOG" --pid=$gradle &

finished_at=""
while kill -0 $gradle 2>/dev/null; do
    if [ -z "$finished_at" ] && grep -qE "required tests (passed|failed)" "$LOG"; then
        finished_at=$SECONDS
    fi
    if [ -n "$finished_at" ] && [ $((SECONDS - finished_at)) -ge "$STOP_SECONDS" ]; then
        echo "::error title=GameTests::The GameTest server didn't stop within $STOP_SECONDS seconds after the tests"
        for pid in $(jps -q 2>/dev/null); do
            echo "===== threads of Java process $pid: $(jps -l 2>/dev/null | grep "^$pid " | cut -d' ' -f2-)"
            jcmd "$pid" Thread.print 2>/dev/null | awk '/^"Server thread"/,/^$/' || true
        done
        pkill -P $gradle 2>/dev/null || true
        kill $gradle 2>/dev/null || true
        pkill -f GradleDaemon 2>/dev/null || true
        exit 1
    fi
    sleep 5
done
wait $gradle
