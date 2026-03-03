package utils;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.logging.Logger;

public class Login {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final Logger logger = Logger.getLogger(Login.class.getName());

    // Locators
    private final By emailField = By.name("loginfmt");
    private final By nextButton = By.id("idSIButton9");
    private final By passwordField = By.id("i0118");
    private final By yesButton = By.id("idSIButton9");
    private final By accessToMaerskEmployeesText =
            By.cssSelector("button.login-page__btn");
    private final By myRequestsText =
            By.xpath("//*[text()='My Requests']");

    // Constructor
    public Login(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void enterEmail(String email) {
        driver.findElement(accessToMaerskEmployeesText).click();

        logger.info("Entering email: " + email);
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(emailField));
        emailInput.clear();
        emailInput.sendKeys(email);
        safeClick(nextButton, "Next (after email)");
    }

    public void enterPassword(String password) {
        logger.info("Entering password.");
        WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordField));
        passwordInput.clear();
        passwordInput.sendKeys(password);
        safeClick(nextButton, "Next (after password)");
        WebElement Yes = wait.until(ExpectedConditions.visibilityOfElementLocated(yesButton));
        Yes.click();
    }

    public boolean isLoginSuccessful() {
        try {
            driver.switchTo().defaultContent();
            wait.until(ExpectedConditions.visibilityOfElementLocated(myRequestsText));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private void safeClick(By locator, String label) {
        try {
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
            try {
                el.click();
            } catch (ElementClickInterceptedException e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
            }
            logger.info("Clicked: " + label);
        } catch (TimeoutException te) {
            throw te;
        }
    }
}