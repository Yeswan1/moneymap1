package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * TransactionSuccessPage - Page Object for the TransactionSuccessScreen.
 */
public class TransactionSuccessPage extends BasePage {

    private final By backToHomeButton = byText("Back to Home");
    private final By addAnotherButton = byText("Add Another");
    private final By successTitle = byText("Transaction Saved!");
    private final By successDesc = byText("Your expense has been successfully recorded");

    public TransactionSuccessPage(AndroidDriver driver) {
        super(driver);
    }

    public boolean isSuccessDisplayed() {
        return isTextVisible("Transaction Saved!") || isTextVisible("successfully recorded")
                || isTextVisible("success");
    }

    public void clickBackToHome() {
        click(backToHomeButton);
    }

    public void clickAddAnother() {
        click(addAnotherButton);
    }
}
