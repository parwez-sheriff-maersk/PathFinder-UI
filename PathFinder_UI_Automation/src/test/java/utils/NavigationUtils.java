package utils;

import static Pages.PathFinderLocators.*;

import java.util.logging.Logger;

import org.openqa.selenium.WebDriver;

public class NavigationUtils {

    private static final Logger logger =
            Logger.getLogger(NavigationUtils.class.getName());

    public static void clickTraceTableTab(WebDriver driver) {

        logger.info("🔎 Clicking Trace Table tab...");

        WaitUtils.waitForElementClickable(driver, TRACE_TABLE_TAB, 30, logger).click();

        WaitUtils.waitForElementVisible(driver, MC_SELECT_DROPDOWN, 30, logger);

        logger.info("✅ Trace Table loaded");

        logger.info("⏳ Waiting 5 seconds for UI to fully stabilize...");
        try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
        logger.info("✅ UI stabilization wait completed");
    }
}
