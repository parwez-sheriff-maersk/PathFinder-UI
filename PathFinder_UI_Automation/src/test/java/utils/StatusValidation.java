package utils;

import static Pages.PathFinderLocators.*;

import java.util.List;
import java.util.logging.Logger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.aventstack.extentreports.ExtentTest;

public class StatusValidation {

    private final WebDriver driver;
    private final Logger logger = Logger.getLogger(StatusValidation.class.getName());

    public StatusValidation(WebDriver driver) {
        this.driver = driver;
    }

    // ============================================================
    // 🔹 MAIN UNIFIED VALIDATION METHOD
    // ============================================================

    public void validatePlatformStatus(String platformName,
                                       String expectedUiStatus,
                                       String identifierValue,
                                       String traceId,
                                       String dbRawStatus) {

        ExtentTest test = ExtentTestManager.getTest();

        identifierValue = safeValue(identifierValue);
        traceId = safeValue(traceId);
        dbRawStatus = safeValue(dbRawStatus);

        // Attach DB table
        attachDbTable(platformName,
                identifierValue,
                traceId,
                dbRawStatus,
                expectedUiStatus);

        // 🔥 Attach JSON (now properly called)
        attachDbJson(platformName,
                identifierValue,
                traceId,
                dbRawStatus,
                platformName);

        List<WebElement> systemCells =
                ShadowDom.findAllDeep(
                        driver,
                        "td[data-header-id='systemName'] span.system-name",
                        logger);

        if (systemCells == null || systemCells.isEmpty()) {
            failTest("❌ No platform rows found!", test);
        }

        for (WebElement system : systemCells) {

            if (!system.isDisplayed()) continue;

            String platformText = system.getText().trim();

            if (platformText.equalsIgnoreCase(platformName)) {

                List<WebElement> statusCells =
                        ShadowDom.findAllDeep(driver, STATUS_CELL_DEEP, logger);

                String finalUiStatus = deriveFinalStatus(statusCells);

                if (!finalUiStatus.equalsIgnoreCase(expectedUiStatus)) {
                    failTest("❌ STATUS MISMATCH → Platform: "
                            + platformName
                            + " | Expected: "
                            + expectedUiStatus
                            + " | Actual: "
                            + finalUiStatus, test);
                }

                if (test != null)
                    test.pass("Platform Status Matched: " + finalUiStatus);

                return;
            }
        }

        failTest("❌ Platform not found: " + platformName, test);
    }

    // ============================================================
    // 🔹 BACKWARD COMPATIBILITY METHODS
    // ============================================================

    public void validateStatusForPlatform(String platformName,
                                          String expectedUiStatus) {

        validatePlatformStatus(
                platformName,
                expectedUiStatus,
                "N/A",
                "N/A",
                "N/A"
        );
    }

    public void validateStatusWithLogging(String platformName,
                                          String expectedStatus,
                                          String identifierValue,
                                          String traceId,
                                          String dbRawStatus) {

        validatePlatformStatus(
                platformName,
                expectedStatus,
                identifierValue,
                traceId,
                dbRawStatus
        );
    }

    // ============================================================
    // 🔹 DB TABLE ATTACHMENT
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
    // 🔹 ATTACH DB RAW JSON TO EXTENT REPORT
    // ============================================================

    private void attachDbJson(String platform,
                              String identifier,
                              String traceId,
                              String dbStatus,
                              String originSystem) {

        ExtentTest test = ExtentTestManager.getTest();
        if (test == null) return;

        String json =
                "{\n" +
                "  \"platform\": \"" + safeValue(platform) + "\",\n" +
                "  \"identifier\": \"" + safeValue(identifier) + "\",\n" +
                "  \"traceId\": \"" + safeValue(traceId) + "\",\n" +
                "  \"dbRawStatus\": \"" + safeValue(dbStatus) + "\",\n" +
                "  \"originSystem\": \"" + safeValue(originSystem) + "\"\n" +
                "}";

        String formatted =
                "<details style='margin-top:8px'>" +
                "<summary><b>View DB Raw JSON</b></summary>" +
                "<pre style='background-color:#1e1e1e;color:#dcdcdc;padding:10px;border-radius:5px'>" +
                json +
                "</pre></details>";

        test.info(formatted);
    }

    // ============================================================
    // 🔹 STATUS DERIVATION
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
    // 🔹 SAFE VALUE HELPER
    // ============================================================

    private String safeValue(String value) {
        return (value == null || value.trim().isEmpty()) ? "N/A" : value;
    }

    // ============================================================
    // 🔹 FAIL HELPER
    // ============================================================

    private void failTest(String message, ExtentTest test) {

        if (test != null) {
            test.fail(message);
            attachScreenshot(test);
        }

        throw new AssertionError(message);
    }

    // ============================================================
    // 🔹 SCREENSHOT METHOD
    // ============================================================

    private void attachScreenshot(ExtentTest test) {

        try {

            if (ExtentTestManager.isScreenshotTaken()) {
                return;
            }

            String base64 = ScreenshotUtils.captureBase64(driver);

            if (base64 != null) {
                test.addScreenCaptureFromBase64String(base64, "Failure Screenshot");
                ExtentTestManager.markScreenshotTaken();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 // ============================================================
 // 🔹 VALIDATE VISIBLE STATUS (BACKWARD SUPPORT)
 // ============================================================

 public void validateVisibleStatus(String expectedUiStatus) {

     ExtentTest test = ExtentTestManager.getTest();

     if (test != null) {
         test.info("Validating visible expanded status");
     }

     List<WebElement> statusCells =
             ShadowDom.findAllDeep(driver, STATUS_CELL_DEEP, logger);

     if (statusCells == null || statusCells.isEmpty()) {
         failTest("❌ No status cells found!", test);
     }

     String finalUiStatus = deriveFinalStatus(statusCells);

     if (!finalUiStatus.equalsIgnoreCase(expectedUiStatus)) {
         failTest("❌ STATUS MISMATCH → Expected: "
                 + expectedUiStatus
                 + " | Actual: "
                 + finalUiStatus, test);
     }

     if (test != null)
         test.pass("Status Matched: " + finalUiStatus);
 }
}