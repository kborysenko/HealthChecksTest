#!/bin/bash
set -e

INSTALL_DIR="$HOME/healthcheck-runner"

echo "Installing to $INSTALL_DIR"

# Clean previous install
rm -rf "$INSTALL_DIR"
mkdir -p "$INSTALL_DIR"

echo "Copying project..."

# Copy only needed files
cp -R src "$INSTALL_DIR/"
cp pom.xml "$INSTALL_DIR/"
cp run-healthcheck.sh "$INSTALL_DIR/"

# Optional files
[ -f README.md ] && cp README.md "$INSTALL_DIR/"
[ -f browsers.json ] && cp browsers.json "$INSTALL_DIR/"
[ -f LICENSE ] && cp LICENSE "$INSTALL_DIR/"

chmod +x "$INSTALL_DIR/run-healthcheck.sh"

echo "Checking Maven..."

MVN=$(command -v mvn)

if [ -z "$MVN" ]; then
    echo "ERROR: Maven is not installed or not in PATH."
    exit 1
fi

echo "Using Maven: $MVN"

# Cron job definition
CRON_JOB="*/15 * * * * $INSTALL_DIR/run-healthcheck.sh >> $INSTALL_DIR/healthcheck.log 2>&1"

echo "Installing cron job..."

TMP_CRON=$(mktemp)

# Load existing crontab (if any)
crontab -l 2>/dev/null > "$TMP_CRON" || true

# Remove old version of our job
grep -v "run-healthcheck.sh" "$TMP_CRON" > "${TMP_CRON}.new" || true

# Add new job
echo "$CRON_JOB" >> "${TMP_CRON}.new"

# Install new crontab
crontab "${TMP_CRON}.new"

# Cleanup
rm -f "$TMP_CRON" "${TMP_CRON}.new"

echo ""
echo "Installation completed successfully."
echo "Installed to: $INSTALL_DIR"
echo "Cron job:"
echo "$CRON_JOB"
echo ""
echo "Verify with: crontab -l"