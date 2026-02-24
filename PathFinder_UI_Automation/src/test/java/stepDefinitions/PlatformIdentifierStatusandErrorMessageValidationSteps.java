package stepDefinitions;

import Pages.PlatformIdentifierStatusErrorandTerminatedMessageValidation;
import io.cucumber.java.en.Then;
import org.openqa.selenium.WebDriver;

import java.util.Properties;
import java.util.logging.Logger;

import utils.TestContext;
import utils.StatusValidation;

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

    private Properties getProperties() {
        return context.getProperties();
    }

    @Then("User validates PlatformIdentifier")
    public void user_validates_platform_identifier() throws InterruptedException {

        WebDriver driver = getDriver();
        Properties props = getProperties();

        PlatformIdentifierStatusErrorandTerminatedMessageValidation platformPage =
                new PlatformIdentifierStatusErrorandTerminatedMessageValidation(driver);

        StatusValidation statusValidation =
                new StatusValidation(driver);

        // 🔥 Read platform IDs from config
        String orangePlatformId = props.getProperty("ORANGE_PLATFORM_ID");
        String redPlatformId    = props.getProperty("RED_PLATFORM_ID");
        String greenPlatformId  = props.getProperty("GREEN_PLATFORM_ID");

        logger.info("================================================================");
        logger.info("🚀 ===== Platform Identifier End-to-End Validation Started =====");
        logger.info("================================================================");

        // ============================================================
        // 🟠 ORANGE FLOW
        // ============================================================

        logger.info("🟠 STEP 1: ORANGE Flow Validation Started");

        platformPage.clickTraceTableTab();
        platformPage.enterPlatformIdentifierAndSearch(orangePlatformId);

        platformPage.processOrangeFlow();
        statusValidation.validateTerminatedStatus();

        logger.info("✅ ORANGE Flow PASSED Successfully");

        // ============================================================
        // 🔴 RED FLOW
        // ============================================================

        logger.info("🔴 STEP 2: RED Flow Validation Started");

        platformPage.updatePlatformIdentifierAndSearch(redPlatformId);

        platformPage.processRedFlow();
        statusValidation.validateSuccessStatus();

        logger.info("✅ RED Flow PASSED Successfully");

        // ============================================================
        // 🔴 ERROR (3rd Level Expansion)
        // ============================================================

        logger.info("⬇ STEP 3: ERROR Validation");

        platformPage.expandToErrorLevel();
        statusValidation.validateErrorStatus();

        logger.info("✅ ERROR Status Validated Successfully");

        // ============================================================
        // 🟢 GREEN FLOW
        // ============================================================

        logger.info("🟢 STEP 4: GREEN Flow Validation Started");

        platformPage.updatePlatformIdentifierAndSearch(greenPlatformId);

        platformPage.processGreenFlow();
        statusValidation.validateSuccessStatus();

        logger.info("✅ GREEN Flow PASSED Successfully");

        logger.info("🎉 ===== ALL FLOWS VALIDATED SUCCESSFULLY =====");
    }
}