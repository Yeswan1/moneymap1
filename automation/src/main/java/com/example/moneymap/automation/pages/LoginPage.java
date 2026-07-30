package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * LoginPage — Page Object for the MoneyMap login screen.
 */
public class LoginPage extends BasePage {

    // ── Locators ──────────────────────────────────────────────────────────────

    private final By emailField = By.xpath(
        "//android.widget.EditText[contains(@text,'Email') or contains(@hint,'Email') or contains(@text,'email')]");
    private final By passwordField = By.xpath(
        "//android.widget.EditText[contains(@text,'Password') or contains(@hint,'Password') or contains(@hint,'password')]");
    private final By signInButton = By.xpath(
        "//*[@text='Sign In' or @text='Login' or @text='LOG IN' or @text='SIGN IN' or @content-desc='Sign In']");
    private final By signUpLink = By.xpath(
        "//*[@text='Sign Up' or @text='Create Account' or @text='Register' or contains(@text,'Sign up')]");
    private final By forgotPasswordLink = By.xpath(
        "//*[contains(@text,'Forgot') or contains(@text,'Reset Password')]");
    private final By googleButton = By.xpath(
        "//*[contains(@text,'Google') or contains(@content-desc,'Google') or @resource-id='com.example.moneymap:id/btn_google_signin']");
    private final By passwordToggle = By.xpath(
        "//*[@content-desc='Toggle password visibility' or contains(@resource-id,'password_toggle') or contains(@resource-id,'toggle')]");
    private final By errorMessage = By.xpath(
        "//*[contains(@text,'Invalid') or contains(@text,'invalid') or contains(@text,'credentials') or " +
        "contains(@text,'error') or contains(@text,'Error') or contains(@text,'required') or contains(@text,'Required') or " +
        "contains(@text,'incorrect') or contains(@text,'Incorrect') or contains(@text,'failed') or contains(@text,'Failed')]");

    public LoginPage(AndroidDriver driver) {
        super(driver);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    public void login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickSignIn();
    }

    public void enterEmail(String email) {
        clearAndType(emailField, email);
    }

    public void enterPassword(String password) {
        clearAndType(passwordField, password);
    }

    public void clickSignIn() {
        click(signInButton);
    }

    public void clickSignUp() {
        click(signUpLink);
    }

    public void clickForgotPassword() {
        click(forgotPasswordLink);
    }

    public void togglePasswordVisibility() {
        click(passwordToggle);
    }

    // ── Assertions ────────────────────────────────────────────────────────────

    /**
     * Returns true when both email and password fields are visible — confirms
     * we are on the login screen.
     */
    public boolean isLoginScreenDisplayed() {
        return isElementDisplayed(emailField) && isElementDisplayed(passwordField);
    }

    /**
     * Returns true if an error/validation message containing the given fragment is visible.
     */
    public boolean isErrorMessageDisplayed(String messageFragment) {
        if (messageFragment == null || messageFragment.isEmpty()) {
            return isElementDisplayed(errorMessage);
        }
        By fragmentLocator = By.xpath(
            "//*[contains(translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" +
            messageFragment.toLowerCase() + "')]");
        return isElementDisplayed(fragmentLocator);
    }

    public boolean isGoogleButtonVisible() {
        return isElementDisplayed(googleButton);
    }

    public String getEmailFieldText() {
        try {
            return getText(emailField);
        } catch (Exception e) {
            return "";
        }
    }
}
