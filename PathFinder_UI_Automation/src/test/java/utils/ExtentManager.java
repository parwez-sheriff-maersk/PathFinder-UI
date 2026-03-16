package utils;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExtentManager {

    private static ExtentReports extent;
    public static String runFolder;

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    // Feature-wise tracking
    private static Map<String, Integer> featurePassMap = new HashMap<>();
    private static Map<String, Integer> featureFailMap = new HashMap<>();

    public static ExtentReports getInstance() {

        if (extent == null) {

            deleteOldReports();

            String timestamp =
                    new SimpleDateFormat("yyyyMMdd_HHmmss")
                            .format(new Date());

            runFolder = System.getProperty("user.dir")
                    + "/test-output/Run_" + timestamp;

            new File(runFolder).mkdirs();

            ExtentSparkReporter spark =
                    new ExtentSparkReporter(runFolder + "/ExtentReport.html");

            spark.config().setReportName("PathFinder Automation - Enterprise Dashboard");
            spark.config().setDocumentTitle("Execution Report");
            spark.config().setTheme(Theme.DARK);
            spark.config().setTimeStampFormat("dd MMM yyyy hh:mm:ss a");

            extent = new ExtentReports();
            extent.attachReporter(spark);

            extent.setSystemInfo("Project", "PathFinder UI Automation");
            extent.setSystemInfo("Tester", "Parwez Sheriff");
            extent.setSystemInfo("Environment", "QA");
            extent.setSystemInfo("Browser", "Chrome");
        }

        return extent;
    }

    // ============================================================
    // 🔹 TRACK TEST RESULTS
    // ============================================================

    public static void trackResult(String featureName, boolean isPassed) {

        totalTests++;

        if (isPassed) {
            passedTests++;
            featurePassMap.put(featureName,
                    featurePassMap.getOrDefault(featureName, 0) + 1);
        } else {
            failedTests++;
            featureFailMap.put(featureName,
                    featureFailMap.getOrDefault(featureName, 0) + 1);
        }
    }

    // ============================================================
    // 🔹 ADD ENTERPRISE DASHBOARD
    // ============================================================

    public static void addDashboard() {

        if (extent == null) return;

        double passPercent = totalTests == 0 ? 0 :
                ((double) passedTests / totalTests) * 100;

        double failPercent = 100 - passPercent;

        StringBuilder featureChart = new StringBuilder();

        for (String feature : featurePassMap.keySet()) {

            int pass = featurePassMap.getOrDefault(feature, 0);
            int fail = featureFailMap.getOrDefault(feature, 0);
            int total = pass + fail;

            double featurePassPercent =
                    total == 0 ? 0 : ((double) pass / total) * 100;

            featureChart.append(
                    "<div style='margin-bottom:15px'>" +
                    "<b>" + feature + "</b><br>" +

                    "<div style='width:100%;background:#333;height:20px;border-radius:10px'>" +
                    "<div style='width:" + featurePassPercent + "%;" +
                    "background:#00ff7f;height:20px;border-radius:10px;" +
                    "animation: grow 2s ease-in-out;'></div>" +
                    "</div>" +

                    "<small style='color:#ccc'>Pass: " + pass +
                    " | Fail: " + fail + "</small>" +
                    "</div>"
            );
        }

        String dashboardHtml =
                "<style>" +
                "@keyframes grow { from { width:0%; } to { width:100%; } }" +
                "</style>" +

                "<div style='padding:20px;background:#1e1e1e;border-radius:10px'>" +

                "<h2 style='color:#00c8ff'>📊 Execution Summary</h2>" +

                "<p>Total Tests: <b>" + totalTests + "</b></p>" +
                "<p style='color:#00ff7f'>Passed: " + passedTests +
                " (" + String.format("%.2f", passPercent) + "%)</p>" +
                "<p style='color:#ff4c4c'>Failed: " + failedTests +
                " (" + String.format("%.2f", failPercent) + "%)</p>" +

                "<div style='width:100%;background:#333;height:30px;border-radius:15px;margin-top:10px'>" +

                "<div style='width:" + passPercent + "%;" +
                "background:#00ff7f;height:30px;border-radius:15px;" +
                "animation: grow 2s ease-in-out;float:left'></div>" +

                "<div style='width:" + failPercent + "%;" +
                "background:#ff4c4c;height:30px;border-radius:15px;" +
                "float:left'></div>" +

                "</div>" +

                "<hr style='margin:20px 0;border-color:#444'>" +

                "<h3 style='color:#00c8ff'>📂 Feature-wise Results</h3>" +
                featureChart +

                "</div>";

        extent.createTest("📊 Enterprise Dashboard")
                .info(dashboardHtml);
    }

    // ============================================================
    // 🔹 DELETE OLD REPORTS
    // ============================================================

    private static void deleteOldReports() {

        Path path = Paths.get(System.getProperty("user.dir"), "test-output");

        try {
            if (Files.exists(path)) {
                Files.walk(path)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                System.out.println("Old reports deleted successfully.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}