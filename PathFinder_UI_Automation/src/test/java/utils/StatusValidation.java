package utils;

import static Pages.PathFinderLocators.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    // Simple overload — scan all, last row decides (no extra params)
    public void validateBookingStatusSimple(String expectedUiStatus) {

        validateBookingStatus(
                "N/A",
                expectedUiStatus,
                "N/A",
                "N/A",
                "N/A"
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
    // 🔹 SCAN ALL ROWS, LAST ROW DECIDES STATUS
    //    Filters by platform_identifier if available
    // ============================================================

    public void validateBookingStatus(String platformName,
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

        // Attach JSON
        attachDbJson(platformName,
                identifierValue,
                traceId,
                dbRawStatus,
                platformName);

        // ── Step 1: Find all Platform Identifier cells to build row index ──
        List<WebElement> pidCells =
                ShadowDom.findAllDeep(driver, PLATFORM_ID_CELL_DEEP, logger);

        List<WebElement> statusCells =
                ShadowDom.findAllDeep(driver, STATUS_CELL_DEEP, logger);

        if (statusCells == null || statusCells.isEmpty()) {
            failTest("❌ No status cells found!", test);
        }

        // ── Step 2: Build a set of row indices matching our identifier ──
        Set<Integer> matchingIndices = new HashSet<>();
        boolean canFilter = pidCells != null && !pidCells.isEmpty()
                && !identifierValue.equals("N/A");

        if (canFilter) {
            logger.info("🔍 Filtering rows by Platform Identifier: " + identifierValue);
            for (int idx = 0; idx < pidCells.size(); idx++) {
                try {
                    String pidText = pidCells.get(idx).getText().trim();
                    if (pidText.equals(identifierValue)) {
                        matchingIndices.add(idx);
                    }
                } catch (Exception ignored) {}
            }
            logger.info("   ✅ Found " + matchingIndices.size()
                    + " rows matching identifier " + identifierValue);
        }

        // ── Step 3: Scan rows — if filter available, only scan matching rows ──
        String lastStatus = null;
        int scannedCount = 0;

        logger.info("🔍 Scanning status rows for " + platformName + "...");

        for (int i = 0; i < statusCells.size(); i++) {

            // If we can filter and this row doesn't match, skip it
            if (canFilter && !matchingIndices.isEmpty() && !matchingIndices.contains(i)) {
                continue;
            }

            try {

                WebElement cell = statusCells.get(i);
                ShadowDom.scrollIntoViewCenter(driver, cell);

                String text = cell.getText().trim().toUpperCase();
                if (text.isEmpty()) continue;

                scannedCount++;
                lastStatus = text;

                logger.info("   ➡ Row " + (i + 1) + " status: " + text
                        + (canFilter ? " [MATCHED]" : ""));

            } catch (Exception ignored) {}
        }

        if (lastStatus == null) {
            failTest("❌ No status found after scanning rows! "
                    + (canFilter ? "(Filtered by: " + identifierValue + ")" : ""), test);
        }

        // ── Step 4: Normalize the last status ──
        String finalUiStatus;

        if (lastStatus.contains("ERROR") || lastStatus.contains("FAIL")) {
            finalUiStatus = "ERROR";
        } else if (lastStatus.contains("TERMINATED") || lastStatus.contains("CANCELLED")) {
            finalUiStatus = "TERMINATED";
        } else if (lastStatus.contains("SUCCESS") || lastStatus.contains("COMPLETED") || lastStatus.contains("CREATED")) {
            finalUiStatus = "SUCCESS";
        } else if (lastStatus.contains("RUNNING") || lastStatus.contains("IN_PROGRESS")) {
            finalUiStatus = "RUNNING";
        } else {
            finalUiStatus = lastStatus;
        }

        logger.info("📊 Total rows scanned: " + scannedCount);
        logger.info("✅ FINAL status (last row): " + finalUiStatus);
        logger.info("📌 Expected status: " + expectedUiStatus);

        if (!finalUiStatus.equalsIgnoreCase(expectedUiStatus)) {
            failTest("❌ STATUS MISMATCH → Platform: "
                    + platformName
                    + " | Identifier: " + identifierValue
                    + " | TraceId: " + traceId
                    + " | DB Raw: " + dbRawStatus
                    + " | Expected: " + expectedUiStatus
                    + " | Actual (last row): " + finalUiStatus
                    + " | Total rows scanned: " + scannedCount, test);
        }

        logger.info("✅ STATUS MATCHED: " + finalUiStatus + " == " + expectedUiStatus);

        if (test != null)
            test.pass("Status Matched (last row): " + finalUiStatus
                    + " | Identifier: " + identifierValue
                    + " | Rows scanned: " + scannedCount);
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