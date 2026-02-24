package Pages;

import static Pages.PathFinderLocators.*;

import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import utils.ColorUtils;
import utils.ShadowDom;
import utils.ExpandDownArrows;
import utils.InputClearFeild;

public class PlatformIdentifierStatusandErrorMessageValidation {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final Logger logger =
            Logger.getLogger(PlatformIdentifierStatusandErrorMessageValidation.class.getName());

    public PlatformIdentifierStatusandErrorMessageValidation(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    // ============================================================
    // TRACE TAB
    // ============================================================

    public void clickTraceTableTab() {

        WebElement traceTab = wait.until(
                ExpectedConditions.elementToBeClickable(TRACE_TABLE_TAB));

        traceTab.click();

        logger.info("⏳ Waiting 20 sec for Trace Table UI hydration...");
        try { Thread.sleep(20000); } catch (InterruptedException ignored) {}
    }

    // ============================================================
    // SEARCH
    // ============================================================

    public void enterPlatformIdentifierAndSearch(String platformId) {

        logger.info("[SEARCH] Platform ID = " + platformId);

        WebElement input = findPlatformInputByLabel(20);

        InputClearFeild.safeClearAndFocus(driver, input);

        input.sendKeys(platformId);
        input.sendKeys(Keys.TAB);

        clickSearchButton();
    }

    public void updatePlatformIdentifierAndSearch(String newPlatformId) {

        logger.info("[UPDATE SEARCH] Platform ID = " + newPlatformId);

        WebElement input = findPlatformInputByLabel(20);

        InputClearFeild.safeClearAndFocus(driver, input);

        input.sendKeys(newPlatformId);
        input.sendKeys(Keys.TAB);

        clickSearchButton();
    }

    // ============================================================
    // CLICK SEARCH
    // ============================================================

    private void clickSearchButton() {

        logger.info("Waiting for Search button to enable...");

        WebElement searchBtn = ShadowDom.waitForInnerClickable(
                driver, SEARCH_BTN_HOST, SEARCH_BTN, 20, logger);

        wait.until(ExpectedConditions.elementToBeClickable(searchBtn));

        ShadowDom.jsClick(driver, searchBtn);

        logger.info("🔎 Search clicked");
    }

    // ============================================================
    // FIND INPUT
    // ============================================================

    private WebElement findPlatformInputByLabel(int timeoutSeconds) {

        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < timeoutSeconds * 1000L) {

            List<WebElement> hosts = driver.findElements(PLATFORM_HOSTS);

            for (WebElement host : hosts) {
                try {
                    SearchContext root = host.getShadowRoot();
                    if (root == null) continue;

                    WebElement input = root.findElement(INNER_INPUT);
                    String ph = input.getAttribute("placeholder");

                    if (ph != null && ph.toLowerCase().contains("platform"))
                        return input;

                } catch (Exception ignore) {}
            }

            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }

        throw new TimeoutException("Platform input not found");
    }

    // ============================================================
    // BADGE DETECTION
    // ============================================================

    private WebElement locateTransactionIdBadge() {
        return ShadowDom.findFirstVisibleDeep(
                driver, GENERIC_BADGE_DEEP, 6, logger);
    }

    public boolean isTransactionIdBadgeOrange() {

        WebElement badge = locateTransactionIdBadge();
        if (badge == null) return false;

        String color = ShadowDom.getComputedStyle(driver, badge, "background-color");
        logger.info("🟠 ORANGE badge CSS color = " + color);

        return ColorUtils.isOrangeCss(color);
    }

    public boolean isTransactionIdRed() {

        WebElement badge = locateTransactionIdBadge();
        if (badge == null) return false;

        String color = ShadowDom.getComputedStyle(driver, badge, "background-color");
        logger.info("🔴 RED badge CSS color = " + color);

        return ColorUtils.isRedCss(color);
    }

    public boolean isTransactionIdBadgeGreen() {

        WebElement badge = locateTransactionIdBadge();
        if (badge == null) return false;

        String color = ShadowDom.getComputedStyle(driver, badge, "background-color");
        logger.info("🟢 GREEN badge CSS color = " + color);

        return ColorUtils.isGreenCss(color);
    }

    // ============================================================
    // COMMON STATUS CAPTURE
    // ============================================================

    private String captureExpandedStatus() {

        List<WebElement> statusCells =
                ShadowDom.findAllDeep(
                        driver,
                        STATUS_CELL_DEEP,
                        logger);

        for (WebElement cell : statusCells) {
            try {
                String text = cell.getText().trim();
                if (!text.isEmpty()) {
                    logger.info("Status Found: " + text);
                    return text.toUpperCase();
                }
            } catch (Exception ignored) {}
        }

        throw new AssertionError("Status not found after expansion");
    }

    // ============================================================
    // FLOWS
    // ============================================================

    public String processOrangeFlow() throws InterruptedException {

        if (!isTransactionIdBadgeOrange())
            throw new AssertionError("Txn not orange");

        ExpandDownArrows expander = new ExpandDownArrows(driver);
        expander.expandToFinalLevel();

        return captureExpandedStatus();
    }

    public String processRedFlow() throws InterruptedException {

        wait.until(d -> isTransactionIdRed());

        ExpandDownArrows expander = new ExpandDownArrows(driver);
        expander.expandToFinalLevel();

        return captureExpandedStatus();
    }

    public String processGreenFlow() throws InterruptedException {

        wait.until(d -> locateTransactionIdBadge() != null);

        ExpandDownArrows expander = new ExpandDownArrows(driver);
        expander.expandToFinalLevel();

        return captureExpandedStatus();
    }

    // ============================================================
    // ERROR LEVEL EXPANSION (ONLY 3rd LEVEL)
    // ============================================================

    public void expandToErrorLevel() throws InterruptedException {

        ExpandDownArrows expander = new ExpandDownArrows(driver);
        expander.expandOnlyThirdLevel();
    }
}