package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * BudgetSetupPage - Page Object for BudgetSetupActivity / Compose BudgetSetupScreen.
 */
public class BudgetSetupPage extends BasePage {

    private final By finishButton = By.xpath(
            "//*[@text='Complete Setup' or @content-desc='Complete Setup' or contains(@text, 'Finish')]");

    public BudgetSetupPage(AndroidDriver driver) {
        super(driver);
    }

    public void enterBudgetForCategory(String categoryName, String amount) {
        By editField = By.xpath(
            "//android.widget.TextView[@text='" + categoryName + "']/ancestor::android.view.View//android.widget.EditText" +
            " | //android.widget.TextView[@text='" + categoryName + "']/../../..//android.widget.EditText"
        );
        clearAndType(editField, amount);
    }

    public void enterTotalLimit(String amount) {
        // Total Limit is computed automatically in the Compose UI based on category limits.
        // Leave as no-op to support compatibility.
    }

    public void enterFoodBudget(String amount) {
        enterBudgetForCategory("Food", amount);
    }

    public void enterShoppingBudget(String amount) {
        enterBudgetForCategory("Shopping", amount);
    }

    public void enterTransportBudget(String amount) {
        enterBudgetForCategory("Transport", amount);
    }

    public void setupBudgets(String total, String food, String transport) {
        enterFoodBudget(food);
        enterTransportBudget(transport);
        scrollToText("Complete Setup");
        clickFinish();
    }

    public void clickFinish() {
        click(finishButton);
    }

    public boolean isBudgetSetupDisplayed() {
        return isTextVisible("Set Your Budget") || isTextVisible("Complete Setup");
    }
}
