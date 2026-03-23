package utils;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Scenario;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

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

        // ===== DRIVER SETUP =====
        // Auto-detect CI environment (GitHub Actions sets CI=true)
        boolean isCI = "true".equalsIgnoreCase(System.getenv("CI"));

        // In CI: use the ChromeDriver installed by setup-chrome action (CHROMEWEBDRIVER env var)
        // Locally: use WebDriverManager to auto-download matching ChromeDriver
        String chromeDriverEnv = System.getenv("CHROMEWEBDRIVER");
        if (chromeDriverEnv != null && !chromeDriverEnv.isEmpty()) {
            System.setProperty("webdriver.chrome.driver", chromeDriverEnv + "/chromedriver");
        } else {
            WebDriverManager.chromedriver().setup();
        }

        ChromeOptions options = new ChromeOptions();
        if (isCI) {
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-setuid-sandbox");
            options.addArguments("--disable-extensions");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--remote-debugging-port=9222");
        }

        WebDriver driver = new ChromeDriver(options);
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
    // AFTER EACH STEP — capture screenshot at exact point of failure
    // ============================================================

    @AfterStep
    public void afterStep(Scenario scenario) {

        if (!scenario.isFailed()) return;
        if (ExtentTestManager.isScreenshotTaken()) return;

        try {

            WebDriver driver = testContext.getDriver();
            if (driver == null) return;

            ExtentTest test = ExtentTestManager.getTest();

            // Current page URL when step failed
            String currentUrl = driver.getCurrentUrl();

            // Log where exactly it failed
            test.fail("<b style='color:#ff4c4c'>Step Failed</b>");
            test.fail("<b>Page URL at failure:</b> <a href='" + currentUrl + "' target='_blank'>" + currentUrl + "</a>");

            // Take screenshot at exact failure point
            String screenshotPath = ScreenshotUtils.captureFullPage(
                    driver,
                    scenario.getName().replaceAll(" ", "_") + "_step_failure"
            );

            if (screenshotPath != null) {
                test.fail("<b>Screenshot at failure point:</b>")
                    .addScreenCaptureFromPath(screenshotPath, "Step Failure Screenshot");
                ExtentTestManager.markScreenshotTaken();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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

            // Track feature-wise results
            String featureName = scenario.getUri() != null
                    ? scenario.getUri().toString()
                    : "Unknown Feature";

            ExtentManager.trackResult(featureName, isPassed);

            if (scenario.isFailed()) {

                // Failure summary box
                String failureSummary =
                    "<div style='background:#2a0a0a;border-left:4px solid #ff4c4c;" +
                    "padding:12px;border-radius:6px;font-family:monospace;font-size:13px'>" +
                    "<b style='color:#ff4c4c'>SCENARIO FAILED</b><br><br>" +
                    "<b>Scenario :</b> " + scenario.getName() + "<br>" +
                    "<b>Feature  :</b> " + scenario.getUri() + "<br>" +
                    "<b>Status   :</b> " + scenario.getStatus() + "<br>" +
                    "</div>";

                test.fail(failureSummary);

                // Take screenshot if @AfterStep didn't already take one
                if (!ExtentTestManager.isScreenshotTaken()) {
                    String screenshotPath = ScreenshotUtils.captureFullPage(
                            driver,
                            scenario.getName().replaceAll(" ", "_")
                    );
                    if (screenshotPath != null) {
                        test.fail("<b>Failure Screenshot:</b>")
                            .addScreenCaptureFromPath(screenshotPath, "Failure Screenshot");
                    }
                }

            } else {

                test.pass("✅ Scenario Passed Successfully");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            ExtentManager.addDashboard();
            ExtentManager.getInstance().flush();

            if (testContext.getDriver() != null) {
                testContext.getDriver().quit();
            }
        }
    }
}