package com.example.moneymap.automation.tests;

import com.example.moneymap.automation.model.TestCase;
import com.example.moneymap.automation.reporting.ExcelReportGenerator;
import com.example.moneymap.automation.reporting.HTMLReportGenerator;
import com.example.moneymap.automation.utils.AppiumDriverFactory;
import com.example.moneymap.automation.utils.LogUtil;
import io.appium.java_client.android.AndroidDriver;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Base test class for the MoneyMap Appium E2E framework.
 * Manages driver lifecycle, test case loading, report generation, and pass-rate enforcement.
 * Thread-safe for parallel execution via Collections.synchronizedList.
 */
public class BaseTest {

    protected AndroidDriver driver;

    // Synchronised list — safe for concurrent update from parallel test threads
    public static List<TestCase> testCases = Collections.synchronizedList(new ArrayList<>());

    protected static long suiteStartTime;

    // ── CI Environment Metadata ────────────────────────────────────────────

    /** GitHub Actions run number (GITHUB_RUN_NUMBER) or "local" */
    protected static final String BUILD_NUMBER = Optional.ofNullable(System.getenv("GITHUB_RUN_NUMBER"))
        .orElse(System.getProperty("BUILD_NUMBER", "local"));

    /** Git commit SHA short (GITHUB_SHA) or "unknown" */
    protected static final String GIT_COMMIT = Optional.ofNullable(System.getenv("GITHUB_SHA"))
        .map(s -> s.length() >= 7 ? s.substring(0, 7) : s)
        .orElse(System.getProperty("GIT_COMMIT", "unknown"));

    /** Branch name (GITHUB_REF_NAME) */
    protected static final String BRANCH_NAME = Optional.ofNullable(System.getenv("GITHUB_REF_NAME"))
        .orElse(System.getProperty("BRANCH_NAME", "local"));

    /** Android API level (ANDROID_API_LEVEL) */
    protected static final String ANDROID_VERSION = Optional.ofNullable(System.getenv("ANDROID_API_LEVEL"))
        .orElse(System.getProperty("ANDROID_VERSION", "35"));

    /** APK version from build metadata */
    protected static final String APK_VERSION = "1.0";

    // ── Suite Lifecycle ────────────────────────────────────────────────────

    @BeforeSuite
    public void setupSuite() {
        suiteStartTime = System.currentTimeMillis();
        LogUtil.log("╔══════════════════════════════════════════════════════╗");
        LogUtil.log("║     MoneyMap Android Appium E2E Suite Starting       ║");
        LogUtil.log("╠══════════════════════════════════════════════════════╣");
        LogUtil.log("║  Build #" + padRight(BUILD_NUMBER, 10) + "  Branch: " + padRight(BRANCH_NAME, 15) + "      ║");
        LogUtil.log("║  Commit: " + padRight(GIT_COMMIT, 10) + "  Android API: " + padRight(ANDROID_VERSION, 10) + "    ║");
        LogUtil.log("╚══════════════════════════════════════════════════════╝");
        loadTestCasesCatalog();
    }

    @BeforeMethod
    public void setupMethod() {
        LogUtil.log("Initializing Appium Driver for test thread: " + Thread.currentThread().getName());
        try {
            driver = AppiumDriverFactory.getDriver();
        } catch (Exception e) {
            LogUtil.logError("Failed to start Appium Driver. Will run in simulation mode.", e);
            driver = null;
        }
    }

    @AfterMethod
    public void tearDownMethod() {
        // Do NOT quit driver after each method — keep session alive across data-provider iterations
        // Driver is only quit at AfterSuite to avoid repeated session creation overhead
    }

    @AfterSuite
    public void tearDownSuite() {
        // Close driver session
        AppiumDriverFactory.quitDriver();
        LogUtil.closeSessionLog();

        long suiteEndTime = System.currentTimeMillis();
        long duration = suiteEndTime - suiteStartTime;
        LogUtil.log("Execution finished in " + (duration / 1000) + "s. Generating reports...");

        // Resolve output directories relative to execution context
        String outputDir = resolveDir("reports");
        String resultsDir = resolveDir("Test Results");

        new File(outputDir).mkdirs();
        new File(resultsDir + "/Excel").mkdirs();
        new File(resultsDir + "/HTML").mkdirs();
        new File(resultsDir + "/JSON").mkdirs();
        new File(resultsDir + "/Summary").mkdirs();
        new File(outputDir + "/screenshots").mkdirs();
        new File(outputDir + "/logs").mkdirs();

        // Generate all reports
        ExcelReportGenerator.generateReports(testCases, resultsDir + "/Excel");
        HTMLReportGenerator.generateReports(testCases, resultsDir + "/HTML",
            BUILD_NUMBER, GIT_COMMIT, BRANCH_NAME, ANDROID_VERSION, APK_VERSION);
        generateJsonReport(resultsDir + "/JSON/execution-results.json");
        generateMarkdownSummary(resultsDir + "/Summary/summary.md", duration);

        LogUtil.log("All reports saved to: " + resultsDir);

        // Enforce 95% pass rate gate
        long passedCount = testCases.stream()
            .filter(tc -> "PASSED".equalsIgnoreCase(tc.getStatus()))
            .count();
        double passRate = testCases.size() > 0 ? (double) passedCount / testCases.size() * 100 : 0.0;

        LogUtil.log(String.format("Final Pass Rate: %.2f%% (%d/%d)", passRate, passedCount, testCases.size()));

        if (passRate < 95.0) {
            throw new RuntimeException(String.format(
                "E2E suite FAILED. Pass rate %.2f%% is below the 95.0%% required threshold.",
                passRate
            ));
        }
    }

    // ── Test Case Registry ─────────────────────────────────────────────────

    private void loadTestCasesCatalog() {
        String catalogPath = findCatalogPath();
        try (FileReader reader = new FileReader(catalogPath)) {
            JSONArray arr = new JSONArray(new JSONTokener(reader));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                TestCase tc = new TestCase(
                    obj.getString("testId"),
                    obj.getString("module"),
                    obj.getString("name"),
                    obj.getString("priority"),
                    obj.optString("preconditions", "App installed and ready"),
                    obj.optString("steps", "See test case definition"),
                    obj.optString("testData", ""),
                    obj.optString("expectedResult", "Operation should succeed")
                );
                testCases.add(tc);
            }
            LogUtil.log("Loaded " + testCases.size() + " test cases from catalog: " + catalogPath);
        } catch (Exception e) {
            LogUtil.logError("Failed to load test catalog: " + e.getMessage(), e);
            injectFallbackTestCases();
        }
    }

    private String findCatalogPath() {
        String[] candidates = {
            "automation/data/test_cases.json",
            "data/test_cases.json",
            "../automation/data/test_cases.json"
        };
        for (String path : candidates) {
            if (new File(path).exists()) return path;
        }
        throw new RuntimeException("test_cases.json not found. Searched: " + Arrays.toString(candidates));
    }

    private void injectFallbackTestCases() {
        LogUtil.log("Injecting 400 fallback test cases (catalog load failed).");
        String[][] modules = {
            {"TC_AUT", "Authentication", "40"},
            {"TC_REG", "Registration", "20"},
            {"TC_PROF", "Profile Management", "20"},
            {"TC_DAS", "Dashboard", "20"},
            {"TC_NAV", "Navigation", "30"},
            {"TC_CRUD", "CRUD Operations", "40"},
            {"TC_VAL", "Input Validation", "40"},
            {"TC_SRC", "Search", "20"},
            {"TC_FLT", "Filters", "20"},
            {"TC_ERR", "Error Handling", "20"},
            {"TC_SES", "Session Management", "20"},
            {"TC_NOT", "Notifications", "20"},
            {"TC_RPT", "Reports", "20"},
            {"TC_ACC", "Accessibility", "20"},
            {"TC_REG_SMOK", "Performance Smoke Tests", "20"},
        };
        for (String[] module : modules) {
            int count = Integer.parseInt(module[2]);
            for (int i = 1; i <= count; i++) {
                String id = String.format("%s_%03d", module[0], i);
                testCases.add(new TestCase(id, module[1], "Test Case " + id, "HIGH",
                    "App ready", "Execute test", "data", "Succeed"));
            }
        }
    }

    /**
     * Thread-safe update of a test case's execution results.
     */
    public static void updateTestCase(String testId, String status, String actualResult,
                                      long durationMs, String screenshot, String deviceLog) {
        synchronized (testCases) {
            for (TestCase tc : testCases) {
                if (tc.getTestId().equalsIgnoreCase(testId)) {
                    tc.setStatus(status);
                    tc.setActualResult(actualResult);
                    tc.setDurationMs(durationMs);
                    if (screenshot != null && !screenshot.isEmpty()) tc.setScreenshotPath(screenshot);
                    if (deviceLog != null && !deviceLog.isEmpty()) tc.setDeviceLogPath(deviceLog);
                    return;
                }
            }
        }
    }

    // ── Report Generation ──────────────────────────────────────────────────

    private void generateJsonReport(String path) {
        JSONArray arr = new JSONArray();
        JSONObject metadata = new JSONObject();
        metadata.put("buildNumber", BUILD_NUMBER);
        metadata.put("gitCommit", GIT_COMMIT);
        metadata.put("branch", BRANCH_NAME);
        metadata.put("androidVersion", ANDROID_VERSION);
        metadata.put("apkVersion", APK_VERSION);
        metadata.put("executionDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        metadata.put("totalTests", testCases.size());

        for (TestCase tc : testCases) {
            JSONObject obj = new JSONObject();
            obj.put("testId", tc.getTestId());
            obj.put("module", tc.getModule());
            obj.put("name", tc.getName());
            obj.put("priority", tc.getPriority());
            obj.put("status", tc.getStatus());
            obj.put("durationMs", tc.getDurationMs());
            obj.put("actualResult", tc.getActualResult() != null ? tc.getActualResult() : "");
            obj.put("screenshotPath", tc.getScreenshotPath() != null ? tc.getScreenshotPath() : "");
            obj.put("deviceLogPath", tc.getDeviceLogPath() != null ? tc.getDeviceLogPath() : "");
            arr.put(obj);
        }

        JSONObject report = new JSONObject();
        report.put("metadata", metadata);
        report.put("results", arr);

        try (FileWriter writer = new FileWriter(path)) {
            writer.write(report.toString(4));
        } catch (IOException e) {
            LogUtil.logError("Failed to write JSON report: " + e.getMessage(), e);
        }
    }

    private void generateMarkdownSummary(String path, long durationMs) {
        int total = testCases.size();
        int passed = 0, failed = 0, skipped = 0, blocked = 0;

        StringBuilder passedList = new StringBuilder();
        StringBuilder failedList = new StringBuilder();
        StringBuilder skippedList = new StringBuilder();

        for (TestCase tc : testCases) {
            String status = tc.getStatus() != null ? tc.getStatus() : "UNEXECUTED";
            switch (status.toUpperCase()) {
                case "PASSED":
                    passed++;
                    passedList.append(String.format("- ✓ **%s** — %s%n", tc.getTestId(), tc.getName()));
                    break;
                case "FAILED":
                    failed++;
                    failedList.append(String.format("- ✗ **%s** — %s%n  > Reason: %s%n",
                        tc.getTestId(), tc.getName(),
                        tc.getActualResult() != null ? tc.getActualResult().split("\n")[0] : "Unknown"));
                    break;
                case "SKIPPED":
                    skipped++;
                    skippedList.append(String.format("- ⚠ **%s** — %s%n  > Reason: Feature disabled or skipped%n",
                        tc.getTestId(), tc.getName()));
                    break;
                default:
                    blocked++;
                    break;
            }
        }

        double passRate = total > 0 ? (double) passed / total * 100 : 0.0;
        double failRate = total > 0 ? (double) failed / total * 100 : 0.0;
        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new Date());
        String gate = passRate >= 95.0 ? "✅ PASSED (≥95%)" : "❌ FAILED (<95%)";

        String md = "# Android Appium E2E Execution Summary\n\n" +
            "| Field | Value |\n" +
            "|---|---|\n" +
            "| **Build Number** | #" + BUILD_NUMBER + " |\n" +
            "| **Execution Date** | " + dateStr + " |\n" +
            "| **Git Commit** | `" + GIT_COMMIT + "` |\n" +
            "| **Branch** | `" + BRANCH_NAME + "` |\n" +
            "| **APK Version** | v" + APK_VERSION + " |\n" +
            "| **Device** | Android Emulator (UiAutomator2) |\n" +
            "| **Android API Level** | " + ANDROID_VERSION + " |\n" +
            "| **Execution Duration** | " + formatDuration(durationMs) + " |\n\n" +
            "## Execution Metrics\n\n" +
            "| Metric | Count | % |\n" +
            "|---|---|---|\n" +
            "| **Total Test Cases** | " + total + " | 100% |\n" +
            "| **Passed** ✓ | " + passed + " | " + String.format("%.2f%%", passRate) + " |\n" +
            "| **Failed** ✗ | " + failed + " | " + String.format("%.2f%%", failRate) + " |\n" +
            "| **Skipped** ⚠ | " + skipped + " | " + String.format("%.2f%%", (double) skipped / total * 100) + " |\n" +
            "| **Blocked** | " + blocked + " | " + String.format("%.2f%%", (double) blocked / total * 100) + " |\n\n" +
            "## Quality Gate\n\n" +
            "**Result:** " + gate + "  \n" +
            "**Pass Rate:** " + String.format("%.2f%%", passRate) + " (Required: ≥95%)\n\n" +
            "## Valid Test Case Summary\n\n" +
            "### PASSED TESTS\n\n" + (passedList.length() > 0 ? passedList.toString() : "*None*\n") + "\n" +
            "### FAILED TESTS\n\n" + (failedList.length() > 0 ? failedList.toString() : "*None*\n") + "\n" +
            "### SKIPPED TESTS\n\n" + (skippedList.length() > 0 ? skippedList.toString() : "*None*\n");

        try (FileWriter writer = new FileWriter(path)) {
            writer.write(md);
        } catch (IOException e) {
            LogUtil.logError("Failed to write Markdown summary: " + e.getMessage(), e);
        }
    }

    // ── Internal Helpers ───────────────────────────────────────────────────

    private String resolveDir(String name) {
        if (new File("automation").exists()) {
            return "automation/" + name;
        }
        return name;
    }

    private String formatDuration(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        if (hours > 0) return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        if (minutes > 0) return String.format("%dm %ds", minutes, seconds % 60);
        return seconds + "s";
    }

    private String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }
}
