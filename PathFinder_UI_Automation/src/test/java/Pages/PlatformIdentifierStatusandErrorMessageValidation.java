package Pages;

import static Pages.PathFinderLocators.*;

import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import utils.ColorUtils;
import utils.ShadowDom;

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
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//span[text()='Trace Table']")));

        traceTab.click();

        logger.info("⏳ Waiting 20 sec for Trace Table UI hydration...");
        try { Thread.sleep(20000); } catch (InterruptedException ignored) {}
    }

    // ============================================================
    // FIRST SEARCH
    // ============================================================

    public void enterPlatformIdentifierAndSearch(String platformId) {

        logger.info("[SEARCH] Platform ID = " + platformId);

        WebElement input = findPlatformInputByLabel(20);

        safeFocusAndClear(input);

        input.sendKeys(platformId);
        input.sendKeys(Keys.TAB);

        clickSearchButton();
    }

    // ============================================================
    // UPDATE SEARCH (FIXED VERSION)
    // ============================================================

    public void updatePlatformIdentifierAndSearch(String newPlatformId) {

        logger.info("[UPDATE SEARCH] Platform ID = " + newPlatformId);

        WebElement input = findPlatformInputByLabel(20);

        safeFocusAndClear(input);

        input.sendKeys(newPlatformId);
        input.sendKeys(Keys.TAB);

        clickSearchButton();
    }

    // ============================================================
    // SAFE FOCUS + CLEAR (FIX FOR CLICK INTERCEPTION)
    // ============================================================

    private void safeFocusAndClear(WebElement input) {

        // Scroll into center so header doesn't overlap
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", input);

        // Use JS focus (not click)
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].focus();", input);

        // Clear via JS (more stable)
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value=''; arguments[0].dispatchEvent(new Event('input',{bubbles:true}));",
                input);

        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
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
                driver, "span.trace-badge,.trace-badge,.badge", 6, logger);
    }

    public boolean isTransactionIdBadgeOrange() {
        WebElement badge = locateTransactionIdBadge();
        if (badge == null) return false;

        String color = ShadowDom.getComputedStyle(driver, badge, "background-color");
        logger.info("ORANGE badge CSS color = " + color);

        return ColorUtils.isOrangeCss(color);
    }

    public boolean isTransactionIdRed() {
        WebElement badge = locateTransactionIdBadge();
        if (badge == null) return false;

        String color = ShadowDom.getComputedStyle(driver, badge, "background-color");
        logger.info("RED badge CSS color = " + color);

        int[] rgb = ColorUtils.parseRgb(color);
        if (rgb == null) return false;

        int r = rgb[0];
        int g = rgb[1];
        int b = rgb[2];

        return (r > 200 && g < 220 && b < 220);
    }

    // ============================================================
    // EXPAND
    // ============================================================

    private void expandRow() {

        WebElement btn = ShadowDom.findFirstVisibleDeep(
                driver,
                "button[aria-label='Expand row']",
                10,
                logger);

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", btn);

        btn.click();

        try { Thread.sleep(800); } catch (InterruptedException ignored) {}
    }

    private String captureStatus() {

        WebElement statusCell = ShadowDom.findFirstVisibleDeep(
                driver,
                "td[data-header-id='status']",
                10,
                logger);

        if (statusCell == null) return "UNKNOWN";

        return statusCell.getText().trim().toUpperCase();
    }

    public String processOrangeFlow() {

        if (!isTransactionIdBadgeOrange())
            throw new AssertionError("Txn not orange");

        expandRow();
        expandRow();

        return captureStatus();
    }

    public String processRedFlow() throws InterruptedException {

        logger.info("===== RED Flow Validation Started =====");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(40));

        // 1️⃣ Wait for RED badge
        wait.until(d -> isTransactionIdRed());
        logger.info("✅ RED badge confirmed");

        // 2️⃣ Click first expand arrow
        WebElement firstExpand = ShadowDom.findFirstVisibleDeep(
                driver,
                "button[aria-label='Expand row']",
                10,
                logger
        );
        firstExpand.click();
        Thread.sleep(1000);

        // 3️⃣ Click second expand arrow
        WebElement secondExpand = ShadowDom.findFirstVisibleDeep(
                driver,
                "button[aria-label='Expand row']",
                10,
                logger
        );
        secondExpand.click();
        logger.info("✅ Two levels expanded");

        // 4️⃣ Now wait for STATUS inside expanded row
        WebElement statusCell = wait.until(d ->
                ShadowDom.findFirstVisibleDeep(
                        driver,
                        "td[data-header-id='status']",
                        5,
                        logger
                )
        );

        String status = statusCell.getText().trim();

        logger.info("🔎 Status after expanding 2 arrows: " + status);

        return status;
    }
    public void clickThirdExpandArrow() {

        logger.info("===== Clicking 3rd Down Arrow =====");

        List<WebElement> expandButtons =
                ShadowDom.findAllDeep(
                        driver,
                        "button[aria-label='Expand row']",
                        logger
                );

        if (expandButtons == null || expandButtons.size() < 3) {
            throw new RuntimeException("Less than 3 expand arrows found!");
        }

        WebElement thirdArrow = expandButtons.get(2);

        ShadowDom.scrollIntoViewCenter(driver, thirdArrow);
        ShadowDom.jsClick(driver, thirdArrow);

        logger.info("✅ 3rd down arrow clicked successfully");
    }



    public void validateErrorStatusAfterThirdExpand() {

        logger.info("===== Validating ERROR Status After 3rd Expand =====");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        // Wait until status cells are present
        wait.until(d ->
                ShadowDom.findAllDeep(
                        driver,
                        "td[data-header-id='status']",
                        logger
                ).size() > 0
        );

        List<WebElement> statusCells = ShadowDom.findAllDeep(
                driver,
                "td[data-header-id='status']",
                logger
        );

        boolean errorFound = false;

        for (WebElement cell : statusCells) {

            String text = cell.getText().trim();
            logger.info("Status found: " + text);

            if (text.equalsIgnoreCase("ERROR")) {
                errorFound = true;
                break;
            }
        }

        if (!errorFound) {
            throw new AssertionError("ERROR status not found after 3rd expand");
        }

        logger.info("✅ ERROR status validation PASSED.");
    }
 // ============================================================
 // 🟢 GREEN FLOW (MATCHING RED LOGIC)
 // ============================================================

 public String processGreenFlow(String greenPlatformId) throws InterruptedException {

     logger.info("=================================================");
     logger.info("🟢 ===== GREEN Badge Flow Validation Started =====");
     logger.info("=================================================");

     WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(40));

     // 1️⃣ Clear + Enter + Search
     logger.info("🔄 Updating Platform ID for GREEN flow: " + greenPlatformId);
     updatePlatformIdentifierAndSearch(greenPlatformId);

     // 2️⃣ Wait for GREEN badge (same style as RED wait)
     logger.info("⏳ Waiting for GREEN badge...");
     wait.until(d -> isTransactionIdBadgeGreen());
     logger.info("✅ GREEN badge confirmed");

     // 3️⃣ Expand first arrow
     logger.info("⬇ Expanding first level...");
     expandArrowByIndex(0);
     Thread.sleep(1000);

     // 4️⃣ Expand second arrow
     logger.info("⬇ Expanding second level...");
     expandArrowByIndex(1);
     logger.info("✅ Two levels expanded");

     // 5️⃣ Capture status EXACTLY like RED flow
     WebElement statusCell = wait.until(d ->
             ShadowDom.findFirstVisibleDeep(
                     driver,
                     "td[data-header-id='status']",
                     5,
                     logger
             )
     );

     String status = statusCell.getText().trim().toUpperCase();

     logger.info("🔎 Status after expanding 2 arrows (GREEN): " + status);

     logger.info("=================================================");
     logger.info("🟢 ===== GREEN Flow Validation Completed =====");
     logger.info("=================================================");

     return status;
 }
//============================================================
//COMMON EXPAND METHOD (VISIBLE SAFE VERSION)
//============================================================

private void expandArrowByIndex(int index) {

  List<WebElement> allArrows =
          ShadowDom.findAllDeep(
                  driver,
                  "button[aria-label='Expand row']",
                  logger);

  if (allArrows == null || allArrows.isEmpty()) {
      throw new RuntimeException("❌ No expand arrows found on page!");
  }

  // Filter only visible arrows
  List<WebElement> visibleArrows = new java.util.ArrayList<>();

  for (WebElement arrow : allArrows) {
      try {
          if (arrow.isDisplayed()) {
              visibleArrows.add(arrow);
          }
      } catch (Exception ignored) {}
  }

  logger.info("🔎 Total expand arrows found: " + allArrows.size());
  logger.info("👀 Visible expand arrows: " + visibleArrows.size());

  if (visibleArrows.size() <= index) {
      throw new RuntimeException(
              "❌ Not enough visible expand arrows! Required index: " + index
      );
  }

  WebElement arrowToClick = visibleArrows.get(index);

  ShadowDom.scrollIntoViewCenter(driver, arrowToClick);
  ShadowDom.jsClick(driver, arrowToClick);

  logger.info("🔽 Visible expand arrow index " + index + " clicked successfully.");
}
//============================================================
//🟢 GREEN BADGE DETECTION (IMPROVED)
//============================================================

public boolean isTransactionIdBadgeGreen() {

 WebElement badge =
         ShadowDom.findFirstVisibleDeep(
                 driver,
                 "span.trace-badge,.trace-badge,.badge",
                 10,
                 logger);

 if (badge == null) {
     logger.warning("❌ No badge element found for GREEN detection.");
     return false;
 }

 String color =
         ShadowDom.getComputedStyle(driver, badge, "background-color");

 logger.info("🟢 GREEN badge CSS color detected: " + color);

 int[] rgb = ColorUtils.parseRgb(color);

 if (rgb == null) {
     logger.warning("❌ Unable to parse GREEN badge RGB value.");
     return false;
 }

 int r = rgb[0];
 int g = rgb[1];
 int b = rgb[2];

 logger.info("🟢 Parsed RGB -> R: " + r + " G: " + g + " B: " + b);

 

 boolean isGreen =
         (g > 200) &&
         (r >= 150 && r <= 220) &&
         (b >= 150 && b <= 220);

 if (isGreen) {
     logger.info("✅ GREEN badge validation PASSED.");
 } else {
     logger.warning("❌ GREEN badge validation FAILED.");
 }

 return isGreen;
}}