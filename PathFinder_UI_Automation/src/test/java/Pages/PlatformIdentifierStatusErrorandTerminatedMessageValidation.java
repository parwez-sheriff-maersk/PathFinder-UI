package Pages;

import static Pages.PathFinderLocators.*;

import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import utils.ExpandDownArrows;
import utils.ShadowDom;
import utils.StatusValidation;
import utils.InputClearFeild;

public class PlatformIdentifierStatusErrorandTerminatedMessageValidation {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final Logger logger =
            Logger.getLogger(
                    PlatformIdentifierStatusErrorandTerminatedMessageValidation.class.getName());

    public PlatformIdentifierStatusErrorandTerminatedMessageValidation(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    // ============================================================
    // TRACE TAB
    // ============================================================

    public void clickTraceTableTab() {

        logger.info("🔎 Clicking Trace Table tab...");

        WebElement traceTab =
                wait.until(ExpectedConditions.elementToBeClickable(TRACE_TABLE_TAB));

        traceTab.click();

        try { Thread.sleep(20000); } catch (InterruptedException ignored) {}
    }

    // ============================================================
    // SEARCH
    // ============================================================

    public void updatePlatformIdentifierAndSearch(String newPlatformId)
            throws InterruptedException {

        collapseAllExpandedRows();

        WebElement input = findPlatformInputByLabel(20);

        InputClearFeild.safeClearAndFocus(driver, input);

        input.sendKeys(newPlatformId);
        input.sendKeys(Keys.TAB);

        clickSearchButton();

        Thread.sleep(3000);
    }

    private void clickSearchButton() {

        WebElement searchBtn = ShadowDom.waitForInnerClickable(
                driver, SEARCH_BTN_HOST, SEARCH_BTN, 20, logger);

        wait.until(ExpectedConditions.elementToBeClickable(searchBtn));

        ShadowDom.jsClick(driver, searchBtn);
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

                        return input;
                    }

                } catch (Exception ignored) {}
            }

            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }

        throw new TimeoutException("❌ Platform input not found.");
    }

    // ============================================================
    // ✅ AMPS EXPANSION + VALIDATION
    // ============================================================

    public void expandAndValidateAmps(String expectedStatus) throws InterruptedException {

        logger.info("🔵 ===== PROCESSING AMPS =====");

        // Level 1 - Expand Transaction
        expandFirstVisibleArrow();
        Thread.sleep(3000);

        // Level 2 - Expand AMPS row
        expandAmpsRow();
        Thread.sleep(3000);

        StatusValidation statusValidation = new StatusValidation(driver);
        statusValidation.validateVisibleStatus(expectedStatus);

        logger.info("✅ AMPS validation completed successfully.");
    }

    // ============================================================
    // SEEBURGER
    // ============================================================

    public void expandAndValidateSeeburger(String expectedStatus) throws InterruptedException {

        logger.info("🟣 ===== PROCESSING SEEBURGER =====");

        ExpandDownArrows expander = new ExpandDownArrows(driver);

        // ✅ Use existing working method
        expander.expandSeeburgerByIndex();

        StatusValidation statusValidation = new StatusValidation(driver);
        //statusValidation.validateVisibleStatus(expectedStatus);
        statusValidation.validateStatusForPlatform("SEEBURGER", expectedStatus);
        logger.info("✅ SEEBURGER validation completed successfully.");
    }
    
    public void expandSeeburgerRow() {

        logger.info("🔎 Expanding SEEBURGER row specifically...");

        List<WebElement> platformCells =
                ShadowDom.findAllDeep(
                        driver,
                        "td[data-header-id='systemName'] span.system-name",
                        logger
                );

        if (platformCells == null || platformCells.isEmpty()) {
            throw new RuntimeException("❌ No platform rows found!");
        }

        for (WebElement platformElement : platformCells) {

            String text = platformElement.getText().trim();
            logger.info("➡ Found platform: " + text);

            if (text.equalsIgnoreCase("SEEBURGER")) {

                logger.info("✅ SEEBURGER row located");

                // find row via JS (safe inside shadow)
                WebElement row = (WebElement) ((JavascriptExecutor) driver)
                        .executeScript("return arguments[0].closest('tr');", platformElement);

                // 🔥 IMPORTANT: click SVG chevron inside this row
                WebElement chevron =
                        row.findElement(By.cssSelector("svg[aria-label='chevron-down']"));

                ShadowDom.scrollIntoViewCenter(driver, chevron);
                ShadowDom.jsClick(driver, chevron);

                logger.info("✅ SEEBURGER arrow clicked successfully");

                try { Thread.sleep(2000); } catch (Exception ignored) {}

                return;
            }
        }

        throw new RuntimeException("❌ SEEBURGER platform row not found");
    }
    
    public void expandPlatformRow(String platformName) {

        logger.info("🔎 Expanding row for: " + platformName);

        // 1️⃣ Find the correct platform row using data-cy
        WebElement row = driver.findElement(
                By.cssSelector("tr[data-cy='" + platformName + "']")
        );

        logger.info("✅ Row found for " + platformName);

        // 2️⃣ Find mc-button inside that row
        WebElement mcButton = row.findElement(
                By.cssSelector("td.mds-table__column--row-expander mc-button")
        );

        logger.info("✅ mc-button found inside row");

        // 3️⃣ Enter shadow root
        SearchContext shadowRoot = mcButton.getShadowRoot();

        // 4️⃣ Find real clickable button
        WebElement innerButton = shadowRoot.findElement(
                By.cssSelector("button[aria-label='Expand row']")
        );

        // 5️⃣ Click
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});",
                innerButton
        );

        innerButton.click();

        logger.info("✅ " + platformName + " expanded successfully");

        try { Thread.sleep(2000); } catch (Exception ignored) {}
    }

    // ============================================================
    // EXPAND AMPS ROW SPECIFICALLY
    // ============================================================

    public void expandAmpsRow() {

        logger.info("🔎 Expanding AMPS row specifically...");

        // 1️⃣ Find all system-name cells
        List<WebElement> platformCells =
                ShadowDom.findAllDeep(
                        driver,
                        "td[data-header-id='systemName'] span.system-name",
                        logger
                );

        for (WebElement cell : platformCells) {

            String text = cell.getText().trim();

            logger.info("➡ Found platform: " + text);

            if (text.equalsIgnoreCase("AMPS")) {

                logger.info("✅ AMPS row located");

                // 2️⃣ Get the entire row
                WebElement row = (WebElement) ((JavascriptExecutor) driver)
                        .executeScript("return arguments[0].closest('tr')", cell);

                if (row == null) {
                    throw new RuntimeException("❌ Could not locate parent row for AMPS");
                }

                // 3️⃣ Inside that row, find expander column host
                WebElement host =
                        row.findElement(By.cssSelector("mc-button"));

                // 4️⃣ Enter shadow root and click inner button
                WebElement arrow =
                        host.getShadowRoot()
                                .findElement(By.cssSelector("button[aria-label='Expand row']"));

                ShadowDom.scrollIntoViewCenter(driver, arrow);
                ShadowDom.jsClick(driver, arrow);

                logger.info("✅ AMPS arrow clicked successfully");

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

        WebDriverWait localWait =
                new WebDriverWait(driver, Duration.ofSeconds(20));

        localWait.until(d ->
                ShadowDom.findAllDeep(
                        driver,
                        "button[aria-label='Expand row']",
                        logger).size() > 0
        );
    }

    // ============================================================
    // COLLAPSE
    // ============================================================

    private void collapseAllExpandedRows()
            throws InterruptedException {

        List<WebElement> arrows =
                ShadowDom.findAllDeep(
                        driver,
                        "button[aria-label='Expand row']",
                        logger);

        for (WebElement arrow : arrows) {

            try {
                String expanded = arrow.getAttribute("aria-expanded");

                if ("true".equalsIgnoreCase(expanded)) {

                    ShadowDom.scrollIntoViewCenter(driver, arrow);
                    ShadowDom.jsClick(driver, arrow);
                    Thread.sleep(500);
                }

            } catch (Exception ignored) {}
        }

        Thread.sleep(1000);
    }
    public void expandFirstVisibleArrow() {

        List<WebElement> arrows =
                ShadowDom.findAllDeep(
                        driver,
                        ANY_EXPAND_BUTTON_DEEP,
                        logger);

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
                ShadowDom.findAllDeep(
                        driver,
                        "button[aria-label='Expand row']",
                        logger
                );

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
 // SMART SEEBURGER HANDLER (WITHOUT TOUCHING EXISTING CODE)
 // ============================================================

 public void expandAndValidateSeeburgerSmart(String expectedStatus) throws InterruptedException {

     logger.info("🟣 ===== SMART SEEBURGER PROCESSING =====");

     ExpandDownArrows expander = new ExpandDownArrows(driver);

     // Expand Level 1 first
     expander.expandFirstTransactionRow();

     List<String> platforms = expander.getAvailablePlatforms();

     // Case 1: Only SEEBURGER exists
     if (platforms.size() == 1 && platforms.contains("SEEBURGER")) {

         logger.info("🔹 Only SEEBURGER present");

         expander.expandSeeburgerByIndex();

         new StatusValidation(driver).validateVisibleStatus(expectedStatus);
         return;
     }

     // Case 2: AMPS + SEEBURGER
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

}