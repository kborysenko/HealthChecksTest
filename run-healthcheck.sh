#!/bin/bash

set -e

echo "==============================="
echo "Started: $(date)"
echo "===== Running Health Checks ====="

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

# Load environment config (.env)
if [ -f ".env" ]; then
    export $(cat .env | xargs)
fi

echo "Working directory: $(pwd)"
echo "User: $(whoami)"
echo "HOME: $HOME"

# Set PATH for cron
export PATH="/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"

echo "PATH: $PATH"

# Maven path
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

# =========================
# EMAIL FUNCTION
# =========================
send_email() {
    SUBJECT="$1"
    BODY="$2"

    echo "$BODY" | msmtp \
        --file="$SCRIPT_DIR/.msmtprc" \
        "$EMAIL_TO"
}

echo "Running health checks..."

"$MVN" clean test -DincludeTags=healthcheck
EXIT_CODE=$?

LOG_FILE="$SCRIPT_DIR/healthcheck.log"

# =========================
# RESULT HANDLING
# =========================
if [ $EXIT_CODE -eq 0 ]; then
    echo "✅ Healthcheck PASSED"

    if [ "$ENABLE_EMAIL" = "y" ]; then
        send_email "Healthcheck PASSED" "All tests passed successfully at $(date)"
    fi

else
    echo "❌ Healthcheck FAILED"

    if [ "$ENABLE_EMAIL" = "y" ]; then
        send_email "❌ Healthcheck FAILED" "Tests failed at $(date). Check logs: $LOG_FILE"
    fi
fi

echo "Finished: $(date)"

exit $EXIT_CODE