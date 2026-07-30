package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * SignupPage - Page Object for the Compose SignupScreen / legacy SignupActivity.
 */
public class SignupPage extends BasePage {

    private final By nameField = By.xpath(
            "//android.widget.EditText[@text='Full Name' or @hint='Full Name' or contains(@text, 'Name')]");
    private final By emailField = By.xpath(
            "//android.widget.EditText[@text='Email Address' or @hint='Email Address' or contains(@text, 'Email')]");
    private final By passwordField = By.xpath(
            "//android.widget.EditText[@text='Password' or @hint='Password' or contains(@text, 'Password')]");
    private final By createAccountButton = By.xpath(
            "//*[@text='Create Account' or @content-desc='Create Account']");
    private final By loginLink = By.xpath(
            "//*[@text='Login' or @content-desc='Login']");

    public SignupPage(AndroidDriver driver) {
        super(driver);
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

    public void clickCreateAccount() {
        click(createAccountButton);
    }

    public void clickLoginLink() {
        click(loginLink);
    }

    public void enterConfirmPassword(String password) {
        // No-op: Compose SignupScreen does not have a confirm password field
    }

    public void checkTerms() {
        // No-op: Compose SignupScreen does not have a terms checkbox
    }

    public void register(String name, String email, String password) {
        enterName(name);
        enterEmail(email);
        enterPassword(password);
        clickCreateAccount();
    }

    public void register(String name, String email, String password, String confirmPassword) {
        register(name, email, password);
    }

    public boolean isSignupScreenDisplayed() {
        return isElementPresent(nameField) && isElementPresent(emailField);
    }

    public boolean isErrorMessageDisplayed(String error) {
        return isElementDisplayed(byText(error));
    }
}
