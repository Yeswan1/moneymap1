@echo off
title Appium Server - MoneyMap
echo ============================================
echo  Starting Appium Server on port 4723
echo ============================================
echo.
echo Device: I2214 (Android 16)
echo UDID: adb-10BD4127600007X-pRQG3j._adb-tls-connect._tcp
echo.

:: Set Android SDK env vars (required by Appium UiAutomator2 driver)
set "ANDROID_HOME=C:\Users\surya\AppData\Local\Android\Sdk"
set "ANDROID_SDK_ROOT=C:\Users\surya\AppData\Local\Android\Sdk"

echo ANDROID_HOME=%ANDROID_HOME%
echo.

"%APPDATA%\npm\appium.cmd" --port 4723 --log-level info
pause
