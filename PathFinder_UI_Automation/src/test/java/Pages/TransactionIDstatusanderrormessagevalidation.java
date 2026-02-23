package Pages;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;

import utils.ShadowDom;

public class TransactionIDstatusanderrormessagevalidation {

    private final WebDriver driver;
    private static final Logger logger =
            Logger.getLogger(TransactionIDstatusanderrormessagevalidation.class.getName());

    public TransactionIDstatusanderrormessagevalidation(WebDriver driver) {
        this.driver = driver;
        logger.info("📄 TransactionIDstatusanderrormessagevalidation page initialized");
    }

    public void Validation() throws Exception {
        logger.info("🚀 Pathfinder: Advanced Search → Partner 'bosch' → Set dates → Click Search");

        try {
            // ===== Open Advanced Search =====
            logger.info("🔎 Waiting for Advanced Search button");
            WebElement advancedSearchBtn = ShadowDom.waitForInnerClickable(
                    driver,
                    PathFinderLocators.ADV_SEARCH_HOST,
                    PathFinderLocators.ADV_SEARCH_BTN,
                    15,
                    logger
            );
            advancedSearchBtn.click();
            logger.info("✅ Advanced Search opened");

            // ===== Partner Name =====
            logger.info("🔎 Waiting for Partner Name input");
            WebElement partnerNameInput = ShadowDom.waitForInnerClickable(
                    driver,
                    PathFinderLocators.PARTNER_NAME_HOST,
                    PathFinderLocators.PARTNER_NAME_INPUT,
                    15,
                    logger
            );
            partnerNameInput.clear();
            partnerNameInput.sendKeys("crocs");
            partnerNameInput.sendKeys(Keys.TAB);

            // ===== Dates =====
            setStartDateYesterday10();
           // Thread.sleep(90000);
            setEndDateToday10(); 
            Thread.sleep(9000000);

            // ===== Search AFTER End Date completes =====
            logger.info("🔎 Waiting for Search button");
            WebElement searchBtn = ShadowDom.waitForInnerClickable(
                    driver,
                    PathFinderLocators.SEARCH_BTN_HOST,
                    PathFinderLocators.SEARCH_BTN,
                    15,
                    logger
            );

            logger.info("🖱️ Clicking Search");
            try {
                searchBtn.click();
            } catch (Exception e) {
                logger.warning("⚠️ Normal click failed, applying JS click");
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", searchBtn);
            }

            // Unconditional wait after Search click (1 minute)
            logger.info("⏳ Waiting 60 seconds for results to load...");
            try { Thread.sleep(60000); } catch (InterruptedException ignored) {}
            logger.info("⏱️ Completed 60s wait. Proceeding...");

            logger.info("🏁 Completed validation flow");

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "❌ Validation flow failed: " + ex.getMessage(), ex);
            throw ex;
        }
    }


    public void setStartDateYesterday10() {
        ZoneId zone = ZoneId.systemDefault();  // datetime-local requires LOCAL time
        LocalDateTime now = LocalDateTime.now(zone);

        LocalDateTime start = now.minusDays(2)
                .withHour(10)
                .withMinute(5)
                .withSecond(0)
                .withNano(0);

        logger.info("🟦 Setting Start Date (local): " + start);

        WebElement startInput = waitForDateInnerInput(PathFinderLocators.START_DATE_HOST, 20, false);
        logInputState(startInput, "Start:before");

        String iso = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].focus(); arguments[0].value=arguments[1];" +
                        "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));",
                startInput, iso
        );

        try { Thread.sleep(150); } catch (Exception ignored) {}

        logInputState(startInput, "Start:after-input");

        String val = (String) ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].value;", startInput);

        if (val == null || val.isBlank()) {
            logger.warning("⚠️ Start cleared → retry w/ commit");

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].focus(); arguments[0].value=arguments[1];" +
                            "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));" +
                            "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));" +
                            "arguments[0].dispatchEvent(new Event('blur',{bubbles:true}));",
                    startInput, iso
            );

            try { Thread.sleep(150); } catch (Exception ignored) {}
            logInputState(startInput, "Start:post-retry");
        }

        logger.info("✅ Start ready (pending End commit)");
    }


    public void setEndDateToday10() throws InterruptedException {
        ZoneId zone = ZoneId.systemDefault();  // datetime-local requires LOCAL time
        LocalDateTime now = LocalDateTime.now(zone);

        // Yesterday 10:05 (as requested)
        LocalDateTime end = now.minusDays(1)
                .withHour(10)
                .withMinute(5)
                .withSecond(0)
                .withNano(0);

        logger.info("🟩 Setting End Date (local, yesterday): " + end);

        JavascriptExecutor js = (JavascriptExecutor) driver;

        WebElement startInput = waitForDateInnerInput(PathFinderLocators.START_DATE_HOST, 20, false);
        WebElement endInput   = waitForDateInnerInput(PathFinderLocators.END_DATE_HOST, 20, true);

        logInputState(startInput, "Start:pre-commit");
        logInputState(endInput,   "End:before");

        boolean endDisabled = Boolean.TRUE.equals(
                js.executeScript("return arguments[0].disabled===true;", endInput)
        );

        // If End is disabled, commit Start to enable it
        if (endDisabled) {
            logger.warning("🟠 End disabled → committing Start to enable End");

            js.executeScript(
                    "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));" +
                            "arguments[0].dispatchEvent(new Event('blur',{bubbles:true}));",
                    startInput
            );

            long t0 = System.currentTimeMillis();
            while (Boolean.TRUE.equals(js.executeScript("return arguments[0].disabled===true;", endInput))
                    && System.currentTimeMillis() - t0 < 2000) {
                try { Thread.sleep(120); } catch (Exception ignored) {}
            }

            
            if (Boolean.TRUE.equals(js.executeScript("return arguments[0].disabled===true;", endInput))) {
                logger.warning("🟥 End still disabled → HARD FALLBACK (force enable)");

                js.executeScript("arguments[0].disabled=false;", endInput);
                try { Thread.sleep(150); } catch (Exception ignored) {}

                logInputState(endInput, "End:after-force-enable");
            }
        }

        String endIso = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        logger.info("⌨️ Assign End ISO: " + endIso);

       
        js.executeScript(
                "arguments[0].focus(); arguments[0].value=arguments[1];" +
                        "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));",
                endInput, endIso
        );

        try { Thread.sleep(150); } catch (Exception ignored) {}

        
        js.executeScript(
                "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));" +
                        "arguments[0].dispatchEvent(new Event('blur',{bubbles:true}));" +
                        "arguments[1].dispatchEvent(new Event('change',{bubbles:true}));" +
                        "arguments[1].dispatchEvent(new Event('blur',{bubbles:true}));",
                startInput, endInput
        );

        try { Thread.sleep(150); } catch (Exception ignored) {}

        logInputState(startInput, "Start:after-commit");
        logInputState(endInput,   "End:after-commit");

       
        String sVal = (String) js.executeScript("return arguments[0].value;", startInput);
        if (sVal == null || sVal.isBlank()) {
            logger.warning("⚠️ Start cleared after commit → reapply");

            LocalDateTime s = LocalDateTime.now(zone).minusDays(2)
                    .withHour(10).withMinute(5).withSecond(0).withNano(0);

            String sIso = s.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

            js.executeScript(
                    "arguments[0].focus(); arguments[0].value=arguments[1];" +
                            "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));",
                    startInput, sIso
            );

            js.executeScript(
                    "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));" +
                            "arguments[0].dispatchEvent(new Event('blur',{bubbles:true}));" +
                            "arguments[1].dispatchEvent(new Event('change',{bubbles:true}));" +
                            "arguments[1].dispatchEvent(new Event('blur',{bubbles:true}));",
                    startInput, endInput
            );

            try { Thread.sleep(160); } catch (Exception ignored) {}

            logInputState(startInput, "Start:post-reapply");
        }

        logger.info("✅ Both dates committed (Start = 2 days back, End = yesterday)");
    }

   

    private WebElement waitForDateInnerInput(By dateHost, int timeout, boolean allowDisabled) {
        logger.info("⏳ Waiting for date inner input: " + dateHost + " | allowDisabled=" + allowDisabled);

        FluentWait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeout))
                .pollingEvery(Duration.ofMillis(400))
                .ignoring(Exception.class);

        return wait.until(d -> {
            List<WebElement> hosts = d.findElements(dateHost);

            for (WebElement host : hosts) {
                try {
                    SearchContext root = host.getShadowRoot();
                    WebElement inner = root.findElement(PathFinderLocators.INNER_ANY_INPUT);

                    if (inner != null && inner.isDisplayed()) {
                        boolean dis = Boolean.TRUE.equals(
                                ((JavascriptExecutor) d).executeScript("return arguments[0].disabled===true;", inner)
                        );

                        if (!dis || allowDisabled) {
                            try {
                                ((JavascriptExecutor) d).executeScript(
                                        "arguments[0].scrollIntoView({block:'center', inline:'center'});", inner);
                            } catch (Exception ignored) {}
                            return inner;
                        }
                    }

                } catch (Exception ignored) {}
            }
            return null;
        });
    }

    

    private void logInputState(WebElement input, String tag) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        String type  = (String) js.executeScript("return arguments[0].getAttribute('type')||'';", input);
        String value = (String) js.executeScript("return arguments[0].value||'';", input);
        String ph    = (String) js.executeScript("return arguments[0].getAttribute('placeholder')||'';", input);
        String min   = (String) js.executeScript("return arguments[0].getAttribute('min')||'';", input);
        String max   = (String) js.executeScript("return arguments[0].getAttribute('max')||'';", input);
        boolean dis  = Boolean.TRUE.equals(js.executeScript("return arguments[0].disabled===true;", input));
        boolean val  = Boolean.TRUE.equals(js.executeScript("return arguments[0].validity.valid;", input));
        String msg   = (String) js.executeScript("return arguments[0].validationMessage||'';", input);

        logger.info("🔎 [" + tag + "] type=" + type + " | value=" + value + " | ph=" + ph +
                " | min=" + min + " | max=" + max + " | valid=" + val + " | disabled=" + dis + " | msg=" + msg);
    }
}