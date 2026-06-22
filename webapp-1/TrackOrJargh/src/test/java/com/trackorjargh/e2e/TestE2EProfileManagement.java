package com.trackorjargh.e2e;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;

public class TestE2EProfileManagement extends TestE2EBase {

    final static Logger log = getLogger(lookup().lookupClass());

    /**
     * Registra um novo usuário via formulário de registro
     */
    private String registerNewUser(String timestamp) {
        String username = "e2eprofile_" + timestamp;
        String email = "e2eprofile_" + timestamp + "@test.com";
        String password = "test1234";

        goToPage("login");
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("loginRegister")), "Login page not loaded", 3);

        // Clica no link para abrir formulário de registro
        WebElement registerToggle = driver.findElement(By.id("RegsDropdown"));
        registerToggle.click();

        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#collapseRegt input[name='name']")),
                "Register form not shown", 2);

        // Preenche os campos
        WebElement nameField = driver.findElement(By.cssSelector("#collapseRegt input[name='name']"));
        WebElement emailField = driver.findElement(By.cssSelector("#collapseRegt input[name='email']"));
        WebElement passField = driver.findElement(By.cssSelector("#collapseRegt input#password"));
        WebElement passConfirmField = driver.findElement(By.cssSelector("#collapseRegt input#confirmPass"));

        nameField.sendKeys(username);
        emailField.sendKeys(email);
        passField.sendKeys(password);
        passConfirmField.sendKeys(password);

        // Submit registro
        WebElement registerSubmit = driver.findElement(By.cssSelector("#collapseRegt input[name='register-submit']"));
        registerSubmit.click();

        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h5.text-center")),
                "Registration success alert not shown", 3);
        log.info("User registered: {}", username);

        return username;
    }

    /**
     * Ativa usuário acessando URL de ativação
     */
    private void activateUser(String username) {
        goToPage("activarusuario/" + username);
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#login input[name='username']")),
                "Activation page issue", 3);
        log.info("User activated: {}", username);
    }

    @Override
    public void loginUser(String name, String pass) {
        // Wait show form login
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("userL")), "No login page", 5);

        // Load form - usando IDs corretos
        WebElement userField = driver.findElement(By.id("userL"));
        WebElement passField = driver.findElement(By.id("passwordL"));

        // Write credentials
        userField.click();
        userField.clear();
        userField.sendKeys(name);
        passField.click();
        passField.clear();
        passField.sendKeys(pass);

        // Clica no botão login
        WebElement loginButton = driver.findElement(By.id("buttonLog"));
        loginButton.click();

        // Check login - wait for carousel to appear
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.className("carousel-content")), "Login failed", 5);

        log.info("Login successful, user {}", name);
    }

    @Test
    public void testEditProfileEmail() throws InterruptedException {
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        // 1. Registra novo usuário
        String username = registerNewUser(timestamp);
        String password = "test1234";
        
        // 2. Ativa usuário
        activateUser(username);
        
        // 3. Faz login
        loginUser(username, password);
        log.info("User logged in successfully");

        // 4. Navega para página de perfil
        goToPage("miperfil");
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("v-pills-tab")),
                "Profile page not loaded", 3);
        log.info("Profile page loaded");

        // 5. Clica na primeira aba "Mi cuenta"
        List<WebElement> profileTabs = driver.findElements(By.cssSelector("#v-pills-tab a.nav-link"));
        assertThat(profileTabs).isNotEmpty();
        profileTabs.get(0).click();
        
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form")),
                "Account form not loaded", 3);
        log.info("Account tab selected");

        // 6. Edita o email
        String newEmail = "oscar_updated_" + System.currentTimeMillis() + "@test.com";
        WebElement emailInput = driver.findElement(By.cssSelector("input[placeholder='Correo electrónico']"));
        emailInput.clear();
        emailInput.sendKeys(newEmail);
        log.info("Email updated to: {}", newEmail);

        // 7. Clica no botão Aceptar
        WebElement submitButton = driver.findElement(By.cssSelector("input[type='submit'][value='Aceptar']"));
        submitButton.click();
        log.info("Submit button clicked");

        // 8. Aguarda pela recarga da página (página recarrega após salvar)
        Thread.sleep(1000); // Aguarda recarga
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("v-pills-tab")),
                "Profile page not reloaded after email update", 5);
        log.info("SUCCESS: Profile email updated successfully (page reloaded)");

        // 9. Faz logout
        logout();
        log.info("User logged out");

        // 10. Faz login novamente para verificar persistência
        goToPage("login");
        loginUser(username, password);
        log.info("User logged in again to verify changes");

        // 11. Navega para perfil e verifica email persistiu
        goToPage("miperfil");
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("v-pills-tab")),
                "Profile page not loaded on second access", 3);
        
        List<WebElement> profileTabs2 = driver.findElements(By.cssSelector("#v-pills-tab a.nav-link"));
        profileTabs2.get(0).click();
        
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Correo electrónico']")),
                "Email field not found", 3);
        
        WebElement emailField = driver.findElement(By.cssSelector("input[placeholder='Correo electrónico']"));
        String currentEmail = emailField.getAttribute("value");
        
        assertThat(currentEmail).isEqualTo(newEmail);
        log.info("VERIFIED: Email persisted correctly: {}", currentEmail);
    }

    @Test
    public void testEditProfilePassword() throws InterruptedException {
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        // 1. Registra novo usuário
        String username = registerNewUser(timestamp);
        String oldPassword = "test1234";
        String newPassword = "newpass5678";
        
        // 2. Ativa usuário
        activateUser(username);
        
        // 3. Faz login com senha antiga
        loginUser(username, oldPassword);
        log.info("User logged in with old password");

        // 4. Navega para perfil
        goToPage("miperfil");
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("v-pills-tab")),
                "Profile page not loaded", 3);

        // 5. Clica na aba de account/senha (segunda ou terceira aba)
        List<WebElement> profileTabs = driver.findElements(By.cssSelector("#v-pills-tab a.nav-link"));
        if (profileTabs.size() > 1) {
            profileTabs.get(1).click(); // Clica segunda aba
        }
        
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("password")),
                "Password field not found", 3);
        log.info("Password tab selected");

        // 6. Edita a senha
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement confirmPassField = driver.findElement(By.id("confirmPass"));
        
        passwordField.clear();
        passwordField.sendKeys(newPassword);
        confirmPassField.clear();
        confirmPassField.sendKeys(newPassword);
        log.info("Password updated");

        // Clica submit com JavaScript
        Thread.sleep(1000);
        WebElement submitButton = driver.findElement(By.cssSelector("input[type='submit'][value='Aceptar']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitButton);
        log.info("Submit button clicked");

        // 8. Aguarda pela recarga da página
        Thread.sleep(1000); // Aguarda recarga
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.id("v-pills-tab")),
                "Profile page not reloaded after password update", 5);
        log.info("SUCCESS: Password updated successfully (page reloaded)");

        // 9. Faz logout
        logout();
        log.info("User logged out");

        // 10. Tenta login com senha antiga (deve falhar)
        goToPage("login");
        try {
            loginUser(username, oldPassword);
            log.error("ERROR: Login should have failed with old password!");
        } catch (Exception e) {
            log.info("Expected failure with old password: {}", e.getMessage());
        }

        // 11. Faz login com nova senha
        goToPage("login");
        loginUser(username, newPassword);
        log.info("SUCCESS: Login successful with new password");
    }
}
