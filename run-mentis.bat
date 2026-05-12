@echo off
REM MENTIS Application Startup Script
REM Sets up Java environment and runs the application

echo ======================================
echo MENTIS - Mental Health Management App
echo ======================================
echo.

REM Prefer existing JAVA_HOME only if valid, otherwise infer it from java.exe on PATH
if defined JAVA_HOME (
    if not exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_HOME="
    )
)

if not defined JAVA_HOME (
    for /f "delims=" %%I in ('where java 2^>nul') do (
        set "JAVA_EXE=%%I"
        goto :java_found
    )
    goto :after_java_home
)

:java_found
for %%I in ("%JAVA_EXE%\..\..") do set "JAVA_HOME=%%~fI"

:after_java_home
if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

echo Verifying Java installation...
java -version
if errorlevel 1 (
    echo ERROR: Java was not found. Install JDK 17+ and set JAVA_HOME or add java to PATH.
    pause
    exit /b 1
)

where mvn >nul 2>&1
if errorlevel 1 (
    echo ERROR: Maven was not found. Install Maven and add mvn to PATH.
    pause
    exit /b 1
)

echo.
echo Starting MENTIS application...
echo.

cd /d "C:\Users\User\Desktop\JAVA\Mentis"

set "MAVEN_REPO_LOCAL=%CD%\.m2\repository"
if not exist "%MAVEN_REPO_LOCAL%" mkdir "%MAVEN_REPO_LOCAL%"

REM Run Maven with JavaFX
mvn -Dmaven.repo.local="%MAVEN_REPO_LOCAL%" clean javafx:run

pause
