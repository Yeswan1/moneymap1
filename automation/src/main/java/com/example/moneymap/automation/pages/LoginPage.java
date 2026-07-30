package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * LoginPage - Page Object for the Compose LoginScreen.
 * Handles both the Compose-based login and the legacy LoginActivity.
 *
 * Compose elements are located via accessibility text/content-desc.
 */
public class LoginPage extends BasePage {

    // ─── Locators ─────────────────────────────────────────────────────────────
    // Compose fields are located via unique text labels. No index-based XPath.
    private final By emailField = By.xpath(
            "//android.widget.EditText[@text='Email Address' or @hint='Email Address' or contains(@text, 'Email')]");

    private final By passwordField = By.xpath(
            "//android.widget.EditText[@text='Password' or @hint='Password' or contains(@text, 'Password')]");

    private final By loginButton = By.xpath(
            "//*[@text='Login' or @content-desc='Login']");

    private final By signUpLink = By.xpath(
            "//*[@text='Sign Up' or @content-desc='Sign Up']");

    private final By forgotPasswordLink = By.xpath(
            "//*[@text='Forgot Password?' or @text='Forgot Password' or contains(@text, 'Forgot')]");

    private final By googleButton = By.xpath(
            "//*[@text='Continue with Google' or contains(@text, 'Google')]");

    private final By rememberMeCheckbox = By.xpath(
            "//*[contains(@resource-id, 'remember_me_checkbox') or @text='Remember me']");

    private final By passwordToggle = By.xpath(
            "//*[@content-desc='Show password' or @content-desc='Hide password']");

    private final By welcomeTitle = By.xpath(
            "//*[@text='Welcome Back' or @text='Welcome Back!']");

    public LoginPage(AndroidDriver driver) {
        super(driver);
        ensureAppReady();
    }

    // ─── Actions ──────────────────────────────────────────────────────────────

    public void enterEmail(String email) {
        clearAndType(emailField, email);
    }

    public void enterPassword(String password) {
        clearAndType(passwordField, password);
    }

    public void clickLogin() {
        click(loginButton);
    }

    public void clickSignUp() {
        click(signUpLink);
    }

    public void clickForgotPassword() {
        click(forgotPasswordLink);
    }

    public void clickContinueWithGoogle() {
        click(googleButton);
    }

    public void togglePasswordVisibility() {
        click(passwordToggle);
    }

    public void toggleRememberMe() {
        click(rememberMeCheckbox);
    }

    public void login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickLogin();
    }

    // ─── Assertions ───────────────────────────────────────────────────────────

    public boolean isLoginScreenDisplayed() {
        return isElementPresent(emailField) && isElementPresent(passwordField);
    }

    public boolean isErrorMessageDisplayed(String expectedError) {
        return isElementDisplayed(byText(expectedError));
    }

    public boolean isTaglineVisible() {
        return isTextVisible("Track Smart. Spend Smart.") || isTextVisible("Login to your account");
    }

    public boolean isGoogleButtonVisible() {
        return isElementDisplayed(googleButton);
    }
}
