package utils;

import static Pages.PathFinderLocators.*;

import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public class StatusValidation {

    private final WebDriver driver;
    private final Logger logger = Logger.getLogger(StatusValidation.class.getName());

    public StatusValidation(WebDriver driver) {
        this.driver = driver;
    }

    // ============================================================
    // VALIDATE ONLY VISIBLE STATUS CELLS (AFTER EXPANSION)
    // ============================================================

    public void validateVisibleStatus(String expectedStatus) {

        logger.info("==============================================");
        logger.info("🔎 Validating Visible Status Rows Only");
        logger.info("==============================================");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        wait.until(d ->
                ShadowDom.findAllDeep(
                        driver,
                        STATUS_CELL_DEEP,
                        logger
                ).size() > 0
        );

        List<WebElement> statusCells =
                ShadowDom.findAllDeep(
                        driver,
                        STATUS_CELL_DEEP,
                        logger
                );

        boolean hasError = false;
        boolean hasTerminated = false;
        boolean hasSuccess = false;
        boolean hasRunning = false;

        for (WebElement cell : statusCells) {

            try {

                if (!cell.isDisplayed()) continue;

                String text = cell.getText().trim().toUpperCase();

                if (text.isEmpty()) continue;

                logger.info("Status found: " + text);

                if (text.contains("ERROR") || text.contains("FAIL"))
                    hasError = true;

                if (text.contains("TERMINATED") || text.contains("CANCELLED"))
                    hasTerminated = true;

                if (text.contains("SUCCESS") || text.contains("COMPLETED") || text.contains("CREATED"))
                    hasSuccess = true;

                if (text.contains("RUNNING") || text.contains("IN_PROGRESS"))
                    hasRunning = true;

            } catch (Exception ignored) {}
        }

        String finalUiStatus;

        if (hasError) {
            finalUiStatus = "ERROR";
        } else if (hasTerminated) {
            finalUiStatus = "TERMINATED";
        } else if (hasSuccess) {
            finalUiStatus = "SUCCESS";
        } else if (hasRunning) {
            finalUiStatus = "RUNNING";
        } else {
            throw new AssertionError("❌ Unable to determine final status!");
        }

        logger.info("🎯 Final UI Status: " + finalUiStatus);
        logger.info("📌 Expected Status: " + expectedStatus);

        if (!finalUiStatus.equalsIgnoreCase(expectedStatus)) {
            throw new AssertionError(
                    "❌ STATUS MISMATCH → Expected: "
                            + expectedStatus
                            + " | Found: "
                            + finalUiStatus
            );
        }

        logger.info("✅ Status MATCHED successfully.");
    }
    public void validateVisibleStatusForAmps(String expectedStatus, String selector) {

        logger.info("==============================================");
        logger.info("🔎 Validating AMPS Expanded Status Only");
        logger.info("==============================================");

        List<WebElement> statusCells =
                ShadowDom.findAllDeep(driver, selector, logger);

        if (statusCells == null || statusCells.isEmpty()) {
            throw new AssertionError("❌ No AMPS status cells found!");
        }

        String finalStatus = null;

        for (WebElement cell : statusCells) {

            if (!cell.isDisplayed()) continue;

            String text = cell.getText().trim();
            logger.info("➡ Found Status: " + text);

            if (!text.isEmpty()) {
                finalStatus = text;
            }
        }

        if (finalStatus == null) {
            throw new AssertionError("❌ Unable to determine final status!");
        }

        logger.info("🎯 Final Visible Status: " + finalStatus);
        logger.info("🎯 Expected Status: " + expectedStatus);

        if (!finalStatus.equalsIgnoreCase(expectedStatus)) {
            throw new AssertionError(
                    "❌ Status mismatch! Expected: "
                            + expectedStatus + " but found: " + finalStatus);
        }

        logger.info("✅ Status validation PASSED");
    }
}
