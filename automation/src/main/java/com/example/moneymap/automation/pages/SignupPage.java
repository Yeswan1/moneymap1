package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * SignupPage — Page Object for the MoneyMap registration screen.
 */
public class SignupPage extends BasePage {

    // ── Locators ──────────────────────────────────────────────────────────────

    private final By nameField = By.xpath(
        "//android.widget.EditText[contains(@hint,'Name') or contains(@hint,'name') or contains(@text,'Full Name')]");
    private final By emailField = By.xpath(
        "//android.widget.EditText[contains(@hint,'Email') or contains(@hint,'email') or contains(@text,'Email')]");
    private final By passwordField = By.xpath(
        "(//android.widget.EditText[contains(@hint,'Password') or contains(@hint,'password')])[1]");
    private final By confirmPasswordField = By.xpath(
        "(//android.widget.EditText[contains(@hint,'Password') or contains(@hint,'password')])[2]");
    private final By createAccountButton = By.xpath(
        "//*[@text='Create Account' or @text='Sign Up' or @text='Register' or @text='CREATE ACCOUNT']");
    private final By signInLink = By.xpath(
        "//*[contains(@text,'Sign In') or contains(@text,'Log In') or contains(@text,'Already have')]");
    private final By errorText = By.xpath(
        "//*[contains(@text,'match') or contains(@text,'Match') or contains(@text,'error') or " +
        "contains(@text,'Error') or contains(@text,'required') or contains(@text,'Required') or " +
        "contains(@text,'exists') or contains(@text,'invalid') or contains(@text,'Invalid')]");

    public SignupPage(AndroidDriver driver) {
        super(driver);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    public void register(String name, String email, String password, String confirmPassword) {
        enterName(name);
        enterEmail(email);
        enterPassword(password);
        enterConfirmPassword(confirmPassword);
        clickCreateAccount();
    }

    public void enterName(String name) {
        clearAndType(nameField, name);
    }

    public void enterEmail(String email) {
        clearAndType(emailField, email);
    }

    public void enterPassword(String password) {
        clearAndType(passwordField, password);
    }

    public void enterConfirmPassword(String confirmPassword) {
        clearAndType(confirmPasswordField, confirmPassword);
    }

    public void clickCreateAccount() {
        click(createAccountButton);
    }

    public void clickLoginLink() {
        click(signInLink);
    }

    // ── Assertions ────────────────────────────────────────────────────────────

    public boolean isSignupScreenDisplayed() {
        return isElementDisplayed(createAccountButton);
    }

    public boolean isErrorMessageDisplayed(String fragment) {
        if (fragment == null || fragment.isEmpty()) {
            return isElementDisplayed(errorText);
        }
        By fragmentLocator = By.xpath(
            "//*[contains(translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" +
            fragment.toLowerCase() + "')]");
        return isElementDisplayed(fragmentLocator);
    }
}
