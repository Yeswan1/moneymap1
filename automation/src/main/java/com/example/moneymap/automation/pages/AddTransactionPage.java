package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * AddTransactionPage - Page Object for the Add Transaction screen.
 * Uses the custom keypad UI (not system keyboard).
 */
public class AddTransactionPage extends BasePage {

    private final By expenseToggle = By.xpath(
            "//*[@text='Expense' or @text='EXPENSE' or @content-desc='Expense']");
    private final By incomeToggle = By.xpath(
            "//*[@text='Income' or @text='INCOME' or @content-desc='Income']");
    private final By amountDisplay = By.xpath(
            "//*[contains(@resource-id, 'amount') or contains(@text, '0.00')]");
    private final By saveButton = By.xpath(
            "//*[@text='Add Expense' or @text='Add Income' or @text='Save Transaction' or contains(@text, 'Save') or contains(@text, 'Add')]");
    private final By noteField = By.xpath(
            "//android.widget.EditText[@text='What did you buy?' or @hint='What did you buy?' or contains(@resource-id, 'description') or contains(@resource-id, 'note') or contains(@resource-id, 'et_note')]");
    private final By backspaceButton = By.id("com.example.moneymap:id/btn_backspace");
    private final By closeButton = By.xpath(
            "//*[@content-desc='Close' or @content-desc='Back' or contains(@content-desc, 'Back') or contains(@text, 'Back')]");
    private final By dateContainer = By.xpath("//*[contains(@resource-id, 'date') or contains(@text, '-')]");
    private final By categoryGrid = By.xpath("//*[contains(@resource-id, 'category')]");

    public AddTransactionPage(AndroidDriver driver) {
        super(driver);
    }

    public void selectExpense() {
        click(expenseToggle);
    }

    public void selectIncome() {
        click(incomeToggle);
    }

    /**
     * Enter amount via the custom keypad UI.
     * Parses the amount string and taps each digit/dot on the keypad grid.
     */
    public void enterAmount(String amount) {
        By amountField = By.xpath("//android.widget.EditText[@text='0.00' or @hint='0.00' or contains(@text, '0.00') or contains(@resource-id, 'amount') or contains(@resource-id, 'et_amount')]");
        try {
            clearAndType(amountField, amount);
        } catch (Exception e) {
            // Keypad fallback
            for (int i = 0; i < 10; i++) {
                try { click(backspaceButton); } catch (Exception ignored) { break; }
            }
            for (char c : amount.toCharArray()) {
                String charStr = String.valueOf(c);
                try {
                    click(By.xpath("//*[@text='" + charStr + "' and contains(@class,'TextView')]"));
                } catch (Exception ignored) {}
            }
        }
    }

    public void selectCategory(String categoryName) {
        try {
            scrollToText(categoryName);
            clickByText(categoryName);
        } catch (Exception e) {
            // Category grid might use content-desc
            click(By.xpath("//*[contains(@content-desc,'" + categoryName + "')]"));
        }
    }

    public void enterNote(String note) {
        clearAndType(noteField, note);
    }

    public void clickSave() {
        click(saveButton);
    }

    public void clickClose() {
        click(closeButton);
    }

    public void tapDateField() {
        click(dateContainer);
    }

    /**
     * Full transaction creation helper.
     * @param type "expense" or "income"
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

    public boolean isAddTransactionScreenDisplayed() {
        return isTextVisible("Add Transaction") || isElementDisplayed(expenseToggle);
    }

    public String getDisplayedAmount() {
        try {
            return getText(amountDisplay);
        } catch (Exception e) {
            return "0";
        }
    }
}
