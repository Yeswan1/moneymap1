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
    // Compose fields lack android:id, so we use text/content-desc/class XPath
    private final By emailField = By.xpath(
            "//android.widget.EditText[@text='your.email@example.com' " +
            "or @hint='your.email@example.com' " +
            "or @text='Email Address' " +
            "or @index='0']");

    private final By passwordField = By.xpath(
            "//android.widget.EditText[@text='Enter your password' " +
            "or @hint='Enter your password' " +
            "or @index='1']");

    private final By signInButton = byText("Sign In");
    private final By loginButton = byText("Login");
    private final By signUpLink = byText("Sign Up");
    private final By forgotPasswordLink = byText("Forgot Password?");
    private final By googleButton = byText("Continue with Google");
    private final By facebookButton = byText("Continue with Facebook");
    private final By rememberMeCheckbox = By.id("com.example.moneymap:id/remember_me_checkbox");
    private final By passwordToggle = By.xpath(
            "//*[@content-desc='Show password' or @content-desc='Hide password']");
    private final By welcomeTitle = byText("Welcome Back!");
    private final By taglineText = byText("Track Smart. Spend Smart.");

    public LoginPage(AndroidDriver driver) {
        super(driver);
    }

    // ─── Actions ──────────────────────────────────────────────────────────────

    public void enterEmail(String email) {
        try {
            clearAndType(emailField, email);
        } catch (Exception e) {
            // Try compose field via index
            clearAndType(By.xpath("//android.widget.EditText[1]"), email);
        }
    }

    public void enterPassword(String password) {
        try {
            clearAndType(passwordField, password);
        } catch (Exception e) {
            clearAndType(By.xpath("//android.widget.EditText[2]"), password);
        }
    }

    public void clickLogin() {
        try {
            click(signInButton);
        } catch (Exception e) {
            click(loginButton);
        }
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

    public void clickContinueWithFacebook() {
        click(facebookButton);
    }

    public void togglePasswordVisibility() {
        try {
            click(passwordToggle);
        } catch (Exception e) {
            // Try by content description
            click(By.xpath("//*[contains(@content-desc, 'password')]"));
        }
    }

    public void toggleRememberMe() {
        try {
            click(rememberMeCheckbox);
        } catch (Exception e) {
            click(byText("Remember me"));
        }
    }

    public void login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickLogin();
    }

    // ─── Assertions ───────────────────────────────────────────────────────────

    public boolean isLoginScreenDisplayed() {
        return isTextVisible("Welcome Back!") || isTextVisible("Sign In") || isTextVisible("Login");
    }

    public boolean isErrorMessageDisplayed(String expectedError) {
        return isElementDisplayed(byText(expectedError));
    }

    public boolean isTaglineVisible() {
        return isTextVisible("Track Smart. Spend Smart.");
    }

    public boolean isGoogleButtonVisible() {
        return isElementDisplayed(googleButton);
    }

    public boolean isFacebookButtonVisible() {
        return isElementDisplayed(facebookButton);
    }
}
