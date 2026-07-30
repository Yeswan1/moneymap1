package com.example.moneymap.automation.listeners;

import com.example.moneymap.automation.model.TestCase;
import com.example.moneymap.automation.tests.BaseTest;
import com.example.moneymap.automation.utils.LogUtil;
import com.example.moneymap.automation.utils.ScreenshotUtil;
import io.appium.java_client.android.AndroidDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.ISuiteListener;
import org.testng.ISuite;

/**
 * TestNGListener — auto-captures screenshots and device logs on test failure.
 *
 * Registered in every testng*.xml suite file via:
 *   <listeners>
 *     <listener class-name="com.example.moneymap.automation.listeners.TestNGListener"/>
 *   </listeners>
 */
public class TestNGListener implements ITestListener, ISuiteListener {

    // ── ITestListener ──────────────────────────────────────────────────────────

    @Override
    public void onTestFailure(ITestResult result) {
        String testId = extractTestId(result);
        long durationMs = result.getEndMillis() - result.getStartMillis();
        String failureReason = extractFailureReason(result);

        LogUtil.logTestFail(testId, failureReason, durationMs);

        // Obtain the AndroidDriver from the test instance
        AndroidDriver driver = extractDriver(result);

        if (driver != null) {
            String screenshotPath = ScreenshotUtil.captureScreenshot(driver, testId);
            String logPath        = LogUtil.captureDeviceLogs(driver, testId);
            String appiumLogPath  = LogUtil.captureAppiumLogs(driver, testId);

            // Persist all paths against the test case record
            BaseTest.updateTestCase(
                testId,
                "FAILED",
                failureReason,
                durationMs,
                screenshotPath,
                logPath,
                "",              // pageSourcePath — captured by BasePage.captureDiagnostics
                appiumLogPath,
                "",              // locatorUsed
                safeCurrentActivity(driver),
                safeCurrentPackage(driver)
            );
        } else {
            // No driver — just record the failure reason
            BaseTest.updateTestCase(testId, "FAILED", failureReason, durationMs, "", "", "", "", "", "", "");
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testId = extractTestId(result);
        long durationMs = result.getEndMillis() - result.getStartMillis();
        BaseTest.updateTestCase(testId, "SKIPPED", "Test skipped", durationMs, "", "");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testId = extractTestId(result);
        long durationMs = result.getEndMillis() - result.getStartMillis();
        LogUtil.logTestPass(testId, durationMs);
        BaseTest.updateTestCase(testId, "PASSED", "Test passed", durationMs, "", "");
    }

    // ── ISuiteListener ─────────────────────────────────────────────────────────

    @Override
    public void onStart(ISuite suite) {
        LogUtil.log("Suite starting: " + suite.getName());
    }

    @Override
    public void onFinish(ISuite suite) {
        LogUtil.log("Suite finished: " + suite.getName());
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private String extractTestId(ITestResult result) {
        // If the first parameter is a TestCase, use its testId
        Object[] params = result.getParameters();
        if (params != null && params.length > 0 && params[0] instanceof TestCase) {
            return ((TestCase) params[0]).getTestId();
        }
        return result.getName();
    }

    private String extractFailureReason(ITestResult result) {
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            String msg = throwable.getMessage();
            return msg != null ? msg : throwable.getClass().getSimpleName();
        }
        return "Unknown failure";
    }

    private AndroidDriver extractDriver(ITestResult result) {
        try {
            Object instance = result.getInstance();
            if (instance instanceof BaseTest) {
                return ((BaseTest) instance).driver;
            }
        } catch (Exception e) {
            LogUtil.logWarning("Could not extract driver from test instance: " + e.getMessage());
        }
        return null;
    }

    private String safeCurrentActivity(AndroidDriver driver) {
        try {
            return driver.currentActivity();
        } catch (Exception e) {
            return "";
        }
    }

    private String safeCurrentPackage(AndroidDriver driver) {
        try {
            return driver.getCurrentPackage();
        } catch (Exception e) {
            return "";
        }
    }
}
