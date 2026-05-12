# MENTIS Application Startup Script for PowerShell
# Sets up Java environment and runs the application

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "MENTIS - Mental Health Management App" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Prefer existing JAVA_HOME, otherwise use java/mvn already available on PATH
if ($env:JAVA_HOME -and -not (Test-Path (Join-Path $env:JAVA_HOME "bin\\java.exe"))) {
    Remove-Item Env:JAVA_HOME
}

if (-not $env:JAVA_HOME) {
    $javaCommand = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCommand) {
        $env:JAVA_HOME = Split-Path (Split-Path $javaCommand.Source -Parent) -Parent
    }
}

if ($env:JAVA_HOME) {
    $env:Path = "$env:JAVA_HOME\bin;" + $env:Path
}

Write-Host "Verifying Java installation..." -ForegroundColor Yellow
java -version
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Java was not found. Install JDK 17+ and set JAVA_HOME or add java to PATH." -ForegroundColor Red
    exit 1
}

Get-Command mvn -ErrorAction SilentlyContinue | Out-Null
if (-not $?) {
    Write-Host "ERROR: Maven was not found. Install Maven and add mvn to PATH." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Starting MENTIS application..." -ForegroundColor Green
Write-Host ""

Set-Location "C:\Users\User\Desktop\JAVA\Mentis"

$mavenRepoLocal = Join-Path (Get-Location) ".m2\repository"
New-Item -ItemType Directory -Force -Path $mavenRepoLocal | Out-Null

# Run Maven with JavaFX
mvn "-Dmaven.repo.local=$mavenRepoLocal" clean javafx:run
