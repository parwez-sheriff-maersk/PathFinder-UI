package stepDefinitions;

import Pages.PlatformIdentifierStatusandErrorMessageValidation;
import io.cucumber.java.en.Then;
import org.junit.Assert;
import org.openqa.selenium.WebDriver;

import java.util.Properties;
import java.util.logging.Logger;

import utils.TestContext;

public class PlatformIdentifierStatusandErrorMessageValidationSteps {

    private static final Logger logger =
            Logger.getLogger(PlatformIdentifierStatusandErrorMessageValidationSteps.class.getName());

    private final TestContext context;

    // ✅ Constructor only stores context
    public PlatformIdentifierStatusandErrorMessageValidationSteps(TestContext context) {
        this.context = context;
        logger.info("🧭 PlatformIdentifier Steps initialized");
    }

    // ✅ Always fetch driver AFTER Hooks initializes it
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

        PlatformIdentifierStatusandErrorMessageValidation platformPage =
                new PlatformIdentifierStatusandErrorMessageValidation(driver);

        logger.info("================================================================");
        logger.info("🚀 ===== Platform Identifier End-to-End Validation Started =====");
        logger.info("================================================================");

        // ============================================================
        // 🟠 ORANGE FLOW
        // ============================================================

        String firstPlatformId = "150000000040167219.0001.0001";

        logger.info("🟠 STEP 1: ORANGE Flow Validation Started");

        platformPage.clickTraceTableTab();
        platformPage.enterPlatformIdentifierAndSearch(firstPlatformId);

        String orangeStatus = platformPage.processOrangeFlow();

        logger.info("📌 ORANGE Flow - Captured Status : " + orangeStatus);
        Assert.assertEquals("TERMINATED", orangeStatus);

        logger.info("✅ ORANGE Flow PASSED Successfully");

        // ============================================================
        // 🔴 RED FLOW
        // ============================================================

        String redPlatformId = "150000000040279748";

        logger.info("🔴 STEP 2: RED Flow Validation Started");

        platformPage.updatePlatformIdentifierAndSearch(redPlatformId);

        String redStatus = platformPage.processRedFlow();

        logger.info("📌 RED Flow - Captured Status : " + redStatus);
        Assert.assertEquals("SUCCESS", redStatus);

        logger.info("✅ RED Flow PASSED Successfully");

        // ============================================================
        // ⬇ THIRD EXPAND (ERROR VALIDATION)
        // ============================================================

        logger.info("⬇ STEP 3: Third Expand & ERROR Validation");

        platformPage.clickThirdExpandArrow();
        platformPage.validateErrorStatusAfterThirdExpand();

        logger.info("✅ ERROR Status Validated Successfully");

        // ============================================================
        // 🟢 GREEN FLOW
        // ============================================================

        String greenPlatformId = "499433176945202401";

        logger.info("🟢 STEP 4: GREEN Flow Validation Started");

        String greenStatus = platformPage.processGreenFlow(greenPlatformId);

        logger.info("📌 GREEN Flow - Captured Status : " + greenStatus);
        Assert.assertEquals("SUCCESS", greenStatus);

        logger.info("✅ GREEN Flow PASSED Successfully");

        logger.info("🎉 ===== ALL FLOWS VALIDATED SUCCESSFULLY =====");
    }
}