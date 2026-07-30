#!/bin/sh
set -e

# Create report directories
mkdir -p automation/reports/screenshots
mkdir -p automation/reports/logs
mkdir -p "automation/Test Results/Excel"
mkdir -p "automation/Test Results/HTML"
mkdir -p "automation/Test Results/JSON"
mkdir -p "automation/Test Results/Summary"

# Wait for emulator to boot
echo "Waiting for emulator to boot..."
adb wait-for-device

BOOT_TIMEOUT=180
ELAPSED=0
BOOTED=""

while [ "$BOOTED" != "1" ]; do
  BOOTED=$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)
  if [ "$BOOTED" = "1" ]; then
    break
  fi
  sleep 3
  ELAPSED=$((ELAPSED + 3))
  if [ "$ELAPSED" -ge "$BOOT_TIMEOUT" ]; then
    echo "Emulator boot timeout"
    exit 1
  fi
  echo "Booting... ${ELAPSED}s"
done

echo "Emulator booted after ${ELAPSED}s"
adb shell input keyevent 82
sleep 2
adb devices

# Install APK
echo "Installing APK..."
adb install -r "$APK_PATH"
adb shell pm list packages | grep moneymap || true
echo "APK installed"

# Install Appium 3 (requires Node >= 20.19.0 and npm >= 10)
echo "Installing Appium 3..."
node --version
npm --version

# Appium 3 requires npm >= 10 — upgrade in-place
npm install -g npm@10.9.2 --loglevel=error
echo "npm upgraded: $(npm --version)"

# Pin Appium server to 3.0.0 (stable Appium 3 release)
npm install -g appium@3.0.0 --loglevel=error
echo "Appium version: $(appium --version)"

# Install uiautomator2 v5.x (requires Appium 3)
# Using 'appium driver install' which pulls from npm registry
appium driver install uiautomator2@5.0.0
echo "Installed drivers:"
appium driver list --installed
echo "Appium and uiautomator2 installed"

# Start Appium server
echo "Starting Appium server..."
appium --port "$APPIUM_PORT" \
  --log automation/reports/logs/appium-server.log \
  --log-level info --relaxed-security \
  > automation/reports/logs/appium-console.log 2>&1 &
APPIUM_PID=$!

# Wait for Appium to be ready
echo "Waiting for Appium..."
APPIUM_TIMEOUT=60
APPIUM_ELAPSED=0
APPIUM_READY=""

while [ -z "$APPIUM_READY" ]; do
  if curl -sf "http://127.0.0.1:${APPIUM_PORT}/status" > /dev/null 2>&1; then
    APPIUM_READY="yes"
    echo "Appium ready after ${APPIUM_ELAPSED}s"
    break
  fi
  sleep 2
  APPIUM_ELAPSED=$((APPIUM_ELAPSED + 2))
  if [ "$APPIUM_ELAPSED" -ge "$APPIUM_TIMEOUT" ]; then
    echo "Appium failed to start"
    cat automation/reports/logs/appium-console.log || true
    exit 1
  fi
  echo "Waiting for Appium... ${APPIUM_ELAPSED}s"
done

# Run Maven tests
echo "Executing 510+ E2E Tests..."
cd automation
mvn clean test \
  -DGITHUB_RUN_NUMBER="$GITHUB_RUN_NUMBER" \
  -DGITHUB_SHA="$GITHUB_SHA" \
  -DGITHUB_REF_NAME="$GITHUB_REF_NAME" \
  -DANDROID_API_LEVEL="$ANDROID_API_LEVEL" \
  -Dsurefire.useFile=false \
  -Dmaven.test.failure.ignore=true \
  --no-transfer-progress \
  2>&1 | tee "../automation/reports/logs/mvn-execution.log" || TEST_EXIT=$?
echo "Maven exit code ${TEST_EXIT:-0}"
cd ..

# Capture post-test evidence
echo "Capturing post-test evidence..."
adb exec-out screencap -p > automation/reports/screenshots/post-test-device.png 2>/dev/null || true
adb logcat -d -t 2000 > automation/reports/logs/adb-full-logcat.log 2>/dev/null || true
cp automation/reports/logs/appium-server.log "automation/Test Results/Summary/" 2>/dev/null || true
echo "Post-test evidence captured"

# Exit with test status
if [ -n "$TEST_EXIT" ] && [ "$TEST_EXIT" -ne 0 ]; then
  echo "Tests failed with exit code $TEST_EXIT"
  exit "$TEST_EXIT"
fi

echo "All tests completed successfully"
exit 0
