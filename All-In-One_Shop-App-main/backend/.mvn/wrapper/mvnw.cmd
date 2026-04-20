@REM Maven Wrapper startup script for Windows
@REM
@echo off

@REM Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set ERROR_CODE=0

@REM Required ENV vars:
@REM JAVA_HOME - location of a JDK home dir

@REM Optional ENV vars:
@REM M2_HOME - location of maven2's installed home dir
@REM MAVEN_BATCH_ECHO - set to 'on' to enable the echoing of the batch commands
@REM MAVEN_BATCH_PAUSE - set to 'on' to wait for a key stroke before ending
@REM MAVEN_OPTS - parameters passed to the Java VM when running Maven
@REM     e.g. to debug Maven itself, use
@REM set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000

if not "%MAVEN_BATCH_ECHO%" == "on"  echo off
@REM set %HOME% to equivalent of $HOME
if "%HOME%" == "" (set "HOME=%HOMEDRIVE%%HOMEPATH%")

@REM Execute a user defined script before this one
if not "%MAVEN_SKIP_RC%" == "" goto skipRcPre
@REM check for pre script, once with legacy .bat ending and once with .cmd ending
if exist "%HOME%\mavenrc_pre.bat" call "%HOME%\mavenrc_pre.bat"
if exist "%HOME%\mavenrc_pre.cmd" call "%HOME%\mavenrc_pre.cmd"
:skipRcPre

@setlocal

set MAVEN_PROJECTBASEDIR=%~dp0..

@REM Find base directory
IF NOT DEFINED MAVEN_PROJECTBASEDIR goto endDetectBaseDir

set WDIR=%MAVEN_PROJECTBASEDIR%
:findBaseDir
IF EXIST "%WDIR%\pom.xml" GOTO baseDirFound
cd ..
IF "%WDIR%"=="%CD%" GOTO baseDirNotFound
set WDIR=%CD%
GOTO findBaseDir

:baseDirFound
set MAVEN_PROJECTBASEDIR=%WDIR%
cd "%MAVEN_PROJECTBASEDIR%"
GOTO endDetectBaseDir

:baseDirNotFound
set MAVEN_PROJECTBASEDIR=%CD%
cd "%MAVEN_PROJECTBASEDIR%"

:endDetectBaseDir

IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" goto downloadWrapper

set MAVEN_WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"

set MAVEN_WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar

FOR /F "tokens=1,2 delims==" %%A IN (%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties) DO (
    IF "%%A"=="wrapperUrl" SET MAVEN_WRAPPER_URL=%%B
)

:downloadWrapper
echo Downloading Maven Wrapper...
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%MAVEN_WRAPPER_URL%' -OutFile '%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar'}"

:executeWrapper
@REM Setup the command line
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

%JAVA_HOME%\bin\java.exe %MAVEN_OPTS% -classpath %MAVEN_WRAPPER_JAR% "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" %WRAPPER_LAUNCHER% %*
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%

if not "%MAVEN_BATCH_PAUSE%" == "on" goto endPause
pause
:endPause
if "%MAVEN_BATCH_ECHO%" == "on" echo on

cmd /C exit /B %ERROR_CODE%
