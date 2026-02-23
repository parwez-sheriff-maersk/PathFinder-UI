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

    // ===== Existing logger-enabled methods (kept) =====
    public static SearchContext getShadowRoot(WebDriver driver, By shadowHost, Logger logger) {
        try {
            if (logger != null) logger.info("🔧 Getting shadow root for host: " + shadowHost);
            WebElement host = driver.findElement(shadowHost);
            SearchContext root = host.getShadowRoot();
            if (logger != null) logger.info("✅ Shadow root obtained for host: " + shadowHost);
            return root;
        } catch (Exception ex) {
            if (logger != null) logger.log(Level.SEVERE, "❌ Failed to get shadow root for host: " + shadowHost + " | " + ex.getMessage(), ex);
            throw ex;
        }
    }

    public static WebElement findElementInShadowRoot(WebDriver driver, By shadowHost, By innerLocator, Logger logger) {
        try {
            if (logger != null) logger.info("🔎 Finding element inside shadow root. Host: " + shadowHost + " | Inner: " + innerLocator);
            SearchContext root = getShadowRoot(driver, shadowHost, logger);
            WebElement element = root.findElement(innerLocator);
            if (logger != null) logger.info("✅ Element found inside shadow root. Inner: " + innerLocator);
            return element;
        } catch (Exception ex) {
            if (logger != null) logger.log(Level.SEVERE, "❌ Failed to find element in shadow root. Host: " + shadowHost + " | Inner: " + innerLocator + " | " + ex.getMessage(), ex);
            throw ex;
        }
    }

    public static WebElement waitForInnerClickable(
            WebDriver driver, By shadowHostLocator, By innerLocator, int timeoutSeconds, Logger logger) {

        if (logger != null) {
            logger.info("⏳ Waiting for inner element to be CLICKABLE. Host: " + shadowHostLocator + " | Inner: " + innerLocator);
        }

        FluentWait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(Exception.class);

        WebElement result = wait.until(d -> {
            List<WebElement> hosts = d.findElements(shadowHostLocator);
            for (WebElement host : hosts) {
                try {
                    SearchContext root = host.getShadowRoot();
                    WebElement inner = root.findElement(innerLocator);
                    boolean displayed = inner.isDisplayed();
                    boolean enabled   = inner.isEnabled(); // respects 'disabled' attribute
                    if (displayed && enabled) {
                        try {
                            ((JavascriptExecutor) d).executeScript(
                                "arguments[0].scrollIntoView({block:'center', inline:'center'});", inner);
                        } catch (Exception ignore) {}
                        return inner;
                    }
                } catch (Exception ignore) {
                    // try next host
                }
            }
            return null;
        });

        if (logger != null) {
            logger.info("✅ Inner element is clickable: " + innerLocator);
        }
        return result;
    }

    public static void setValueAndDispatch(WebDriver driver, WebElement input, String value) {
        ((JavascriptExecutor) driver).executeScript(
            "const el = arguments[0]; const val = arguments[1];" +
            "el.value = val;" +
            "el.dispatchEvent(new Event('input',  {bubbles:true}));" +
            "el.dispatchEvent(new Event('change', {bubbles:true}));",
            input, value
        );
    }

    /** Click via JS (often safer for shadow-root elements). */
    public static void jsClick(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    /** Get computed style (e.g., background-color,color) for any element. */
    public static String getComputedStyle(WebDriver driver, WebElement element, String cssProperty) {
        Object val = ((JavascriptExecutor) driver).executeScript(
                "return window.getComputedStyle(arguments[0]).getPropertyValue(arguments[1]);",
                element, cssProperty);
        return val == null ? "" : val.toString().trim();
    }

    /** Scroll element to center (safe no-throw). */
    public static void scrollIntoViewCenter(WebDriver driver, WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center', inline:'center'});", element);
        } catch (Exception ignore) {}
    }

    // ===== Added: find inner element by scanning ANY matching shadow hosts =====
    public static WebElement findElementInAnyShadowHost(
            WebDriver driver, By hostsLocator, By innerLocator, int timeoutSeconds, Logger logger) {

        if (logger != null) logger.info("🔎 Scanning ANY host: " + hostsLocator + " for inner: " + innerLocator);

        FluentWait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(Duration.ofMillis(400))
                .ignoring(Exception.class);

        return wait.until(d -> {
            List<WebElement> hosts = d.findElements(hostsLocator);
            if (logger != null) logger.info("🧩 Hosts found: " + hosts.size() + " for " + hostsLocator);

            for (int i = 0; i < hosts.size(); i++) {
                WebElement host = hosts.get(i);
                try {
                    SearchContext root = host.getShadowRoot();
                    WebElement inner = root.findElement(innerLocator);
                    if (inner != null) {
                        if (logger != null) logger.info("✅ Found inner element in host[" + i + "].");
                        return inner;
                    }
                } catch (Exception ignore) {
                    // try next host
                }
            }
            return null;
        });
    }

    // =========================
    // ✅ NEW: Deep search helpers
    // =========================

    /** Returns all elements matching selector across document + all shadowRoots (recursive). */
    @SuppressWarnings("unchecked")
    public static List<WebElement> deepQueryAll(WebDriver driver, String selector) {
        Object result = ((JavascriptExecutor) driver).executeScript(
            "const sel = arguments[0];" +
            "const out = [];" +
            "const visited = new Set();" +
            "function pushAll(root){ try { root.querySelectorAll(sel).forEach(e=>out.push(e)); } catch(e){} }" +
            "function walk(node){ " +
            "  if(!node || visited.has(node)) return;" +
            "  visited.add(node);" +
            "  pushAll(node);" +
            "  const children = node.children ? Array.from(node.children) : [];" +
            "  for(const ch of children){ walk(ch); }" +
            "  if(node.shadowRoot){ walk(node.shadowRoot); }" +
            "}" +
            "walk(document);" +
            "return out;",
            selector
        );
        return (List<WebElement>) result;
    }

    /** First visible element by deep selector within timeout. */
    public static WebElement findFirstVisibleDeep(WebDriver driver, String selector, int timeoutSeconds, Logger logger) {
        if (logger != null) logger.info("🔎 Deep-searching first VISIBLE element for selector: " + selector);

        FluentWait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(Duration.ofMillis(400))
                .ignoring(Exception.class);

        return wait.until(d -> {
            List<WebElement> all = deepQueryAll(d, selector);
            if (logger != null) logger.info("Deep matches found: " + all.size() + " for selector: " + selector);
            for (WebElement el : all) {
                try {
                    Boolean visible = (Boolean) ((JavascriptExecutor) d).executeScript(
                        "const el=arguments[0]; const r=el.getBoundingClientRect();" +
                        "const cs = window.getComputedStyle(el);" +
                        "return r.width>0 && r.height>0 && cs.visibility!=='hidden' && cs.display!=='none';", el);
                    if (Boolean.TRUE.equals(visible)) {
                        scrollIntoViewCenter(d, el);
                        return el;
                    }
                } catch (Exception ignore) {}
            }
            return null;
        });
    }
    public static java.util.List<WebElement> findAllVisibleDeep(
            WebDriver driver,
            String cssSelector,
            int timeoutSeconds,
            java.util.logging.Logger logger) {

        WebDriverWait wait = new WebDriverWait(driver,
                java.time.Duration.ofSeconds(timeoutSeconds));

        return wait.until(d -> {

            java.util.List<WebElement> all =
                    d.findElements(By.cssSelector(cssSelector));

            java.util.List<WebElement> visible =
                    new java.util.ArrayList<>();

            for (WebElement el : all) {
                try {
                    if (el.isDisplayed()) {
                        visible.add(el);
                    }
                } catch (Exception ignored) {}
            }

            if (visible.isEmpty()) return null;

            logger.info("Visible elements found: " + visible.size());
            return visible;
        });
    }
    public static List<WebElement> findAllDeep(
            WebDriver driver,
            String cssSelector,
            Logger logger) {

        logger.info("🔎 Deep searching ALL elements for selector: " + cssSelector);

        List<WebElement> elements = deepQueryAll(driver, cssSelector);

        logger.info("Found elements count: " +
                (elements == null ? 0 : elements.size()));

        return elements;
    }



}