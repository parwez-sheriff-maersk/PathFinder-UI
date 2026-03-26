package Pages;

import static Pages.PathFinderLocators.*;

import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.openqa.selenium.*;

import utils.DatabaseUtils;
import utils.ExpandDownArrows;
import utils.InputClearFeild;
import utils.NavigationUtils;
import utils.PlatformRecord;
import utils.ShadowDom;
import utils.StatusMapper;
import utils.StatusValidation;
import utils.WaitUtils;

public class BusinessIdentifierBookingPages {

    private final WebDriver driver;

    private static final Logger logger =
            Logger.getLogger(BusinessIdentifierBookingPages.class.getName());

    private static final int MAX_RETRY = 2;

    public BusinessIdentifierBookingPages(WebDriver driver) {
        this.driver = driver;
    }

    // ============================================================
    // TRACE TAB (reusable from NavigationUtils)
    // ============================================================

    public void clickTraceTableTab() {
        NavigationUtils.clickTraceTableTab(driver);
    }

    // ============================================================
    // SEARCH + VALIDATE BOOKING NUMBER
    // ============================================================

    public void searchAndValidateBooking(Properties prop) throws InterruptedException {

        logger.info("==================================================");
        logger.info("🚀 BOOKING NUMBER VALIDATION STARTED");
        logger.info("==================================================");

        selectBookingFromDropdown();

        List<PlatformRecord> records =
                DatabaseUtils.getLatestBookingNumberAmps(prop);

        if (records == null || records.isEmpty()) {
            throw new RuntimeException("❌ No BOOKING records found in DB!");
        }

        logger.info("📦 Total records fetched: " + records.size());

        ExpandDownArrows expander = new ExpandDownArrows(driver);
        StatusValidation statusValidation = new StatusValidation(driver);

        for (int i = 0; i < records.size(); i++) {

            PlatformRecord record = records.get(i);

            String bookingValue = record.getPlatformId();
            String traceId = record.getTraceId();
            String dbRawStatus = record.getDbStatus();
            String originSystem = record.getOriginSystem();

            String expectedStatus =
                    StatusMapper.mapDbToUiStatus(
                            dbRawStatus,
                            originSystem);

            logger.info("==================================================");
            logger.info("🔁 RECORD " + (i + 1) + " OF " + records.size());
            logger.info("   Booking Number  : " + bookingValue);
            logger.info("   Trace ID        : " + traceId);
            logger.info("   Origin System   : " + originSystem);
            logger.info("   DB Status       : " + dbRawStatus);
            logger.info("   Expected UI Map : " + expectedStatus);
            logger.info("==================================================");

            // Always refresh before 2nd+ search to reset Shadow DOM state
            if (i > 0) {
                logger.info("🔄 Refreshing page before next search...");
                driver.navigate().refresh();
                Thread.sleep(4000);

                logger.info("🔄 Re-clicking Trace Table tab after refresh...");
                NavigationUtils.clickTraceTableTab(driver);
                selectBookingFromDropdown();
            }

            // Enter booking and search — with "No matching records" retry
            enterBookingAndSearchWithRetry(bookingValue);

            logger.info("📌 [" + (i + 1) + "] Expanding rows (AMPS)...");
            expander.expandFirstRowThenAmps();
            logger.info("✅ Rows expanded");

            logger.info("📌 [" + (i + 1) + "] Validating AMPS status (scan all, last row decides)...");
            statusValidation.validateBookingStatus(
                    "AMPS",
                    expectedStatus,
                    bookingValue,
                    traceId,
                    dbRawStatus
            );

            logger.info("==================================================");
            logger.info("✅ RECORD " + (i + 1) + " — PASSED");
            logger.info("==================================================");

            Thread.sleep(2000);
        }

        logger.info("==================================================");
        logger.info("🎉 BOOKING NUMBER VALIDATION COMPLETED");
        logger.info("   Total Records Validated: " + records.size());
        logger.info("==================================================");
    }

    // ============================================================
    // SELECT BOOKING FROM DROPDOWN
    // ============================================================

    private void selectBookingFromDropdown() throws InterruptedException {

        logger.info("📌 Opening Business Identifier dropdown...");
        WebElement dropdown =
                WaitUtils.waitForElementClickable(driver, MC_SELECT_DROPDOWN, 30, logger);
        dropdown.click();
        Thread.sleep(1500);
        logger.info("✅ Dropdown opened");

        logger.info("📌 Selecting 'BOOKING NUMBER' option...");
        List<WebElement> options =
                ShadowDom.findAllDeep(driver, DROPDOWN_OPTION_DEEP, logger);

        for (WebElement option : options) {

            String text = option.getText()
                    .trim()
                    .replaceAll("\\s+", " ");

            if (text.equalsIgnoreCase("BOOKING NUMBER")) {

                ShadowDom.scrollIntoViewCenter(driver, option);
                Thread.sleep(800);
                ShadowDom.jsClick(driver, option);
                logger.info("✅ 'BOOKING NUMBER' selected");
                break;
            }
        }

        Thread.sleep(2000);
        logger.info("✅ BOOKING NUMBER selection completed");
    }

    // ============================================================
    // ENTER BOOKING + SEARCH WITH RETRY ON "NO MATCHING RECORDS"
    // ============================================================

    private void enterBookingAndSearchWithRetry(String bookingValue) throws InterruptedException {

        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {

            logger.info("🔍 Search attempt " + attempt + " for: " + bookingValue);
            enterBookingAndSearch(bookingValue);

            // Check if "No matching records found" is displayed
            if (isNoMatchingRecordsFound()) {

                logger.info("⚠ 'No matching records found' detected on attempt " + attempt);

                if (attempt < MAX_RETRY) {
                    logger.info("🔄 Refreshing and retrying...");
                    driver.navigate().refresh();
                    Thread.sleep(4000);

                    NavigationUtils.clickTraceTableTab(driver);
                    selectBookingFromDropdown();
                } else {
                    throw new RuntimeException(
                            "❌ No matching records found in UI for Booking: " + bookingValue
                            + " after " + MAX_RETRY + " attempts");
                }

            } else {
                logger.info("✅ Search results found for: " + bookingValue);
                return;
            }
        }
    }

    // ============================================================
    // DETECT "NO MATCHING RECORDS FOUND" SCREEN
    // ============================================================

    private boolean isNoMatchingRecordsFound() {

        try {
            String pageSource = driver.getPageSource();
            if (pageSource != null && pageSource.contains("No matching records found")) {
                return true;
            }

            // Also check via Shadow DOM text content
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String bodyText = (String) js.executeScript(
                    "return document.body.innerText || document.body.textContent || '';");

            if (bodyText != null && bodyText.contains("No matching records found")) {
                return true;
            }
        } catch (Exception e) {
            logger.warning("⚠ Could not check for 'No matching records' text: " + e.getMessage());
        }

        return false;
    }

    // ============================================================
    // ENTER BOOKING + CLICK SEARCH
    // ============================================================

    private void enterBookingAndSearch(String bookingValue) throws InterruptedException {

        logger.info("📝 Entering Booking Number: " + bookingValue);

        WaitUtils.waitForElementVisible(driver, INLINE_INPUT_FIELDS, 30, logger);
        WebElement valueHost = driver.findElement(INLINE_INPUT_FIELDS);

        SearchContext shadow = valueHost.getShadowRoot();
        WebElement valueInput = shadow.findElement(SHADOW_INPUT);

        InputClearFeild.safeClearAndFocus(driver, valueInput);

        valueInput.sendKeys(bookingValue);
        valueInput.sendKeys(Keys.TAB);
        logger.info("✅ Booking Number entered: " + bookingValue);

        Thread.sleep(1000);

        logger.info("🔍 Clicking Search button...");
        WebElement searchBtn =
                ShadowDom.waitForInnerClickable(
                        driver,
                        SEARCH_BTN_HOST,
                        SEARCH_BTN,
                        20,
                        logger);

        ShadowDom.jsClick(driver, searchBtn);
        logger.info("✅ Search button clicked");

        logger.info("⏳ Waiting for search results to load...");
        Thread.sleep(5000);
    }
}
