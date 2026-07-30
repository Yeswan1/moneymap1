package com.example.moneymap.automation.pages;

import com.example.moneymap.automation.utils.LogUtil;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * AddTransactionPage — Page Object for the Add Transaction screen.
 */
public class AddTransactionPage extends BasePage {

    // ── Locators ──────────────────────────────────────────────────────────────

    private final By amountField = By.xpath(
        "//android.widget.EditText[contains(@hint,'Amount') or contains(@hint,'amount') " +
        "or contains(@resource-id,'amount') or contains(@resource-id,'et_amount')]");
    private final By expenseToggle = By.xpath(
        "//*[@text='Expense' or @text='EXPENSE' or @content-desc='Expense' " +
        "or @resource-id='com.example.moneymap:id/btn_expense']");
    private final By incomeToggle = By.xpath(
        "//*[@text='Income' or @text='INCOME' or @content-desc='Income' " +
        "or @resource-id='com.example.moneymap:id/btn_income']");
    private final By categorySelector = By.xpath(
        "//*[contains(@text,'Category') or contains(@text,'category') or " +
        "contains(@resource-id,'category') or contains(@resource-id,'spinner_category')]");
    private final By noteField = By.xpath(
        "//android.widget.EditText[contains(@hint,'Note') or contains(@hint,'note') " +
        "or contains(@hint,'Description') or contains(@resource-id,'note') or contains(@resource-id,'description')]");
    private final By saveButton = By.xpath(
        "//*[@text='Save' or @text='SAVE' or @text='Add Transaction' or @text='ADD TRANSACTION' " +
        "or @text='Submit' or @resource-id='com.example.moneymap:id/btn_save']");
    private final By closeButton = By.xpath(
        "//*[@content-desc='Close' or @content-desc='Navigate up' or @content-desc='Back' " +
        "or contains(@resource-id,'close') or contains(@resource-id,'back')]");
    private final By screenTitle = By.xpath(
        "//*[@text='Add Transaction' or @text='New Transaction' or contains(@resource-id,'toolbar_title')]");

    public AddTransactionPage(AndroidDriver driver) {
        super(driver);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    public void selectExpense() {
        click(expenseToggle);
    }

    public void selectIncome() {
        click(incomeToggle);
    }

    public void enterAmount(String amount) {
        clearAndType(amountField, amount);
    }

    public void selectCategory(String categoryName) {
        try {
            click(categorySelector);
            waitSeconds(1);
            // Try to find the category item in the list
            By categoryItem = By.xpath(
                "//*[contains(@text,'" + categoryName + "') or @text='" + categoryName + "']");
            if (!isElementDisplayed(categoryItem)) {
                scrollToText(categoryName);
            }
            click(categoryItem);
        } catch (Exception e) {
            LogUtil.logWarning("Could not select category '" + categoryName + "': " + e.getMessage());
        }
    }

    public void enterNote(String note) {
        try {
            clearAndType(noteField, note);
        } catch (Exception e) {
            LogUtil.logWarning("Note field not found, skipping: " + e.getMessage());
        }
    }

    public void clickSave() {
        click(saveButton);
    }

    public void clickClose() {
        try {
            click(closeButton);
        } catch (Exception e) {
            pressBack();
        }
    }

    /**
     * Convenience method: selects type, enters amount/category/note, then saves.
     */
    public void createTransaction(String type, String amount, String category, String note) {
        if ("income".equalsIgnoreCase(type)) {
            selectIncome();
        } else {
            selectExpense();
        }
        enterAmount(amount);
        selectCategory(category);
        if (note != null && !note.isEmpty()) {
            enterNote(note);
        }
        clickSave();
    }

    // ── Assertions ────────────────────────────────────────────────────────────

    public boolean isAddTransactionScreenDisplayed() {
        return isElementDisplayed(saveButton) || isElementDisplayed(amountField);
    }

    public String getDisplayedAmount() {
        try {
            return getText(amountField);
        } catch (Exception e) {
            return null;
        }
    }
}
