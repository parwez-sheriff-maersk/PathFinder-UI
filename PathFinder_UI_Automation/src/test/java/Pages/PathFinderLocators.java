package Pages;

import org.openqa.selenium.By;

public final class PathFinderLocators {

    private PathFinderLocators() {}

    // =========================
    // Search / Inputs
    // =========================
    public static final By SEARCH_BTN_HOST = By.cssSelector("mc-button.search-btn");
    public static final By SEARCH_BTN      = By.cssSelector("button");

    public static final By PLATFORM_HOSTS  = By.cssSelector("mc-input-inline, mc-input");

    public static final By INNER_LABEL = By.cssSelector(
        "label[part='label'], #label, .label, mc-label[part='label']"
    );
    public static final By INNER_INPUT = By.cssSelector(
        "input[part='input'], input.input, input[data-cy='input'], input"
    );

    public static final By ADV_SEARCH_HOST    = By.cssSelector("mc-button");
    public static final By PARTNER_NAME_HOST  = By.cssSelector("mc-input");
    public static final By ADV_SEARCH_BTN     = By.cssSelector(
        "button[aria-label*='Advanced Search'], button[title*='Advanced Search']"
    );
    public static final By PARTNER_NAME_INPUT = By.cssSelector("input[placeholder*='Partner']");

    public static final By START_DATE_HOST     = By.cssSelector("mc-drawer[open] mc-input[name='start-time']");
    public static final By END_DATE_HOST       = By.cssSelector("mc-drawer[open] mc-input[name='end-time']");
    public static final By INNER_ANY_INPUT     = By.cssSelector("input");
    public static final By DATE_PICKER_BUTTON  = By.cssSelector("button[aria-label='Open']");

    // =========================
    // Table / Status badge (generic)
    // =========================
    public static final By TRACE_TABLE_HOST       = By.cssSelector("mds-table[aria-label='Trace transactions table']");
    public static final By STATUS_BADGE_IN_SHADOW = By.cssSelector("tbody td span.trace-badge");
    public static final By TRACE_TABLE_HOST_ANY   = By.cssSelector("mds-table, mds-data-table");
    public static final By STATUS_BADGE_GENERIC   = By.cssSelector(
        "tbody td span.trace-badge, tbody td .trace-badge, tbody td mds-badge, tbody td .badge"
    );

    // =========================
    // Transaction ID badge (top panel)
    // =========================
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

    // =========================
    // Row expanders (chevrons)
    // =========================

    // Host mc-button for the row expander (DO NOT put aria-label on host; it's on the inner <button>)
    public static final By PLATFORM_ROW_EXPANDER_HOST =
            By.cssSelector("mc-button.mc-table__expanded-row__trigger");

    // Inner clickable <button> inside mc-button shadowRoot
    public static final By PLATFORM_ROW_EXPANDER_INNER =
            By.cssSelector("button[aria-label='Expand row']");

    public static final By PLATFORM_ROW_COLLAPSER_INNER =
            By.cssSelector("button[aria-label='Collapse row']");

    // Fallback hosts (older/variant DOMs)
    public static final By PLATFORM_ROW_EXPANDER_HOST_ALT1 =
            By.cssSelector(".mds-table__column--row-expander mc-button");

    public static final By PLATFORM_ROW_EXPANDER_HOST_ALT2 =
            By.cssSelector(".mds-table__column--row-expander mds-button[aria-label='Expand row'], mds-button[aria-label='Expand row']");

    // ===== Deep selectors (strings) for cross-shadow deep scans =====

    // Any inner <button aria-label='Expand row'> (main or nested)
    public static final String ANY_EXPAND_BUTTON_DEEP =
            "mc-button.mc-table__expanded-row__trigger button[aria-label='Expand row'], " +
            ".mds-table__column--row-expander mc-button button[aria-label='Expand row'], " +
            "button[aria-label='Expand row']";

    // The expanded-details container that appears after the main row is expanded
    public static final String EXPANDED_DETAILS_DEEP =
            "div[role='row'] + div[role='rowgroup'], " +  // common grid pattern
            ".mds-table__expanded-row, " +                // generic class if present
            ".mc-table__expanded-row";                    // MC variant if present

    // Inside expanded details, the nested Platform expander button (inner <button>)
    public static final String NESTED_PLATFORM_EXPAND_BUTTON_IN_DETAILS_DEEP =
            "mc-button.mc-table__expanded-row__trigger button[aria-label='Expand row']";

    // Status cell inside the expanded details table rows (data-header-id='status')
    public static final String DETAILS_STATUS_CELL_DEEP =
            "td[data-header-id='status'], [data-header-id='status']";
}