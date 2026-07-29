package com.example.moneymap.automation.utils;

import io.appium.java_client.android.AndroidDriver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Structured logging and ADB device log capture utility.
 * All output is timestamped and written to both console and a rolling session log file.
 */
public class LogUtil {

    private static final String LOG_BASE_DIR = "reports/logs";
    private static final String SESSION_LOG_FILE = "session.log";
    private static FileWriter sessionLogWriter;
    private static final Object LOCK = new Object();

    static {
        try {
            String logDir = resolveLogDir();
            new File(logDir).mkdirs();
            sessionLogWriter = new FileWriter(logDir + "/" + SESSION_LOG_FILE, true);
        } catch (IOException e) {
            System.err.println("Failed to initialise session log writer: " + e.getMessage());
        }
    }

    /**
     * Logs an informational message with timestamp to console and session log.
     */
    public static void log(String message) {
        String entry = formatEntry("INFO", message);
        System.out.println(entry);
        writeToSessionLog(entry);
    }

    /**
     * Logs an error message with stack trace to console and session log.
     */
    public static void logError(String message, Throwable t) {
        String entry = formatEntry("ERROR", message);
        System.err.println(entry);
        writeToSessionLog(entry);
        if (t != null) {
            t.printStackTrace();
            writeToSessionLog(stackTraceToString(t));
        }
    }

    /**
     * Captures ADB logcat output from the connected device and writes it to a file.
     *
     * @param driver  The active AndroidDriver session (used to get app package)
     * @param testId  The test case ID for naming the log file
     * @return Relative path to the saved log file, or empty string on failure
     */
    public static String captureDeviceLogs(AndroidDriver driver, String testId) {
        try {
            String logDir = resolveLogDir();
            new File(logDir).mkdirs();

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String safeTestId = testId.replaceAll("[^a-zA-Z0-9_\\-]", "_");
            String logFilePath = logDir + "/" + safeTestId + "_" + timestamp + "_adb.log";

            // Capture last 500 lines of logcat filtered to the app's package
            ProcessBuilder pb = new ProcessBuilder(
                "adb", "logcat", "-d", "-t", "500", "*:E", "moneymap:V"
            );
            pb.redirectErrorStream(true);
            pb.redirectOutput(new File(logFilePath));
            Process process = pb.start();
            process.waitFor();

            log("ADB device log captured: " + logFilePath);
            return logFilePath;
        } catch (Exception e) {
            logError("Failed to capture device logs for " + testId + ": " + e.getMessage(), e);
            return "";
        }
    }

    /**
     * Captures Appium server log snippet and appends to session log.
     *
     * @param driver  The active AndroidDriver session
     * @param testId  Test case identifier for context
     */
    public static void captureAppiumLogs(AndroidDriver driver, String testId) {
        try {
            if (driver != null) {
                java.util.List<String> logs = driver.manage().logs().get("server").getAll()
                    .stream()
                    .map(entry -> entry.getTimestamp() + " " + entry.getLevel() + ": " + entry.getMessage())
                    .collect(java.util.stream.Collectors.toList());

                String logEntry = formatEntry("APPIUM", "[" + testId + "] Server log lines: " + logs.size());
                writeToSessionLog(logEntry);
            }
        } catch (Exception e) {
            // Appium log retrieval is best-effort only
            log("Appium log capture skipped for " + testId + ": " + e.getMessage());
        }
    }

    /**
     * Closes the session log writer. Call this at suite teardown.
     */
    public static void closeSessionLog() {
        synchronized (LOCK) {
            if (sessionLogWriter != null) {
                try {
                    sessionLogWriter.flush();
                    sessionLogWriter.close();
                } catch (IOException e) {
                    System.err.println("Failed to close session log: " + e.getMessage());
                }
            }
        }
    }

    // ── Internal Helpers ─────────────────────────────────────────────────────

    private static String formatEntry(String level, String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        String thread = Thread.currentThread().getName();
        return String.format("[%s] [%s] [%s] %s", timestamp, level, thread, message);
    }

    private static void writeToSessionLog(String entry) {
        synchronized (LOCK) {
            if (sessionLogWriter != null) {
                try {
                    sessionLogWriter.write(entry + System.lineSeparator());
                    sessionLogWriter.flush();
                } catch (IOException e) {
                    System.err.println("Failed to write to session log: " + e.getMessage());
                }
            }
        }
    }

    private static String stackTraceToString(Throwable t) {
        java.io.StringWriter sw = new java.io.StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private static String resolveLogDir() {
        if (new File("automation").exists()) {
            return "automation/" + LOG_BASE_DIR;
        }
        return LOG_BASE_DIR;
    }
}