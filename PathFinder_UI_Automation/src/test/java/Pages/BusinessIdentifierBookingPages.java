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

public class BusinessIdentifierBookingPages {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final Logger logger =
            Logger.getLogger(BusinessIdentifierBookingPages.class.getName());

    public BusinessIdentifierBookingPages(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    // ============================================================
    // CLICK TRACE TABLE TAB
    // ============================================================

    public void clickTraceTableTab() {

        logger.info("🔽 Clicking Trace Table Tab...");

        WebElement traceTab =
                wait.until(ExpectedConditions.elementToBeClickable(
                        TRACE_TABLE_TAB));

        traceTab.click();

        logger.info("✅ Trace Table Tab clicked");

        // Wait until dropdown appears (table fully loaded)
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("mc-select")));

        logger.info("✅ Trace Table fully loaded");
    }

    // ============================================================
    // SEARCH + VALIDATE BOOKING NUMBER
    // ============================================================

    public void searchAndValidateBooking(Properties prop) throws InterruptedException {

        logger.info("==================================================");
        logger.info("🚀 BOOKING NUMBER VALIDATION STARTED");
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
        // SELECT BOOKING NUMBER (EXACT MATCH)
        // ------------------------------------------------------------

        List<WebElement> options =
                ShadowDom.findAllDeep(driver, "mc-option", logger);

        boolean found = false;

        for (WebElement option : options) {

            String text = option.getText()
                    .trim()
                    .replaceAll("\\s+", " ");

            if (text.equalsIgnoreCase("BOOKING NUMBER")) {

                logger.info("🎯 Exact match found: " + text);

                ShadowDom.scrollIntoViewCenter(driver, option);
                Thread.sleep(800);
                ShadowDom.jsClick(driver, option);

                found = true;
                break;
            }
        }

        if (!found) {
            throw new RuntimeException("❌ Exact 'BOOKING NUMBER' option not found in dropdown!");
        }

        Thread.sleep(2000);
        logger.info("✅ BOOKING NUMBER selected successfully");

        // ------------------------------------------------------------
        // FETCH LATEST 5 FROM DB
        // ------------------------------------------------------------

        List<PlatformRecord> records =
                DatabaseUtils.getLatestBookingNumberAmps(prop);

        if (records == null || records.isEmpty()) {
            throw new RuntimeException("❌ No BOOKING records found in DB!");
        }

        logger.info("📦 Total records fetched: " + records.size());

        ExpandDownArrows expander = new ExpandDownArrows(driver);
        StatusValidation statusValidation = new StatusValidation(driver);

        String lastSearchedBooking = "";

        // ------------------------------------------------------------
        // LOOP THROUGH EACH RECORD
        // ------------------------------------------------------------

        for (int i = 0; i < records.size(); i++) {

            PlatformRecord record = records.get(i);

            String bookingValue = record.getPlatformId();

            String expectedStatus =
                    StatusMapper.mapDbToUiStatus(
                            record.getDbStatus(),
                            record.getOriginSystem());

            logger.info("==================================================");
            logger.info("🔎 Validating Record " + (i + 1));
            logger.info("📌 Booking Number  : " + bookingValue);
            logger.info("📌 DB Status       : " + record.getDbStatus());
            logger.info("📌 Expected UI Map : " + expectedStatus);
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

            valueInput.sendKeys(bookingValue);
            valueInput.sendKeys(Keys.TAB);

            Thread.sleep(1000);

            // --------------------------------------------------------
            // CLICK SEARCH (Skip wait if same booking)
            // --------------------------------------------------------

            if (!bookingValue.equals(lastSearchedBooking)) {

                WebElement searchBtn =
                        ShadowDom.waitForInnerClickable(
                                driver,
                                SEARCH_BTN_HOST,
                                SEARCH_BTN,
                                20,
                                logger);

                ShadowDom.jsClick(driver, searchBtn);

                logger.info("🔎 Search clicked. Waiting for table data...");

                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

                wait.until(d -> {
                    List<WebElement> arrows =
                            ShadowDom.findAllDeep(driver,
                                    ANY_EXPAND_BUTTON_DEEP,
                                    logger);
                    return arrows != null && arrows.size() > 0;
                });

                logger.info("✅ Table ready for expansion");

            } else {
                logger.info("⚠ Same booking detected. Skipping refresh wait.");
            }

            lastSearchedBooking = bookingValue;

            // --------------------------------------------------------
            // WAIT 5 SECONDS BEFORE EXPANSION (UI SLOW SAFETY)
            // --------------------------------------------------------

            Thread.sleep(5000);

            // --------------------------------------------------------
            // EXPAND → AMPS
            // --------------------------------------------------------

            expander.expandFirstRowThenAmps();

            // --------------------------------------------------------
            // VALIDATE AMPS
            // --------------------------------------------------------

            statusValidation.validateStatusForPlatform(
                    "AMPS",
                    expectedStatus
            );

            logger.info("✅ AMPS validation completed successfully.");

            Thread.sleep(2000);

            // --------------------------------------------------------
            // OPTIONAL: REFRESH PAGE FOR STABILITY
            // --------------------------------------------------------

            driver.navigate().refresh();
            Thread.sleep(4000);

            // Re-open Trace Table after refresh
            clickTraceTableTab();
        }

        logger.info("==================================================");
        logger.info("🎉 BOOKING NUMBER VALIDATION COMPLETED");
        logger.info("==================================================");
    }
}