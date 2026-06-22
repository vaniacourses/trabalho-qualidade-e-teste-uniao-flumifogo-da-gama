package com.trackorjargh.e2e;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;

public class TestE2EAdminCreateDelete extends TestE2EBase {

    final static Logger log = getLogger(lookup().lookupClass());

    @Test
    public void testAdminCreateAndDeleteBook() {
        goToPage("login");
        loginUser("jesus", "1234");

        String bookName = "E2E Test Book " + System.currentTimeMillis();
        String bookAuthors = "E2E Author";
        String bookYear = "2024";
        String bookSynopsis = "Book created by E2E Selenium test";

        goToPage("subirContenido");
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("report")), "Upload page not loaded", 3);

        JavascriptExecutor js = (JavascriptExecutor) driver;
        List<WebElement> uploadTabs = driver.findElements(By.cssSelector("#adminTab a.nav-link"));
        assertThat(uploadTabs).hasSize(3);
        js.executeScript("document.querySelectorAll('#adminTab a')[2].click();");

        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#books.tab-pane.active")),
                "Books tab not active in upload page", 3);

        js.executeScript("document.querySelector('#books input[type=file]').removeAttribute('required');");

        WebElement nameInput = driver.findElement(By.cssSelector("#books input[name='newName']"));
        nameInput.sendKeys(bookName);

        WebElement authorsInput = driver.findElement(By.cssSelector("#books input[name='authors']"));
        authorsInput.sendKeys(bookAuthors);

        WebElement yearInput = driver.findElement(By.cssSelector("#books input[name='year']"));
        yearInput.clear();
        yearInput.sendKeys(bookYear);

        WebElement synopsisTextarea = driver.findElement(By.cssSelector("#books textarea[name='synopsis']"));
        synopsisTextarea.sendKeys(bookSynopsis);

        WebElement submitButton = driver.findElement(By.cssSelector("#books input[name='filmModSubmit']"));
        submitButton.click();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("Book creation form submitted: {}", bookName);

        goToPage("administracion");
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("report")), "Admin page not loaded", 3);

        List<WebElement> adminTabs = driver.findElements(By.cssSelector("#adminTab a.nav-link"));
        assertThat(adminTabs).hasSize(4);
        adminTabs.get(3).click();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        WebElement activePanel = driver.findElement(By.cssSelector("#books.tab-pane.active"));
        Select bookDropdown = new Select(activePanel.findElement(By.tagName("select")));
        bookDropdown.selectByVisibleText(bookName);

        activePanel.findElement(By.cssSelector("button[name='targetBook']")).click();

        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='confirmDelete']")),
                "Edit form not loaded", 3);

        WebElement deleteCheckbox = driver.findElement(By.cssSelector("input[name='confirmDelete']"));
        if (!deleteCheckbox.isSelected()) {
            deleteCheckbox.click();
        }

        WebElement sendButton = driver.findElement(By.cssSelector("input[name='filmModSubmit']"));
        sendButton.click();

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("Book delete form submitted: {}", bookName);

        goToPage("administracion");
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("report")), "Admin page not loaded", 3);

        adminTabs = driver.findElements(By.cssSelector("#adminTab a.nav-link"));
        adminTabs.get(3).click();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        activePanel = driver.findElement(By.cssSelector("#books.tab-pane.active"));
        Select verifyDropdown = new Select(activePanel.findElement(By.tagName("select")));
        List<WebElement> options = verifyDropdown.getOptions();
        boolean bookGone = true;
        for (WebElement option : options) {
            if (option.getText().equals(bookName)) {
                bookGone = false;
                break;
            }
        }
        assertThat(bookGone).isTrue();
        log.info("Book '{}' successfully deleted", bookName);

        logout();
    }
}
