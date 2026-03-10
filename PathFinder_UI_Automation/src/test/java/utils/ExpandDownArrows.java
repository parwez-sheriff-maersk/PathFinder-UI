package utils;

import static Pages.PathFinderLocators.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import Pages.PathFinderLocators;

public class ExpandDownArrows {

    private final WebDriver driver;
    private final Logger logger = Logger.getLogger(ExpandDownArrows.class.getName());

    public ExpandDownArrows(WebDriver driver) {
        this.driver = driver;
    }

    // ============================================================
    // LEVEL 1 – Expand First Transaction Row
    // ============================================================

    public void expandFirstTransactionRow() {

        logger.info("⬇ Expanding Level 1 (Transaction)...");

        WebElement arrow =
                ShadowDom.waitForInnerClickable(
                        driver,
                        PathFinderLocators.PLATFORM_ROW_EXPANDER_HOST,
                        PathFinderLocators.PLATFORM_ROW_EXPANDER_INNER,
                        10,
                        logger
                );

        if (arrow == null) {
            throw new RuntimeException("❌ Level 1 expand arrow not found");
        }

        ShadowDom.scrollIntoViewCenter(driver, arrow);
        ShadowDom.jsClick(driver, arrow);

        logger.info("✅ Level 1 expanded");

        try { Thread.sleep(2000); } catch (Exception ignored) {}
    }

    // ============================================================
    // LEVEL 2 – Expand Platform By Name (AMPS / SEEBURGER)
    // ============================================================

    public void expandPlatformRowInsideExpandedSection(String platformName) {

        logger.info("⬇ Expanding Level 2 (" + platformName + ")...");

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

            if (text.equalsIgnoreCase(platformName)) {

                logger.info("✅ Matched platform: " + platformName);

                // Get row using JS (safe for Shadow DOM)
                WebElement row = (WebElement) ((JavascriptExecutor) driver)
                        .executeScript("return arguments[0].closest('tr');", platformElement);

                WebElement arrow =
                        row.findElement(By.cssSelector("button[aria-label='Expand row']"));

                ShadowDom.scrollIntoViewCenter(driver, arrow);
                ShadowDom.jsClick(driver, arrow);

                logger.info("✅ Level 2 expanded for: " + platformName);

                try { Thread.sleep(2000); } catch (Exception ignored) {}

                return;
            }
        }

        throw new RuntimeException("❌ Platform row not found: " + platformName);
    }

    // ============================================================
    // EXPAND TO FINAL LEVEL (Level 1 + Level 2)
    // ============================================================

    public void expandToFinalLevel(String platformName) {

        expandFirstTransactionRow();
        expandPlatformRowInsideExpandedSection(platformName);

        logger.info("✅ Expansion completed.");
    }
 // ============================================================
 // EXPAND LEVEL 1 + NEXT LEVEL SAFELY (WITHOUT RECLICKING SAME)
 // ============================================================

 // ============================================================
 // EXPAND LEVEL 1 + LEVEL 2 SAFELY (NEW METHOD)
 // ============================================================

 public void expandTwoLevelsSafely(String platformName) throws InterruptedException {

     logger.info("⬇ Expanding Level 1...");
     expandFirstTransactionRow();
     Thread.sleep(2000);

     logger.info("⬇ Expanding Level 2 (" + platformName + ")...");
     expandPlatformRowInsideExpandedSection(platformName);
     Thread.sleep(2000);

     logger.info("✅ Two-level expansion completed safely.");
 }
 
//============================================================
//SAFE EXPANSION FOR SEEBURGER (DOES NOT BREAK AMPS)
//============================================================

public void expandToFinalLevelSafe() throws InterruptedException {

  logger.info("⬇ [SAFE] Expanding Level 1...");

  List<WebElement> arrows =
          ShadowDom.findAllDeep(
                  driver,
                  ANY_EXPAND_BUTTON_DEEP,
                  logger);

  if (arrows == null || arrows.isEmpty()) {
      throw new RuntimeException("❌ No expand arrows found!");
  }

  // Click first arrow
  WebElement firstArrow = arrows.get(0);
  ShadowDom.scrollIntoViewCenter(driver, firstArrow);
  ShadowDom.jsClick(driver, firstArrow);

  Thread.sleep(1500);

  logger.info("⬇ [SAFE] Expanding Level 2...");

  // Re-fetch arrows AFTER first expansion
  arrows =
          ShadowDom.findAllDeep(
                  driver,
                  ANY_EXPAND_BUTTON_DEEP,
                  logger);

  if (arrows.size() < 2) {
      throw new RuntimeException("❌ Second level arrow not found!");
  }

  WebElement secondArrow = arrows.get(1);
  ShadowDom.scrollIntoViewCenter(driver, secondArrow);
  ShadowDom.jsClick(driver, secondArrow);

  Thread.sleep(2000);

  logger.info("✅ SAFE Expansion completed.");
}
//============================================================
//EXPAND LEVEL 1 + EXPLICIT SECOND ARROW (FOR SEEBURGER)
//============================================================

//============================================================
//EXPAND LEVEL 1 + LEVEL 2 USING EXISTING METHODS
//============================================================

public void expandToFinalLevelForSeeburger(String platformName) throws InterruptedException {

 logger.info("⬇ Expanding Level 1 (SEEBURGER)...");
 expandFirstTransactionRow();
 Thread.sleep(2000);

 logger.info("⬇ Expanding Level 2 (" + platformName + ")...");
 expandPlatformRowInsideExpandedSection(platformName);
 Thread.sleep(2000);

 logger.info("✅ SEEBURGER expansion completed.");
}
//============================================================
//SEEBURGER EXPANSION USING INDEX (STABLE)
//============================================================

public void expandSeeburgerByIndex() throws InterruptedException {

    List<WebElement> arrows =
            ShadowDom.findAllDeep(
                    driver,
                    ANY_EXPAND_BUTTON_DEEP,
                    logger);

    if (arrows == null || arrows.isEmpty()) {
        throw new RuntimeException("❌ No expand arrows found!");
    }

    logger.info("⬇ Expanding Level 1 (index 0)");

    WebElement firstArrow = arrows.get(0);
    ShadowDom.scrollIntoViewCenter(driver, firstArrow);
    ShadowDom.jsClick(driver, firstArrow);

    Thread.sleep(2000);

    // Re-fetch arrows after Level 1 expansion
    arrows =
            ShadowDom.findAllDeep(
                    driver,
                    ANY_EXPAND_BUTTON_DEEP,
                    logger);

    if (arrows.size() == 2) {
        // 🔹 Single SEEBURGER case
        logger.info("⬇ Single SEEBURGER detected - Expanding index 1");

        WebElement secondArrow = arrows.get(1);
        ShadowDom.scrollIntoViewCenter(driver, secondArrow);
        ShadowDom.jsClick(driver, secondArrow);
    } 
    else if (arrows.size() >= 3) {
        // 🔹 AMPS + SEEBURGER case
        logger.info("⬇ Multiple platforms detected - Expanding SEEBURGER at index 2");

        WebElement thirdArrow = arrows.get(2);
        ShadowDom.scrollIntoViewCenter(driver, thirdArrow);
        ShadowDom.jsClick(driver, thirdArrow);
    }
    else {
        throw new RuntimeException("❌ Unexpected expansion structure!");
    }

    Thread.sleep(2000);

    logger.info("✅ SEEBURGER expansion completed.");
}

//============================================================
//GET AVAILABLE PLATFORMS AFTER LEVEL 1 EXPANSION
//============================================================

public List<String> getAvailablePlatforms() {

 List<String> platformNames = new ArrayList<>();

 List<WebElement> platformCells =
         ShadowDom.findAllDeep(
                 driver,
                 "td[data-header-id='systemName'] span.system-name",
                 logger
         );

 if (platformCells == null || platformCells.isEmpty()) {
     logger.warning("⚠ No platform rows found!");
     return platformNames;
 }

 for (WebElement element : platformCells) {

     String name = element.getText().trim();

     if (!name.isEmpty() && !platformNames.contains(name)) {
         platformNames.add(name);
     }
 }

 logger.info("🧠 Detected Platforms: " + platformNames);

 return platformNames;
}
//============================================================
//EXPAND FIRST TRANSACTION + SEEBURGER ONLY (NO LOOP)
//============================================================

//============================================================
//EXPAND FIRST TRANSACTION + SEEBURGER (SCOPED & STABLE)
//============================================================

public void expandFirstSeeburgerScoped() throws InterruptedException {

 logger.info("⬇ Expanding FIRST transaction (Level 1)...");

 List<WebElement> level1Arrows =
         ShadowDom.findAllDeep(
                 driver,
                 "mc-button.mc-table__expanded-row__trigger button[aria-label='Expand row']",
                 logger);

 if (level1Arrows == null || level1Arrows.isEmpty()) {
     throw new RuntimeException("❌ No Level 1 arrows found!");
 }

 WebElement level1Arrow = level1Arrows.get(0);

 ShadowDom.scrollIntoViewCenter(driver, level1Arrow);
 ShadowDom.jsClick(driver, level1Arrow);

 Thread.sleep(2000);

 logger.info("⬇ Looking for SEEBURGER inside expanded section...");

 // 🔥 Scope to this transaction only
 WebElement transactionRow = (WebElement) ((JavascriptExecutor) driver)
         .executeScript("return arguments[0].closest('tr');", level1Arrow);

 WebElement expandedRow = (WebElement) ((JavascriptExecutor) driver)
         .executeScript("return arguments[0].nextElementSibling;", transactionRow);

 if (expandedRow == null) {
     throw new RuntimeException("❌ Expanded section not found!");
 }

 // Find SEEBURGER inside THIS section only
 List<WebElement> platforms =
         expandedRow.findElements(
                 By.cssSelector("td[data-header-id='systemName'] span.system-name"));

 for (WebElement platform : platforms) {

     if (platform.getText().trim().equalsIgnoreCase("SEEBURGER")) {

         logger.info("✅ SEEBURGER found inside first transaction");

         WebElement platformRow = (WebElement) ((JavascriptExecutor) driver)
                 .executeScript("return arguments[0].closest('tr');", platform);

         WebElement level2Arrow =
                 platformRow.findElement(By.cssSelector("button[aria-label='Expand row']"));

         ShadowDom.scrollIntoViewCenter(driver, level2Arrow);
         ShadowDom.jsClick(driver, level2Arrow);

         Thread.sleep(2000);

         logger.info("✅ SEEBURGER expanded successfully");
         return;
     }
 }

 throw new RuntimeException("❌ SEEBURGER not found inside first transaction!");
}
//============================================================
//EXPAND FIRST TRANSACTION (GENERIC VERSION FOR SEARCH RESULT)
//============================================================

public void expandFirstTransactionSimple() throws InterruptedException {

 logger.info("⬇ Expanding FIRST transaction (simple mode)...");

 List<WebElement> arrows =
         ShadowDom.findAllDeep(
                 driver,
                 "button[aria-label='Expand row']",
                 logger);

 if (arrows == null || arrows.isEmpty()) {
     throw new RuntimeException("❌ No expand arrows found!");
 }

 // Click first visible arrow only
 WebElement firstArrow = null;

 for (WebElement arrow : arrows) {
     if (arrow.isDisplayed()) {
         firstArrow = arrow;
         break;
     }
 }

 if (firstArrow == null) {
     throw new RuntimeException("❌ No visible expand arrow found!");
 }

 ShadowDom.scrollIntoViewCenter(driver, firstArrow);
 ShadowDom.jsClick(driver, firstArrow);

 Thread.sleep(2000);

 logger.info("✅ Level 1 expanded (simple)");
}
//============================================================
//LEVEL 2 – Expand SEEBURGER (INDEX MATCHING VERSION)
//============================================================

//============================================================
//EXPAND FIRST TRANSACTION + SEEBURGER (HOUSE BILL SAFE)
//DOES NOT TOUCH ANY EXISTING METHOD
//============================================================

public void expandFirstSeeburgerForHouseBill() throws InterruptedException {

 logger.info("⬇ Expanding FIRST transaction (Level 1)...");

 // ---- LEVEL 1 ----
 List<WebElement> level1Arrows =
         ShadowDom.findAllDeep(
                 driver,
                 "button[aria-label='Expand row']",
                 logger);

 if (level1Arrows == null || level1Arrows.isEmpty()) {
     throw new RuntimeException("❌ No Level 1 arrows found!");
 }

 WebElement firstArrow = level1Arrows.get(0);

 ShadowDom.scrollIntoViewCenter(driver, firstArrow);
 ShadowDom.jsClick(driver, firstArrow);

 Thread.sleep(2000);

 logger.info("✅ Level 1 expanded");


 // ---- LEVEL 2 (SEEBURGER) ----
 logger.info("⬇ Expanding SEEBURGER (Level 2)...");

 List<WebElement> platformCells =
         ShadowDom.findAllDeep(
                 driver,
                 "td[data-header-id='systemName'] span.system-name",
                 logger);

 for (WebElement cell : platformCells) {

     if (cell.getText().trim().equalsIgnoreCase("SEEBURGER")) {

         logger.info("✅ Found SEEBURGER row");

         // Move to its row
         WebElement row = (WebElement) ((JavascriptExecutor) driver)
                 .executeScript("return arguments[0].closest('tr');", cell);

         // Find expand button in that row (LEFT column)
         List<WebElement> rowButtons =
                 row.findElements(By.cssSelector("button"));

         for (WebElement btn : rowButtons) {

             String aria = btn.getAttribute("aria-label");

             if (aria != null && aria.equalsIgnoreCase("Expand row")) {

                 ShadowDom.scrollIntoViewCenter(driver, btn);
                 ShadowDom.jsClick(driver, btn);

                 Thread.sleep(2000);

                 logger.info("✅ SEEBURGER Level 2 expanded");
                 return;
             }
         }
     }
 }

 throw new RuntimeException("❌ SEEBURGER Level 2 arrow not found!");
}
///============================================================
// EXPAND FIRST RECORD + SEEBURGER (FINAL STABLE VERSION)
//============================================================

public void expandFirstSeeburgerFinal() throws InterruptedException {

    logger.info("⬇ Expanding FIRST transaction (HouseBill)...");

    // Get all expand arrows
    List<WebElement> arrows =
            ShadowDom.findAllDeep(
                    driver,
                    "button[aria-label='Expand row']",
                    logger);

    if (arrows == null || arrows.size() < 2) {
        throw new RuntimeException("❌ Not enough expand arrows found!");
    }

    // LEVEL 1
    WebElement level1Arrow = arrows.get(0);
    ShadowDom.scrollIntoViewCenter(driver, level1Arrow);
    ShadowDom.jsClick(driver, level1Arrow);

    Thread.sleep(2000);
    logger.info("✅ Level 1 expanded");


    // Refresh arrows after expansion
    arrows =
            ShadowDom.findAllDeep(
                    driver,
                    "button[aria-label='Expand row']",
                    logger);

    if (arrows.size() < 2) {
        throw new RuntimeException("❌ Level 2 arrow not found!");
    }

    // LEVEL 2 (SEEBURGER is always next arrow in first transaction)
    WebElement level2Arrow = arrows.get(1);

    ShadowDom.scrollIntoViewCenter(driver, level2Arrow);
    ShadowDom.jsClick(driver, level2Arrow);

    Thread.sleep(2000);

    logger.info("✅ SEEBURGER expanded successfully");
}
public void expandFirstRowThenSeeburger() throws InterruptedException {

    logger.info("==================================================");
    logger.info("⬇ Expanding First Transaction → SEEBURGER Only");
    logger.info("==================================================");

    // ------------------------------------------------------------
    // STEP 1: Expand FIRST TRANSACTION (Level 1)
    // ------------------------------------------------------------

    List<WebElement> level1Arrows =
            ShadowDom.findAllDeep(
                    driver,
                    ANY_EXPAND_BUTTON_DEEP,
                    logger);

    if (level1Arrows == null || level1Arrows.isEmpty()) {
        throw new RuntimeException("❌ No Level 1 arrows found!");
    }

    WebElement level1Arrow = level1Arrows.get(0);

    logger.info("⬇ Expanding Level 1 (First Transaction)");

    ShadowDom.scrollIntoViewCenter(driver, level1Arrow);
    ShadowDom.jsClick(driver, level1Arrow);

    Thread.sleep(2000);

    // ------------------------------------------------------------
    // STEP 2: Find SEEBURGER row (normal DOM)
    // ------------------------------------------------------------

    List<WebElement> systemCells =
            ShadowDom.findAllDeep(
                    driver,
                    "td[data-header-id='systemName'] span.system-name",
                    logger);

    for (WebElement system : systemCells) {

        if (system.getText().trim().equalsIgnoreCase("SEEBURGER")) {

            logger.info("✅ SEEBURGER row found");

            WebElement platformRow = (WebElement) ((JavascriptExecutor) driver)
                    .executeScript("return arguments[0].closest('tr');", system);

            // ------------------------------------------------------------
            // STEP 3: Find Level-2 arrow INSIDE that row using Deep Search
            // ------------------------------------------------------------

            List<WebElement> level2Arrows =
                    ShadowDom.findAllDeep(
                            driver,
                            platformRow,
                            "button[aria-label='Expand row']",
                            logger);

            if (level2Arrows == null || level2Arrows.isEmpty()) {
                throw new RuntimeException("❌ Level 2 arrow not found inside SEEBURGER row!");
            }

            WebElement level2Arrow = level2Arrows.get(0);

            logger.info("⬇ Expanding Level 2 (SEEBURGER)");

            ShadowDom.scrollIntoViewCenter(driver, level2Arrow);
            ShadowDom.jsClick(driver, level2Arrow);

            Thread.sleep(2000);

            logger.info("✅ SEEBURGER expansion completed.");
            logger.info("==================================================");
            return;
        }
    }

    throw new RuntimeException("❌ SEEBURGER row not found!");
}
}