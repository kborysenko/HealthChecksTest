HealthChecksTest

Automated UI health check framework with scheduled execution and optional email notifications.

Prerequisites

1. Install required tools via Homebrew:

- brew install openjdk
- brew install maven
- brew install msmtp

2. Verify:

- java -version
- mvn -version
- msmtp --version

Launch Steps

1. Download and build project

2. Move to parent directory (if needed)
If you opened project in IDE terminal:
cd ..

3. Create project archive
   zip -r HealthChecksTest.zip HealthChecksTest \
   -x "HealthChecksTest/.git/*" \
   -x "HealthChecksTest/target/*" \
   -x "HealthChecksTest/build/*" \
   -x "HealthChecksTest/allure-results/*" \
   -x "HealthChecksTest/.idea/*" \
   -x "HealthChecksTest/*.iml" \
   -x "HealthChecksTest/.DS_Store"

4. Unzip project to test folder
   unzip HealthChecksTest.zip -d test-run

5. Go to project folder
   cd test-run/HealthChecksTest

6. Run installer
   ./install.sh

7. Verify cron job

- Check if cron was installed:
- crontab -l

8. Test email sending (optional)

If email is enabled:
echo "hello test" | msmtp --file=./.msmtprc your@gmail.com

9. Check execution logs
   cat /Users/user/healthcheck-runner/healthcheck.log

======================

What happens after setup

Every 15 minutes cron will:

- run UI tests
- validate application health
- optionally send email on failure

Notes
.msmtprc must have correct permissions:
chmod 600 .msmtprc
Gmail requires App Password, not normal password



