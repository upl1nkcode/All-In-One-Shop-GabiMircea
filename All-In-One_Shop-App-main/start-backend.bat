@echo off
echo Starting AllInOne Shop Backend Server...
echo.
echo Using Development Profile (H2 In-Memory Database)
echo API will be available at: http://localhost:8080/api
echo.

cd /d "%~dp0backend"

echo Checking for Maven...
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Maven is not installed or not in PATH
    echo Please install Maven or use Maven Wrapper
    pause
    exit /b 1
)

echo.
echo Starting Spring Boot application...
echo.

mvn spring-boot:run -Dspring-boot.run.profiles=dev

pause
