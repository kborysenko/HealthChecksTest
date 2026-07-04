#!/usr/bin/env python3
"""Send the healthcheck failure email with attachments.

Invoked by docker-entrypoint.sh on failure:

    python3 notify_email.py "<subject>" "<body>"

Always attaches the failure screenshots (PNG) and the plaintext test summary
(surefire *.txt) — both are safe for Gmail. The Allure report is HTML/JS, which
Gmail blocks as an attachment (552 5.7.0), so by default it is LINKED, not attached.
Set ALLURE_ATTACH=true to attach it as a password-protected zip that bypasses the scan.

Configuration (environment):
    SMTP_HOST, SMTP_PORT (default 465), SMTP_USER, SMTP_PASS, EMAIL_FROM (default SMTP_USER), EMAIL_TO
    ALLURE_ATTACH (default off), ALLURE_ZIP_PASSWORD (default "healthcheck"), REPORT_URL (optional)
    GITHUB_SERVER_URL / GITHUB_REPOSITORY / GITHUB_RUN_ID (used to build a CI report link)

Transport: port 465 -> implicit TLS, 587 -> STARTTLS, other -> plain (local test sink).
Skips silently if SMTP_HOST/EMAIL_TO are unset, and never raises — a broken mailer must
not mask the underlying test failure or crash the container.
"""
import glob
import os
import smtplib
import subprocess
import sys
import xml.etree.ElementTree as ET
from email.message import EmailMessage
from pathlib import Path

SCREENSHOT_GLOB = "target/screenshots/*.png"
SUREFIRE_GLOB = "target/surefire-reports/*.txt"
SUREFIRE_XML_GLOB = "target/surefire-reports/TEST-*.xml"
ALLURE_REPORT_DIR = "target/site/allure-maven-plugin"
ALLURE_ZIP = "target/allure-report.zip"

TRUTHY = {"1", "true", "yes", "on"}
MSG_MAX = 300  # max chars of a failure message shown per test in the body


def _attach(msg, path, *, filename=None, maintype="application", subtype="octet-stream"):
    msg.add_attachment(
        Path(path).read_bytes(),
        maintype=maintype,
        subtype=subtype,
        filename=filename or Path(path).name,
    )


def _make_encrypted_zip(src_dir, out_path, password):
    """Encrypted (ZipCrypto) zip via the `zip` CLI so Gmail can't scan the HTML/JS inside."""
    if os.path.exists(out_path):
        os.remove(out_path)
    subprocess.run(
        ["zip", "-q", "-r", "-P", password, os.path.abspath(out_path), "."],
        cwd=src_dir,
        check=True,
    )
    return out_path


def prepare_report():
    """Return (zip_path_to_attach_or_None, body_note); builds the encrypted zip in attach mode."""
    have_report = Path(ALLURE_REPORT_DIR).is_dir()
    attach = os.environ.get("ALLURE_ATTACH", "").strip().lower() in TRUTHY

    if attach and have_report:
        password = os.environ.get("ALLURE_ZIP_PASSWORD") or "healthcheck"
        try:
            _make_encrypted_zip(ALLURE_REPORT_DIR, ALLURE_ZIP, password)
            note = (f"\n\nThe Allure report is attached as allure-report.zip "
                    f"(password-protected: {password}). Unzip and open index.html.")
            return ALLURE_ZIP, note
        except Exception as exc:
            print(f"WARN: could not build encrypted report zip: {exc}")
            # fall through to link mode

    # Link / hint mode — nothing HTML is attached, so Gmail accepts the message.
    url = os.environ.get("REPORT_URL")
    if not url and os.environ.get("GITHUB_RUN_ID"):
        server = os.environ.get("GITHUB_SERVER_URL", "https://github.com")
        repo = os.environ.get("GITHUB_REPOSITORY", "")
        url = f"{server}/{repo}/actions/runs/{os.environ['GITHUB_RUN_ID']}"
    if url:
        return None, f"\n\nFull Allure report: {url}"
    return None, ("\n\nFull Allure report: target/site/allure-maven-plugin/index.html "
                  "(on the runner; uploaded as a CI artifact in GitHub Actions).")


def failed_tests():
    """Genuinely-failed tests from surefire XML: [(name, first_line_of_message), ...].

    Only a testcase's DIRECT <failure>/<error> child counts — retries live in separate
    <rerunFailure>/<rerunError> elements and flaky-but-passed tests use <flakyFailure>, so this
    yields one line per truly-failed test (reruns deduped, passed/flaky excluded).
    """
    failures = []
    for xml_path in sorted(glob.glob(SUREFIRE_XML_GLOB)):
        try:
            root = ET.parse(xml_path).getroot()
        except ET.ParseError:
            continue
        for tc in root.iter("testcase"):
            node = tc.find("failure")
            if node is None:
                node = tc.find("error")
            if node is None:
                continue  # passed, or flaky-but-passed
            name = f"{tc.get('classname', '')}.{tc.get('name', '')}".strip(".")
            message = (node.get("message") or node.get("type") or "").strip()
            first_line = message.splitlines()[0] if message else ""
            if len(first_line) > MSG_MAX:
                first_line = first_line[:MSG_MAX].rstrip() + "…"
            failures.append((name, first_line))
    return failures


def failures_section():
    """A concise 'Failed checks' block for the email body ('' if none are parseable)."""
    failures = failed_tests()
    if not failures:
        return ""
    lines = [f"\n\nFailed checks ({len(failures)}):"]
    for name, message in failures:
        lines.append(f" • {name}" + (f" — {message}" if message else ""))
    return "\n".join(lines)


def build_message(subject, body):
    msg = EmailMessage()
    msg["From"] = os.environ.get("EMAIL_FROM") or os.environ.get("SMTP_USER")
    msg["To"] = os.environ["EMAIL_TO"]
    msg["Subject"] = subject

    report_zip, note = prepare_report()
    msg.set_content(body + failures_section() + note)  # body must be set before attachments

    for png in sorted(glob.glob(SCREENSHOT_GLOB)):
        _attach(msg, png, maintype="image", subtype="png")
    for txt in sorted(glob.glob(SUREFIRE_GLOB)):
        _attach(msg, txt, maintype="text", subtype="plain")
    if report_zip:
        _attach(msg, report_zip, filename="allure-report.zip", maintype="application", subtype="zip")

    return msg


def send(msg):
    host = os.environ["SMTP_HOST"]
    port = int(os.environ.get("SMTP_PORT", "465"))
    user = os.environ.get("SMTP_USER")
    password = os.environ.get("SMTP_PASS", "")

    server = smtplib.SMTP_SSL(host, port, timeout=30) if port == 465 \
        else smtplib.SMTP(host, port, timeout=30)
    with server:
        if port == 587:
            server.starttls()
        if user:
            server.login(user, password)
        server.send_message(msg)


def main():
    subject = sys.argv[1] if len(sys.argv) > 1 else "UI healthcheck FAILED"
    body = sys.argv[2] if len(sys.argv) > 2 else ""

    if not os.environ.get("SMTP_HOST") or not os.environ.get("EMAIL_TO"):
        print("Email skipped: SMTP_HOST/EMAIL_TO not set.")
        return 0
    if not (os.environ.get("EMAIL_FROM") or os.environ.get("SMTP_USER")):
        print("WARN: email skipped — neither EMAIL_FROM nor SMTP_USER set (no From address).")
        return 0

    try:
        msg = build_message(subject, body)
        attachments = [part.get_filename() for part in msg.iter_attachments()]
        print(f"Sending email to {os.environ['EMAIL_TO']} with attachments: {attachments}")
        send(msg)
        print("Email sent.")
    except Exception as exc:  # never mask the test failure
        print(f"WARN: email notification failed: {exc}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
