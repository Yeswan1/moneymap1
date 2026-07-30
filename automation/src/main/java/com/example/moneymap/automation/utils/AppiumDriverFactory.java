package com.example.moneymap.automation.utils;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.net.URL;
import java.time.Duration;

/**
 * AppiumDriverFactory - Thread-safe singleton factory for AndroidDriver.
 * Loads configuration from appium-config.json and handles path resolution.
 */
public class AppiumDriverFactory {

    private static AndroidDriver driver;
    private static final String DEFAULT_APPIUM_URL = "http://127.0.0.1:4723";

    public static synchronized AndroidDriver getDriver() {
        if (driver == null) {
            try {
                JSONObject config = loadConfig();
                UiAutomator2Options options = buildOptions(config);
                String appiumUrl = config.optString("appiumUrl", DEFAULT_APPIUM_URL);
                URL serverUrl = new URI(appiumUrl).toURL();
                driver = new AndroidDriver(serverUrl, options);
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
                LogUtil.log("Appium Driver initialized successfully. Session: " + driver.getSessionId());
            } catch (Exception e) {
                LogUtil.logError("Failed to initialize AndroidDriver", e);
                throw new RuntimeException("Appium driver initialization failed: " + e.getMessage(), e);
            }
        }
        return driver;
    }

    public static synchronized void quitDriver() {
        if (driver != null) {
            try {
                driver.quit();
                LogUtil.log("Appium Driver session closed.");
            } catch (Exception e) {
                LogUtil.logError("Error quitting driver", e);
            } finally {
                driver = null;
            }
        }
    }

    public static synchronized boolean isDriverAlive() {
        if (driver == null) return false;
        try {
            driver.getSessionId();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ─── Configuration ────────────────────────────────────────────────────────

    private static JSONObject loadConfig() throws Exception {
        // Search for config file in multiple locations
        String[] searchPaths = {
            "automation/config/appium-config.json",
            "config/appium-config.json",
            "../automation/config/appium-config.json"
        };
        for (String path : searchPaths) {
            File f = new File(path);
            if (f.exists()) {
                LogUtil.log("Loading Appium config from: " + f.getAbsolutePath());
                return new JSONObject(new JSONTokener(new FileReader(f)));
            }
        }
        LogUtil.log("Config file not found; using defaults.");
        return new JSONObject();
    }

    private static UiAutomator2Options buildOptions(JSONObject config) {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName(config.optString("platformName", "Android"));
        options.setAutomationName(config.optString("automationName", "UiAutomator2"));
        options.setDeviceName(config.optString("deviceName", "Android Emulator"));

        // Set UDID so Appium targets the correct physical device (required for wireless ADB)
        String udid = config.optString("udid", "");
        if (!udid.isEmpty()) {
            options.setUdid(udid);
            LogUtil.log("Using device UDID: " + udid);
        }

        options.setAppPackage(config.optString("appPackage", "com.example.moneymap"));
        options.setAppActivity(config.optString("appActivity", "com.example.moneymap.MainActivity"));
        options.setNoReset(config.optBoolean("noReset", false));
        options.setFullReset(config.optBoolean("fullReset", false));
        options.setCapability("autoGrantPermissions", config.optBoolean("autoGrantPermissions", true));
        options.setCapability("newCommandTimeout", config.optInt("newCommandTimeout", 300));
        options.setCapability("systemPort", config.optInt("systemPort", 8200));
        options.setAdbExecTimeout(Duration.ofMillis(config.optInt("adbExecTimeout", 120000)));

        // Resolve APK path
        String appPath = resolveApkPath(config.optString("app", ""));
        if (!appPath.isEmpty()) {
            options.setApp(appPath);
            LogUtil.log("APK path resolved: " + appPath);
        }

        return options;
    }

    private static String resolveApkPath(String configuredPath) {
        if (!configuredPath.isEmpty() && new File(configuredPath).exists()) {
            return configuredPath;
        }
        // Try relative paths
        String[] candidates = {
            "../app/build/outputs/apk/debug/app-debug.apk",
            "app/build/outputs/apk/debug/app-debug.apk",
            "../../app/build/outputs/apk/debug/app-debug.apk"
        };
        for (String candidate : candidates) {
            File f = new File(candidate);
            if (f.exists()) {
                return f.getAbsolutePath();
            }
        }
        LogUtil.log("WARNING: APK file not found. Appium will attempt to use installed app.");
        return "";
    }
}
