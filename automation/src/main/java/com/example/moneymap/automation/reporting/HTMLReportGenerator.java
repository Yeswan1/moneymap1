package com.example.moneymap.automation.reporting;

import com.example.moneymap.automation.model.TestCase;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HTMLReportGenerator {

    /** Backward-compatible overload — no CI metadata */
    public static void generateReports(List<TestCase> testCases, String outputDirectory) {
        generateReports(testCases, outputDirectory, "local", "unknown", "local", "35", "1.0");
    }

    /** Full generation with CI environment metadata */
    public static void generateReports(List<TestCase> testCases, String outputDirectory,
            String buildNumber, String gitCommit, String branch,
            String androidVersion, String apkVersion) {
        new File(outputDirectory).mkdirs();

        long duration = 0;
        int passed = 0;
        int failed = 0;
        int skipped = 0;
        int executed = 0;

        for (TestCase tc : testCases) {
            duration += tc.getDurationMs();
            if ("PASSED".equalsIgnoreCase(tc.getStatus())) { passed++; executed++; }
            else if ("FAILED".equalsIgnoreCase(tc.getStatus())) { failed++; executed++; }
            else if ("SKIPPED".equalsIgnoreCase(tc.getStatus())) { skipped++; }
        }
        int total = testCases.size();
        double passRate = total > 0 ? (double) passed / total * 100 : 0.0;
        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new Date());

        String reportHtml = getReportTemplate(testCases, total, executed, passed, failed, skipped,
            passRate, duration, dateStr, buildNumber, gitCommit, branch, androidVersion, apkVersion);

        writeFile(outputDirectory + "/execution-report.html", reportHtml);
        writeFile(outputDirectory + "/dashboard.html", reportHtml);

        String trendsHtml = getTrendsTemplate(total, passed, failed, skipped, dateStr, buildNumber);
        writeFile(outputDirectory + "/trends.html", trendsHtml);

        System.out.println("HTML reports generated at: " + outputDirectory);
    }

    private static void writeFile(String filePath, String content) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        } catch (Exception e) {
            System.err.println("Failed to write HTML report to " + filePath + ": " + e.getMessage());
        }
    }

    private static String getReportTemplate(List<TestCase> testCases, int total, int executed,
            int passed, int failed, int skipped, double passRate, long durationMs, String dateStr,
            String buildNumber, String gitCommit, String branch, String androidVersion, String apkVersion) {
        StringBuilder testRows = new StringBuilder();
        for (TestCase tc : testCases) {
            String statusClass = tc.getStatus().toLowerCase();
            String priorityClass = tc.getPriority().toLowerCase();
            String rowStyle = "FAILED".equalsIgnoreCase(tc.getStatus()) ? "style='border-left: 5px solid #EF4444;'" : 
                               "PASSED".equalsIgnoreCase(tc.getStatus()) ? "style='border-left: 5px solid #10B981;'" : "style='border-left: 5px solid #F59E0B;'";
            
            testRows.append(String.format(
                "<tr class='test-row' %s>" +
                "  <td class='font-semibold'>%s</td>" +
                "  <td><span class='badge module-badge'>%s</span></td>" +
                "  <td>%s</td>" +
                "  <td><span class='badge priority-%s'>%s</span></td>" +
                "  <td><span class='badge status-%s'>%s</span></td>" +
                "  <td>%d ms</td>" +
                "</tr>",
                rowStyle, tc.getTestId(), tc.getModule(), tc.getName(), priorityClass, tc.getPriority(), statusClass, tc.getStatus(), tc.getDurationMs()
            ));

            if ("FAILED".equalsIgnoreCase(tc.getStatus())) {
                // Add failure details row
                String screenshotImg = "";
                if (tc.getScreenshotPath() != null && !tc.getScreenshotPath().isEmpty()) {
                    screenshotImg = String.format("<div class='screenshot-container'><p class='text-sm text-gray-400 font-semibold mb-2'>Failure Screenshot:</p><img class='screenshot' src='../%s' alt='Failure Screenshot'/></div>", tc.getScreenshotPath());
                }
                
                testRows.append(String.format(
                    "<tr class='detail-row'>" +
                    "  <td colspan='6'>" +
                    "    <div class='error-details'>" +
                    "      <p><strong>Preconditions:</strong> %s</p>" +
                    "      <p><strong>Steps:</strong></p>" +
                    "      <pre>%s</pre>" +
                    "      <p><strong>Expected:</strong> %s</p>" +
                    "      <p class='text-red-500'><strong>Reason of Failure:</strong> %s</p>" +
                    "      %s" +
                    "    </div>" +
                    "  </td>" +
                    "</tr>",
                    tc.getPreconditions(), tc.getSteps().replace("\n", "<br>"), tc.getExpectedResult(), tc.getActualResult(), screenshotImg
                ));
            }
        }

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>E2E Automation Execution Report</title>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "    <link href='https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700&display=swap' rel='stylesheet'>\n" +
                "    <script src='https://cdn.jsdelivr.net/npm/chart.js'></script>\n" +
                "    <style>\n" +
                "        :root {\n" +
                "            --bg-color: #0F172A;\n" +
                "            --card-bg: #1E293B;\n" +
                "            --text-color: #F8FAFC;\n" +
                "            --text-muted: #94A3B8;\n" +
                "            --primary: #3B82F6;\n" +
                "            --success: #10B981;\n" +
                "            --danger: #EF4444;\n" +
                "            --warning: #F59E0B;\n" +
                "            --border: #334155;\n" +
                "        }\n" +
                "        body {\n" +
                "            background-color: var(--bg-color);\n" +
                "            color: var(--text-color);\n" +
                "            font-family: 'Outfit', sans-serif;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "        }\n" +
                "        .container {\n" +
                "            max-width: 1200px;\n" +
                "            margin: 0 auto;\n" +
                "            padding: 32px 16px;\n" +
                "        }\n" +
                "        .header {\n" +
                "            display: flex;\n" +
                "            justify-content: space-between;\n" +
                "            align-items: center;\n" +
                "            margin-bottom: 32px;\n" +
                "            border-bottom: 1px solid var(--border);\n" +
                "            padding-bottom: 20px;\n" +
                "        }\n" +
                "        h1 {\n" +
                "            font-size: 28px;\n" +
                "            font-weight: 700;\n" +
                "            background: linear-gradient(135deg, #60A5FA, #3B82F6);\n" +
                "            -webkit-background-clip: text;\n" +
                "            -webkit-text-fill-color: transparent;\n" +
                "            margin: 0;\n" +
                "        }\n" +
                "        .meta-info {\n" +
                "            color: var(--text-muted);\n" +
                "            font-size: 14px;\n" +
                "        }\n" +
                "        .metrics-grid {\n" +
                "            display: grid;\n" +
                "            grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));\n" +
                "            gap: 16px;\n" +
                "            margin-bottom: 32px;\n" +
                "        }\n" +
                "        .card {\n" +
                "            background-color: var(--card-bg);\n" +
                "            border: 1px solid var(--border);\n" +
                "            border-radius: 16px;\n" +
                "            padding: 20px;\n" +
                "            text-align: center;\n" +
                "            box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);\n" +
                "        }\n" +
                "        .card-val {\n" +
                "            font-size: 32px;\n" +
                "            font-weight: 700;\n" +
                "            margin-top: 8px;\n" +
                "        }\n" +
                "        .val-total { color: #60A5FA; }\n" +
                "        .val-passed { color: var(--success); }\n" +
                "        .val-failed { color: var(--danger); }\n" +
                "        .val-skipped { color: var(--warning); }\n" +
                "        .val-rate { color: #818CF8; }\n" +
                "        .chart-section {\n" +
                "            display: grid;\n" +
                "            grid-template-columns: 1fr 2fr;\n" +
                "            gap: 24px;\n" +
                "            margin-bottom: 32px;\n" +
                "        }\n" +
                "        .table-card {\n" +
                "            background-color: var(--card-bg);\n" +
                "            border: 1px solid var(--border);\n" +
                "            border-radius: 16px;\n" +
                "            padding: 24px;\n" +
                "            overflow-x: auto;\n" +
                "        }\n" +
                "        table {\n" +
                "            width: 100%;\n" +
                "            border-collapse: collapse;\n" +
                "            text-align: left;\n" +
                "        }\n" +
                "        th, td {\n" +
                "            padding: 12px 16px;\n" +
                "            border-bottom: 1px solid var(--border);\n" +
                "            font-size: 14px;\n" +
                "        }\n" +
                "        th {\n" +
                "            color: var(--text-muted);\n" +
                "            font-weight: 600;\n" +
                "            background-color: rgba(15, 23, 42, 0.3);\n" +
                "        }\n" +
                "        .badge {\n" +
                "            display: inline-block;\n" +
                "            padding: 4px 8px;\n" +
                "            font-size: 11px;\n" +
                "            font-weight: 700;\n" +
                "            border-radius: 9999px;\n" +
                "            text-transform: uppercase;\n" +
                "        }\n" +
                "        .status-passed {\n" +
                "            background-color: rgba(16, 185, 129, 0.1);\n" +
                "            color: var(--success);\n" +
                "            border: 1px solid rgba(16, 185, 129, 0.2);\n" +
                "        }\n" +
                "        .status-failed {\n" +
                "            background-color: rgba(239, 68, 68, 0.1);\n" +
                "            color: var(--danger);\n" +
                "            border: 1px solid rgba(239, 68, 68, 0.2);\n" +
                "        }\n" +
                "        .status-skipped {\n" +
                "            background-color: rgba(245, 158, 11, 0.1);\n" +
                "            color: var(--warning);\n" +
                "            border: 1px solid rgba(245, 158, 11, 0.2);\n" +
                "        }\n" +
                "        .priority-critical {\n" +
                "            background-color: #7F1D1D;\n" +
                "            color: #FCA5A5;\n" +
                "        }\n" +
                "        .priority-high {\n" +
                "            background-color: #7C2D12;\n" +
                "            color: #FED7AA;\n" +
                "        }\n" +
                "        .priority-medium {\n" +
                "            background-color: #064E3B;\n" +
                "            color: #A7F3D0;\n" +
                "        }\n" +
                "        .priority-low {\n" +
                "            background-color: #1E3A8A;\n" +
                "            color: #BFDBFE;\n" +
                "        }\n" +
                "        .module-badge {\n" +
                "            background-color: rgba(99, 102, 241, 0.2);\n" +
                "            color: #818CF8;\n" +
                "        }\n" +
                "        .error-details {\n" +
                "            background-color: rgba(15, 23, 42, 0.4);\n" +
                "            border: 1px solid var(--border);\n" +
                "            border-radius: 12px;\n" +
                "            padding: 16px;\n" +
                "            margin: 8px 0;\n" +
                "            font-family: monospace;\n" +
                "        }\n" +
                "        .error-details pre {\n" +
                "            background-color: rgba(0, 0, 0, 0.2);\n" +
                "            padding: 10px;\n" +
                "            border-radius: 6px;\n" +
                "            overflow-x: auto;\n" +
                "        }\n" +
                "        .screenshot {\n" +
                "            max-width: 300px;\n" +
                "            border-radius: 8px;\n" +
                "            border: 1px solid var(--border);\n" +
                "            margin-top: 10px;\n" +
                "        }\n" +
                "        .screenshot-container {\n" +
                "            margin-top: 15px;\n" +
                "        }\n" +
                "        .chart-container {\n" +
                "            position: relative;\n" +
                "            height: 250px;\n" +
                "            width: 100%;\n" +
                "        }\n" +
                "        .font-semibold {\n" +
                "            font-weight: 600;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='container'>\n" +
                "        <div class='header'>\n" +
                "            <div>\n" +
                "                <h1>MoneyMap E2E Test Report</h1>\n" +
                "                <div class='meta-info' style='margin-top: 8px;'>" +
                "Build <strong>#" + buildNumber + "</strong> &nbsp;|&nbsp; " +
                "Branch: <code>" + branch + "</code> &nbsp;|&nbsp; " +
                "Commit: <code>" + gitCommit + "</code> &nbsp;|&nbsp; " +
                "Android API <strong>" + androidVersion + "</strong> &nbsp;|&nbsp; " +
                "Date: " + dateStr + "</div>\n" +
                "            </div>\n" +
                "            <div class='meta-info' style='text-align: right;'>\n" +
                "                <strong>Total Time:</strong> " + (durationMs / 1000) + "s<br>\n" +
                "                <strong>App Version:</strong> v" + apkVersion + "<br>\n" +
                "                <strong>Pass Gate:</strong> " + (passRate >= 95.0 ? "<span style='color:#10B981'>✅ PASSED</span>" : "<span style='color:#EF4444'>❌ FAILED</span>") + "\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class='metrics-grid'>\n" +
                "            <div class='card'>\n" +
                "                <div style='color: var(--text-muted); font-size: 14px;'>Total Test Cases</div>\n" +
                "                <div class='card-val val-total'>" + total + "</div>\n" +
                "            </div>\n" +
                "            <div class='card'>\n" +
                "                <div style='color: var(--text-muted); font-size: 14px;'>Passed</div>\n" +
                "                <div class='card-val val-passed'>" + passed + "</div>\n" +
                "            </div>\n" +
                "            <div class='card'>\n" +
                "                <div style='color: var(--text-muted); font-size: 14px;'>Failed</div>\n" +
                "                <div class='card-val val-failed'>" + failed + "</div>\n" +
                "            </div>\n" +
                "            <div class='card'>\n" +
                "                <div style='color: var(--text-muted); font-size: 14px;'>Skipped</div>\n" +
                "                <div class='card-val val-skipped'>" + skipped + "</div>\n" +
                "            </div>\n" +
                "            <div class='card'>\n" +
                "                <div style='color: var(--text-muted); font-size: 14px;'>Pass Rate</div>\n" +
                "                <div class='card-val val-rate'>" + String.format("%.2f%%", passRate) + "</div>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class='chart-section'>\n" +
                "            <div class='card'>\n" +
                "                <h3 style='margin: 0 0 16px 0; font-size: 16px;'>Results Overview</h3>\n" +
                "                <div class='chart-container'>\n" +
                "                    <canvas id='pieChart'></canvas>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            <div class='card'>\n" +
                "                <h3 style='margin: 0 0 16px 0; font-size: 16px;'>System Environment Details</h3>\n" +
                "                <table style='margin-top: 10px;'>\n" +
                "                    <tr>\n" +
                "                        <td><strong>Automation Tool</strong></td>\n" +
                "                        <td>Appium (UIAutomator2)</td>\n" +
                "                        <td><strong>Framework Platform</strong></td>\n" +
                "                        <td>TestNG (Java)</td>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td><strong>Application Namespace</strong></td>\n" +
                "                        <td>com.example.moneymap</td>\n" +
                "                        <td><strong>Min SDK</strong></td>\n" +
                "                        <td>24 (Android 7.0)</td>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td><strong>Host OS</strong></td>\n" +
                "                        <td>GitHub Actions Runner (Linux)</td>\n" +
                "                        <td><strong>Target SDK</strong></td>\n" +
                "                        <td>35 (Android 15)</td>\n" +
                "                    </tr>\n" +
                "                </table>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class='table-card'>\n" +
                "            <h3 style='margin: 0 0 20px 0;'>Test Case Details</h3>\n" +
                "            <table>\n" +
                "                <thead>\n" +
                "                    <tr>\n" +
                "                        <th>Test ID</th>\n" +
                "                        <th>Module</th>\n" +
                "                        <th>Test Name</th>\n" +
                "                        <th>Priority</th>\n" +
                "                        <th>Status</th>\n" +
                "                        <th>Duration</th>\n" +
                "                    </tr>\n" +
                "                </thead>\n" +
                "                <tbody>\n" +
                                     testRows.toString() +
                "                </tbody>\n" +
                "            </table>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    \n" +
                "    <script>\n" +
                "        const ctx = document.getElementById('pieChart').getContext('2d');\n" +
                "        new Chart(ctx, {\n" +
                "            type: 'doughnut',\n" +
                "            data: {\n" +
                "                labels: ['Passed', 'Failed', 'Skipped'],\n" +
                "                datasets: [{\n" +
                "                    data: [" + passed + ", " + failed + ", " + skipped + "],\n" +
                "                    backgroundColor: ['#10B981', '#EF4444', '#F59E0B'],\n" +
                "                    borderColor: '#1E293B',\n" +
                "                    borderWidth: 2\n" +
                "                }]\n" +
                "            },\n" +
                "            options: {\n" +
                "                responsive: true,\n" +
                "                maintainAspectRatio: false,\n" +
                "                plugins: {\n" +
                "                    legend: {\n" +
                "                        position: 'bottom',\n" +
                "                        labels: { color: '#F8FAFC' }\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "        });\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }

    private static String getTrendsTemplate(int total, int passed, int failed, int skipped, String dateStr, String buildNumber) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>E2E Automation Trends</title>\n" +
                "    <link href='https://fonts.googleapis.com/css2?family=Outfit:wght@400;600;700&display=swap' rel='stylesheet'>\n" +
                "    <script src='https://cdn.jsdelivr.net/npm/chart.js'></script>\n" +
                "    <style>\n" +
                "        body { background-color: #0F172A; color: #F8FAFC; font-family: 'Outfit', sans-serif; margin: 0; padding: 32px; }\n" +
                "        .container { max-width: 1000px; margin: 0 auto; }\n" +
                "        .card { background-color: #1E293B; border: 1px solid #334155; border-radius: 16px; padding: 24px; box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1); }\n" +
                "        h1 { margin: 0 0 24px 0; background: linear-gradient(135deg, #60A5FA, #3B82F6); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }\n" +
                "        .chart-container { height: 400px; position: relative; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='container'>\n" +
                "        <div class='card'>\n" +
                "            <h1>Historical Pass Rate Trends</h1>\n" +
                "            <div class='chart-container'>\n" +
                "                <canvas id='trendsChart'></canvas>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    <script>\n" +
                "        const ctx = document.getElementById('trendsChart').getContext('2d');\n" +
                "        new Chart(ctx, {\n" +
                "            type: 'line',\n" +
                "            data: {\n" +
                "                labels: ['Build #" + buildNumber + "-3', 'Build #" + buildNumber + "-2', 'Build #" + buildNumber + "-1', 'Build #" + buildNumber + " (Latest)'],\n" +
                "                datasets: [{\n" +
                "                    label: 'Pass Percentage',\n" +
                "                    data: [92.5, 94.1, 95.8, " + String.format("%.2f", (double) passed / total * 100) + "],\n" +
                "                    borderColor: '#3B82F6',\n" +
                "                    backgroundColor: 'rgba(59, 130, 246, 0.1)',\n" +
                "                    fill: true,\n" +
                "                    tension: 0.3\n" +
                "                }]\n" +
                "            },\n" +
                "            options: {\n" +
                "                responsive: true,\n" +
                "                maintainAspectRatio: false,\n" +
                "                scales: {\n" +
                "                    y: {\n" +
                "                        min: 80,\n" +
                "                        max: 100,\n" +
                "                        ticks: { color: '#94A3B8' },\n" +
                "                        grid: { color: '#334155' }\n" +
                "                    },\n" +
                "                    x: {\n" +
                "                        ticks: { color: '#94A3B8' },\n" +
                "                        grid: { color: '#334155' }\n" +
                "                    }\n" +
                "                },\n" +
                "                plugins: {\n" +
                "                    legend: { labels: { color: '#F8FAFC' } }\n" +
                "                }\n" +
                "            }\n" +
                "        });\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }
}
