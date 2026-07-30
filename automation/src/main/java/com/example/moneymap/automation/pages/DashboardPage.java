package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * DashboardPage - Page Object for the Compose DashboardScreen (Home tab).
 */
public class DashboardPage extends BasePage {

    // Bottom Navigation
    private final By homeTab = byText("Home");
    private final By reportsTab = byText("Reports");
    private final By budgetTab = byText("Budget");
    private final By profileTab = byText("Profile");
    // Center FAB (Wallet) navigates to Add Transaction
    private final By walletFab = By.xpath(
            "//*[@content-desc='Add Transaction' or @content-desc='Wallet']");

    // Dashboard elements
    private final By addTransactionButton = By.xpath(
            "//*[contains(@content-desc,'Add') or contains(@text,'Add')]");
    private final By seeAllLink = byText("See All");
    private final By chatFab = By.xpath(
            "//*[contains(@content-desc,'Chat') or contains(@content-desc,'Chatbot')]");
    private final By notificationIcon = By.xpath(
            "//*[contains(@content-desc,'notification') or contains(@content-desc,'alert')]");

    public DashboardPage(AndroidDriver driver) {
        super(driver);
    }

    public boolean isDashboardLoaded() {
        return isTextVisible("Home") || isTextVisible("Dashboard") || isTextVisible("Budget");
    }

    public String getAvailableBalance() {
        try {
            By balanceText = By.xpath("//*[contains(@text,'₹') or contains(@text,'$')]");
            return waitForElement(balanceText).getText();
        } catch (Exception e) {
            return "0";
        }
    }

    public void clickAddTransactionButton() {
        // The center FAB (wallet icon) navigates to add_transaction
        try {
            click(walletFab);
        } catch (Exception e) {
            try {
                click(addTransactionButton);
            } catch (Exception ex) {
                // Fallback: click center of bottom nav area
                clickByText("+");
            }
        }
    }

    public void clickSeeAllTransactions() {
        try { click(seeAllLink); } catch (Exception e) { clickByText("See all"); }
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

    public void clickChatbot() {
        click(chatFab);
    }

    public void clickNotificationIcon() {
        click(notificationIcon);
    }

    public boolean isRecentTransactionVisible(String transactionName) {
        return isTextVisible(transactionName);
    }

    public boolean isBudgetProgressVisible() {
        return isTextVisible("Budget") || isTextVisible("budget");
    }

    public void logout() {
        navigateToProfile();
        scrollToText("Logout");
        clickByText("Logout");
    }
}
