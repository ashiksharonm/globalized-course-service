@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup script, version 3.2.0 (Windows)
@REM ----------------------------------------------------------------------------
@echo off
setlocal

if defined JAVA_HOME (
  set JAVACMD=%JAVA_HOME%\bin\java.exe
) else (
  where java >nul 2>&1
  if errorlevel 1 (
    echo ERROR: Java not found. Please install Java or set JAVA_HOME.
    exit /b 1
  )
  set JAVACMD=java
)

set APP_HOME=%~dp0
set WRAPPER_PROPERTIES=%APP_HOME%.mvn\wrapper\maven-wrapper.properties

for /f "tokens=2 delims==" %%A in ('findstr "distributionUrl" "%WRAPPER_PROPERTIES%"') do set DIST_URL=%%A

set MAVEN_USER_HOME=%USERPROFILE%\.m2
for %%F in ("%DIST_URL%") do set MAVEN_NAME=%%~nF
set MAVEN_HOME=%MAVEN_USER_HOME%\wrapper\%MAVEN_NAME%

if not exist "%MAVEN_HOME%" (
  mkdir "%MAVEN_USER_HOME%\wrapper" 2>nul
  set TMP_ZIP=%MAVEN_USER_HOME%\wrapper\%MAVEN_NAME%.zip
  echo Downloading Maven: %DIST_URL%
  powershell -Command "Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%TMP_ZIP%'"
  powershell -Command "Expand-Archive -Path '%TMP_ZIP%' -DestinationPath '%MAVEN_USER_HOME%\wrapper'"
  del "%TMP_ZIP%"
)

"%MAVEN_HOME%\bin\mvn.cmd" %*
