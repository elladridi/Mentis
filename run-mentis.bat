@echo off
REM MENTIS Application Startup Script
REM Sets up Java environment and runs the application

echo ======================================
echo MENTIS - Mental Health Management App
echo ======================================
echo.

REM Set Java Home
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Verifying Java installation...
java -version

echo.
echo Starting MENTIS application...
echo.

cd /d "C:\Users\User\Desktop\JAVA\Mentis"

set "MAVEN_REPO_LOCAL=%CD%\.m2\repository"
if not exist "%MAVEN_REPO_LOCAL%" mkdir "%MAVEN_REPO_LOCAL%"

REM Run Maven with JavaFX
mvn -Dmaven.repo.local="%MAVEN_REPO_LOCAL%" clean javafx:run

pause
