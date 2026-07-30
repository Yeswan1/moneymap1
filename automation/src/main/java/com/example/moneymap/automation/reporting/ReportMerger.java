package com.example.moneymap.automation.reporting;

import com.example.moneymap.automation.model.TestCase;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * ReportMerger - Merges execution-results.json from different shards, deduplicates,
 * and regenerates consolidated Excel and HTML reports.
 */
public class ReportMerger {
    public static void main(String[] args) {
        String inputDir = args.length > 0 ? args[0] : "automation/Test Results/JSON";
        String outputDir = args.length > 1 ? args[1] : "automation/Test Results";

        if (!new File("automation").exists()) {
            inputDir = args.length > 0 ? args[0] : "Test Results/JSON";
            outputDir = args.length > 1 ? args[1] : "Test Results";
        }

        System.out.println("Merging reports from input directory: " + inputDir);
        File dir = new File(inputDir);
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("Input directory does not exist: " + inputDir);
            System.exit(1);
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".json") && !name.contains("execution-results.json"));
        // Fallback: If no suffix JSON found, try any json files
        if (files == null || files.length == 0) {
            files = dir.listFiles((d, name) -> name.endsWith(".json"));
        }

        if (files == null || files.length == 0) {
            System.out.println("No JSON files found in: " + inputDir);
            System.exit(0);
        }

        List<TestCase> mergedCases = new ArrayList<>();
        Map<String, TestCase> casesMap = new LinkedHashMap<>();

        String buildNumber = "local";
        String branchName = "main";
        String gitCommit = "local";

        // Sort files by name so execution order stays stable
        Arrays.sort(files, Comparator.comparing(File::getName));

        for (File file : files) {
            System.out.println("Processing file: " + file.getAbsolutePath());
            try (FileReader reader = new FileReader(file)) {
                JSONObject report = new JSONObject(new JSONTokener(reader));
                buildNumber = report.optString("buildNumber", buildNumber);
                branchName = report.optString("branch", branchName);
                gitCommit = report.optString("gitCommit", gitCommit);

                JSONArray arr = report.optJSONArray("testCases");
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        String testId = obj.optString("testId");

                        TestCase tc = new TestCase(
                            testId,
                            obj.optString("module"),
                            obj.optString("name"),
                            obj.optString("priority", "MEDIUM"),
                            obj.optString("preconditions", ""),
                            obj.optString("steps", ""),
                            obj.optString("testData", ""),
                            obj.optString("expectedResult", "")
                        );
                        tc.setStatus(obj.optString("status", "UNEXECUTED"));
                        tc.setActualResult(obj.optString("actualResult", ""));
                        tc.setDurationMs(obj.optLong("durationMs", 0));
                        tc.setScreenshotPath(obj.optString("screenshotPath", ""));
                        tc.setDeviceLogPath(obj.optString("deviceLogPath", ""));
                        tc.setPageSourcePath(obj.optString("pageSourcePath", ""));
                        tc.setAppiumLogPath(obj.optString("appiumLogPath", ""));
                        tc.setLocatorUsed(obj.optString("locatorUsed", ""));
                        tc.setCurrentActivity(obj.optString("currentActivity", ""));
                        tc.setCurrentPackage(obj.optString("currentPackage", ""));

                        // Deduplicate logic: Keep executed cases over UNEXECUTED cases
                        if (casesMap.containsKey(testId)) {
                            TestCase existing = casesMap.get(testId);
                            if (!tc.getStatus().equals("UNEXECUTED") || existing.getStatus().equals("UNEXECUTED")) {
                                casesMap.put(testId, tc);
                            }
                        } else {
                            casesMap.put(testId, tc);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error processing file " + file.getName() + ": " + e.getMessage());
            }
        }

        mergedCases.addAll(casesMap.values());
        System.out.println("Total unique test cases merged: " + mergedCases.size());

        // Create target directories
        new File(outputDir + "/Excel").mkdirs();
        new File(outputDir + "/HTML").mkdirs();
        new File(outputDir + "/JSON").mkdirs();

        // Write merged execution-results.json
        String consolidatedJsonPath = outputDir + "/JSON/execution-results.json";
        System.out.println("Writing consolidated JSON report: " + consolidatedJsonPath);
        writeMergedJsonReport(mergedCases, consolidatedJsonPath, buildNumber, branchName, gitCommit);

        // Generate Excel reports
        System.out.println("Generating consolidated Excel reports...");
        try {
            ExcelReportGenerator.generateReports(mergedCases, outputDir + "/Excel", buildNumber, branchName, gitCommit);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Generate HTML reports
        System.out.println("Generating consolidated HTML reports...");
        try {
            HTMLReportGenerator.generateReports(mergedCases, outputDir + "/HTML", buildNumber, branchName, gitCommit);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Consolidated reports generated successfully in: " + outputDir);
    }

    private static void writeMergedJsonReport(List<TestCase> testCases, String path, String buildNumber, String branchName, String gitCommit) {
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
            obj.put("pageSourcePath",tc.getPageSourcePath());
            obj.put("appiumLogPath", tc.getAppiumLogPath());
            obj.put("locatorUsed",   tc.getLocatorUsed());
            obj.put("currentActivity",tc.getCurrentActivity());
            obj.put("currentPackage",tc.getCurrentPackage());
            arr.put(obj);
        }

        JSONObject report = new JSONObject();
        report.put("buildNumber",  buildNumber);
        report.put("gitCommit",    gitCommit);
        report.put("branch",       branchName);
        report.put("executedAt",   new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
        report.put("testCases",    arr);

        try (java.io.FileWriter writer = new java.io.FileWriter(path)) {
            writer.write(report.toString(2));
        } catch (Exception e) {
            System.err.println("Failed to write consolidated JSON report: " + e.getMessage());
        }
    }
}
