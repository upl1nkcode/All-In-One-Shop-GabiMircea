# Installing Maven Wrapper
Write-Host "Installing Maven Wrapper to backend project..." -ForegroundColor Green

# Check if Maven is installed
$mavenInstalled = Get-Command mvn -ErrorAction SilentlyContinue

if ($null -eq $mavenInstalled) {
    Write-Host ""
    Write-Host "Maven is not installed on your system." -ForegroundColor Yellow
    Write-Host "You have two options:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Option 1: Install Maven" -ForegroundColor Cyan
    Write-Host "  1. Download Maven from: https://maven.apache.org/download.cgi"
    Write-Host "  2. Extract to C:\Program Files\Apache\maven"
    Write-Host "  3. Add to PATH: C:\Program Files\Apache\maven\bin"
    Write-Host "  4. Restart PowerShell and run this script again"
    Write-Host ""
    Write-Host "Option 2: Install via Chocolatey (if you have it)" -ForegroundColor Cyan
    Write-Host "  Run: choco install maven"
    Write-Host ""
    Write-Host "After installing Maven, re-run this script to generate the Maven Wrapper."
    Write-Host ""
    
    $response = Read-Host "Would you like to continue anyway and try to download Maven Wrapper manually? (y/n)"
    
    if ($response -ne "y") {
        exit
    }
    
    # Download maven wrapper jar manually
    Write-Host "Downloading Maven Wrapper files manually..." -ForegroundColor Yellow
    
    $wrapperJarUrl = "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
    $wrapperPropertiesUrl = "https://raw.githubusercontent.com/takari/maven-wrapper/master/maven-wrapper.properties"
    
    New-Item -Path "backend\.mvn\wrapper" -ItemType Directory -Force | Out-Null
    
    Write-Host "Downloading maven-wrapper.jar..."
    Invoke-WebRequest -Uri $wrapperJarUrl -OutFile "backend\.mvn\wrapper\maven-wrapper.jar"
    
    Write-Host "Creating wrapper files..."
    
    # Create mvnw.cmd
    @"
@REM Maven Wrapper Script
@REM
@echo off

@REM Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set MAVEN_CMD_LINE_ARGS=%*

set MAVEN_PROJECTBASEDIR=%~dp0
set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

java -jar %WRAPPER_JAR% %MAVEN_CMD_LINE_ARGS%

if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@REM End of local scope
if "%OS%"=="Windows_NT" endlocal

exit /B %ERROR_CODE%
"@ | Out-File -FilePath "backend\mvnw.cmd" -Encoding ASCII
    
    Write-Host "Maven Wrapper installed successfully!" -ForegroundColor Green
    
} else {
    # Maven is installed, generate wrapper properly
    Set-Location backend
    mvn -N wrapper:wrapper
    Set-Location ..
    Write-Host "Maven Wrapper generated successfully!" -ForegroundColor Green
}

Write-Host ""
Write-Host "You can now run the backend with:" -ForegroundColor Cyan
Write-Host "  .\backend\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev" -ForegroundColor White
