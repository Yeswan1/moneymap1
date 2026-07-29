package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class RoleSelectionPage extends BasePage {

    private final By studentOption = byText("Student");
    private final By professionalOption = byText("Employee");
    private final By homemakerOption = byText("Homemaker");
    private final By employeeOption = byText("Employee");
    private final By generalOption = byText("General");
    private final By continueBtn = byText("Continue");

    public RoleSelectionPage(AndroidDriver driver) {
        super(driver);
    }

    public void selectStudent() {
        click(studentOption);
    }

    public void selectProfessional() {
        click(professionalOption);
    }

    public void selectHomemaker() {
        click(homemakerOption);
    }

    public void selectEmployee() {
        click(employeeOption);
    }

    public void selectGeneral() {
        click(generalOption);
    }

    public void clickContinue() {
        click(continueBtn);
    }
}
