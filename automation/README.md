# MoneyMap Enterprise E2E Automation Framework

> **510+ Appium test cases** · Excel/HTML/JSON/Markdown reports · GitHub Pages publishing · Full CI/CD pipeline

---

## 📁 Framework Structure

```
automation/
├── config/
│   └── appium-config.json          # Appium capabilities config
├── data/
│   ├── test_cases.json             # 510+ test case catalog
│   └── test_data.json              # Test credentials and data
├── src/
│   ├── main/java/com/example/moneymap/automation/
│   │   ├── model/
│   │   │   └── TestCase.java       # Test case data model
│   │   ├── pages/                  # Page Object Model classes
│   │   │   ├── BasePage.java
│   │   │   ├── LoginPage.java
│   │   │   ├── SignupPage.java
│   │   │   ├── RoleSelectionPage.java
│   │   │   ├── ProfileSetupPage.java
│   │   │   ├── BudgetSetupPage.java
│   │   │   ├── NotificationPermissionPage.java
│   │   │   ├── DashboardPage.java
│   │   │   ├── AddTransactionPage.java
│   │   │   ├── TransactionSuccessPage.java
│   │   │   ├── HistoryPage.java
│   │   │   └── OnboardingPage.java
│   │   ├── reporting/
│   │   │   ├── ExcelReportGenerator.java   # 7-sheet XLSX reports
│   │   │   └── HTMLReportGenerator.java    # Dark-theme HTML reports
│   │   └── utils/
│   │       ├── AppiumDriverFactory.java    # Singleton driver factory
│   │       ├── ScreenshotUtil.java         # Screenshot capture
│   │       └── LogUtil.java               # Structured logging
│   └── test/java/com/example/moneymap/automation/
│       ├── listeners/
│       │   └── TestNGListener.java        # Pass/fail capture
│       └── tests/
│           ├── BaseTest.java              # Suite lifecycle + reporting
│           └── E2EAutomationTest.java     # 510-case test executor
├── Test Results/
│   ├── Excel/                      # *.xlsx reports (generated)
│   ├── HTML/                       # *.html reports (generated)
│   ├── JSON/                       # execution-results.json (generated)
│   └── Summary/                    # summary.md (generated)
├── reports/
│   ├── screenshots/                # Failure screenshots (generated)
│   └── logs/                       # Execution + device logs (generated)
├── pom.xml                         # Maven build + dependencies
└── README.md
```

---

## 🧪 Test Case Distribution (510 Total)

| Module | Count |
|--------|-------|
| Authentication | 40 |
| Authorization | 30 |
| Registration | 20 |
| Profile Management | 20 |
| Navigation | 30 |
| Dashboard | 20 |
| Forms | 40 |
| CRUD Operations | 40 |
| Search | 20 |
| Filters | 20 |
| Input Validation | 40 |
| Error Handling | 20 |
| Session Management | 20 |
| Notifications | 20 |
| File Upload | 20 |
| Offline Handling | 10 |
| Accessibility | 20 |
| Responsive UI | 10 |
| Performance Smoke Tests | 20 |
| Regression Suite | 50 |
| **Total** | **510** |

---

## 🚀 Local Execution Guide

### Prerequisites
- Java 21+
- Maven 3.8+
- Node.js 18+
- Android SDK (ANDROID_HOME set)
- Android Emulator or physical device

### Step 1 — Build APK
```bash
# From project root
./gradlew assembleDebug
```

### Step 2 — Start Emulator
```bash
emulator -avd <your_avd_name> -no-snapshot -gpu swiftshader_indirect &
adb wait-for-device
```

### Step 3 — Install APK
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Step 4 — Install & Start Appium
```bash
npm install -g appium
appium driver install uiautomator2
appium &
```

### Step 5 — Run Tests
```bash
cd automation
mvn clean test
```

### Step 6 — View Reports
Reports are generated in:
- `automation/Test Results/HTML/execution-report.html`
- `automation/Test Results/Excel/Automation_Test_Report.xlsx`
- `automation/Test Results/JSON/execution-results.json`
- `automation/Test Results/Summary/summary.md`

---

## 🔄 CI/CD Execution Guide

The pipeline runs automatically on every push via `.github/workflows/android-e2e.yml`.

**Pipeline stages:**
1. Checkout → Java 21 → Node 18 → Android SDK
2. Build debug APK → Verify APK
3. Launch Android Emulator (API 35, google_apis, x86_64)
4. Install APK → Install Appium → Start Appium
5. Execute 510+ test cases via `mvn clean test`
6. Generate Excel + HTML + JSON + Markdown reports
7. Deploy reports to GitHub Pages
8. Upload artifacts (30-day retention)
9. Publish GitHub Actions summary

**Live Report URL:**
```
https://<github-username>.github.io/<repository-name>/reports/latest/execution-report.html
```

---

## 📊 Report Files

| File | Description |
|------|-------------|
| `Automation_Test_Report.xlsx` | 7-sheet master report |
| `Passed_Test_Cases.xlsx` | Passed tests only |
| `Failed_Test_Cases.xlsx` | Failed tests with error details |
| `Execution_Summary.xlsx` | Metrics + module pass rates |
| `execution-report.html` | Full dark-theme HTML report with charts |
| `dashboard.html` | Module breakdown dashboard |
| `trends.html` | Historical pass rate trends |
| `execution-results.json` | Machine-readable full results |
| `summary.md` | GitHub Actions step summary |
| `screenshots/*.png` | Failure screenshots |
| `logs/execution.log` | Full execution log |
| `logs/*_device_*.log` | Per-test device logcat |

---

## ⚙️ Configuration

Edit `config/appium-config.json` to update:
- `deviceName` — target device/emulator name
- `appiumUrl` — Appium server URL (default: `http://127.0.0.1:4723`)
- `noReset` — `true` to skip app reset between runs
- `app` — absolute path to APK (auto-resolved in CI)

---

## 🔧 Troubleshooting Guide

### Appium fails to start
```bash
# Check Node.js version
node --version   # must be >= 16

# Reinstall Appium
npm uninstall -g appium
npm install -g appium
appium driver install uiautomator2

# Check Appium is responding
curl http://127.0.0.1:4723/status
```

### Emulator not booting (CI)
- Runner must be `macos-13` (Intel) for HVF support
- Use API 35 with `google_apis` target and `x86_64` arch
- Increase `timeout-minutes` in workflow if needed

### APK not found
- Ensure `./gradlew assembleDebug` succeeded
- Check `app/build/outputs/apk/debug/app-debug.apk` exists
- The factory auto-resolves relative paths

### Tests fail with "element not found"
- Compose elements use accessibility text/content-desc (no android:id)
- Page Objects use multiple fallback locators
- Increase `DEFAULT_TIMEOUT` in `BasePage.java` (default: 15s)

### Pass rate below 95%
- Check `automation/reports/logs/execution.log` for failures
- View `Test Results/HTML/execution-report.html` failure detail rows
- Screenshots in `reports/screenshots/` show UI state at failure

### GitHub Pages not updating
- Ensure `GITHUB_TOKEN` has `pages: write` and `contents: write`
- Check Actions > gh-pages branch deployment logs
- First run may require enabling Pages in repository Settings

---

## 📦 GitHub Repository Configuration

1. **Enable GitHub Pages:**
   - Settings → Pages → Source: `gh-pages` branch, `/ (root)`

2. **Required Permissions (auto-configured in workflow):**
   - `contents: write` — push to gh-pages
   - `pages: write` — GitHub Pages deployment
   - `id-token: write` — OIDC for Pages

3. **Secrets needed:** None — uses `GITHUB_TOKEN` (auto-provided)

4. **Branch protection:** Workflow runs on `main`, `master`, `develop`
