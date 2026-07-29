package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * Page Object Model for the Search and Filter screen of the MoneyMap app.
 * Covers transaction search, category filters, and date range filters in
 * TransactionHistoryActivity and Compose-based history screens.
 */
public class SearchPage extends BasePage {

    // ── Element Locators ──────────────────────────────────────────────────────

    // Search bar input
    private static final By SEARCH_BAR = By.xpath(
        "//*[@resource-id='com.example.moneymap:id/search_bar' " +
        "or @content-desc='Search transactions' " +
        "or @hint='Search' " +
        "or @class='android.widget.SearchView']");

    // Search result list
    private static final By SEARCH_RESULTS = By.xpath(
        "//*[@resource-id='com.example.moneymap:id/search_results' " +
        "or @content-desc='Search Results']");

    // "No results" empty state text
    private static final By NO_RESULTS_TEXT = By.xpath(
        "//*[@text='No transactions found' or @text='No results' or contains(@text,'No data')]");

    // Filter chip: All
    private static final By FILTER_ALL = By.xpath(
        "//*[@text='All' and @class='android.widget.Chip' or @content-desc='Filter All']");

    // Filter chip: Income
    private static final By FILTER_INCOME = By.xpath(
        "//*[@text='Income' and @class='android.widget.Chip' or @content-desc='Filter Income']");

    // Filter chip: Expense
    private static final By FILTER_EXPENSE = By.xpath(
        "//*[@text='Expense' and @class='android.widget.Chip' or @content-desc='Filter Expense']");

    // Category dropdown / spinner
    private static final By CATEGORY_FILTER_SPINNER = By.xpath(
        "//*[@resource-id='com.example.moneymap:id/category_filter' " +
        "or @content-desc='Filter by category']");

    // Date range: From field
    private static final By DATE_FROM = By.xpath(
        "//*[@resource-id='com.example.moneymap:id/date_from' or @content-desc='Start date']");

    // Date range: To field
    private static final By DATE_TO = By.xpath(
        "//*[@resource-id='com.example.moneymap:id/date_to' or @content-desc='End date']");

    // Apply filter button
    private static final By APPLY_FILTER_BUTTON = By.xpath(
        "//*[@text='Apply' or @text='Filter' or @content-desc='Apply Filter']");

    // Clear / Reset filter button
    private static final By CLEAR_FILTER_BUTTON = By.xpath(
        "//*[@text='Clear' or @text='Reset' or @content-desc='Clear Filter']");

    // ── Constructor ───────────────────────────────────────────────────────────

    public SearchPage(AndroidDriver driver) {
        super(driver);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    /** Types a search query into the search bar */
    public void search(String query) {
        click(SEARCH_BAR);
        type(SEARCH_BAR, query);
    }

    /** Clears the search bar */
    public void clearSearch() {
        clearField(SEARCH_BAR);
    }

    /** Returns true if search results are displayed */
    public boolean hasSearchResults() {
        return isElementDisplayed(SEARCH_RESULTS);
    }

    /** Returns true if the no-results empty state is shown */
    public boolean isNoResultsDisplayed() {
        return isElementDisplayed(NO_RESULTS_TEXT);
    }

    /** Applies the "All" transactions filter */
    public void filterAll() {
        click(FILTER_ALL);
    }

    /** Applies the "Income" filter chip */
    public void filterByIncome() {
        click(FILTER_INCOME);
    }

    /** Applies the "Expense" filter chip */
    public void filterByExpense() {
        click(FILTER_EXPENSE);
    }

    /** Taps the apply filter button */
    public void applyFilter() {
        click(APPLY_FILTER_BUTTON);
    }

    /** Clears/resets all active filters */
    public void clearFilters() {
        if (isElementDisplayed(CLEAR_FILTER_BUTTON)) {
            click(CLEAR_FILTER_BUTTON);
        }
    }

    /** Returns true if the Income filter chip is visible */
    public boolean isIncomeFilterAvailable() {
        return isElementDisplayed(FILTER_INCOME);
    }

    /** Returns true if the Expense filter chip is visible */
    public boolean isExpenseFilterAvailable() {
        return isElementDisplayed(FILTER_EXPENSE);
    }
}
