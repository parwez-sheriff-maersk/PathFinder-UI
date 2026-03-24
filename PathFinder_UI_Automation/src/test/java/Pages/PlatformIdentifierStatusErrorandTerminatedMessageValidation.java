package Pages;

import static Pages.PathFinderLocators.*;

import java.util.List;
import java.util.logging.Logger;

import org.openqa.selenium.*;

import utils.ExpandDownArrows;
import utils.InputClearFeild;
import utils.NavigationUtils;
import utils.ShadowDom;
import utils.StatusValidation;
import utils.WaitUtils;

public class PlatformIdentifierStatusErrorandTerminatedMessageValidation {

    private final WebDriver driver;

    private static final Logger logger =
            Logger.getLogger(
                    PlatformIdentifierStatusErrorandTerminatedMessageValidation.class.getName());

    public PlatformIdentifierStatusErrorandTerminatedMessageValidation(WebDriver driver) {
        this.driver = driver;
    }

    // ============================================================
    // TRACE TAB (reusable from NavigationUtils)
    // ============================================================

    public void clickTraceTableTab() {
        NavigationUtils.clickTraceTableTab(driver);
    }

    // ============================================================
    // SEARCH
    // ============================================================

    public void updatePlatformIdentifierAndSearch(String newPlatformId)
            throws InterruptedException {

        logger.info("📌 Collapsing any expanded rows...");
        collapseAllExpandedRows();

        logger.info("📌 Finding Platform Identifier input...");
        WebElement input = findPlatformInputByLabel(20);

        logger.info("🔄 Clearing Platform Identifier field...");
        InputClearFeild.safeClearAndFocus(driver, input);

        logger.info("📝 Entering Platform ID: " + newPlatformId);
        input.sendKeys(newPlatformId);
        input.sendKeys(Keys.TAB);

        logger.info("🔍 Clicking Search button...");
        clickSearchButton();

        logger.info("⏳ Waiting for search results to load...");
        Thread.sleep(5000);
        logger.info("✅ Search completed for Platform ID: " + newPlatformId);
    }

    private void clickSearchButton() {

        logger.info("   ➡ Locating Search button...");
        WebElement searchBtn = ShadowDom.waitForInnerClickable(
                driver, SEARCH_BTN_HOST, SEARCH_BTN, 20, logger);

        WaitUtils.waitForElementClickable(driver,
                SEARCH_BTN_HOST, 10, logger);

        ShadowDom.jsClick(driver, searchBtn);
        logger.info("   ✅ Search button clicked");
    }

    private WebElement findPlatformInputByLabel(int timeoutSeconds) {

        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < timeoutSeconds * 1000L) {

            List<WebElement> hosts = driver.findElements(PLATFORM_HOSTS);

            for (WebElement host : hosts) {

                try {
                    SearchContext root = host.getShadowRoot();
                    if (root == null) continue;

                    WebElement input = root.findElement(INNER_INPUT);
                    String placeholder = input.getAttribute("placeholder");

                    if (placeholder != null &&
                            placeholder.toLowerCase().contains("platform")) {

                        logger.info("   ✅ Platform input found");
                        return input;
                    }

                } catch (Exception ignored) {}
            }

            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }

        throw new TimeoutException("❌ Platform input not found.");
    }

    // ============================================================
    // AMPS EXPANSION + VALIDATION
    // ============================================================

    public void expandAndValidateAmps(String expectedStatus) throws InterruptedException {

        logger.info("🔵 ===== PROCESSING AMPS =====");

        logger.info("⬇ Expanding Level 1 (Transaction)...");
        expandFirstVisibleArrow();
        Thread.sleep(3000);

        logger.info("⬇ Expanding Level 2 (AMPS row)...");
        expandAmpsRow();
        Thread.sleep(3000);

        logger.info("🔍 Validating AMPS status. Expected: " + expectedStatus);
        StatusValidation statusValidation = new StatusValidation(driver);
        statusValidation.validateVisibleStatus(expectedStatus);

        logger.info("✅ AMPS validation completed successfully.");
    }

    // ============================================================
    // SEEBURGER EXPANSION + VALIDATION
    // ============================================================

    public void expandAndValidateSeeburger(String expectedStatus) throws InterruptedException {

        logger.info("🔽 Scrolling to SEEBURGER row before expansion...");

        try {
            List<WebElement> platforms = ShadowDom.findAllDeep(
                    driver, PLATFORM_SYSTEM_NAME_DEEP, logger);

            for (WebElement p : platforms) {

                if (p.isDisplayed() &&
                    p.getText().trim().equalsIgnoreCase("SEEBURGER")) {

                    ((JavascriptExecutor) driver)
                            .executeScript(
                                "arguments[0].scrollIntoView({block:'center'});", p);

                    Thread.sleep(1000);
                    logger.info("✅ Scrolled to SEEBURGER row.");
                    break;
                }
            }

        } catch (Exception e) {
            logger.warning("⚠ Scroll before SEEBURGER expansion failed: " + e.getMessage());
        }

        logger.info("🟣 ===== PROCESSING SEEBURGER =====");

        ExpandDownArrows expander = new ExpandDownArrows(driver);
        expander.expandSeeburgerByIndex();

        logger.info("🔍 Validating SEEBURGER status. Expected: " + expectedStatus);
        StatusValidation statusValidation = new StatusValidation(driver);
        statusValidation.validateStatusForPlatform("SEEBURGER", expectedStatus);

        logger.info("✅ SEEBURGER validation completed successfully.");
    }

    public void expandSeeburgerRow() {

        logger.info("🔎 Expanding SEEBURGER row specifically...");

        List<WebElement> platformCells =
                ShadowDom.findAllDeep(driver, PLATFORM_SYSTEM_NAME_DEEP, logger);

        if (platformCells == null || platformCells.isEmpty()) {
            throw new RuntimeException("❌ No platform rows found!");
        }

        for (WebElement platformElement : platformCells) {

            String text = platformElement.getText().trim();
            logger.info("   ➡ Found platform: " + text);

            if (text.equalsIgnoreCase("SEEBURGER")) {

                logger.info("   ✅ SEEBURGER row located");

                WebElement row = (WebElement) ((JavascriptExecutor) driver)
                        .executeScript("return arguments[0].closest('tr');", platformElement);

                WebElement chevron =
                        row.findElement(By.cssSelector("svg[aria-label='chevron-down']"));

                ShadowDom.scrollIntoViewCenter(driver, chevron);
                ShadowDom.jsClick(driver, chevron);

                logger.info("   ✅ SEEBURGER arrow clicked");

                try { Thread.sleep(2000); } catch (Exception ignored) {}

                return;
            }
        }

        throw new RuntimeException("❌ SEEBURGER platform row not found");
    }

    public void expandPlatformRow(String platformName) {

        logger.info("🔎 Expanding row for: " + platformName);

        WebElement row = driver.findElement(
                By.cssSelector("tr[data-cy='" + platformName + "']"));

        logger.info("   ✅ Row found for " + platformName);

        WebElement mcButton = row.findElement(
                By.cssSelector("td.mds-table__column--row-expander mc-button"));

        SearchContext shadowRoot = mcButton.getShadowRoot();

        WebElement innerButton = shadowRoot.findElement(PLATFORM_ROW_EXPANDER_INNER);

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", innerButton);

        innerButton.click();

        logger.info("   ✅ " + platformName + " expanded successfully");

        try { Thread.sleep(2000); } catch (Exception ignored) {}
    }

    // ============================================================
    // EXPAND AMPS ROW SPECIFICALLY
    // ============================================================

    public void expandAmpsRow() {

        logger.info("🔎 Expanding AMPS row specifically...");

        List<WebElement> platformCells =
                ShadowDom.findAllDeep(driver, PLATFORM_SYSTEM_NAME_DEEP, logger);

        for (WebElement cell : platformCells) {

            String text = cell.getText().trim();
            logger.info("   ➡ Found platform: " + text);

            if (text.equalsIgnoreCase("AMPS")) {

                logger.info("   ✅ AMPS row located");

                WebElement row = (WebElement) ((JavascriptExecutor) driver)
                        .executeScript("return arguments[0].closest('tr')", cell);

                if (row == null) {
                    throw new RuntimeException("❌ Could not locate parent row for AMPS");
                }

                WebElement host = row.findElement(By.cssSelector("mc-button"));

                WebElement arrow =
                        host.getShadowRoot()
                                .findElement(PLATFORM_ROW_EXPANDER_INNER);

                ShadowDom.scrollIntoViewCenter(driver, arrow);
                ShadowDom.jsClick(driver, arrow);

                logger.info("   ✅ AMPS arrow clicked");

                try { Thread.sleep(2000); } catch (Exception ignored) {}

                return;
            }
        }

        throw new RuntimeException("❌ AMPS row not found");
    }

    // ============================================================
    // WAIT FOR EXPAND ARROWS
    // ============================================================

    public void waitForExpandArrowsAfterSearch() {

        logger.info("⏳ Waiting for expand arrows to appear (deep search)...");

        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(20))
                .until(d -> {
                    List<WebElement> arrows = ShadowDom.findAllDeep(
                            driver, EXPAND_ROW_BTN_DEEP, logger);
                    return arrows != null && !arrows.isEmpty();
                });

        logger.info("✅ Expand arrows found");
    }

    // ============================================================
    // COLLAPSE
    // ============================================================

    private void collapseAllExpandedRows()
            throws InterruptedException {

        logger.info("🔄 Collapsing all expanded rows...");

        List<WebElement> arrows =
                ShadowDom.findAllDeep(driver, EXPAND_ROW_BTN_DEEP, logger);

        int collapsed = 0;

        for (WebElement arrow : arrows) {

            try {
                String expanded = arrow.getAttribute("aria-expanded");

                if ("true".equalsIgnoreCase(expanded)) {

                    ShadowDom.scrollIntoViewCenter(driver, arrow);
                    ShadowDom.jsClick(driver, arrow);
                    collapsed++;
                    Thread.sleep(500);
                }

            } catch (Exception ignored) {}
        }

        logger.info("✅ Collapsed " + collapsed + " rows");
        Thread.sleep(1000);
    }

    public void expandFirstVisibleArrow() {

        logger.info("⬇ Expanding first visible arrow...");

        List<WebElement> arrows =
                ShadowDom.findAllDeep(driver, ANY_EXPAND_BUTTON_DEEP, logger);

        if (arrows.isEmpty()) {
            throw new RuntimeException("❌ No expand arrows found!");
        }

        WebElement firstArrow = arrows.get(0);

        ShadowDom.scrollIntoViewCenter(driver, firstArrow);
        ShadowDom.jsClick(driver, firstArrow);

        logger.info("✅ First transaction arrow clicked");

        try { Thread.sleep(2000); } catch (Exception ignored) {}
    }

    public void expandFirstTransactionRow() {

        logger.info("⬇ Expanding Level 1 (Transaction)...");

        List<WebElement> arrows =
                ShadowDom.findAllDeep(driver, EXPAND_ROW_BTN_DEEP, logger);

        if (arrows == null || arrows.isEmpty()) {
            logger.info("ℹ No Level 1 arrow found. Possibly already expanded.");
            return;
        }

        try {
            WebElement firstArrow = arrows.get(0);

            ShadowDom.scrollIntoViewCenter(driver, firstArrow);
            ShadowDom.jsClick(driver, firstArrow);

            logger.info("✅ Level 1 expanded");
            Thread.sleep(2000);

        } catch (Exception e) {
            logger.info("ℹ Level 1 expansion skipped (already expanded)");
        }
    }

    // ============================================================
    // SMART SEEBURGER HANDLER
    // ============================================================

    public void expandAndValidateSeeburgerSmart(String expectedStatus) throws InterruptedException {

        logger.info("🟣 ===== SMART SEEBURGER PROCESSING =====");

        ExpandDownArrows expander = new ExpandDownArrows(driver);

        expander.expandFirstTransactionRow();

        List<String> platforms = expander.getAvailablePlatforms();

        if (platforms.size() == 1 && platforms.contains("SEEBURGER")) {

            logger.info("🔹 Only SEEBURGER present");
            expander.expandSeeburgerByIndex();
            new StatusValidation(driver).validateVisibleStatus(expectedStatus);
            return;
        }

        if (platforms.contains("AMPS")) {

            logger.info("🔵 Expanding AMPS first...");
            expander.expandPlatformRowInsideExpandedSection("AMPS");
            new StatusValidation(driver).validateVisibleStatus(expectedStatus);
            Thread.sleep(1000);
        }

        if (platforms.contains("SEEBURGER")) {

            logger.info("🟣 Expanding SEEBURGER after AMPS...");
            expander.expandSeeburgerByIndex();
            new StatusValidation(driver).validateVisibleStatus(expectedStatus);
        }

        logger.info("✅ SMART SEEBURGER validation completed.");
    }

    public void scrollToSeeburgerRow() throws InterruptedException {

        logger.info("🔽 Preparing to scroll to SEEBURGER row...");

        List<WebElement> systemCells =
                ShadowDom.findAllDeep(driver, PLATFORM_SYSTEM_NAME_DEEP, logger);

        boolean found = false;

        for (WebElement cell : systemCells) {

            String systemName = cell.getText().trim().toUpperCase();

            if (systemName.equals("SEEBURGER")) {

                logger.info("🎯 SEEBURGER row found. Scrolling now...");

                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].scrollIntoView({block:'center'});", cell);

                Thread.sleep(3000);

                logger.info("✅ Scroll completed and waited 3 seconds.");
                found = true;
                break;
            }
        }

        if (!found) {
            logger.warning("⚠ SEEBURGER row not found for scrolling.");
        }
    }
}
