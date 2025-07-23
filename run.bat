@echo off
REM Anime-TUI Run Script for Windows
REM Builds and runs the application with Maven

echo Building Anime-TUI...
call mvn clean package -DskipTests=false

if %ERRORLEVEL% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Starting Anime-TUI...
echo.

java -jar target\anime-tui-*.jar %*

pause
