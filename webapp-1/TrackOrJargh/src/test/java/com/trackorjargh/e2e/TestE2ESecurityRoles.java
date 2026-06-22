package com.trackorjargh.e2e;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;

public class TestE2ESecurityRoles extends TestE2EBase {

    final static Logger log = getLogger(lookup().lookupClass());

    @Test
    public void testAuthenticationFailureHandler() {
        goToPage("login");
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#login input[name='username']")),
                "Login page not loaded", 3);

        // 1. Login com usuário inexistente → RNF16: "El usuario introducido no existe"
        WebElement userField = driver.findElement(By.cssSelector("#login input[name='username']"));
        WebElement passField = driver.findElement(By.cssSelector("#login input[name='password']"));
        userField.clear();
        userField.sendKeys("nao_existe");
        passField.clear();
        passField.sendKeys("qualquer");
        passField.sendKeys(Keys.ENTER);

        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h5.text-center")),
                "Error message not shown for non-existent user", 3);
        assertThat(driver.findElement(By.cssSelector("h5.text-center")).getText())
                .contains("El usuario introducido no existe");
        log.info("RNF16 OK: non-existent user error message displayed");

        // 2. Login com senha errada para usuário existente → RNF16: "Contrasena invalida"
        goToPage("login");
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#login input[name='username']")),
                "Login page not loaded", 3);

        userField = driver.findElement(By.cssSelector("#login input[name='username']"));
        passField = driver.findElement(By.cssSelector("#login input[name='password']"));
        userField.clear();
        userField.sendKeys("oscar");
        passField.clear();
        passField.sendKeys("senha_errada");
        passField.sendKeys(Keys.ENTER);

        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h5.text-center")),
                "Error message not shown for wrong password", 3);
        assertThat(driver.findElement(By.cssSelector("h5.text-center")).getText())
                .contains("Contraseña");
        log.info("RNF16 OK: wrong password error message displayed");
    }

    @Test
    public void testRoleBasedAccessControl() {
        // 1. ROLE_USER (oscar) → NÃO deve acessar /administracion
        goToPage("login");
        loginUser("oscar", "1234");

        driver.get(sutUrl + "administracion");

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThat(driver.findElements(By.id("adminTab")))
                .as("ROLE_USER should not see adminTab on /administracion")
                .isEmpty();
        assertThat(driver.findElement(By.tagName("body")).getText())
                .contains("ERROR");
        log.info("RNF12 OK: ROLE_USER blocked from /administracion (error page shown)");

        // Navega para homepage para verificar sessão intacta (error.html não tem nav-menu)
        driver.get(sutUrl);
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.className("carousel-content")),
                "Homepage not loaded", 3);

        WebElement menuBar = driver.findElement(By.cssSelector("ul.nav-menu"));
        List<WebElement> navItems = menuBar.findElements(By.tagName("a"));
        assertThat(navItems.get(navItems.size() - 1).getText())
                .as("User should still be logged in after 403")
                .isEqualToIgnoringCase("logout");
        logout();

        // 2. ROLE_ADMIN (jesus) → DEVE acessar /administracion
        goToPage("login");
        loginUser("jesus", "1234");

        driver.get(sutUrl + "administracion");
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("report")),
                "Admin page not loaded for admin user", 3);

        List<WebElement> adminTabs = driver.findElements(By.cssSelector("#adminTab a.nav-link"));
        assertThat(adminTabs)
                .as("ROLE_ADMIN should see 4 admin tabs")
                .hasSize(4);
        log.info("RNF12 OK: ROLE_ADMIN accessed /administracion with 4 tabs");

        logout();
    }
}
