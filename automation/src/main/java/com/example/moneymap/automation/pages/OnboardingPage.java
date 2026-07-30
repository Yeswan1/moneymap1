package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * OnboardingPage - Page Object for OnboardingScreen / OnboardingActivity.
 */
public class OnboardingPage extends BasePage {

    private final By skipButton = By.xpath(
            "//*[@text='Skip' or @content-desc='Skip']");
    private final By nextButton = By.xpath(
            "//*[@text='Next' or @content-desc='Next' or @text='Next ->']");
    private final By getStartedButton = By.xpath(
            "//*[@text='Get Started' or @content-desc='Get Started']");

    public OnboardingPage(AndroidDriver driver) {
        super(driver);
    }

    public void skipOnboarding() {
        try {
            click(skipButton);
        } catch (Exception e) {
            // If no skip, navigate forward
            clickNext();
            clickNext();
        }
    }

    public void clickNext() {
        click(nextButton);
    }

    public void clickGetStarted() {
        try { click(getStartedButton); }
        catch (Exception e) { clickNext(); }
    }

    public boolean isOnboardingDisplayed() {
        return isTextVisible("Track Every Penny") || isTextVisible("Smart Budgeting")
                || isTextVisible("Pocket Manager");
    }
}
