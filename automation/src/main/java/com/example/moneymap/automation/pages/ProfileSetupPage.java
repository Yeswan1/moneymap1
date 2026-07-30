package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * ProfileSetupPage - Page Object for student/employee/homemaker/general setup screens.
 */
public class ProfileSetupPage extends BasePage {

    private final By nameField = By.xpath("//android.widget.EditText[1]");
    private final By incomeOrPocketMoneyField = By.xpath("//android.widget.EditText[2]");
    private final By institutionOrCompanyField = By.xpath("//android.widget.EditText[3]");
    private final By nextButton = By.xpath(
            "//*[@text='Next ->' or @text='Next' or @content-desc='Next']");
    private final By backButton = By.xpath(
            "//*[@content-desc='Back' or @resource-id='com.example.moneymap:id/btn_back']");

    public ProfileSetupPage(AndroidDriver driver) {
        super(driver);
    }

    public void setupStudentProfile(String name, String pocketMoney, String college) {
        try {
            clearAndType(nameField, name);
            clearAndType(incomeOrPocketMoneyField, pocketMoney);
            clearAndType(institutionOrCompanyField, college);
        } catch (Exception e) {
            // Compose-based fields
            By composeInput = By.className("android.widget.EditText");
            java.util.List<org.openqa.selenium.WebElement> fields = findElements(composeInput);
            if (fields.size() > 0) fields.get(0).sendKeys(name);
            if (fields.size() > 1) fields.get(1).sendKeys(pocketMoney);
            if (fields.size() > 2) fields.get(2).sendKeys(college);
        }
        click(nextButton);
    }

    public void setupProfessionalProfile(String name, String salary, String company) {
        try {
            clearAndType(nameField, name);
            clearAndType(incomeOrPocketMoneyField, salary);
            clearAndType(institutionOrCompanyField, company);
        } catch (Exception ignored) {}
        click(nextButton);
    }

    public void setupHomemakerProfile(String name, String budget) {
        try {
            clearAndType(nameField, name);
            clearAndType(incomeOrPocketMoneyField, budget);
        } catch (Exception ignored) {}
        click(nextButton);
    }

    public void setupGeneralProfile(String name, String income) {
        try {
            clearAndType(nameField, name);
            clearAndType(incomeOrPocketMoneyField, income);
        } catch (Exception ignored) {}
        click(nextButton);
    }

    public boolean isSetupScreenDisplayed() {
        return isTextVisible("Set up Profile") || isTextVisible("Profile");
    }
}
