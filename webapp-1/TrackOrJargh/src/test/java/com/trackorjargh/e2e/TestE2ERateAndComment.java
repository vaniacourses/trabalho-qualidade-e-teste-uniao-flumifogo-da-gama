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

public class TestE2ERateAndComment extends TestE2EBase {

    final static Logger log = getLogger(lookup().lookupClass());

    @Test
    public void testRateAndComment() {
        goToPage("login");
        loginUser("alfonso", "1234");

        String showName = "The Big Bang Theory";
        driver.get(sutUrl + "serie/" + java.net.URLEncoder.encode(showName, java.nio.charset.StandardCharsets.UTF_8));

        JavascriptExecutor js = (JavascriptExecutor) driver;
        waitUntil(ExpectedConditions.presenceOfElementLocated(By.id("call-to-action")),
                "Call-to-action section not found in DOM", 10);

        js.executeScript("document.getElementById('call-to-action').scrollIntoView({block: 'center'});");
        log.info("Show detail page loaded: {}", showName);

        js.executeScript("$('#rateYo').rateYo('rating', 4);");
        log.info("Rated show with 4 stars");

        js.executeScript("document.getElementById('contact').scrollIntoView({block: 'center'});");

        String commentText = "E2E test comment - " + System.currentTimeMillis();
        String encodedText = java.net.URLEncoder.encode(commentText, java.nio.charset.StandardCharsets.UTF_8);
        driver.get(driver.getCurrentUrl() + "?messageSent=" + encodedText);

        waitUntil(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#contact .comment p.description")),
                "Comments section not found after submission", 5);

        List<WebElement> comments = driver.findElements(By.cssSelector("#contact .comment p.description"));
        boolean commentFound = false;
        for (WebElement comment : comments) {
            if (comment.getText().contains(commentText)) {
                commentFound = true;
                break;
            }
        }
        assertThat(commentFound).isTrue();
        log.info("Comment verified: {}", commentText);

        logout();
    }
}
