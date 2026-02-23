package utils;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Hooks {
    private final TestContext testContext;

    public Hooks(TestContext context) {
        this.testContext = context;
    }

    @Before
    public void setUp() throws IOException {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();

        testContext.setDriver(driver);

        Properties props = new Properties();
        InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
        if (input != null) {
            props.load(input);
        }
        testContext.setProperties(props);
    }

    @After
    public void tearDown() {
        if (testContext.getDriver() != null) {
            testContext.getDriver().quit();
        }
    }
}
