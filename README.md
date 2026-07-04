# UI System Healthcheck

Automated, smoke-level UI healthchecks for two product areas of the Ad Intelligence app:

1. **Ad Intelligence · Brands module** — navigation present, reports offered as working entry points, no blocking errors, and drilling into a report actually navigates.
2. **AI Chatbot · "Ask anything…"** — the input is usable, a prompt can be sent, and a real (non-placeholder) assistant answer renders back.

Each check goes beyond "the page loaded" and exercises the feature the way a user would, so a green run means the feature is genuinely usable. Built to run repeatedly on a schedule, inside a Docker container, and to alert (Slack + email) the moment a feature is down.

- **Stack:** Java 17 · [Selenide](https://selenide.org/) (Selenium) · JUnit 5 · Allure
- **Driver:** resolved automatically by Selenium Manager (no local driver install)

---

## Prerequisites

- **Docker** (the only requirement to run it) — Chrome + Java + Maven all live in the image.
- For local (non-Docker) runs: JDK 17 and Maven, plus Chrome installed.

---

## Quick start (Docker)

```bash
# 1. Configure an environment (target + credentials + optional alert secrets)
cp envs/staging.env.example envs/staging.env
$EDITOR envs/staging.env

# 2. Build the image  (Apple Silicon: add  --platform linux/amd64)
docker build -t ui-healthcheck .

# 3. Run the healthcheck
docker run --rm --env-file envs/staging.env -v "$PWD/target:/app/target" ui-healthcheck
```

Or with Compose:

```bash
docker compose run --rm healthcheck
```

The container runs the tests headless, exits `0` on success / non-zero on failure, and on failure sends alerts (see [Notifications](#failure-notifications)). Reports and failure screenshots land in `target/`.

---

## Local run (without Docker)

```bash
cp config.properties.example src/test/resources/config.properties   # then fill in
$EDITOR src/test/resources/config.properties

mvn test                 # runs only the @Tag("healthcheck") tests
mvn allure:serve         # open the HTML report
```

Config is read from **environment variables first**, then `config.properties`, so you can also just:

```bash
APP_URL=https://stg-ui.adcint.com APP_USERNAME=... APP_PASSWORD=... mvn test
```

---

## Configuration & multiple environments

All configuration is injected at runtime — **nothing sensitive is committed**.

| Env var | `config.properties` key | Meaning |
|---|---|---|
| `APP_URL` | `app.url` | Base URL under test |
| `APP_USERNAME` | `app.username` | Login email |
| `APP_PASSWORD` | `app.password` | Login password |
| `HEADLESS` | `selenide.headless` | Headless Chrome (default `true`) |

Each environment is just its own env file: copy `envs/staging.env.example` to `envs/staging.env`, `envs/prod.env`, etc. (all gitignored), and select one with `--env-file`.

---

## Failure notifications

On failure the container notifies over **both** channels, each enabled only if its variables are set (so either or both work):

- **Slack** — set `SLACK_WEBHOOK_URL` (an [incoming webhook](https://api.slack.com/messaging/webhooks)). Webhooks can't carry files, so Slack gets the alert text plus a CI-run link (when running in GitHub Actions).
- **Email** — set `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASS`, `EMAIL_FROM`, `EMAIL_TO` (Gmail needs an App Password). The email attaches the failure **screenshots** and a plaintext **test summary**, and **links** the full Allure report. Gmail blocks HTML/JS attachments (`552 5.7.0`), so the report is linked — not attached — by default. Set `ALLURE_ATTACH=true` (optionally `ALLURE_ZIP_PASSWORD`, default `healthcheck`) to attach it as a password-protected zip that bypasses the scan. Sent by `notify_email.py`; supports SMTPS (`465`) and STARTTLS (`587`).

Notification lives in `docker-entrypoint.sh`, which captures the test exit code and alerts *after* the run — so a failing test always triggers an alert. On failure it generates the Allure report (`mvn allure:report`) for the CI artifact / link (and for the optional attach mode).

### Testing notifications

Alerts fire whenever the test run exits non-zero, so you can emulate a failure just by pointing at a bad target. Keep it tidy with a throwaway env file (same shape as the compose flow):

```bash
cp envs/staging.env.example envs/failtest.env   # set a bad APP_URL
docker run --rm --platform linux/amd64 --env-file envs/failtest.env ui-healthcheck
```

Login navigation fails → tests error → both configured channels notify → the container exits `1`.

Tips:
- To inspect the alert without spamming a real channel, point `SLACK_WEBHOOK_URL` at a [`webhook.site`](https://webhook.site) URL (live inspector) or `https://httpbin.org/post` (echoes the payload to the run log).
- Email uses SMTPS (implicit TLS, port `465`) — a Gmail **App Password** works out of the box.

---

## Reports & screenshots

- **Allure** report: `mvn allure:serve` (or `mvn allure:report` → `target/site/allure-maven-plugin`).
- **Screenshot + page source on failure** are captured while the browser is still open (`ScreenshotOnFailure` JUnit 5 extension), attached to the Allure report, and written to `target/screenshots/`.
- Surefire results: `target/surefire-reports/`.

---

## CI & scheduling

- **GitHub Actions** (`.github/workflows/healthcheck.yml`) builds and runs the image every 30 min (`schedule`) and on demand (`workflow_dispatch`), pulling credentials from repo **secrets** and uploading screenshots/reports as artifacts on failure.
- **Self-hosted alternative** — schedule the container with host cron:
  ```
  */30 * * * * docker run --rm --env-file /path/envs/staging.env ui-healthcheck >> /var/log/uihealthcheck.log 2>&1
  ```
