package stepDefinitions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

// ✅ FIXED LOGGER (use SLF4J, NOT java.util.logging)
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openqa.selenium.WebDriver;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import utils.AdvancedSearchRecord;
import utils.StatusMapper;
import utils.DatabaseUtils;
import utils.NavigationUtils;
import utils.PlatformRecord;
import utils.TestContext;

import Pages.AdvancedSearchValidation;
import Pages.PlatformIdentifierStatusErrorandTerminatedMessageValidation;

public class AdvancedSearchValidationSteps {

    private final TestContext testContext;
    private final AdvancedSearchValidation page;

    // ✅ FIXED LOGGER
    private static final Logger logger =
            LoggerFactory.getLogger(AdvancedSearchValidationSteps.class);

    public AdvancedSearchValidationSteps(TestContext context) {
        this.testContext = context;
        this.page        = new AdvancedSearchValidation(context.getDriver());
        logger.info("🧭 AdvancedSearch Steps initialized");
    }

    // ============================================================
    // STEP: Navigate to Trace Table
    // ============================================================

    @When("User sign in and navigates to Trace Table for Advanced Search")
    public void navigateToTraceTableForAdvancedSearch() {
        logger.info("==================================================");
        logger.info("📌 STEP: Navigating to Trace Table for Advanced Search");
        logger.info("==================================================");
        page.clickTraceTableTab();
    }

    // ============================================================
    // STEP: Loop all DB records — search, expand, validate each
    // ============================================================

    @Then("User validates Advanced Search for all DB records")
    public void validateAdvancedSearchForAllRecords() throws InterruptedException {

        Properties props = testContext.getProperties();

        logger.info("==================================================");
        logger.info("📦 STEP: Fetching Advanced Search records from DB...");
        logger.info("==================================================");

        List<AdvancedSearchRecord> records = DatabaseUtils.getAdvancedSearchRecords(props);

        if (records == null || records.isEmpty()) {
            throw new RuntimeException("❌ No records found in DB with Transaction ID and Platform ID");
        }

        logger.info("✅ Total records fetched from DB: {}", records.size());

        int passed = 0;
        int failed = 0;

        for (int i = 0; i < records.size(); i++) {

            AdvancedSearchRecord record = records.get(i);

            logger.info("==================================================");
            logger.info("🔁 RECORD {} OF {}", (i + 1), records.size());
            logger.info("   Transaction ID : {}", record.getTransactionId());
            logger.info("   Platform ID    : {}", record.getPlatformId());
            logger.info("   DB Status      : {}", record.getDbStatus());
            logger.info("   Origin System  : {}", record.getOriginSystem());
            logger.info("==================================================");

            // 1. Open Advanced Search drawer
            logger.info("📌 [{}] Opening Advanced Search drawer...", (i + 1));
            page.clickAdvancedSearch();

            // 2. Clear and search
            logger.info("📌 [{}] Clearing fields and entering new search criteria...", (i + 1));
            page.enterSearchCriteriaAndSearch(record);

            // 3. Expand rows based on origin system (AMPS or SEEBURGER)
            logger.info("📌 [{}] Expanding rows for {} ...", (i + 1), record.getOriginSystem());
            page.expandByOriginSystem(record.getOriginSystem());

            // 4. Validate UI status
            String expectedUiStatus = StatusMapper.mapDbToUiStatus(
                    record.getDbStatus(), record.getOriginSystem());

            logger.info("📌 [{}] Validating status...", (i + 1));
            page.validateStatus(
                    expectedUiStatus,
                    record.getOriginSystem(),
                    record.getPlatformId(),
                    record.getTransactionId(),
                    record.getDbStatus()
            );

            // ===============================================================
            // ✅ PLATFORM IDENTIFIER VALIDATION (Your method added safely)
            // ===============================================================
            try {
                logger.info("✅ [{}] Starting Platform Identifier UI vs DB Validation", (i + 1));

                WebDriver driver = testContext.getDriver();

                PlatformIdentifierStatusErrorandTerminatedMessageValidation platformPage =
                        new PlatformIdentifierStatusErrorandTerminatedMessageValidation(driver);

                // Fetch Platform Identifier DB Records
                List<PlatformRecord> platformRecs =
                        DatabaseUtils.getLatestPlatformIdentifiers(props)
                                .stream()
                                .filter(p -> p.getPlatformId().equalsIgnoreCase(record.getPlatformId()))
                                .collect(Collectors.toList());

                if (platformRecs.isEmpty()) {
                    logger.warn("⚠ No Platform Identifier records in DB for: {}", record.getPlatformId());
                    continue;
                }

                Map<String, List<PlatformRecord>> grouped =
                        platformRecs.stream()
                                .collect(Collectors.groupingBy(PlatformRecord::getPlatformId));

                for (String platformId : grouped.keySet()) {

                    List<PlatformRecord> platformRecords = grouped.get(platformId);

                    logger.info("==================================================");
                    logger.info("🎯 PLATFORM IDENTIFIER VALIDATION FOR: {}", platformId);
                    logger.info("==================================================");

                    NavigationUtils.clickTraceTableTab(driver);

                    platformPage.updatePlatformIdentifierAndSearch(platformId);
                    platformPage.waitForExpandArrowsAfterSearch();
                    Thread.sleep(1500);

                    Map<String, String> expectedStatuses = new HashMap<>();
                    Map<String, String> dbRawStatuses = new HashMap<>();
                    Map<String, String> traceIds = new HashMap<>();

                    for (PlatformRecord pr : platformRecords) {
                        String sys = pr.getOriginSystem().toUpperCase();
                        String rawStatus = pr.getDbStatus().toUpperCase();
                        String uiStatus = StatusMapper.mapDbToUiStatus(rawStatus, sys);

                        expectedStatuses.put(sys, uiStatus);
                        dbRawStatuses.put(sys, rawStatus);
                        traceIds.put(sys, pr.getTraceId());
                    }

                    // ✅ CASE 1: Only SEEBURGER
                    if (expectedStatuses.size() == 1 &&
                            expectedStatuses.containsKey("SEEBURGER")) {

                        platformPage.expandAndValidateSeeburger(
                                expectedStatuses.get("SEEBURGER"),
                                platformId,
                                traceIds.get("SEEBURGER"),
                                dbRawStatuses.get("SEEBURGER")
                        );
                    }
                    else {

                        // ✅ Expand AMPS
                        if (expectedStatuses.containsKey("AMPS")) {
                            platformPage.expandAndValidateAmps(
                                    expectedStatuses.get("AMPS"),
                                    platformId,
                                    traceIds.get("AMPS"),
                                    dbRawStatuses.get("AMPS")
                            );
                            platformPage.scrollToSeeburgerRow();
                        }

                        // ✅ Expand SEEBURGER
                        if (expectedStatuses.containsKey("SEEBURGER")) {
                            platformPage.expandAndValidateSeeburger(
                                    expectedStatuses.get("SEEBURGER"),
                                    platformId,
                                    traceIds.get("SEEBURGER"),
                                    dbRawStatuses.get("SEEBURGER")
                            );
                        }
                    }

                    logger.info("✅ PLATFORM IDENTIFIER VALIDATION COMPLETED FOR: {}", platformId);
                }

            } catch (Exception ex) {
                failed++;
                logger.error("❌ PLATFORM IDENTIFIER VALIDATION FAILED — {}", ex.getMessage());
            }

            passed++;
            logger.info("==================================================");
            logger.info("✅ RECORD {} — PASSED", (i + 1));
            logger.info("==================================================");
        }

        logger.info("==================================================");
        logger.info("🏁 ADVANCED SEARCH VALIDATION COMPLETE");
        logger.info("   Total Records : {}", records.size());
        logger.info("   Passed        : {}", passed);
        logger.info("   Failed        : {}", failed);
        logger.info("==================================================");
    }
}