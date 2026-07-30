package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * AddTransactionPage - Page Object for the Add Transaction screen.
 * Uses the custom keypad UI (not system keyboard).
 */
public class AddTransactionPage extends BasePage {

    private final By expenseToggle = By.xpath(
            "//*[@text='EXPENSE' or @content-desc='EXPENSE']");
    private final By incomeToggle = By.xpath(
            "//*[@text='INCOME' or @content-desc='INCOME']");
    private final By amountDisplay = By.xpath(
            "//*[@resource-id='com.example.moneymap:id/amount_text']");
    private final By saveButton = By.xpath(
            "//*[@resource-id='com.example.moneymap:id/btn_save' " +
            "or @text='Save Transaction' or @content-desc='Save Transaction']");
    private final By noteField = By.xpath(
            "//*[@resource-id='com.example.moneymap:id/et_note' " +
            "or @hint='Add a note...' or @text='Add a note...']");
    private final By backspaceButton = By.id("com.example.moneymap:id/btn_backspace");
    private final By closeButton = By.xpath(
            "//*[@resource-id='com.example.moneymap:id/btn_close' " +
            "or @content-desc='Close']");
    private final By dateContainer = By.id("com.example.moneymap:id/date_container");
    private final By categoryGrid = By.id("com.example.moneymap:id/category_grid");

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
        // First clear by tapping backspace multiple times
        for (int i = 0; i < 10; i++) {
            try { click(backspaceButton); } catch (Exception ignored) { break; }
        }
        // Tap each character on the keypad
        for (char c : amount.toCharArray()) {
            if (c == '-') continue; // Keypad doesn't support minus
            String charStr = String.valueOf(c);
            try {
                click(By.xpath("//*[@text='" + charStr + "' and contains(@class,'TextView')]"));
            } catch (Exception ignored) {}
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
        try {
            clearAndType(noteField, note);
        } catch (Exception e) {
            By noteFallback = By.xpath("//android.widget.EditText[contains(@hint,'note') or contains(@text,'note')]");
            clearAndType(noteFallback, note);
        }
    }

    public void clickSave() {
        scrollToText("Save Transaction");
        click(saveButton);
    }

    public void clickClose() {
        try { click(closeButton); } catch (Exception e) { pressBack(); }
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
