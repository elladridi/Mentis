# MENTIS Application Startup Script for PowerShell
# Sets up Java environment and runs the application

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "MENTIS - Mental Health Management App" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Set Java Home
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

Write-Host "Verifying Java installation..." -ForegroundColor Yellow
java -version

Write-Host ""
Write-Host "Starting MENTIS application..." -ForegroundColor Green
Write-Host ""

Set-Location "C:\Users\User\Desktop\JAVA\Mentis"

$mavenRepoLocal = Join-Path (Get-Location) ".m2\repository"
New-Item -ItemType Directory -Force -Path $mavenRepoLocal | Out-Null

# Run Maven with JavaFX
mvn "-Dmaven.repo.local=$mavenRepoLocal" clean javafx:run
