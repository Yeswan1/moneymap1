package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * HistoryPage - Page Object for Transaction History / Wallet tab.
 */
public class HistoryPage extends BasePage {

    private final By searchIcon = By.xpath(
            "//*[contains(@content-desc,'Search') or contains(@text,'Search')]");
    private final By filterIcon = By.xpath(
            "//*[contains(@content-desc,'Filter') or contains(@content-desc,'filter')]");
    private final By categoriesLink = byText("Categories");
    private final By subscriptionsLink = byText("Subscriptions");
    private final By transactionItems = By.xpath(
            "//android.widget.LinearLayout[contains(@resource-id,'transaction')]" +
            " | //*[contains(@class,'LazyColumn')]//android.view.ViewGroup");
    private final By backButton = By.xpath("//*[@content-desc='Back']");

    public HistoryPage(AndroidDriver driver) {
        super(driver);
    }

    public boolean isHistoryScreenDisplayed() {
        return isTextVisible("History") || isTextVisible("Wallet") || isTextVisible("Transactions");
    }

    public void clickSearchIcon() {
        click(searchIcon);
    }

    public void clickFilterIcon() {
        click(filterIcon);
    }

    public void tapTransaction(int index) {
        try {
            List<WebElement> items = findElements(transactionItems);
            if (index < items.size()) {
                items.get(index).click();
            }
        } catch (Exception e) {
            // Fallback: tap first visible transaction
            click(transactionItems);
        }
    }

    public void tapTransactionByName(String name) {
        clickByText(name);
    }

    public int getTransactionCount() {
        return findElements(transactionItems).size();
    }

    public void clickBack() {
        try { click(backButton); } catch (Exception e) { pressBack(); }
    }

    public void clickCategories() {
        click(categoriesLink);
    }

    public void clickSubscriptions() {
        click(subscriptionsLink);
    }
}
