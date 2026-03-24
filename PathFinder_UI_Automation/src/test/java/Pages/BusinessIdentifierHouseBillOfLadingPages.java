package Pages;

import static Pages.PathFinderLocators.*;

import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.openqa.selenium.*;

import utils.DatabaseUtils;
import utils.ExpandDownArrows;
import utils.InputClearFeild;
import utils.PlatformRecord;
import utils.ShadowDom;
import utils.StatusMapper;
import utils.StatusValidation;
import utils.WaitUtils;

public class BusinessIdentifierHouseBillOfLadingPages {

    private final WebDriver driver;

    private static final Logger logger =
            Logger.getLogger(BusinessIdentifierHouseBillOfLadingPages.class.getName());

    public BusinessIdentifierHouseBillOfLadingPages(WebDriver driver) {
        this.driver = driver;
    }

    public void searchAndValidateHouseBill(Properties prop) throws InterruptedException {

        logger.info("==================================================");
        logger.info("🚀 HOUSE BILL OF LADING VALIDATION STARTED");
        logger.info("==================================================");

        // ------------------------------------------------------------
        // OPEN BUSINESS IDENTIFIER DROPDOWN
        // ------------------------------------------------------------

        logger.info("📌 Opening Business Identifier dropdown...");
        WebElement dropdown =
                WaitUtils.waitForElementClickable(driver, MC_SELECT_DROPDOWN, 30, logger);
        dropdown.click();
        Thread.sleep(1500);
        logger.info("✅ Dropdown opened");

        // ------------------------------------------------------------
        // SELECT HOUSE BILL OF LADING
        // ------------------------------------------------------------

        logger.info("📌 Selecting 'HOUSE BILL OF LADING' option...");
        List<WebElement> options =
                ShadowDom.findAllDeep(driver, DROPDOWN_OPTION_DEEP, logger);

        for (WebElement option : options) {
            if (option.getText().trim()
                    .equalsIgnoreCase("HOUSE BILL OF LADING")) {

                ShadowDom.scrollIntoViewCenter(driver, option);
                ShadowDom.jsClick(driver, option);
                logger.info("✅ 'HOUSE BILL OF LADING' selected");
                break;
            }
        }

        Thread.sleep(2000);

        // ------------------------------------------------------------
        // FETCH LATEST 5 FROM DB
        // ------------------------------------------------------------

        logger.info("📦 Fetching House Bill records from DB...");
        List<PlatformRecord> records =
                DatabaseUtils.getLatestHouseBillOfLadingSeeburger(prop);

        if (records == null || records.isEmpty()) {
            throw new RuntimeException("❌ No HOUSE BILL records found in DB!");
        }

        logger.info("✅ Total records fetched: " + records.size());

        ExpandDownArrows expander = new ExpandDownArrows(driver);
        StatusValidation statusValidation = new StatusValidation(driver);

        // ------------------------------------------------------------
        // LOOP THROUGH EACH RECORD
        // ------------------------------------------------------------

        for (int i = 0; i < records.size(); i++) {

            PlatformRecord record = records.get(i);

            String houseBillValue = record.getPlatformId();
            String transactionId  = record.getTraceId();
            String expectedStatus =
                    StatusMapper.mapDbToUiStatus(
                            record.getDbStatus(),
                            record.getOriginSystem());

            logger.info("==================================================");
            logger.info("🔁 RECORD " + (i + 1) + " OF " + records.size());
            logger.info("   House Bill      : " + houseBillValue);
            logger.info("   Transaction ID  : " + transactionId);
            logger.info("   DB Status       : " + record.getDbStatus());
            logger.info("   Expected UI Map : " + expectedStatus);
            logger.info("==================================================");

            // --------------------------------------------------------
            // ENTER BUSINESS IDENTIFIER VALUE
            // --------------------------------------------------------

            logger.info("📌 [" + (i + 1) + "] Entering Business Identifier value...");

            WaitUtils.waitForElementVisible(driver, INLINE_INPUT_FIELDS, 30, logger);
            List<WebElement> allInputs = driver.findElements(INLINE_INPUT_FIELDS);

            WebElement biHost = allInputs.get(0);
            SearchContext biShadow = biHost.getShadowRoot();
            WebElement biInput = biShadow.findElement(SHADOW_INPUT);

            InputClearFeild.safeClearAndFocus(driver, biInput);
            biInput.sendKeys(houseBillValue);
            biInput.sendKeys(Keys.TAB);

            logger.info("✅ Business Identifier entered: " + houseBillValue);
            Thread.sleep(1000);

            // --------------------------------------------------------
            // ENTER TRANSACTION IDENTIFIER
            // --------------------------------------------------------

            logger.info("📌 [" + (i + 1) + "] Entering Transaction Identifier...");

            WebElement txnHost = allInputs.get(1);
            SearchContext txnShadow = txnHost.getShadowRoot();
            WebElement txnInput = txnShadow.findElement(SHADOW_INPUT);

            InputClearFeild.safeClearAndFocus(driver, txnInput);
            txnInput.sendKeys(transactionId);
            txnInput.sendKeys(Keys.TAB);

            logger.info("✅ Transaction Identifier entered: " + transactionId);
            Thread.sleep(1000);

            // --------------------------------------------------------
            // CLICK SEARCH
            // --------------------------------------------------------

            logger.info("📌 [" + (i + 1) + "] Clicking Search button...");

            WebElement searchBtn =
                    ShadowDom.waitForInnerClickable(
                            driver,
                            SEARCH_BTN_HOST,
                            SEARCH_BTN,
                            20,
                            logger);

            ShadowDom.jsClick(driver, searchBtn);
            logger.info("✅ Search button clicked");

            logger.info("⏳ [" + (i + 1) + "] Waiting for search results to load...");
            Thread.sleep(5000);

            logger.info("⏳ [" + (i + 1) + "] Waiting for expand arrows to appear...");
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(30))
                    .until(d -> {
                        List<WebElement> arrows =
                                ShadowDom.findAllDeep(driver,
                                        ANY_EXPAND_BUTTON_DEEP,
                                        logger);
                        return arrows != null && !arrows.isEmpty();
                    });
            logger.info("✅ Search results loaded and table ready for expansion");

            // --------------------------------------------------------
            // EXPAND (EXISTING WORKING LOGIC)
            // --------------------------------------------------------

            logger.info("📌 [" + (i + 1) + "] Expanding rows (SEEBURGER)...");
            expander.expandFirstRowThenSeeburger();
            logger.info("✅ Rows expanded");

            // --------------------------------------------------------
            // VALIDATE (EXISTING WORKING LOGIC)
            // --------------------------------------------------------

            logger.info("📌 [" + (i + 1) + "] Validating status...");
            statusValidation.validateStatusWithLogging(
                    "SEEBURGER",
                    expectedStatus,
                    houseBillValue,
                    transactionId,
                    record.getDbStatus()
            );

            logger.info("==================================================");
            logger.info("✅ RECORD " + (i + 1) + " — PASSED");
            logger.info("==================================================");
            Thread.sleep(2000);
        }

        logger.info("==================================================");
        logger.info("🎉 HOUSE BILL OF LADING VALIDATION COMPLETED");
        logger.info("   Total Records Validated: " + records.size());
        logger.info("==================================================");
    }
}
