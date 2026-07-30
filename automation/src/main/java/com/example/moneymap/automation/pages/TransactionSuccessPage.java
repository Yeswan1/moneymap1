package com.example.moneymap.automation.pages;

import com.example.moneymap.automation.utils.LogUtil;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * TransactionSuccessPage — Page Object for the transaction success confirmation screen.
 *
 * Uses shortWait so that isSuccessDisplayed() returns quickly (false) when called
 * in validation tests that assert the screen is NOT shown.
 */
public class TransactionSuccessPage extends BasePage {

    // ── Locators ──────────────────────────────────────────────────────────────

    private final By successIndicator = By.xpath(
        "//*[contains(@text,'Transaction Added') or contains(@text,'Transaction Saved') " +
        "or contains(@text,'Success') or contains(@text,'success') " +
        "or contains(@text,'saved') or contains(@text,'added') " +
        "or contains(@content-desc,'success') or contains(@content-desc,'Success') " +
        "or contains(@resource-id,'success')]");

    private final By backToHomeButton = By.xpath(
        "//*[@text='Back to Home' or @text='Done' or @text='DONE' or @text='OK' " +
        "or @text='Back' or @text='HOME' or @text='Go to Dashboard' " +
        "or @resource-id='com.example.moneymap:id/btn_home' " +
        "or @resource-id='com.example.moneymap:id/btn_back_home']");

    public TransactionSuccessPage(AndroidDriver driver) {
        super(driver);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    public void clickBackToHome() {
        try {
            click(backToHomeButton);
        } catch (Exception e) {
            LogUtil.logWarning("Back to Home button not found, pressing hardware back.");
            pressBack();
        }
    }

    // ── Assertions ────────────────────────────────────────────────────────────

    /**
     * Uses shortWait (5 s) so callers that assert this is NOT displayed do not
     * block for the full default timeout.
     */
    public boolean isSuccessDisplayed() {
        return isElementDisplayed(successIndicator);
    }
}
