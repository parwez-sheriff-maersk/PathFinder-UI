package Pages;

import static Pages.PathFinderLocators.*;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import utils.DatabaseUtils;
import utils.ExpandDownArrows;
import utils.InputClearFeild;
import utils.PlatformRecord;
import utils.ShadowDom;
import utils.StatusMapper;
import utils.StatusValidation;

public class BusinessIdentifierHouseBillOfLadingPages {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final Logger logger =
            Logger.getLogger(BusinessIdentifierHouseBillOfLadingPages.class.getName());

    public BusinessIdentifierHouseBillOfLadingPages(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    public void searchAndValidateHouseBill(Properties prop) throws InterruptedException {

        logger.info("==================================================");
        logger.info("🚀 HOUSE BILL OF LADING VALIDATION STARTED");
        logger.info("==================================================");

        // ------------------------------------------------------------
        // OPEN BUSINESS IDENTIFIER DROPDOWN
        // ------------------------------------------------------------

        WebElement dropdown =
                wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("mc-select")));

        dropdown.click();
        Thread.sleep(1500);

        // ------------------------------------------------------------
        // SELECT HOUSE BILL OF LADING
        // ------------------------------------------------------------

        List<WebElement> options =
                ShadowDom.findAllDeep(driver, "mc-option", logger);

        for (WebElement option : options) {
            if (option.getText().trim()
                    .equalsIgnoreCase("HOUSE BILL OF LADING")) {

                ShadowDom.scrollIntoViewCenter(driver, option);
                ShadowDom.jsClick(driver, option);
                break;
            }
        }

        Thread.sleep(2000);

        // ------------------------------------------------------------
        // FETCH LATEST 5 FROM DB
        // ------------------------------------------------------------

        List<PlatformRecord> records =
                DatabaseUtils.getLatestHouseBillOfLadingSeeburger(prop);

        if (records == null || records.isEmpty()) {
            throw new RuntimeException("❌ No HOUSE BILL records found in DB!");
        }

        logger.info("📦 Total records fetched: " + records.size());

        // ✅ Create once (not inside loop)
        ExpandDownArrows expander = new ExpandDownArrows(driver);
        StatusValidation statusValidation = new StatusValidation(driver);

        // ------------------------------------------------------------
        // LOOP THROUGH EACH RECORD
        // ------------------------------------------------------------

        for (int i = 0; i < records.size(); i++) {

            PlatformRecord record = records.get(i);

            String houseBillValue = record.getPlatformId();
            String expectedStatus =
                    StatusMapper.mapDbToUiStatus(
                            record.getDbStatus(),
                            record.getOriginSystem());

            logger.info("==================================================");
            logger.info("🔎 Validating Record " + (i + 1));
            logger.info("📌 House Bill      : " + houseBillValue);
            logger.info("📌 DB Status       : " + record.getDbStatus());
            logger.info("📌 Expected UI Map : " + expectedStatus);
            logger.info("📌 Trace ID        : " + record.getTraceId());
            logger.info("==================================================");

            // --------------------------------------------------------
            // ENTER VALUE
            // --------------------------------------------------------

            WebElement valueHost =
                    wait.until(ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("mc-input.inline-input")));

            SearchContext shadow = valueHost.getShadowRoot();
            WebElement valueInput =
                    shadow.findElement(By.cssSelector("input"));

            InputClearFeild.safeClearAndFocus(driver, valueInput);

            valueInput.sendKeys(houseBillValue);
            valueInput.sendKeys(Keys.TAB);

            Thread.sleep(1000);

            // --------------------------------------------------------
            // CLICK SEARCH
            // --------------------------------------------------------

            WebElement searchBtn =
                    ShadowDom.waitForInnerClickable(
                            driver,
                            SEARCH_BTN_HOST,
                            SEARCH_BTN,
                            20,
                            logger);

            ShadowDom.jsClick(driver, searchBtn);
            Thread.sleep(3000);

            // --------------------------------------------------------
            // EXPAND USING EXISTING WORKING METHOD
            // --------------------------------------------------------

            expander.expandFirstRowThenSeeburger();

            // --------------------------------------------------------
            // VALIDATE USING EXISTING WORKING METHOD
            // --------------------------------------------------------

            statusValidation.validateStatusWithLogging(
                    "SEEBURGER",
                    expectedStatus,
                    houseBillValue,
                    record.getTraceId(),
                    record.getDbStatus()
            );

            logger.info("✅ SEEBURGER validation completed successfully.");
            Thread.sleep(2000);
        }

        logger.info("==================================================");
        logger.info("🎉 HOUSE BILL OF LADING VALIDATION COMPLETED");
        logger.info("==================================================");
    }
}