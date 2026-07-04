#!/usr/bin/env bash
# Runs the healthcheck suite and, on failure, notifies via Slack and/or email.
set -o pipefail

now() { date -u +'%Y-%m-%dT%H:%M:%SZ'; }

echo "=== UI healthcheck started: $(now) ==="
echo "Target: ${APP_URL:-<APP_URL not set>}"

mvn -q -B test
CODE=$?

if [ "$CODE" -eq 0 ]; then
    echo "=== Healthcheck PASSED ($(now)) ==="
    exit 0
fi

echo "=== Healthcheck FAILED with exit code $CODE ($(now)) ==="

echo "Generating Allure report..."
mvn -q -B allure:report || echo "WARN: Allure report generation failed"

SUBJECT="🔴 UI healthcheck FAILED: ${APP_URL:-unknown}"
BODY="UI healthcheck failed at $(now) against ${APP_URL:-unknown}. Failure screenshots and a test summary are attached; raw artifacts are in target/ (surefire-reports, screenshots, allure-results)."

notify_slack() {
    [ -n "${SLACK_WEBHOOK_URL:-}" ] || return 0
    echo "Sending Slack notification..."
    # Slack incoming webhooks can't carry files — send text, plus a CI run link when available.
    text="$SUBJECT\\n$BODY"
    if [ -n "${GITHUB_RUN_ID:-}" ]; then
        text="$text\\nCI run: ${GITHUB_SERVER_URL:-https://github.com}/${GITHUB_REPOSITORY:-}/actions/runs/${GITHUB_RUN_ID}"
    fi
    payload=$(printf '{"text":"%s"}' "$text")
    curl -sS -X POST -H 'Content-type: application/json' --data "$payload" "$SLACK_WEBHOOK_URL" \
        || echo "WARN: Slack notification failed"
    echo
}

notify_email() {
    [ -n "${SMTP_HOST:-}" ] && [ -n "${EMAIL_TO:-}" ] || return 0
    python3 /app/notify_email.py "$SUBJECT" "$BODY" || echo "WARN: email notification failed"
}

notify_slack
notify_email

exit "$CODE"
