package utils;

import static Pages.PathFinderLocators.*;

import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.aventstack.extentreports.ExtentTest;

public class StatusValidation {

    private final WebDriver driver;
    private final Logger logger = Logger.getLogger(StatusValidation.class.getName());

    public StatusValidation(WebDriver driver) {
        this.driver = driver;
    }

    // ============================================================
    // COMMON DB TABLE ATTACHMENT (NEW - USED EVERYWHERE)
    // ============================================================

    private void attachDbTable(String platform,
                               String identifier,
                               String traceId,
                               String dbStatus,
                               String expectedStatus) {

        ExtentTest test = ExtentTestManager.getTest();
        if (test == null) return;

        String tableHtml =
                "<div style='margin-top:10px'>" +
                "<b>Database Validation Details</b>" +
                "<table border='1' style='border-collapse:collapse; width:60%; margin-top:8px'>" +
                "<tr><td style='padding:6px'><b>Platform</b></td><td style='padding:6px'>" + platform + "</td></tr>" +
                "<tr><td style='padding:6px'><b>Identifier</b></td><td style='padding:6px'>" + identifier + "</td></tr>" +
                "<tr><td style='padding:6px'><b>Trace ID</b></td><td style='padding:6px'>" + traceId + "</td></tr>" +
                "<tr><td style='padding:6px'><b>DB Raw Status</b></td><td style='padding:6px'>" + dbStatus + "</td></tr>" +
                "<tr><td style='padding:6px'><b>Expected UI Status</b></td><td style='padding:6px'>" + expectedStatus + "</td></tr>" +
                "</table></div>";

        test.info(tableHtml);
    }

    // ============================================================
    // VALIDATE ONLY VISIBLE STATUS CELLS
    // ============================================================

    public void validateVisibleStatus(String expectedStatus) {

        ExtentTest test = ExtentTestManager.getTest();
        if (test != null) test.info("Validating Visible Status Rows");

        // DB table attached (generic values if DB not passed)
        attachDbTable("N/A", "N/A", "N/A", "N/A", expectedStatus);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        wait.until(d ->
                ShadowDom.findAllDeep(driver, STATUS_CELL_DEEP, logger).size() > 0
        );

        List<WebElement> statusCells =
                ShadowDom.findAllDeep(driver, STATUS_CELL_DEEP, logger);

        String finalUiStatus = deriveFinalStatus(statusCells);

        if (!finalUiStatus.equalsIgnoreCase(expectedStatus)) {

            if (test != null) {
                test.fail("STATUS MISMATCH → Expected: "
                        + expectedStatus + " | Found: " + finalUiStatus);
                attachScreenshot(test);
            }

            throw new AssertionError(
                    "❌ STATUS MISMATCH → Expected: "
                            + expectedStatus
                            + " | Found: "
                            + finalUiStatus
            );
        }

        if (test != null) test.pass("Status Matched: " + finalUiStatus);
    }

    // ============================================================
    // VALIDATE AMPS SPECIFIC STATUS
    // ============================================================

    public void validateVisibleStatusForAmps(String expectedStatus, String selector) {

        ExtentTest test = ExtentTestManager.getTest();
        if (test != null) test.info("Validating AMPS Expanded Status");

        attachDbTable("AMPS", "N/A", "N/A", "N/A", expectedStatus);

        List<WebElement> statusCells =
                ShadowDom.findAllDeep(driver, selector, logger);

        if (statusCells == null || statusCells.isEmpty()) {

            if (test != null) {
                test.fail("No AMPS status cells found!");
                attachScreenshot(test);
            }

            throw new AssertionError("❌ No AMPS status cells found!");
        }

        String finalStatus = null;

        for (WebElement cell : statusCells) {
            if (!cell.isDisplayed()) continue;
            String text = cell.getText().trim();
            if (!text.isEmpty()) finalStatus = text;
        }

        if (!finalStatus.equalsIgnoreCase(expectedStatus)) {

            if (test != null) {
                test.fail("AMPS STATUS MISMATCH → Expected: "
                        + expectedStatus + " | Found: " + finalStatus);
                attachScreenshot(test);
            }

            throw new AssertionError(
                    "❌ AMPS STATUS MISMATCH → Expected: "
                            + expectedStatus
                            + " | Found: "
                            + finalStatus
            );
        }

        if (test != null) test.pass("AMPS Status Matched: " + finalStatus);
    }

    // ============================================================
    // VALIDATE STATUS WITH FULL DB + UI LOGGING
    // ============================================================

    public void validateStatusWithLogging(String platformName,
                                          String expectedStatus,
                                          String identifierValue,
                                          String traceId,
                                          String dbRawStatus) {

        ExtentTest test = ExtentTestManager.getTest();

        // Full DB details table
        attachDbTable(platformName, identifierValue, traceId, dbRawStatus, expectedStatus);

        List<WebElement> statusCells =
                ShadowDom.findAllDeep(driver, STATUS_CELL_DEEP, logger);

        String finalUiStatus = deriveFinalStatus(statusCells);

        if (!finalUiStatus.equalsIgnoreCase(expectedStatus)) {

            if (test != null) {
                test.fail("STATUS MISMATCH → Platform: " + platformName
                        + " | Expected UI: " + expectedStatus
                        + " | Actual UI: " + finalUiStatus);
                attachScreenshot(test);
            }

            throw new AssertionError(
                    "❌ STATUS MISMATCH → Platform: " + platformName +
                            " | Identifier: " + identifierValue +
                            " | TraceId: " + traceId +
                            " | DB Status: " + dbRawStatus +
                            " | Expected UI: " + expectedStatus +
                            " | Actual UI: " + finalUiStatus
            );
        }

        if (test != null) test.pass("Status Matched Successfully: " + finalUiStatus);
    }

    // ============================================================
    // VALIDATE STATUS FOR SPECIFIC PLATFORM
    // ============================================================

    public void validateStatusForPlatform(String platformFromDb,
                                          String expectedStatus) {

        ExtentTest test = ExtentTestManager.getTest();

        attachDbTable(platformFromDb, "N/A", "N/A", "N/A", expectedStatus);

        List<WebElement> systemCells =
                ShadowDom.findAllDeep(
                        driver,
                        "td[data-header-id='systemName'] span.system-name",
                        logger);

        if (systemCells == null || systemCells.isEmpty()) {

            if (test != null) {
                test.fail("Platform rows not found!");
                attachScreenshot(test);
            }

            throw new AssertionError("❌ No platform rows found!");
        }

        for (WebElement system : systemCells) {

            if (!system.isDisplayed()) continue;

            String platformText = system.getText().trim();

            if (platformText.equalsIgnoreCase(platformFromDb)) {

                List<WebElement> statusCells =
                        ShadowDom.findAllDeep(driver, STATUS_CELL_DEEP, logger);

                String finalUiStatus = deriveFinalStatus(statusCells);

                if (!finalUiStatus.equalsIgnoreCase(expectedStatus)) {

                    if (test != null) {
                        test.fail("Platform STATUS MISMATCH → Expected: "
                                + expectedStatus + " | Found: " + finalUiStatus);
                        attachScreenshot(test);
                    }

                    throw new AssertionError(
                            "❌ STATUS MISMATCH → Platform: "
                                    + platformFromDb
                                    + " | Expected: "
                                    + expectedStatus
                                    + " | Actual: "
                                    + finalUiStatus
                    );
                }

                if (test != null)
                    test.pass("Platform Status Matched: " + finalUiStatus);

                return;
            }
        }

        throw new AssertionError("❌ Platform not found: " + platformFromDb);
    }

    // ============================================================
    // COMMON STATUS DERIVATION METHOD (UNCHANGED)
    // ============================================================

    private String deriveFinalStatus(List<WebElement> statusCells) {

        boolean hasError = false;
        boolean hasTerminated = false;
        boolean hasSuccess = false;
        boolean hasRunning = false;

        for (WebElement cell : statusCells) {

            try {

                if (!cell.isDisplayed()) continue;

                String text = cell.getText().trim().toUpperCase();
                if (text.isEmpty()) continue;

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

        if (hasError) return "ERROR";
        if (hasTerminated) return "TERMINATED";
        if (hasSuccess) return "SUCCESS";
        if (hasRunning) return "RUNNING";

        throw new AssertionError("❌ Unable to determine final UI status!");
    }

    // ============================================================
    // COMMON SCREENSHOT METHOD (UNCHANGED)
    // ============================================================

    private void attachScreenshot(ExtentTest test) {

        try {

            if (ExtentTestManager.isScreenshotTaken()) {
                return;
            }

            String path = ScreenshotUtils.captureFullPage(
                    driver,
                    "Status_Mismatch"
            );

            if (path != null) {
                test.addScreenCaptureFromPath(path, "Failure Screenshot");
                ExtentTestManager.markScreenshotTaken();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}