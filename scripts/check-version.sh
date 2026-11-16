#!/usr/bin/env bash
# Checks that the version in the source of the active Stonecutter node (C0deNet.VERSION) is mod.version. Stonecutter
# writes the swap mod_version only into the generated sources of the other nodes; the active node (1.21.1-fabric)
# is built from src/ as it is, so its jar reported 0.1.0 up to 0.3.0 (found on 2026-11-16, task 8.1.1; the same as c0de task 16.16.2). After changing mod.version, run
#   ./gradlew "Refresh active project"
# and commit C0deNet.java.
#
# Usage: scripts/check-version.sh
set -euo pipefail
cd "$(dirname "$0")/.."

expected="$(sed -n 's/^mod\.version = "\(.*\)"/\1/p' stonecutter.properties.toml)"
actual="$(sed -n 's/.*VERSION = \/\*\$ mod_version\*\/ *"\(.*\)";/\1/p' src/main/java/de/joernott/c0denet/C0deNet.java)"
if [ "$expected" != "$actual" ]; then
    echo "::error file=src/main/java/de/joernott/c0denet/C0deNet.java::C0deNet.VERSION is '$actual', but mod.version is '$expected'; run ./gradlew \"Refresh active project\""
    exit 1
fi
echo "C0deNet.VERSION is $actual"
