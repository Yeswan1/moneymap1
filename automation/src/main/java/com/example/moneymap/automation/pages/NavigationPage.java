package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * Page Object Model for the main bottom navigation bar of the MoneyMap app.
 * Handles tab switching between Home/Dashboard, History, Reports, and Profile tabs.
 */
public class NavigationPage extends BasePage {

    // ── Bottom Navigation Tab Locators ────────────────────────────────────────

    // Home / Dashboard tab
    private static final By TAB_HOME = By.xpath(
        "//*[@content-desc='Home' or @text='Home' " +
        "or @resource-id='com.example.moneymap:id/nav_home']");

    // Transaction History tab
    private static final By TAB_HISTORY = By.xpath(
        "//*[@content-desc='History' or @text='History' or @text='Transactions' " +
        "or @resource-id='com.example.moneymap:id/nav_history']");

    // Reports / Analytics tab
    private static final By TAB_REPORTS = By.xpath(
        "//*[@content-desc='Reports' or @text='Reports' or @text='Analytics' " +
        "or @resource-id='com.example.moneymap:id/nav_reports']");

    // Profile / Settings tab
    private static final By TAB_PROFILE = By.xpath(
        "//*[@content-desc='Profile' or @text='Profile' or @text='Settings' " +
        "or @resource-id='com.example.moneymap:id/nav_profile']");

    // Add transaction FAB (centre button in some nav bars)
    private static final By FAB_ADD = By.xpath(
        "//*[@content-desc='Add Transaction' or @content-desc='Add' " +
        "or @resource-id='com.example.moneymap:id/fab_add']");

    // Bottom navigation container
    private static final By BOTTOM_NAV_BAR = By.xpath(
        "//*[@resource-id='com.example.moneymap:id/bottom_navigation' " +
        "or @resource-id='com.example.moneymap:id/bottom_nav']");

    // ── Constructor ───────────────────────────────────────────────────────────

    public NavigationPage(AndroidDriver driver) {
        super(driver);
    }

    // ── Navigation Actions ────────────────────────────────────────────────────

    /** Navigates to the Home/Dashboard tab */
    public void goToHome() {
        click(TAB_HOME);
    }

    /** Navigates to the Transaction History tab */
    public void goToHistory() {
        click(TAB_HISTORY);
    }

    /** Navigates to the Reports/Analytics tab */
    public void goToReports() {
        click(TAB_REPORTS);
    }

    /** Navigates to the Profile tab */
    public void goToProfile() {
        click(TAB_PROFILE);
    }

    /** Taps the floating Add Transaction button */
    public void tapAddButton() {
        click(FAB_ADD);
    }

    // ── Assertions ────────────────────────────────────────────────────────────

    /** Returns true if the bottom navigation bar is visible */
    public boolean isNavBarVisible() {
        return isElementDisplayed(BOTTOM_NAV_BAR);
    }

    /** Returns true if the Home tab is present in the nav bar */
    public boolean isHomeTabVisible() {
        return isElementDisplayed(TAB_HOME);
    }

    /** Returns true if the History tab is present in the nav bar */
    public boolean isHistoryTabVisible() {
        return isElementDisplayed(TAB_HISTORY);
    }

    /** Returns true if the Reports tab is present in the nav bar */
    public boolean isReportsTabVisible() {
        return isElementDisplayed(TAB_REPORTS);
    }

    /** Returns true if the Profile tab is present in the nav bar */
    public boolean isProfileTabVisible() {
        return isElementDisplayed(TAB_PROFILE);
    }

    /** Returns true if the Add Transaction FAB is visible */
    public boolean isFabVisible() {
        return isElementDisplayed(FAB_ADD);
    }
}
