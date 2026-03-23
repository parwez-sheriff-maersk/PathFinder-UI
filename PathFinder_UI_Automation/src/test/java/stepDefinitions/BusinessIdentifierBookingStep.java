package stepDefinitions;

import Pages.PathFinderLocators;
import Pages.BusinessIdentifierBookingPages;
import io.cucumber.java.en.Then;
import org.openqa.selenium.WebDriver;

import java.util.logging.Logger;

import utils.TestContext;
import utils.DatabaseUtils;
import utils.PlatformRecord;
import utils.StatusMapper;
import utils.WaitUtils;

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

        logger.info("🚀 ===== BOOKING NUMBER DB vs UI VALIDATION STARTED =====");

        WaitUtils.waitForElementClickable(driver, PathFinderLocators.TRACE_TABLE_TAB, 30, logger);

        logger.info("✅ Dashboard Loaded");

        BusinessIdentifierBookingPages bookingPage =
                new BusinessIdentifierBookingPages(driver);

        // 🔥 CALL TRACE TAB METHOD
        bookingPage.clickTraceTableTab();

        // 🔥 CALL MAIN FLOW
        bookingPage.searchAndValidateBooking(context.getProperties());

        logger.info("🎉 ===== ALL BOOKING NUMBERS VALIDATED SUCCESSFULLY =====");
    }
}