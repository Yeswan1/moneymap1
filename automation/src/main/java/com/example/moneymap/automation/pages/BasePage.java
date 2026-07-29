package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class BasePage {
    protected AndroidDriver driver;
    protected WebDriverWait wait;

    public BasePage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    protected WebElement waitForElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        waitForElement(locator).click();
    }

    protected void type(By locator, String text) {
        WebElement element = waitForElement(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(By locator) {
        return waitForElement(locator).getText();
    }

    protected boolean isElementDisplayed(By locator) {
        try {
            return waitForElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /** Clears the text content of an input field */
    protected void clearField(By locator) {
        try {
            WebElement element = waitForElement(locator);
            element.clear();
        } catch (Exception e) {
            // Best-effort clear
        }
    }

    /** Compose/XPath helper: find element by text or content-desc */
    public By byText(String text) {
        return By.xpath("//*[contains(@text, '" + text + "') or contains(@content-desc, '" + text + "')]");
    }

    protected void clickByText(String text) {
        click(byText(text));
    }

    /**
     * Attempts to find an element by resource-id (short form without package prefix).
     */
    protected By byId(String resourceId) {
        return By.xpath("//*[@resource-id='com.example.moneymap:id/" + resourceId + "']");
    }

    /**
     * Checks if a specific text is visible anywhere on screen (fast, no wait).
     */
    protected boolean isTextOnScreen(String text) {
        try {
            return !driver.findElements(byText(text)).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
