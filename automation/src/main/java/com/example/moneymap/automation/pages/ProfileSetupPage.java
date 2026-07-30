package com.example.moneymap.automation.pages;

import com.example.moneymap.automation.utils.LogUtil;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * ProfileSetupPage — Page Object for role-specific profile setup screens
 * (Student Setup, Employee Setup, Homemaker Setup, General Setup).
 */
public class ProfileSetupPage extends BasePage {

    // ── Locators ──────────────────────────────────────────────────────────────

    private final By nameField = By.xpath(
        "//android.widget.EditText[contains(@hint,'Name') or contains(@hint,'name') " +
        "or @resource-id='com.example.moneymap:id/et_name']");
    private final By currencySpinner = By.xpath(
        "//*[contains(@resource-id,'currency') or contains(@resource-id,'spinner_currency') " +
        "or @text='INR' or @text='USD' or @text='EUR']");
    private final By monthlyAmountField = By.xpath(
        "//android.widget.EditText[contains(@hint,'Income') or contains(@hint,'income') " +
        "or contains(@hint,'Allowance') or contains(@hint,'allowance') " +
        "or contains(@hint,'Salary') or contains(@hint,'salary') " +
        "or contains(@hint,'Budget') or contains(@hint,'budget') " +
        "or contains(@resource-id,'income') or contains(@resource-id,'allowance')]");
    private final By organisationField = By.xpath(
        "//android.widget.EditText[contains(@hint,'Company') or contains(@hint,'company') " +
        "or contains(@hint,'Institution') or contains(@hint,'institution') " +
        "or contains(@hint,'College') or contains(@hint,'college') " +
        "or contains(@resource-id,'company') or contains(@resource-id,'institution')]");
    private final By nextButton = By.xpath(
        "//*[@text='Next' or @text='NEXT' or @text='Continue' or @text='CONTINUE' " +
        "or @resource-id='com.example.moneymap:id/btn_next']");

    public ProfileSetupPage(AndroidDriver driver) {
        super(driver);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    public void enterName(String name) {
        try {
            clearAndType(nameField, name);
        } catch (Exception e) {
            LogUtil.logWarning("Name field not found on profile setup: " + e.getMessage());
        }
    }

    public void selectCurrency(String currency) {
        try {
            click(currencySpinner);
            waitSeconds(1);
            By currencyItem = By.xpath("//*[@text='" + currency + "' or contains(@text,'" + currency + "')]");
            click(currencyItem);
        } catch (Exception e) {
            LogUtil.logWarning("Could not select currency '" + currency + "': " + e.getMessage());
        }
    }

    public void enterMonthlyAmount(String amount) {
        try {
            clearAndType(monthlyAmountField, amount);
        } catch (Exception e) {
            LogUtil.logWarning("Monthly amount field not found: " + e.getMessage());
        }
    }

    public void enterOrganisation(String org) {
        try {
            clearAndType(organisationField, org);
        } catch (Exception e) {
            LogUtil.logWarning("Organisation field not found: " + e.getMessage());
        }
    }

    public void clickNext() {
        click(nextButton);
    }

    // ── Assertions ────────────────────────────────────────────────────────────

    public boolean isProfileSetupDisplayed() {
        return isElementDisplayed(nextButton);
    }
}
