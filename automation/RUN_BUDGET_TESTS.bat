@echo off
title MoneyMap - Run Budget Tests
cd automation
echo Running Budget Suite...
maven\apache-maven-3.9.9\bin\mvn.cmd test -DsuiteXmlFile=src/test/resources/testng-budget.xml
pause
