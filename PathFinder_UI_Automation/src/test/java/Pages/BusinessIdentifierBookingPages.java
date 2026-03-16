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

        for (PlatformRecord record : records) {

            String bookingValue = record.getPlatformId();
            String traceId = record.getTraceId();
            String dbRawStatus = record.getDbStatus();
            String originSystem = record.getOriginSystem();

            String expectedStatus =
                    StatusMapper.mapDbToUiStatus(
                            dbRawStatus,
                            originSystem);

            logger.info("--------------------------------------------------");
            logger.info("📌 Booking Number  : " + bookingValue);
            logger.info("📌 Trace ID        : " + traceId);
            logger.info("📌 DB Status       : " + dbRawStatus);
            logger.info("📌 Expected UI Map : " + expectedStatus);
            logger.info("--------------------------------------------------");

            if (bookingValue.equals(lastSearchedBooking)) {

                logger.info("⚠ Duplicate booking detected → Performing refresh flow");

                driver.navigate().refresh();
                Thread.sleep(4000);

                clickTraceTableTab();
                selectBookingFromDropdown();
                enterBookingAndSearch(bookingValue);

            } else {

                enterBookingAndSearch(bookingValue);
            }

            lastSearchedBooking = bookingValue;

            Thread.sleep(5000);

            expander.expandFirstRowThenAmps();

            statusValidation.validatePlatformStatus(
                    "AMPS",
                    expectedStatus,
                    bookingValue,
                    traceId,
                    dbRawStatus
            );

            logger.info("✅ AMPS validation completed successfully.");

            Thread.sleep(2000);
        }

        logger.info("==================================================");
        logger.info("🎉 BOOKING NUMBER VALIDATION COMPLETED");
        logger.info("==================================================");
    }

    // ============================================================
    // SELECT BOOKING FROM DROPDOWN
    // ============================================================

    private void selectBookingFromDropdown() throws InterruptedException {

        WebElement dropdown =
                wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("mc-select")));

        dropdown.click();
        Thread.sleep(1500);

        List<WebElement> options =
                ShadowDom.findAllDeep(driver, "mc-option", logger);

        for (WebElement option : options) {

            String text = option.getText()
                    .trim()
                    .replaceAll("\\s+", " ");

            if (text.equalsIgnoreCase("BOOKING NUMBER")) {

                ShadowDom.scrollIntoViewCenter(driver, option);
                Thread.sleep(800);
                ShadowDom.jsClick(driver, option);
                break;
            }
        }

        Thread.sleep(2000);
        logger.info("✅ BOOKING NUMBER selected successfully");
    }

    // ============================================================
    // ENTER BOOKING + CLICK SEARCH
    // ============================================================

    private void enterBookingAndSearch(String bookingValue) throws InterruptedException {

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

        WebElement searchBtn =
                ShadowDom.waitForInnerClickable(
                        driver,
                        SEARCH_BTN_HOST,
                        SEARCH_BTN,
                        20,
                        logger);

        ShadowDom.jsClick(driver, searchBtn);

        logger.info("🔎 Search clicked. Waiting for table data...");

        wait.until(d -> {
            List<WebElement> arrows =
                    ShadowDom.findAllDeep(driver,
                            ANY_EXPAND_BUTTON_DEEP,
                            logger);
            return arrows != null && arrows.size() > 0;
        });

        logger.info("✅ Table ready for expansion");
    }
}