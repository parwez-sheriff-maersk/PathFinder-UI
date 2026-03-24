package utils;

import static Pages.PathFinderLocators.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
                        PLATFORM_ROW_EXPANDER_HOST,
                        PLATFORM_ROW_EXPANDER_INNER,
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
                        PLATFORM_SYSTEM_NAME_DEEP,
                        logger
                );

        if (platformCells == null || platformCells.isEmpty()) {
            throw new RuntimeException("❌ No platform rows found!");
        }

        for (WebElement platformElement : platformCells) {

            String text = platformElement.getText().trim();
            logger.info("   ➡ Found platform: " + text);

            if (text.equalsIgnoreCase(platformName)) {

                logger.info("   ✅ Matched platform: " + platformName);

                WebElement row = (WebElement) ((JavascriptExecutor) driver)
                        .executeScript("return arguments[0].closest('tr');", platformElement);

                WebElement arrow =
                        row.findElement(PLATFORM_ROW_EXPANDER_INNER);

                ShadowDom.scrollIntoViewCenter(driver, arrow);
                ShadowDom.jsClick(driver, arrow);

                logger.info("   ✅ Level 2 expanded for: " + platformName);

                try { Thread.sleep(2000); } catch (Exception ignored) {}

                return;
            }
        }

        throw new RuntimeException("❌ Platform row not found: " + platformName);
    }

    // ============================================================
    // GET AVAILABLE PLATFORMS AFTER LEVEL 1 EXPANSION
    // ============================================================

    public List<String> getAvailablePlatforms() {

        List<String> platformNames = new ArrayList<>();

        List<WebElement> platformCells =
                ShadowDom.findAllDeep(
                        driver,
                        PLATFORM_SYSTEM_NAME_DEEP,
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

    // ============================================================
    // EXPAND FIRST TRANSACTION + SEEBURGER (HOUSE BILL OF LADING)
    // ============================================================

    public void expandFirstRowThenSeeburger() throws InterruptedException {

        logger.info("==================================================");
        logger.info("⬇ Expanding First Transaction → SEEBURGER Only");
        logger.info("==================================================");

        // -------------------------------------------------
        // STEP 1: Expand FIRST TRANSACTION (Level 1)
        // -------------------------------------------------

        logger.info("⏳ Waiting 5 seconds before Level 1 expansion...");
        Thread.sleep(5000);

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

        // -------------------------------------------------
        // STEP 2: Find SEEBURGER row (normal DOM)
        // -------------------------------------------------

        List<WebElement> systemCells =
                ShadowDom.findAllDeep(
                        driver,
                        PLATFORM_SYSTEM_NAME_DEEP,
                        logger);

        for (WebElement system : systemCells) {

            if (system.getText().trim().equalsIgnoreCase("SEEBURGER")) {

                logger.info("✅ SEEBURGER row found");

                WebElement platformRow = (WebElement) ((JavascriptExecutor) driver)
                        .executeScript("return arguments[0].closest('tr');", system);

                // -------------------------------------------------
                // STEP 3: Find Level-2 arrow INSIDE that row
                // -------------------------------------------------

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

    // ============================================================
    // EXPAND TRANSACTION + SEEBURGER BY INDEX (PLATFORM IDENTIFIER)
    // ============================================================

    public void expandSeeburgerByIndex() throws InterruptedException {

        logger.info("==================================================");
        logger.info("⬇ Expanding Transaction → SEEBURGER");
        logger.info("==================================================");

        // -------------------------------------------------
        // WAIT BEFORE FIRST EXPANSION (IMPORTANT FOR SLOW DATA)
        // -------------------------------------------------
        logger.info("⏳ Waiting 5 seconds before Level 1 expansion...");
        Thread.sleep(5000);

        // -------------------------------------------------
        // STEP 1: Expand Level 1 (Transaction)
        // -------------------------------------------------

        List<WebElement> arrows =
                ShadowDom.findAllDeep(driver, ANY_EXPAND_BUTTON_DEEP, logger);

        if (arrows == null || arrows.isEmpty()) {
            throw new RuntimeException("❌ No expand arrows found!");
        }

        WebElement firstArrow = arrows.get(0);

        logger.info("⬇ Expanding Level 1 (Transaction)");

        ShadowDom.scrollIntoViewCenter(driver, firstArrow);
        ShadowDom.jsClick(driver, firstArrow);

        Thread.sleep(2000);

        // -------------------------------------------------
        // STEP 2: Get all platform rows after expansion
        // -------------------------------------------------

        List<WebElement> platforms =
                ShadowDom.findAllDeep(
                        driver,
                        PLATFORM_SYSTEM_NAME_DEEP,
                        logger);

        if (platforms == null || platforms.isEmpty()) {
            throw new RuntimeException("❌ No platform rows found after expansion!");
        }

        logger.info("🧠 Platforms detected: " + platforms.size());

        // -------------------------------------------------
        // SCENARIO 1: Only one platform (SEEBURGER)
        // -------------------------------------------------

        if (platforms.size() == 1) {

            logger.info("⬇ Single platform detected → Expanding index 1");

            arrows = ShadowDom.findAllDeep(driver, ANY_EXPAND_BUTTON_DEEP, logger);

            if (arrows.size() < 2) {
                throw new RuntimeException("❌ Second level arrow not found!");
            }

            WebElement secondArrow = arrows.get(1);

            ShadowDom.scrollIntoViewCenter(driver, secondArrow);
            ShadowDom.jsClick(driver, secondArrow);

            Thread.sleep(2000);

            logger.info("✅ SEEBURGER expansion completed (Single Platform)");
            return;
        }

        // -------------------------------------------------
        // SCENARIO 2: Multiple platforms (AMPS + SEEBURGER)
        // -------------------------------------------------

        for (WebElement platform : platforms) {

            if (platform.getText().trim().equalsIgnoreCase("SEEBURGER")) {

                logger.info("⬇ SEEBURGER detected → Expanding correct row");

                WebElement platformRow = (WebElement) ((JavascriptExecutor) driver)
                        .executeScript("return arguments[0].closest('tr');", platform);

                List<WebElement> level2Arrows =
                        ShadowDom.findAllDeep(
                                driver,
                                platformRow,
                                "button[aria-label='Expand row']",
                                logger);

                if (level2Arrows == null || level2Arrows.isEmpty()) {
                    throw new RuntimeException("❌ SEEBURGER expand arrow not found!");
                }

                WebElement level2Arrow = level2Arrows.get(0);

                ShadowDom.scrollIntoViewCenter(driver, level2Arrow);
                ShadowDom.jsClick(driver, level2Arrow);

                Thread.sleep(2000);

                logger.info("✅ SEEBURGER expansion completed (Multiple Platforms)");
                return;
            }
        }

        throw new RuntimeException("❌ SEEBURGER platform not found!");
    }

    // ============================================================
    // EXPAND TRANSACTION + AMPS (BOOKING / ADVANCED SEARCH)
    // ============================================================

    public void expandFirstRowThenAmps() throws InterruptedException {

        logger.info("==================================================");
        logger.info("⬇ Expanding Transaction → AMPS");
        logger.info("==================================================");

        // -------------------------------------------------
        // WAIT BEFORE FIRST EXPANSION (IMPORTANT FOR SLOW DATA)
        // -------------------------------------------------
        logger.info("⏳ Waiting 5 seconds before Level 1 expansion...");
        Thread.sleep(5000);

        // -------------------------------------------------
        // STEP 1: Expand Level 1 (Transaction)
        // -------------------------------------------------

        List<WebElement> arrows =
                ShadowDom.findAllDeep(driver, ANY_EXPAND_BUTTON_DEEP, logger);

        if (arrows == null || arrows.isEmpty()) {
            throw new RuntimeException("❌ No expand arrows found!");
        }

        WebElement firstArrow = arrows.get(0);

        logger.info("⬇ Expanding Level 1 (Transaction)");

        ShadowDom.scrollIntoViewCenter(driver, firstArrow);
        ShadowDom.jsClick(driver, firstArrow);

        Thread.sleep(2000);

        // -------------------------------------------------
        // STEP 2: Get all platform rows after expansion
        // -------------------------------------------------

        List<WebElement> platforms =
                ShadowDom.findAllDeep(
                        driver,
                        PLATFORM_SYSTEM_NAME_DEEP,
                        logger);

        if (platforms == null || platforms.isEmpty()) {
            throw new RuntimeException("❌ No platform rows found after expansion!");
        }

        logger.info("🧠 Platforms detected: " + platforms.size());

        // -------------------------------------------------
        // SCENARIO 1: Only one platform (AMPS)
        // -------------------------------------------------

        if (platforms.size() == 1) {

            logger.info("⬇ Single platform detected → Expanding index 1");

            arrows = ShadowDom.findAllDeep(driver, ANY_EXPAND_BUTTON_DEEP, logger);

            if (arrows.size() < 2) {
                throw new RuntimeException("❌ Second level arrow not found!");
            }

            WebElement secondArrow = arrows.get(1);

            ShadowDom.scrollIntoViewCenter(driver, secondArrow);
            ShadowDom.jsClick(driver, secondArrow);

            Thread.sleep(2000);

            logger.info("✅ AMPS expansion completed (Single Platform)");
            return;
        }

        // -------------------------------------------------
        // SCENARIO 2: Multiple platforms (AMPS + SEEBURGER)
        // -------------------------------------------------

        for (WebElement platform : platforms) {

            if (platform.getText().trim().equalsIgnoreCase("AMPS")) {

                logger.info("⬇ AMPS detected → Expanding correct row");

                WebElement platformRow = (WebElement) ((JavascriptExecutor) driver)
                        .executeScript("return arguments[0].closest('tr');", platform);

                List<WebElement> level2Arrows =
                        ShadowDom.findAllDeep(
                                driver,
                                platformRow,
                                "button[aria-label='Expand row']",
                                logger);

                if (level2Arrows == null || level2Arrows.isEmpty()) {
                    throw new RuntimeException("❌ AMPS expand arrow not found!");
                }

                WebElement level2Arrow = level2Arrows.get(0);

                ShadowDom.scrollIntoViewCenter(driver, level2Arrow);
                ShadowDom.jsClick(driver, level2Arrow);

                Thread.sleep(2000);

                logger.info("✅ AMPS expansion completed (Multiple Platforms)");
                return;
            }
        }

        throw new RuntimeException("❌ AMPS platform not found!");
    }
}
