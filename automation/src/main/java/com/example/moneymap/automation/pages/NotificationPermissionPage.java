package com.example.moneymap.automation.pages;

import com.example.moneymap.automation.utils.LogUtil;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * NotificationPermissionPage — handles both the Android system permission dialog
 * and the in-app notification opt-in screen.
 */
public class NotificationPermissionPage extends BasePage {

    // ── Locators — in-app screen ───────────────────────────────────────────────

    private final By inAppAllowButton = By.xpath(
        "//*[@text='Allow' or @text='ALLOW' or @text='Enable Notifications' or " +
        "@text='Enable' or @resource-id='com.example.moneymap:id/btn_allow_notifications']");
    private final By inAppNotNowButton = By.xpath(
        "//*[@text='Not Now' or @text='NOT NOW' or @text='Skip' or @text='Later' or @text='Maybe Later' " +
        "or @resource-id='com.example.moneymap:id/btn_skip_notifications']");

    // ── Locators — system dialog ───────────────────────────────────────────────

    private final By systemAllowButton = By.xpath(
        "//*[@resource-id='com.android.permissioncontroller:id/permission_allow_button' " +
        "or @resource-id='com.android.packageinstaller:id/permission_allow_button' " +
        "or (@text='Allow' and contains(@resource-id,'permission'))]");
    private final By systemDenyButton = By.xpath(
        "//*[@resource-id='com.android.permissioncontroller:id/permission_deny_button' " +
        "or @resource-id='com.android.packageinstaller:id/permission_deny_button' " +
        "or (@text=\"Don't allow\" and contains(@resource-id,'permission'))]");

    // ── Screen detection ───────────────────────────────────────────────────────

    private final By permissionScreenIndicator = By.xpath(
        "//*[contains(@text,'notification') or contains(@text,'Notification') or " +
        "contains(@resource-id,'permission') or @text='Allow' or @text='Not Now']");

    public NotificationPermissionPage(AndroidDriver driver) {
        super(driver);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    /**
     * Taps the Allow/Enable button. Tries in-app button first, falls back to system dialog.
     */
    public void clickAllow() {
        if (isElementDisplayed(inAppAllowButton)) {
            click(inAppAllowButton);
            LogUtil.log("Tapped in-app notification Allow button.");
        } else if (isElementDisplayed(systemAllowButton)) {
            click(systemAllowButton);
            LogUtil.log("Tapped system permission Allow button.");
        } else {
            LogUtil.logWarning("Allow button not found on notification permission screen.");
        }
    }

    /**
     * Taps the Not Now/Skip/Deny button. Tries in-app button first, falls back to system dialog.
     */
    public void clickNotNow() {
        if (isElementDisplayed(inAppNotNowButton)) {
            click(inAppNotNowButton);
            LogUtil.log("Tapped in-app notification Not Now button.");
        } else if (isElementDisplayed(systemDenyButton)) {
            click(systemDenyButton);
            LogUtil.log("Tapped system permission Deny button.");
        } else {
            LogUtil.logWarning("Not Now/Deny button not found on notification permission screen.");
        }
    }

    // ── Assertions ────────────────────────────────────────────────────────────

    public boolean isNotificationPermissionScreenDisplayed() {
        return isElementDisplayed(inAppAllowButton) ||
               isElementDisplayed(inAppNotNowButton) ||
               isElementDisplayed(systemAllowButton) ||
               isElementDisplayed(permissionScreenIndicator);
    }
}
