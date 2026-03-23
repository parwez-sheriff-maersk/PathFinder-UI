package stepDefinitions;

import Pages.BusinessIdentifierHouseBillOfLadingPages;
import Pages.PathFinderLocators;
import io.cucumber.java.en.Then;
import org.openqa.selenium.WebDriver;
import utils.TestContext;
import utils.WaitUtils;

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

            WaitUtils.waitForElementClickable(driver, PathFinderLocators.TRACE_TABLE_TAB, 30, logger);

            logger.info("✅ Dashboard Loaded Successfully");

            // 🔥 CLICK TRACE TABLE TAB
            WaitUtils.waitForElementClickable(driver, PathFinderLocators.TRACE_TABLE_TAB, 30, logger).click();
            Thread.sleep(3000);

            logger.info("✅ Trace Table tab clicked");

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