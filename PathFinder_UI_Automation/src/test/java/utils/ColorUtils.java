package utils;

public class ColorUtils {

    private ColorUtils() {}

    // ============================================================
    // RGB PARSER (Supports rgb + rgba)
    // ============================================================

    public static int[] parseRgb(String cssColor) {

        if (cssColor == null) return null;

        String cleaned = cssColor.trim().toLowerCase();

        if (!(cleaned.startsWith("rgb(") || cleaned.startsWith("rgba(")))
            return null;

        cleaned = cleaned.replace("rgba(", "")
                         .replace("rgb(", "")
                         .replace(")", "")
                         .trim();

        String[] parts = cleaned.split(",");

        if (parts.length < 3) return null;

        try {
            int r = Integer.parseInt(parts[0].trim());
            int g = Integer.parseInt(parts[1].trim());
            int b = Integer.parseInt(parts[2].trim());
            return new int[]{r, g, b};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ============================================================
    // ORANGE
    // ============================================================

    public static boolean isOrangeCss(String cssColor) {

        int[] rgb = parseRgb(cssColor);
        if (rgb == null) return false;

        int r = rgb[0];
        int g = rgb[1];
        int b = rgb[2];

        // Example: rgb(255, 220, 166)
        return (r >= 230 && g >= 180 && b <= 190);
    }

    // ============================================================
    // RED
    // ============================================================

    public static boolean isRedCss(String cssColor) {

        int[] rgb = parseRgb(cssColor);
        if (rgb == null) return false;

        int r = rgb[0];
        int g = rgb[1];
        int b = rgb[2];

        // Strong red dominance
        return (r >= 230 && g <= 210 && b <= 210);
    }

    // ============================================================
    // GREEN
    // ============================================================

    public static boolean isGreenCss(String cssColor) {

        int[] rgb = parseRgb(cssColor);
        if (rgb == null) return false;

        int r = rgb[0];
        int g = rgb[1];
        int b = rgb[2];

        // Example UI: rgb(191, 249, 199)
        return (g >= 220 && r <= 210 && b <= 210);
    }
}