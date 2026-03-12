package stepDefinitions;

import Pages.PathFinderLocators;
import Pages.BusinessIdentifierBookingPages;
import io.cucumber.java.en.Then;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import utils.TestContext;
import utils.DatabaseUtils;
import utils.PlatformRecord;
import utils.StatusMapper;

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
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        logger.info("🚀 ===== BOOKING NUMBER DB vs UI VALIDATION STARTED =====");

        wait.until(ExpectedConditions.elementToBeClickable(
                PathFinderLocators.TRACE_TABLE_TAB));

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