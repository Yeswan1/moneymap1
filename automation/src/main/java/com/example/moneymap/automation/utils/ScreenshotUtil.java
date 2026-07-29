package com.example.moneymap.automation.utils;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Screenshot utility for capturing failure evidence during Appium test execution.
 * All paths are relative to the working directory for CI compatibility.
 */
public class ScreenshotUtil {

    // Relative path — works from both automation/ (local) and root (CI)
    private static final String SCREENSHOT_BASE_DIR = "reports/screenshots";

    /**
     * Captures a PNG screenshot of the current device screen.
     *
     * @param driver The active AndroidDriver session
     * @param testId The test case ID used for the filename
     * @return Relative path to the saved screenshot, or empty string on failure
     */
    public static String captureScreenshot(AndroidDriver driver, String testId) {
        if (driver == null) {
            LogUtil.log("Screenshot skipped — driver is null for: " + testId);
            return "";
        }
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
            // Sanitise testId to be a valid filename component
            String safeTestId = testId.replaceAll("[^a-zA-Z0-9_\\-]", "_");
            String fileName = safeTestId + "_" + timestamp + ".png";

            // Resolve output directory relative to execution context
            String baseDir = resolveScreenshotDir();
            File destDir = new File(baseDir);
            destDir.mkdirs();

            File destFile = new File(destDir, fileName);
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            String relativePath = baseDir + "/" + fileName;
            LogUtil.log("Screenshot captured: " + relativePath);
            return relativePath;
        } catch (Exception e) {
            LogUtil.logError("Failed to capture screenshot for " + testId + ": " + e.getMessage(), e);
            return "";
        }
    }

    /**
     * Captures a full-device screenshot using ADB (bypasses app FLAG_SECURE restrictions).
     *
     * @param testId The test case ID for naming
     * @return Relative path to the saved screenshot, or empty string on failure
     */
    public static String captureAdbScreenshot(String testId) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
            String safeTestId = testId.replaceAll("[^a-zA-Z0-9_\\-]", "_");
            String localPath = resolveScreenshotDir() + "/" + safeTestId + "_adb_" + timestamp + ".png";

            new File(resolveScreenshotDir()).mkdirs();

            ProcessBuilder pb = new ProcessBuilder(
                "adb", "exec-out", "screencap", "-p"
            );
            pb.redirectOutput(new File(localPath));
            Process process = pb.start();
            process.waitFor();

            LogUtil.log("ADB screenshot saved: " + localPath);
            return localPath;
        } catch (Exception e) {
            LogUtil.logError("ADB screenshot failed for " + testId + ": " + e.getMessage(), e);
            return "";
        }
    }

    private static String resolveScreenshotDir() {
        // When running from root (CI): use automation/reports/screenshots
        if (new File("automation").exists()) {
            return "automation/" + SCREENSHOT_BASE_DIR;
        }
        // When running from automation/ (local mvn): use reports/screenshots
        return SCREENSHOT_BASE_DIR;
    }
}
