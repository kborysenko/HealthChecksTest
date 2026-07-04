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

echo ""
echo "=== Email Notification (optional) ==="

read -p "Enable email alerts? (y/n): " ENABLE_EMAIL

EMAIL=""
GMAIL_USER=""
GMAIL_APP_PASS=""

if [ "$ENABLE_EMAIL" = "y" ]; then
    read -p "Send alerts to email (to): " EMAIL
    read -p "Gmail address (from): " GMAIL_USER
    read -s -p "Gmail App Password: " GMAIL_APP_PASS
    echo ""
fi

# -------------------------
# CONFIG FILE (APP)
# -------------------------
mkdir -p "$INSTALL_DIR/src/test/resources"

cat > "$INSTALL_DIR/src/test/resources/config.properties" <<EOF
app.url=$APP_URL
app.username=$USERNAME
app.password=$PASSWORD
EOF

echo "Configuration saved."

# -------------------------
# EMAIL CONFIG (.env)
# -------------------------
if [ "$ENABLE_EMAIL" = "y" ]; then
cat > "$INSTALL_DIR/.env" <<EOF
ENABLE_EMAIL=y
EMAIL_TO=$EMAIL
GMAIL_USER=$GMAIL_USER
GMAIL_APP_PASS=$GMAIL_APP_PASS
EOF
fi

# -------------------------
# MSMTP CONFIG
# -------------------------
if [ "$ENABLE_EMAIL" = "y" ]; then
cat > "$INSTALL_DIR/.msmtprc" <<EOF
defaults
auth on
tls on
tls_trust_file /etc/ssl/cert.pem
logfile ~/.msmtp.log

account gmail
host smtp.gmail.com
port 587

auth on
user $GMAIL_USER
password $GMAIL_APP_PASS
from $GMAIL_USER

account default : gmail
EOF

chmod 600 "$INSTALL_DIR/.msmtprc"
fi

# -------------------------
# MAVEN CHECK
# -------------------------
echo ""
echo "Checking Maven..."

MVN=$(command -v mvn)

if [ -z "$MVN" ]; then
    echo "ERROR: Maven is not installed or not in PATH."
    exit 1
fi

echo "Using Maven: $MVN"

# -------------------------
# CRON JOB
# -------------------------
CRON_JOB="*/15 * * * * /bin/bash $INSTALL_DIR/run-healthcheck.sh >> $INSTALL_DIR/healthcheck.log 2>&1"

echo "Installing cron job..."

TMP_CRON=$(mktemp)

crontab -l 2>/dev/null > "$TMP_CRON" || true

grep -v "run-healthcheck.sh" "$TMP_CRON" > "${TMP_CRON}.new" || true

echo "$CRON_JOB" >> "${TMP_CRON}.new"

crontab "${TMP_CRON}.new"

rm -f "$TMP_CRON" "${TMP_CRON}.new"

# -------------------------
# SUMMARY
# -------------------------
echo ""
echo "========================================"
echo "Installation completed successfully."
echo "Installed to: $INSTALL_DIR"
echo ""
echo "Config file:"
echo "  $INSTALL_DIR/src/test/resources/config.properties"

if [ "$ENABLE_EMAIL" = "y" ]; then
echo ""
echo "Email config:"
echo "  $INSTALL_DIR/.env"
echo "  $INSTALL_DIR/.msmtprc (chmod 600 applied)"
fi

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