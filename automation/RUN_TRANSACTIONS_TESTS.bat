@echo off
title MoneyMap - Run Transactions Tests
cd automation
echo Running Transactions Suite...
maven\apache-maven-3.9.9\bin\mvn.cmd test -DsuiteXmlFile=src/test/resources/testng-transactions.xml
pause
