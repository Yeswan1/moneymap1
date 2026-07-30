@echo off
title MoneyMap - Run Settings Tests
cd automation
echo Running Settings Suite...
maven\apache-maven-3.9.9\bin\mvn.cmd test -DsuiteXmlFile=src/test/resources/testng-settings.xml
pause
