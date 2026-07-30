package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * RoleSelectionPage - Page Object for the Compose RoleSelectionScreen.
 * Also handles legacy RoleSelectionActivity (btn_student, btn_employee, etc.)
 */
public class RoleSelectionPage extends BasePage {

    private final By studentButton = By.xpath(
            "//*[@resource-id='com.example.moneymap:id/btn_student' " +
            "or @text='Student' or @content-desc='Student']");
    private final By employeeButton = By.xpath(
            "//*[@resource-id='com.example.moneymap:id/btn_employee' " +
            "or @text='Employee' or @content-desc='Employee']");
    private final By homemakerButton = By.xpath(
            "//*[@resource-id='com.example.moneymap:id/btn_homemaker' " +
            "or @text='Homemaker' or @content-desc='Homemaker']");
    private final By freelancerButton = By.xpath(
            "//*[@resource-id='com.example.moneymap:id/btn_freelancer' " +
            "or @text='Freelancer' or @content-desc='Freelancer']");
    private final By generalButton = By.xpath(
            "//*[@resource-id='com.example.moneymap:id/btn_general' " +
            "or @text='General' or @content-desc='General']");
    private final By continueButton = By.xpath(
            "//*[@resource-id='com.example.moneymap:id/continue_button' " +
            "or @text='Continue' or @content-desc='Continue']");

    public RoleSelectionPage(AndroidDriver driver) {
        super(driver);
    }

    public void selectStudent() {
        click(studentButton);
    }

    public void selectProfessional() {
        try { click(employeeButton); }
        catch (Exception e) { clickByText("Employee"); }
    }

    public void selectHomemaker() {
        click(homemakerButton);
    }

    public void selectFreelancer() {
        click(freelancerButton);
    }

    public void selectGeneral() {
        click(generalButton);
    }

    public void clickContinue() {
        click(continueButton);
    }

    public boolean isRoleSelectionDisplayed() {
        return isTextVisible("Who are you?") || isTextVisible("Who are you");
    }
}
