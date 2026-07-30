package com.example.moneymap.automation.pages;

import com.example.moneymap.automation.utils.LogUtil;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * BasePage - Foundation class for all Page Objects.
 * Provides fluent element interaction helpers and retry logic.
 */
public class BasePage {

    protected AndroidDriver driver;
    protected WebDriverWait wait;
    protected WebDriverWait shortWait;
    private static final int DEFAULT_TIMEOUT = 15;
    private static final int SHORT_TIMEOUT = 5;
    private static final int MAX_RETRIES = 3;

    public BasePage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(SHORT_TIMEOUT));
    }

    // ─── Core element interactions ────────────────────────────────────────────

    protected WebElement waitForElement(By locator) {
        return waitForElement(locator, Duration.ofSeconds(DEFAULT_TIMEOUT));
    }

    protected WebElement waitForElement(By locator, Duration timeout) {
        try {
            WebDriverWait customWait = new WebDriverWait(driver, timeout);
            return customWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (Exception e) {
            String locStr = locator.toString();
            LogUtil.logError("Timeout waiting for visibility of: " + locStr, e);
            captureDiagnostics("Timeout_Visible_" + locStr.replaceAll("[^a-zA-Z0-9]", "_"));
            throw new AssertionError("Timeout waiting for visibility of element: " + locStr, e);
        }
    }

    protected WebElement waitForClickable(By locator) {
        return waitForClickable(locator, Duration.ofSeconds(DEFAULT_TIMEOUT));
    }

    protected WebElement waitForClickable(By locator, Duration timeout) {
        try {
            WebDriverWait customWait = new WebDriverWait(driver, timeout);
            return customWait.until(ExpectedConditions.elementToBeClickable(locator));
        } catch (Exception e) {
            String locStr = locator.toString();
            LogUtil.logError("Timeout waiting for clickability of: " + locStr, e);
            captureDiagnostics("Timeout_Clickable_" + locStr.replaceAll("[^a-zA-Z0-9]", "_"));
            throw new AssertionError("Timeout waiting for clickability of element: " + locStr, e);
        }
    }

    protected void click(By locator) {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                waitForClickable(locator).click();
                return;
            } catch (StaleElementReferenceException e) {
                attempts++;
                if (attempts >= MAX_RETRIES) throw e;
            }
        }
    }

    protected void type(By locator, String text) {
        WebElement element = waitForElement(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected void clearAndType(By locator, String text) {
        WebElement element = waitForClickable(locator);
        element.click();
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(By locator) {
        return waitForElement(locator).getText();
    }

    protected boolean isElementDisplayed(By locator) {
        try {
            return shortWait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected List<WebElement> findElements(By locator) {
        return driver.findElements(locator);
    }

    // ─── Compose/Accessibility-friendly locators ──────────────────────────────

    /** Match by @text or @content-desc */
    protected By byText(String text) {
        return By.xpath("//*[contains(@text, '" + escapeXpath(text) + "') " +
                "or contains(@content-desc, '" + escapeXpath(text) + "')]");
    }

    /** Match by exact @text */
    protected By byExactText(String text) {
        return By.xpath("//*[@text='" + escapeXpath(text) + "']");
    }

    /** Match by resource-id */
    protected By byId(String resourceId) {
        return By.id("com.example.moneymap:id/" + resourceId);
    }

    protected void clickByText(String text) {
        click(byText(text));
    }

    public boolean isTextVisible(String text) {
        return isElementDisplayed(byText(text));
    }

    // ─── Scroll helpers ───────────────────────────────────────────────────────

    protected void scrollToText(String text) {
        try {
            driver.findElement(AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true))" +
                            ".scrollIntoView(new UiSelector().textContains(\"" + text + "\"))"));
        } catch (Exception e) {
            LogUtil.log("Could not scroll to text: " + text);
        }
    }

    protected void swipeUp() {
        try {
            driver.findElement(AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true)).scrollBackward()"));
        } catch (Exception ignored) {}
    }

    // ─── Wait helpers ─────────────────────────────────────────────────────────

    protected void waitSeconds(int seconds) {
        try { Thread.sleep(seconds * 1000L); } catch (InterruptedException ignored) {}
    }

    protected boolean waitForText(String text, int timeoutSecs) {
        try {
            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSecs));
            customWait.until(ExpectedConditions.visibilityOfElementLocated(byText(text)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ─── Navigation helpers ───────────────────────────────────────────────────

    public void pressBack() {
        driver.navigate().back();
    }

    // ─── Startup State machine ────────────────────────────────────────────────

    public void ensureAppReady() {
        LogUtil.log("--- Starting ensureAppReady() sync check ---");
        long start = System.currentTimeMillis();
        long timeoutMs = 25000; // 25 seconds max to reach Login screen
        boolean ready = false;

        while (System.currentTimeMillis() - start < timeoutMs) {
            // 1. Check for system permission dialogs and dismiss
            dismissPermissionDialogIfPresent();

            // 2. Check for update/alert dialogs and dismiss
            dismissUpdateDialogIfPresent();

            // 3. Check if we are on Onboarding screen
            if (isOnboardingScreenVisible()) {
                LogUtil.log("Onboarding screen detected. Attempting to click 'Skip'.");
                clickSkipOnboarding();
            }

            // 4. Check if we are on Login screen (both email and password fields exist)
            if (isLoginScreenFullyLoaded()) {
                ready = true;
                break;
            }

            waitSeconds(1);
        }

        if (!ready) {
            LogUtil.logError("CRITICAL: App not ready (Login screen unreachable). Capturing diagnostics...", null);
            captureDiagnostics("ensureAppReady_FAILED");
            throw new AssertionError("Application failed to reach login screen within timeout.");
        }

        LogUtil.log("--- App is ready. Login screen displayed and verified. ---");
    }

    private void dismissPermissionDialogIfPresent() {
        try {
            By allowButton = By.xpath("//*[@text='Allow' or @text='WHILE USING THE APP' or @text='While using the app' or contains(@resource-id, 'permission_allow_button')]");
            List<WebElement> elements = driver.findElements(allowButton);
            if (!elements.isEmpty() && elements.get(0).isDisplayed()) {
                elements.get(0).click();
                LogUtil.log("Dismissed system permission dialog.");
            }
        } catch (Exception ignored) {}
    }

    private void dismissUpdateDialogIfPresent() {
        try {
            By cancelBtn = By.xpath("//*[@text='Cancel' or @text='Not Now' or @text='Skip' or contains(@resource-id, 'button2')]");
            List<WebElement> elements = driver.findElements(cancelBtn);
            if (!elements.isEmpty() && elements.get(0).isDisplayed()) {
                elements.get(0).click();
                LogUtil.log("Dismissed update/alert dialog.");
            }
        } catch (Exception ignored) {}
    }

    private boolean isOnboardingScreenVisible() {
        try {
            return isElementPresent(By.xpath("//*[@text='Skip']")) ||
                   isElementPresent(By.xpath("//*[@text='Track with Ease' or @text='Smart Budgeting' or @text='Visual Reports']"));
        } catch (Exception e) {
            return false;
        }
    }

    private void clickSkipOnboarding() {
        try {
            By skipBtn = By.xpath("//*[@text='Skip']");
            WebElement btn = driver.findElement(skipBtn);
            btn.click();
            LogUtil.log("Clicked Onboarding 'Skip' button.");

            // Wait for onboarding to disappear
            WebDriverWait shortW = new WebDriverWait(driver, Duration.ofSeconds(5));
            shortW.until(ExpectedConditions.invisibilityOfElementLocated(skipBtn));
            LogUtil.log("Onboarding Skip completed: skip button disappeared.");
        } catch (Exception e) {
            LogUtil.log("Failed to click Skip or wait for disappearance: " + e.getMessage());
        }
    }

    private boolean isLoginScreenFullyLoaded() {
        try {
            By email = By.xpath("//android.widget.EditText[contains(@text, 'Email Address') or @hint='Email Address']");
            By pwd = By.xpath("//android.widget.EditText[contains(@text, 'Password') or @hint='Password']");
            return isElementPresent(email) && isElementPresent(pwd);
        } catch (Exception e) {
            return false;
        }
    }

    public void captureDiagnostics(String prefix) {
        if (driver == null) return;
        try {
            String activity = driver.currentActivity();
            String pkg = driver.getCurrentPackage();
            String pageSource = driver.getPageSource();
            LogUtil.log("--- Diagnostic Capture for " + prefix + " ---");
            LogUtil.log("Current Package: " + pkg);
            LogUtil.log("Current Activity: " + activity);

            String logDir = "reports/logs/";
            if (new java.io.File("automation").exists()) {
                logDir = "automation/reports/logs/";
            }
            java.io.File dir = new java.io.File(logDir);
            if (!dir.exists()) dir.mkdirs();

            java.io.File sourceFile = new java.io.File(dir, prefix + "_pagesource.xml");
            try (java.io.FileWriter fw = new java.io.FileWriter(sourceFile)) {
                fw.write(pageSource);
                LogUtil.log("Page source saved to: " + sourceFile.getAbsolutePath());
            }

            // Capture screenshot
            com.example.moneymap.automation.utils.ScreenshotUtil.captureScreenshot(driver, prefix);

            // Capture logs
            LogUtil.captureDeviceLogs(driver, prefix);
            LogUtil.captureAppiumLogs(driver, prefix);
        } catch (Exception e) {
            LogUtil.logError("Failed to capture diagnostics for " + prefix, e);
        }
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private String escapeXpath(String value) {
        return value.replace("'", "\\'");
    }
}
