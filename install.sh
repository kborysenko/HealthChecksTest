#!/bin/bash
set -e

INSTALL_DIR="$HOME/healthcheck-runner"

echo "Installing to $INSTALL_DIR"

# Clean previous install
rm -rf "$INSTALL_DIR"
mkdir -p "$INSTALL_DIR"

echo "Copying project..."

# Copy project
cp -R src "$INSTALL_DIR/"
cp pom.xml "$INSTALL_DIR/"
cp run-healthcheck.sh "$INSTALL_DIR/"

# Optional files
[ -f README.md ] && cp README.md "$INSTALL_DIR/"
[ -f browsers.json ] && cp browsers.json "$INSTALL_DIR/"
[ -f LICENSE ] && cp LICENSE "$INSTALL_DIR/"

chmod +x "$INSTALL_DIR/run-healthcheck.sh"

echo ""
echo "=== Application Configuration ==="

read -p "Application URL: " APP_URL
read -p "Username: " USERNAME
read -s -p "Password: " PASSWORD
echo ""

mkdir -p "$INSTALL_DIR/src/test/resources"

cat > "$INSTALL_DIR/src/test/resources/config.properties" <<EOF
app.url=$APP_URL
app.username=$USERNAME
app.password=$PASSWORD
EOF

echo "Configuration saved."

echo ""
echo "Checking Maven..."

MVN=$(command -v mvn)

if [ -z "$MVN" ]; then
    echo "ERROR: Maven is not installed or not in PATH."
    exit 1
fi

echo "Using Maven: $MVN"

# Cron job definition
CRON_JOB="*/15 * * * * /bin/bash $INSTALL_DIR/run-healthcheck.sh >> $INSTALL_DIR/healthcheck.log 2>&1"

echo "Installing cron job..."

TMP_CRON=$(mktemp)

# Load existing crontab
crontab -l 2>/dev/null > "$TMP_CRON" || true

# Remove previous healthcheck job
grep -v "run-healthcheck.sh" "$TMP_CRON" > "${TMP_CRON}.new" || true

# Add new job
echo "$CRON_JOB" >> "${TMP_CRON}.new"

# Install new crontab
crontab "${TMP_CRON}.new"

# Cleanup
rm -f "$TMP_CRON" "${TMP_CRON}.new"

echo ""
echo "========================================"
echo "Installation completed successfully."
echo "Installed to: $INSTALL_DIR"
echo "Configuration file:"
echo "  $INSTALL_DIR/src/test/resources/config.properties"
echo ""
echo "Cron job:"
echo "  $CRON_JOB"
echo ""
echo "Verify cron with:"
echo "  crontab -l"
echo ""
echo "Run manually with:"
echo "  cd $INSTALL_DIR && ./run-healthcheck.sh"
echo "========================================"