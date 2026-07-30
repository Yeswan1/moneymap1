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
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
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

    protected boolean isTextVisible(String text) {
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

    protected void pressBack() {
        driver.navigate().back();
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private String escapeXpath(String value) {
        return value.replace("'", "\\'");
    }
}
