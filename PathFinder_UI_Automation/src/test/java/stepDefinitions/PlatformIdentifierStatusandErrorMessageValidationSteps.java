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

    private final WebDriver driver;
    private final Properties properties;
    private final PlatformIdentifierStatusandErrorMessageValidation platformPage;

    public PlatformIdentifierStatusandErrorMessageValidationSteps(TestContext context) {

        this.driver = context.getDriver();
        this.properties = context.getProperties();

        this.platformPage =
                new PlatformIdentifierStatusandErrorMessageValidation(driver);

        logger.info("🧭 PlatformIdentifier Steps initialized");
    }

    @Then("User validates PlatformIdentifier")
    public void user_validates_platform_identifier() throws InterruptedException {

        logger.info("================================================================");
        logger.info("🚀 ===== Platform Identifier End-to-End Validation Started =====");
        logger.info("================================================================");

        // ============================================================
        // 🟠 ORANGE FLOW
        // ============================================================

        String firstPlatformId = "150000000040167219.0001.0001";

        logger.info("");
        logger.info("------------------------------------------------------------");
        logger.info("🟠 STEP 1: ORANGE Flow Validation Started");
        logger.info("------------------------------------------------------------");

        platformPage.clickTraceTableTab();
        platformPage.enterPlatformIdentifierAndSearch(firstPlatformId);

        String orangeStatus = platformPage.processOrangeFlow();

        logger.info("📌 ORANGE Flow - Captured Status : " + orangeStatus);
        logger.info("📌 ORANGE Flow - Expected Status : TERMINATED");

        Assert.assertEquals("TERMINATED", orangeStatus);

        logger.info("✅ ORANGE Flow PASSED Successfully");
        logger.info("------------------------------------------------------------");


        // ============================================================
        // 🔴 RED FLOW
        // ============================================================

        String redPlatformId = "150000000040279748";

        logger.info("");
        logger.info("------------------------------------------------------------");
        logger.info("🔴 STEP 2: RED Flow Validation Started");
        logger.info("------------------------------------------------------------");

        platformPage.updatePlatformIdentifierAndSearch(redPlatformId);

        String redStatus = platformPage.processRedFlow();

        logger.info("📌 RED Flow - Captured Status : " + redStatus);
        logger.info("📌 RED Flow - Expected Status : SUCCESS");

        Assert.assertEquals("SUCCESS", redStatus);

        logger.info("✅ RED Flow PASSED Successfully");
        logger.info("------------------------------------------------------------");


        // ============================================================
        // ⬇ THIRD EXPAND (ERROR VALIDATION)
        // ============================================================

        logger.info("");
        logger.info("------------------------------------------------------------");
        logger.info("⬇ STEP 3: Third Expand & ERROR Validation");
        logger.info("------------------------------------------------------------");

        platformPage.clickThirdExpandArrow();
        platformPage.validateErrorStatusAfterThirdExpand();

        logger.info("✅ ERROR Status Validated Successfully");
        logger.info("------------------------------------------------------------");


        // ============================================================
        // 🟢 GREEN FLOW
        // ============================================================

        String greenPlatformId = "499433176945202401";

        logger.info("");
        logger.info("------------------------------------------------------------");
        logger.info("🟢 STEP 4: GREEN Flow Validation Started");
        logger.info("------------------------------------------------------------");

        logger.info("🔎 Executing GREEN flow for Platform ID: " + greenPlatformId);

        String greenStatus = platformPage.processGreenFlow(greenPlatformId);

        logger.info("📌 GREEN Flow - Captured Status : " + greenStatus);
        logger.info("📌 GREEN Flow - Expected Status : SUCCESS");

        if (!greenStatus.equals("SUCCESS")) {
            logger.severe("❌ GREEN Flow FAILED. Expected SUCCESS but got: " + greenStatus);
        }

        Assert.assertEquals("SUCCESS", greenStatus);

        logger.info("✅ GREEN Flow PASSED Successfully");
        logger.info("------------------------------------------------------------");


        logger.info("");
        logger.info("================================================================");
        logger.info("🎉 ===== ALL FLOWS VALIDATED SUCCESSFULLY =====");
        logger.info("================================================================");
    }}