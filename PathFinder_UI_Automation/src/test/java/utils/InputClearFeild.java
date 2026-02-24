package utils;

import java.util.logging.Logger;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class InputClearFeild {

    private static final Logger logger =
            Logger.getLogger(InputClearFeild.class.getName());

    private InputClearFeild() {}

    public static void safeClearAndFocus(WebDriver driver, WebElement input) {

        if (driver == null || input == null) {
            throw new IllegalArgumentException("Driver or input element is null");
        }

        JavascriptExecutor js = (JavascriptExecutor) driver;

        logger.info("🔄 Clearing and focusing input field...");

        js.executeScript(
                "arguments[0].scrollIntoView({block:'center'});", input);

        js.executeScript(
                "arguments[0].focus();", input);

        js.executeScript(
                "arguments[0].value='';" +
                "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));",
                input);

        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        logger.info("✅ Input cleared successfully.");
    }
}