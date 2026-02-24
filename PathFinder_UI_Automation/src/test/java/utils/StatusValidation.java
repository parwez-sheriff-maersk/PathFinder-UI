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
    // GENERIC STATUS VALIDATION
    // ============================================================

    private void validateStatus(String expectedStatus) {

        logger.info("==============================================");
        logger.info("🔎 Validating Expected Status: " + expectedStatus);
        logger.info("==============================================");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        // Wait until at least one status cell appears
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

        boolean matchFound = false;

        for (WebElement cell : statusCells) {

            String text = cell.getText().trim().toUpperCase();

            if (!text.isEmpty()) {
                logger.info("Status found: " + text);
            }

            if (text.equals(expectedStatus.toUpperCase())) {
                matchFound = true;
                break;
            }
        }

        if (!matchFound) {
            throw new AssertionError(
                    expectedStatus + " status not found in expanded rows"
            );
        }

        logger.info("✅ " + expectedStatus + " status validation PASSED.");
    }

    // ============================================================
    // PUBLIC METHODS
    // ============================================================

    public void validateSuccessStatus() {
        validateStatus("SUCCESS");
    }

    public void validateErrorStatus() {
        validateStatus("ERROR");
    }

    public void validateFailedStatus() {
        validateStatus("FAILED");
    }

    public void validateTerminatedStatus() {
        validateStatus("TERMINATED");
    }
}