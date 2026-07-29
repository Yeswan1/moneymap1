package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * Page Object Model for the Reports/Analytics screen of the MoneyMap app.
 * Covers the ReportsActivity and the Compose-based reports screen.
 */
public class ReportsPage extends BasePage {

    // ── Element Locators ──────────────────────────────────────────────────────

    // Reports tab / screen title
    private static final By REPORTS_TITLE = By.xpath(
        "//*[@text='Reports' or @text='Analytics' or @content-desc='Reports']");

    // Date range selectors
    private static final By DATE_RANGE_WEEKLY = By.xpath(
        "//*[@text='Weekly' or @content-desc='Weekly']");
    private static final By DATE_RANGE_MONTHLY = By.xpath(
        "//*[@text='Monthly' or @content-desc='Monthly']");
    private static final By DATE_RANGE_YEARLY = By.xpath(
        "//*[@text='Yearly' or @content-desc='Yearly']");

    // Chart area (income vs expense bar/pie chart)
    private static final By CHART_CONTAINER = By.xpath(
        "//*[@content-desc='chart' or @resource-id='com.example.moneymap:id/chart']");

    // Income total label
    private static final By INCOME_TOTAL = By.xpath(
        "//*[@content-desc='Income Total' or contains(@text,'Income')]");

    // Expense total label
    private static final By EXPENSE_TOTAL = By.xpath(
        "//*[@content-desc='Expense Total' or contains(@text,'Expense')]");

    // Category breakdown list
    private static final By CATEGORY_LIST = By.xpath(
        "//*[@resource-id='com.example.moneymap:id/category_list' or @content-desc='Category Breakdown']");

    // Export / share button (if available)
    private static final By EXPORT_BUTTON = By.xpath(
        "//*[@text='Export' or @content-desc='Export' or @content-desc='Share']");

    // ── Constructor ───────────────────────────────────────────────────────────

    public ReportsPage(AndroidDriver driver) {
        super(driver);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    /** Checks if the Reports screen is visible */
    public boolean isReportsScreenLoaded() {
        return isElementDisplayed(REPORTS_TITLE);
    }

    /** Selects the Weekly date range tab */
    public void selectWeeklyRange() {
        click(DATE_RANGE_WEEKLY);
    }

    /** Selects the Monthly date range tab */
    public void selectMonthlyRange() {
        click(DATE_RANGE_MONTHLY);
    }

    /** Selects the Yearly date range tab */
    public void selectYearlyRange() {
        click(DATE_RANGE_YEARLY);
    }

    /** Returns true if the chart container is displayed */
    public boolean isChartVisible() {
        return isElementDisplayed(CHART_CONTAINER);
    }

    /** Returns the displayed income total text */
    public String getIncomeTotalText() {
        return getText(INCOME_TOTAL);
    }

    /** Returns the displayed expense total text */
    public String getExpenseTotalText() {
        return getText(EXPENSE_TOTAL);
    }

    /** Returns true if the category breakdown list is present */
    public boolean isCategoryBreakdownVisible() {
        return isElementDisplayed(CATEGORY_LIST);
    }

    /** Taps the export/share button if available */
    public boolean tapExportIfAvailable() {
        if (isElementDisplayed(EXPORT_BUTTON)) {
            click(EXPORT_BUTTON);
            return true;
        }
        return false;
    }
}
