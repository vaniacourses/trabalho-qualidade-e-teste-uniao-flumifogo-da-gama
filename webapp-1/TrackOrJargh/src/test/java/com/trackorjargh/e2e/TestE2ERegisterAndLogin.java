package com.trackorjargh.e2e;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;

public class TestE2ERegisterAndLogin extends TestE2EBase {

    final static Logger log = getLogger(lookup().lookupClass());

    @Test
    public void testRegisterAndLogin() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String username = "e2euser_" + timestamp;
        String email = "e2e_" + timestamp + "@test.com";
        String password = "test1234";

        goToPage("login");
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("loginRegister")), "Login page not loaded", 3);

        WebElement registerToggle = driver.findElement(By.id("RegsDropdown"));
        registerToggle.click();

        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#collapseRegt input[name='name']")),
                "Register form not shown", 2);

        WebElement nameField = driver.findElement(By.cssSelector("#collapseRegt input[name='name']"));
        WebElement emailField = driver.findElement(By.cssSelector("#collapseRegt input[name='email']"));
        WebElement passField = driver.findElement(By.cssSelector("#collapseRegt input#password"));
        WebElement passConfirmField = driver.findElement(By.cssSelector("#collapseRegt input#confirmPass"));

        nameField.sendKeys(username);
        emailField.sendKeys(email);
        passField.sendKeys(password);
        passConfirmField.sendKeys(password);

        WebElement registerSubmit = driver.findElement(By.cssSelector("#collapseRegt input[name='register-submit']"));
        registerSubmit.click();

        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h5.text-center")),
                "Registration success alert not shown", 3);

        log.info("User registered successfully: {}", username);

        goToPage("activarusuario/" + username);
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#login input[name='username']")),
                "Activation page not loaded", 3);
        log.info("User activated: {}", username);

        loginUser(username, password);

        logout();
    }
}
