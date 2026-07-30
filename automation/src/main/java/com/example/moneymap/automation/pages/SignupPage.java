package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * SignupPage - Page Object for the Compose SignupScreen / legacy SignupActivity.
 */
public class SignupPage extends BasePage {

    private final By nameField = By.xpath(
            "//android.widget.EditText[@hint='John Doe' or @text='John Doe' or @index='0']");
    private final By emailField = By.xpath(
            "//android.widget.EditText[contains(@hint,'email') or contains(@text,'email') or @index='1']");
    private final By passwordField = By.xpath(
            "//android.widget.EditText[contains(@hint,'password') or @id='com.example.moneymap:id/password_input']");
    private final By confirmPasswordField = By.id("com.example.moneymap:id/confirm_password_input");
    private final By termsCheckbox = By.id("com.example.moneymap:id/terms_checkbox");
    private final By createAccountButton = byText("Create Account");
    private final By loginLink = byText("Sign In");
    private final By googleButton = byText("Continue with Google");

    public SignupPage(AndroidDriver driver) {
        super(driver);
    }

    public void enterName(String name) {
        try {
            clearAndType(nameField, name);
        } catch (Exception e) {
            clearAndType(By.xpath("//android.widget.EditText[1]"), name);
        }
    }

    public void enterEmail(String email) {
        try {
            clearAndType(emailField, email);
        } catch (Exception e) {
            clearAndType(By.xpath("//android.widget.EditText[2]"), email);
        }
    }

    public void enterPassword(String password) {
        try {
            clearAndType(passwordField, password);
        } catch (Exception e) {
            clearAndType(By.xpath("//android.widget.EditText[3]"), password);
        }
    }

    public void enterConfirmPassword(String password) {
        try {
            clearAndType(confirmPasswordField, password);
        } catch (Exception e) {
            clearAndType(By.xpath("//android.widget.EditText[4]"), password);
        }
    }

    public void checkTerms() {
        try {
            click(termsCheckbox);
        } catch (Exception e) {
            // Compose T&C might be a SpannableString text, not a CheckBox
            try { click(byText("I agree")); } catch (Exception ignored) {}
        }
    }

    public void clickCreateAccount() {
        click(createAccountButton);
    }

    public void clickLoginLink() {
        click(loginLink);
    }

    public void register(String name, String email, String password, String confirmPassword) {
        enterName(name);
        enterEmail(email);
        enterPassword(password);
        enterConfirmPassword(confirmPassword);
        checkTerms();
        clickCreateAccount();
    }

    public boolean isSignupScreenDisplayed() {
        return isTextVisible("Create Account") || isTextVisible("Start your journey");
    }

    public boolean isErrorMessageDisplayed(String error) {
        return isElementDisplayed(byText(error));
    }
}
