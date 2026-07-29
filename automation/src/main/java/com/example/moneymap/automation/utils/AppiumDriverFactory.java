package com.example.moneymap.automation.utils;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.net.URI;
import java.net.URL;
import java.time.Duration;

/**
 * Thread-safe Appium AndroidDriver factory using ThreadLocal storage.
 * Each thread (parallel test worker) gets its own independent driver instance.
 */
public class AppiumDriverFactory {

    // ThreadLocal ensures each parallel thread has its own driver instance
    private static final ThreadLocal<AndroidDriver> driverThreadLocal = new ThreadLocal<>();

    /**
     * Returns the AndroidDriver for the current thread. Creates one if it doesn't exist.
     */
    public static AndroidDriver getDriver() {
        if (driverThreadLocal.get() == null) {
            try {
                // Resolve config path — supports both local and CI execution contexts
                String configPath = resolveConfigPath();
                JSONObject config = new JSONObject(new JSONTokener(new FileReader(configPath)));

                UiAutomator2Options options = new UiAutomator2Options();
                options.setPlatformName(config.optString("platformName", "Android"));
                options.setAutomationName(config.optString("automationName", "UiAutomator2"));
                options.setDeviceName(config.optString("deviceName", "Android Emulator"));

                // Resolve APK path with multiple fallback strategies
                options.setApp(resolveApkPath(config.optString("app")));

                options.setAppPackage(config.optString("appPackage", "com.example.moneymap"));
                options.setAppActivity(config.optString("appActivity", "com.example.moneymap.MainActivity"));
                options.setCapability("appWaitActivity", config.optString("appWaitActivity", "com.example.moneymap.*"));
                options.setCapability("appWaitDuration", config.optInt("appWaitDuration", 30000));
                options.setNoReset(config.optBoolean("noReset", false));
                options.setFullReset(config.optBoolean("fullReset", false));
                options.setCapability("autoGrantPermissions", config.optBoolean("autoGrantPermissions", true));
                options.setCapability("newCommandTimeout", config.optInt("newCommandTimeout", 300));
                options.setCapability("systemPort", config.optInt("systemPort", 8200));
                options.setAdbExecTimeout(Duration.ofMillis(config.optInt("adbExecTimeout", 120000)));
                options.setCapability("uiautomator2ServerLaunchTimeout", config.optInt("uiautomator2ServerLaunchTimeout", 60000));
                options.setCapability("uiautomator2ServerInstallTimeout", config.optInt("uiautomator2ServerInstallTimeout", 60000));

                URL serverUrl = new URI("http://127.0.0.1:4723").toURL();
                AndroidDriver driver = new AndroidDriver(serverUrl, options);
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
                driverThreadLocal.set(driver);

                LogUtil.log("AndroidDriver session created for thread: " + Thread.currentThread().getName());
            } catch (Exception e) {
                LogUtil.logError("Failed to initialize AndroidDriver: " + e.getMessage(), e);
                throw new RuntimeException("AndroidDriver init failed: " + e.getMessage(), e);
            }
        }
        return driverThreadLocal.get();
    }

    /**
     * Quits and removes the driver for the current thread.
     */
    public static void quitDriver() {
        AndroidDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
                LogUtil.log("AndroidDriver session closed for thread: " + Thread.currentThread().getName());
            } catch (Exception e) {
                System.err.println("Error quitting driver: " + e.getMessage());
            } finally {
                driverThreadLocal.remove();
            }
        }
    }

    /**
     * Returns true if a live driver session exists on the current thread.
     */
    public static boolean isDriverActive() {
        return driverThreadLocal.get() != null;
    }

    private static String resolveConfigPath() {
        String[] candidates = {
            "automation/config/appium-config.json",
            "config/appium-config.json",
            "../automation/config/appium-config.json"
        };
        for (String path : candidates) {
            if (new java.io.File(path).exists()) return path;
        }
        throw new RuntimeException("appium-config.json not found. Searched: " + String.join(", ", candidates));
    }

    private static String resolveApkPath(String configuredPath) {
        // 1. Try the configured path as-is
        if (configuredPath != null && new java.io.File(configuredPath).exists()) {
            return new java.io.File(configuredPath).getAbsolutePath();
        }
        // 2. Try common relative paths from automation/ and root
        String[] fallbacks = {
            "../app/build/outputs/apk/debug/app-debug.apk",
            "app/build/outputs/apk/debug/app-debug.apk",
            "../../app/build/outputs/apk/debug/app-debug.apk"
        };
        for (String path : fallbacks) {
            java.io.File f = new java.io.File(path);
            if (f.exists()) {
                LogUtil.log("Resolved APK path: " + f.getAbsolutePath());
                return f.getAbsolutePath();
            }
        }
        // 3. Return configured path and let Appium report the error clearly
        LogUtil.log("WARNING: APK not found at any expected location. Using: " + configuredPath);
        return configuredPath;
    }
}
