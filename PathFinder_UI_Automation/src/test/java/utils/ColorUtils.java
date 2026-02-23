package utils;

public class ColorUtils {

    private ColorUtils() {}

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

    // 🔶 ORANGE detection (your working one)
    public static boolean isOrangeCss(String cssColor) {

        int[] rgb = parseRgb(cssColor);
        if (rgb == null) return false;

        int r = rgb[0];
        int g = rgb[1];
        int b = rgb[2];

        // Your UI orange = rgb(255, 220, 166)
        return (r >= 240 && g >= 200 && b <= 180);
    }

    // 🔴 FIXED RED detection (ONLY change we made)
    public static boolean isRedCss(String cssColor) {

        int[] rgb = parseRgb(cssColor);
        if (rgb == null) return false;

        int r = rgb[0];
        int g = rgb[1];
        int b = rgb[2];

        // Your UI red = rgb(255, 200, 200)
        return (r >= 240 && g >= 180 && b >= 180);
    }
}
