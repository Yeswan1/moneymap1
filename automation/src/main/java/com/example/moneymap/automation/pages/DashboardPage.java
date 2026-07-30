package com.example.moneymap.automation.pages;

import com.example.moneymap.automation.utils.LogUtil;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * DashboardPage — Page Object for the MoneyMap main dashboard screen.
 */
public class DashboardPage extends BasePage {

    // ── Locators ──────────────────────────────────────────────────────────────

    private final By addTransactionFab = By.xpath(
        "//*[@content-desc='Add Transaction' or @resource-id='com.example.moneymap:id/fab_add' " +
        "or @resource-id='com.example.moneymap:id/fab' or @content-desc='Add' " +
        "or contains(@resource-id,'fab')]");
    private final By balanceText = By.xpath(
        "//*[contains(@text,'₹') or contains(@text,'$') or contains(@text,'Balance') " +
        "or contains(@resource-id,'balance') or contains(@resource-id,'total')]");
    private final By reportsTab = By.xpath(
        "//*[@text='Reports' or @content-desc='Reports' or contains(@resource-id,'tab_reports')]");
    private final By budgetTab = By.xpath(
        "//*[@text='Budget' or @content-desc='Budget' or contains(@resource-id,'tab_budget')]");
    private final By profileTab = By.xpath(
        "//*[@text='Profile' or @content-desc='Profile' or contains(@resource-id,'tab_profile')]");
    private final By homeTab = By.xpath(
        "//*[@text='Home' or @content-desc='Home' or contains(@resource-id,'tab_home')]");
    private final By seeAllTransactions = By.xpath(
        "//*[@text='See All' or @text='View All' or @text='SEE ALL' or contains(@text,'transactions') " +
        "or contains(@resource-id,'see_all') or contains(@resource-id,'view_all')]");
    private final By logoutButton = By.xpath(
        "//*[@text='Logout' or @text='Log Out' or @text='LOGOUT' or @text='Sign Out' " +
        "or contains(@resource-id,'logout') or contains(@resource-id,'sign_out')]");

    public DashboardPage(AndroidDriver driver) {
        super(driver);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    public void clickAddTransactionButton() {
        click(addTransactionFab);
    }

    public void navigateToReports() {
        click(reportsTab);
    }

    public void navigateToBudget() {
        click(budgetTab);
    }

    public void navigateToProfile() {
        click(profileTab);
    }

    public void navigateToHome() {
        click(homeTab);
    }

    public void logout() {
        // Navigate to profile tab first, then tap logout
        try {
            navigateToProfile();
            waitSeconds(1);
            click(logoutButton);
        } catch (Exception e) {
            LogUtil.logWarning("Logout via profile tab failed, trying scroll: " + e.getMessage());
            scrollToText("Logout");
            click(logoutButton);
        }
    }

    public void clickSeeAllTransactions() {
        click(seeAllTransactions);
    }

    // ── Assertions ────────────────────────────────────────────────────────────

    /**
     * Returns true when the Add Transaction FAB is visible — confirms we are on the dashboard.
     */
    public boolean isDashboardLoaded() {
        return isElementDisplayed(addTransactionFab);
    }

    public String getAvailableBalance() {
        try {
            return getText(balanceText);
        } catch (Exception e) {
            LogUtil.logWarning("Could not read balance text: " + e.getMessage());
            return null;
        }
    }
}
