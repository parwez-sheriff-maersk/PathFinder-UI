package utils;

import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class ShadowDom {

    private ShadowDom() {}

    // ============================================================
    // SHADOW ROOT HANDLING
    // ============================================================

    public static SearchContext getShadowRoot(WebDriver driver, By shadowHost, Logger logger) {
        try {
            if (logger != null) logger.info("🔧 Getting shadow root for host: " + shadowHost);
            WebElement host = driver.findElement(shadowHost);
            SearchContext root = host.getShadowRoot();
            if (logger != null) logger.info("✅ Shadow root obtained for host: " + shadowHost);
            return root;
        } catch (Exception ex) {
            if (logger != null)
                logger.log(Level.SEVERE,
                        "❌ Failed to get shadow root for host: "
                                + shadowHost + " | " + ex.getMessage(), ex);
            throw ex;
        }
    }

    public static WebElement findElementInShadowRoot(
            WebDriver driver, By shadowHost, By innerLocator, Logger logger) {

        try {
            SearchContext root = getShadowRoot(driver, shadowHost, logger);
            return root.findElement(innerLocator);
        } catch (Exception ex) {
            if (logger != null)
                logger.log(Level.SEVERE,
                        "❌ Failed to find element in shadow root.", ex);
            throw ex;
        }
    }

    // ============================================================
    // WAIT FOR INNER CLICKABLE
    // ============================================================

    public static WebElement waitForInnerClickable(
            WebDriver driver,
            By shadowHostLocator,
            By innerLocator,
            int timeoutSeconds,
            Logger logger) {

        FluentWait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(Exception.class);

        return wait.until(d -> {

            List<WebElement> hosts = d.findElements(shadowHostLocator);

            for (WebElement host : hosts) {
                try {
                    SearchContext root = host.getShadowRoot();
                    WebElement inner = root.findElement(innerLocator);

                    if (inner.isDisplayed() && inner.isEnabled()) {

                        ((JavascriptExecutor) d).executeScript(
                                "arguments[0].scrollIntoView({block:'center'});",
                                inner);

                        return inner;
                    }
                } catch (Exception ignored) {}
            }

            return null;
        });
    }

    // ============================================================
    // JS HELPERS
    // ============================================================

    public static void jsClick(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", element);
    }

    public static void scrollIntoViewCenter(WebDriver driver, WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center', inline:'center'});",
                    element);
        } catch (Exception ignored) {}
    }

    public static String getComputedStyle(
            WebDriver driver, WebElement element, String cssProperty) {

        Object val = ((JavascriptExecutor) driver).executeScript(
                "return window.getComputedStyle(arguments[0]).getPropertyValue(arguments[1]);",
                element, cssProperty);

        return val == null ? "" : val.toString().trim();
    }

    public static void setValueAndDispatch(
            WebDriver driver, WebElement input, String value) {

        ((JavascriptExecutor) driver).executeScript(
                "const el = arguments[0];" +
                        "el.value = arguments[1];" +
                        "el.dispatchEvent(new Event('input', {bubbles:true}));" +
                        "el.dispatchEvent(new Event('change', {bubbles:true}));",
                input, value);
    }

    // ============================================================
    // DEEP SEARCH (SHADOW DOM SAFE)
    // ============================================================

    @SuppressWarnings("unchecked")
    public static List<WebElement> deepQueryAll(WebDriver driver, String selector) {

        Object result = ((JavascriptExecutor) driver).executeScript(
                "const sel = arguments[0];" +
                        "const out = [];" +
                        "function walk(node) {" +
                        "  if (!node) return;" +
                        "  if (node.querySelectorAll) {" +
                        "    try { node.querySelectorAll(sel).forEach(e => out.push(e)); } catch(e){}" +
                        "  }" +
                        "  if (node.shadowRoot) walk(node.shadowRoot);" +
                        "  if (node.children) Array.from(node.children).forEach(walk);" +
                        "}" +
                        "walk(document);" +
                        "return out;",
                selector);

        return (List<WebElement>) result;
    }

    public static List<WebElement> findAllDeep(
            WebDriver driver,
            String cssSelector,
            Logger logger) {

        if (logger != null)
            logger.info("🔎 Deep searching selector: " + cssSelector);

        List<WebElement> elements = deepQueryAll(driver, cssSelector);

        if (logger != null)
            logger.info("Found elements count: "
                    + (elements == null ? 0 : elements.size()));

        return elements;
    }

    // ============================================================
    // ✅ NEW SCROLL METHOD (Fixes Your Error)
    // ============================================================

    public static void scrollPageDown(WebDriver driver) {

        try {

            JavascriptExecutor js = (JavascriptExecutor) driver;

            js.executeScript(
                    "window.scrollBy({top: 800, behavior: 'smooth'});"
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 // ============================================================
 // DEEP SEARCH FROM SCOPED ELEMENT (OVERLOADED VERSION)
 // ============================================================

 @SuppressWarnings("unchecked")
 public static List<WebElement> findAllDeep(
         WebDriver driver,
         WebElement rootElement,
         String cssSelector,
         Logger logger) {

     if (logger != null)
         logger.info("🔎 Deep searching inside scoped element: " + cssSelector);

     Object result = ((JavascriptExecutor) driver).executeScript(
             "const root = arguments[0];" +
             "const sel = arguments[1];" +
             "const out = [];" +
             "function walk(node) {" +
             "  if (!node) return;" +
             "  if (node.querySelectorAll) {" +
             "    try { node.querySelectorAll(sel).forEach(e => out.push(e)); } catch(e){}" +
             "  }" +
             "  if (node.shadowRoot) walk(node.shadowRoot);" +
             "  if (node.children) Array.from(node.children).forEach(walk);" +
             "}" +
             "walk(root);" +
             "return out;",
             rootElement,
             cssSelector);

     List<WebElement> elements = (List<WebElement>) result;

     if (logger != null)
         logger.info("Scoped elements count: "
                 + (elements == null ? 0 : elements.size()));

     return elements;
 }

 }