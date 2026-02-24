package utils;

import static Pages.PathFinderLocators.*;

import java.util.List;
import java.util.logging.Logger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ExpandDownArrows {

    private final WebDriver driver;
    private final Logger logger = Logger.getLogger(ExpandDownArrows.class.getName());

    public ExpandDownArrows(WebDriver driver) {
        this.driver = driver;
    }

    // ============================================================
    // EXPAND FIRST VISIBLE ARROW
    // ============================================================

    private void expandFirstVisibleArrow() {

        List<WebElement> arrows =
                ShadowDom.findAllDeep(
                        driver,
                        ANY_EXPAND_BUTTON_DEEP,
                        logger);

        if (arrows == null || arrows.isEmpty()) {
            throw new RuntimeException("❌ No expand arrows found on page!");
        }

        for (WebElement arrow : arrows) {
            try {
                if (arrow.isDisplayed()) {
                    ShadowDom.scrollIntoViewCenter(driver, arrow);
                    ShadowDom.jsClick(driver, arrow);
                    logger.info("🔽 Expanded a visible arrow.");
                    return;
                }
            } catch (Exception ignored) {}
        }

        throw new RuntimeException("❌ No visible expand arrow found!");
    }

    // ============================================================
    // EXPAND TO FINAL LEVEL (LEVEL 1 + LEVEL 2)
    // ============================================================

    public void expandToFinalLevel() throws InterruptedException {

        logger.info("⬇ Expanding Level 1...");
        expandFirstVisibleArrow();
        Thread.sleep(1500);

        logger.info("⬇ Expanding Level 2...");
        expandFirstVisibleArrow();
        Thread.sleep(2000);

        logger.info("✅ Expansion completed.");
    }

    // ============================================================
    // EXPAND ONLY THIRD LEVEL (FOR ERROR)
    // ============================================================

    public void expandOnlyThirdLevel() throws InterruptedException {

        List<WebElement> arrows =
                ShadowDom.findAllDeep(
                        driver,
                        ANY_EXPAND_BUTTON_DEEP,
                        logger);

        if (arrows == null || arrows.size() < 3) {
            throw new RuntimeException("❌ Not enough expand arrows found!");
        }

        logger.info("⬇ Clicking ONLY Level 3 Arrow (index 2)");

        WebElement thirdArrow = arrows.get(2);

        ShadowDom.scrollIntoViewCenter(driver, thirdArrow);
        ShadowDom.jsClick(driver, thirdArrow);

        Thread.sleep(2000);

        logger.info("✅ Level 3 expansion completed");
    }
}