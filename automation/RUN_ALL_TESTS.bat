@echo off
title MoneyMap - Run ALL Tests
echo ============================================
echo  MoneyMap Appium Tests - ALL Suites
echo ============================================
echo.
echo BEFORE RUNNING ensure:
echo  1. Backend running:  cd backend ^&^& npm run start:dev
echo  2. Appium running:   double-click START_APPIUM.bat
echo  3. Phone connected:  USB Debugging ON
echo.
pause

cd automation
echo.
echo Running ALL Suites...
echo.
maven\apache-maven-3.9.9\bin\mvn.cmd test -DsuiteXmlFile=src/test/resources/testng.xml

echo.
echo ============================================
echo  Done! Open: automation\Test Results\HTML\execution-report.html
echo ============================================
pause
