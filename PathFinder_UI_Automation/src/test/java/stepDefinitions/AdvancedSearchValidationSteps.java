package stepDefinitions;

import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import utils.AdvancedSearchRecord;
import utils.StatusMapper;
import utils.DatabaseUtils;
import utils.TestContext;
import Pages.AdvancedSearchValidation;

public class AdvancedSearchValidationSteps {

    private final TestContext testContext;
    private final AdvancedSearchValidation page;

    private static final Logger logger =
            Logger.getLogger(AdvancedSearchValidationSteps.class.getName());

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
    // STEP: Loop all 5 DB records — search, expand, validate each
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

        logger.info("✅ Total records fetched from DB: " + records.size());

        int passed = 0;
        int failed = 0;

        for (int i = 0; i < records.size(); i++) {

            AdvancedSearchRecord record = records.get(i);

            logger.info("==================================================");
            logger.info("🔁 RECORD " + (i + 1) + " OF " + records.size());
            logger.info("   Transaction ID : " + record.getTransactionId());
            logger.info("   Platform ID    : " + record.getPlatformId());
            logger.info("   DB Status      : " + record.getDbStatus());
            logger.info("   Origin System  : " + record.getOriginSystem());
            logger.info("==================================================");

            // 1. Open Advanced Search drawer
            logger.info("📌 [" + (i + 1) + "] Opening Advanced Search drawer...");
            page.clickAdvancedSearch();

            // 2. Clear old values, enter new Transaction ID + Platform ID, click Search
            logger.info("📌 [" + (i + 1) + "] Clearing fields and entering new search criteria...");
            page.enterSearchCriteriaAndSearch(record);

            // 3. Expand Row 1 + Row 2 (AMPS method)
            logger.info("📌 [" + (i + 1) + "] Expanding rows (Level 1 + Level 2)...");
            page.expandRow1();
            page.expandRow2(record.getOriginSystem());

            // 4. Validate status
            String expectedUiStatus = StatusMapper.mapDbToUiStatus(
                    record.getDbStatus(), record.getOriginSystem());
            logger.info("📌 [" + (i + 1) + "] Validating status...");
            logger.info("   DB Status      : " + record.getDbStatus());
            logger.info("   Mapped UI Status: " + expectedUiStatus);
            page.validateStatus(expectedUiStatus);

            passed++;
            logger.info("==================================================");
            logger.info("✅ RECORD " + (i + 1) + " — PASSED");
            logger.info("==================================================");
        }

        logger.info("==================================================");
        logger.info("🏁 ADVANCED SEARCH VALIDATION COMPLETE");
        logger.info("   Total Records : " + records.size());
        logger.info("   Passed        : " + passed);
        logger.info("   Failed        : " + failed);
        logger.info("==================================================");
    }
}
