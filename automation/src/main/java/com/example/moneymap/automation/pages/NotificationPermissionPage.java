package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class NotificationPermissionPage extends BasePage {

    private final By allowButton = byText("Allow");
    private final By notNowButton = byText("Not Now");
    private final By denyButton = By.xpath("//*[@text=\"Don't allow\" or @text='Deny' or @text='Not Now']");

    public NotificationPermissionPage(AndroidDriver driver) {
        super(driver);
    }

    public void clickAllow() {
        click(allowButton);
    }

    public void clickNotNow() {
        if (isElementDisplayed(notNowButton)) {
            click(notNowButton);
        } else {
            clickDeny();
        }
    }

    /** Denies the notification permission request */
    public void clickDeny() {
        if (isElementDisplayed(denyButton)) {
            click(denyButton);
        }
    }
}
