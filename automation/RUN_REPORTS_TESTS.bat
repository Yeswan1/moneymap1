@echo off
title MoneyMap - Run Reports Tests
cd automation
echo Running Reports Suite...
maven\apache-maven-3.9.9\bin\mvn.cmd test -DsuiteXmlFile=src/test/resources/testng-reports.xml
pause
