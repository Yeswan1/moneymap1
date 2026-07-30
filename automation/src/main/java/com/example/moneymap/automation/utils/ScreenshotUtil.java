package com.example.moneymap.automation.utils;

import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ScreenshotUtil - Captures screenshots and saves them to the reports directory.
 */
public class ScreenshotUtil {

    private static String getScreenshotDir() {
        // Prefer absolute test-results directories
        if (new File("automation").exists()) {
            return "automation/reports/screenshots/";
        }
        return "reports/screenshots/";
    }

    /**
     * Capture a screenshot and save to the reports/screenshots directory.
     * @param driver the AndroidDriver instance
     * @param testCaseId used to name the file
     * @return relative path to the screenshot (from the reports root)
     */
    public static String captureScreenshot(AndroidDriver driver, String testCaseId) {
        if (driver == null) return "";
        try {
            String screenshotDir = getScreenshotDir();
            File dir = new File(screenshotDir);
            if (!dir.exists()) dir.mkdirs();

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = testCaseId + "_" + timestamp + ".png";
            File destFile = new File(dir, fileName);

            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(srcFile, destFile);
            LogUtil.log("Screenshot saved: " + destFile.getAbsolutePath());
            return "screenshots/" + fileName;  // relative from reports/
        } catch (Exception e) {
            LogUtil.logError("Screenshot capture failed for " + testCaseId, e);
            return "";
        }
    }

    /**
     * Capture screenshot as Base64 (for embedding in HTML reports).
     */
    public static String captureBase64(AndroidDriver driver) {
        if (driver == null) return "";
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
        } catch (Exception e) {
            LogUtil.logError("Base64 screenshot capture failed", e);
            return "";
        }
    }

    /**
     * Delete all screenshots older than a given number of days.
     */
    public static void cleanOldScreenshots(int daysOld) {
        File dir = new File(getScreenshotDir());
        if (!dir.exists()) return;
        long threshold = System.currentTimeMillis() - (long) daysOld * 24 * 60 * 60 * 1000;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.lastModified() < threshold) {
                f.delete();
            }
        }
    }
}
