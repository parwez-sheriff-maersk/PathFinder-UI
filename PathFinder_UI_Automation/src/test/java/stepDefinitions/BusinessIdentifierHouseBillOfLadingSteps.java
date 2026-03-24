package stepDefinitions;

import Pages.BusinessIdentifierHouseBillOfLadingPages;
import io.cucumber.java.en.Then;
import org.openqa.selenium.WebDriver;
import utils.NavigationUtils;
import utils.TestContext;

import java.util.logging.Logger;

public class BusinessIdentifierHouseBillOfLadingSteps {

    private static final Logger logger =
            Logger.getLogger(BusinessIdentifierHouseBillOfLadingSteps.class.getName());

    private final TestContext context;

    public BusinessIdentifierHouseBillOfLadingSteps(TestContext context) {
        this.context = context;
        logger.info("🧭 BusinessIdentifier-HouseBillOfLading Steps initialized");
    }

    private WebDriver getDriver() {
        WebDriver driver = context.getDriver();
        if (driver == null) {
            throw new IllegalStateException("WebDriver is still null. Check Hooks.");
        }
        return driver;
    }

    @Then("User validates BusinessIdentifier- House Bill Of Lading")
    public void user_validates_business_identifier_house_bill_of_lading()
            throws InterruptedException {

        WebDriver driver = getDriver();

        logger.info("==================================================");
        logger.info("🚀 STARTING HOUSE BILL OF LADING VALIDATION");
        logger.info("==================================================");

        try {

            // Reusable Trace Table click from NavigationUtils
            NavigationUtils.clickTraceTableTab(driver);

            BusinessIdentifierHouseBillOfLadingPages page =
                    new BusinessIdentifierHouseBillOfLadingPages(driver);

            page.searchAndValidateHouseBill(context.getProperties());

            logger.info("🎉 HOUSE BILL OF LADING VALIDATION COMPLETED");
            logger.info("==================================================");

        } catch (Exception e) {

            logger.severe("==================================================");
            logger.severe("❌ HOUSE BILL VALIDATION FAILED");
            logger.severe("❌ ERROR MESSAGE: " + e.getMessage());
            logger.severe("❌ FULL STACK TRACE BELOW");
            logger.severe("==================================================");

            e.printStackTrace();

            // 🔥 Re-throw so Cucumber marks scenario as failed
            throw e;
        }
    }
}