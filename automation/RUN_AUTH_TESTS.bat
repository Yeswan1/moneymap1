@echo off
title MoneyMap - Run Auth Tests
echo ============================================
echo  MoneyMap Appium Tests - Auth Suite
echo ============================================
echo.
echo STEP 1: Make sure backend is running  (cd backend ^&^& npm run start:dev)
echo STEP 2: Make sure Appium is running   (double-click START_APPIUM.bat)
echo STEP 3: Make sure phone is connected  (USB Debugging ON)
echo.
pause

cd automation
echo.
echo Running Auth Suite...
echo.
maven\apache-maven-3.9.9\bin\mvn.cmd test -DsuiteXmlFile=src/test/resources/testng-auth.xml

echo.
echo ============================================
echo  Done! Open Test Results\HTML\execution-report.html
echo ============================================
pause
