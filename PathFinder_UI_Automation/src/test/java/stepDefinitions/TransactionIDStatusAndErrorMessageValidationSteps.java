package stepDefinitions;

import java.time.Duration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import utils.TestContext;
import utils.Login;
import Pages.TransactionIDstatusanderrormessagevalidation; // Page class

public class TransactionIDStatusAndErrorMessageValidationSteps {

    private static final Logger logger =
        Logger.getLogger(TransactionIDStatusAndErrorMessageValidationSteps.class.getName());

    private final WebDriver driver;
    private final Properties properties;
    private final Login login;
    private final TransactionIDstatusanderrormessagevalidation pathfinderPage;

    public TransactionIDStatusAndErrorMessageValidationSteps(TestContext context) {
        this.driver = context.getDriver();
        this.properties = context.getProperties();

        if (this.driver == null) {
            logger.severe("❌ WebDriver is not initialized. Check Hooks/TestContext.");
            throw new IllegalStateException("WebDriver is not initialized!");
        }

        this.login = new Login(this.driver);
        this.pathfinderPage = new TransactionIDstatusanderrormessagevalidation(this.driver);

        logger.info("🧭 StepDefinitions initialized with WebDriver and Page object");
    }

    @Given("the user is on the Pathfinder application")
    public void the_user_is_on_the_pathfinder_application() {
        logger.info("🌐 Navigating to Pathfinder application...");

        driver.get("https://pathfinder-preprod.maersk-digital.net/");
       // driver.get("https://pathfinder-dev.maersk-digital.net/");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        try {
            wait.until(d -> {
                String title = d.getTitle();
                return title != null && title.contains("Pathfinder");
            });

            logger.info("✅ Pathfinder page ready. Title: " + safeTitle());
        } catch (Exception te) {
            logger.log(Level.SEVERE, "⏳ Pathfinder page did not appear within 30s. Title: " + safeTitle(), te);
            throw te;
        }
    }

    @When("User sign in with environment credentials")
    public void signInWithEnvironmentCredentials() throws InterruptedException {
        logger.info("🔐 Signing in with environment credentials");

        String username = properties.getProperty("USER_NAME");
        String password = properties.getProperty("USER_PASSWORD");

        logger.fine("Using username: " + username);

        login.enterEmail(username);
        login.enterPassword(password);

        logger.info("⏳ Waiting for SSO flow to complete (90s)");
      //  Thread.sleep(90000);
        logger.info("✅ Sign-in wait completed");
    }
    @Then("User validates transaction ID")
    public void user_validates_transaction_id() throws Exception {
        logger.info("🧪 Starting Transaction ID validation (Step)");

        try {
            logger.info("➡️ Calling Page: TransactionIDstatusanderrormessagevalidation.Validation()");
            pathfinderPage.Validation();
            logger.info("✅ Transaction ID validation completed (Step)");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "❌ Transaction ID validation failed in Step: " + ex.getMessage(), ex);

            try {
                pathfinderPage.setStartDateYesterday10();
                pathfinderPage.setEndDateToday10();
           //  Thread.sleep(90000);
                logger.info("ℹ️ Date range set after failure (yesterday 10:00 → today 10:00)");
            } catch (Exception e2) {
                logger.severe("⚠️ Failed to set date range during failure handling: " + e2.getMessage());
            }

           
            throw ex;
            
        }
    }

    private String safeTitle() {
        try {
            return driver.getTitle();
        } catch (Exception e) {
            logger.warning("⚠️ Could not read title: " + e.getMessage());
            return "<unknown>";
        }
    }
}