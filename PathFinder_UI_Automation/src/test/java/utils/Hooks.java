package utils;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

public class Hooks {

    private final TestContext testContext;

    public Hooks(TestContext context) {
        this.testContext = context;
    }

    // ============================================================
    // BEFORE SCENARIO
    // ============================================================

    @Before
    public void setUp(Scenario scenario) throws IOException {

        // ===== EXISTING DRIVER SETUP (UNCHANGED) =====
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();

        testContext.setDriver(driver);

        Properties props = new Properties();
        InputStream input =
                getClass().getClassLoader()
                        .getResourceAsStream("config.properties");

        if (input != null) {
            props.load(input);
        }

        testContext.setProperties(props);
        // =================================================

        // ===== EXTENT REPORT INITIALIZATION =====
        ExtentReports extent = ExtentManager.getInstance();

        ExtentTest test = extent.createTest(scenario.getName())
                .assignAuthor("Parwez Sheriff")
                .assignCategory("UI Automation");

        ExtentTestManager.setTest(test);

        test.info("Test Started: " + scenario.getName());
    }

    // ============================================================
    // AFTER SCENARIO
    // ============================================================

    @After
    public void tearDown(Scenario scenario) {

        try {

            ExtentTest test = ExtentTestManager.getTest();
            WebDriver driver = testContext.getDriver();

            boolean isPassed = !scenario.isFailed();

            // ========================================================
            // 🔥 NEW: TRACK FEATURE-WISE RESULTS (ADDED)
            // ========================================================

            String featureName = scenario.getUri() != null
                    ? scenario.getUri().toString()
                    : "Unknown Feature";

            ExtentManager.trackResult(featureName, isPassed);

            // ========================================================
            // EXISTING STATUS LOGIC (UNCHANGED)
            // ========================================================

            if (scenario.isFailed()) {

                test.fail("❌ Scenario Failed");
                test.fail("Failure Status: " + scenario.getStatus());

                // 🔥 Take screenshot ONLY if not already taken
                if (!ExtentTestManager.isScreenshotTaken()) {

                    String screenshotPath =
                            ScreenshotUtils.captureFullPage(
                                    driver,
                                    scenario.getName().replaceAll(" ", "_")
                            );

                    if (screenshotPath != null) {
                        test.addScreenCaptureFromPath(
                                screenshotPath,
                                "Failure Screenshot"
                        );
                    }
                }

            } else {

                test.pass("✅ Scenario Passed Successfully");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // ========================================================
            // 🔥 NEW: ADD ENTERPRISE DASHBOARD (ADDED)
            // ========================================================
            ExtentManager.addDashboard();

            // Flush report (EXISTING — UNCHANGED)
            ExtentManager.getInstance().flush();

            // ===== EXISTING DRIVER QUIT (UNCHANGED) =====
            if (testContext.getDriver() != null) {
                testContext.getDriver().quit();
            }
        }
    }
}