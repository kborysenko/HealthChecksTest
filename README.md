auto-awesome/
│
├── src/
├── pom.xml
├── run-healthcheck.sh        (macOS runner)
├── install.sh                (macOS installer + cron setup)
│
├── run-healthcheck.ps1       (Windows runner)
├── install.ps1               (Windows installer + scheduled task)
│
└── README.md

🍏 macOS Setup (cron)
1. Unzip project
   unzip auto-awesome.zip
   cd auto-awesome
2. Make scripts executable
      chmod +x install.sh
      chmod +x run-healthcheck.sh
3. Run installer ./install.sh

This will:

Copy project to:
~/healthcheck-runner

Create cron job:
*/15 * * * * ~/healthcheck-runner/run-healthcheck.sh

Log output to:
~/healthcheck-runner/healthcheck.log

🪟 Windows Setup (Task Scheduler)
1. Unzip project
   Expand-Archive .\auto-awesome.zip C:\temp\auto-awesome
   cd C:\temp\auto-awesome
2. Run installer (PowerShell as Admin)
      powershell -ExecutionPolicy Bypass -File .\install.ps1
3. What installer does

Copies project to:
C:\Users\<YOU>\healthcheck-runner

Creates scheduled task:
HealthCheck
Runs every 15 minutes

Executes: run-healthcheck.ps1

🧰 Requirements
macOS
Java (JDK 8+ / 11+ / 17+)
Maven (mvn)
cron enabled (default on macOS)
Windows
Java (JDK installed)
Maven added to PATH
PowerShell 5+

📄 Logs
macOS
~/healthcheck-runner/healthcheck.log
Windows
C:\Users\<YOU>\healthcheck-runner\healthcheck.log

