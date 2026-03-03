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

}
