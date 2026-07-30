package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * BudgetSetupPage - Page Object for BudgetSetupActivity / Compose BudgetSetupScreen.
 */
public class BudgetSetupPage extends BasePage {

    private final By totalLimitField = By.id("com.example.moneymap:id/et_total_limit");
    private final By foodBudgetField = By.id("com.example.moneymap:id/et_food_budget");
    private final By shoppingBudgetField = By.id("com.example.moneymap:id/et_shopping_budget");
    private final By transportBudgetField = By.id("com.example.moneymap:id/et_transport_budget");
    private final By entertainmentBudgetField = By.id("com.example.moneymap:id/et_entertainment_budget");
    private final By finishButton = By.xpath(
            "//*[@resource-id='com.example.moneymap:id/btn_finish' " +
            "or @text='Complete Setup' or @content-desc='Complete Setup']");

    public BudgetSetupPage(AndroidDriver driver) {
        super(driver);
    }

    public void enterTotalLimit(String amount) {
        try {
            clearAndType(totalLimitField, amount);
        } catch (Exception e) {
            clearAndType(By.xpath("//android.widget.EditText[1]"), amount);
        }
    }

    public void enterFoodBudget(String amount) {
        try {
            clearAndType(foodBudgetField, amount);
        } catch (Exception e) {
            clearAndType(By.xpath("//android.widget.EditText[2]"), amount);
        }
    }

    public void enterShoppingBudget(String amount) {
        try {
            clearAndType(shoppingBudgetField, amount);
        } catch (Exception e) {
            clearAndType(By.xpath("//android.widget.EditText[3]"), amount);
        }
    }

    public void enterTransportBudget(String amount) {
        try {
            clearAndType(transportBudgetField, amount);
        } catch (Exception e) {
            clearAndType(By.xpath("//android.widget.EditText[4]"), amount);
        }
    }

    public void setupBudgets(String total, String food, String transport) {
        enterTotalLimit(total);
        enterFoodBudget(food);
        enterTransportBudget(transport);
        scrollToText("Complete Setup");
        clickFinish();
    }

    public void clickFinish() {
        click(finishButton);
    }

    public boolean isBudgetSetupDisplayed() {
        return isTextVisible("Budget Setup") || isTextVisible("TOTAL MONTHLY LIMIT");
    }
}
