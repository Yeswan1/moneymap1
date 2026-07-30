package com.example.moneymap.automation.reporting;

import com.example.moneymap.automation.model.TestCase;
import com.example.moneymap.automation.utils.LogUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ExcelReportGenerator - Produces 7-sheet XLSX reports.
 * Sheets: 1. All Tests  2. Passed  3. Failed  4. Skipped  5. Metrics  6. Defects  7. Pass Rate by Module
 *
 * NOTE: Does NOT import BaseTest — build/branch info is passed as parameters to avoid
 * src/main importing from src/test (Maven source set violation).
 */
public class ExcelReportGenerator {

    public static void generateReports(List<TestCase> testCases, String outputDirectory) {
        generateReports(testCases, outputDirectory, "local", "main", "local");
    }

    public static void generateReports(List<TestCase> testCases, String outputDirectory,
                                       String buildNumber, String branchName, String gitCommit) {
        new File(outputDirectory).mkdirs();
        generateMasterReport  (testCases, outputDirectory + "/Automation_Test_Report.xlsx", buildNumber, branchName);
        generateStatusReport  (testCases, "PASSED",  outputDirectory + "/Passed_Test_Cases.xlsx");
        generateStatusReport  (testCases, "FAILED",  outputDirectory + "/Failed_Test_Cases.xlsx");
        generateSummaryReport (testCases, outputDirectory + "/Execution_Summary.xlsx", buildNumber, branchName);
        LogUtil.log("Excel reports generated in: " + outputDirectory);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Master report — 7 sheets
    // ─────────────────────────────────────────────────────────────────────────

    private static void generateMasterReport(List<TestCase> testCases, String filePath,
                                              String buildNumber, String branchName) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Styles s = new Styles(wb);

            // Sheet 1: Executed Test Cases
            XSSFSheet s1 = wb.createSheet("Executed Test Cases");
            writeTestSheet(s1, testCases, s, null);

            // Sheet 2: Passed
            XSSFSheet s2 = wb.createSheet("Passed Tests");
            writeTestSheet(s2, testCases, s, "PASSED");

            // Sheet 3: Failed
            XSSFSheet s3 = wb.createSheet("Failed Tests");
            writeTestSheet(s3, testCases, s, "FAILED");

            // Sheet 4: Skipped
            XSSFSheet s4 = wb.createSheet("Skipped Tests");
            writeTestSheet(s4, testCases, s, "SKIPPED");

            // Sheet 5: Execution Metrics
            XSSFSheet s5 = wb.createSheet("Execution Metrics");
            writeMetricsSheet(s5, testCases, s, wb, buildNumber, branchName);

            // Sheet 6: Defect Summary
            XSSFSheet s6 = wb.createSheet("Defect Summary");
            writeDefectSheet(s6, testCases, s);

            // Sheet 7: Pass Rate by Module
            XSSFSheet s7 = wb.createSheet("Pass Rate Summary");
            writePassRateSheet(s7, testCases, s);

            saveWorkbook(wb, filePath);
        } catch (Exception e) {
            LogUtil.logError("Failed to generate master Excel report", e);
        }
    }

    private static void generateStatusReport(List<TestCase> testCases, String status, String filePath) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Styles s = new Styles(wb);
            XSSFSheet sheet = wb.createSheet(status + " Tests");
            writeTestSheet(sheet, testCases, s, status);
            saveWorkbook(wb, filePath);
        } catch (Exception e) {
            LogUtil.logError("Failed to generate " + status + " Excel report", e);
        }
    }

    private static void generateSummaryReport(List<TestCase> testCases, String filePath,
                                              String buildNumber, String branchName) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Styles s = new Styles(wb);
            XSSFSheet metrics = wb.createSheet("Execution Metrics");
            writeMetricsSheet(metrics, testCases, s, wb, buildNumber, branchName);
            XSSFSheet passRate = wb.createSheet("Pass Rate Summary");
            writePassRateSheet(passRate, testCases, s);
            saveWorkbook(wb, filePath);
        } catch (Exception e) {
            LogUtil.logError("Failed to generate Summary Excel report", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Sheet writers
    // ─────────────────────────────────────────────────────────────────────────

    private static void writeTestSheet(XSSFSheet sheet, List<TestCase> testCases, Styles s, String filterStatus) {
        // Header row
        String[] headers = {"Test ID","Module","Test Name","Priority","Preconditions","Steps",
                            "Test Data","Expected Result","Actual Result","Status","Duration (ms)"};
        Row hRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell c = hRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(s.header);
        }
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, headers.length - 1));
        sheet.createFreezePane(0, 1);

        int row = 1;
        for (TestCase tc : testCases) {
            if (filterStatus != null && !filterStatus.equalsIgnoreCase(tc.getStatus())) continue;
            Row r = sheet.createRow(row++);
            r.createCell(0).setCellValue(tc.getTestId());
            r.createCell(1).setCellValue(tc.getModule());
            r.createCell(2).setCellValue(tc.getName());
            r.createCell(3).setCellValue(tc.getPriority());
            r.createCell(4).setCellValue(tc.getPreconditions());
            r.createCell(5).setCellValue(tc.getSteps());
            r.createCell(6).setCellValue(tc.getTestData());
            r.createCell(7).setCellValue(tc.getExpectedResult());
            r.createCell(8).setCellValue(tc.getActualResult() != null ? tc.getActualResult() : "");

            Cell statusCell = r.createCell(9);
            statusCell.setCellValue(tc.getStatus());
            switch (tc.getStatus().toUpperCase()) {
                case "PASSED":  statusCell.setCellStyle(s.passed);  break;
                case "FAILED":  statusCell.setCellStyle(s.failed);  break;
                case "SKIPPED": statusCell.setCellStyle(s.skipped); break;
                default:        statusCell.setCellStyle(s.unexec);  break;
            }
            r.createCell(10).setCellValue(tc.getDurationMs());
        }
        autoSize(sheet, headers.length);
    }

    private static void writeMetricsSheet(XSSFSheet sheet, List<TestCase> testCases, Styles s, Workbook wb,
                                          String buildNumber, String branchName) {
        int total = testCases.size();
        long passed = testCases.stream().filter(tc -> "PASSED".equalsIgnoreCase(tc.getStatus())).count();
        long failed = testCases.stream().filter(tc -> "FAILED".equalsIgnoreCase(tc.getStatus())).count();
        long skipped = testCases.stream().filter(tc -> "SKIPPED".equalsIgnoreCase(tc.getStatus())).count();
        long totalMs = testCases.stream().mapToLong(TestCase::getDurationMs).sum();
        double passRate = total > 0 ? (double) passed / total * 100 : 0.0;

        // Title
        Row title = sheet.createRow(0);
        Cell titleCell = title.createCell(0);
        titleCell.setCellValue("MoneyMap E2E Execution Metrics Report");
        titleCell.setCellStyle(s.title);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        // Sub-title
        Row sub = sheet.createRow(1);
        sub.createCell(0).setCellValue("Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
                + " | Build: " + buildNumber + " | Branch: " + branchName);

        sheet.createRow(2); // spacer

        // Header
        Row hRow = sheet.createRow(3);
        String[] cols = {"Metric","Value","Info"};
        for (int i = 0; i < cols.length; i++) {
            Cell c = hRow.createCell(i); c.setCellValue(cols[i]); c.setCellStyle(s.header);
        }

        Object[][] data = {
            {"Total Test Cases",       total,              "Loaded from test catalog"},
            {"Executed",               passed + failed,    "Passed + Failed"},
            {"Passed",                 passed,             ""},
            {"Failed",                 failed,             ""},
            {"Skipped / Blocked",      skipped,            ""},
            {"Pass Percentage",        String.format("%.2f%%", passRate), passRate >= 95 ? "✅ MEETS THRESHOLD" : "❌ BELOW 95%"},
            {"Fail Percentage",        String.format("%.2f%%", total > 0 ? (double)failed/total*100 : 0), ""},
            {"Total Execution Time (ms)", totalMs,         ""},
            {"Average per Test (ms)",  total > 0 ? totalMs / total : 0, ""},
            {"Pass Threshold",         "95.00%",           "Workflow fails below this"},
            {"App Package",            "com.example.moneymap", ""},
            {"App Version",            "1.0",              "versionName from build.gradle.kts"},
            {"Framework",              "Appium + TestNG + Java", ""},
            {"Min SDK",                24,                 "Android 7.0"},
            {"Target SDK",             35,                 "Android 15"},
        };

        int rowIdx = 4;
        for (Object[] row : data) {
            Row r = sheet.createRow(rowIdx++);
            r.createCell(0).setCellValue(row[0].toString());
            r.createCell(1).setCellValue(row[1].toString());
            r.createCell(2).setCellValue(row[2].toString());
        }
        autoSize(sheet, 3);
    }

    private static void writeDefectSheet(XSSFSheet sheet, List<TestCase> testCases, Styles s) {
        String[] headers = {"Test ID","Module","Test Name","Priority","Error Message","Screenshot Path","Device Log"};
        Row hRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell c = hRow.createCell(i); c.setCellValue(headers[i]); c.setCellStyle(s.header);
        }
        int row = 1;
        for (TestCase tc : testCases) {
            if (!"FAILED".equalsIgnoreCase(tc.getStatus())) continue;
            Row r = sheet.createRow(row++);
            r.createCell(0).setCellValue(tc.getTestId());
            r.createCell(1).setCellValue(tc.getModule());
            r.createCell(2).setCellValue(tc.getName());
            r.createCell(3).setCellValue(tc.getPriority());
            r.createCell(4).setCellValue(tc.getActualResult() != null ? tc.getActualResult() : "");
            r.createCell(5).setCellValue(tc.getScreenshotPath() != null ? tc.getScreenshotPath() : "");
            r.createCell(6).setCellValue(tc.getDeviceLogPath() != null ? tc.getDeviceLogPath() : "");
        }
        autoSize(sheet, headers.length);
    }

    private static void writePassRateSheet(XSSFSheet sheet, List<TestCase> testCases, Styles s) {
        Map<String, int[]> stats = new LinkedHashMap<>();
        for (TestCase tc : testCases) {
            int[] c = stats.computeIfAbsent(tc.getModule(), k -> new int[3]);
            c[0]++;
            if ("PASSED".equalsIgnoreCase(tc.getStatus())) c[1]++;
            else if ("FAILED".equalsIgnoreCase(tc.getStatus())) c[2]++;
        }
        String[] headers = {"Module","Total","Passed","Failed","Skipped","Pass Rate","Status"};
        Row hRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell c = hRow.createCell(i); c.setCellValue(headers[i]); c.setCellStyle(s.header);
        }
        int row = 1;
        for (Map.Entry<String, int[]> e : stats.entrySet()) {
            int[] c = e.getValue();
            int skipped = c[0] - c[1] - c[2];
            double rate = c[0] > 0 ? (double) c[1] / c[0] * 100 : 0.0;
            Row r = sheet.createRow(row++);
            r.createCell(0).setCellValue(e.getKey());
            r.createCell(1).setCellValue(c[0]);
            r.createCell(2).setCellValue(c[1]);
            r.createCell(3).setCellValue(c[2]);
            r.createCell(4).setCellValue(skipped);
            r.createCell(5).setCellValue(String.format("%.2f%%", rate));
            Cell statusCell = r.createCell(6);
            if (rate >= 95)      { statusCell.setCellValue("✅ PASS"); statusCell.setCellStyle(s.passed); }
            else if (rate >= 80) { statusCell.setCellValue("⚠️ WARN"); statusCell.setCellStyle(s.skipped); }
            else                 { statusCell.setCellValue("❌ FAIL"); statusCell.setCellStyle(s.failed); }
        }
        autoSize(sheet, headers.length);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Style factory
    // ─────────────────────────────────────────────────────────────────────────

    private static class Styles {
        final CellStyle header, passed, failed, skipped, unexec, title;
        Styles(Workbook wb) {
            header  = makeStyle(wb, new byte[]{0x3B,(byte)0x82,(byte)0xF6}, IndexedColors.WHITE,  true,  12);
            passed  = makeStyle(wb, new byte[]{0x10,(byte)0xB9,(byte)0x81}, IndexedColors.WHITE,  true,  11);
            failed  = makeStyle(wb, new byte[]{(byte)0xEF,0x44,0x44},       IndexedColors.WHITE,  true,  11);
            skipped = makeStyle(wb, new byte[]{(byte)0xF5,(byte)0x9E,0x0B}, IndexedColors.BLACK,  true,  11);
            unexec  = makeStyle(wb, new byte[]{0x64,0x74,(byte)0x8B},       IndexedColors.WHITE,  false, 11);
            title   = makeStyle(wb, new byte[]{0x1E,0x29,0x3B},             IndexedColors.WHITE,  true,  14);
        }
        private CellStyle makeStyle(Workbook wb, byte[] rgb, IndexedColors fg, boolean bold, int size) {
            XSSFCellStyle style = (XSSFCellStyle) wb.createCellStyle();
            XSSFColor color = new XSSFColor(rgb, null);
            style.setFillForegroundColor(color);
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);            style.setAlignment(HorizontalAlignment.CENTER);
            style.setBorderBottom(BorderStyle.THIN); style.setBorderTop(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);   style.setBorderRight(BorderStyle.THIN);
            Font font = wb.createFont();
            font.setColor(fg.getIndex());
            font.setBold(bold);
            font.setFontHeightInPoints((short) size);
            style.setFont(font);
            return style;
        }
    }

    private static void autoSize(Sheet sheet, int cols) {
        for (int i = 0; i < cols; i++) {
            try { sheet.autoSizeColumn(i); } catch (Exception ignored) {}
        }
    }

    private static void saveWorkbook(Workbook wb, String filePath) {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            wb.write(fos);
            LogUtil.log("Excel saved: " + filePath);
        } catch (IOException e) {
            LogUtil.logError("Failed to save Excel: " + filePath, e);
        }
    }
}
