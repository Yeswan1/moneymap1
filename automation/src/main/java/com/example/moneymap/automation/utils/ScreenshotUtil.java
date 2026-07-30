package com.example.moneymap.automation.utils;

import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ScreenshotUtil — captures PNG screenshots on test failure.
 *
 * Files are saved to:
 *   automation/reports/screenshots/<prefix>_<yyyyMMdd_HHmmss_SSS>.png
 * or
 *   reports/screenshots/<prefix>_<yyyyMMdd_HHmmss_SSS>.png
 * depending on the working directory.
 */
public class ScreenshotUtil {

    private static final String TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss_SSS";

    /**
     * Captures a PNG screenshot from the given AndroidDriver and saves it with
     * a timestamped filename.
     *
     * @param driver active AndroidDriver session (null-safe — returns "" if null)
     * @param prefix typically the testId, e.g. "TC_AUTH_001"
     * @return relative path to the saved screenshot file, or "" on failure
     */
    public static String captureScreenshot(AndroidDriver driver, String prefix) {
        if (driver == null) {
            LogUtil.logWarning("ScreenshotUtil: driver is null, skipping screenshot for " + prefix);
            return "";
        }
        try {
            String timestamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
            String safePrefix = (prefix != null && !prefix.isEmpty()) ? prefix : "unknown";
            String fileName = safePrefix + "_" + timestamp + ".png";

            File screenshotDir = resolveScreenshotDir();
            screenshotDir.mkdirs();

            File destFile = new File(screenshotDir, fileName);
            File srcFile = driver.getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(srcFile, destFile);

            String relativePath = screenshotDir.getPath() + File.separator + fileName;
            LogUtil.log("Screenshot saved: " + relativePath);
            return relativePath;
        } catch (Exception e) {
            LogUtil.logError("ScreenshotUtil: failed to capture screenshot for " + prefix, e);
            return "";
        }
    }

    // ── Path resolution ──────────────────────────────────────────────────────

    private static File resolveScreenshotDir() {
        if (new File("automation").isDirectory()) {
            return new File("automation/reports/screenshots");
        }
        return new File("reports/screenshots");
    }
}
