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
        try {
            AndroidDriver driver = AppiumDriverFactory.getDriver();
            if (driver != null && AppiumDriverFactory.isDriverAlive()) {
                String testId = tc != null ? tc.getTestId() : "UNKNOWN";
                screenshotPath = ScreenshotUtil.captureScreenshot(driver, testId);
                deviceLogPath = LogUtil.captureDeviceLogs(driver, testId);
            }
        } catch (Exception e) {
            LogUtil.logError("Listener failed to capture artifacts", e);
        }

        if (tc != null) {
            BaseTest.updateTestCase(tc.getTestId(), "FAILED",
                    fullReason, duration, screenshotPath, deviceLogPath);
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
