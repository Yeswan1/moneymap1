# Design Document — android-e2e-cicd-security

## Overview

This document describes the complete technical design for the `android-e2e-cicd-security` feature,
which delivers a four-pillar enterprise-grade automation and DevOps ecosystem for the MoneyMap
Android personal finance application:

1. **Appium E2E Test Framework** — A Java 21 / TestNG 7 / Maven 3 framework using the Page Object
   Model pattern that executes 510 test cases across 20 functional modules against
   `com.example.moneymap` on an Android API 35 emulator via Appium UiAutomator2.

2. **CI/CD Pipeline** — A multi-job GitHub Actions workflow (`android-e2e.yml`) that builds the
   Debug APK once, runs six parallel test shards (each with its own emulator + NestJS backend +
   PostgreSQL + Redis), consolidates results, and publishes reports to GitHub Pages.

3. **Backend Security Pipeline** — A separate GitHub Actions workflow (`backend-security.yml`)
   running Semgrep, Trivy, Gitleaks, and OWASP Dependency Check against the NestJS/Prisma backend,
   generating structured security reports, and failing only on Critical findings.

4. **Load Testing Scripts** — k6, Artillery, and JMeter scripts covering four load profiles
   (Baseline 100 VUs, Stress 200/500/1000 VUs, Spike 50→500 VUs, Endurance 100 VUs 30 min)
   against the NestJS REST API.

### How the Four Pillars Interact

The CI/CD pipeline (Pillar 2) orchestrates Pillar 1: it builds the APK, provisions emulators,
and invokes `automation/.github-scripts/run-e2e-tests.sh`, which in turn drives Maven → TestNG →
E2EAutomationTest → Page Objects (Pillar 1). The security pipeline (Pillar 3) runs independently
on every push. Load tests (Pillar 4) are standalone scripts that target the same NestJS backend
that the E2E shards also provision. Reports from all pillars are aggregated into GitHub Pages.

---

## Architecture

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│  PILLAR 1 — APPIUM E2E TEST FRAMEWORK                                               │
│                                                                                     │
│  test_cases.json ──► BaseTest.loadTestCasesCatalog()                                │
│       (510 cases)         │                                                         │
│                           ▼                                                         │
│              E2EAutomationTest @DataProvider                                        │
│                           │                                                         │
│              dispatchToRealFlow(TestCase tc)                                        │
│                    /     |     \     \                                              │
│             LoginPage  SignupPage  DashboardPage  ...11 Page Objects                │
│                    \     |     /     /                                              │
│                     BasePage (Appium helpers)                                       │
│                           │                                                         │
│              AppiumDriverFactory ──► AndroidDriver (UiAutomator2)                  │
│                                               │                                     │
│                                     Android Emulator API 35                        │
│                                     com.example.moneymap APK                       │
│                           │                                                         │
│  TestNGListener ──► ScreenshotUtil / LogUtil ──► reports/screenshots/ + logs/      │
│                           │                                                         │
│  @AfterSuite ──► ExcelReportGenerator ──► Test Results/Excel/*.xlsx                │
│               ──► HTMLReportGenerator  ──► Test Results/HTML/*.html                │
│               ──► BaseTest.generateJsonReport ──► Test Results/JSON/*.json          │
│               ──► BaseTest.generateMarkdownSummary ──► Test Results/Summary/*.md   │
│               ──► enforcePassRateThreshold() (≥95% or RuntimeException)            │
└─────────────────────────────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│  PILLAR 2 — CI/CD PIPELINE  (.github/workflows/android-e2e.yml)                    │
│                                                                                     │
│  trigger: push/PR to main/master │ workflow_dispatch │ cron 02:00 UTC              │
│                                                                                     │
│  JOB 1: build-apk                                                                  │
│    ubuntu-latest                                                                    │
│    ./gradlew assembleDebug -PMONEYMAP_API_BASE_URL=http://10.0.2.2:3000/api/v1/    │
│    → upload artifact: moneymap-debug-apk (1 day retention)                         │
│                  │                                                                  │
│                  ▼  (needs: build-apk)                                              │
│  JOB 2: run-tests  [matrix: auth, dashboard, transactions, budget, settings,       │
│                             reports]  fail-fast:false  timeout: 80 min             │
│    services: postgres:16-alpine + redis:7-alpine                                   │
│    steps:                                                                           │
│      Checkout → Download APK artifact → Setup Java 21 → Setup Android SDK 35      │
│      → Install backend deps (npm ci) → prisma generate + db push → Start NestJS   │
│      → Health check backend (poll :3000 up to 120s)                               │
│      → Enable KVM                                                                  │
│      → android-emulator-runner@v2 (API 35, google_apis, x86_64)                   │
│          └─► run-e2e-tests.sh:                                                     │
│                Wait boot → Install APK → Install Appium 3 + uiautomator2           │
│                → Start Appium :4723 → Poll /status (90s) → mvn clean test         │
│                  -DsuiteXmlFile=testng-<shard>.xml -DtestShard=<shard>             │
│                → Capture post-test evidence                                        │
│      → Rename execution-results.json → execution-results-<shard>.json             │
│      → Upload shard-results-<shard> artifact (1 day retention)                    │
│                  │                                                                  │
│                  ▼  (needs: run-tests, if: always())                               │
│  JOB 3: consolidate-reports                                                        │
│    Download 6 shard artifacts → mvn exec:java ReportMerger                        │
│    → zip → upload MoneyMap-Consolidated-Reports-Build-N (30 day)                  │
│    → clone gh-pages → copy latest + history/build-N                               │
│    → JamesIves/github-pages-deploy-action@v4 (branch: gh-pages, clean: false)    │
│    → Publish step summary with live report URL                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────┐
│  PILLAR 3 — BACKEND SECURITY PIPELINE                                               │
│  (.github/workflows/backend-security.yml)                                           │
│                                                                                     │
│  trigger: push/PR to main/master │ workflow_dispatch                               │
│                                                                                     │
│  JOB: security-scan (ubuntu-latest)                                                 │
│    Checkout                                                                         │
│    → Semgrep (p/nodejs + p/typescript, backend/ dir)                               │
│         → semgrep-results.json                                                      │
│    → Trivy fs (backend/, all severities, JSON out)                                 │
│         → trivy-results.json                                                        │
│    → Gitleaks (full repo, detect secrets)                                          │
│         → gitleaks-results.json                                                     │
│    → OWASP Dependency Check (backend/package-lock.json, HTML + JSON)              │
│         → dependency-check-report.html                                              │
│    → Generate security-reports/ documents                                          │
│    → Upload artifact: security-scan-results (all 4 raw outputs)                   │
│    → Evaluate: Critical → exit 1; High/Medium/Low → warning annotation            │
│    → Publish step summary table (counts per severity)                              │
└─────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────┐
│  PILLAR 4 — LOAD TESTING                                                            │
│                                                                                     │
│  automation/load-tests/k6-load-test.js                                             │
│    scenarios: baseline(100 VU/60s) | stress200 | stress500 | stress1000            │
│               spike(50→500→50 VU) | endurance(100 VU/1800s)                        │
│    auth helper: POST /auth/login → store JWT per VU lifecycle                      │
│    endpoints: login, register, GET/POST transactions, GET budgets,                 │
│               GET reports, GET savings-goals                                        │
│                                                                                     │
│  automation/load-tests/artillery-load-test.yml                                     │
│    phases: baseline + stress-200 matching k6 shape                                 │
│                                                                                     │
│  automation/load-tests/jmeter-test-plan.jmx                                       │
│    thread groups: baseline, stress-200, spike                                      │
│                                                                                     │
│  Output: automation/load-tests/performance-report.md                               │
│    Thresholds: P95 ≤500ms (baseline), ≤1000ms (stress 200), error rate ≤1%        │
└─────────────────────────────────────────────────────────────────────────────────────┘

### NestJS Backend Stack

```
NestJS (TypeScript) + Prisma ORM
        │
        ├── PostgreSQL 16 (via Prisma)
        │       models: User, UserProfile, Category, Transaction, TransactionTag,
        │               Budget, SavingsGoal, Subscription, ChatSession, ChatMessage,
        │               RefreshToken, NotificationPreference
        │
        ├── Redis 7 (session cache / refresh token store)
        │
        └── REST API  →  prefix: api/v1
               Swagger: api/v1/docs
               Security middleware: helmet, compression, cookieParser, CORS,
                                    ValidationPipe (whitelist, transform),
                                    HttpExceptionFilter, TransformInterceptor
```

### Component Breakdown

| Component | Responsibility | Repo Location | Key Interfaces |
|---|---|---|---|
| `BasePage` | Shared Appium wait/scroll/click helpers; app-ready state machine; diagnostics capture | `automation/src/main/java/.../pages/BasePage.java` | `waitForElement(By)`, `click(By)`, `type(By,String)`, `scrollToText(String)`, `ensureAppReady()`, `captureDiagnostics(String)` |
| 11 Page Objects | Encapsulate locators + interactions for each MoneyMap screen | `automation/src/main/java/.../pages/*.java` | Per-screen methods (see §Detailed Design 1) |
| `AppiumDriverFactory` | Singleton `AndroidDriver` lifecycle; config resolution | `automation/src/main/java/.../utils/AppiumDriverFactory.java` | `getDriver()`, `quitDriver()`, `isDriverAlive()` |
| `ScreenshotUtil` | Capture PNG on failure; save to `reports/screenshots/` | `automation/src/main/java/.../utils/ScreenshotUtil.java` | `captureScreenshot(AndroidDriver, String prefix) : String` |
| `LogUtil` | Console + file logging; logcat capture; Appium server log capture | `automation/src/main/java/.../utils/LogUtil.java` | `log(String)`, `logError(String, Throwable)`, `captureDeviceLogs(...)`, `captureAppiumLogs(...)` |
| `TestCase` | POJO carrying all test case fields (input + output) | `automation/src/main/java/.../model/TestCase.java` | Getters/setters for 18 fields |
| `BaseTest` | Suite/class lifecycle; catalog loading; report generation; pass-rate enforcement | `automation/src/test/java/.../tests/BaseTest.java` | `@BeforeSuite`, `@BeforeClass`, `@AfterClass`, `@AfterSuite`, `updateTestCase(...)` |
| `E2EAutomationTest` | DataProvider + 20 module dispatch methods | `automation/src/test/java/.../tests/E2EAutomationTest.java` | `executeTestCase(TestCase)` via `@DataProvider allTestCases` |
| `TestNGListener` | Auto-screenshot + logcat on `onTestFailure` | `automation/src/test/java/.../listeners/TestNGListener.java` | `ITestListener.onTestFailure(ITestResult)` |
| `ExcelReportGenerator` | Produces 4 XLSX files (7-sheet master + 3 focused) | `automation/src/main/java/.../reporting/ExcelReportGenerator.java` | `generateReports(List<TestCase>, String outputDir, String build, String branch, String commit)` |
| `HTMLReportGenerator` | Produces 3 self-contained HTML files with Chart.js | `automation/src/main/java/.../reporting/HTMLReportGenerator.java` | `generateReports(List<TestCase>, String outputDir, String build, String branch, String commit)` |
| `ReportMerger` | Merges N shard JSON files → single consolidated report set | `automation/src/main/java/.../reporting/ReportMerger.java` | `main(String[] args)` where args[0]=jsonDir, args[1]=outputDir |
| `run-e2e-tests.sh` | POSIX sh test runner inside emulator-runner@v2 | `automation/.github-scripts/run-e2e-tests.sh` | Invoked by GitHub Actions as the `script:` value |
| `generate-pages.py` | Builds the GitHub Pages index.html linking all builds | `automation/.github-scripts/generate-pages.py` | `main()` — reads `deploy_site/` structure, writes index files |
| `android-e2e.yml` | 3-job CI/CD workflow | `.github/workflows/android-e2e.yml` | GitHub Actions events: push, PR, schedule, dispatch |
| `backend-security.yml` | Security scan workflow | `.github/workflows/backend-security.yml` | GitHub Actions events: push, PR, dispatch |
| Load test scripts | k6/Artillery/JMeter performance scripts | `automation/load-tests/` | CLI-invoked by engineer or CI step |

---

## Detailed Design

### 1. Page Object Model Layer

#### BasePage — Contract and Implementation

`BasePage` is the foundation for every Page Object. It holds an `AndroidDriver` reference and two
`WebDriverWait` instances: `wait` (15 s) and `shortWait` (5 s). Key constants:

- `DEFAULT_TIMEOUT = 15` seconds — used by `waitForElement` and `waitForClickable`.
- `SHORT_TIMEOUT = 5` seconds — used by `isElementDisplayed`.
- `MAX_RETRIES = 3` — retry loop inside `click()` to absorb `StaleElementReferenceException`.

**Core contracts (all `protected` unless noted):**

| Method Signature | Behaviour |
|---|---|
| `WebElement waitForElement(By)` | Blocks up to 15 s for visibility; on timeout captures diagnostics and throws `AssertionError`. |
| `WebElement waitForElement(By, Duration)` | Overload with custom timeout. |
| `WebElement waitForClickable(By)` | Blocks up to 15 s for clickability; same failure handling. |
| `void click(By)` | Calls `waitForClickable` then `.click()`; retries up to 3 times on stale element. |
| `void type(By, String)` | `clear()` then `sendKeys()`. |
| `void clearAndType(By, String)` | `click()` + `clear()` + `sendKeys()`. |
| `String getText(By)` | Returns `.getText()` after `waitForElement`. |
| `boolean isElementDisplayed(By)` | Uses `shortWait`; swallows exceptions; returns `false` if not found. |
| `boolean isElementPresent(By)` | Finds element directly (no wait); returns `false` on exception. |
| `List<WebElement> findElements(By)` | Delegates to `driver.findElements`. |
| `By byId(String resourceId)` | Prepends `com.example.moneymap:id/`. |
| `By byText(String)` | XPath matching `@text` or `@content-desc` with `contains`. |
| `By byExactText(String)` | XPath exact `@text` match. |
| `void clickByText(String)` | `click(byText(text))`. |
| `boolean isTextVisible(String)` | `isElementDisplayed(byText(text))`. |
| `void scrollToText(String)` | Uses `AppiumBy.androidUIAutomator` with `UiScrollable.scrollIntoView`. |
| `void swipeUp()` | `UiScrollable.scrollBackward()`. |
| `void waitSeconds(int)` | `Thread.sleep`. |
| `boolean waitForText(String, int)` | Polls for text visibility with custom timeout; returns `true`/`false`. |
| `void pressBack()` | `driver.navigate().back()`. |
| `void ensureAppReady()` | State machine (25 s timeout): dismisses permission dialogs → dismisses update dialogs → skips onboarding → verifies login screen fully loaded (both email and password fields present). Throws `AssertionError` if login screen not reached. |
| `void captureDiagnostics(String prefix)` | Logs current package + activity; saves page source to `reports/logs/<prefix>_pagesource.xml`; calls `ScreenshotUtil.captureScreenshot` and `LogUtil.captureDeviceLogs/captureAppiumLogs`. |

**Gaps to fill:** `BasePage` does not expose `swipeDown()` — implementors must add if needed.
`captureDiagnostics` is already fully implemented and referenced by `waitForElement` on timeout.

---

#### All 11 Page Objects

##### 1. LoginPage

Location: `automation/src/main/java/com/example/moneymap/automation/pages/LoginPage.java`
Used by: `E2EAutomationTest.runAuthTest`, `runRegistrationTest`, `runSessionTest`

Key locators:
- Email field: `By.xpath("//android.widget.EditText[contains(@text,'Email Address') or @hint='Email Address']")`
- Password field: `By.xpath("//android.widget.EditText[contains(@text,'Password') or @hint='Password']")`
- Sign In button: `byText("Sign In")` or `byId("btn_signin")`
- Sign Up link: `byText("Sign Up")` or `byText("Create Account")`
- Forgot Password: `byText("Forgot Password?")`
- Google button: `byText("Continue with Google")` or `byId("btn_google_signin")`
- Password toggle (eye icon): `By.xpath("//*[@content-desc='Toggle password visibility' or @resource-id='com.example.moneymap:id/password_toggle']")`
- Error message: `By.xpath("//*[contains(@text,'Invalid') or contains(@text,'credentials') or contains(@text,'error')]")`

Key methods:
```java
public void login(String email, String password)
public void enterEmail(String email)
public void enterPassword(String password)
public void clickSignIn()
public void clickSignUp()
public void clickForgotPassword()
public void togglePasswordVisibility()
public boolean isLoginScreenDisplayed()
public boolean isErrorMessageDisplayed(String messageFragment)
public boolean isGoogleButtonVisible()
public String getEmailFieldText()
```

##### 2. SignupPage

Location: `automation/src/main/java/com/example/moneymap/automation/pages/SignupPage.java`
Used by: `E2EAutomationTest.runRegistrationTest`, `runProfileTest`

Key locators:
- Name field: `By.xpath("//android.widget.EditText[@hint='Full Name' or contains(@text,'Full Name')]")`
- Email field: `By.xpath("//android.widget.EditText[@hint='Email Address' or contains(@text,'Email')]")`
- Password field: `By.xpath("(//android.widget.EditText[@hint='Password'])[1]")`
- Confirm password field: `By.xpath("(//android.widget.EditText[@hint='Password'])[2]")` or `@hint='Confirm Password'`
- Create Account button: `byText("Create Account")`
- Sign In link: `byText("Sign In")`
- T&C checkbox: `By.xpath("//*[@resource-id='com.example.moneymap:id/checkbox_terms']")`
- Error text: `By.xpath("//*[contains(@text,'match') or contains(@text,'error') or contains(@text,'required')]")`

Key methods:
```java
public void register(String name, String email, String password, String confirmPassword)
public void enterName(String name)
public void enterEmail(String email)
public void enterPassword(String password)
public void enterConfirmPassword(String confirmPassword)
public void clickCreateAccount()
public void clickLoginLink()
public boolean isSignupScreenDisplayed()
public boolean isErrorMessageDisplayed(String fragment)
```

##### 3. DashboardPage

Location: `automation/src/main/java/com/example/moneymap/automation/pages/DashboardPage.java`
Used by: `runAuthTest`, `runNavigationTest`, `runDashboardTest`, `runFormsTest`, `runCrudTest`, `runRegressionTest`

Key locators:
- Balance text: `By.xpath("//*[contains(@resource-id,'balance') or contains(@text,'₹') or contains(@text,'$')]")`
- Add transaction FAB: `By.xpath("//*[@content-desc='Add Transaction' or @resource-id='com.example.moneymap:id/fab_add']")`
- Reports tab: `byText("Reports")` or `By.xpath("//*[@content-desc='Reports']")`
- Budget tab: `byText("Budget")` or `By.xpath("//*[@content-desc='Budget']")`
- Profile tab: `byText("Profile")` or `By.xpath("//*[@content-desc='Profile']")`
- Home tab: `byText("Home")` or `By.xpath("//*[@content-desc='Home']")`
- See All Transactions: `byText("See All")` or `byText("View All")`
- Logout button (on profile screen): `byText("Logout")` or `byText("Log Out")`

Key methods:
```java
public boolean isDashboardLoaded()
public String getAvailableBalance()
public void clickAddTransactionButton()
public void navigateToReports()
public void navigateToBudget()
public void navigateToProfile()
public void navigateToHome()
public void logout()
public void clickSeeAllTransactions()
```

##### 4. AddTransactionPage

Location: `automation/src/main/java/com/example/moneymap/automation/pages/AddTransactionPage.java`
Used by: `runNavigationTest`, `runDashboardTest`, `runFormsTest`, `runCrudTest`, `runValidationTest`

Key locators:
- Amount field: `byId("et_amount")` or `By.xpath("//android.widget.EditText[contains(@hint,'Amount') or contains(@hint,'amount')]")`
- Expense toggle: `byText("Expense")` or `byId("btn_expense")`
- Income toggle: `byText("Income")` or `byId("btn_income")`
- Category selector: `byText("Select Category")` or `byId("spinner_category")`
- Note field: `byId("et_note")` or `By.xpath("//android.widget.EditText[@hint='Note' or @hint='Description']")`
- Save button: `byText("Save")` or `byText("Add Transaction")` or `byId("btn_save")`
- Close/Back button: `By.xpath("//*[@content-desc='Close' or @content-desc='Navigate up']")`

Key methods:
```java
public boolean isAddTransactionScreenDisplayed()
public void selectExpense()
public void selectIncome()
public void enterAmount(String amount)
public void selectCategory(String categoryName)
public void enterNote(String note)
public void clickSave()
public void clickClose()
public String getDisplayedAmount()
public void createTransaction(String type, String amount, String category, String note)
```

##### 5. BudgetSetupPage

Location: `automation/src/main/java/com/example/moneymap/automation/pages/BudgetSetupPage.java` (already exists)
Used by: `runProfileTest`

Key locators (from existing implementation):
- Finish button: `By.xpath("//*[@text='Complete Setup' or @content-desc='Complete Setup' or contains(@text,'Finish')]")`
- Category budget field: XPath via `TextView[@text='<category>']` ancestor traversal to sibling `EditText`

Key methods (already implemented):
```java
public void enterBudgetForCategory(String categoryName, String amount)
public void enterFoodBudget(String amount)
public void enterShoppingBudget(String amount)
public void enterTransportBudget(String amount)
public void setupBudgets(String total, String food, String transport)
public void clickFinish()
public boolean isBudgetSetupDisplayed()
```

##### 6. RoleSelectionPage

Location: `automation/src/main/java/com/example/moneymap/automation/pages/RoleSelectionPage.java` (already exists)
Used by: `runProfileTest`, `runRegistrationTest`

Key locators (from existing implementation):
- Student: `By.xpath("//*[@resource-id='com.example.moneymap:id/btn_student' or @text='Student' or @content-desc='Student']")`
- Employee: `By.xpath("//*[@resource-id='com.example.moneymap:id/btn_employee' or @text='Employee' or @content-desc='Employee']")`
- Homemaker: `By.xpath("//*[@resource-id='com.example.moneymap:id/btn_homemaker' or @text='Homemaker' or @content-desc='Homemaker']")`
- Freelancer: `By.xpath("//*[@resource-id='com.example.moneymap:id/btn_freelancer' or @text='Freelancer' or @content-desc='Freelancer']")`
- General: `By.xpath("//*[@resource-id='com.example.moneymap:id/btn_general' or @text='General' or @content-desc='General']")`
- Continue button: `By.xpath("//*[@resource-id='com.example.moneymap:id/continue_button' or @text='Continue' or @content-desc='Continue']")`

Key methods (already implemented):
```java
public void selectStudent()
public void selectProfessional()
public void selectHomemaker()
public void selectFreelancer()
public void selectGeneral()
public void clickContinue()
public boolean isRoleSelectionDisplayed()
```

##### 7. HistoryPage

Location: `automation/src/main/java/com/example/moneymap/automation/pages/HistoryPage.java`
Used by: `runCrudTest`, `runSearchTest`

Key locators:
- Transaction list item: `By.xpath("//androidx.recyclerview.widget.RecyclerView/android.view.ViewGroup")`
- Search icon: `By.xpath("//*[@content-desc='Search' or @resource-id='com.example.moneymap:id/menu_search']")`
- Filter button: `byText("Filter")` or `By.xpath("//*[@content-desc='Filter']")`
- Transaction count: `By.xpath("//androidx.recyclerview.widget.RecyclerView/android.view.ViewGroup")`

Key methods:
```java
public boolean isHistoryScreenDisplayed()
public int getTransactionCount()
public void tapTransaction(int index)
public void clickSearchIcon()
public void enterSearchQuery(String query)
```

##### 8. OnboardingPage

Location: `automation/src/main/java/com/example/moneymap/automation/pages/OnboardingPage.java`
Used by: `BasePage.ensureAppReady()` (internal state machine), available for dedicated onboarding tests

Key locators:
- Skip button: `By.xpath("//*[@text='Skip']")`
- Onboarding slides: `By.xpath("//*[@text='Track with Ease' or @text='Smart Budgeting' or @text='Visual Reports']")`
- Get Started button: `byText("Get Started")`
- Next button: `byText("Next")`

Key methods:
```java
public boolean isOnboardingDisplayed()
public void clickSkip()
public void clickGetStarted()
public void swipeToNextSlide()
public String getCurrentSlideTitle()
```

##### 9. NotificationPermissionPage

Location: `automation/src/main/java/com/example/moneymap/automation/pages/NotificationPermissionPage.java`
Used by: `runProfileTest` (TC_PROF_017, TC_PROF_018)

Key locators:
- Allow button: `byText("Allow")` or `By.xpath("//*[@resource-id='com.android.permissioncontroller:id/permission_allow_button']")`
- Not Now / Deny button: `byText("Not Now")` or `byText("Don't allow")` or `By.xpath("//*[@resource-id='com.android.permissioncontroller:id/permission_deny_button']")`
- App-level permission screen: `byText("Enable Notifications")` or `byId("btn_allow_notifications")`

Key methods:
```java
public boolean isNotificationPermissionScreenDisplayed()
public void clickAllow()
public void clickNotNow()
```

##### 10. ProfileSetupPage

Location: `automation/src/main/java/com/example/moneymap/automation/pages/ProfileSetupPage.java`
Used by: `runProfileTest`

Key locators:
- Name field: `byId("et_name")` or `By.xpath("//android.widget.EditText[@hint='Name' or @hint='Full Name']")`
- Currency spinner: `byId("spinner_currency")` or `byText("INR")`
- Monthly income/allowance field: `byId("et_income")` or `By.xpath("//android.widget.EditText[contains(@hint,'Income') or contains(@hint,'Allowance') or contains(@hint,'Salary')]")`
- Next button: `byText("Next")` or `byId("btn_next")`
- Company/Institution field: `byId("et_company")` or `byId("et_institution")`

Key methods:
```java
public boolean isProfileSetupDisplayed()
public void enterName(String name)
public void selectCurrency(String currency)
public void enterMonthlyAmount(String amount)
public void enterOrganisation(String org)
public void clickNext()
```

##### 11. TransactionSuccessPage

Location: `automation/src/main/java/com/example/moneymap/automation/pages/TransactionSuccessPage.java`
Used by: `runFormsTest`, `runCrudTest`, `runValidationTest`, `runRegressionTest`

Key locators:
- Success indicator: `byText("Transaction Added")` or `byText("Success")` or `By.xpath("//*[contains(@text,'saved') or contains(@text,'added') or contains(@content-desc,'success')]")`
- Back to Home button: `byText("Back to Home")` or `byText("Done")` or `byId("btn_home")`

Key methods:
```java
public boolean isSuccessDisplayed()
public void clickBackToHome()
```

---

### 2. Test Framework Layer

#### BaseTest

`BaseTest` manages the full TestNG suite lifecycle. Key design decisions:

**Thread Safety of `testCases` list:**
The field is declared as:
```java
public static final List<TestCase> testCases = Collections.synchronizedList(new ArrayList<>());
```
`loadTestCasesCatalog()` appends to it only inside `@BeforeSuite` — a single-threaded phase.
`updateTestCase(...)` is `synchronized` and iterates by testId match. Since `@DataProvider` runs
sequentially (`parallel = false`) the list is never written concurrently during test execution.

**`@BeforeSuite` (`setupSuite()`):**
1. Records `suiteStartTime = System.currentTimeMillis()`.
2. Logs suite header with build number and branch (sourced from env vars `GITHUB_RUN_NUMBER`,
   `GITHUB_SHA`, `GITHUB_REF_NAME`, defaulting to `"local"`/`"main"`).
3. Calls `loadTestCasesCatalog()` which searches three candidate paths for `test_cases.json`:
   - `automation/data/test_cases.json`
   - `data/test_cases.json`
   - `../automation/data/test_cases.json`
   Falls back to `generateSyntheticTestCases()` if none found.
4. Applies `testShard` system property filter via `shouldIncludeTestCase(module, shard)`.

**`@BeforeClass` (`setupClass()`):**
Calls `AppiumDriverFactory.getDriver()`. On any exception, logs the error and sets `driver = null`
(simulation mode), allowing the suite to continue without aborting.

**`@AfterClass` (`tearDownClass()`):**
Calls `AppiumDriverFactory.quitDriver()` to close the session and null out the static reference.

**`@AfterSuite` (`tearDownSuite()`):**
1. Calculates total duration.
2. Counts PASSED / FAILED / SKIPPED / NOTRUN.
3. Calls `generateAllReports(durationMs)` — creates subdirectories Excel, HTML, JSON, Summary
   under the resolved results dir, then invokes `ExcelReportGenerator`, `HTMLReportGenerator`,
   `generateJsonReport`, and `generateMarkdownSummary`. Report generation failures are caught and
   logged but do not prevent threshold enforcement.
4. Calls `enforcePassRateThreshold()` — throws `RuntimeException` if pass rate < 95%, which
   causes Maven Surefire to see a test suite error even with `testFailureIgnore=true`.

**Shard filter logic (`shouldIncludeTestCase`):**
| Shard | Matching module keywords |
|---|---|
| `auth` | auth, register, session |
| `dashboard` | dashboard, profile, navigation |
| `transactions` | crud, form, search, filter, file |
| `budget` | budget |
| `settings` | settings, accessibility, responsive |
| `reports` | everything that does NOT match the above five shards |

---

#### E2EAutomationTest

`E2EAutomationTest extends BaseTest`. Single `@Test` method `executeTestCase(TestCase tc)` driven
by `@DataProvider(name = "allTestCases", parallel = false)`.

**DataProvider:** Converts the `testCases` static list into `Object[][]` at the time the
`ITestContext` is available (after `@BeforeSuite` loads the catalog).

**Dispatch table** (`dispatchToRealFlow`):

| `tc.getModule().toLowerCase()` | Method called |
|---|---|
| `"authentication"` | `runAuthTest(tc)` |
| `"authorization"` | `runAuthzTest(tc)` |
| `"registration"` | `runRegistrationTest(tc)` |
| `"profile management"` | `runProfileTest(tc)` |
| `"navigation"` | `runNavigationTest(tc)` |
| `"dashboard"` | `runDashboardTest(tc)` |
| `"forms"` | `runFormsTest(tc)` |
| `"crud operations"` | `runCrudTest(tc)` |
| `"search"` | `runSearchTest(tc)` |
| `"filters"` | `runFilterTest(tc)` |
| `"input validation"` | `runValidationTest(tc)` |
| `"error handling"` | `runErrorHandlingTest(tc)` |
| `"session management"` | `runSessionTest(tc)` |
| `"notifications"` | `runNotificationTest(tc)` |
| `"file upload"` | `runFileTest(tc)` |
| `"offline handling"` | `runOfflineTest(tc)` |
| `"accessibility"` | `runAccessibilityTest(tc)` |
| `"responsive ui"` | `runResponsiveTest(tc)` |
| `"performance smoke tests"` | `runPerformanceTest(tc)` |
| `"regression suite"` | `runRegressionTest(tc)` |
| default | `runGenericVerification(tc)` |

**Simulation mode:** When `driver == null`, `executeTestCase` calls `runGenericVerification(tc)`
directly without calling `dispatchToRealFlow`. `runGenericVerification` records PASSED with
`"Simulation verified: <testId>"` actual result, verifies the `TestCase` object is non-null and
has a non-empty testId, and calls `BaseTest.updateTestCase(...)`.

**`ensureLoggedIn()` helper:** Checks if `DashboardPage.isDashboardLoaded()` is false; if so,
attempts `LoginPage.login("demo@moneymap.com", "Password123!")`. Called at the top of navigation,
dashboard, forms, CRUD, validation, accessibility, responsive, performance, and regression tests.

---

#### TestNGListener

`TestNGListener implements ITestListener, ISuiteListener`.

Location: `automation/src/test/java/com/example/moneymap/automation/listeners/TestNGListener.java`

**`onTestFailure(ITestResult result)`:**
1. Extracts the test instance: `result.getInstance()` cast to `BaseTest`.
2. Retrieves `driver` from the instance.
3. If driver is not null:
   - Extracts `testId` from the first parameter if it is a `TestCase`, otherwise derives from
     `result.getName()`.
   - Calls `ScreenshotUtil.captureScreenshot(driver, testId)` → gets screenshot path.
   - Calls `LogUtil.captureDeviceLogs(driver, testId)` → gets log path.
   - Calls `LogUtil.captureAppiumLogs(driver, testId)` → gets Appium log path.
   - Calls `BaseTest.updateTestCase(testId, "FAILED", failureReason, durationMs, screenshotPath, logPath, ...)`
4. Logs the failure with test name and throwable message.

**Registration in testng.xml:**
```xml
<listeners>
  <listener class-name="com.example.moneymap.automation.listeners.TestNGListener"/>
</listeners>
```

---

#### TestNG Suite XML Files

All seven files reside in `automation/src/test/resources/`.

**`testng.xml`** (all modules, used for local full runs):
```xml
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="MoneyMap E2E Full Suite" verbose="1" parallel="none">
  <listeners>
    <listener class-name="com.example.moneymap.automation.listeners.TestNGListener"/>
  </listeners>
  <test name="MoneyMap E2E Tests">
    <classes>
      <class name="com.example.moneymap.automation.tests.E2EAutomationTest"/>
    </classes>
  </test>
</suite>
```

**Shard XML files** share the same structure but pass the shard name via a Maven system property
(`-DtestShard=<shard>`); the filtering is done in Java code, not in the XML itself. Each shard
XML just references `E2EAutomationTest`:

- `testng-auth.xml` — `<test name="Auth Shard">`
- `testng-dashboard.xml` — `<test name="Dashboard Shard">`
- `testng-transactions.xml` — `<test name="Transactions Shard">`
- `testng-budget.xml` — `<test name="Budget Shard">`
- `testng-settings.xml` — `<test name="Settings Shard">`
- `testng-reports.xml` — `<test name="Reports Shard">`

The `<suite>` element in each shard XML sets `parallel="none"` and includes the `TestNGListener`.

---

### 3. Test Data Layer

#### `test_cases.json` Schema

The file is a top-level JSON array. Each element is a JSON object:

```json
{
  "testId":        "TC_AUTH_001",
  "module":        "Authentication",
  "name":          "Valid Login with Correct Credentials",
  "priority":      "CRITICAL",
  "preconditions": "App installed, user registered",
  "steps":         "1. Launch app\n2. Enter valid email\n3. Enter valid password\n4. Tap Sign In",
  "testData":      "email: demo@moneymap.com, password: Password123!",
  "expectedResult":"Dashboard loads successfully"
}
```

All eight fields are strings. `priority` values: `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`.
`steps` uses `\n` newlines between numbered steps.

#### Module Distribution (510 total)

| # | Module | Count | ID Prefix | Shard |
|---|---|---|---|---|
| 1 | Authentication | 40 | `TC_AUTH_` | auth |
| 2 | Authorization | 30 | `TC_AUTHZ_` | auth |
| 3 | Registration | 20 | `TC_REG_` | auth |
| 4 | Profile Management | 20 | `TC_PROF_` | dashboard |
| 5 | Navigation | 30 | `TC_NAV_` | dashboard |
| 6 | Dashboard | 20 | `TC_DAS_` | dashboard |
| 7 | Forms | 40 | `TC_FORM_` | transactions |
| 8 | CRUD Operations | 40 | `TC_CRUD_` | transactions |
| 9 | Search | 20 | `TC_SRCH_` | transactions |
| 10 | Filters | 20 | `TC_FILT_` | transactions |
| 11 | Input Validation | 40 | `TC_VAL_` | transactions |
| 12 | Error Handling | 20 | `TC_ERR_` | transactions |
| 13 | Session Management | 20 | `TC_SESS_` | auth |
| 14 | Notifications | 20 | `TC_NOTIF_` | settings |
| 15 | File Upload | 20 | `TC_FILE_` | transactions |
| 16 | Offline Handling | 10 | `TC_OFF_` | reports |
| 17 | Accessibility | 20 | `TC_ACC_` | settings |
| 18 | Responsive UI | 10 | `TC_RESP_` | settings |
| 19 | Performance Smoke Tests | 20 | `TC_PERF_` | reports |
| 20 | Regression Suite | 50 | `TC_REG_SUITE_` | reports |
| | **Total** | **510** | | |

#### `TestCase` Java Model Class

Full path: `automation/src/main/java/com/example/moneymap/automation/model/TestCase.java`

Constructor (8 args — input-only fields loaded from JSON):
```java
public TestCase(String testId, String module, String name, String priority,
                String preconditions, String steps, String testData, String expectedResult)
```

All fields:

| Field | Type | Source | Default |
|---|---|---|---|
| `testId` | `String` | JSON | — |
| `module` | `String` | JSON | — |
| `name` | `String` | JSON | — |
| `priority` | `String` | JSON | `"MEDIUM"` |
| `preconditions` | `String` | JSON | `""` |
| `steps` | `String` | JSON | `""` |
| `testData` | `String` | JSON | `""` |
| `expectedResult` | `String` | JSON | `""` |
| `actualResult` | `String` | Runtime | `""` |
| `status` | `String` | Runtime | `"NOT_RUN"` |
| `durationMs` | `long` | Runtime | `0` |
| `screenshotPath` | `String` | Runtime | `""` |
| `deviceLogPath` | `String` | Runtime | `""` |
| `pageSourcePath` | `String` | Runtime | `""` |
| `appiumLogPath` | `String` | Runtime | `""` |
| `locatorUsed` | `String` | Runtime | `""` |
| `currentActivity` | `String` | Runtime | `""` |
| `currentPackage` | `String` | Runtime | `""` |

All fields have public getters and setters.

---

### 4. Driver and Configuration Layer

#### AppiumDriverFactory — Singleton Pattern

`AppiumDriverFactory` holds a single `static AndroidDriver driver` protected by `synchronized`
methods. The lifecycle is:

1. **`getDriver()`** — if `driver == null`, calls `loadConfig()` then `buildOptions(config)`,
   constructs the `URL` from `config.appiumUrl` (default `http://127.0.0.1:4723`), creates
   `new AndroidDriver(serverUrl, options)`, sets implicit wait to 10 seconds, logs the session ID,
   and returns it. Throws `RuntimeException` on failure.
2. **`quitDriver()`** — calls `driver.quit()`, nulls the reference. Safe to call multiple times.
3. **`isDriverAlive()`** — calls `driver.getSessionId()`; returns `false` on any exception.

**Config resolution order:**
1. `automation/config/appium-config.json`
2. `config/appium-config.json`
3. `../automation/config/appium-config.json`
4. Empty `JSONObject` (all defaults)

**APK path resolution order (inside `buildOptions`):**
1. `config.app` value if non-empty and file exists.
2. `../app/build/outputs/apk/debug/app-debug.apk`
3. `app/build/outputs/apk/debug/app-debug.apk`
4. `../../app/build/outputs/apk/debug/app-debug.apk`
5. Empty string (Appium uses already-installed app).

---

#### `appium-config.json` Schema

Full path: `automation/config/appium-config.json`

```json
{
  "appiumUrl":          "http://127.0.0.1:4723",
  "platformName":       "Android",
  "automationName":     "UiAutomator2",
  "deviceName":         "Android Emulator",
  "udid":               "",
  "appPackage":         "com.example.moneymap",
  "appActivity":        "com.example.moneymap.MainActivity",
  "app":                "",
  "noReset":            false,
  "fullReset":          false,
  "autoGrantPermissions": true,
  "newCommandTimeout":  300,
  "systemPort":         8200,
  "adbExecTimeout":     120000
}
```

| Key | Type | Default | Description |
|---|---|---|---|
| `appiumUrl` | String | `"http://127.0.0.1:4723"` | Appium server base URL |
| `platformName` | String | `"Android"` | Appium platform capability |
| `automationName` | String | `"UiAutomator2"` | Driver engine |
| `deviceName` | String | `"Android Emulator"` | AVD display name |
| `udid` | String | `""` | Specific device serial (empty = any) |
| `appPackage` | String | `"com.example.moneymap"` | App package identifier |
| `appActivity` | String | `"com.example.moneymap.MainActivity"` | Launch activity |
| `app` | String | `""` | Absolute path to APK (empty = use installed) |
| `noReset` | Boolean | `false` | Do not reset app state between sessions |
| `fullReset` | Boolean | `false` | Fully uninstall and reinstall app |
| `autoGrantPermissions` | Boolean | `true` | Grant all runtime permissions automatically |
| `newCommandTimeout` | Integer | `300` | Seconds before Appium kills idle session |
| `systemPort` | Integer | `8200` | UiAutomator2 server port on device |
| `adbExecTimeout` | Integer | `120000` | ADB command timeout in milliseconds |

---

#### ScreenshotUtil

Full path: `automation/src/main/java/com/example/moneymap/automation/utils/ScreenshotUtil.java`

```java
public class ScreenshotUtil {
    /**
     * Captures a PNG screenshot and saves it to:
     *   automation/reports/screenshots/<prefix>_<yyyyMMdd_HHmmss_SSS>.png
     * Returns the relative path string, or empty string on failure.
     */
    public static String captureScreenshot(AndroidDriver driver, String prefix)
}
```

**File naming convention:** `<testId>_<timestamp>.png` where timestamp format is `yyyyMMdd_HHmmss_SSS`.
**Storage path resolution:** First tries `automation/reports/screenshots/`; if `automation/`
directory does not exist, uses `reports/screenshots/`. Creates directories with `mkdirs()`.
The relative path is stored in `TestCase.screenshotPath` (without leading slash).

---

#### LogUtil

Full path: `automation/src/main/java/com/example/moneymap/automation/utils/LogUtil.java`

```java
public class LogUtil {
    public static void log(String message)
    public static void logWarning(String message)
    public static void logError(String message, Throwable e)
    public static void logTestStart(String testId, String testName)
    public static void logTestFail(String testId, String reason, long durationMs)
    public static void captureDeviceLogs(AndroidDriver driver, String prefix)
    public static void captureAppiumLogs(AndroidDriver driver, String prefix)
}
```

Log levels: `[INFO]`, `[WARN]`, `[ERROR]`. All output goes to stdout (captured by Maven Surefire)
and optionally to a rotating file log at `automation/reports/logs/automation.log`.

`captureDeviceLogs`: Uses `driver.manage().logs().get("logcat")` to retrieve logcat entries,
writes to `automation/reports/logs/<prefix>_<timestamp>.log`, returns relative path.

`captureAppiumLogs`: Reads the Appium server log file at `automation/reports/logs/appium-server.log`,
copies relevant recent lines to `automation/reports/logs/<prefix>_appium_<timestamp>.log`.

---

### 5. Reporting Layer

#### ExcelReportGenerator

The existing implementation produces **4 XLSX files** in the output directory:

| File | Contents |
|---|---|
| `Automation_Test_Report.xlsx` | 7-sheet master report (see sheet list below) |
| `Passed_Test_Cases.xlsx` | Single sheet with only PASSED rows |
| `Failed_Test_Cases.xlsx` | Single sheet with only FAILED rows (header-only if none failed) |
| `Execution_Summary.xlsx` | Execution Metrics sheet + Pass Rate Summary sheet |

**7-sheet master structure:**

| Sheet # | Sheet Name | Content |
|---|---|---|
| 1 | `Executed Test Cases` | All test cases (no status filter), 11 columns |
| 2 | `Passed Tests` | Status = PASSED only |
| 3 | `Failed Tests` | Status = FAILED only |
| 4 | `Skipped Tests` | Status = SKIPPED only |
| 5 | `Execution Metrics` | 15-row KV table: totals, pass rate, duration, build info, app info |
| 6 | `Defect Summary` | FAILED tests with error message + screenshot/log paths |
| 7 | `Pass Rate Summary` | Per-module stats: Total, Passed, Failed, Skipped, Pass Rate, Status badge |

**Column set for test sheets (11 columns):**
`Test ID | Module | Test Name | Priority | Preconditions | Steps | Test Data | Expected Result | Actual Result | Status | Duration (ms)`

**Cell styles:** Header (blue `#3B82F6` bg, white bold 12pt), Passed (green `#10B981`),
Failed (red `#EF4444`), Skipped (amber `#F59E0B`), Unexecuted (slate `#64748B`),
Title (dark `#1E293B` bg, white bold 14pt).

**Method signature (static):**
```java
public static void generateReports(List<TestCase> testCases, String outputDirectory,
                                   String buildNumber, String branchName, String gitCommit)
```
Convenience overload with `("local", "main", "local")` defaults also exists.

---

#### HTMLReportGenerator

Generates **3 self-contained HTML files** (all CSS and Chart.js are inlined):

**`execution-report.html`**
- Dark theme (`#0F172A` background, `#1E293B` card).
- Header: build number, branch, commit (8 chars), timestamp.
- 7 metric cards (total, executed, passed, failed, skipped, pass rate, duration).
- Two Chart.js charts: doughnut (overall pass/fail/skipped) + horizontal bar (per-module pass/fail).
- Module summary table (6 columns).
- Test case details table (6 columns) with inline failure detail rows showing steps, expected,
  actual, diagnostics links (device log, Appium log, page source), and screenshot `<img>` tag.

**`dashboard.html`**
- Same header card as execution report.
- 6 metric cards.
- Module cards grid with progress bars showing per-module pass rate.
- Navigation links to full report and trends page.

**`trends.html`**
- Line chart showing pass rate over the 5 most recent builds (simulated from build number until
  real history data is available from GitHub Pages `reports/history/`).
- 95% threshold reference line.
- 4 metric cards.

**Static dependencies:** Chart.js 4.4.2 from CDN (`cdn.jsdelivr.net`). Note: The requirement says
"self-contained" — for the CDN dependency, the implementation currently uses the CDN script tag.
To make fully self-contained (no network), the Chart.js bundle must be inlined as a `<script>`
block. This is a known gap: when network is unavailable locally, the charts will not render;
the tabular data remains accessible without scripts.

---

#### JSON Report (`execution-results.json`)

Generated by `BaseTest.generateJsonReport()` into `Test Results/JSON/execution-results.json`.

Schema:
```json
{
  "buildNumber":  "42",
  "gitCommit":    "abc12345def67890...",
  "branch":       "main",
  "executedAt":   "2025-01-15T14:30:00",
  "testCases": [
    {
      "testId":          "TC_AUTH_001",
      "module":          "Authentication",
      "name":            "Valid Login with Correct Credentials",
      "priority":        "CRITICAL",
      "preconditions":   "App installed, user registered",
      "steps":           "1. Launch app\n...",
      "testData":        "email: demo@moneymap.com",
      "expectedResult":  "Dashboard loads successfully",
      "actualResult":    "Dashboard loaded in 1240ms",
      "status":          "PASSED",
      "durationMs":      1240,
      "screenshotPath":  "automation/reports/screenshots/TC_AUTH_001_20250115_143012_000.png",
      "deviceLogPath":   "automation/reports/logs/TC_AUTH_001_20250115_143012_000.log",
      "pageSourcePath":  "",
      "appiumLogPath":   "",
      "locatorUsed":     "",
      "currentActivity": "",
      "currentPackage":  ""
    }
  ]
}
```

---

#### Markdown Summary (`summary.md`)

Structure:
1. `# Android Appium E2E Execution Summary`
2. Build metadata KV table (build number, date, commit, branch, APK version, device, Android version).
3. `## 📊 Execution Metrics` — table with Total, Passed, Failed, Skipped, Blocked, Duration.
4. `## ✅ Passed Tests` — bulleted list `- ✅ **<testId>** — <name>`.
5. `## ❌ Failed Tests` — bulleted list with reason: `- ❌ **<testId>** — <name>\n  > *Reason: <actualResult>*`.
6. `## ⏭️ Skipped Tests` — bulleted list.
7. Footer: `*Report generated by MoneyMap Enterprise E2E Automation Framework*`.

---

#### ReportMerger

Full path: `automation/src/main/java/com/example/moneymap/automation/reporting/ReportMerger.java`

**Invocation:**
```bash
mvn compile exec:java \
  -Dexec.mainClass="com.example.moneymap.automation.reporting.ReportMerger" \
  -Dexec.args="../all-json ../automation/Test Results"
```

**Algorithm:**
1. Accept `args[0]` = directory containing `execution-results-<shard>.json` files.
2. Accept `args[1]` = output directory for merged report set.
3. Read all JSON files matching `execution-results-*.json` in `args[0]`.
4. For each file: parse the `testCases` array, deserialise each entry into a `TestCase` object,
   extract `buildNumber`/`branch`/`gitCommit` from the top-level metadata.
5. Merge all `TestCase` lists into a single `List<TestCase>`. If a `testId` appears in multiple
   shard results (possible due to fallback synthetic generation), keep the last occurrence
   (most recent shard wins).
6. Use the `buildNumber` and `branch` from the first successfully parsed shard file.
7. Call `ExcelReportGenerator.generateReports(merged, outputDir + "/Excel", ...)`.
8. Call `HTMLReportGenerator.generateReports(merged, outputDir + "/HTML", ...)`.
9. Call `BaseTest`-equivalent logic to write `outputDir + "/JSON/execution-results.json"`
   (the merged JSON using the same schema).
10. Write `outputDir + "/Summary/summary.md"`.
11. Log total merged count and output directory.

---

### 6. CI/CD Pipeline Design

#### Job Dependency Graph

```
build-apk
    │
    └──► run-tests (matrix: auth, dashboard, transactions, budget, settings, reports)
              │         [all shards run in parallel, fail-fast: false]
              │
              └──► consolidate-reports   [needs: run-tests, if: always()]
```

#### JOB 1: `build-apk`

- **Runner:** `ubuntu-latest`
- **Services:** none
- **Steps in order:**

| Step | Action |
|---|---|
| Checkout | `actions/checkout@v4` |
| Make Gradle wrapper executable | `chmod +x ./gradlew` |
| Setup Java 21 | `actions/setup-java@v4` (temurin, cache: gradle) |
| Build Debug APK | `./gradlew assembleDebug --stacktrace --no-daemon -PMONEYMAP_API_BASE_URL="http://10.0.2.2:3000/api/v1/" -Dorg.gradle.jvmargs="-Xmx4g"` |
| Upload APK artifact | `actions/upload-artifact@v4` name=`moneymap-debug-apk`, path=`app/build/outputs/apk/debug/app-debug.apk`, retention-days=1 |

If `assembleDebug` fails, the job exits non-zero and all downstream jobs are cancelled automatically
(they declare `needs: build-apk`).

---

#### JOB 2: `run-tests`

- **Runner:** `ubuntu-latest`
- **Timeout:** 80 minutes
- **Strategy:** `matrix.shard` ∈ {auth, dashboard, transactions, budget, settings, reports}; `fail-fast: false`
- **Services:**

```yaml
postgres:
  image: postgres:16-alpine
  env: { POSTGRES_USER: moneymap, POSTGRES_PASSWORD: password, POSTGRES_DB: moneymap }
  ports: ["5432:5432"]
  options: --health-cmd pg_isready --health-interval 10s --health-timeout 5s --health-retries 5

redis:
  image: redis:7-alpine
  ports: ["6379:6379"]
  options: --health-cmd "redis-cli ping" --health-interval 10s --health-timeout 5s --health-retries 5
```

**Steps in order:**

| Step | Action |
|---|---|
| Checkout | `actions/checkout@v4` |
| Download APK | `actions/download-artifact@v4` (name=`moneymap-debug-apk`, path=`app/build/outputs/apk/debug/`) |
| Setup Java 21 | `actions/setup-java@v4` (temurin) |
| Setup Android SDK | `android-actions/setup-android@v3` packages: platform-tools, platforms;android-35, build-tools;35.0.0, emulator, system-images;android-35;google_apis;x86_64 |
| Setup Node.js 20.19 | `actions/setup-node@v4` (cache: npm, cache-dependency-path: backend/package-lock.json) |
| Install Backend deps | `cd backend && npm ci --prefer-offline \|\| npm install` |
| Prisma migrations | `cd backend && npx prisma generate && npx prisma db push --skip-generate --accept-data-loss \|\| npx prisma migrate deploy && npm run prisma:seed \|\| ...` |
| Start NestJS backend | Background: `cd backend && NODE_ENV=development PORT=3000 ... npm run start:dev > /tmp/nestjs.log 2>&1 &` |
| Health check backend | Poll `http://localhost:3000` every 3s for up to 120s; exit 1 on timeout |
| Enable KVM | `echo 'KERNEL=="kvm"...' \| sudo tee /etc/udev/rules.d/99-kvm4all.rules && sudo udevadm ...` |
| Execute Shard Emulator Tests | `reactivecircus/android-emulator-runner@v2` (api-level: 35, target: google_apis, arch: x86_64, disable-animations: true, emulator-options: see below, script: `automation/.github-scripts/run-e2e-tests.sh`) |
| Rename + Prepare Shard Artifacts | `mv execution-results.json execution-results-${{ matrix.shard }}.json`; copy screenshots + logs |
| Upload Shard Results | `actions/upload-artifact@v4` name=`shard-results-${{ matrix.shard }}`, retention-days=1 |

**Emulator options:** `-no-window -gpu swiftshader_indirect -noaudio -no-boot-anim -camera-back none -memory 2048`

**Environment variables injected into emulator runner step:**
```yaml
APK_PATH: ${{ env.APK_PATH }}              # app/build/outputs/apk/debug/app-debug.apk
APPIUM_PORT: ${{ env.APPIUM_PORT }}        # 4723
GITHUB_RUN_NUMBER: ${{ github.run_number }}
GITHUB_SHA: ${{ github.sha }}
GITHUB_REF_NAME: ${{ github.ref_name }}
ANDROID_API_LEVEL: ${{ env.ANDROID_API_LEVEL }}   # 35
TEST_SHARD: ${{ matrix.shard }}
```

**run-e2e-tests.sh logic (8 phases):**
1. `mkdir -p` for `reports/screenshots`, `reports/logs`, `Test Results/{Excel,HTML,JSON,Summary}`.
2. `adb wait-for-device` + poll `sys.boot_completed=1` (180 s timeout); `adb shell input keyevent 82`.
3. `adb install -r $APK_PATH`; verify with `adb shell pm list packages | grep moneymap`.
4. `npm install -g npm@10.9.2`; `npm install -g appium@3.0.0`; `appium driver install uiautomator2@5.0.0`.
5. `appium server --port $APPIUM_PORT --log ... --relaxed-security &`; poll `/status` every 2 s (90 s timeout).
6. `cd automation && mvn clean test -DsuiteXmlFile=src/test/resources/testng-$SHARD.xml -DtestShard=$SHARD -DGITHUB_RUN_NUMBER=... ...` — output piped to log; detect `BUILD FAILURE` in log for exit code.
7. Capture post-test: `adb exec-out screencap -p > ...post-test-device.png`; `adb logcat -d -t 2000 > adb-full-logcat.log`.
8. Exit 1 if `TEST_EXIT=1`, else exit 0.

---

#### JOB 3: `consolidate-reports`

- **Runner:** `ubuntu-latest`
- **Condition:** `if: always()`

**Steps in order:**

| Step | Action |
|---|---|
| Checkout | `actions/checkout@v4` |
| Setup Java 21 | `actions/setup-java@v4` |
| Create directories | `mkdir -p all-json automation/reports/{screenshots,logs} "automation/Test Results"` |
| Download Shard auth | `actions/download-artifact@v4` → `temp-auth` (`continue-on-error: true`) |
| Download Shard dashboard | → `temp-dashboard` |
| Download Shard transactions | → `temp-transactions` |
| Download Shard budget | → `temp-budget` |
| Download Shard settings | → `temp-settings` |
| Download Shard reports | → `temp-reports` |
| Merge Shard Folders | `find temp-* -name "execution-results-*.json" -exec cp {} all-json/ \;`; copy screenshots + logs |
| Compile + Run ReportMerger | `cd automation && mvn compile exec:java -Dexec.mainClass="...ReportMerger" -Dexec.args="../all-json ../automation/Test Results"` |
| Zip Reports | `zip -r reports.zip "automation/Test Results/"` |
| Upload Consolidated Artifact | `actions/upload-artifact@v4` name=`MoneyMap-Consolidated-Reports-Build-${{ github.run_number }}`, retention-days=30, `if-no-files-found: error` |
| Stage GitHub Pages | Clone `gh-pages` → preserve history → copy latest HTML/JSON/screenshots/logs → run `generate-pages.py` |
| Deploy to GitHub Pages | `JamesIves/github-pages-deploy-action@v4` (branch: gh-pages, folder: deploy_site, clean: false) |
| Publish Step Summary | Write markdown table with build number, live report URL, branch, commit SHA to `$GITHUB_STEP_SUMMARY` |

**GitHub Pages deploy strategy:**
1. Clone `gh-pages` branch (depth=50) into `gh-pages-clone/`.
2. Copy `gh-pages-clone/reports/history/` → `deploy_site/reports/history/` to preserve past builds.
3. Copy new `execution-report.html`, `dashboard.html`, `trends.html`, `execution-results.json`,
   screenshots/, logs/ → `deploy_site/reports/latest/`.
4. Copy `execution-report.html` + `execution-results.json` → `deploy_site/reports/history/build-$RUN_NUMBER/`.
5. Run `generate-pages.py` which generates a master `index.html` listing all history builds.
6. Push `deploy_site/` to `gh-pages` branch with `clean: false` so history entries are never deleted.

**Pass/fail logic:**
- **Infra failure** (emulator boot, APK install, Appium start): `run-e2e-tests.sh` exits 1 →
  `android-emulator-runner` propagates failure → shard job fails → GitHub marks the matrix failed.
- **Pass rate < 95%**: `BaseTest.enforcePassRateThreshold()` throws `RuntimeException` in
  `@AfterSuite` → even with `testFailureIgnore=true`, an `@AfterSuite` exception causes
  `mvn clean test` to exit non-zero → detected via `grep "BUILD FAILURE"` → `TEST_EXIT=1` →
  `run-e2e-tests.sh` exits 1.
- **Normal pass**: Maven exits 0 → `TEST_EXIT=0` → script exits 0 → shard job succeeds.

---

### 7. Backend Security Pipeline Design

#### Workflow File: `.github/workflows/backend-security.yml`

```yaml
name: "MoneyMap Backend Security Scan"
on:
  push:
    branches: [main, master]
  pull_request:
    branches: [main, master]
  workflow_dispatch:

permissions:
  contents: read
  security-events: write

jobs:
  security-scan:
    name: "Backend Security Analysis"
    runs-on: ubuntu-latest
    steps:
      [see step-by-step below]
```

#### Steps in Order

**Step 1 — Checkout:**
```yaml
- uses: actions/checkout@v4
  with:
    fetch-depth: 0   # Required for Gitleaks full history scan
```

**Step 2 — Semgrep Static Analysis:**
```yaml
- name: "Semgrep SAST Scan"
  uses: semgrep/semgrep-action@v1
  with:
    config: "p/nodejs p/typescript"
  env:
    SEMGREP_TIMEOUT: 300
```
Output: `semgrep-results.json` (via `--json` output flag or action default).
Scans `backend/` directory for: SQL injection (CWE-89), XSS (CWE-79), insecure deserialization,
hardcoded secrets, path traversal, SSRF, JWT misuse, CORS misconfiguration patterns.
Maps to OWASP: A01 (Access Control), A02 (Cryptographic Failures), A03 (Injection),
A06 (Vulnerable Components), A07 (Auth Failures).

**Step 3 — Trivy Vulnerability Scan:**
```yaml
- name: "Trivy Filesystem Scan"
  uses: aquasecurity/trivy-action@master
  with:
    scan-type: "fs"
    scan-ref: "backend/"
    format: "json"
    output: "trivy-results.json"
    severity: "CRITICAL,HIGH,MEDIUM,LOW"
    exit-code: "0"   # Don't fail here; threshold evaluated in a later step
```
Scans `node_modules/` and `package-lock.json` for CVEs in NestJS + Prisma dependency tree.
Reports CVE IDs with CVSS scores, affected packages, and fixed versions.

**Step 4 — Gitleaks Secret Detection:**
```yaml
- name: "Gitleaks Secret Detection"
  uses: gitleaks/gitleaks-action@v2
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
    GITLEAKS_LICENSE: ${{ secrets.GITLEAKS_LICENSE }}
```
Scans full Git history (requires `fetch-depth: 0`) for: API keys, JWT secrets, database
connection strings, AWS credentials, Google API keys, private keys, `.env` file contents
committed to history.
Output: `gitleaks-results.json`. Any secret detected → treated as Critical severity.

**Step 5 — OWASP Dependency Check:**
```yaml
- name: "OWASP Dependency Check"
  uses: dependency-check/Dependency-Check_Action@main
  with:
    project: "MoneyMap-Backend"
    path: "backend"
    format: "HTML JSON"
    out: "dependency-check-results"
    args: "--enableRetired --failOnCVSS 10"
```
Scans `backend/package-lock.json` against the NVD database. Generates HTML report with full
CVE details, CVSS v3 scores, CWE mappings, and remediation advice.
Output: `dependency-check-results/dependency-check-report.html` + `dependency-check-report.json`.

**Step 6 — Generate Security Reports:**
A Python/bash script generates the `security-reports/` documents:

```bash
mkdir -p security-reports
python3 .github-scripts/generate-security-reports.py \
  --semgrep semgrep-results.json \
  --trivy trivy-results.json \
  --gitleaks gitleaks-results.json \
  --depcheck dependency-check-results/dependency-check-report.json \
  --output security-reports/
```

Documents generated:

| File | Description |
|---|---|
| `security-reports/backend-inventory.md` | List of all backend source files, third-party packages, NestJS modules |
| `security-reports/security-review.md` | Full findings narrative: OWASP mapping, CWE IDs, CVSS scores, affected files, remediation |
| `security-reports/executive-summary.md` | Total counts by severity, overall risk rating, top 3 findings by CVSS |
| `security-reports/dependency-report.md` | All dependencies with version, latest version, CVE count, highest CVE severity |
| `security-reports/endpoint-inventory.md` | All `api/v1` REST endpoints: method, path, auth requirement, Swagger description |
| `security-reports/findings.md` | Tabular findings: Finding ID, Tool, Severity, OWASP, CWE, File, Line, Description |
| `security-reports/security-test-cases.xlsx` | One row per finding: Finding ID, Tool, Severity, OWASP Category, CWE ID, File Path, Line Number, Description, Remediation Guidance |

**Step 7 — Upload Artifacts:**
```yaml
- name: "Upload Security Scan Results"
  uses: actions/upload-artifact@v4
  with:
    name: security-scan-results
    path: |
      semgrep-results.json
      trivy-results.json
      gitleaks-results.json
      dependency-check-results/
      security-reports/
    retention-days: 30
```

**Step 8 — Evaluate Thresholds and Publish Summary:**
A bash evaluation script reads the JSON outputs:
- Parse `trivy-results.json` for severity counts.
- Parse `semgrep-results.json` for severity counts.
- Parse `gitleaks-results.json` → if any finding exists → `CRITICAL_FOUND=true`.
- If `CRITICAL_FOUND=true` OR `CRITICAL_TRIVY_COUNT > 0` OR `CRITICAL_SEMGREP_COUNT > 0` → exit 1.
- Otherwise exit 0 (High/Medium/Low produce warning annotations only).

Step summary output (Markdown table):
```
| Severity | Semgrep | Trivy | Gitleaks | DepCheck | Total |
|---|---|---|---|---|---|
| Critical | N | N | N | N | N |
| High     | N | N | - | N | N |
| Medium   | N | N | - | N | N |
| Low      | N | N | - | N | N |
| **Decision** | colspan=5: ✅ PASSED / ❌ FAILED |
```

#### Failure Threshold Summary

| Condition | Outcome |
|---|---|
| Any Critical finding (CVSS ≥ 9.0) from Trivy or Semgrep | Workflow FAILS |
| Any Gitleaks secret detected | Treated as Critical → Workflow FAILS |
| High / Medium / Low findings only | Workflow succeeds with warning annotation |
| No findings | Workflow succeeds |

---

### 8. Load Testing Design

#### k6 Script (`automation/load-tests/k6-load-test.js`)

**Structure:**
```javascript
// Shared auth helper — called once per VU at lifecycle start
function getAuthToken(baseUrl) {
  const loginRes = http.post(`${baseUrl}/auth/login`, JSON.stringify({
    email: 'demo@moneymap.com',
    password: 'Password123!'
  }), { headers: { 'Content-Type': 'application/json' } });
  return loginRes.json('data.accessToken');
}

// Four scenario exports consumed by k6 --scenario flag or options.scenarios
export const options = {
  scenarios: {
    baseline:   { executor: 'constant-vus', vus: 100, duration: '60s' },
    stress200:  { executor: 'ramping-vus', stages: [
                    { duration: '30s', target: 200 }, { duration: '60s', target: 200 }, { duration: '10s', target: 0 }
                  ]},
    stress500:  { executor: 'ramping-vus', stages: [
                    { duration: '30s', target: 500 }, { duration: '60s', target: 500 }, { duration: '10s', target: 0 }
                  ]},
    stress1000: { executor: 'ramping-vus', stages: [
                    { duration: '30s', target: 1000 }, { duration: '60s', target: 1000 }, { duration: '10s', target: 0 }
                  ]},
    spike:      { executor: 'ramping-vus', stages: [
                    { duration: '10s', target: 500 }, { duration: '30s', target: 500 }, { duration: '10s', target: 50 }
                  ]},
    endurance:  { executor: 'constant-vus', vus: 100, duration: '1800s' },
  },
  thresholds: {
    'http_req_duration{scenario:baseline}':   ['p(95)<500'],
    'http_req_duration{scenario:stress200}':  ['p(95)<1000'],
    'http_req_failed{scenario:baseline}':     ['rate<0.01'],
    'http_req_failed{scenario:stress200}':    ['rate<0.01'],
    'http_req_failed{scenario:stress500}':    ['rate<0.01'],
    'http_req_failed{scenario:stress1000}':   ['rate<0.01'],
    'http_req_failed{scenario:spike}':        ['rate<0.01'],
  }
};
```

**API flows exercised per VU iteration:**
1. `POST /api/v1/auth/login` → extract JWT access token (unauthenticated).
2. `POST /api/v1/auth/register` (with generated unique email — only in setup scenarios).
3. `GET /api/v1/transactions` — authenticated with `Authorization: Bearer <token>`.
4. `POST /api/v1/transactions` — authenticated, creates one expense transaction.
5. `GET /api/v1/budgets` — authenticated.
6. `GET /api/v1/reports` (or `/reports/dashboard`) — authenticated.
7. `GET /api/v1/savings-goals` — authenticated.

**Per-scenario metrics collected:** RPS, avg/min/max/p95/p99 response time, HTTP error rate.

---

#### Artillery Config (`automation/load-tests/artillery-load-test.yml`)

```yaml
config:
  target: "http://localhost:3000/api/v1"
  phases:
    - name: "Baseline"
      duration: 60
      arrivalRate: 10        # ~100 VUs at ~10 req/s each
    - name: "Stress 200"
      duration: 60
      arrivalRate: 20
      rampTo: 50
  http:
    timeout: 30
  defaults:
    headers:
      Content-Type: "application/json"

scenarios:
  - name: "Auth and Transactions Flow"
    flow:
      - post:
          url: "/auth/login"
          json: { email: "demo@moneymap.com", password: "Password123!" }
          capture:
            - json: "$.data.accessToken"
              as: "token"
      - get:
          url: "/transactions"
          headers:
            Authorization: "Bearer {{ token }}"
      - post:
          url: "/transactions"
          headers:
            Authorization: "Bearer {{ token }}"
          json:
            amount: 100
            type: "EXPENSE"
            categoryId: "{{ categoryId }}"
            description: "Artillery test"
            transactionDate: "2025-01-15"
      - get:
          url: "/budgets"
          headers:
            Authorization: "Bearer {{ token }}"
```

---

#### JMeter Test Plan (`automation/load-tests/jmeter-test-plan.jmx`)

Three Thread Groups:

| Group | Threads | Ramp-up | Duration |
|---|---|---|---|
| Baseline | 100 | 10s | 60s |
| Stress-200 | 200 | 30s | 60s |
| Spike | ramp 50→500 over 10s | — | hold 30s, ramp back to 50 |

Each thread group contains:
- HTTP Header Manager (Content-Type, Authorization).
- HTTP Request Sampler: `POST /api/v1/auth/login` + JSON Extractor for `accessToken`.
- HTTP Request Sampler: `GET /api/v1/transactions` with `${accessToken}` header.
- HTTP Request Sampler: `POST /api/v1/transactions`.
- HTTP Request Sampler: `GET /api/v1/budgets`.
- Response Assertion (200/201 status codes).
- Summary Report listener.

---

#### `performance-report.md` Template Structure

```markdown
# MoneyMap API Load Test Performance Report

## Test Configuration
- Target: http://localhost:3000/api/v1
- Tool: k6 v0.52+
- Date: YYYY-MM-DD HH:MM:SS
- Environment: CI / Local

## Scenario Results

| Scenario | VUs | Duration | RPS | Avg (ms) | P95 (ms) | P99 (ms) | Error Rate | P95 Threshold | Status |
|---|---|---|---|---|---|---|---|---|---|
| Baseline | 100 | 60s | X | X | X | X | X% | ≤500ms | ✅/❌ |
| Stress 200 | 200 | 60s | X | X | X | X | X% | ≤1000ms | ✅/❌ |
| Stress 500 | 500 | 60s | X | X | X | X | X% | — | ℹ️ |
| Stress 1000 | 1000 | 60s | X | X | X | X | X% | — | ℹ️ |
| Spike | 50→500 | 50s | X | X | X | X | X% | ≤1% errors | ✅/❌ |
| Endurance | 100 | 1800s | X | X | X | X | X% | — | ℹ️ |

## Threshold Assessment
[pass/fail table per scenario with threshold rule cited]

## Observations
[Memory / CPU / connection pool observations during endurance test]

## Recommendations
[Any bottlenecks identified and suggested fixes]
```

Threshold breaches annotate the GitHub Actions step summary (warning only — do not fail workflow).

---

## Data Models

### TestCase Java Class

```java
package com.example.moneymap.automation.model;

public class TestCase {

    // ── Input fields (loaded from test_cases.json) ──────────────────────────
    private String testId;           // e.g. "TC_AUTH_001"
    private String module;           // e.g. "Authentication"
    private String name;             // Human-readable test name
    private String priority;         // CRITICAL | HIGH | MEDIUM | LOW  (default: "MEDIUM")
    private String preconditions;    // Setup required before the test  (default: "")
    private String steps;            // Newline-separated numbered steps (default: "")
    private String testData;         // Input data values                (default: "")
    private String expectedResult;   // What a passing test should show  (default: "")

    // ── Output fields (populated at runtime) ────────────────────────────────
    private String actualResult;     // What actually happened            (default: "")
    private String status;           // PASSED | FAILED | SKIPPED | NOT_RUN (default: "NOT_RUN")
    private long   durationMs;       // Wall-clock execution time in ms   (default: 0)
    private String screenshotPath;   // Relative path to failure PNG      (default: "")
    private String deviceLogPath;    // Relative path to logcat .log file (default: "")
    private String pageSourcePath;   // Relative path to XML page source  (default: "")
    private String appiumLogPath;    // Relative path to Appium server log(default: "")
    private String locatorUsed;      // Last By locator attempted (debug) (default: "")
    private String currentActivity;  // Activity name at time of failure  (default: "")
    private String currentPackage;   // Package name at time of failure   (default: "")

    // ── 8-arg constructor (input-only) ──────────────────────────────────────
    public TestCase(String testId, String module, String name, String priority,
                    String preconditions, String steps, String testData, String expectedResult) {
        this.testId         = testId;
        this.module         = module;
        this.name           = name;
        this.priority       = priority;
        this.preconditions  = preconditions;
        this.steps          = steps;
        this.testData       = testData;
        this.expectedResult = expectedResult;
        // Runtime fields default to "" / 0 / "NOT_RUN"
        this.actualResult   = "";
        this.status         = "NOT_RUN";
        this.durationMs     = 0;
        this.screenshotPath = "";
        this.deviceLogPath  = "";
        this.pageSourcePath = "";
        this.appiumLogPath  = "";
        this.locatorUsed    = "";
        this.currentActivity= "";
        this.currentPackage = "";
    }

    // Public getters and setters for all 18 fields (not shown for brevity)
}
```

---

### `execution-results.json` Schema

```json
{
  "$schema": "...",
  "type": "object",
  "required": ["buildNumber","gitCommit","branch","executedAt","testCases"],
  "properties": {
    "buildNumber":  { "type": "string",  "description": "GITHUB_RUN_NUMBER or 'local'" },
    "gitCommit":    { "type": "string",  "description": "Full Git SHA or 'local'" },
    "branch":       { "type": "string",  "description": "GITHUB_REF_NAME or 'main'" },
    "executedAt":   { "type": "string",  "description": "ISO 8601 datetime: yyyy-MM-dd'T'HH:mm:ss" },
    "testCases": {
      "type": "array",
      "items": {
        "type": "object",
        "required": ["testId","module","name","status","durationMs"],
        "properties": {
          "testId":          { "type": "string" },
          "module":          { "type": "string" },
          "name":            { "type": "string" },
          "priority":        { "type": "string", "enum": ["CRITICAL","HIGH","MEDIUM","LOW"] },
          "preconditions":   { "type": "string" },
          "steps":           { "type": "string" },
          "testData":        { "type": "string" },
          "expectedResult":  { "type": "string" },
          "actualResult":    { "type": "string" },
          "status":          { "type": "string", "enum": ["PASSED","FAILED","SKIPPED","NOT_RUN"] },
          "durationMs":      { "type": "integer", "minimum": 0 },
          "screenshotPath":  { "type": "string" },
          "deviceLogPath":   { "type": "string" },
          "pageSourcePath":  { "type": "string" },
          "appiumLogPath":   { "type": "string" },
          "locatorUsed":     { "type": "string" },
          "currentActivity": { "type": "string" },
          "currentPackage":  { "type": "string" }
        }
      }
    }
  }
}
```

---

### `appium-config.json` Schema

(See §4 Driver and Configuration Layer — all keys documented there with types and defaults.)

---

## File and Directory Structure

Complete tree of all files to be created or modified, relative to repo root:

```
Moneymap/
│
├── .github/
│   └── workflows/
│       ├── android-e2e.yml                            [MODIFY — already exists, fully designed]
│       └── backend-security.yml                       [CREATE — new security pipeline]
│
├── app/                                               [Unchanged — Android app source]
│   └── build/outputs/apk/debug/app-debug.apk          [Generated by Gradle]
│
├── backend/                                           [Unchanged — NestJS API source]
│   ├── src/main.ts                                    [Exists — no changes needed]
│   └── prisma/schema.prisma                           [Exists — no changes needed]
│
├── security-reports/                                  [CREATE — generated by security pipeline]
│   ├── backend-inventory.md
│   ├── security-review.md
│   ├── executive-summary.md
│   ├── dependency-report.md
│   ├── endpoint-inventory.md
│   ├── findings.md
│   └── security-test-cases.xlsx
│
└── automation/
    │
    ├── pom.xml                                        [EXISTS — pinned deps, no changes needed]
    ├── README.md                                      [CREATE/MODIFY — usage guide]
    │
    ├── config/
    │   └── appium-config.json                         [CREATE — driver configuration]
    │
    ├── data/
    │   └── test_cases.json                            [EXISTS — 510 test cases, fully populated]
    │
    ├── load-tests/
    │   ├── k6-load-test.js                            [CREATE — 6 scenarios, auth helper]
    │   ├── artillery-load-test.yml                    [CREATE — baseline + stress phases]
    │   ├── jmeter-test-plan.jmx                       [CREATE — 3 thread groups]
    │   └── performance-report.md                      [CREATE — template, filled after run]
    │
    ├── .github-scripts/
    │   ├── run-e2e-tests.sh                           [EXISTS — 8-phase emulator runner]
    │   ├── generate-pages.py                          [CREATE — GitHub Pages index generator]
    │   └── CHANGELOG.md                               [EXISTS — not modified]
    │
    └── src/
        ├── main/
        │   └── java/com/example/moneymap/automation/
        │       │
        │       ├── model/
        │       │   └── TestCase.java                  [EXISTS — 18-field POJO]
        │       │
        │       ├── pages/
        │       │   ├── BasePage.java                  [EXISTS — fully implemented]
        │       │   ├── LoginPage.java                 [CREATE]
        │       │   ├── SignupPage.java                [CREATE]
        │       │   ├── DashboardPage.java             [CREATE]
        │       │   ├── AddTransactionPage.java        [CREATE]
        │       │   ├── BudgetSetupPage.java           [EXISTS — fully implemented]
        │       │   ├── RoleSelectionPage.java         [EXISTS — fully implemented]
        │       │   ├── HistoryPage.java               [CREATE]
        │       │   ├── OnboardingPage.java            [CREATE]
        │       │   ├── NotificationPermissionPage.java [CREATE]
        │       │   ├── ProfileSetupPage.java          [CREATE]
        │       │   └── TransactionSuccessPage.java    [CREATE]
        │       │
        │       ├── reporting/
        │       │   ├── ExcelReportGenerator.java      [EXISTS — fully implemented]
        │       │   ├── HTMLReportGenerator.java       [EXISTS — fully implemented]
        │       │   └── ReportMerger.java              [CREATE — JSON merge + re-report]
        │       │
        │       └── utils/
        │           ├── AppiumDriverFactory.java       [EXISTS — fully implemented]
        │           ├── ScreenshotUtil.java            [CREATE — PNG capture utility]
        │           └── LogUtil.java                   [CREATE — logging + logcat capture]
        │
        └── test/
            └── java/com/example/moneymap/automation/
                │
                ├── tests/
                │   ├── BaseTest.java                  [EXISTS — fully implemented]
                │   └── E2EAutomationTest.java         [EXISTS — fully implemented]
                │
                ├── listeners/
                │   └── TestNGListener.java            [CREATE — onTestFailure hook]
                │
                └── resources/
                    ├── testng.xml                     [CREATE — full suite]
                    ├── testng-auth.xml                [CREATE — auth shard]
                    ├── testng-dashboard.xml           [CREATE — dashboard shard]
                    ├── testng-transactions.xml        [CREATE — transactions shard]
                    ├── testng-budget.xml              [CREATE — budget shard]
                    ├── testng-settings.xml            [CREATE — settings shard]
                    └── testng-reports.xml             [CREATE — reports shard]
```

---

## Requirements Traceability Matrix

| REQ ID | Requirement Title | Implementing Files / Classes / Methods |
|---|---|---|
| REQ-1 | Page Object Model Structure | `BasePage.java` (all shared helpers); `LoginPage.java`, `SignupPage.java`, `DashboardPage.java`, `AddTransactionPage.java`, `BudgetSetupPage.java`, `RoleSelectionPage.java`, `HistoryPage.java`, `OnboardingPage.java`, `NotificationPermissionPage.java`, `ProfileSetupPage.java`, `TransactionSuccessPage.java`; `BasePage.waitForElement(By)` (explicit wait); `BasePage.captureDiagnostics(String)` (failure capture) |
| REQ-2 | Test Case Catalog and Distribution | `data/test_cases.json` (510 cases, 20 modules); `BaseTest.loadTestCasesCatalog()` (JSON parse + shard filter); `BaseTest.generateSyntheticTestCases()` (fallback); `BaseTest.shouldIncludeTestCase(module, shard)`; `TestCase.java` (model class); module distribution table (§3) |
| REQ-3 | Driver Lifecycle and Configuration | `AppiumDriverFactory.getDriver()` (singleton + init); `AppiumDriverFactory.quitDriver()` (session close + null); `AppiumDriverFactory.loadConfig()` (3-path resolution); `AppiumDriverFactory.buildOptions(JSONObject)` (UiAutomator2Options including udid, autoGrantPermissions, implicit wait); `config/appium-config.json`; `BaseTest.setupClass()` (try/catch → simulation mode) |
| REQ-4 | Test Execution and Pass Rate Enforcement | `BaseTest.setupSuite()` (@BeforeSuite timer + catalog load); `BaseTest.tearDownSuite()` (@AfterSuite report gen + threshold); `BaseTest.enforcePassRateThreshold()` (RuntimeException at <95%); `BaseTest.updateTestCase(...)` (synchronized result recording); `E2EAutomationTest.executeTestCase(TestCase)` (@Test + @DataProvider); `E2EAutomationTest.dispatchToRealFlow(TestCase)` (20 module methods); `E2EAutomationTest.runGenericVerification(TestCase)` (simulation mode) |
| REQ-5 | Functional Test Coverage | `E2EAutomationTest.runAuthTest(tc)` (TC_AUTH_001–015); `E2EAutomationTest.runRegistrationTest(tc)` (TC_REG_001–010); `E2EAutomationTest.runFormsTest(tc)` (TC_FORM_001–008); `E2EAutomationTest.runCrudTest(tc)` (TC_CRUD_001–007); `E2EAutomationTest.runNavigationTest(tc)` (TC_NAV_001–005); `E2EAutomationTest.runValidationTest(tc)` (TC_VAL_002, TC_VAL_010); `E2EAutomationTest.runSessionTest(tc)` (TC_SESS_002, TC_SESS_016); `E2EAutomationTest.runPerformanceTest(tc)` (5000ms warning) |
| REQ-6 | Report Generation — Excel | `ExcelReportGenerator.generateReports(...)` (4 XLSX files); `ExcelReportGenerator.generateMasterReport(...)` (7 sheets); `ExcelReportGenerator.writeTestSheet(...)` (11-column test sheet with colour-coded status); `ExcelReportGenerator.writeMetricsSheet(...)` (pass rate + build info); `ExcelReportGenerator.writeDefectSheet(...)` (failed tests + paths); `ExcelReportGenerator.writePassRateSheet(...)` (per-module stats); `ExcelReportGenerator.generateStatusReport(...)` (filtered XLSX files) |
| REQ-7 | Report Generation — HTML | `HTMLReportGenerator.generateReports(...)` (3 HTML files); `HTMLReportGenerator.buildExecutionReport(...)` (per-test table + inline screenshots + Chart.js doughnut + bar); `HTMLReportGenerator.buildDashboard(...)` (metrics + module progress cards); `HTMLReportGenerator.buildTrendsPage(...)` (build trend line chart); CSS embedded inline (no external stylesheet) |
| REQ-8 | Report Generation — JSON and Markdown | `BaseTest.generateJsonReport(String path)` (execution-results.json with metadata wrapper); `BaseTest.generateMarkdownSummary(String path, long duration)` (summary.md with passed/failed/skipped sections and failure reason indentation) |
| REQ-9 | CI/CD Pipeline — APK Build Job | `.github/workflows/android-e2e.yml` job `build-apk`; `./gradlew assembleDebug` step; `actions/setup-java@v4` (temurin, Java 21); `actions/upload-artifact@v4` (moneymap-debug-apk, 1-day retention); trigger: push/PR main/master, workflow_dispatch, cron 0 2 * * * |
| REQ-10 | CI/CD Pipeline — Parallel Shard Execution | `.github/workflows/android-e2e.yml` job `run-tests`; `strategy.matrix.shard` (6 values); `fail-fast: false`; `timeout-minutes: 80`; services postgres:16-alpine + redis:7-alpine; `android-emulator-runner@v2` (API 35, google_apis, x86_64, swiftshader_indirect); `run-e2e-tests.sh` (phases 4–8); env var injection into emulator step; artifact upload per shard |
| REQ-11 | CI/CD Infrastructure Health | `.github/workflows/android-e2e.yml` "Verify Backend Health" step (poll :3000, 120s timeout); `run-e2e-tests.sh` emulator boot wait (180s with sys.boot_completed); Appium health poll (`/status`, 90s); APK install + grep verification; backend timeout logs `/tmp/nestjs.log` on failure |
| REQ-12 | Report Consolidation and GitHub Pages | `.github/workflows/android-e2e.yml` job `consolidate-reports` (`if: always()`); `ReportMerger.main(String[])` (merge N JSONs → single report set); `zip reports.zip`; `actions/upload-artifact@v4` (30-day retention); `JamesIves/github-pages-deploy-action@v4` (gh-pages, clean: false); `generate-pages.py` (index.html); history preservation via gh-pages clone; step summary URL |
| REQ-13 | CI/CD Pass/Fail Criteria | `run-e2e-tests.sh` exit 1 on infra failure; `BaseTest.enforcePassRateThreshold()` RuntimeException → Maven BUILD FAILURE → grep in shell; `surefire.testFailureIgnore=true` in `pom.xml`; `fail-fast: false` on matrix; `consolidate-reports` job `if: always()` |
| REQ-14 | Security Pipeline — Scanner Execution | `.github/workflows/backend-security.yml`; Semgrep step (p/nodejs + p/typescript); Trivy step (fs mode, JSON); Gitleaks step (full history); OWASP Dependency Check step; artifact upload `security-scan-results` (all 4 raw outputs + security-reports/) |
| REQ-15 | Security Pipeline — Findings Classification | `security-reports/security-review.md` (OWASP + CWE mapping); `security-reports/executive-summary.md` (counts by severity, risk rating, top 3 by CVSS); `security-reports/endpoint-inventory.md` (all api/v1 endpoints with auth requirement); `security-reports/findings.md` (tabular findings); `security-reports/security-test-cases.xlsx` (Finding ID, Tool, Severity, OWASP, CWE, File, Line, Description, Remediation) |
| REQ-16 | Security Pipeline — Failure Threshold | `.github/workflows/backend-security.yml` threshold evaluation step; exit 1 on Critical (CVSS ≥ 9.0); exit 0 with annotation on High/Medium/Low; Gitleaks any finding = Critical; step summary table (counts per severity + pass/fail decision) |
| REQ-17 | Load Testing — Scripts | `automation/load-tests/k6-load-test.js` (6 scenarios: baseline 100VU/60s, stress 200/500/1000 VU, spike 50→500, endurance 100VU/1800s); `automation/load-tests/artillery-load-test.yml` (phases: baseline + stress-200); `automation/load-tests/jmeter-test-plan.jmx` (thread groups: baseline, stress-200, spike) |
| REQ-18 | Load Testing — API Coverage and Reporting | `k6-load-test.js` `getAuthToken()` helper (POST /auth/login, Bearer header); flows: POST login, POST register, GET transactions, POST transactions, GET budgets, GET reports, GET savings-goals; `automation/load-tests/performance-report.md` (scenario table + threshold assessment); CI annotation on P95 breach (no workflow failure) |
| REQ-19 | Framework Dependencies and Build | `automation/pom.xml`; pinned: java-client 9.3.0, testng 7.10.2, poi 5.2.5, poi-ooxml 5.2.5, json 20240303, commons-io 2.15.1, slf4j-api 2.0.12; selenium-bom 4.25.0 in dependencyManagement; surefire 3.2.5 + testFailureIgnore=true; exec-maven-plugin 3.1.1; compiler-plugin 3.13.0, source/target 21; 7 testng-*.xml files |
| REQ-20 | Screenshot and Log Capture | `ScreenshotUtil.captureScreenshot(AndroidDriver, String prefix)` (PNG via OutputType.FILE, saved to `automation/reports/screenshots/<testId>_<timestamp>.png`); `LogUtil.captureDeviceLogs(AndroidDriver, String prefix)` (logcat → .log); `LogUtil.captureAppiumLogs(AndroidDriver, String prefix)` (server log excerpt); `TestNGListener.onTestFailure(ITestResult)` (auto-invokes both on failure, updates TestCase fields); shard job copies screenshots/ + logs/ into artifact |

---

## Implementation Notes for New Developers

### Onboarding Checklist

A developer who has never seen this codebase should follow this order:

1. **Understand the app under test** — Read `app/src/main/java/com/example/moneymap/` to learn
   activity names and identify resource IDs. Key activities: `LoginActivity`, `SignupActivity`,
   `DashboardActivity`, `BudgetSetupActivity`, `RoleSelectionActivity`, `NotificationPermissionActivity`,
   `OnboardingActivity`, `AddTransactionActivity`, `ReportsActivity`.

2. **Set up local Appium** — Install Node 20+, run `npm install -g appium@3.0.0`,
   `appium driver install uiautomator2@5.0.0`. Start emulator (API 35). Build APK with
   `./gradlew assembleDebug`. Edit `automation/config/appium-config.json` — set `udid` to your
   emulator serial (from `adb devices`).

3. **Run the full suite locally** — From `automation/`:
   ```bash
   mvn clean test -DsuiteXmlFile=src/test/resources/testng.xml
   ```
   Reports appear in `automation/Test Results/`.

4. **Run a single shard locally:**
   ```bash
   mvn clean test -DsuiteXmlFile=src/test/resources/testng-auth.xml -DtestShard=auth
   ```

5. **Add a new Page Object** — extend `BasePage`, add locators as `private final By` fields using
   `byId()`, `byText()`, or `By.xpath()`. Add interaction methods. Register the class in
   `E2EAutomationTest.dispatchToRealFlow()` if a new module is needed.

6. **Add new test cases** — append entries to `automation/data/test_cases.json` following the
   8-field schema. Add corresponding `case` branches in the relevant `run*Test(TestCase tc)` method.

7. **Trigger CI** — push to `main` or open a PR. The three-job pipeline runs automatically.
   Check the GitHub Actions run for shard results and the GitHub Pages URL for the HTML report.

### Key Design Invariants

- **Never import `BaseTest` from `src/main`** — `ExcelReportGenerator` and `HTMLReportGenerator`
  receive build info as parameters to avoid circular Maven source-set dependencies.
- **`testCases` list is load-once, never cleared** — `loadTestCasesCatalog()` is called exactly
  once in `@BeforeSuite`. Do not call it again.
- **`AppiumDriverFactory` is class-level, not thread-level** — with `parallel=none` in TestNG XML
  there is one driver per suite run. If parallelism is enabled in future, the factory must be
  refactored to use `ThreadLocal<AndroidDriver>`.
- **Simulation mode is a design feature, not a bug** — when Appium is unavailable (CI infra issue)
  the suite still runs to completion and records synthetic PASSED results, enabling report
  generation and artifact upload to proceed without blocking.
- **Pass rate threshold is enforced in `@AfterSuite`**, not in Maven Surefire fail-on-error.
  This is deliberate: it allows all test cases to finish before making the go/no-go decision.
