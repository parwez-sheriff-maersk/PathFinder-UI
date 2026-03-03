package stepDefinitions;

import Pages.PathFinderLocators;
import Pages.PlatformIdentifierStatusErrorandTerminatedMessageValidation;
import io.cucumber.java.en.Then;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;

import utils.TestContext;
import utils.DatabaseUtils;
import utils.PlatformRecord;
import utils.StatusMapper;

public class PlatformIdentifierStatusandErrorMessageValidationSteps {

    private static final Logger logger =
            Logger.getLogger(PlatformIdentifierStatusandErrorMessageValidationSteps.class.getName());

    private final TestContext context;

    public PlatformIdentifierStatusandErrorMessageValidationSteps(TestContext context) {
        this.context = context;
        logger.info("🧭 PlatformIdentifier Steps initialized");
    }

    private WebDriver getDriver() {
        WebDriver driver = context.getDriver();
        if (driver == null) {
            throw new IllegalStateException("WebDriver is still null. Check Hooks.");
        }
        return driver;
    }

    @Then("User validates PlatformIdentifier")
    public void user_validates_platform_identifier() throws InterruptedException {

        WebDriver driver = getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        PlatformIdentifierStatusErrorandTerminatedMessageValidation platformPage =
                new PlatformIdentifierStatusErrorandTerminatedMessageValidation(driver);

        logger.info("🚀 ===== DB vs UI Platform Identifier Validation Started =====");

        // Wait until dashboard is ready
        wait.until(ExpectedConditions.elementToBeClickable(
                PathFinderLocators.TRACE_TABLE_TAB));

        logger.info("✅ Dashboard Loaded");

        // Open Trace Table
        platformPage.clickTraceTableTab();

        // Fetch latest records from DB
        List<PlatformRecord> records =
                DatabaseUtils.getLatestPlatformIdentifiers(context.getProperties());

        if (records.isEmpty()) {
            throw new RuntimeException("❌ No platform identifiers found in DB!");
        }

        logger.info("📊 Total Records From DB: " + records.size());

        for (PlatformRecord record : records) {

            String platformId   = record.getPlatformId();
            String dbStatus     = record.getDbStatus().toUpperCase();
            String originSystem = record.getOriginSystem().toUpperCase();

            String normalizedDbStatus =
                    StatusMapper.mapDbToUiStatus(dbStatus, originSystem);

            logger.info("==================================================");
            logger.info("📌 Platform ID            : " + platformId);
            logger.info("🔵 " + originSystem + " Status (DB)  : " + dbStatus);
            logger.info("🔄 Normalized DB Status   : " + normalizedDbStatus);
            logger.info("==================================================");

            // 🔍 SEARCH PLATFORM ID
            platformPage.updatePlatformIdentifierAndSearch(platformId);

            logger.info("⏳ Waiting for table refresh...");
            platformPage.waitForExpandArrowsAfterSearch();
            Thread.sleep(2000);

            logger.info("🔎 Expanding system: " + originSystem);

            // ============================================================
            // EXPAND + VALIDATE BASED ON SYSTEM
            // ============================================================

            if (originSystem.equalsIgnoreCase("AMPS")) {

                // ✅ Working method unchanged
                platformPage.expandAndValidateAmps(normalizedDbStatus);

            } else if (originSystem.equalsIgnoreCase("SEEBURGER")) {

                // ✅ SEEBURGER only needs expectedStatus
                platformPage.expandAndValidateSeeburger(normalizedDbStatus);

            } else {

                throw new RuntimeException("❌ Unknown origin system: " + originSystem);
            }

            logger.info("✅ " + originSystem + " status MATCHED");
        }

        logger.info("🎉 ===== ALL PLATFORM IDENTIFIERS VALIDATED SUCCESSFULLY =====");
    }
}