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
import utils.StatusValidation;

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
    // STEP 4 — Expand Level 1 (Transaction) + Level 2 (Platform)
    //          Match DB origin system with UI platforms
    //          (Same pattern as Platform Identifier)
    // ============================================================

    public void expandByOriginSystem(String originSystem) throws InterruptedException {

        String dbSystem = originSystem.trim().toUpperCase();

        // ── STEP 1: Expand Level 1 (Transaction row) ──────────
        logger.info("⬇ Step 1: Expanding Level 1 (Transaction row)...");
        logger.info("⏳ Waiting 5 seconds for skeleton loading...");
        Thread.sleep(5000);

        List<WebElement> arrows = ShadowDom.findAllDeep(driver, ANY_EXPAND_BUTTON_DEEP, logger);
        if (arrows == null || arrows.isEmpty()) {
            throw new RuntimeException("❌ No Level 1 expand arrows found!");
        }

        WebElement firstArrow = arrows.get(0);
        ShadowDom.scrollIntoViewCenter(driver, firstArrow);
        ShadowDom.jsClick(driver, firstArrow);
        logger.info("✅ Level 1 (Transaction) expanded");

        Thread.sleep(3000);

        // ── STEP 2: Detect platforms visible in UI ────────────
        List<String> uiPlatforms = expander.getAvailablePlatforms();
        logger.info("🧠 Platforms visible in UI: " + uiPlatforms);
        logger.info("🗄 DB origin system: " + dbSystem);

        // ── STEP 3: Match DB origin with UI and expand ────────
        if (dbSystem.equals("SEEBURGER") && uiPlatforms.stream()
                .anyMatch(p -> p.equalsIgnoreCase("SEEBURGER"))) {

            logger.info("🟣 SEEBURGER found in UI — matches DB origin. Expanding SEEBURGER row...");
            expandPlatformRow("SEEBURGER");

        } else if (dbSystem.equals("AMPS") && uiPlatforms.stream()
                .anyMatch(p -> p.equalsIgnoreCase("AMPS"))) {

            logger.info("🔵 AMPS found in UI — matches DB origin. Expanding AMPS row...");
            expandPlatformRow("AMPS");

        } else {
            // Fallback: if only one platform visible, expand it
            logger.info("⚠ DB origin " + dbSystem + " not found in UI platforms: " + uiPlatforms);
            logger.info("⬇ Expanding first available platform as fallback...");

            arrows = ShadowDom.findAllDeep(driver, ANY_EXPAND_BUTTON_DEEP, logger);
            if (arrows.size() >= 2) {
                ShadowDom.scrollIntoViewCenter(driver, arrows.get(1));
                ShadowDom.jsClick(driver, arrows.get(1));
                Thread.sleep(2000);
                logger.info("✅ Fallback: Second arrow expanded");
            }
        }

        logger.info("✅ Level 1 (Transaction) + Level 2 (" + dbSystem + ") expansion complete");
    }

    // ============================================================
    // Expand a specific platform row by name (AMPS / SEEBURGER)
    // after Level 1 is already expanded
    // ============================================================

    private void expandPlatformRow(String platformName) throws InterruptedException {

        List<WebElement> platformCells = ShadowDom.findAllDeep(
                driver, PLATFORM_SYSTEM_NAME_DEEP, logger);

        for (WebElement cell : platformCells) {

            String text = cell.getText().trim();

            if (text.equalsIgnoreCase(platformName)) {

                logger.info("   ✅ " + platformName + " row located in UI");

                WebElement row = (WebElement) ((JavascriptExecutor) driver)
                        .executeScript("return arguments[0].closest('tr');", cell);

                if (row == null) {
                    throw new RuntimeException("❌ Could not find parent row for " + platformName);
                }

                // Find expand arrow inside this platform row
                List<WebElement> rowArrows = ShadowDom.findAllDeep(
                        driver, row, "button[aria-label='Expand row']", logger);

                if (rowArrows == null || rowArrows.isEmpty()) {
                    // Fallback: try mc-button shadow root approach
                    logger.info("   ⚠ No scoped arrow found, trying mc-button shadow root...");
                    WebElement mcButton = row.findElement(By.cssSelector("mc-button"));
                    WebElement arrow = mcButton.getShadowRoot()
                            .findElement(PLATFORM_ROW_EXPANDER_INNER);
                    ShadowDom.scrollIntoViewCenter(driver, arrow);
                    ShadowDom.jsClick(driver, arrow);
                } else {
                    WebElement arrow = rowArrows.get(0);
                    ShadowDom.scrollIntoViewCenter(driver, arrow);
                    ShadowDom.jsClick(driver, arrow);
                }

                logger.info("   ✅ " + platformName + " row expanded");
                Thread.sleep(3000);
                return;
            }
        }

        throw new RuntimeException("❌ Platform row not found in UI: " + platformName);
    }

    // ============================================================
    // STEP 6 — Validate Status vs DB
    // ============================================================

    public void validateStatus(String expectedStatus,
                               String originSystem,
                               String platformId,
                               String transactionId,
                               String dbRawStatus) {

        logger.info("🔍 Validating UI status (scan all, last row decides). Expected: " + expectedStatus);

        StatusValidation statusValidation = new StatusValidation(driver);
        statusValidation.validateBookingStatus(
                originSystem,
                expectedStatus,
                platformId,
                transactionId,
                dbRawStatus);
    }
}
