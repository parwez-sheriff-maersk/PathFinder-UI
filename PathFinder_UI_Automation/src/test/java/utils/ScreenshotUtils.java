package utils;

import org.openqa.selenium.*;
import java.util.Base64;

public class ScreenshotUtils {

    /**
     * Captures a screenshot and returns it as a Base64 string.
     * No file is written — screenshot is embedded directly into ExtentReport HTML.
     */
    public static String captureBase64(WebDriver driver) {
        try {
            byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
