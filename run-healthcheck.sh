#!/bin/bash

echo "==============================="
echo "Started: $(date)"
echo "===== Running Health Checks ====="

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Working directory: $SCRIPT_DIR"

# Change to the project directory
cd "$SCRIPT_DIR" || {
    echo "Failed to change directory to $SCRIPT_DIR"
    exit 1
}

# Find Maven
MVN=$(command -v mvn)

if [ -z "$MVN" ]; then
    echo "❌ Maven not found. Please install Maven and ensure it is in your PATH."
    exit 1
fi

echo "Using Maven: $MVN"

# Run the health checks
"$MVN" clean test -DincludeTags=healthcheck

EXIT_CODE=$?

if [ $EXIT_CODE -ne 0 ]; then
    echo "❌ Healthcheck FAILED"

    # Optional Slack notification
    # curl -X POST https://hooks.slack.com/services/XXX \
    #   -H "Content-Type: application/json" \
    #   --data '{"text":"❌ UI Healthcheck failed"}'
else
    echo "✅ Healthcheck PASSED"
fi

echo "Finished: $(date)"

exit $EXIT_CODE