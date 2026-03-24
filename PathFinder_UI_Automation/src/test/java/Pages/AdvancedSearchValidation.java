package Pages;

import static Pages.PathFinderLocators.*;

import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import utils.AdvancedSearchRecord;
import utils.ExpandDownArrows;
import utils.InputClearFeild;
import utils.NavigationUtils;
import utils.ShadowDom;

public class AdvancedSearchValidation {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final ExpandDownArrows expander;

    private static final Logger logger =
            Logger.getLogger(AdvancedSearchValidation.class.getName());

    public AdvancedSearchValidation(WebDriver driver) {
        this.driver   = driver;
        this.wait     = new WebDriverWait(driver, Duration.ofSeconds(30));
        this.expander = new ExpandDownArrows(driver);
    }

    // ============================================================
    // STEP 1 — Navigate to Trace Table
    // ============================================================

    public void clickTraceTableTab() {
        logger.info("📌 Navigating to Trace Table using NavigationUtils...");
        NavigationUtils.clickTraceTableTab(driver);
        logger.info("✅ Trace Table navigation complete");
    }

    // ============================================================
    // STEP 2 — Open Advanced Search Drawer
    // ============================================================

    public void clickAdvancedSearch() {

        logger.info("🔎 Attempting to open Advanced Search drawer...");

        new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(30))
                .pollingEvery(Duration.ofMillis(800))
                .ignoring(Exception.class)
                .until(d -> {
                    if (!d.findElements(ADV_DRAWER_OPEN).isEmpty()) {
                        return true;
                    }
                    ((JavascriptExecutor) d).executeScript(JS_CLICK_ADVANCED_SEARCH_BTN);
                    return null;
                });

        logger.info("✅ Advanced Search drawer is now open");
    }

    // ============================================================
    // STEP 3 — Enter Transaction ID and Platform ID, then Search
    // ============================================================

    public void enterSearchCriteriaAndSearch(AdvancedSearchRecord record)
            throws InterruptedException {

        logger.info("🔄 Clearing Transaction ID field...");
        logger.info("📝 Entering Transaction ID: " + record.getTransactionId());
        enterIntoMcInput(ADV_DRAWER_TRANSACTION_HOST, record.getTransactionId());
        logger.info("✅ Transaction ID entered successfully");

        logger.info("🔄 Clearing Platform ID field...");
        logger.info("📝 Entering Platform ID: " + record.getPlatformId());
        enterIntoMcInput(ADV_DRAWER_PLATFORM_HOST, record.getPlatformId());
        logger.info("✅ Platform ID entered successfully");

        logger.info("🔍 Clicking Search button in Advanced Search drawer...");
        clickDrawerSearchButton();

        logger.info("⏳ Waiting for search results to load...");
        waitForResults();

        logger.info("⏳ Waiting 5 seconds for data to fully render...");
        Thread.sleep(5000);
        logger.info("✅ Search results loaded successfully");
    }

    private void enterIntoMcInput(By hostLocator, String value) {

        logger.info("   ➡ Locating input field: " + hostLocator);
        WebElement host = wait.until(
                ExpectedConditions.presenceOfElementLocated(hostLocator));

        SearchContext shadow = host.getShadowRoot();
        WebElement input = shadow.findElement(SHADOW_INPUT);

        logger.info("   ➡ Clearing field using InputClearFeild utility...");
        InputClearFeild.safeClearAndFocus(driver, input);

        logger.info("   ➡ Typing value: " + value);
        input.sendKeys(value);
        input.sendKeys(Keys.TAB);
        logger.info("   ✅ Value entered and tab pressed");
    }

    private void clickDrawerSearchButton() {

        logger.info("🔍 Locating Search button inside drawer...");

        new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(20))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(Exception.class)
                .until(d -> {
                    ((JavascriptExecutor) d).executeScript(JS_CLICK_DRAWER_SEARCH_BTN);
                    return true;
                });

        logger.info("✅ Search button clicked successfully");
    }

    private void waitForResults() {

        logger.info("⏳ Polling for expand row buttons to appear...");

        new WebDriverWait(driver, Duration.ofSeconds(30)).until(d -> {
            List<WebElement> rows = ShadowDom.findAllDeep(
                    d, EXPAND_ROW_BTN_DEEP, logger);
            return rows != null && !rows.isEmpty();
        });

        logger.info("✅ Expand row buttons detected — results are ready");
    }

    // ============================================================
    // STEP 4 — Expand Row 1 + Row 2 (using AMPS method)
    // ============================================================

    public void expandRow1() throws InterruptedException {

        logger.info("⬇ Expanding Row 1 + Row 2 using expandFirstRowThenAmps()...");
        expander.expandFirstRowThenAmps();
        logger.info("✅ Row 1 (Transaction) + Row 2 (Platform) expanded");
    }

    // ============================================================
    // STEP 5 — Expand Row 2 (already done by expandFirstRowThenAmps)
    // ============================================================

    public void expandRow2(String originSystem) throws InterruptedException {

        logger.info("ℹ Row 2 (" + originSystem + ") already expanded by AMPS method — skipping");
    }

    // ============================================================
    // STEP 6 — Validate Status vs DB
    // ============================================================

    public void validateStatus(String expectedStatus) {

        logger.info("🔍 Validating UI status against expected: " + expectedStatus);

        logger.info("   ➡ Searching for status cells using: " + STATUS_CELL_DEEP);
        List<WebElement> statusCells = ShadowDom.findAllDeep(
                driver, STATUS_CELL_DEEP, logger);

        if (statusCells == null || statusCells.isEmpty()) {
            logger.info("   ⚠ No status cells found, trying badge fallback: " + GENERIC_BADGE_DEEP);
            statusCells = ShadowDom.findAllDeep(driver, GENERIC_BADGE_DEEP, logger);
        }

        if (statusCells == null || statusCells.isEmpty()) {
            throw new RuntimeException("❌ No status cells found in expanded row");
        }

        logger.info("   ➡ Found " + statusCells.size() + " status cells, checking values...");

        boolean matched = false;

        for (WebElement cell : statusCells) {
            String uiStatus = cell.getText().trim();
            if (!uiStatus.isEmpty()) {
                logger.info("   ➡ UI Status: [" + uiStatus + "] vs Expected: [" + expectedStatus + "]");
                if (uiStatus.equalsIgnoreCase(expectedStatus)) {
                    matched = true;
                    logger.info("   ✅ STATUS MATCHED: " + uiStatus + " == " + expectedStatus);
                    break;
                }
            }
        }

        if (!matched) {
            throw new RuntimeException(
                    "❌ Status MISMATCH! Expected: [" + expectedStatus +
                    "] but not found in UI status cells.");
        }
    }
}
