@echo off
title MoneyMap - Run Dashboard Tests
cd automation
echo Running Dashboard Suite...
maven\apache-maven-3.9.9\bin\mvn.cmd test -DsuiteXmlFile=src/test/resources/testng-dashboard.xml
pause
