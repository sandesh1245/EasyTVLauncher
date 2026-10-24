@echo off
setlocal
echo ===================================================
echo   Starting Easy TV Launcher on Android TV Emulator
echo ===================================================

set SDK_DIR=%LOCALAPPDATA%\Android\Sdk
set EMULATOR_EXE=%SDK_DIR%\emulator\emulator.exe
set ADB_EXE=%SDK_DIR%\platform-tools\adb.exe
set APK_PATH=%~dp0app\build\outputs\apk\debug\app-debug.apk

if not exist "%EMULATOR_EXE%" (
    echo [ERROR] Emulator not found at: %EMULATOR_EXE%
    pause
    exit /b 1
)

if not exist "%APK_PATH%" (
    echo [ERROR] APK not found at: %APK_PATH%
    echo Please build the APK first with gradlew assembleDebug
    pause
    exit /b 1
)

echo [1/3] Launching Android TV Emulator (SignageTV)...
start "" "%EMULATOR_EXE%" -avd SignageTV

echo [2/3] Waiting for emulator to boot up...
"%ADB_EXE%" wait-for-device

:check_boot
for /f "tokens=*" %%i in ('"%ADB_EXE%" shell getprop sys.boot_completed 2^>nul') do set BOOT_STATE=%%i
if not "%BOOT_STATE%"=="1" (
    timeout /t 3 /nobreak >nul
    goto check_boot
)

echo.
echo [3/3] Emulator is ready! Installing Easy TV Launcher...
"%ADB_EXE%" install -r "%APK_PATH%"

echo.
echo Launching Easy TV Launcher...
"%ADB_EXE%" shell monkey -p com.easytv.launcher -c android.intent.category.LAUNCHER 1

echo.
echo ===================================================
echo   Easy TV Launcher is now running on your emulator!
echo ===================================================
pause
