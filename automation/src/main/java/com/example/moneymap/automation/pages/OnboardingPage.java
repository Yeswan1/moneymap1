package com.example.moneymap.automation.pages;

import com.example.moneymap.automation.utils.LogUtil;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * OnboardingPage — Page Object for the MoneyMap onboarding flow.
 */
public class OnboardingPage extends BasePage {

    // ── Locators ──────────────────────────────────────────────────────────────

    private final By skipButton = By.xpath(
        "//*[@text='Skip' or @text='SKIP' or @content-desc='Skip']");
    private final By getStartedButton = By.xpath(
        "//*[@text='Get Started' or @text='GET STARTED' or @content-desc='Get Started']");
    private final By nextButton = By.xpath(
        "//*[@text='Next' or @text='NEXT' or @content-desc='Next']");
    private final By slideTitle = By.xpath(
        "//*[contains(@text,'Track') or contains(@text,'Budget') or contains(@text,'Reports') " +
        "or contains(@text,'Welcome') or contains(@resource-id,'onboarding_title')]");

    public OnboardingPage(AndroidDriver driver) {
        super(driver);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    public void clickSkip() {
        click(skipButton);
    }

    public void clickGetStarted() {
        click(getStartedButton);
    }

    public void clickNext() {
        try {
            click(nextButton);
        } catch (Exception e) {
            LogUtil.logWarning("Next button not found on onboarding: " + e.getMessage());
        }
    }

    public void swipeToNextSlide() {
        swipeUp();
    }

    // ── Assertions ────────────────────────────────────────────────────────────

    public boolean isOnboardingDisplayed() {
        return isElementDisplayed(skipButton) || isElementDisplayed(getStartedButton);
    }

    public String getCurrentSlideTitle() {
        try {
            return getText(slideTitle);
        } catch (Exception e) {
            return "";
        }
    }
}
