package com.example.moneymap.automation.listeners;

import com.example.moneymap.automation.model.TestCase;
import com.example.moneymap.automation.tests.BaseTest;
import com.example.moneymap.automation.utils.AppiumDriverFactory;
import com.example.moneymap.automation.utils.LogUtil;
import com.example.moneymap.automation.utils.ScreenshotUtil;
import io.appium.java_client.android.AndroidDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * TestNG listener for the MoneyMap E2E automation suite.
 * Captures screenshots, ADB device logs, and stack traces on test failure.
 * Updates the test case registry with PASSED/FAILED/SKIPPED status and timing.
 */
public class TestNGListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        TestCase tc = extractTestCase(result);
        if (tc != null) {
            LogUtil.log("▶ START [" + tc.getTestId() + "] " + tc.getName());
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        TestCase tc = extractTestCase(result);
        long duration = result.getEndMillis() - result.getStartMillis();
        if (tc != null) {
            BaseTest.updateTestCase(tc.getTestId(), "PASSED", "Executed successfully.", duration, "", "");
            LogUtil.log(String.format("✓ PASSED [%s] in %dms", tc.getTestId(), duration));
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        TestCase tc = extractTestCase(result);
        long duration = result.getEndMillis() - result.getStartMillis();

        // Build stack trace string
        Throwable throwable = result.getThrowable();
        String stackTrace = "";
        String errorMessage = "Unknown error";
        if (throwable != null) {
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            stackTrace = sw.toString();
            errorMessage = throwable.getMessage() != null ? throwable.getMessage() : throwable.getClass().getSimpleName();
        }

        // Capture failure evidence using the thread-local driver
        String screenshotPath = "";
        String deviceLogPath = "";
        try {
            AndroidDriver driver = AppiumDriverFactory.getDriver();
            if (driver != null) {
                String testId = tc != null ? tc.getTestId() : "UnknownTC";
                screenshotPath = ScreenshotUtil.captureScreenshot(driver, testId);
                deviceLogPath = LogUtil.captureDeviceLogs(driver, testId);
            }
        } catch (Exception e) {
            LogUtil.log("Evidence capture failed (driver may be null): " + e.getMessage());
        }

        // Build enriched failure reason including device info
        String deviceInfo = getDeviceInfo();
        String fullFailureReason = errorMessage + "\n\n" +
            "Device: " + deviceInfo + "\n\n" +
            "Stack Trace:\n" + stackTrace;

        if (tc != null) {
            BaseTest.updateTestCase(tc.getTestId(), "FAILED", fullFailureReason, duration,
                screenshotPath, deviceLogPath);
            LogUtil.log(String.format("✗ FAILED [%s] in %dms — %s", tc.getTestId(), duration, errorMessage));
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        TestCase tc = extractTestCase(result);
        long duration = result.getEndMillis() - result.getStartMillis();
        if (tc != null) {
            BaseTest.updateTestCase(tc.getTestId(), "SKIPPED", "Test skipped by TestNG runner.", duration, "", "");
            LogUtil.log("⚠ SKIPPED [" + tc.getTestId() + "]");
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // Not used — all failures are critical
    }

    @Override
    public void onStart(ITestContext context) {
        LogUtil.log("Test context started: " + context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        LogUtil.log(String.format(
            "Test context finished: %s — Passed: %d | Failed: %d | Skipped: %d",
            context.getName(),
            context.getPassedTests().size(),
            context.getFailedTests().size(),
            context.getSkippedTests().size()
        ));
    }

    // ── Internal Helpers ───────────────────────────────────────────────────

    private TestCase extractTestCase(ITestResult result) {
        Object[] params = result.getParameters();
        if (params != null && params.length > 0 && params[0] instanceof TestCase) {
            return (TestCase) params[0];
        }
        return null;
    }

    /**
     * Returns a human-readable device info string for failure reports.
     * Uses the ThreadLocal driver to avoid cross-thread driver access.
     */
    private String getDeviceInfo() {
        try {
            AndroidDriver driver = AppiumDriverFactory.getDriver();
            if (driver != null) {
                String platformVersion = (String) driver.getCapabilities().getCapability("platformVersion");
                String deviceName = (String) driver.getCapabilities().getCapability("deviceName");
                return String.format("%s (Android %s)",
                    deviceName != null ? deviceName : "Android Emulator",
                    platformVersion != null ? platformVersion : "Unknown");
            }
        } catch (Exception ignored) {}
        return "Android Emulator (info unavailable)";
    }
}
