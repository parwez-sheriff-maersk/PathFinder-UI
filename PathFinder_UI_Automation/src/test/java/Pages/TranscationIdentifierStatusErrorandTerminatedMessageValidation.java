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

public class TranscationIdentifierStatusErrorandTerminatedMessageValidation {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final Logger logger =
            Logger.getLogger(TranscationIdentifierStatusErrorandTerminatedMessageValidation.class.getName());

    public TranscationIdentifierStatusErrorandTerminatedMessageValidation(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    // ============================================================
    // TRACE TAB
    // ============================================================

    public void clickTraceTableTab() {

        WebElement traceTab =
                wait.until(ExpectedConditions.elementToBeClickable(TRACE_TABLE_TAB));

        traceTab.click();

        logger.info("⏳ Waiting 15 sec for Trace Table UI hydration...");
        try { Thread.sleep(15000); } catch (InterruptedException ignored) {}
    }

    // ============================================================
    // SEARCH
    // ============================================================

    public void enterTransactionIdentifierAndSearch(String txnId) {

        logger.info("[SEARCH] Transaction ID = " + txnId);

        WebElement input = findTransactionInput(20);

        InputClearFeild.safeClearAndFocus(driver, input);

        input.sendKeys(txnId);
        input.sendKeys(Keys.TAB);

        clickSearchButton();
    }

    private void clickSearchButton() {

        WebElement searchBtn = ShadowDom.waitForInnerClickable(
                driver, SEARCH_BTN_HOST, SEARCH_BTN, 20, logger);

        ShadowDom.jsClick(driver, searchBtn);

        logger.info("🔎 Search clicked");
    }

    private WebElement findTransactionInput(int timeoutSeconds) {

        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < timeoutSeconds * 1000L) {

            List<WebElement> hosts = driver.findElements(PLATFORM_HOSTS);

            for (WebElement host : hosts) {
                try {
                    SearchContext root = host.getShadowRoot();
                    WebElement input = root.findElement(INNER_INPUT);
                    String placeholder = input.getAttribute("placeholder");

                    if (placeholder != null &&
                            placeholder.toLowerCase().contains("transaction"))
                        return input;

                } catch (Exception ignore) {}
            }

            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }

        throw new TimeoutException("Transaction Identifier input not found");
    }

    // ============================================================
    // BADGE
    // ============================================================

   // private WebElement locateBadge() {
       // return ShadowDom.findFirstVisibleDeep(
             //   driver, GENERIC_BADGE_DEEP, 10, logger);
    }

    /*public boolean isOrangeBadge() {

       // WebElement badge = locateBadge();
        String color = ShadowDom.getComputedStyle(driver, badge, "background-color");

        logger.info("🟠 ORANGE badge CSS = " + color);
        return ColorUtils.isOrangeCss(color);
    }

    public boolean isGreenBadge() {

        WebElement badge = locateBadge();
        String color = ShadowDom.getComputedStyle(driver, badge, "background-color");

        logger.info("🟢 GREEN badge CSS = " + color);
        return ColorUtils.isGreenCss(color);
    }

    public boolean isRedBadge() {

        WebElement badge = locateBadge();
        String color = ShadowDom.getComputedStyle(driver, badge, "background-color");

        logger.info("🔴 RED badge CSS = " + color);
        return ColorUtils.isRedCss(color);
    }

    // ============================================================
    // FLOWS
    // ============================================================

    public void processOrangeFlow() throws InterruptedException {

        wait.until(d -> isOrangeBadge());

        ExpandDownArrows expander = new ExpandDownArrows(driver);
      //  expander.expandToFinalLevel();
    }

    public void processGreenFlow() throws InterruptedException {

        wait.until(d -> isGreenBadge());

        ExpandDownArrows expander = new ExpandDownArrows(driver);
        //expander.expandToFinalLevel();
    }

    public void processRedFlow() throws InterruptedException {

        wait.until(d -> isRedBadge());

        ExpandDownArrows expander = new ExpandDownArrows(driver);
        //expander.expandToFinalLevel();
    }

    public void expandToErrorLevel() throws InterruptedException {

        ExpandDownArrows expander = new ExpandDownArrows(driver);
        expander.expandOnlyThirdLevel();
    }
}*/