package utils;

import org.openqa.selenium.*;
import java.io.File;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenshotUtils {

    public static String captureFullPage(WebDriver driver, String fileName) {

        try {

            File src = ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.FILE);

            String timestamp =
                    new SimpleDateFormat("yyyyMMdd_HHmmss")
                            .format(new Date());

            String folderPath =
                    ExtentManager.runFolder + "/screenshots";

            Files.createDirectories(Paths.get(folderPath));

            String filePath =
                    folderPath + "/" + fileName + "_" + timestamp + ".png";

            Files.copy(src.toPath(), Paths.get(filePath));

            return filePath;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}