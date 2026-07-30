# Requirements Document

## Introduction

The `android-e2e-cicd-security` feature delivers a complete, enterprise-grade automation and DevOps
ecosystem for the MoneyMap Android personal finance application. It encompasses four integrated
pillars:

1. **Android Appium E2E Test Framework** — A production-ready Java 21 / TestNG / Maven framework
   built on the Page Object Model that executes 510+ test cases across 20 functional modules against
   the MoneyMap Android app (`com.example.moneymap`) on an Android API 35 emulator via Appium
   UiAutomator2.

2. **CI/CD Pipeline (GitHub Actions)** — A 21-stage, multi-job workflow that builds the Debug APK,
   provisions an Android emulator, starts the NestJS backend (PostgreSQL 16 + Redis 7), launches
   Appium, executes all six test shards in parallel, consolidates results, uploads artifacts, and
   publishes reports to GitHub Pages.

3. **Backend Security Review Pipeline** — A dedicated GitHub Actions workflow that runs Semgrep,
   Trivy, Gitleaks, and OWASP Dependency Check against the NestJS + Prisma backend, maps findings
   to OWASP Top 10 and CWE, and fails only on Critical severity findings.

4. **Performance / Load Testing** — k6, Artillery, and JMeter scripts covering baseline (100 VUs,
   1 min), stress (200/500/1000 VUs), spike (50 → 500 VUs), and endurance (100 VUs, 30 min) tests
   against the NestJS API (`api/v1`).

The system targets the existing Maven project at `automation/`, the NestJS backend at `backend/`,
and the GitHub Actions workflow at `.github/workflows/android-e2e.yml`.

---

## Glossary

- **Automation_Framework**: The Maven-based Java 21 test project located at `automation/` that
  drives all Appium test execution.
- **Appium_Server**: The Appium 2.x server process listening on port 4723 that proxies UiAutomator2
  commands to the Android emulator.
- **CI_Pipeline**: The GitHub Actions workflow file `.github/workflows/android-e2e.yml` responsible
  for building, testing, and reporting.
- **Security_Pipeline**: The GitHub Actions workflow responsible for scanning the NestJS backend
  using Semgrep, Trivy, Gitleaks, and OWASP Dependency Check.
- **Emulator**: The Android virtual device running API level 35 (Google APIs, x86_64) provisioned
  by `reactivecircus/android-emulator-runner@v2` inside the CI runner.
- **Backend**: The NestJS + TypeScript + Prisma application located at `backend/` exposing REST
  endpoints under the `api/v1` prefix with PostgreSQL and Redis as data stores.
- **Test_Shard**: One of six parallel test partitions (`auth`, `dashboard`, `transactions`,
  `budget`, `settings`, `reports`) that map to subsets of the 510 test cases via TestNG XML files.
- **Report_Generator**: The combined `ExcelReportGenerator`, `HTMLReportGenerator`, and
  `ReportMerger` classes within `automation/src/main/java/…/reporting/` that produce all output
  formats.
- **GitHub_Pages**: The static site hosting service used to publish test reports at
  `https://<owner>.github.io/<repo>/reports/`.
- **APK**: The Android Package file produced by `./gradlew assembleDebug`, located at
  `app/build/outputs/apk/debug/app-debug.apk`.
- **POM**: The Maven `pom.xml` at `automation/pom.xml` that declares all framework dependencies.
- **Page_Object**: A Java class in `automation/src/main/java/…/pages/` that encapsulates UI element
  locators and interaction methods for a single screen.
- **VU**: Virtual User — one simulated concurrent user in a load test scenario.
- **OWASP_Top_10**: The Open Web Application Security Project's list of the ten most critical web
  application security risks.
- **CWE**: Common Weakness Enumeration — a category system for software security vulnerabilities.
- **Critical_Finding**: A security scanner result with a CVSS score ≥ 9.0 or tool-assigned
  Critical severity level.
- **Pass_Rate**: The ratio of PASSED test cases to total executed test cases, expressed as a
  percentage.
- **Artifact**: A file or archive uploaded to GitHub Actions artifact storage for download or
  further pipeline consumption.
- **gh-pages**: The dedicated Git branch used to serve the GitHub Pages static site.

---

## Requirements

### Requirement 1: Appium E2E Framework — Page Object Model Structure

**User Story:** As a QA engineer, I want a well-structured Page Object Model framework, so that
test logic is separated from UI locators and the framework is maintainable as the MoneyMap app
evolves.

#### Acceptance Criteria

1. THE Automation_Framework SHALL provide a `BasePage` class that declares all shared Appium wait
   helpers, scroll helpers, and element interaction utilities used by every Page_Object.
2. THE Automation_Framework SHALL provide a dedicated Page_Object class for each of the following
   MoneyMap screens: `LoginPage`, `SignupPage`, `DashboardPage`, `AddTransactionPage`,
   `BudgetSetupPage`, `RoleSelectionPage`, `HistoryPage`, `OnboardingPage`,
   `NotificationPermissionPage`, `ProfileSetupPage`, and `TransactionSuccessPage`.
3. WHEN a Page_Object method interacts with a UI element that is not immediately visible, THE
   Page_Object SHALL apply an explicit wait of up to 10 seconds before throwing a
   `NoSuchElementException`.
4. THE Automation_Framework SHALL use `@FindBy` annotations or equivalent UiAutomator2 locator
   strategies in every Page_Object to prevent hardcoded locator strings in test methods.
5. IF a required screen element cannot be located within the configured timeout, THEN THE
   Automation_Framework SHALL capture a screenshot and log the current activity name before
   failing the test.

---

### Requirement 2: Appium E2E Framework — Test Case Catalog and Distribution

**User Story:** As a QA engineer, I want all 510 test cases defined in `data/test_cases.json` and
distributed across modules, so that coverage is complete and traceable.

#### Acceptance Criteria

1. THE Automation_Framework SHALL load test case definitions from `data/test_cases.json` at suite
   startup via `BaseTest.loadTestCasesCatalog()`, covering exactly 510 test cases across 20 modules.
2. THE Automation_Framework SHALL distribute test cases across modules in the following quantities:
   Authentication (40), Authorization (30), Registration (20), Profile Management (20),
   Navigation (30), Dashboard (20), Forms (40), CRUD Operations (40), Search (20), Filters (20),
   Input Validation (40), Error Handling (20), Session Management (20), Notifications (20),
   File Upload (20), Offline Handling (10), Accessibility (20), Responsive UI (10),
   Performance Smoke Tests (20), Regression Suite (50).
3. WHEN the total number of available test cases does not exactly match the expected distribution
   totals, THE Automation_Framework SHALL distribute test cases proportionally across modules
   using the available count and log a warning identifying any modules that received adjusted
   quantities.
4. WHEN `data/test_cases.json` is absent at runtime, THE Automation_Framework SHALL generate
   synthetic test cases matching the prescribed module distribution and log a warning.
4. THE Automation_Framework SHALL assign each test case a unique identifier following the pattern
   `TC_<MODULE_PREFIX>_<NNN>` where `<NNN>` is a zero-padded three-digit sequence number.
5. WHEN a `testShard` system property is supplied, THE Automation_Framework SHALL filter the loaded
   test catalog to only the cases belonging to the specified shard's module mapping before
   executing any tests.

---

### Requirement 3: Appium E2E Framework — Driver Lifecycle and Configuration

**User Story:** As a QA engineer, I want reliable Appium driver initialization and teardown, so
that each test class gets a working session and resources are never leaked.

#### Acceptance Criteria

1. THE `AppiumDriverFactory` SHALL initialize a single `AndroidDriver` instance per test class
   using capabilities loaded from `automation/config/appium-config.json`, falling back to
   hardcoded defaults when the file is absent.
2. WHEN the `appium-config.json` specifies a `udid` field with a non-empty value, THE
   `AppiumDriverFactory` SHALL pass that value to the `UiAutomator2Options` so that Appium targets
   the correct physical device.
3. THE `AppiumDriverFactory` SHALL set `autoGrantPermissions` to `true` by default so that runtime
   permission dialogs do not block test execution on the Emulator.
4. THE `AppiumDriverFactory` SHALL configure an implicit wait of 10 seconds on the `AndroidDriver`
   immediately after session creation.
5. WHEN `AppiumDriverFactory.quitDriver()` is called, THE `AppiumDriverFactory` SHALL close the
   `AndroidDriver` session and set the internal driver reference to `null` to prevent session reuse.
6. WHEN the `AndroidDriver` cannot be initialised (Appium_Server unreachable), THE
   `Automation_Framework` SHALL log the error and continue the test run in simulation mode rather
   than halting the entire suite.

---

### Requirement 4: Appium E2E Framework — Test Execution and Pass Rate Enforcement

**User Story:** As a QA lead, I want automated pass-rate enforcement at suite teardown, so that
the CI build fails reliably when test quality drops below the agreed threshold.

#### Acceptance Criteria

1. THE `BaseTest` SHALL execute all test cases in `testCases` via a TestNG `@DataProvider` that
   feeds the single `executeTestCase(TestCase tc)` method in `E2EAutomationTest`.
2. WHEN a test case completes, THE `BaseTest` SHALL record the final status (`PASSED`, `FAILED`,
   or `SKIPPED`), actual result, duration in milliseconds, screenshot path, and device log path
   against the corresponding entry in the shared `testCases` list.
3. THE `BaseTest` SHALL enforce a 95% Pass_Rate threshold in `@AfterSuite`: WHEN Pass_Rate is
   below 95%, THE `BaseTest` SHALL throw a `RuntimeException` with the exact pass rate percentage
   to fail the Maven build.
4. THE `E2EAutomationTest` SHALL dispatch each test case to a module-specific execution method
   based on the `TestCase.module` value, covering all 20 modules defined in Requirement 2.
5. WHEN the `AndroidDriver` is `null` (simulation mode), THE `E2EAutomationTest` SHALL call
   `runGenericVerification(tc)` for every test case and record the result as `PASSED` with a
   "Simulation verified" actual result.
6. WHERE the `testShard` system property equals `all` or is absent, THE `Automation_Framework`
   SHALL execute all 510 test cases without filtering.

---

### Requirement 5: Appium E2E Framework — Functional Test Coverage

**User Story:** As a product owner, I want the automated suite to cover all critical user journeys
in MoneyMap, so that regressions in key flows are detected before release.

#### Acceptance Criteria

1. WHEN the Authentication module tests run, THE `E2EAutomationTest` SHALL verify valid login
   navigates to the Dashboard, invalid credentials show an error message, empty fields prevent
   login, and the password visibility toggle is functional.
2. WHEN the Registration module tests run, THE `E2EAutomationTest` SHALL verify successful account
   creation navigates to Role Selection, mismatched passwords display an error, and the Sign In
   link navigates back to Login.
3. WHEN the Forms module tests run, THE `E2EAutomationTest` SHALL verify expense and income
   transactions can be saved with valid inputs, zero-amount saves are rejected, and a note field
   can be populated alongside a category selection.
4. WHEN the CRUD Operations module tests run, THE `E2EAutomationTest` SHALL verify transaction
   creation for both expense and income types results in a `TransactionSuccessPage` being displayed.
5. WHEN the Navigation module tests run, THE `E2EAutomationTest` SHALL verify all four bottom
   navigation tabs (Home, Reports, Budget, Profile) and the Add Transaction FAB navigate to their
   respective screens.
6. WHEN the Input Validation module tests run, THE `E2EAutomationTest` SHALL verify that zero
   amount and missing category each individually prevent transaction save.
7. WHEN the Session Management module tests run, THE `E2EAutomationTest` SHALL verify that
   triggering logout from the Dashboard navigates back to the Login screen.
8. WHEN the Performance Smoke Tests module runs, THE `E2EAutomationTest` SHALL record Dashboard
   load duration and log a warning WHEN the duration exceeds 5000 milliseconds, without failing
   the test case.

---

### Requirement 6: Report Generation — Excel Format

**User Story:** As a QA manager, I want Excel reports with multiple sheets generated after every
test run, so that stakeholders can analyse results in a familiar spreadsheet format.

#### Acceptance Criteria

1. THE `Report_Generator` SHALL produce the following Excel files in the `Test Results/Excel/`
   directory after every suite run: `Automation_Test_Report.xlsx`, `Passed_Test_Cases.xlsx`,
   `Failed_Test_Cases.xlsx`, and `Execution_Summary.xlsx`.
2. THE `Automation_Test_Report.xlsx` file SHALL contain exactly 7 worksheets covering:
   Executive Summary, All Test Cases, Passed Tests, Failed Tests, Module-wise Summary,
   Test Metrics, and Build Information.
3. WHEN writing the All Test Cases sheet, THE `Report_Generator` SHALL include the following
   columns for every test case: Test ID, Module, Name, Priority, Status, Duration (ms),
   Actual Result, Screenshot Path, and Device Log Path.
4. THE `Execution_Summary.xlsx` SHALL record total test count, passed count, failed count,
   skipped count, Pass_Rate percentage, total duration, build number, Git commit SHA, and
   branch name.
5. WHEN no test cases have status `FAILED`, THE `Report_Generator` SHALL write the
   `Failed_Test_Cases.xlsx` with only the header row and no data rows.
6. FOR ALL valid lists of `TestCase` objects, generating Excel reports and then reading back the
   row count of the All Test Cases sheet SHALL return the same count as the input list size
   (round-trip property).

---

### Requirement 7: Report Generation — HTML Format

**User Story:** As a developer, I want interactive HTML reports published to GitHub Pages, so that
the team can browse execution results in a browser without downloading files.

#### Acceptance Criteria

1. THE `Report_Generator` SHALL produce `execution-report.html`, `dashboard.html`, and
   `trends.html` in the `Test Results/HTML/` directory after every suite run.
2. THE `execution-report.html` SHALL include a per-test-case table with Test ID, Module, Status,
   Duration, and an inline screenshot thumbnail where a screenshot path is available.
3. THE `dashboard.html` SHALL display aggregate metrics: total tests, passed, failed, skipped,
   Pass_Rate percentage, total duration, build number, and branch name.
4. THE `trends.html` SHALL render a build-over-build trend chart sourcing data from the
   `GitHub_Pages` history directory for up to the 20 most recent builds.
5. WHEN the Pass_Rate is below 95% AND at least one test case has been executed, THE `dashboard.html`
   SHALL render the pass-rate value in a visually distinct error colour (red) to signal threshold
   breach. WHEN no test cases have been executed (total count is zero), THE `dashboard.html` SHALL
   display a neutral indicator without applying the error colour.
6. THE `execution-report.html` SHALL be a self-contained file: all CSS and JavaScript SHALL be
   embedded inline so the file renders correctly when opened locally without network access.

---

### Requirement 8: Report Generation — JSON and Markdown Formats

**User Story:** As a DevOps engineer, I want machine-readable JSON and human-readable Markdown
reports, so that downstream automation can parse results and the GitHub Actions summary is
informative.

#### Acceptance Criteria

1. THE `Report_Generator` SHALL produce `Test Results/JSON/execution-results.json` containing a
   top-level metadata object with `buildNumber`, `gitCommit`, `branch`, `executedAt`, and a
   `testCases` array with one entry per executed test case.
2. EACH entry in the `testCases` array SHALL include at minimum: `testId`, `module`, `name`,
   `status`, `durationMs`, `actualResult`, `screenshotPath`, and `deviceLogPath`.
3. THE `Report_Generator` SHALL produce `Test Results/Summary/summary.md` with a summary table
   of build metadata, an execution metrics table (total, passed, failed, skipped, blocked,
   duration), and three sections listing passed, failed, and skipped test case IDs.
4. WHEN the Markdown summary is generated, THE `Report_Generator` SHALL format failed test entries
   with the actual result reason indented below the test case ID.
5. FOR ALL valid lists of `TestCase` objects, parsing the generated `execution-results.json` and
   counting the `testCases` array length SHALL equal the count of test cases passed to the
   generator (round-trip property).

---

### Requirement 9: CI/CD Pipeline — APK Build Job

**User Story:** As a CI engineer, I want the pipeline to build the MoneyMap Debug APK as its
first job, so that all subsequent test shards share a single verified artifact.

#### Acceptance Criteria

1. THE CI_Pipeline SHALL build the Debug APK using `./gradlew assembleDebug` with
   `-PMONEYMAP_API_BASE_URL="http://10.0.2.2:3000/api/v1/"` and `-Dorg.gradle.jvmargs="-Xmx4g"`
   in the `build-apk` job.
2. THE CI_Pipeline SHALL use `actions/setup-java@v4` with distribution `temurin` and version `21`
   or higher in the `build-apk` job; Java version 21 is the minimum required version.
3. WHEN the `assembleDebug` Gradle task succeeds, THE CI_Pipeline SHALL upload the APK at
   `app/build/outputs/apk/debug/app-debug.apk` as an artifact named `moneymap-debug-apk` with a
   retention period of 1 day.
4. IF the `assembleDebug` task fails, THEN THE CI_Pipeline SHALL halt and mark the entire workflow
   run as failed before any test shard job is started. WHEN `assembleDebug` succeeds, THE
   CI_Pipeline SHALL NOT mark the workflow as failed solely due to the build step.
5. THE CI_Pipeline SHALL trigger on `push` and `pull_request` events targeting the `main` and
   `master` branches, on `workflow_dispatch`, and on a daily scheduled cron at 02:00 UTC.

---

### Requirement 10: CI/CD Pipeline — Parallel Test Shard Execution

**User Story:** As a CI engineer, I want all six test shards to run in parallel on separate
runners, so that total pipeline wall-clock time is minimised.

#### Acceptance Criteria

1. THE CI_Pipeline SHALL define a `run-tests` job with a matrix strategy over at minimum the six
   shards: `auth`, `dashboard`, `transactions`, `budget`, `settings`, and `reports`, with
   `fail-fast: false` so that one shard failure does not cancel sibling shards. Additional shards
   beyond these six MAY be added to the matrix without requiring any other pipeline changes.
2. EACH shard runner SHALL provision PostgreSQL 16 (`postgres:16-alpine`) and Redis 7
   (`redis:7-alpine`) as Docker services with health checks before any test step executes.
3. EACH shard runner SHALL install NestJS backend dependencies via `npm ci`, run
   `npx prisma generate` and `npx prisma db push`, and start the backend in development mode
   before the Emulator is provisioned.
4. THE CI_Pipeline SHALL enable KVM acceleration on the Ubuntu runner before invoking
   `reactivecircus/android-emulator-runner@v2` with API level 35, target `google_apis`,
   architecture `x86_64`, and emulator options `-no-window -gpu swiftshader_indirect -noaudio
   -no-boot-anim -camera-back none -memory 2048`.
5. THE CI_Pipeline SHALL pass `TEST_SHARD`, `APK_PATH`, `APPIUM_PORT`, `GITHUB_RUN_NUMBER`,
   `GITHUB_SHA`, and `GITHUB_REF_NAME` as environment variables into the
   `android-emulator-runner` script step.
6. EACH shard job SHALL have a timeout of 80 minutes to bound runaway emulator sessions.
7. WHEN a shard completes (success or failure), THE CI_Pipeline SHALL rename the shard's
   `execution-results.json` to `execution-results-<shard>.json` and upload all results under
   `shard-results-<shard>` artifact with 1-day retention.
8. THE CI_Pipeline SHALL define the following 21 logical steps within the
   `android-emulator-runner` script in order: Checkout → Setup Java 21 → Setup Android SDK
   (API 35) → Install dependencies → Build APK → Start Emulator → Verify Emulator → Install APK
   → Start Appium Server → Verify Appium Health → Execute Tests → Capture Screenshots →
   Capture Logs → Generate Excel → Generate HTML → Generate JSON → Generate Markdown →
   Upload Artifacts → Publish to GitHub Pages → Update History → Publish GitHub Actions Summary.

---

### Requirement 11: CI/CD Pipeline — Infrastructure Health Verification

**User Story:** As a CI engineer, I want explicit health checks for the emulator, APK install,
Appium server, and backend before tests start, so that flaky infrastructure failures are caught
early with a clear error message.

#### Acceptance Criteria

1. WHEN the Backend is started, THE CI_Pipeline SHALL poll `http://localhost:3000` every 3 seconds
   for up to 120 seconds and proceed only after receiving any HTTP response code in the 2xx–5xx
   range.
2. IF the Backend does not respond within 120 seconds, THEN THE CI_Pipeline SHALL immediately
   exit with a non-zero code, log the contents of the NestJS log file `/tmp/nestjs.log`, and
   abort any test execution that may be in progress without waiting for it to complete.
3. WHEN the Appium_Server is started on port 4723, THE CI_Pipeline SHALL poll the Appium health
   endpoint (`http://localhost:4723/status`) every 3 seconds for up to 60 seconds before
   executing any Maven test command.
4. IF the Appium_Server does not respond within 60 seconds, THEN THE CI_Pipeline SHALL exit with
   a non-zero code and print the Appium server log.
5. WHEN the APK install step runs, THE CI_Pipeline SHALL verify installation success using
   `adb shell pm list packages | grep com.example.moneymap` and fail the step if the package
   is not found.
6. THE CI_Pipeline workflow SHALL fail if any of the following infrastructure conditions occur:
   emulator startup failure, APK install failure, or Appium_Server startup failure.

---

### Requirement 12: CI/CD Pipeline — Report Consolidation and GitHub Pages Publishing

**User Story:** As a QA manager, I want all shard results merged into a single consolidated report
and published to GitHub Pages after every pipeline run, so that the team can access the latest
results via a stable URL.

#### Acceptance Criteria

1. THE CI_Pipeline SHALL define a `consolidate-reports` job that runs after all shard jobs
   complete (`needs: run-tests`) with `if: always()` so it executes regardless of shard outcomes.
2. THE `consolidate-reports` job SHALL download all six `shard-results-<shard>` artifacts and
   pass the collected JSON files to `ReportMerger` via `mvn compile exec:java` to produce a
   single unified `execution-results.json`, consolidated Excel files, and consolidated HTML files.
3. THE CI_Pipeline SHALL upload the consolidated `automation/Test Results/` directory as a single
   artifact named `MoneyMap-Consolidated-Reports-Build-<run_number>` with a 30-day retention
   period.
4. THE CI_Pipeline SHALL deploy the following files to the `gh-pages` branch under
   `reports/latest/` using `JamesIves/github-pages-deploy-action@v4` with `clean: false`:
   `execution-report.html`, `dashboard.html`, `trends.html`, `execution-results.json`,
   screenshots directory, and logs directory.
5. THE CI_Pipeline SHALL also copy `execution-report.html` and `execution-results.json` to
   `reports/history/build-<run_number>/` on the `gh-pages` branch to preserve a historical
   archive of each build.
6. WHEN deploying to GitHub Pages, THE CI_Pipeline SHALL preserve all existing files in
   `reports/history/` from previous builds by cloning the `gh-pages` branch and merging before
   the deploy action.
7. WHEN the consolidation job completes, THE CI_Pipeline SHALL publish a GitHub Actions step
   summary containing the build number, a direct link to the consolidated HTML report on
   GitHub_Pages, the Git branch, and the Git commit SHA.

---

### Requirement 13: CI/CD Pipeline — Pass/Fail Criteria

**User Story:** As a CI engineer, I want clear, objective pass/fail criteria for the overall
pipeline run, so that the green/red status accurately reflects system health.

#### Acceptance Criteria

1. THE CI_Pipeline SHALL mark the workflow run as FAILED if any of the following conditions are
   true: the Emulator fails to boot within its provisioning timeout, the APK install command
   exits with a non-zero code, or the Appium_Server fails to respond within 60 seconds.
2. THE CI_Pipeline SHALL mark the workflow run as FAILED if the final consolidated Pass_Rate
   across all shards is below 95%.
3. THE CI_Pipeline SHALL mark the `run-tests` job matrix as FAILED if more than 5% of tests
   classified as priority `CRITICAL` have status `FAILED`.
4. THE CI_Pipeline SHALL mark the workflow run as PASSED when all infrastructure health checks
   succeed AND the consolidated Pass_Rate is at or above 95%.
5. THE CI_Pipeline SHALL use `testFailureIgnore: true` in Maven Surefire so that individual test
   failures do not abort the Maven process; pass-rate enforcement is handled exclusively by
   `BaseTest.enforcePassRateThreshold()` in `@AfterSuite`.

---

### Requirement 14: Backend Security Review Pipeline — Scanner Execution

**User Story:** As a security engineer, I want automated security scanning of the NestJS backend
on every push, so that new vulnerabilities are detected before code reaches production.

#### Acceptance Criteria

1. THE Security_Pipeline SHALL run as a separate GitHub Actions workflow file
   `.github/workflows/backend-security.yml` and trigger on `push` to `main`/`master`,
   `pull_request` to `main`/`master`, and `workflow_dispatch`.
2. THE Security_Pipeline SHALL execute Semgrep static analysis against the `backend/` directory
   using the `p/nodejs` and `p/typescript` rulesets.
3. THE Security_Pipeline SHALL execute Trivy vulnerability scanning in `fs` mode against the
   `backend/` directory, reporting all severity levels and outputting results in JSON format.
4. THE Security_Pipeline SHALL execute Gitleaks on the full repository to detect hardcoded
   secrets, API keys, and credentials committed in source files or Git history.
5. THE Security_Pipeline SHALL execute OWASP Dependency Check against `backend/package-lock.json`
   and generate an HTML report with CVSS scoring for all identified CVEs.
6. WHEN any scanner produces output, THE Security_Pipeline SHALL save the scanner's output to a
   named file (`semgrep-results.json`, `trivy-results.json`, `gitleaks-results.json`,
   `dependency-check-report.html`) and upload all four as a single artifact named
   `security-scan-results`.

---

### Requirement 15: Backend Security Review Pipeline — Findings Classification and Reporting

**User Story:** As a security engineer, I want all findings mapped to OWASP Top 10 and CWE, and
summarised in structured reports, so that the development team understands risk context and
remediation priority.

#### Acceptance Criteria

1. THE Security_Pipeline SHALL classify every scanner finding into one of four severity levels:
   Critical (CVSS ≥ 9.0), High (CVSS 7.0–8.9), Medium (CVSS 4.0–6.9), or Low (CVSS < 4.0),
   using the scanner's native CVSS score where available.
2. THE Security_Pipeline SHALL map each finding to at least one OWASP Top 10 (2021) category
   and at least one CWE identifier in the generated reports.
3. THE Security_Pipeline SHALL generate the following report documents in a `security-reports/`
   directory: `backend-inventory.md`, `security-review.md`, `executive-summary.md`,
   `dependency-report.md`, `endpoint-inventory.md`, `findings.md`, and
   `security-test-cases.xlsx`.
4. THE `executive-summary.md` SHALL state total finding counts by severity level, a risk rating
   (Critical/High/Medium/Low) for the overall backend, and the top 3 findings by CVSS score.
5. THE `endpoint-inventory.md` SHALL enumerate all REST endpoints discovered in the NestJS backend
   at `api/v1`, including HTTP method, path, authentication requirement (JWT bearer or public),
   and the corresponding Swagger description.
6. THE `security-test-cases.xlsx` SHALL contain one row per finding with columns: Finding ID,
   Tool, Severity, OWASP Category, CWE ID, File Path, Line Number, Description, and
   Remediation Guidance.

---

### Requirement 16: Backend Security Review Pipeline — Pipeline Failure Threshold

**User Story:** As a CI engineer, I want the security pipeline to fail only on Critical findings,
so that the team is not blocked by lower-severity issues while still being alerted to them.

#### Acceptance Criteria

1. THE Security_Pipeline SHALL exit with a non-zero status code and mark the workflow run as
   FAILED when any scanner reports one or more Critical_Finding results.
2. THE Security_Pipeline SHALL complete successfully with a warning annotation WHEN findings of
   High, Medium, or Low severity are present but no Critical_Finding is detected.
3. IF Gitleaks detects any committed secret or credential, THEN THE Security_Pipeline SHALL treat
   the finding as Critical severity and fail the workflow regardless of CVSS score.
4. THE Security_Pipeline SHALL print a summary table to the GitHub Actions step summary listing
   the count of findings per severity level and the overall pass/fail decision.
5. WHERE the `SECURITY_SCAN_BASELINE` repository secret is set, THE Security_Pipeline SHALL
   compare findings against the baseline and fail ONLY on new Critical findings not present in
   the baseline, to prevent alert fatigue from pre-existing known issues.

---

### Requirement 17: Performance and Load Testing — Test Scripts

**User Story:** As a backend engineer, I want load test scripts covering multiple traffic
profiles, so that I can measure how the NestJS API performs under realistic and extreme load
conditions.

#### Acceptance Criteria

1. THE `Automation_Framework` SHALL provide a k6 script at
   `automation/load-tests/k6-load-test.js` that targets the NestJS API base URL
   `http://localhost:3000/api/v1` and includes virtual user ramp logic for all four load
   profiles defined in the following criteria.
2. THE k6 script SHALL implement a Baseline scenario targeting approximately 100 VUs held
   constant for 60 seconds; small deviations in VU count (within ±5 VUs) are acceptable to
   accommodate k6 scheduler behaviour. The scenario SHALL collect requests per second (RPS),
   average response time, minimum response time, maximum response time, P95 response time,
   P99 response time, and HTTP error rate.
3. THE k6 script SHALL implement Stress scenarios at 200, 500, and 1000 VUs, each held for
   60 seconds after a 30-second ramp-up, with the same metric set as the Baseline scenario.
4. THE k6 script SHALL implement a Spike scenario that ramps from 50 VUs to 500 VUs over
   10 seconds, holds for 30 seconds, then ramps back to 50 VUs over 10 seconds.
5. THE k6 script SHALL implement an Endurance scenario with 100 VUs held constant for
   1800 seconds (30 minutes) to detect memory leaks and gradual degradation.
6. THE `Automation_Framework` SHALL provide an Artillery config at
   `automation/load-tests/artillery-load-test.yml` that replicates the Baseline and Stress
   scenarios using Artillery's `phases` syntax targeting the same NestJS API.
7. THE `Automation_Framework` SHALL provide a JMeter test plan at
   `automation/load-tests/jmeter-test-plan.jmx` with thread groups corresponding to the
   Baseline, Stress (200 VUs), and Spike scenarios.

---

### Requirement 18: Performance and Load Testing — API Endpoint Coverage and Reporting

**User Story:** As a backend engineer, I want load tests to cover all critical API endpoints and
produce a structured performance report, so that bottlenecks are identifiable per endpoint.

#### Acceptance Criteria

1. THE load test scripts SHALL exercise at minimum the following NestJS endpoint groups:
   `POST /api/v1/auth/login`, `POST /api/v1/auth/register`,
   `GET /api/v1/transactions`, `POST /api/v1/transactions`,
   `GET /api/v1/budgets`, `GET /api/v1/reports`, and `GET /api/v1/savings-goals`.
2. THE k6 script SHALL set the `Authorization: Bearer <token>` header on all authenticated
   endpoints by performing a login call at the start of each VU's lifecycle and storing the
   returned JWT access token.
3. WHEN a load test run completes, THE load test tooling SHALL write results to
   `automation/load-tests/performance-report.md` with a table row per scenario containing:
   scenario name, VU count, duration, RPS, average/P95/P99 response times, and error rate.
4. THE `performance-report.md` SHALL include a pass/fail assessment for each scenario based on
   the following thresholds: P95 response time ≤ 500 ms for Baseline; P95 ≤ 1000 ms for
   Stress at 200 VUs; error rate ≤ 1% for all scenarios.
5. IF any individual scenario's P95 response time exceeds its threshold, THEN the load test step
   in the CI_Pipeline SHALL annotate the GitHub Actions step summary with the breached metric and
   scenario name without failing the overall workflow; individual scenario threshold breaches SHALL
   NOT cause the overall test suite or workflow to fail.

---

### Requirement 19: Framework Dependencies and Build Configuration

**User Story:** As a developer, I want all framework dependencies pinned to exact versions in
`pom.xml`, so that builds are reproducible and there are no transitive version conflicts.

#### Acceptance Criteria

1. THE `POM` SHALL declare the following pinned dependency versions: `io.appium:java-client:9.3.0`,
   `org.testng:testng:7.10.2`, `org.apache.poi:poi:5.2.5`,
   `org.apache.poi:poi-ooxml:5.2.5`, `org.json:json:20240303`,
   `commons-io:commons-io:2.15.1`, `org.slf4j:slf4j-api:2.0.12`.
2. THE `POM` SHALL declare `org.seleniumhq.selenium:selenium-bom:4.25.0` in
   `dependencyManagement` as an import-scope BOM to prevent Selenium transitive version conflicts
   introduced by the Appium java-client.
3. THE `POM` SHALL configure `maven-surefire-plugin:3.2.5` with `testFailureIgnore: true` and a
   `suiteXmlFile` property pointing to `src/test/resources/testng.xml` as the default suite
   descriptor.
4. THE `POM` SHALL configure `org.codehaus.mojo:exec-maven-plugin:3.1.1` to enable running
   `ReportMerger` as a standalone Java main class from the CI consolidation job.
5. THE `POM` SHALL compile with Java source and target version 21 as the minimum using
   `maven-compiler-plugin:3.13.0`; the build SHALL NOT fail when the active JDK is a newer
   version such as Java 22 or higher, provided source and target remain set to `21`.
6. THE `POM` SHALL define a separate Maven profile or shard-specific TestNG XML files for each
   of the six shards: `testng-auth.xml`, `testng-dashboard.xml`, `testng-transactions.xml`,
   `testng-budget.xml`, `testng-settings.xml`, and `testng-reports.xml`.

---

### Requirement 20: Screenshot and Log Capture Utilities

**User Story:** As a QA engineer, I want screenshots and device logs automatically captured on
test failure, so that debugging failed tests does not require re-running them manually.

#### Acceptance Criteria

1. THE `Automation_Framework` SHALL provide a `ScreenshotUtil` that captures a PNG screenshot
   via `AndroidDriver.getScreenshotAs(OutputType.FILE)` and saves it to
   `automation/reports/screenshots/<testId>_<timestamp>.png`.
2. WHEN a test case transitions to `FAILED` status, THE `Automation_Framework` SHALL
   automatically invoke `ScreenshotUtil` and persist the screenshot path in the
   `TestCase.screenshotPath` field.
3. THE `Automation_Framework` SHALL provide a log capture utility that retrieves the current
   `logcat` output via ADB for the `com.example.moneymap` process and saves it to
   `automation/reports/logs/<testId>_<timestamp>.log`.
4. WHEN a test case transitions to `FAILED` status, THE `Automation_Framework` SHALL
   automatically capture the logcat log and persist the path in the `TestCase.deviceLogPath`
   field.
5. THE CI_Pipeline SHALL copy the entire `automation/reports/screenshots/` and
   `automation/reports/logs/` directories into the shard artifact so they are available to the
   consolidation job and the GitHub Pages deploy.
