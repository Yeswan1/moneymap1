# GitHub Actions Scripts

This directory contains utility scripts used by the GitHub Actions CI/CD pipeline.

## Scripts

### `run-e2e-tests.sh`

Main test execution script for Android emulator and Appium automation.

**Purpose:**
- Waits for Android emulator to fully boot
- Installs the APK on the emulator
- Sets up Appium server and UIAutomator2 driver
- Executes 510+ Maven/TestNG test cases
- Captures screenshots, logs, and test evidence

**Usage:**
```bash
chmod +x run-e2e-tests.sh
./run-e2e-tests.sh
```

**Environment Variables Required:**
- `APK_PATH` - Path to the APK file to install
- `APPIUM_PORT` - Port number for Appium server (default: 4723)
- `GITHUB_RUN_NUMBER` - Build number for Maven properties
- `GITHUB_SHA` - Commit SHA for Maven properties
- `GITHUB_REF_NAME` - Branch name for Maven properties
- `ANDROID_API_LEVEL` - Android API level being tested

**Called by:**
- `.github/workflows/android-e2e.yml` in Stage 12-18 (Emulator Appium Tests)

---

### `generate-pages.py`

Generates GitHub Pages HTML files for the E2E test report deployment.

**Purpose:**
- Creates the main landing page (`index.html`) with test execution summary
- Generates the build history index (`history/index.html`) listing all previous reports
- Dynamically adjusts content based on test execution status

**Usage:**
```bash
python3 generate-pages.py
```

**Environment Variables Required:**
- `GH_RUN_NUMBER` - GitHub Actions run number
- `GH_REF_NAME` - Branch name
- `GH_SHA` - Commit SHA
- `GH_REPOSITORY` - Repository full name (owner/repo)
- `GH_REPO_OWNER` - Repository owner
- `GH_RUN_ID` - GitHub Actions run ID

**Output:**
- `deploy_site/index.html` - Main landing page with auto-redirect to latest report
- `deploy_site/reports/history/index.html` - Build history page

**Called by:**
- `.github/workflows/android-e2e.yml` in Stage 20a (Prepare GitHub Pages Deploy Structure)
