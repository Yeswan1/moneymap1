package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class HistoryPage extends BasePage {

    private static final By SEARCH_INPUT = By.xpath(
        "//*[@resource-id='com.example.moneymap:id/search_bar' " +
        "or @hint='Search' or @content-desc='Search transactions' " +
        "or @class='android.widget.SearchView'//android.widget.EditText]");

    private static final By HISTORY_TITLE = By.xpath(
        "//*[@text='History' or @text='Transactions' or @text='Transaction History' " +
        "or @content-desc='History']");

    private static final By TRANSACTION_LIST = By.xpath(
        "//*[@resource-id='com.example.moneymap:id/transactions_list' " +
        "or @resource-id='com.example.moneymap:id/recycler_view']");

    private static final By FIRST_TRANSACTION_ITEM = By.xpath(
        "//*[contains(@text,'Food') or contains(@text,'Transport') " +
        "or contains(@text,'Income') or contains(@text,'Expense')]");

    private final By searchIcon = By.xpath(
        "//*[contains(@content-desc,'Search') or @resource-id='com.example.moneymap:id/search_btn']");
    private final By filterIcon = By.xpath(
        "//*[contains(@content-desc,'Filter') or @resource-id='com.example.moneymap:id/filter_btn']");

    public HistoryPage(AndroidDriver driver) {
        super(driver);
    }

    /** Returns true if the history screen title or transaction list is visible */
    public boolean isHistoryListDisplayed() {
        return isElementDisplayed(HISTORY_TITLE) || isElementDisplayed(TRANSACTION_LIST);
    }

    /** Searches for a transaction using the search bar */
    public void searchTransactions(String query) {
        try {
            // Try clicking a search icon first
            if (isElementDisplayed(searchIcon)) {
                click(searchIcon);
            }
            // Then type into the search input
            if (isElementDisplayed(SEARCH_INPUT)) {
                type(SEARCH_INPUT, query);
            }
        } catch (Exception e) {
            // Search not available on this screen — skip gracefully
        }
    }

    public void clickSearch() {
        try {
            click(searchIcon);
        } catch (Exception e) {
            click(byText("Search"));
        }
    }

    public void clickFilter() {
        try {
            click(filterIcon);
        } catch (Exception e) {
            click(byText("Filter"));
        }
    }

    public boolean isTransactionListEmpty() {
        return !isElementDisplayed(FIRST_TRANSACTION_ITEM);
    }

    public boolean isTransactionVisible(String noteOrCategory) {
        return isElementDisplayed(byText(noteOrCategory));
    }
}
