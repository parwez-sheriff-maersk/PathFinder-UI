package stepDefinitions;

import Pages.PathFinderLocators;
import Pages.PlatformIdentifierStatusErrorandTerminatedMessageValidation;
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

        wait.until(ExpectedConditions.elementToBeClickable(
                PathFinderLocators.TRACE_TABLE_TAB));

        logger.info("✅ Dashboard Loaded");

        platformPage.clickTraceTableTab();

        List<PlatformRecord> records =
                DatabaseUtils.getLatestPlatformIdentifiers(context.getProperties());

        if (records.isEmpty()) {
            throw new RuntimeException("❌ No platform identifiers found in DB!");
        }

        logger.info("📊 Total Records From DB: " + records.size());

        // ============================================================
        // GROUP DB RECORDS BY PLATFORM ID
        // ============================================================

        Map<String, List<PlatformRecord>> groupedByPlatformId =
                records.stream()
                        .collect(Collectors.groupingBy(PlatformRecord::getPlatformId));

        for (String platformId : groupedByPlatformId.keySet()) {

            List<PlatformRecord> platformRecords = groupedByPlatformId.get(platformId);

            logger.info("==================================================");
            logger.info("🎯 VALIDATION START FOR PLATFORM ID: " + platformId);
            logger.info("==================================================");

            // 🔍 SEARCH ONCE
            platformPage.updatePlatformIdentifierAndSearch(platformId);

            logger.info("⏳ Waiting for table refresh...");
            platformPage.waitForExpandArrowsAfterSearch();
            Thread.sleep(2000);

            // Collect unique systems + expected status
            Map<String, String> expectedStatuses = new HashMap<>();

            for (PlatformRecord record : platformRecords) {

                String originSystem = record.getOriginSystem().toUpperCase();
                String dbStatus     = record.getDbStatus().toUpperCase();

                String normalizedDbStatus =
                        StatusMapper.mapDbToUiStatus(dbStatus, originSystem);

                expectedStatuses.put(originSystem, normalizedDbStatus);

                logger.info("🗄 DB Record -> " + originSystem +
                        " | Raw DB Status: " + dbStatus +
                        " | Normalized UI Status: " + normalizedDbStatus);
            }

            logger.info("🧭 Systems detected in DB for this Platform ID:");

            for (String system : expectedStatuses.keySet()) {
                logger.info("   ➤ " + system +
                        " -> Expected Status: " + expectedStatuses.get(system));
            }

            logger.info("==================================================");

            // ============================================================
            // SCENARIO 1: ONLY SEEBURGER
            // ============================================================

            if (expectedStatuses.size() == 1 &&
                    expectedStatuses.containsKey("SEEBURGER")) {

                logger.info("🟣 SCENARIO: ONLY SEEBURGER present");
                logger.info("🟣 Starting SEEBURGER validation...");

                platformPage.expandAndValidateSeeburger(
                        expectedStatuses.get("SEEBURGER"));

                logger.info("🟣 SEEBURGER validation completed.");
            }

            // ============================================================
            // SCENARIO 2: AMPS and/or SEEBURGER
            // ============================================================

            else {

                if (expectedStatuses.containsKey("AMPS")) {

                    logger.info("🔵 AMPS detected for this Platform ID");
                    logger.info("🔵 Starting AMPS validation...");

                    platformPage.expandAndValidateAmps(
                            expectedStatuses.get("AMPS"));

                    logger.info("🔵 AMPS validation completed.");

                    // Scroll before validating SEEBURGER
                    platformPage.scrollToSeeburgerRow();
                    Thread.sleep(1500);
                }

                if (expectedStatuses.containsKey("SEEBURGER")) {

                    logger.info("🟣 SEEBURGER detected for this Platform ID");
                    logger.info("🟣 Starting SEEBURGER validation...");

                    platformPage.expandAndValidateSeeburger(
                            expectedStatuses.get("SEEBURGER"));

                    logger.info("🟣 SEEBURGER validation completed.");
                }
            }

            logger.info("✅ VALIDATION FINISHED FOR PLATFORM ID: " + platformId);
            logger.info("==================================================");
        }

        logger.info("🎉 ===== ALL PLATFORM IDENTIFIERS VALIDATED SUCCESSFULLY =====");
        }}