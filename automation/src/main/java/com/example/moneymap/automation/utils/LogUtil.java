package com.example.moneymap.automation.utils;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.logging.LogEntry;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * LogUtil - Provides structured execution logging and device log capture.
 */
public class LogUtil {

    private static String getLogDir() {
        if (new File("automation").exists()) return "automation/reports/logs/";
        return "reports/logs/";
    }

    // ─── Execution logging ────────────────────────────────────────────────────

    public static void log(String message) {
        String formatted = formatLine("INFO", message);
        System.out.println(formatted);
        appendToFile("execution.log", formatted);
    }

    public static void logWarning(String message) {
        String formatted = formatLine("WARN", message);
        System.out.println(formatted);
        appendToFile("execution.log", formatted);
    }

    public static void logError(String message, Throwable t) {
        String formatted = formatLine("ERROR", message + (t != null ? " - " + t.getMessage() : ""));
        System.err.println(formatted);
        appendToFile("execution.log", formatted);
        if (t != null) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(
                    new File(getLogDir(), "execution.log"), true))) {
                t.printStackTrace(pw);
            } catch (Exception ignored) {}
        }
    }

    public static void logTestStart(String testId, String testName) {
        log(String.format("▶ START  [%s] %s", testId, testName));
    }

    public static void logTestPass(String testId, long durationMs) {
        log(String.format("✔ PASS   [%s] Duration: %dms", testId, durationMs));
    }

    public static void logTestFail(String testId, String reason, long durationMs) {
        log(String.format("✘ FAIL   [%s] Duration: %dms | Reason: %s", testId, durationMs, reason));
    }

    public static void logTestSkip(String testId) {
        log(String.format("⊘ SKIP   [%s]", testId));
    }

    // ─── Device log capture ───────────────────────────────────────────────────

    /**
     * Captures logcat entries from the device and writes them to a file.
     * @return relative path from reports root to the log file
     */
    public static String captureDeviceLogs(AndroidDriver driver, String testCaseId) {
        if (driver == null) return "";
        String logDir = getLogDir();
        File dir = new File(logDir);
        if (!dir.exists()) dir.mkdirs();

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = testCaseId + "_device_" + timestamp + ".log";
        File logFile = new File(dir, fileName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile))) {
            List<LogEntry> entries = driver.manage().logs().get("logcat").getAll();
            writer.println("=== Device Logcat for " + testCaseId + " ===");
            writer.println("Captured at: " + new Date());
            writer.println("Entry count: " + entries.size());
            writer.println("===========================================");
            for (LogEntry entry : entries) {
                writer.println(new Date(entry.getTimestamp()) + " | " +
                        entry.getLevel() + " | " + entry.getMessage());
            }
            log("Device logs captured: " + fileName + " (" + entries.size() + " entries)");
            return "logs/" + fileName;
        } catch (Exception e) {
            logError("Failed to capture device logs for " + testCaseId, e);
            return "";
        }
    }

    // ─── Appium log capture ───────────────────────────────────────────────────

    public static String captureAppiumLogs(AndroidDriver driver, String testCaseId) {
        if (driver == null) return "";
        String logDir = getLogDir();
        File dir = new File(logDir);
        if (!dir.exists()) dir.mkdirs();

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = testCaseId + "_appium_" + timestamp + ".log";
        File logFile = new File(dir, fileName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile))) {
            List<LogEntry> entries = driver.manage().logs().get("server").getAll();
            for (LogEntry entry : entries) {
                writer.println(new Date(entry.getTimestamp()) + " | " + entry.getMessage());
            }
            return "logs/" + fileName;
        } catch (Exception e) {
            // Appium server logs may not always be available
            return "";
        }
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private static String formatLine(String level, String message) {
        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        return String.format("[%s] [%-5s] %s", ts, level, message);
    }

    private static synchronized void appendToFile(String fileName, String message) {
        File dir = new File(getLogDir());
        if (!dir.exists()) dir.mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(new File(dir, fileName), true))) {
            pw.println(message);
        } catch (Exception ignored) {}
    }
}
