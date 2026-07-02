Write-Host "==============================="
Write-Host "Started: $(Get-Date)"
Write-Host "===== Running Health Checks ====="

$ProjectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectDir

$logFile = "$ProjectDir\healthcheck.log"

# Ensure Maven exists
$mvn = Get-Command mvn -ErrorAction SilentlyContinue

if (-not $mvn) {
    "ERROR: Maven not found in PATH" | Out-File $logFile -Append
    Write-Host "❌ Maven not found"
    exit 1
}

Write-Host "Using Maven: $($mvn.Source)"

# Run tests and log output
& mvn clean test -DincludeTags=healthcheck *>> $logFile

if ($LASTEXITCODE -ne 0) {
    "FAILED: $(Get-Date)" | Out-File $logFile -Append
    Write-Host "❌ Healthcheck FAILED"
    exit $LASTEXITCODE
}

"PASSED: $(Get-Date)" | Out-File $logFile -Append
Write-Host "✅ Healthcheck PASSED"
Write-Host "Finished: $(Get-Date)"