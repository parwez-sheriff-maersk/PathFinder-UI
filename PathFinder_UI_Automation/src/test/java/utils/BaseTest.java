package utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;

public class BaseTest {
    public static WebDriver driver;

   

    public void getReady() {
        if (driver == null) {
            WebDriverManager.chromedriver().setup(); 
            driver = new ChromeDriver();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.manage().window().maximize();
        }
    }


   

    public static void tearDown() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
