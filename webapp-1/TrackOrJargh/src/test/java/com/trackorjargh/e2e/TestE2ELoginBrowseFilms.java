package com.trackorjargh.e2e;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;

public class TestE2ELoginBrowseFilms extends TestE2EBase {

    final static Logger log = getLogger(lookup().lookupClass());

    @Test
    public void testLoginAndBrowseFilms() {
        goToPage("login");
        loginUser("oscar", "1234");

        goToPage("peliculas");
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("elements")), "Films grid not shown", 3);

        List<WebElement> filmLinks = driver.findElements(By.name("nameFilm"));
        assertThat(filmLinks).isNotEmpty();
        String filmName = filmLinks.get(0).getText();
        log.info("Clicking on film: {}", filmName);
        filmLinks.get(0).click();

        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("intro")), "Film detail not shown", 3);

        WebElement introSection = driver.findElement(By.id("intro"));
        assertThat(introSection.isDisplayed()).isTrue();
        log.info("Film detail page loaded for: {}", filmName);

        logout();
    }
}
