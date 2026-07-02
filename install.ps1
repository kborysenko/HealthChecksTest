param(
    [string]$InstallDir = "$env:USERPROFILE\healthcheck-runner"
)

Write-Host "Installing to $InstallDir"

# Remove previous install
if (Test-Path $InstallDir) {
    Remove-Item -Recurse -Force $InstallDir
}

# Create folder
New-Item -ItemType Directory -Path $InstallDir | Out-Null

# Copy project files (assumes script runs from ZIP extracted folder)
$SourceDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "Copying files..."

Copy-Item "$SourceDir\*" $InstallDir -Recurse -Force

Write-Host "Checking Maven..."

$mvn = Get-Command mvn -ErrorAction SilentlyContinue
if (-not $mvn) {
    Write-Host "❌ Maven not found. Install Maven and add to PATH."
    exit 1
}

Write-Host "Using Maven: $($mvn.Source)"

# Task Scheduler config (cron replacement)
$taskName = "HealthCheck"

$action = New-ScheduledTaskAction `
    -Execute "powershell.exe" `
    -Argument "-ExecutionPolicy Bypass -File `"$InstallDir\run-healthcheck.ps1`""

$trigger = New-ScheduledTaskTrigger `
    -Once -At (Get-Date).AddMinutes(1) `
    -RepetitionInterval (New-TimeSpan -Minutes 15) `
    -RepetitionDuration (New-TimeSpan -Days 3650)

# Remove existing task if exists
if (Get-ScheduledTask -TaskName $taskName -ErrorAction SilentlyContinue) {
    Unregister-ScheduledTask -TaskName $taskName -Confirm:$false
}

Register-ScheduledTask `
    -TaskName $taskName `
    -Action $action `
    -Trigger $trigger `
    -RunLevel Highest `
    -Force

Write-Host ""
Write-Host "✅ Installation completed"
Write-Host "Installed to: $InstallDir"
Write-Host "Task created: $taskName"