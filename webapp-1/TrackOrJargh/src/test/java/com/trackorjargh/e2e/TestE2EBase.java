package com.trackorjargh.e2e;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

public class TestE2EBase extends ElastestBaseTest {

    final static Logger log = getLogger(lookup().lookupClass());

    @BeforeAll
    public static void setupE2E() {
        sutUrl = "http://localhost:8000/";
        log.info("sutUrl set to: {}", sutUrl);
    }

    public void goToPage() {
        driver.get(sutUrl);
    }

    public void goToPage(String page) {
        driver.get(sutUrl + page);
    }

    public void waitUntil(ExpectedCondition<WebElement> expectedCondition, String errorMessage, int seconds) {
        WebDriverWait waiter = new WebDriverWait(driver, Duration.ofSeconds(seconds));
        try {
            waiter.until(expectedCondition);
        } catch (org.openqa.selenium.TimeoutException timeout) {
            log.error(errorMessage);
            throw new org.openqa.selenium.TimeoutException(
                    "\"" + errorMessage + "\" (checked with condition) > " + timeout.getMessage());
        }
    }

    public void loginUser(String name, String pass) {
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#login input[name='username']")),
                "No login page", 5);

        WebElement userField = driver.findElement(By.cssSelector("#login input[name='username']"));
        WebElement passField = driver.findElement(By.cssSelector("#login input[name='password']"));

        userField.click();
        userField.clear();
        userField.sendKeys(name);
        passField.click();
        passField.clear();
        passField.sendKeys(pass);
        passField.sendKeys(Keys.ENTER);

        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.className("carousel-content")),
                "Login failed for user: " + name, 5);

        WebElement menuBar = driver.findElement(By.cssSelector("ul.nav-menu"));
        List<WebElement> elementsBar = menuBar.findElements(By.tagName("a"));
        WebElement lastItem = elementsBar.get(elementsBar.size() - 1);
        assertThat(lastItem.getText()).isEqualToIgnoringCase("logout");
        WebElement userElement = elementsBar.get(elementsBar.size() - 2);
        assertThat(userElement.getText()).isEqualToIgnoringCase(name);

        log.info("Login successful, user {}", name);
    }

    public void logout() {
        driver.get(sutUrl + "logout");
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.nav-menu a[href='/login']")),
                "Logout redirect failed", 5);

        WebElement menuBar = driver.findElement(By.cssSelector("ul.nav-menu"));
        List<WebElement> elementsBar = menuBar.findElements(By.tagName("a"));
        WebElement loginButton = elementsBar.get(elementsBar.size() - 1);
        assertThat(loginButton.getText()).isEqualToIgnoringCase("login");

        log.info("Logout successful");
    }
}
