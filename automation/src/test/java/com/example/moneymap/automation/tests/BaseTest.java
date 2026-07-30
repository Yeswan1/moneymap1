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
import org.testng.annotations.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * BaseTest - Suite lifecycle management, test case loading, and report generation.
 */
public class BaseTest {

    protected AndroidDriver driver;
    public static final List<TestCase> testCases = Collections.synchronizedList(new ArrayList<>());
    public static long suiteStartTime;
    public static String buildNumber = System.getenv().getOrDefault("GITHUB_RUN_NUMBER", "local");
    public static String gitCommit    = System.getenv().getOrDefault("GITHUB_SHA", "local");
    public static String branchName   = System.getenv().getOrDefault("GITHUB_REF_NAME", "main");

    // ─── Suite lifecycle ──────────────────────────────────────────────────────

    @BeforeSuite(alwaysRun = true)
    public void setupSuite() {
        suiteStartTime = System.currentTimeMillis();
        LogUtil.log("============================================================");
        LogUtil.log(" MoneyMap Enterprise E2E Automation Suite Starting");
        LogUtil.log(" Build: " + buildNumber + " | Branch: " + branchName);
        LogUtil.log("============================================================");
        loadTestCasesCatalog();
    }

    @BeforeClass(alwaysRun = true)
    public void setupClass() {
        LogUtil.log("Initializing Appium Driver for: " + this.getClass().getSimpleName());
        try {
            driver = AppiumDriverFactory.getDriver();
        } catch (Exception e) {
            LogUtil.logError("Appium driver unavailable — running in simulation mode", e);
            driver = null;
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        LogUtil.log("Closing Appium Driver for: " + this.getClass().getSimpleName());
        AppiumDriverFactory.quitDriver();
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
        long duration = System.currentTimeMillis() - suiteStartTime;
        LogUtil.log("============================================================");
        LogUtil.log(" Suite Complete. Duration: " + (duration / 1000) + "s");
        LogUtil.log(" Total test cases loaded: " + testCases.size());
        
        // Count test statuses
        int passed = 0, failed = 0, skipped = 0, notRun = 0;
        for (TestCase tc : testCases) {
            String status = tc.getStatus().toUpperCase();
            switch (status) {
                case "PASSED": passed++; break;
                case "FAILED": failed++; break;
                case "SKIPPED": skipped++; break;
                default: notRun++; break;
            }
        }
        LogUtil.log(" Test Results: Passed=" + passed + " Failed=" + failed + 
                    " Skipped=" + skipped + " NotRun=" + notRun);
        
        try {
            generateAllReports(duration);
            LogUtil.log(" ✓ Reports generation completed successfully");
        } catch (Exception e) {
            LogUtil.logError("❌ CRITICAL: Report generation failed", e);
            e.printStackTrace();
            // Don't throw - let threshold enforcement run
        }
        
        enforcePassRateThreshold();
    }

    // ─── Test Case loading ────────────────────────────────────────────────────

    private void loadTestCasesCatalog() {
        String[] searchPaths = {
            "automation/data/test_cases.json",
            "data/test_cases.json",
            "../automation/data/test_cases.json"
        };
        for (String path : searchPaths) {
            File f = new File(path);
            if (f.exists()) {
                try (FileReader reader = new FileReader(f)) {
                    JSONArray arr = new JSONArray(new JSONTokener(reader));
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        TestCase tc = new TestCase(
                            obj.optString("testId"),
                            obj.optString("module"),
                            obj.optString("name"),
                            obj.optString("priority", "MEDIUM"),
                            obj.optString("preconditions", ""),
                            obj.optString("steps", ""),
                            obj.optString("testData", ""),
                            obj.optString("expectedResult", "")
                        );
                        testCases.add(tc);
                    }
                    LogUtil.log("Loaded " + testCases.size() + " test cases from: " + path);
                    return;
                } catch (Exception e) {
                    LogUtil.logError("Error loading test catalog from " + path, e);
                }
            }
        }
        LogUtil.logWarning("test_cases.json not found — generating synthetic cases");
        generateSyntheticTestCases();
    }

    private void generateSyntheticTestCases() {
        String[][] modules = {
            {"Authentication",40}, {"Authorization",30}, {"Registration",20},
            {"Profile Management",20}, {"Navigation",30}, {"Dashboard",20},
            {"Forms",40}, {"CRUD Operations",40}, {"Search",20}, {"Filters",20},
            {"Input Validation",40}, {"Error Handling",20}, {"Session Management",20},
            {"Notifications",20}, {"File Upload",20}, {"Offline Handling",10},
            {"Accessibility",20}, {"Responsive UI",10}, {"Performance Smoke Tests",20},
            {"Regression Suite",50}
        };
        int id = 1;
        for (String[] moduleInfo : modules) {
            String module = moduleInfo[0];
            int count = Integer.parseInt(moduleInfo[1]);
            String prefix = module.replaceAll("[^A-Z]", "").substring(0, Math.min(4, module.replaceAll("[^A-Z]","").length()));
            if (prefix.isEmpty()) prefix = module.substring(0,3).toUpperCase();
            for (int i = 1; i <= count; i++) {
                testCases.add(new TestCase(
                    "TC_" + prefix + "_" + String.format("%03d", i),
                    module, module + " Test " + i, "MEDIUM",
                    "App running", "Execute test " + i, "N/A", "Test succeeds"
                ));
            }
        }
        LogUtil.log("Generated " + testCases.size() + " synthetic test cases.");
    }

    // ─── Test case result update ──────────────────────────────────────────────

    public static synchronized void updateTestCase(String testId, String status,
            String actualResult, long durationMs, String screenshot, String deviceLog) {
        for (TestCase tc : testCases) {
            if (tc.getTestId().equalsIgnoreCase(testId)) {
                tc.setStatus(status);
                tc.setActualResult(actualResult != null ? actualResult : "");
                tc.setDurationMs(durationMs);
                if (screenshot != null && !screenshot.isEmpty()) tc.setScreenshotPath(screenshot);
                if (deviceLog != null && !deviceLog.isEmpty())   tc.setDeviceLogPath(deviceLog);
                return;
            }
        }
    }

    // ─── Report generation ────────────────────────────────────────────────────

    private void generateAllReports(long durationMs) {
        String resultsDir = resolveResultsDir();
        LogUtil.log("Report generation starting...");
        LogUtil.log("Results directory: " + resultsDir);
        LogUtil.log("Test cases to report: " + testCases.size());
        
        mkdirs(resultsDir + "/Excel");
        mkdirs(resultsDir + "/HTML");
        mkdirs(resultsDir + "/JSON");
        mkdirs(resultsDir + "/Summary");
        
        LogUtil.log("Created directories:");
        LogUtil.log("  - " + resultsDir + "/Excel");
        LogUtil.log("  - " + resultsDir + "/HTML");
        LogUtil.log("  - " + resultsDir + "/JSON");
        LogUtil.log("  - " + resultsDir + "/Summary");

        try {
            LogUtil.log("Generating Excel reports...");
            ExcelReportGenerator.generateReports(testCases, resultsDir + "/Excel");
            LogUtil.log("✓ Excel reports completed");
        } catch (Exception e) {
            LogUtil.logError("❌ Excel report generation failed", e);
            e.printStackTrace();
        }

        try {
            LogUtil.log("Generating HTML reports...");
            HTMLReportGenerator.generateReports(testCases, resultsDir + "/HTML");
            LogUtil.log("✓ HTML reports completed");
        } catch (Exception e) {
            LogUtil.logError("❌ HTML report generation failed", e);
            e.printStackTrace();
        }

        try {
            LogUtil.log("Generating JSON report...");
            generateJsonReport(resultsDir + "/JSON/execution-results.json");
            LogUtil.log("✓ JSON report completed");
        } catch (Exception e) {
            LogUtil.logError("❌ JSON report generation failed", e);
            e.printStackTrace();
        }

        try {
            LogUtil.log("Generating Markdown summary...");
            generateMarkdownSummary(resultsDir + "/Summary/summary.md", durationMs);
            LogUtil.log("✓ Markdown summary completed");
        } catch (Exception e) {
            LogUtil.logError("❌ Markdown summary generation failed", e);
            e.printStackTrace();
        }

        LogUtil.log("All reports generated in: " + resultsDir);
        
        // List generated files for verification
        try {
            LogUtil.log("Verifying generated files:");
            File excelDir = new File(resultsDir + "/Excel");
            if (excelDir.exists()) {
                File[] excelFiles = excelDir.listFiles();
                LogUtil.log("Excel files: " + (excelFiles != null ? excelFiles.length : 0));
                if (excelFiles != null) {
                    for (File f : excelFiles) {
                        LogUtil.log("  - " + f.getName() + " (" + f.length() + " bytes)");
                    }
                }
            }
            File htmlDir = new File(resultsDir + "/HTML");
            if (htmlDir.exists()) {
                File[] htmlFiles = htmlDir.listFiles();
                LogUtil.log("HTML files: " + (htmlFiles != null ? htmlFiles.length : 0));
                if (htmlFiles != null) {
                    for (File f : htmlFiles) {
                        LogUtil.log("  - " + f.getName() + " (" + f.length() + " bytes)");
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.logError("Could not list generated files", e);
        }
    }

    private String resolveResultsDir() {
        if (new File("automation").exists()) return "automation/Test Results";
        if (new File("Test Results").exists()) return "Test Results";
        return "Test Results";
    }

    private void mkdirs(String path) {
        new File(path).mkdirs();
    }

    // ─── JSON report ──────────────────────────────────────────────────────────

    private void generateJsonReport(String path) {
        JSONArray arr = new JSONArray();
        for (TestCase tc : testCases) {
            JSONObject obj = new JSONObject();
            obj.put("testId",        tc.getTestId());
            obj.put("module",        tc.getModule());
            obj.put("name",          tc.getName());
            obj.put("priority",      tc.getPriority());
            obj.put("preconditions", tc.getPreconditions());
            obj.put("steps",         tc.getSteps());
            obj.put("testData",      tc.getTestData());
            obj.put("expectedResult",tc.getExpectedResult());
            obj.put("actualResult",  tc.getActualResult());
            obj.put("status",        tc.getStatus());
            obj.put("durationMs",    tc.getDurationMs());
            obj.put("screenshotPath",tc.getScreenshotPath());
            obj.put("deviceLogPath", tc.getDeviceLogPath());
            arr.put(obj);
        }
        // Also write execution metadata
        JSONObject report = new JSONObject();
        report.put("buildNumber",  buildNumber);
        report.put("gitCommit",    gitCommit);
        report.put("branch",       branchName);
        report.put("executedAt",   new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
        report.put("testCases",    arr);

        try (FileWriter writer = new FileWriter(path)) {
            writer.write(report.toString(2));
        } catch (IOException e) {
            LogUtil.logError("Failed to write JSON report", e);
        }
    }

    // ─── Markdown summary ─────────────────────────────────────────────────────

    private void generateMarkdownSummary(String path, long durationMs) {
        int total   = testCases.size();
        int passed  = 0; int failed  = 0; int skipped = 0; int blocked = 0;

        StringBuilder passedList  = new StringBuilder();
        StringBuilder failedList  = new StringBuilder();
        StringBuilder skippedList = new StringBuilder();

        for (TestCase tc : testCases) {
            switch (tc.getStatus().toUpperCase()) {
                case "PASSED":
                    passed++;
                    passedList.append(String.format("- ✅ **%s** — %s%n", tc.getTestId(), tc.getName()));
                    break;
                case "FAILED":
                    failed++;
                    failedList.append(String.format("- ❌ **%s** — %s%n  > *Reason: %s*%n",
                            tc.getTestId(), tc.getName(), tc.getActualResult()));
                    break;
                case "SKIPPED":
                    skipped++;
                    skippedList.append(String.format("- ⏭️ **%s** — %s%n", tc.getTestId(), tc.getName()));
                    break;
                case "BLOCKED":
                    blocked++;
                    break;
                default:
                    break;
            }
        }

        double passRate = total > 0 ? (double) passed / total * 100 : 0.0;
        double failRate = total > 0 ? (double) failed / total * 100 : 0.0;
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        StringBuilder md = new StringBuilder();
        md.append("# Android Appium E2E Execution Summary\n\n");
        md.append("| Metric | Value |\n|---|---|\n");
        md.append("| **Build Number** | ").append(buildNumber).append(" |\n");
        md.append("| **Execution Date** | ").append(date).append(" |\n");
        md.append("| **Git Commit** | ").append(gitCommit).append(" |\n");
        md.append("| **Branch** | ").append(branchName).append(" |\n");
        md.append("| **APK Version** | v1.0 |\n");
        md.append("| **Device** | Android Emulator (UiAutomator2) |\n");
        md.append("| **Android Version** | API 35 |\n\n");

        md.append("## 📊 Execution Metrics\n\n");
        md.append("| Metric | Count | Percentage |\n|---|---|---|\n");
        md.append(String.format("| **Total Test Cases** | %d | 100%% |%n", total));
        md.append(String.format("| ✅ **Passed** | %d | %.2f%% |%n", passed, passRate));
        md.append(String.format("| ❌ **Failed** | %d | %.2f%% |%n", failed, failRate));
        md.append(String.format("| ⏭️ **Skipped** | %d | %.2f%% |%n", skipped, total > 0 ? (double) skipped/total*100 : 0));
        md.append(String.format("| 🚫 **Blocked** | %d | - |%n", blocked));
        md.append(String.format("| ⏱️ **Duration** | %ds | - |%n%n", durationMs / 1000));

        md.append("## ✅ Passed Tests\n\n");
        md.append(passedList.length() > 0 ? passedList.toString() : "*None*\n");
        md.append("\n## ❌ Failed Tests\n\n");
        md.append(failedList.length() > 0 ? failedList.toString() : "*None*\n");
        md.append("\n## ⏭️ Skipped Tests\n\n");
        md.append(skippedList.length() > 0 ? skippedList.toString() : "*None*\n");

        md.append("\n---\n*Report generated by MoneyMap Enterprise E2E Automation Framework*\n");

        try (FileWriter writer = new FileWriter(path)) {
            writer.write(md.toString());
        } catch (IOException e) {
            LogUtil.logError("Failed to write Markdown summary", e);
        }
    }

    // ─── Pass rate enforcement ────────────────────────────────────────────────

    private void enforcePassRateThreshold() {
        if (testCases.isEmpty()) return;
        long passed = testCases.stream()
                .filter(tc -> "PASSED".equalsIgnoreCase(tc.getStatus())).count();
        double passRate = (double) passed / testCases.size() * 100;
        LogUtil.log(String.format("Final pass rate: %.2f%% (%d/%d)", passRate, passed, testCases.size()));
        if (passRate < 95.0) {
            String msg = String.format(
                "Suite FAILED: Pass rate %.2f%% is below 95%% threshold.", passRate);
            LogUtil.logError(msg, null);
            throw new RuntimeException(msg);
        }
        LogUtil.log("Suite PASSED: Pass rate meets 95%% threshold.");
    }
}
