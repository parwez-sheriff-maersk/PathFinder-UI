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
import Pages.TransactionIDstatusanderrormessagevalidation;

public class TransactionIDStatusAndErrorMessageValidationSteps {

    private static final Logger logger =
            Logger.getLogger(TransactionIDStatusAndErrorMessageValidationSteps.class.getName());

    private final TestContext context;

    // ✅ Constructor only stores context
    public TransactionIDStatusAndErrorMessageValidationSteps(TestContext context) {
        this.context = context;
    }

    // ✅ Always fetch driver AFTER Hooks initializes it
    private WebDriver getDriver() {
        WebDriver driver = context.getDriver();
        if (driver == null) {
            throw new IllegalStateException("WebDriver is still null. Check Hooks.");
        }
        return driver;
    }

    private Properties getProperties() {
        return context.getProperties();
    }

    @Given("the user is on the Pathfinder application")
    public void the_user_is_on_the_pathfinder_application() {

        WebDriver driver = getDriver();

        logger.info("🌐 Navigating to Pathfinder application...");

        driver.get("https://pathfinder-preprod.maersk-digital.net/");
        // driver.get("https://pathfinder-dev.maersk-digital.net/");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        try {
            wait.until(d -> d.getTitle() != null && d.getTitle().contains("Pathfinder"));
            logger.info("✅ Pathfinder page ready. Title: " + driver.getTitle());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "⏳ Pathfinder page did not load in time", e);
            throw e;
        }
    }

    @When("User sign in with environment credentials")
    public void signInWithEnvironmentCredentials() {

        WebDriver driver = getDriver();
        Properties props = getProperties();

        logger.info("🔐 Signing in with environment credentials");

        String username = props.getProperty("USER_NAME");
        String password = props.getProperty("USER_PASSWORD");

        Login login = new Login(driver);

        login.enterEmail(username);
        login.enterPassword(password);

        logger.info("✅ Sign-in steps executed");
    }

    @Then("User validates transaction ID")
    public void user_validates_transaction_id() throws Exception {

        WebDriver driver = getDriver();

        logger.info("🧪 Starting Transaction ID validation");

        TransactionIDstatusanderrormessagevalidation page =
                new TransactionIDstatusanderrormessagevalidation(driver);

        try {
            page.Validation();
            logger.info("✅ Transaction ID validation completed successfully");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "❌ Transaction ID validation failed", ex);
            throw ex;
        }
    }
}