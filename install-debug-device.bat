@echo off
setlocal

rem Build the debug APK and install it on a connected Android device, then launch the app.
rem Requirements: USB debugging enabled on the device and the PC trusts it (adb devices shows "device").

set "PROJECT_DIR=%~dp0"
set "APP_ID=com.oken.albii"
set "ACTIVITY=.MainActivity"

if not defined JAVA_HOME set "JAVA_HOME=%USERPROFILE%\dev\jdk-17.0.20.1+1"
if not defined ANDROID_HOME set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
set "ADB=%ANDROID_HOME%\platform-tools\adb.exe"

if not exist "%ADB%" (
    echo [ERROR] adb not found: %ADB%
    echo Install platform-tools or set ANDROID_HOME.
    exit /b 1
)

echo === Checking connected devices ===
"%ADB%" start-server >nul 2>&1
"%ADB%" devices
set "DEVICE_COUNT=0"
for /f "skip=1 tokens=1,2" %%a in ('"%ADB%" devices') do (
    if "%%b"=="device" set /a DEVICE_COUNT+=1
)
if "%DEVICE_COUNT%"=="0" (
    echo [ERROR] No authorized device found. Enable USB debugging and allow this PC on the device.
    exit /b 1
)

echo === Building and installing debug APK ===
pushd "%PROJECT_DIR%"
call gradlew.bat installDebug
set "RESULT=%ERRORLEVEL%"
popd
if not "%RESULT%"=="0" (
    echo [ERROR] Build or install failed. See the messages above.
    exit /b %RESULT%
)

echo === Launching app ===
"%ADB%" shell am start -n %APP_ID%/%ACTIVITY%
if errorlevel 1 (
    echo [WARN] Install succeeded but launch failed. Open the app manually.
    exit /b 1
)

echo [OK] Installed and launched %APP_ID%
endlocal
exit /b 0
