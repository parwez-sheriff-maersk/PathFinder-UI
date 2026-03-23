package utils;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExtentManager {

    private static ExtentReports extent;
    public static String runFolder;

    private static int totalTests  = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    private static long startTime = System.currentTimeMillis();

    // Feature-wise tracking
    private static Map<String, Integer> featurePassMap = new LinkedHashMap<>();
    private static Map<String, Integer> featureFailMap = new LinkedHashMap<>();

    // Maximum number of run folders to keep
    private static final int MAX_REPORTS_TO_KEEP = 1;

    // ============================================================
    // GET INSTANCE
    // ============================================================

    public static ExtentReports getInstance() {

        if (extent == null) {

            deleteOldReports();

            startTime = System.currentTimeMillis();

            String timestamp =
                    new SimpleDateFormat("yyyyMMdd_HHmmss")
                            .format(new Date());

            runFolder = System.getProperty("user.dir")
                    + "/test-output/Run_" + timestamp;

            new File(runFolder + "/details").mkdirs();

            ExtentSparkReporter spark =
                    new ExtentSparkReporter(runFolder + "/details/ExtentReport.html");

            spark.config().setReportName("PathFinder UI Automation Report");
            spark.config().setDocumentTitle("PathFinder Execution Report");
            spark.config().setTheme(Theme.DARK);
            spark.config().setTimeStampFormat("dd MMM yyyy hh:mm:ss a");

            extent = new ExtentReports();
            extent.attachReporter(spark);

            extent.setSystemInfo("Project",     "PathFinder UI Automation");
            extent.setSystemInfo("Tester",      "Parwez Sheriff");
            extent.setSystemInfo("Environment", "Pre-Production");
            extent.setSystemInfo("Browser",     "Chrome");
            extent.setSystemInfo("Run Date",    new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(new Date()));
        }

        return extent;
    }

    // ============================================================
    // TRACK TEST RESULTS
    // ============================================================

    public static void trackResult(String rawFeatureUri, boolean isPassed) {

        String featureName = cleanFeatureName(rawFeatureUri);

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
    // ADD ENTERPRISE DASHBOARD
    // ============================================================

    public static void addDashboard() {

        if (extent == null) return;

        long durationMs   = System.currentTimeMillis() - startTime;
        long minutes      = durationMs / 60000;
        long seconds      = (durationMs % 60000) / 1000;
        String execTime   = minutes + "m " + seconds + "s";

        double passPercent = totalTests == 0 ? 0 :
                ((double) passedTests / totalTests) * 100;
        double failPercent = 100 - passPercent;

        // Collect all feature names (pass + fail)
        Set<String> allFeatures = new LinkedHashSet<>();
        allFeatures.addAll(featurePassMap.keySet());
        allFeatures.addAll(featureFailMap.keySet());

        StringBuilder featureRows = new StringBuilder();

        for (String feature : allFeatures) {

            int pass  = featurePassMap.getOrDefault(feature, 0);
            int fail  = featureFailMap.getOrDefault(feature, 0);
            int total = pass + fail;

            double fp = total == 0 ? 0 : ((double) pass / total) * 100;
            double ff = 100 - fp;

            String statusColor  = fail == 0 ? "#00ff7f" : "#ff4c4c";
            String statusLabel  = fail == 0 ? "PASSED" : "FAILED";

            featureRows.append(
                "<tr>" +
                "<td>" + feature + "</td>" +
                "<td><b style='color:#00c87a'>" + pass + "</b></td>" +
                "<td><b style='color:#ff4c4c'>" + fail + "</b></td>" +
                "<td>" + total + "</td>" +
                "<td><div class='mini-bar'>" +
                    "<div class='mini-bar-pass' style='width:" + String.format("%.1f", fp) + "%'></div>" +
                    "<div class='mini-bar-fail' style='width:" + String.format("%.1f", ff) + "%'></div>" +
                "</div></td>" +
                "<td><span class='badge " + (fail == 0 ? "badge-pass" : "badge-fail") + "'>" + statusLabel + "</span></td>" +
                "</tr>"
            );
        }

        String dashboardHtml =
            "<div style='font-family:Arial,sans-serif;padding:20px;background:#1a1a2e;border-radius:12px;color:#eee'>" +

            "<h2 style='color:#00c8ff;margin-bottom:5px'>PathFinder Automation — Execution Summary</h2>" +
            "<p style='color:#888;margin-top:0'>Execution Time: <b style='color:#fff'>" + execTime + "</b></p>" +

            // Summary cards
            "<div style='display:flex;gap:15px;margin:20px 0'>" +

            "<div style='flex:1;background:#16213e;padding:15px;border-radius:10px;text-align:center;" +
            "border-left:4px solid #00c8ff'>" +
            "<div style='font-size:28px;font-weight:bold;color:#00c8ff'>" + totalTests + "</div>" +
            "<div style='color:#aaa;font-size:13px'>Total Tests</div></div>" +

            "<div style='flex:1;background:#16213e;padding:15px;border-radius:10px;text-align:center;" +
            "border-left:4px solid #00ff7f'>" +
            "<div style='font-size:28px;font-weight:bold;color:#00ff7f'>" + passedTests + "</div>" +
            "<div style='color:#aaa;font-size:13px'>Passed</div></div>" +

            "<div style='flex:1;background:#16213e;padding:15px;border-radius:10px;text-align:center;" +
            "border-left:4px solid #ff4c4c'>" +
            "<div style='font-size:28px;font-weight:bold;color:#ff4c4c'>" + failedTests + "</div>" +
            "<div style='color:#aaa;font-size:13px'>Failed</div></div>" +

            "<div style='flex:1;background:#16213e;padding:15px;border-radius:10px;text-align:center;" +
            "border-left:4px solid #f0a500'>" +
            "<div style='font-size:28px;font-weight:bold;color:#f0a500'>" + String.format("%.1f", passPercent) + "%</div>" +
            "<div style='color:#aaa;font-size:13px'>Pass Rate</div></div>" +

            "</div>" +

            // Overall progress bar
            "<div style='margin-bottom:20px'>" +
            "<div style='color:#aaa;font-size:12px;margin-bottom:5px'>Overall Pass / Fail</div>" +
            "<div style='width:100%;background:#333;height:20px;border-radius:10px;overflow:hidden'>" +
            "<div style='width:" + String.format("%.1f", passPercent) + "%;background:#00ff7f;height:20px;float:left'></div>" +
            "<div style='width:" + String.format("%.1f", failPercent) + "%;background:#ff4c4c;height:20px;float:left'></div>" +
            "</div></div>" +

            // Feature table
            "<h3 style='color:#00c8ff'>Feature-wise Results</h3>" +
            "<table style='width:100%;border-collapse:collapse;background:#16213e;border-radius:8px;overflow:hidden'>" +
            "<thead><tr style='background:#0f3460;color:#00c8ff;font-size:13px'>" +
            "<th style='padding:10px;text-align:left'>Feature</th>" +
            "<th style='padding:10px'>Passed</th>" +
            "<th style='padding:10px'>Failed</th>" +
            "<th style='padding:10px'>Total</th>" +
            "<th style='padding:10px'>Progress</th>" +
            "<th style='padding:10px'>Status</th>" +
            "</tr></thead>" +
            "<tbody>" + featureRows + "</tbody>" +
            "</table>" +

            "</div>";

        extent.createTest("📊 Execution Dashboard")
                .info(dashboardHtml);

        generateStandaloneReport(execTime, passPercent, failPercent, featureRows.toString());
    }

    // ============================================================
    // GENERATE BEAUTIFUL STANDALONE index.html
    // ============================================================

    private static void generateStandaloneReport(String execTime, double passPercent,
                                                  double failPercent, String featureRows) {
        String runDate      = new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(new Date());
        String overallStatus = failedTests == 0 ? "ALL TESTS PASSED" : "SOME TESTS FAILED";
        String overallColor  = failedTests == 0 ? "#00c87a" : "#ff4c4c";
        String overallBg     = failedTests == 0 ? "#0a2e1a" : "#2a0a0a";
        String overallBorder = failedTests == 0 ? "#00c87a" : "#ff4c4c";

        String html =
            "<!DOCTYPE html><html lang='en'><head>" +
            "<meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'>" +
            "<title>PathFinder Automation Report</title>" +
            "<style>" +
            "*{box-sizing:border-box;margin:0;padding:0}" +
            "body{font-family:'Segoe UI',Arial,sans-serif;background:#0d0d1a;color:#e0e0e0;min-height:100vh}" +
            ".header{background:linear-gradient(135deg,#003057 0%,#001a33 100%);padding:30px 40px;display:flex;justify-content:space-between;align-items:center;border-bottom:3px solid #00c8ff}" +
            ".header .title{color:#fff;font-size:22px;font-weight:700;letter-spacing:1px}" +
            ".header .subtitle{color:#a0c4ff;font-size:13px;margin-top:4px}" +
            ".header .run-info{text-align:right;color:#a0c4ff;font-size:12px;line-height:1.8}" +
            ".header .run-info b{color:#fff}" +
            ".banner{margin:28px 40px 0;padding:16px 24px;border-radius:10px;background:" + overallBg + ";border-left:5px solid " + overallBorder + ";display:flex;align-items:center;gap:12px}" +
            ".dot{width:14px;height:14px;border-radius:50%;background:" + overallColor + ";flex-shrink:0}" +
            ".banner-label{font-size:16px;font-weight:700;color:" + overallColor + "}" +
            ".banner-sub{font-size:12px;color:#888;margin-top:2px}" +
            ".cards{display:flex;gap:16px;margin:24px 40px}" +
            ".card{flex:1;background:#16213e;border-radius:12px;padding:20px;text-align:center}" +
            ".card .num{font-size:36px;font-weight:800}" +
            ".card .lbl{font-size:12px;color:#888;margin-top:4px;text-transform:uppercase;letter-spacing:1px}" +
            ".section{margin:0 40px 28px}" +
            ".section-title{font-size:13px;font-weight:700;color:#00c8ff;text-transform:uppercase;letter-spacing:1px;margin-bottom:12px;padding-bottom:6px;border-bottom:1px solid #1e2a45}" +
            ".prog{background:#1e1e2e;border-radius:8px;overflow:hidden;height:22px;display:flex}" +
            ".prog-p{background:linear-gradient(90deg,#00c87a,#00ff7f);height:22px;display:flex;align-items:center;justify-content:center;font-size:11px;font-weight:700;color:#000}" +
            ".prog-f{background:linear-gradient(90deg,#cc0000,#ff4c4c);height:22px;display:flex;align-items:center;justify-content:center;font-size:11px;font-weight:700;color:#fff}" +
            "table{width:100%;border-collapse:collapse;background:#16213e;border-radius:10px;overflow:hidden}" +
            "thead tr{background:#0f3460}" +
            "thead th{padding:12px 16px;color:#00c8ff;font-size:12px;text-transform:uppercase;letter-spacing:1px;text-align:left}" +
            "thead th:not(:first-child){text-align:center}" +
            "tbody tr:nth-child(even){background:#131c35}" +
            "tbody td{padding:11px 16px;font-size:13px;color:#ddd}" +
            "tbody td:not(:first-child){text-align:center}" +
            ".badge{display:inline-block;padding:3px 12px;border-radius:12px;font-size:11px;font-weight:700}" +
            ".bp{background:#00c87a;color:#000}" +
            ".bf{background:#ff4c4c;color:#fff}" +
            ".mini{width:100%;background:#333;border-radius:6px;overflow:hidden;height:10px;display:flex}" +
            ".mini-p{background:#00c87a;height:10px}" +
            ".mini-f{background:#ff4c4c;height:10px}" +
            ".btn-wrap{text-align:center;margin:10px 40px 40px}" +
            ".btn{display:inline-block;padding:14px 36px;background:linear-gradient(135deg,#003057,#0066cc);color:#fff;text-decoration:none;border-radius:8px;font-size:14px;font-weight:700;letter-spacing:0.5px}" +
            ".footer{text-align:center;padding:20px;color:#444;font-size:11px;border-top:1px solid #1e2a45}" +
            "</style></head><body>" +

            "<div class='header'>" +
            "<div><div class='title'>PathFinder UI Automation</div><div class='subtitle'>Automated Test Execution Report</div></div>" +
            "<div class='run-info'><div>Run Date: <b>" + runDate + "</b></div><div>Execution Time: <b>" + execTime + "</b></div><div>Environment: <b>Pre-Production</b></div></div>" +
            "</div>" +

            "<div class='banner'><div class='dot'></div>" +
            "<div><div class='banner-label'>" + overallStatus + "</div>" +
            "<div class='banner-sub'>" + totalTests + " scenarios executed &bull; " + passedTests + " passed &bull; " + failedTests + " failed</div></div></div>" +

            "<div class='cards'>" +
            "<div class='card' style='border-top:3px solid #00c8ff'><div class='num' style='color:#00c8ff'>" + totalTests + "</div><div class='lbl'>Total Tests</div></div>" +
            "<div class='card' style='border-top:3px solid #00c87a'><div class='num' style='color:#00c87a'>" + passedTests + "</div><div class='lbl'>Passed</div></div>" +
            "<div class='card' style='border-top:3px solid #ff4c4c'><div class='num' style='color:#ff4c4c'>" + failedTests + "</div><div class='lbl'>Failed</div></div>" +
            "<div class='card' style='border-top:3px solid #f0a500'><div class='num' style='color:#f0a500'>" + String.format("%.1f", passPercent) + "%</div><div class='lbl'>Pass Rate</div></div>" +
            "</div>" +

            "<div class='section'><div class='section-title'>Overall Pass / Fail</div>" +
            "<div class='prog'>" +
            "<div class='prog-p' style='width:" + String.format("%.1f", passPercent) + "%'>" + (passPercent > 8 ? String.format("%.1f", passPercent) + "%" : "") + "</div>" +
            "<div class='prog-f' style='width:" + String.format("%.1f", failPercent) + "%'>" + (failPercent > 8 ? String.format("%.1f", failPercent) + "%" : "") + "</div>" +
            "</div></div>" +

            "<div class='section'><div class='section-title'>Feature-wise Results</div>" +
            "<table><thead><tr><th>Feature</th><th>Passed</th><th>Failed</th><th>Total</th><th style='min-width:120px'>Progress</th><th>Status</th></tr></thead>" +
            "<tbody>" + featureRows + "</tbody></table></div>" +

            "<div class='btn-wrap'><a href='details/ExtentReport.html' class='btn'>View Detailed Test Logs &rarr;</a></div>" +

            "<div class='footer'>Generated by PathFinder UI Automation &bull; GitHub Actions CI/CD</div>" +
            "</body></html>";

        try (PrintWriter pw = new PrintWriter(new FileWriter(runFolder + "/index.html"))) {
            pw.print(html);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // CLEAN FEATURE NAME FROM URI
    // ============================================================

    private static String cleanFeatureName(String uri) {
        if (uri == null || uri.isEmpty()) return "Unknown Feature";
        // Extract filename from path e.g. "classpath:features/BusinessIdentifierBooking.feature" -> "BusinessIdentifier Booking"
        String name = uri.replaceAll(".*[/\\\\]", "").replace(".feature", "");
        // Insert space before each uppercase letter group for readability
        name = name.replaceAll("([A-Z])", " $1").trim();
        return name;
    }

    // ============================================================
    // KEEP LAST 5 REPORTS — DELETE OLDER ONES
    // ============================================================

    private static void deleteOldReports() {

        File testOutputDir = Paths.get(System.getProperty("user.dir"), "test-output").toFile();

        if (!testOutputDir.exists()) return;

        File[] runs = testOutputDir.listFiles(
                f -> f.isDirectory() && f.getName().startsWith("Run_"));

        if (runs == null || runs.length == 0) return;

        // Sort oldest first
        Arrays.sort(runs, Comparator.comparing(File::getName));

        // Delete ALL existing runs — new run will be created fresh
        for (File run : runs) {
            deleteFolder(run);
        }
    }

    private static void deleteFolder(File folder) {
        try {
            Files.walk(folder.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}