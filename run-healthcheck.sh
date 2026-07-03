#!/bin/bash

set -e

echo "==============================="
echo "Started: $(date)"
echo "===== Running Health Checks ====="

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

echo "Working directory: $(pwd)"
echo "User: $(whoami)"
echo "HOME: $HOME"

# Set PATH for cron
export PATH="/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"

echo "PATH: $PATH"

# Use the known Maven location
MVN="/usr/local/bin/mvn"

if [ ! -x "$MVN" ]; then
    echo "❌ Maven executable not found at: $MVN"
    ls -l /usr/local/bin
    exit 1
fi

echo "Using Maven: $MVN"

echo "Java version:"
java -version || {
    echo "❌ Java is not available."
    exit 1
}

echo "Maven version:"
"$MVN" -version || {
    echo "❌ Maven cannot start."
    exit 1
}

echo "Running health checks..."

"$MVN" clean test -DincludeTags=healthcheck

EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
    echo "✅ Healthcheck PASSED"
else
    echo "❌ Healthcheck FAILED"
fi

echo "Finished: $(date)"

exit $EXIT_CODE