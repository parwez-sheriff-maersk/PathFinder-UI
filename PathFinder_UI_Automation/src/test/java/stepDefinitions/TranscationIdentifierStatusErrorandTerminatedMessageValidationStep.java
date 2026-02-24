package stepDefinitions;

import Pages.TranscationIdentifierStatusErrorandTerminatedMessageValidation;
import io.cucumber.java.en.Then;
import org.openqa.selenium.WebDriver;

import java.util.Properties;
import java.util.logging.Logger;

import utils.TestContext;
import utils.StatusValidation;

public class TranscationIdentifierStatusErrorandTerminatedMessageValidationStep {

    private static final Logger logger =
            Logger.getLogger(TranscationIdentifierStatusErrorandTerminatedMessageValidationStep.class.getName());

    private final TestContext context;

    public TranscationIdentifierStatusErrorandTerminatedMessageValidationStep(TestContext context) {
        this.context = context;
    }

    @Then("User validates TranscationIdentifier")
    public void User_validates_TranscationIdentifier() throws Exception {

        WebDriver driver = context.getDriver();
        Properties props = context.getProperties();

        TranscationIdentifierStatusErrorandTerminatedMessageValidation page =
                new TranscationIdentifierStatusErrorandTerminatedMessageValidation(driver);

        StatusValidation statusValidation =
                new StatusValidation(driver);

        String orangeTxn = props.getProperty("ORANGE_TRANSACTION_ID");
        String greenTxn  = props.getProperty("GREEN_TRANSACTION_ID");
        String redTxn    = props.getProperty("RED_TRANSACTION_ID");

        logger.info("================================================================");
        logger.info("🚀 ===== Transaction Identifier End-to-End Validation Started =====");
        logger.info("================================================================");

        page.clickTraceTableTab();

        // ============================================================
        // 🟠 ORANGE FLOW
        // ============================================================

        logger.info("🟠 STEP 1: ORANGE Flow Validation Started");

        page.enterTransactionIdentifierAndSearch(orangeTxn);
        page.processOrangeFlow();
        statusValidation.validateTerminatedStatus();

        logger.info("✅ ORANGE Flow PASSED Successfully");

        // ============================================================
        // 🟢 GREEN FLOW
        // ============================================================

        logger.info("🟢 STEP 2: GREEN Flow Validation Started");

        page.enterTransactionIdentifierAndSearch(greenTxn);
        page.processGreenFlow();
        statusValidation.validateSuccessStatus();

        logger.info("✅ GREEN Flow PASSED Successfully");

        // ============================================================
        // 🔴 RED FLOW
        // ============================================================

        logger.info("🔴 STEP 3: RED Flow Validation Started");

        page.enterTransactionIdentifierAndSearch(redTxn);
        page.processRedFlow();
        statusValidation.validateErrorStatus();

        logger.info("⬇ Validating third-level SUCCESS status");
        page.expandToErrorLevel();
        statusValidation.validateSuccessStatus();

        logger.info("🎉 ===== ALL TRANSACTION FLOWS VALIDATED SUCCESSFULLY =====");
    }}