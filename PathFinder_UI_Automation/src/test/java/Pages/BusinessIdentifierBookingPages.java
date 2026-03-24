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

        String lastSearchedBooking = "";

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

            if (bookingValue.equals(lastSearchedBooking)) {

                logger.info("⚠ Duplicate booking detected → Performing refresh flow");

                driver.navigate().refresh();
                Thread.sleep(4000);

                logger.info("🔄 Re-clicking Trace Table tab after refresh...");
                NavigationUtils.clickTraceTableTab(driver);
                selectBookingFromDropdown();
                enterBookingAndSearch(bookingValue);

            } else {

                enterBookingAndSearch(bookingValue);
            }

            lastSearchedBooking = bookingValue;

            logger.info("📌 [" + (i + 1) + "] Expanding rows (AMPS)...");
            expander.expandFirstRowThenAmps();
            logger.info("✅ Rows expanded");

            logger.info("📌 [" + (i + 1) + "] Validating AMPS status...");
            statusValidation.validatePlatformStatus(
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

        logger.info("⏳ Waiting for expand arrows to appear...");
        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(20))
                .until(d -> {
                    List<WebElement> arrows =
                            ShadowDom.findAllDeep(driver,
                                    ANY_EXPAND_BUTTON_DEEP,
                                    logger);
                    return arrows != null && !arrows.isEmpty();
                });

        logger.info("✅ Search results loaded and table ready for expansion");
    }
}