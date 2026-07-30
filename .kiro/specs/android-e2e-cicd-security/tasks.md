# Implementation Plan

## Overview

Implementation of the MoneyMap enterprise-grade Android E2E automation framework, CI/CD pipeline,
backend security review pipeline, and load testing scripts. Tasks are ordered by dependency:
foundation utilities first, then Page Objects, then test infrastructure, then reporting,
then CI/CD and security pipelines, and finally load testing and documentation.

## Tasks

- [x] 1. Verify and complete `TestCase.java` model — all 18 fields
  - Open `automation/src/main/java/com/example/moneymap/automation/model/TestCase.java` and confirm all 18 fields exist: `testId`, `module`, `name`, `priority`, `preconditions`, `steps`, `testData`, `expectedResult`, `actualResult`, `status`, `durationMs`, `screenshotPath`, `deviceLogPath`, `pageSourcePath`, `appiumLogPath`, `locatorUsed`, `currentActivity`, `currentPackage`.
  - Add any missing runtime output fields (`pageSourcePath`, `appiumLogPath`, `locatorUsed`, `currentActivity`, `currentPackage`) with empty-string defaults.
  - Ensure the 8-argument constructor initialises all runtime fields to `""` / `0` / `"NOT_RUN"`.
  - Verify public getters and setters exist for every field.
  - _Requirements: REQ-2, REQ-8_


- [x] 2. Verify and complete `appium-config.json`
  - Open `automation/config/appium-config.json` and confirm all 14 keys are present: `appiumUrl`, `platformName`, `automationName`, `deviceName`, `udid`, `appPackage`, `appActivity`, `app`, `noReset`, `fullReset`, `autoGrantPermissions`, `newCommandTimeout`, `systemPort`, `adbExecTimeout`.
  - Ensure default values match the design: `appiumUrl: "http://127.0.0.1:4723"`, `appPackage: "com.example.moneymap"`, `appActivity: "com.example.moneymap.MainActivity"`, `autoGrantPermissions: true`, `newCommandTimeout: 300`, `systemPort: 8200`, `adbExecTimeout: 120000`.
  - Add any missing keys with correct defaults without changing keys that already match.
  - _Requirements: REQ-3_

- [x] 3. Create `ScreenshotUtil.java`
  - Create `automation/src/main/java/com/example/moneymap/automation/utils/ScreenshotUtil.java`.
  - Implement `public static String captureScreenshot(AndroidDriver driver, String prefix)`: call `driver.getScreenshotAs(OutputType.FILE)`, resolve save directory to `automation/reports/screenshots/` (falling back to `reports/screenshots/` if `automation/` does not exist), create directories with `mkdirs()`.
  - Name the file `<prefix>_<yyyyMMdd_HHmmss_SSS>.png` using `SimpleDateFormat`.
  - Copy the temp file to the target path using `commons-io FileUtils.copyFile`, return the relative path string; return empty string and log the error on any exception.
  - _Requirements: REQ-20_


- [x] 4. Create/verify `LogUtil.java` — device and Appium log capture
  - Verify `automation/src/main/java/com/example/moneymap/automation/utils/LogUtil.java` has methods: `log(String)`, `logWarning(String)`, `logError(String, Throwable)`, `logTestStart(String, String)`, `logTestFail(String, String, long)`.
  - Add `public static String captureDeviceLogs(AndroidDriver driver, String prefix)`: retrieve logcat entries via `driver.manage().logs().get("logcat")`, write to `automation/reports/logs/<prefix>_<timestamp>.log`, return relative path.
  - Add `public static String captureAppiumLogs(AndroidDriver driver, String prefix)`: read recent lines from Appium server log at `automation/reports/logs/appium-server.log`, copy to `automation/reports/logs/<prefix>_appium_<timestamp>.log`, return relative path.
  - Create directories with `mkdirs()` before writing; return empty string on exception.
  - _Requirements: REQ-20_

- [x] 5. Create `LoginPage.java`
  - Create `automation/src/main/java/com/example/moneymap/automation/pages/LoginPage.java` extending `BasePage`.
  - Define locators for: email field (XPath hint/text), password field (XPath hint/text), Sign In button, Sign Up link, Forgot Password link, Google Sign-In button, password visibility toggle, and error message text.
  - Implement methods: `login(String email, String password)`, `enterEmail(String)`, `enterPassword(String)`, `clickSignIn()`, `clickSignUp()`, `clickForgotPassword()`, `togglePasswordVisibility()`, `isLoginScreenDisplayed()`, `isErrorMessageDisplayed(String fragment)`, `isGoogleButtonVisible()`.
  - `isLoginScreenDisplayed()` returns true when both the email and password fields are visible.
  - _Requirements: REQ-1, REQ-5_


- [x] 6. Create `SignupPage.java`
  - Create `automation/src/main/java/com/example/moneymap/automation/pages/SignupPage.java` extending `BasePage`.
  - Define locators for: full name field, email field, password field, confirm password field, Create Account button, Sign In link, error message text, T&C checkbox.
  - Implement methods: `register(String name, String email, String password, String confirmPassword)`, `enterName(String)`, `enterEmail(String)`, `enterPassword(String)`, `enterConfirmPassword(String)`, `clickCreateAccount()`, `clickLoginLink()`, `isSignupScreenDisplayed()`, `isErrorMessageDisplayed(String fragment)`.
  - `isSignupScreenDisplayed()` returns true when the Create Account button is visible.
  - _Requirements: REQ-1, REQ-5_

- [x] 7. Create `DashboardPage.java`
  - Create `automation/src/main/java/com/example/moneymap/automation/pages/DashboardPage.java` extending `BasePage`.
  - Define locators for: balance text (contains `₹` or `$`), Add Transaction FAB (content-desc or resource-id), bottom nav tabs (Reports, Budget, Profile, Home), See All Transactions link, Logout button.
  - Implement methods: `isDashboardLoaded()`, `getAvailableBalance()`, `clickAddTransactionButton()`, `navigateToReports()`, `navigateToBudget()`, `navigateToProfile()`, `navigateToHome()`, `logout()`, `clickSeeAllTransactions()`.
  - `isDashboardLoaded()` returns true when the Add Transaction FAB is visible.
  - `logout()` navigates to the Profile tab first, then taps the Logout button.
  - _Requirements: REQ-1, REQ-5_


- [x] 8. Create `AddTransactionPage.java`
  - Create `automation/src/main/java/com/example/moneymap/automation/pages/AddTransactionPage.java` extending `BasePage`.
  - Define locators for: amount EditText (hint "Amount"), Expense toggle button, Income toggle button, category selector spinner/text, note/description field, Save button, Close/Back button.
  - Implement methods: `isAddTransactionScreenDisplayed()`, `selectExpense()`, `selectIncome()`, `enterAmount(String)`, `selectCategory(String categoryName)`, `enterNote(String)`, `clickSave()`, `clickClose()`, `getDisplayedAmount()`.
  - Implement convenience method `createTransaction(String type, String amount, String category, String note)` that calls selectExpense/Income, enterAmount, selectCategory, enterNote, clickSave.
  - `selectCategory(String)` scrolls the category list if needed and taps the matching item.
  - _Requirements: REQ-1, REQ-5_

- [x] 9. Create `HistoryPage.java`
  - Create `automation/src/main/java/com/example/moneymap/automation/pages/HistoryPage.java` extending `BasePage`.
  - Define locators for: RecyclerView transaction list items (`//androidx.recyclerview.widget.RecyclerView/android.view.ViewGroup`), Search icon, Filter button.
  - Implement methods: `isHistoryScreenDisplayed()`, `getTransactionCount()`, `tapTransaction(int index)`, `clickSearchIcon()`, `enterSearchQuery(String query)`.
  - `getTransactionCount()` returns the size of `findElements(transactionListLocator)`.
  - `tapTransaction(int index)` finds all items and taps the element at the given zero-based index.
  - _Requirements: REQ-1, REQ-5_


- [x] 10. Create `OnboardingPage.java`
  - Create `automation/src/main/java/com/example/moneymap/automation/pages/OnboardingPage.java` extending `BasePage`.
  - Define locators for: Skip button, Get Started button, Next button, slide title text (contains "Track", "Budget", "Reports").
  - Implement methods: `isOnboardingDisplayed()`, `clickSkip()`, `clickGetStarted()`, `swipeToNextSlide()`, `getCurrentSlideTitle()`.
  - `isOnboardingDisplayed()` returns true when Skip or Get Started is visible.
  - `swipeToNextSlide()` performs a horizontal left swipe using `UiScrollable` or driver swipe gesture.
  - _Requirements: REQ-1_

- [x] 11. Create `NotificationPermissionPage.java`
  - Create `automation/src/main/java/com/example/moneymap/automation/pages/NotificationPermissionPage.java` extending `BasePage`.
  - Define locators covering both the system Android permission dialog (resource-id `com.android.permissioncontroller:id/permission_allow_button` / `permission_deny_button`) and the in-app permission screen (`byText("Allow")` / `byText("Not Now")`).
  - Implement methods: `isNotificationPermissionScreenDisplayed()`, `clickAllow()`, `clickNotNow()`.
  - `clickAllow()` tries the in-app button first; if not found, tries the system dialog button.
  - `clickNotNow()` tries the in-app "Not Now" button first; if not found, tries the system "Don't allow" button.
  - _Requirements: REQ-1, REQ-5_


- [x] 12. Create `ProfileSetupPage.java`
  - Create `automation/src/main/java/com/example/moneymap/automation/pages/ProfileSetupPage.java` extending `BasePage`.
  - Define locators for: name field, currency spinner, monthly income/allowance/budget field, organisation/institution/company field, Next button.
  - Implement methods: `isProfileSetupDisplayed()`, `enterName(String)`, `selectCurrency(String)`, `enterMonthlyAmount(String)`, `enterOrganisation(String)`, `clickNext()`.
  - `isProfileSetupDisplayed()` returns true when the Next button is visible.
  - `selectCurrency(String)` taps the currency spinner and then taps the matching text item.
  - _Requirements: REQ-1_

- [x] 13. Create `TransactionSuccessPage.java`
  - Create `automation/src/main/java/com/example/moneymap/automation/pages/TransactionSuccessPage.java` extending `BasePage`.
  - Define locator for success indicator: XPath matching text containing "Transaction Added", "Success", "saved", or "added", or content-desc containing "success".
  - Define locator for Back to Home button: `byText("Back to Home")` or `byText("Done")` or `byId("btn_home")`.
  - Implement methods: `isSuccessDisplayed()` using `isElementDisplayed(successLocator)`, `clickBackToHome()`.
  - `isSuccessDisplayed()` must return false quickly (uses `shortWait`) to avoid blocking validation tests that assert the screen is NOT shown.
  - _Requirements: REQ-1, REQ-5_


- [x] 14. Create `TestNGListener.java` — auto-capture on failure
  - Create `automation/src/test/java/com/example/moneymap/automation/listeners/TestNGListener.java` implementing `ITestListener` and `ISuiteListener`.
  - In `onTestFailure(ITestResult result)`: cast `result.getInstance()` to `BaseTest`, retrieve `driver`; if driver is not null, extract `testId` from `result.getParameters()[0]` if it is a `TestCase`, otherwise derive from `result.getName()`.
  - Call `ScreenshotUtil.captureScreenshot(driver, testId)` to get the screenshot path.
  - Call `LogUtil.captureDeviceLogs(driver, testId)` to get the device log path.
  - Call `LogUtil.captureAppiumLogs(driver, testId)` to get the Appium log path.
  - Call `BaseTest.updateTestCase(testId, "FAILED", failureMessage, durationMs, screenshotPath, logPath, "", appiumLogPath, "", "", "")` to persist all paths.
  - _Requirements: REQ-20, REQ-4_

- [ ] 15. Create all 7 TestNG XML suite files
  - Create `automation/src/test/resources/testng.xml` — full suite; `<suite parallel="none">`; includes `TestNGListener` listener; single `<test>` containing `E2EAutomationTest`.
  - Create `automation/src/test/resources/testng-auth.xml` — Auth shard; same structure, `<test name="Auth Shard">`.
  - Create `automation/src/test/resources/testng-dashboard.xml` — Dashboard shard.
  - Create `automation/src/test/resources/testng-transactions.xml` — Transactions shard.
  - Create `automation/src/test/resources/testng-budget.xml` — Budget shard.
  - Create `automation/src/test/resources/testng-settings.xml` — Settings shard.
  - Create `automation/src/test/resources/testng-reports.xml` — Reports shard.
  - Every XML file must declare the `TestNGListener` via `<listeners>` and reference `com.example.moneymap.automation.tests.E2EAutomationTest` — shard filtering is handled in Java via the `testShard` system property, not in XML.
  - _Requirements: REQ-19, REQ-10_


- [~] 16. Verify and complete `test_cases.json` — all 510 test cases
  - Count current entries in `automation/data/test_cases.json` and compare against required module distribution: Authentication (40), Authorization (30), Registration (20), Profile Management (20), Navigation (30), Dashboard (20), Forms (40), CRUD Operations (40), Search (20), Filters (20), Input Validation (40), Error Handling (20), Session Management (20), Notifications (20), File Upload (20), Offline Handling (10), Accessibility (20), Responsive UI (10), Performance Smoke Tests (20), Regression Suite (50) = 510 total.
  - For each module that is short, append the missing JSON objects following the 8-field schema: `testId`, `module`, `name`, `priority`, `preconditions`, `steps`, `testData`, `expectedResult`.
  - Use the correct `testId` prefix per module: `TC_AUTH_`, `TC_AUTHZ_`, `TC_REG_`, `TC_PROF_`, `TC_NAV_`, `TC_DAS_`, `TC_FORM_`, `TC_CRUD_`, `TC_SRCH_`, `TC_FILT_`, `TC_VAL_`, `TC_ERR_`, `TC_SESS_`, `TC_NOTIF_`, `TC_FILE_`, `TC_OFF_`, `TC_ACC_`, `TC_RESP_`, `TC_PERF_`, `TC_REG_SUITE_` — zero-padded 3-digit sequence numbers.
  - Ensure `priority` values are one of `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`.
  - Validate JSON syntax parses without errors after edits.
  - _Requirements: REQ-2_

- [~] 17. Create `ReportMerger.java`
  - Create `automation/src/main/java/com/example/moneymap/automation/reporting/ReportMerger.java` with a `public static void main(String[] args)` entry point.
  - `args[0]` = directory containing `execution-results-<shard>.json` files; `args[1]` = output directory for the merged report set.
  - Read all JSON files matching `execution-results-*.json` in `args[0]`; for each file parse the `testCases` array and deserialise into `TestCase` objects; collect `buildNumber`, `branch`, `gitCommit` from the top-level metadata of the first successfully parsed file.
  - Merge all `TestCase` lists into one; if a `testId` appears in multiple shards, keep the last occurrence (most recent shard wins).
  - Call `ExcelReportGenerator.generateReports(mergedList, args[1] + "/Excel", buildNumber, branch, gitCommit)`.
  - Call `HTMLReportGenerator.generateReports(mergedList, args[1] + "/HTML", buildNumber, branch, gitCommit)`.
  - Write a merged `execution-results.json` to `args[1] + "/JSON/"` using the same schema as `BaseTest.generateJsonReport`.
  - Write `args[1] + "/Summary/summary.md"` using the same logic as `BaseTest.generateMarkdownSummary`.
  - _Requirements: REQ-12, REQ-8_


- [~] 18. Verify `ExcelReportGenerator.java` — 7-sheet master + 4 output files
  - Open `automation/src/main/java/com/example/moneymap/automation/reporting/ExcelReportGenerator.java` and confirm it produces 4 XLSX files: `Automation_Test_Report.xlsx`, `Passed_Test_Cases.xlsx`, `Failed_Test_Cases.xlsx`, `Execution_Summary.xlsx`.
  - Confirm `Automation_Test_Report.xlsx` contains exactly 7 sheets: Executed Test Cases, Passed Tests, Failed Tests, Skipped Tests, Execution Metrics, Defect Summary, Pass Rate Summary.
  - Confirm test sheets have 11 columns: Test ID, Module, Test Name, Priority, Preconditions, Steps, Test Data, Expected Result, Actual Result, Status, Duration (ms).
  - Confirm Pass Rate Summary sheet has per-module rows with Total, Passed, Failed, Skipped, Pass Rate percentage, and Status badge columns.
  - Confirm Execution Metrics sheet includes build number, branch, Git commit, total count, pass rate, and duration.
  - Confirm cells are colour-coded: PASSED green (`#10B981`), FAILED red (`#EF4444`), SKIPPED amber (`#F59E0B`), NOT_RUN slate (`#64748B`).
  - Add any missing sheets or columns identified above.
  - _Requirements: REQ-6_

- [~] 19. Verify `HTMLReportGenerator.java` — 3 self-contained HTML files
  - Open `automation/src/main/java/com/example/moneymap/automation/reporting/HTMLReportGenerator.java` and confirm it produces `execution-report.html`, `dashboard.html`, and `trends.html`.
  - Confirm `execution-report.html` includes a per-test-case table (Test ID, Module, Status, Duration, screenshot thumbnail) and two Chart.js charts (doughnut for overall pass/fail, bar for per-module).
  - Confirm `dashboard.html` shows aggregate metrics cards and per-module progress bars; confirm the pass-rate metric renders in red when below 95% and at least one test has been executed; confirm neutral display when total test count is zero.
  - Confirm `trends.html` renders a line chart with up to 20 build data points and a 95% threshold reference line.
  - Confirm Chart.js 4.x is bundled via CDN `<script>` tag within the file so it is self-contained when online; add a visible fallback message for offline environments.
  - Add any missing output files or charts identified above.
  - _Requirements: REQ-7_


- [~] 20. Verify and enhance `android-e2e.yml` — CI/CD pipeline
  - Confirm the workflow triggers on `push`/`pull_request` to `main`/`master`, `workflow_dispatch`, and scheduled cron `0 2 * * *`.
  - Confirm `build-apk` job uses `actions/setup-java@v4` (temurin, Java 21+), runs `./gradlew assembleDebug --stacktrace --no-daemon -PMONEYMAP_API_BASE_URL="http://10.0.2.2:3000/api/v1/" -Dorg.gradle.jvmargs="-Xmx4g"`, and uploads artifact `moneymap-debug-apk` with 1-day retention.
  - Confirm `run-tests` matrix has 6 shards (`auth`, `dashboard`, `transactions`, `budget`, `settings`, `reports`), `fail-fast: false`, 80-minute timeout, postgres:16-alpine + redis:7-alpine services with health checks, and passes `TEST_SHARD`, `APK_PATH`, `APPIUM_PORT`, `GITHUB_RUN_NUMBER`, `GITHUB_SHA`, `GITHUB_REF_NAME`, `ANDROID_API_LEVEL` into the emulator runner step.
  - Confirm backend health check polls `http://localhost:3000` every 3 seconds for up to 120 seconds and immediately exits with non-zero code and logs `/tmp/nestjs.log` on timeout.
  - Confirm `consolidate-reports` job: has `if: always()`, downloads all 6 shard artifacts, runs `ReportMerger` via `mvn compile exec:java`, uploads consolidated reports with 30-day retention, deploys to GitHub Pages via `JamesIves/github-pages-deploy-action@v4` with `clean: false`, preserves history, publishes step summary with live report URL.
  - Add any missing steps or fix any gaps identified above.
  - _Requirements: REQ-9, REQ-10, REQ-11, REQ-12, REQ-13_

- [~] 21. Verify and enhance `run-e2e-tests.sh` — emulator runner script
  - Confirm the script creates required directories: `automation/reports/screenshots`, `automation/reports/logs`, `automation/Test Results/{Excel,HTML,JSON,Summary}`.
  - Confirm emulator boot wait polls `sys.boot_completed=1` via ADB with a 180-second timeout; fails with exit code 1 if emulator does not boot.
  - Confirm APK install step runs `adb install -r $APK_PATH` and verifies with `adb shell pm list packages | grep com.example.moneymap`; fails if package not found.
  - Confirm Appium install: `npm install -g appium@3.0.0` + `appium driver install uiautomator2@5.0.0`; starts Appium server with `--port $APPIUM_PORT --relaxed-security`; polls `/status` endpoint every 2 seconds for up to 90 seconds; fails if not healthy.
  - Confirm Maven test command passes `-DsuiteXmlFile=src/test/resources/testng-${TEST_SHARD}.xml -DtestShard=${TEST_SHARD}` and `-DGITHUB_RUN_NUMBER`, `-DGITHUB_SHA`, `-DGITHUB_REF_NAME` as system properties.
  - Confirm `grep "BUILD FAILURE"` in Maven output sets `TEST_EXIT=1` and the script exits with that code.
  - _Requirements: REQ-10, REQ-11_


- [~] 22. Create `.github/workflows/backend-security.yml` — Security Pipeline
  - Create the workflow with triggers on `push`/`pull_request` to `main`/`master` and `workflow_dispatch`; set `fetch-depth: 0` on checkout so Gitleaks can scan full Git history.
  - Add Semgrep SAST step using `semgrep/semgrep-action@v1` with `config: "p/nodejs p/typescript"`; save output to `semgrep-results.json`.
  - Add Trivy filesystem scan using `aquasecurity/trivy-action@master` with `scan-type: fs`, `scan-ref: backend/`, `format: json`, `output: trivy-results.json`, `exit-code: 0` (threshold evaluated later).
  - Add Gitleaks secret detection using `gitleaks/gitleaks-action@v2`; any secret detected is treated as Critical.
  - Add OWASP Dependency Check using `dependency-check/Dependency-Check_Action@main` with `project: MoneyMap-Backend`, `path: backend`, `format: "HTML JSON"`, `out: dependency-check-results`.
  - Add threshold evaluation step: parse JSON outputs for Critical severity counts; exit 1 if any Critical finding or Gitleaks secret found; exit 0 with warning annotations for High/Medium/Low.
  - Add step summary publishing with Markdown table (Severity × Tool counts + PASSED/FAILED decision row).
  - Add artifact upload step for all scan outputs + `security-reports/` with 30-day retention.
  - _Requirements: REQ-14, REQ-15, REQ-16_

- [~] 23. Create `security-reports/` placeholder files
  - Create `security-reports/backend-inventory.md` with header and placeholder sections: Technology Stack, Architecture, Authentication, Database, API Endpoints list.
  - Create `security-reports/executive-summary.md` with sections: Total Findings table by severity, Overall Risk Rating, Top 3 Findings table (Finding ID, CVSS, Title, OWASP, Remediation).
  - Create `security-reports/security-review.md` with a findings table template (columns: Finding ID, Severity, Type, CWE, OWASP, File, Endpoint, Description, Evidence, Impact, Remediation).
  - Create `security-reports/dependency-report.md` with a dependency table template (columns: Package, Version, Latest, CVE Count, Highest Severity, Status).
  - Create `security-reports/endpoint-inventory.md` with a REST endpoint table (columns: Method, Path, Auth Required, Description, Module).
  - Create `security-reports/findings.md` with a consolidated findings table (columns: Finding ID, Tool, Severity, OWASP Category, CWE ID, File Path, Line, Description).
  - _Requirements: REQ-15_


- [~] 24. Create `automation/load-tests/k6-load-test.js`
  - Define `export const options` with 6 named scenarios: `baseline` (constant-vus, 100 VUs, 60s), `stress200`/`stress500`/`stress1000` (ramping-vus: 30s ramp-up → 60s hold → 10s ramp-down), `spike` (10s ramp 50→500, 30s hold, 10s back to 50), `endurance` (constant-vus, 100 VUs, 1800s).
  - Define thresholds: `http_req_duration{scenario:baseline}` P95 < 500ms, `http_req_duration{scenario:stress200}` P95 < 1000ms, `http_req_failed` error rate < 0.01 for all six scenarios.
  - Implement `getAuthToken(baseUrl)` helper: POST to `/auth/login` with `demo@moneymap.com`/`Password123!`, extract and return `data.accessToken` from response JSON.
  - Implement default export VU function: call `getAuthToken`, then exercise 7 endpoints in sequence — POST login, GET transactions (authenticated), POST transaction, GET budgets, GET reports, GET savings-goals — all with `Authorization: Bearer <token>` header.
  - Read base URL from `__ENV.BASE_URL` with default `http://localhost:3000/api/v1`.
  - _Requirements: REQ-17, REQ-18_

- [~] 25. Create `automation/load-tests/artillery-load-test.yml`
  - Set `config.target: "http://localhost:3000/api/v1"` and `http.timeout: 30`.
  - Define `phases`: Baseline (duration: 60, arrivalRate: 10) and Stress-200 (duration: 60, arrivalRate: 20, rampTo: 50).
  - Set `defaults.headers` with `Content-Type: application/json`.
  - Define one scenario "Auth and Transactions Flow" with flow: POST `/auth/login` → capture `$.data.accessToken` as `token` → GET `/transactions` with `Authorization: Bearer {{ token }}` → POST `/transactions` with expense body → GET `/budgets` with auth header.
  - _Requirements: REQ-17_


- [~] 26. Create `automation/load-tests/jmeter-test-plan.jmx`
  - Create a valid JMeter 5.x JMX XML file with three Thread Groups: Baseline (100 threads, 10s ramp-up, 60s duration), Stress-200 (200 threads, 30s ramp-up, 60s duration), Spike (ramp 50→500 over 10s, hold 30s, ramp back to 50).
  - Each Thread Group must contain: HTTP Header Manager (`Content-Type: application/json`), HTTP Sampler for `POST /api/v1/auth/login` with JSON body, JSON Extractor for `accessToken`, HTTP Sampler for `GET /api/v1/transactions` with `${accessToken}` in Authorization header, HTTP Sampler for `POST /api/v1/transactions`, HTTP Sampler for `GET /api/v1/budgets`, Response Assertion (status codes 200/201).
  - Add a Summary Report listener at the test plan level.
  - Set HTTP Request defaults server to `localhost`, port `3000`.
  - _Requirements: REQ-17_

- [~] 27. Create `automation/load-tests/performance-report.md`
  - Create the file with a Test Configuration section (Target URL, Tool, Date placeholder, Environment).
  - Add `## Scenario Results` table with columns: Scenario, VUs, Duration, RPS, Avg (ms), P95 (ms), P99 (ms), Error Rate, P95 Threshold, Status — include placeholder rows for all 6 scenarios.
  - Add `## Threshold Assessment` section listing the threshold rule per scenario (P95 ≤ 500ms for Baseline, P95 ≤ 1000ms for Stress-200, error rate ≤ 1% for all) with PASS/FAIL placeholder.
  - Add `## Observations` section with sub-headings: Memory Behaviour, CPU Usage, Connection Pool Saturation.
  - Add `## Recommendations` section with a placeholder for bottleneck analysis notes.
  - _Requirements: REQ-18_


- [~] 28. Create `automation/.github-scripts/generate-pages.py`
  - Implement `main()` that reads all subdirectories matching `build-*` inside `deploy_site/reports/history/`, sorted by build number descending (newest first).
  - For each build directory, generate an HTML table row: build number, link to `execution-report.html`, link to `execution-results.json`, and directory modification timestamp.
  - Write `deploy_site/index.html` — self-contained HTML listing all builds, a prominent "Latest Report" link to `reports/latest/execution-report.html`, and a project header.
  - Write `deploy_site/reports/index.html` — a similar build listing scoped to the reports subdirectory.
  - Handle the first-run case where `deploy_site/reports/history/` does not exist by generating an empty-state page without raising an exception.
  - _Requirements: REQ-12_

- [~] 29. Update `automation/README.md` — Complete usage and CI guide
  - Update Prerequisites: JDK 21+, Node.js 20+, Appium 3.x (`npm install -g appium@3.0.0` + `appium driver install uiautomator2@5.0.0`), Android SDK API 35 (system-images;android-35;google_apis;x86_64), Maven 3.8+.
  - Add/verify "Local Setup" section: clone → set `ANDROID_HOME` → build APK (`./gradlew assembleDebug`) → edit `config/appium-config.json` (set `udid`) → start emulator → start Appium → run tests.
  - Add "Running Full Suite": `cd automation && mvn clean test -DsuiteXmlFile=src/test/resources/testng.xml`.
  - Add "Running Individual Shards": `mvn clean test -DsuiteXmlFile=src/test/resources/testng-<shard>.xml -DtestShard=<shard>` for each of the 6 shards.
  - Add "Report Locations" table: Excel → `Test Results/Excel/`, HTML → `Test Results/HTML/`, JSON → `Test Results/JSON/`, Summary → `Test Results/Summary/`, Screenshots → `reports/screenshots/`, Logs → `reports/logs/`.
  - Add "CI/CD Pipeline" section: describe the 3-job flow (build-apk → 6-shard matrix → consolidate-reports) and GitHub Pages URL format `https://<owner>.github.io/<repo>/reports/latest/execution-report.html`.
  - Verify Troubleshooting section covers: Appium not starting, emulator not booting, APK not found, backend health check timeout, pass rate below 95% threshold.
  - _Requirements: REQ-19_


## Task Dependency Graph

```json
{
  "waves": [
    {
      "wave": 1,
      "description": "Foundation — utilities and configuration",
      "tasks": ["1", "2", "3", "4"]
    },
    {
      "wave": 2,
      "description": "Page Objects — all 9 new screen classes",
      "tasks": ["5", "6", "7", "8", "9", "10", "11", "12", "13"],
      "dependsOn": ["1", "2", "3", "4"]
    },
    {
      "wave": 3,
      "description": "Test Infrastructure — listener and TestNG XML files",
      "tasks": ["14", "15"],
      "dependsOn": ["3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"]
    },
    {
      "wave": 4,
      "description": "Test Data — complete 510 test cases",
      "tasks": ["16"],
      "dependsOn": ["15"]
    },
    {
      "wave": 5,
      "description": "Reporting — ReportMerger, verify Excel and HTML generators",
      "tasks": ["17", "18", "19"],
      "dependsOn": ["1", "16"]
    },
    {
      "wave": 6,
      "description": "CI/CD Pipeline — workflow and shell runner script",
      "tasks": ["20", "21"],
      "dependsOn": ["14", "15", "17", "18", "19"]
    },
    {
      "wave": 7,
      "description": "Security Pipeline and Load Testing (parallel)",
      "tasks": ["22", "23", "24", "25", "26", "27"],
      "dependsOn": ["20"]
    },
    {
      "wave": 8,
      "description": "GitHub Pages generator",
      "tasks": ["28"],
      "dependsOn": ["20"]
    },
    {
      "wave": 9,
      "description": "Documentation",
      "tasks": ["29"],
      "dependsOn": ["20", "21", "22", "23", "24", "25", "26", "27", "28"]
    }
  ]
}
```

## Notes

- Tasks 1–4 are pure Java utility classes with no Appium driver dependency — they can be compiled and tested in isolation without a running emulator.
- Tasks 5–13 (Page Objects) extend `BasePage` which already exists at `automation/src/main/java/.../pages/BasePage.java`. Do not modify `BasePage` unless a required helper method is missing.
- `BudgetSetupPage.java` and `RoleSelectionPage.java` already exist and are fully implemented — no tasks are created for them. Verify they compile correctly after the `BasePage` dependency is confirmed.
- `AppiumDriverFactory.java`, `ExcelReportGenerator.java`, `HTMLReportGenerator.java`, `BaseTest.java`, and `E2EAutomationTest.java` all exist. Tasks 18–21 are verification+enhancement tasks — only add what is missing.
- The `testShard` system property filter in `BaseTest.shouldIncludeTestCase()` already implements the shard routing logic. The TestNG XML files (Task 15) do not need to replicate this — they just reference `E2EAutomationTest`.
- For load testing scripts (Tasks 24–26), a running NestJS backend with a seeded `demo@moneymap.com` account is required. The k6 and Artillery scripts assume this user exists.
- The security pipeline (Task 22) requires the `GITHUB_TOKEN` secret (available by default in Actions). `GITLEAKS_LICENSE` is optional — the action runs in free tier mode without it.
- All file paths in tasks are relative to the repository root unless stated otherwise.
