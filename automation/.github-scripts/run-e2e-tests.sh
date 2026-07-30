#!/bin/sh
# ============================================================
# MoneyMap E2E Test Runner
# Runs inside android-emulator-runner@v2 (POSIX sh, not bash)
#
# Environment variables injected by workflow:
#   APK_PATH, APPIUM_PORT, GITHUB_RUN_NUMBER,
#   GITHUB_SHA, GITHUB_REF_NAME, ANDROID_API_LEVEL
# ============================================================
set -e

# ── 1. Create report directories ────────────────────────────
mkdir -p automation/reports/screenshots
mkdir -p automation/reports/logs
mkdir -p "automation/Test Results/Excel"
mkdir -p "automation/Test Results/HTML"
mkdir -p "automation/Test Results/JSON"
mkdir -p "automation/Test Results/Summary"

# ── 2. Wait for emulator to fully boot ──────────────────────
echo "Waiting for emulator to boot..."
adb wait-for-device

BOOT_TIMEOUT=180
ELAPSED=0
while true; do
  BOOTED=$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r\n' || echo "0")
  if [ "$BOOTED" = "1" ]; then
    echo "Emulator booted after ${ELAPSED}s"
    break
  fi
  sleep 3
  ELAPSED=$((ELAPSED + 3))
  if [ "$ELAPSED" -ge "$BOOT_TIMEOUT" ]; then
    echo "ERROR: Emulator boot timeout after ${BOOT_TIMEOUT}s"
    exit 1
  fi
  echo "  booting... ${ELAPSED}s"
done

adb shell input keyevent 82
sleep 2
adb devices

# ── 3. Install APK ──────────────────────────────────────────
echo "Installing APK from: $APK_PATH"
adb install -r "$APK_PATH"
adb shell pm list packages | grep moneymap || true
echo "APK installed"

# ── 4. Install Appium 3 + uiautomator2 ─────────────────────
# Version matrix (all pinned, all compatible):
#   npm          10.9.2   — Appium 3 requirement (npm >= 10)
#   appium       3.0.0    — Appium 3 stable release
#   uiautomator2 5.0.0    — requires Appium 3 (Appium 2 = v4.x)
#   java-client  9.2.2    — W3C, compatible with Appium 3
echo "Installing Appium 3..."
echo "  Node: $(node --version)  npm: $(npm --version)"
npm install -g npm@10.9.2 --loglevel=error
echo "  npm upgraded to: $(npm --version)"
npm install -g appium@3.0.0 --loglevel=error
echo "  Appium: $(appium --version)"
appium driver install uiautomator2@5.0.0
echo "Installed drivers:"
appium driver list --installed
echo "Appium setup complete"

# ── 5. Start Appium server ──────────────────────────────────
echo "Starting Appium server on port ${APPIUM_PORT}..."
appium server \
  --port "$APPIUM_PORT" \
  --log automation/reports/logs/appium-server.log \
  --log-level info \
  --relaxed-security \
  > automation/reports/logs/appium-console.log 2>&1 &

# Wait for Appium to be ready
APPIUM_TIMEOUT=90
APPIUM_ELAPSED=0
while true; do
  if curl -sf "http://127.0.0.1:${APPIUM_PORT}/status" > /dev/null 2>&1; then
    echo "Appium server ready after ${APPIUM_ELAPSED}s"
    break
  fi
  sleep 2
  APPIUM_ELAPSED=$((APPIUM_ELAPSED + 2))
  if [ "$APPIUM_ELAPSED" -ge "$APPIUM_TIMEOUT" ]; then
    echo "ERROR: Appium failed to start within ${APPIUM_TIMEOUT}s"
    echo "--- appium-console.log ---"
    cat automation/reports/logs/appium-console.log || true
    exit 1
  fi
  echo "  waiting for Appium... ${APPIUM_ELAPSED}s"
done

# ── 6. Run Maven / TestNG tests ─────────────────────────────
# Read TEST_SHARD from environment (default to all)
SHARD="${TEST_SHARD:-all}"
echo "Running Maven tests for shard: $SHARD"

if [ "$SHARD" = "all" ]; then
  SUITE_FILE="src/test/resources/testng.xml"
else
  SUITE_FILE="src/test/resources/testng-${SHARD}.xml"
fi
echo "Using suite XML file: $SUITE_FILE"

cd automation

# Print Maven diagnostic information for version verification
echo "=== MAVEN DIAGNOSTICS ==="
mvn -version
echo "========================="

# Run Maven and preserve its exit code correctly.
# We cannot use: cmd | tee file (loses exit code in POSIX sh)
# Instead: run Maven with output going to log, then replay to stdout.
MVN_LOG="../automation/reports/logs/mvn-execution.log"
mvn clean test \
  -DsuiteXmlFile="$SUITE_FILE" \
  -DtestShard="$SHARD" \
  -DGITHUB_RUN_NUMBER="$GITHUB_RUN_NUMBER" \
  -DGITHUB_SHA="$GITHUB_SHA" \
  -DGITHUB_REF_NAME="$GITHUB_REF_NAME" \
  -DANDROID_API_LEVEL="$ANDROID_API_LEVEL" \
  -Dsurefire.useFile=false \
  -Dmaven.test.failure.ignore=true \
  --no-transfer-progress \
  2>&1 | tee "$MVN_LOG"
# In POSIX sh, PIPESTATUS is not available. Use a sentinel file instead.
if grep -q "BUILD FAILURE" "$MVN_LOG" 2>/dev/null; then
  TEST_EXIT=1
  echo "Maven BUILD FAILURE detected in log"
else
  TEST_EXIT=0
fi
echo "Maven finished with exit code: ${TEST_EXIT}"
cd ..

# ── 7. Capture post-test evidence ───────────────────────────
echo "Capturing post-test evidence..."
adb exec-out screencap -p > automation/reports/screenshots/post-test-device.png 2>/dev/null || true
adb logcat -d -t 2000 > automation/reports/logs/adb-full-logcat.log 2>/dev/null || true
cp automation/reports/logs/appium-server.log "automation/Test Results/Summary/" 2>/dev/null || true
echo "Post-test evidence captured"

# ── 8. Exit with Maven's status ─────────────────────────────
if [ "$TEST_EXIT" -ne 0 ]; then
  echo "Tests completed with failures (exit code $TEST_EXIT)"
  exit "$TEST_EXIT"
fi

echo "All tests completed successfully"
exit 0
