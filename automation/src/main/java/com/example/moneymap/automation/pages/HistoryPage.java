package com.example.moneymap.automation.pages;

import com.example.moneymap.automation.utils.LogUtil;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * HistoryPage — Page Object for the Transaction History screen.
 */
public class HistoryPage extends BasePage {

    // ── Locators ──────────────────────────────────────────────────────────────

    private final By transactionListItems = By.xpath(
        "//androidx.recyclerview.widget.RecyclerView/android.view.ViewGroup " +
        "| //androidx.recyclerview.widget.RecyclerView/android.widget.LinearLayout " +
        "| //androidx.recyclerview.widget.RecyclerView/android.widget.RelativeLayout");
    private final By searchIcon = By.xpath(
        "//*[@content-desc='Search' or contains(@resource-id,'search') or @text='Search']");
    private final By filterButton = By.xpath(
        "//*[@text='Filter' or @content-desc='Filter' or contains(@resource-id,'filter')]");
    private final By historyScreenIndicator = By.xpath(
        "//*[@text='Transactions' or @text='History' or @text='All Transactions' " +
        "or contains(@resource-id,'history') or contains(@resource-id,'transaction_list')]");
    private final By searchField = By.xpath(
        "//android.widget.EditText[contains(@hint,'Search') or contains(@text,'Search')]");

    public HistoryPage(AndroidDriver driver) {
        super(driver);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    public void tapTransaction(int index) {
        try {
            List<WebElement> items = findElements(transactionListItems);
            if (index >= 0 && index < items.size()) {
                items.get(index).click();
            } else {
                LogUtil.logWarning("Transaction index " + index + " out of range (total: " + items.size() + ")");
            }
        } catch (Exception e) {
            LogUtil.logError("Failed to tap transaction at index " + index, e);
        }
    }

    public void clickSearchIcon() {
        try {
            click(searchIcon);
        } catch (Exception e) {
            LogUtil.logWarning("Search icon not found: " + e.getMessage());
        }
    }

    public void enterSearchQuery(String query) {
        try {
            clearAndType(searchField, query);
        } catch (Exception e) {
            LogUtil.logWarning("Search field not found: " + e.getMessage());
        }
    }

    // ── Assertions ────────────────────────────────────────────────────────────

    public boolean isHistoryScreenDisplayed() {
        return isElementDisplayed(historyScreenIndicator) ||
               !findElements(transactionListItems).isEmpty();
    }

    public int getTransactionCount() {
        try {
            return findElements(transactionListItems).size();
        } catch (Exception e) {
            return 0;
        }
    }
}
