package com.example.moneymap.automation.listeners;

import com.example.moneymap.automation.model.TestCase;
import com.example.moneymap.automation.tests.BaseTest;
import com.example.moneymap.automation.utils.AppiumDriverFactory;
import com.example.moneymap.automation.utils.LogUtil;
import com.example.moneymap.automation.utils.ScreenshotUtil;
import io.appium.java_client.android.AndroidDriver;
import org.testng.*;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * TestNGListener - Captures pass/fail/skip status, screenshots, and logs for every test.
 */
public class TestNGListener implements ITestListener, ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
        LogUtil.log("==========================================");
        LogUtil.log("  Suite starting: " + suite.getName());
        LogUtil.log("==========================================");
    }

    @Override
    public void onFinish(ISuite suite) {
        LogUtil.log("==========================================");
        LogUtil.log("  Suite finished: " + suite.getName());
        LogUtil.log("==========================================");
    }

    @Override
    public void onTestStart(ITestResult result) {
        TestCase tc = extractTestCase(result);
        if (tc != null) {
            LogUtil.logTestStart(tc.getTestId(), tc.getName());
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        TestCase tc = extractTestCase(result);
        long duration = result.getEndMillis() - result.getStartMillis();
        if (tc != null) {
            BaseTest.updateTestCase(tc.getTestId(), "PASSED",
                    "Executed successfully.", duration, "", "");
            LogUtil.logTestPass(tc.getTestId(), duration);
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        TestCase tc = extractTestCase(result);
        long duration = result.getEndMillis() - result.getStartMillis();

        // Build failure message
        Throwable throwable = result.getThrowable();
        String errorMessage = throwable != null ? throwable.getMessage() : "Unknown failure";
        StringWriter sw = new StringWriter();
        if (throwable != null) throwable.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();
        String fullReason = errorMessage + (stackTrace.isEmpty() ? "" : "\n\nStack Trace:\n" + stackTrace);

        // Capture artifacts
        String screenshotPath = "";
        String deviceLogPath = "";
        String pageSourcePath = "";
        String appiumLogPath = "";
        String locatorUsed = "";
        String activity = "";
        String pkg = "";

        try {
            AndroidDriver driver = AppiumDriverFactory.getDriver();
            if (driver != null && AppiumDriverFactory.isDriverAlive()) {
                String testId = tc != null ? tc.getTestId() : "UNKNOWN";
                screenshotPath = ScreenshotUtil.captureScreenshot(driver, testId);
                deviceLogPath = LogUtil.captureDeviceLogs(driver, testId);
                appiumLogPath = LogUtil.captureAppiumLogs(driver, testId);

                try {
                    activity = driver.currentActivity();
                    pkg = driver.getCurrentPackage();
                } catch (Exception ignored) {}

                try {
                    String pageSource = driver.getPageSource();
                    String logDir = "reports/logs/";
                    if (new java.io.File("automation").exists()) {
                        logDir = "automation/reports/logs/";
                    }
                    java.io.File dir = new java.io.File(logDir);
                    if (!dir.exists()) dir.mkdirs();

                    java.io.File sourceFile = new java.io.File(dir, testId + "_pagesource.xml");
                    try (java.io.FileWriter fw = new java.io.FileWriter(sourceFile)) {
                        fw.write(pageSource);
                    }
                    pageSourcePath = "logs/" + sourceFile.getName();
                } catch (Exception ignored) {}

                if (throwable != null) {
                    String msg = throwable.getMessage();
                    if (msg != null) {
                        if (msg.contains("By.")) {
                            int idx = msg.indexOf("By.");
                            int end = msg.indexOf(" ", idx);
                            if (end == -1) end = msg.length();
                            locatorUsed = msg.substring(idx, end);
                        } else if (msg.contains("locator:")) {
                            int idx = msg.indexOf("locator:");
                            int end = msg.indexOf("\n", idx);
                            if (end == -1) end = msg.length();
                            locatorUsed = msg.substring(idx, end).trim();
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.logError("Listener failed to capture artifacts", e);
        }

        if (tc != null) {
            BaseTest.updateTestCase(tc.getTestId(), "FAILED",
                    fullReason, duration, screenshotPath, deviceLogPath,
                    pageSourcePath, appiumLogPath, locatorUsed, activity, pkg);
            LogUtil.logTestFail(tc.getTestId(), errorMessage, duration);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        TestCase tc = extractTestCase(result);
        long duration = result.getEndMillis() - result.getStartMillis();
        if (tc != null) {
            String reason = result.getThrowable() != null
                    ? result.getThrowable().getMessage() : "Skipped by TestNG";
            BaseTest.updateTestCase(tc.getTestId(), "SKIPPED", reason, duration, "", "");
            LogUtil.logTestSkip(tc.getTestId());
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        onTestFailure(result);
    }

    @Override
    public void onStart(ITestContext context) {
        LogUtil.log("Test context starting: " + context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        LogUtil.log(String.format("Test context finished: %s | Passed: %d | Failed: %d | Skipped: %d",
                context.getName(),
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size()));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private TestCase extractTestCase(ITestResult result) {
        Object[] params = result.getParameters();
        if (params != null && params.length > 0 && params[0] instanceof TestCase) {
            return (TestCase) params[0];
        }
        return null;
    }
}
