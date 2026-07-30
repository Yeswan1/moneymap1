package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * NotificationPermissionPage - Page Object for NotificationPermissionScreen.
 */
public class NotificationPermissionPage extends BasePage {

    private final By allowButton = By.xpath(
            "//*[@resource-id='com.example.moneymap:id/btn_allow' " +
            "or @text='Allow' or @content-desc='Allow']");
    private final By notNowButton = By.xpath(
            "//*[@resource-id='com.example.moneymap:id/btn_not_now' " +
            "or @text='Not Now' or @content-desc='Not Now']");
    // Android system permission dialog
    private final By systemAllowButton = By.xpath(
            "//*[@text='Allow' or @resource-id='com.android.permissioncontroller:id/permission_allow_button']");

    public NotificationPermissionPage(AndroidDriver driver) {
        super(driver);
    }

    public void clickAllow() {
        // First handle app-level permission screen
        try {
            click(allowButton);
        } catch (Exception ignored) {}
        // Then handle Android system dialog if it appears
        waitSeconds(1);
        try {
            click(systemAllowButton);
        } catch (Exception ignored) {}
    }

    public void clickNotNow() {
        try {
            click(notNowButton);
        } catch (Exception e) {
            clickByText("Not Now");
        }
    }

    public boolean isNotificationPermissionScreenDisplayed() {
        return isTextVisible("Stay Notified") || isTextVisible("notification");
    }
}
