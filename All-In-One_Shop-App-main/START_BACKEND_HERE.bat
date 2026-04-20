@echo off
echo ========================================
echo Starting AllInOne Shop Backend Server
echo ========================================
echo.

REM Set JAVA_HOME to use Java 17
set "JAVA_HOME=C:\Users\Admin\.jdks\openjdk-17"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Using Java version:
java -version
echo.

cd /d "%~dp0backend"

echo Checking Maven Wrapper...
if not exist ".mvn\wrapper\maven-wrapper.jar" (
    echo Downloading Maven Wrapper...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; New-Item -ItemType Directory -Force -Path '.mvn\wrapper' | Out-Null; Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar' -OutFile '.mvn\wrapper\maven-wrapper.jar'"
)

echo.
echo Starting Spring Boot application with DEV profile...
echo API will be available at: http://localhost:8080/api
echo Press Ctrl+C to stop the server
echo.

REM Download Maven if not present
if not exist ".mvn\wrapper\apache-maven-3.9.6" (
    echo Downloading and extracting Maven...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile '.mvn\maven.zip'; Expand-Archive -Path '.mvn\maven.zip' -DestinationPath '.mvn\wrapper' -Force; Remove-Item '.mvn\maven.zip'"
    echo Maven downloaded successfully!
)

REM Use the downloaded Maven
set "MAVEN_HOME=%CD%\.mvn\wrapper\apache-maven-3.9.6"
set "PATH=%MAVEN_HOME%\bin;%PATH%"

echo Using Maven from: %MAVEN_HOME%
echo.

REM Run Spring Boot
call "%MAVEN_HOME%\bin\mvn.cmd" spring-boot:run -Dspring-boot.run.profiles=dev

pause
