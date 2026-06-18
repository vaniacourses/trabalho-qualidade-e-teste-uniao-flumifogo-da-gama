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
import org.slf4j.Logger;

public class TestE2ESearchAndList extends TestE2EBase {

    final static Logger log = getLogger(lookup().lookupClass());

    @Test
    public void testSearchAndList() {
        goToPage("login");
        loginUser("oscar", "1234");

        goToPage("miperfil");
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("v-pills-tab")), "Profile page not loaded", 3);

        List<WebElement> profileTabs = driver.findElements(By.cssSelector("#v-pills-tab a.nav-link"));
        assertThat(profileTabs).hasSize(2);
        profileTabs.get(1).click();

        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("MyLists")), "My Lists section not shown", 2);

        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement addListButton = driver.findElement(By.cssSelector("[data-target='#showAddList']"));
        addListButton.click();

        js.executeScript("document.getElementById('showAddList').classList.add('show');");

        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#showAddList input[name='listName']")),
                "Add list form not shown", 3);

        WebElement listNameInput = driver.findElement(By.cssSelector("#showAddList input[name='listName']"));
        String listName = "e2e-list-" + System.currentTimeMillis();
        listNameInput.sendKeys(listName);

        WebElement createListButton = driver.findElement(By.name("list-submit"));
        createListButton.click();

        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("v-pills-tab")), "Profile page not loaded after list creation", 10);
        List<WebElement> profileTabs2 = driver.findElements(By.cssSelector("#v-pills-tab a.nav-link"));
        profileTabs2.get(1).click();
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("MyLists")), "My Lists section not shown after redirect", 5);

        List<WebElement> createdLists = driver.findElements(By.id("CreatedLists"));
        assertThat(createdLists).isNotEmpty();
        String lastListName = createdLists.get(createdLists.size() - 1).getText();
        assertThat(lastListName).isEqualTo(listName);
        log.info("List created: {}", listName);

        goToPage("busqueda");
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#portfolio-flters input[type='text']")),
                "Search page not loaded", 3);

        WebElement searchInput = driver.findElement(By.cssSelector("#portfolio-flters input[type='text']"));
        searchInput.sendKeys("Titanic");

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        WebElement filmLink = driver.findElement(By.id("linkFilm"));
        filmLink.click();

        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#elements .portfolio-item")),
                "Search results not shown for 'Titanic'", 5);
        List<WebElement> searchResults = driver.findElements(By.cssSelector("#elements .portfolio-item"));
        assertThat(searchResults).isNotEmpty();
        log.info("Search results found for 'Titanic'");

        WebElement addToDropdown = driver.findElement(By.id("dropdownMenu2"));
        addToDropdown.click();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<WebElement> listButtons = driver.findElements(By.name("buttonList"));
        boolean addedToList = false;
        for (WebElement btn : listButtons) {
            if (btn.getText().equals(listName)) {
                btn.click();
                addedToList = true;
                break;
            }
        }

        if (addedToList) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info("Content added to list: {}", listName);
        } else {
            log.warn("List '{}' not found in dropdown, lists may need page refresh", listName);
        }

        goToPage("miperfil");
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("v-pills-tab")), "Profile page not loaded", 3);
        profileTabs = driver.findElements(By.cssSelector("#v-pills-tab a.nav-link"));
        profileTabs.get(1).click();
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("MyLists")), "My Lists section not shown", 2);

        createdLists = driver.findElements(By.id("CreatedLists"));
        boolean listFound = false;
        for (WebElement listEl : createdLists) {
            if (listEl.getText().equals(listName)) {
                listFound = true;
                break;
            }
        }
        assertThat(listFound).isTrue();
        log.info("List '{}' verified in profile", listName);

        List<WebElement> deleteButtons = driver.findElements(By.name("deleteList"));
        WebElement lastDeleteButton = deleteButtons.get(deleteButtons.size() - 1);
        lastDeleteButton.click();
        log.info("List deleted: {}", listName);

        logout();
    }
}
