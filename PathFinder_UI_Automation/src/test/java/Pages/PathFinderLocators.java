package Pages;

import org.openqa.selenium.By;

public final class PathFinderLocators {

    private PathFinderLocators() {}

    // =============================================================
    // COMMON / REUSABLE LOCATORS
    // =============================================================

    // Generic inner <input> inside any shadow root (mc-input, mc-drawer, etc.)
    public static final By SHADOW_INPUT = By.cssSelector("input");

    // Alias — old code references INNER_ANY_INPUT, points to same locator
    public static final By INNER_ANY_INPUT = SHADOW_INPUT;

    // mc-select dropdown (used as Trace Table loaded indicator AND Business Identifier dropdown)
    public static final By MC_SELECT_DROPDOWN = By.cssSelector("mc-select");

    // Trace Table tab
    public static final By TRACE_TABLE_TAB =
            By.xpath("//span[text()='Trace Table']");

    // Deep search for dropdown options
    public static final String DROPDOWN_OPTION_DEEP = "mc-option";

    // Deep search for status cells
    public static final String STATUS_CELL_DEEP =
            "td[data-header-id='status']";

    // Deep search for platform identifier cells (to filter rows by identifier)
    public static final String PLATFORM_ID_CELL_DEEP =
            "td[data-header-id='platformIdentifier']";

    // Fallback badge selectors for status
    public static final String GENERIC_BADGE_DEEP =
            "span.trace-badge, .trace-badge, .badge";

    // Platform system name cells (used to find AMPS/SEEBURGER rows after expansion)
    public static final String PLATFORM_SYSTEM_NAME_DEEP =
            "td[data-header-id='systemName'] span.system-name";

    // Deep search selector for expand row buttons
    public static final String EXPAND_ROW_BTN_DEEP =
            "button[aria-label='Expand row']";

    // =============================================================
    // SEARCH BUTTON / INPUT FIELDS
    // =============================================================

    public static final By SEARCH_BTN_HOST = By.cssSelector("mc-button.search-btn");
    public static final By SEARCH_BTN      = By.cssSelector("button");

    public static final By PLATFORM_HOSTS  = By.cssSelector("mc-input-inline, mc-input");

    public static final By INNER_LABEL = By.cssSelector(
        "label[part='label'], #label, .label, mc-label[part='label']"
    );
    public static final By INNER_INPUT = By.cssSelector(
        "input[part='input'], input.input, input[data-cy='input'], input"
    );

    // All inline input fields (Business Identifier Value + Transaction Identifier)
    public static final By INLINE_INPUT_FIELDS =
            By.cssSelector("mc-input.inline-input");

    // =============================================================
    // ADVANCED SEARCH (DATE DRAWER) LOCATORS
    // =============================================================

    public static final By ADV_SEARCH_HOST    = By.cssSelector("mc-button");
    public static final By PARTNER_NAME_HOST  = By.cssSelector("mc-input");
    public static final By ADV_SEARCH_BTN     = By.cssSelector(
        "button[aria-label*='Advanced Search'], button[title*='Advanced Search']"
    );
    public static final By PARTNER_NAME_INPUT = By.cssSelector("input[placeholder*='Partner']");

    public static final By START_DATE_HOST     = By.cssSelector("mc-drawer[open] mc-input[name='start-time']");
    public static final By END_DATE_HOST       = By.cssSelector("mc-drawer[open] mc-input[name='end-time']");
    public static final By DATE_PICKER_BUTTON  = By.cssSelector("button[aria-label='Open']");

    // =============================================================
    // TRACE TABLE / STATUS BADGES
    // =============================================================

    public static final By TRACE_TABLE_HOST       = By.cssSelector("mds-table[aria-label='Trace transactions table']");
    public static final By STATUS_BADGE_IN_SHADOW = By.cssSelector("tbody td span.trace-badge");
    public static final By TRACE_TABLE_HOST_ANY   = By.cssSelector("mds-table, mds-data-table");
    public static final By STATUS_BADGE_GENERIC   = By.cssSelector(
        "tbody td span.trace-badge, tbody td .trace-badge, tbody td mds-badge, tbody td .badge"
    );

    public static final By TRANSACTION_ID_BADGE = By.cssSelector(
        ".transaction-id-section span.trace-badge, " +
        "[aria-label='Transaction ID'] span.trace-badge"
    );

    public static final By TXN_ID_BADGE_OPT1 = By.cssSelector(
        ".transaction-id-section .trace-badge, .transaction-id-section .badge"
    );
    public static final By TXN_ID_BADGE_OPT2 = By.cssSelector(
        "[aria-label='Transaction ID'] .trace-badge, [aria-label='Transaction ID'] .badge"
    );
    public static final By TXN_ID_BADGE_OPT3 = By.cssSelector(
        "[data-cy='transaction-id'] .trace-badge, [data-testid='transaction-id'] .trace-badge"
    );
    public static final By TXN_ID_BADGE_OPT4 = By.cssSelector(
        ".trace-badge[aria-label*='Transaction'], .badge[aria-label*='Transaction']"
    );

    // =============================================================
    // ROW EXPANDER LOCATORS
    // =============================================================

    public static final By PLATFORM_ROW_EXPANDER_HOST =
            By.cssSelector("mc-button.mc-table__expanded-row__trigger");

    public static final By PLATFORM_ROW_EXPANDER_INNER =
            By.cssSelector("button[aria-label='Expand row']");

    public static final By PLATFORM_ROW_COLLAPSER_INNER =
            By.cssSelector("button[aria-label='Collapse row']");

    public static final By PLATFORM_ROW_EXPANDER_HOST_ALT1 =
            By.cssSelector(".mds-table__column--row-expander mc-button");

    public static final By PLATFORM_ROW_EXPANDER_HOST_ALT2 =
            By.cssSelector(".mds-table__column--row-expander mds-button[aria-label='Expand row'], mds-button[aria-label='Expand row']");

    public static final String ANY_EXPAND_BUTTON_DEEP =
            "mc-button.mc-table__expanded-row__trigger button[aria-label='Expand row'], " +
            ".mds-table__column--row-expander mc-button button[aria-label='Expand row'], " +
            "button[aria-label='Expand row']";

    public static final String EXPANDED_DETAILS_DEEP =
            "div[role='row'] + div[role='rowgroup'], " +
            ".mds-table__expanded-row, " +
            ".mc-table__expanded-row";

    public static final String NESTED_PLATFORM_EXPAND_BUTTON_IN_DETAILS_DEEP =
            "mc-button.mc-table__expanded-row__trigger button[aria-label='Expand row']";

    // =============================================================
    // ADVANCED SEARCH DRAWER LOCATORS (Transaction ID + Platform ID)
    // =============================================================

    public static final By ADV_DRAWER_OPEN =
            By.cssSelector("mc-drawer[open]");

    public static final By ADV_DRAWER_TRANSACTION_HOST =
            By.cssSelector("mc-drawer[open] mc-input[title='Enter Transaction (trace) ID']");

    public static final By ADV_DRAWER_PLATFORM_HOST =
            By.cssSelector("mc-drawer[open] mc-input[title='Enter the platform Id']");

    public static final By ADV_DRAWER_SEARCH_BTN_HOST =
            By.cssSelector("mc-drawer[open] mc-button");

    // =============================================================
    // JS SCRIPTS FOR SHADOW DOM BUTTON CLICKS
    // =============================================================

    public static final String JS_CLICK_ADVANCED_SEARCH_BTN =
            "var hosts = document.querySelectorAll('mc-button');" +
            "for (var h of hosts) {" +
            "  if (h.shadowRoot) {" +
            "    var inner = h.shadowRoot.querySelector(\"button[aria-label='Advanced Search']\");" +
            "    if (inner) { inner.click(); break; }" +
            "  }" +
            "}";

    public static final String JS_CLICK_DRAWER_SEARCH_BTN =
            "var hosts = document.querySelectorAll('mc-drawer[open] mc-button');" +
            "for (var h of hosts) {" +
            "  if (h.shadowRoot) {" +
            "    var inner = h.shadowRoot.querySelector(\"button[aria-label='Search']\");" +
            "    if (inner) { inner.click(); return true; }" +
            "  }" +
            "}" +
            "return false;";
}
