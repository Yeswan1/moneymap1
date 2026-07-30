package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * ProfileSetupPage - Page Object for student/employee/homemaker/general setup screens.
 */
public class ProfileSetupPage extends BasePage {

    private final By nameField = By.xpath(
            "//android.widget.EditText[@text='Full Name' or @hint='Full Name' or contains(@text, 'Name')]");
    private final By pocketMoneyField = By.xpath(
            "//android.widget.EditText[@text='Monthly Allowance' or @hint='Monthly Allowance' or contains(@text, 'Allowance')]");
    private final By incomeField = By.xpath(
            "//android.widget.EditText[@text='Monthly Income' or @hint='Monthly Income' or contains(@text, 'Income')]");
    private final By budgetField = By.xpath(
            "//android.widget.EditText[@text='Monthly Household Budget' or @hint='Monthly Household Budget' or contains(@text, 'Budget')]");
    private final By collegeField = By.xpath(
            "//android.widget.EditText[@text='School/College Name' or @hint='School/College Name' or contains(@text, 'College') or contains(@text, 'School')]");
    private final By companyField = By.xpath(
            "//android.widget.EditText[@text='Company Name' or @hint='Company Name' or contains(@text, 'Company')]");

    private final By nextButton = By.xpath(
            "//*[@text='Next ->' or @text='Next' or @content-desc='Next' or contains(@text, 'Continue')]");
    private final By backButton = By.xpath(
            "//*[@content-desc='Back' or @resource-id='com.example.moneymap:id/btn_back']");

    public ProfileSetupPage(AndroidDriver driver) {
        super(driver);
    }

    public void setupStudentProfile(String name, String pocketMoney, String college) {
        clearAndType(nameField, name);
        clearAndType(pocketMoneyField, pocketMoney);
        clearAndType(collegeField, college);
        click(nextButton);
    }

    public void setupProfessionalProfile(String name, String salary, String company) {
        clearAndType(nameField, name);
        clearAndType(incomeField, salary);
        clearAndType(companyField, company);
        click(nextButton);
    }

    public void setupHomemakerProfile(String name, String budget) {
        clearAndType(nameField, name);
        clearAndType(budgetField, budget);
        click(nextButton);
    }

    public void setupGeneralProfile(String name, String income) {
        clearAndType(nameField, name);
        clearAndType(incomeField, income);
        click(nextButton);
    }

    public boolean isSetupScreenDisplayed() {
        return isElementPresent(nameField);
    }
}
