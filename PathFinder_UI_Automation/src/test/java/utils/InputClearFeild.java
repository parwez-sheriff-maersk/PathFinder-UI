package utils;

import java.util.logging.Logger;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
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

        // Step 1: Scroll into view
        js.executeScript(
                "arguments[0].scrollIntoView({block:'center'});", input);

        // Step 2: Click to focus (triggers component's focus handler)
        js.executeScript("arguments[0].click();", input);

        try { Thread.sleep(300); } catch (InterruptedException ignored) {}

        // Step 3: Select all text using Ctrl+A, then Delete
        // This triggers the component's native input/change events properly
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));

        try { Thread.sleep(200); } catch (InterruptedException ignored) {}

        input.sendKeys(Keys.DELETE);

        try { Thread.sleep(300); } catch (InterruptedException ignored) {}

        // Step 4: Also clear via JS as backup + dispatch events
        js.executeScript(
                "arguments[0].value='';" +
                "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));" +
                "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));",
                input);

        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        // Step 5: Focus again for sendKeys
        js.executeScript("arguments[0].focus();", input);

        try { Thread.sleep(200); } catch (InterruptedException ignored) {}

        logger.info("✅ Input cleared successfully.");
    }
}
