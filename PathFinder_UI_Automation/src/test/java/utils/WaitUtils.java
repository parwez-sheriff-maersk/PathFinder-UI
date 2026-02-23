package utils;

import java.time.Duration;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WaitUtils {

    public static boolean waitForSpinnerToDisappear(WebDriver driver, String spinnerXpath, long timeoutMillis, Logger logger) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(timeoutMillis));
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(spinnerXpath)));
        } catch (Exception e) {
            logger.warning("⚠️ Spinner did not disappear in time: " + e.getMessage());
            return false;
        }
    }

    public static WebElement waitForElementVisible(WebDriver driver, By locator, int timeoutSeconds, Logger logger) {
        logger.info("⏳ Waiting for element to be visible: " + locator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForElementClickable(WebDriver driver, By locator, int timeoutSeconds, Logger logger) {
        logger.info("⏳ Waiting for element to be clickable: " + locator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static boolean waitForTextToBePresent(WebDriver driver, By locator, String text, int timeoutSeconds, Logger logger) {
        logger.info("⏳ Waiting for text '" + text + "' to be present in element: " + locator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    public static boolean waitForElementToDisappear(WebDriver driver, By locator, int timeoutSeconds, Logger logger) {
        logger.info("⏳ Waiting for element to disappear: " + locator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public static WebElement waitTill(WebDriver driver, By locator, int timeoutSeconds, Logger logger) {
        logger.info("⏳ Waiting for element using waitTill: " + locator);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
}
