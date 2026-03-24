package stepDefinitions;

import Pages.BusinessIdentifierBookingPages;
import io.cucumber.java.en.Then;
import org.openqa.selenium.WebDriver;

import java.util.logging.Logger;

import utils.TestContext;
import utils.NavigationUtils;

public class BusinessIdentifierBookingStep {

    private static final Logger logger =
            Logger.getLogger(BusinessIdentifierBookingStep.class.getName());

    private final TestContext context;

    public BusinessIdentifierBookingStep(TestContext context) {
        this.context = context;
        logger.info("🧭 Booking Identifier Steps initialized");
    }

    private WebDriver getDriver() {
        WebDriver driver = context.getDriver();
        if (driver == null) {
            throw new IllegalStateException("WebDriver is null. Check Hooks.");
        }
        return driver;
    }

    @Then("User validates BusinessIdentifier- Booking Number")
    public void user_validates_booking_number() throws InterruptedException {

        WebDriver driver = getDriver();

        logger.info("==================================================");
        logger.info("🚀 STARTING BOOKING NUMBER VALIDATION");
        logger.info("==================================================");

        try {

            // Reusable Trace Table click from NavigationUtils
            NavigationUtils.clickTraceTableTab(driver);

            BusinessIdentifierBookingPages bookingPage =
                    new BusinessIdentifierBookingPages(driver);

            bookingPage.searchAndValidateBooking(context.getProperties());

            logger.info("🎉 BOOKING NUMBER VALIDATION COMPLETED");
            logger.info("==================================================");

        } catch (Exception e) {

            logger.severe("==================================================");
            logger.severe("❌ BOOKING NUMBER VALIDATION FAILED");
            logger.severe("❌ ERROR MESSAGE: " + e.getMessage());
            logger.severe("❌ FULL STACK TRACE BELOW");
            logger.severe("==================================================");

            e.printStackTrace();

            // Re-throw so Cucumber marks scenario as failed
            throw e;
        }
    }
}