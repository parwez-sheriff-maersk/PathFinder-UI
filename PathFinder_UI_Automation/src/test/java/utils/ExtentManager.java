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

            // Create run folder and details subfolder
            new File(runFolder + "/details").mkdirs();

            // ExtentReport goes into details/ subfolder (hidden from user)
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
    // ADD ENTERPRISE DASHBOARD + GENERATE INDEX.HTML
    // ============================================================

    public static void addDashboard() {

        if (extent == null) return;

        long durationMs   = System.currentTimeMillis() - startTime;
        long minutes      = durationMs / 60000;
        long seconds      = (durationMs % 60000) / 1000;
        String execTime   = minutes + "m " + seconds + "s";
        String runDate    = new SimpleDateFormat("dd MMM yyyy, hh:mm:ss a").format(new Date());

        double passPercent = totalTests == 0 ? 0 :
                ((double) passedTests / totalTests) * 100;
        double failPercent = 100 - passPercent;

        // Collect all feature names (pass + fail)
        Set<String> allFeatures = new LinkedHashSet<>();
        allFeatures.addAll(featurePassMap.keySet());
        allFeatures.addAll(featureFailMap.keySet());

        // Build feature rows for the table
        StringBuilder featureRows = new StringBuilder();

        for (String feature : allFeatures) {

            int pass  = featurePassMap.getOrDefault(feature, 0);
            int fail  = featureFailMap.getOrDefault(feature, 0);
            int total = pass + fail;

            double fp = total == 0 ? 0 : ((double) pass / total) * 100;
            double ff = 100 - fp;

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

        // Generate the beautiful standalone index.html
        generateIndexReport(runFolder, execTime, runDate,
                passPercent, failPercent, featureRows.toString());
    }

    // ============================================================
    // GENERATE BEAUTIFUL INDEX.HTML (FRONT PAGE)
    // ============================================================

    private static void generateIndexReport(String runFolder,
                                            String execTime,
                                            String runDate,
                                            double passPercent,
                                            double failPercent,
                                            String featureRowsHtml) {

        String overallStatus = failedTests == 0 ? "ALL PASSED" : "HAS FAILURES";
        String statusColor   = failedTests == 0 ? "#00ff7f" : "#ff4c4c";
        String statusGlow    = failedTests == 0
                ? "0 0 20px rgba(0,255,127,0.4)" : "0 0 20px rgba(255,76,76,0.4)";

        String html = "<!DOCTYPE html>\n" +
            "<html lang='en'>\n" +
            "<head>\n" +
            "  <meta charset='UTF-8'>\n" +
            "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
            "  <title>PathFinder Automation Dashboard</title>\n" +
            "  <style>\n" +
            "    * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
            "    body {\n" +
            "      font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
            "      background: linear-gradient(135deg, #0a0a1a 0%, #1a1a3e 50%, #0a0a2a 100%);\n" +
            "      color: #e0e0e0; min-height: 100vh; padding: 0;\n" +
            "    }\n" +
            "\n" +
            "    /* ---- Header ---- */\n" +
            "    .header {\n" +
            "      background: linear-gradient(90deg, #0f3460 0%, #16213e 100%);\n" +
            "      padding: 30px 50px; display: flex; align-items: center;\n" +
            "      justify-content: space-between; border-bottom: 3px solid #00c8ff;\n" +
            "      box-shadow: 0 4px 30px rgba(0,200,255,0.15);\n" +
            "    }\n" +
            "    .header-left h1 {\n" +
            "      font-size: 28px; color: #00c8ff;\n" +
            "      text-shadow: 0 0 15px rgba(0,200,255,0.5);\n" +
            "    }\n" +
            "    .header-left p { color: #8892b0; font-size: 14px; margin-top: 4px; }\n" +
            "    .header-right { text-align: right; }\n" +
            "    .header-right .status-badge {\n" +
            "      display: inline-block; padding: 8px 24px; border-radius: 25px;\n" +
            "      font-weight: bold; font-size: 14px; letter-spacing: 1px;\n" +
            "      color: " + statusColor + ";\n" +
            "      border: 2px solid " + statusColor + ";\n" +
            "      box-shadow: " + statusGlow + ";\n" +
            "    }\n" +
            "    .header-right .run-date { color: #8892b0; font-size: 12px; margin-top: 8px; }\n" +
            "\n" +
            "    /* ---- Container ---- */\n" +
            "    .container { max-width: 1200px; margin: 0 auto; padding: 40px 30px; }\n" +
            "\n" +
            "    /* ---- Summary Cards ---- */\n" +
            "    .cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 40px; }\n" +
            "    .card {\n" +
            "      background: linear-gradient(145deg, #16213e, #1a1a4e);\n" +
            "      border-radius: 16px; padding: 25px; text-align: center;\n" +
            "      border: 1px solid rgba(0,200,255,0.1);\n" +
            "      box-shadow: 0 8px 32px rgba(0,0,0,0.3);\n" +
            "      transition: transform 0.3s, box-shadow 0.3s;\n" +
            "    }\n" +
            "    .card:hover { transform: translateY(-5px); box-shadow: 0 12px 40px rgba(0,200,255,0.2); }\n" +
            "    .card .number { font-size: 42px; font-weight: bold; margin-bottom: 5px; }\n" +
            "    .card .label { font-size: 13px; color: #8892b0; text-transform: uppercase; letter-spacing: 1px; }\n" +
            "    .card-total .number { color: #00c8ff; }\n" +
            "    .card-pass .number { color: #00ff7f; }\n" +
            "    .card-fail .number { color: #ff4c4c; }\n" +
            "    .card-rate .number { color: #f0a500; }\n" +
            "    .card-total { border-left: 4px solid #00c8ff; }\n" +
            "    .card-pass  { border-left: 4px solid #00ff7f; }\n" +
            "    .card-fail  { border-left: 4px solid #ff4c4c; }\n" +
            "    .card-rate  { border-left: 4px solid #f0a500; }\n" +
            "\n" +
            "    /* ---- Donut Chart ---- */\n" +
            "    .chart-section {\n" +
            "      display: flex; align-items: center; justify-content: center;\n" +
            "      gap: 50px; margin-bottom: 40px;\n" +
            "    }\n" +
            "    .donut-container { position: relative; width: 200px; height: 200px; }\n" +
            "    .donut-center {\n" +
            "      position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);\n" +
            "      text-align: center;\n" +
            "    }\n" +
            "    .donut-center .pct { font-size: 36px; font-weight: bold; color: #fff; }\n" +
            "    .donut-center .pct-label { font-size: 12px; color: #8892b0; }\n" +
            "    .chart-legend { display: flex; flex-direction: column; gap: 12px; }\n" +
            "    .legend-item { display: flex; align-items: center; gap: 10px; font-size: 14px; }\n" +
            "    .legend-dot {\n" +
            "      width: 14px; height: 14px; border-radius: 50%; display: inline-block;\n" +
            "    }\n" +
            "\n" +
            "    /* ---- Progress Bar ---- */\n" +
            "    .progress-section { margin-bottom: 40px; }\n" +
            "    .progress-label { color: #8892b0; font-size: 13px; margin-bottom: 8px; }\n" +
            "    .progress-bar {\n" +
            "      width: 100%; height: 24px; background: #1a1a3e;\n" +
            "      border-radius: 12px; overflow: hidden;\n" +
            "      box-shadow: inset 0 2px 8px rgba(0,0,0,0.4);\n" +
            "    }\n" +
            "    .progress-fill-pass {\n" +
            "      height: 100%; float: left;\n" +
            "      background: linear-gradient(90deg, #00ff7f, #00c87a);\n" +
            "      border-radius: 12px 0 0 12px;\n" +
            "      transition: width 1s ease;\n" +
            "    }\n" +
            "    .progress-fill-fail {\n" +
            "      height: 100%; float: left;\n" +
            "      background: linear-gradient(90deg, #ff4c4c, #cc3333);\n" +
            "      border-radius: 0 12px 12px 0;\n" +
            "      transition: width 1s ease;\n" +
            "    }\n" +
            "\n" +
            "    /* ---- Feature Table ---- */\n" +
            "    .section-title {\n" +
            "      font-size: 20px; color: #00c8ff; margin-bottom: 15px;\n" +
            "      padding-bottom: 8px; border-bottom: 2px solid rgba(0,200,255,0.2);\n" +
            "    }\n" +
            "    .feature-table {\n" +
            "      width: 100%; border-collapse: collapse;\n" +
            "      background: linear-gradient(145deg, #16213e, #1a1a4e);\n" +
            "      border-radius: 12px; overflow: hidden;\n" +
            "      box-shadow: 0 8px 32px rgba(0,0,0,0.3);\n" +
            "    }\n" +
            "    .feature-table thead tr {\n" +
            "      background: linear-gradient(90deg, #0f3460, #16213e);\n" +
            "    }\n" +
            "    .feature-table th {\n" +
            "      padding: 14px 16px; text-align: left; color: #00c8ff;\n" +
            "      font-size: 13px; text-transform: uppercase; letter-spacing: 0.5px;\n" +
            "    }\n" +
            "    .feature-table td {\n" +
            "      padding: 12px 16px; border-top: 1px solid rgba(255,255,255,0.05);\n" +
            "      font-size: 14px;\n" +
            "    }\n" +
            "    .feature-table tr:hover { background: rgba(0,200,255,0.05); }\n" +
            "\n" +
            "    /* ---- Mini Progress Bar in Table ---- */\n" +
            "    .mini-bar {\n" +
            "      width: 100%; height: 8px; background: #333;\n" +
            "      border-radius: 4px; overflow: hidden; display: flex;\n" +
            "    }\n" +
            "    .mini-bar-pass { background: #00ff7f; height: 100%; }\n" +
            "    .mini-bar-fail { background: #ff4c4c; height: 100%; }\n" +
            "\n" +
            "    /* ---- Badges ---- */\n" +
            "    .badge {\n" +
            "      padding: 4px 14px; border-radius: 20px; font-size: 11px;\n" +
            "      font-weight: bold; letter-spacing: 0.5px;\n" +
            "    }\n" +
            "    .badge-pass { background: rgba(0,255,127,0.15); color: #00ff7f; border: 1px solid rgba(0,255,127,0.3); }\n" +
            "    .badge-fail { background: rgba(255,76,76,0.15); color: #ff4c4c; border: 1px solid rgba(255,76,76,0.3); }\n" +
            "\n" +
            "    /* ---- CTA Button ---- */\n" +
            "    .cta-section { text-align: center; margin-top: 50px; }\n" +
            "    .cta-btn {\n" +
            "      display: inline-block; padding: 16px 48px;\n" +
            "      background: linear-gradient(90deg, #00c8ff, #0088cc);\n" +
            "      color: #fff; text-decoration: none; font-size: 16px;\n" +
            "      font-weight: bold; border-radius: 30px; letter-spacing: 0.5px;\n" +
            "      box-shadow: 0 8px 30px rgba(0,200,255,0.3);\n" +
            "      transition: transform 0.3s, box-shadow 0.3s;\n" +
            "    }\n" +
            "    .cta-btn:hover {\n" +
            "      transform: translateY(-3px);\n" +
            "      box-shadow: 0 12px 40px rgba(0,200,255,0.5);\n" +
            "    }\n" +
            "\n" +
            "    /* ---- Footer ---- */\n" +
            "    .footer {\n" +
            "      text-align: center; padding: 30px; color: #555;\n" +
            "      font-size: 12px; border-top: 1px solid rgba(255,255,255,0.05);\n" +
            "      margin-top: 50px;\n" +
            "    }\n" +
            "\n" +
            "    /* ---- Exec Info ---- */\n" +
            "    .exec-info {\n" +
            "      display: flex; justify-content: center; gap: 40px;\n" +
            "      margin-bottom: 40px; color: #8892b0; font-size: 13px;\n" +
            "    }\n" +
            "    .exec-info span b { color: #fff; }\n" +
            "  </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "\n" +
            "  <!-- Header -->\n" +
            "  <div class='header'>\n" +
            "    <div class='header-left'>\n" +
            "      <h1>PathFinder Automation Dashboard</h1>\n" +
            "      <p>UI Automation Test Execution Report</p>\n" +
            "    </div>\n" +
            "    <div class='header-right'>\n" +
            "      <div class='status-badge'>" + overallStatus + "</div>\n" +
            "      <div class='run-date'>" + runDate + "</div>\n" +
            "    </div>\n" +
            "  </div>\n" +
            "\n" +
            "  <div class='container'>\n" +
            "\n" +
            "    <!-- Execution Info -->\n" +
            "    <div class='exec-info'>\n" +
            "      <span>Environment: <b>Pre-Production</b></span>\n" +
            "      <span>Browser: <b>Chrome</b></span>\n" +
            "      <span>Execution Time: <b>" + execTime + "</b></span>\n" +
            "      <span>Tester: <b>Parwez Sheriff</b></span>\n" +
            "    </div>\n" +
            "\n" +
            "    <!-- Summary Cards -->\n" +
            "    <div class='cards'>\n" +
            "      <div class='card card-total'>\n" +
            "        <div class='number'>" + totalTests + "</div>\n" +
            "        <div class='label'>Total Tests</div>\n" +
            "      </div>\n" +
            "      <div class='card card-pass'>\n" +
            "        <div class='number'>" + passedTests + "</div>\n" +
            "        <div class='label'>Passed</div>\n" +
            "      </div>\n" +
            "      <div class='card card-fail'>\n" +
            "        <div class='number'>" + failedTests + "</div>\n" +
            "        <div class='label'>Failed</div>\n" +
            "      </div>\n" +
            "      <div class='card card-rate'>\n" +
            "        <div class='number'>" + String.format("%.1f", passPercent) + "%</div>\n" +
            "        <div class='label'>Pass Rate</div>\n" +
            "      </div>\n" +
            "    </div>\n" +
            "\n" +
            "    <!-- Donut Chart + Legend -->\n" +
            "    <div class='chart-section'>\n" +
            "      <div class='donut-container'>\n" +
            "        <svg viewBox='0 0 36 36' style='width:200px;height:200px;transform:rotate(-90deg)'>\n" +
            "          <circle cx='18' cy='18' r='15.9' fill='none' stroke='#1a1a3e' stroke-width='3.5'/>\n" +
            "          <circle cx='18' cy='18' r='15.9' fill='none' stroke='#00ff7f' stroke-width='3.5'\n" +
            "                  stroke-dasharray='" + String.format("%.1f", passPercent) + " " + String.format("%.1f", failPercent) + "'\n" +
            "                  stroke-linecap='round'/>\n" +
            "          <circle cx='18' cy='18' r='15.9' fill='none' stroke='#ff4c4c' stroke-width='3.5'\n" +
            "                  stroke-dasharray='" + String.format("%.1f", failPercent) + " " + String.format("%.1f", passPercent) + "'\n" +
            "                  stroke-dashoffset='-" + String.format("%.1f", passPercent) + "'\n" +
            "                  stroke-linecap='round'/>\n" +
            "        </svg>\n" +
            "        <div class='donut-center'>\n" +
            "          <div class='pct'>" + String.format("%.0f", passPercent) + "%</div>\n" +
            "          <div class='pct-label'>Pass Rate</div>\n" +
            "        </div>\n" +
            "      </div>\n" +
            "      <div class='chart-legend'>\n" +
            "        <div class='legend-item'>\n" +
            "          <span class='legend-dot' style='background:#00ff7f'></span>\n" +
            "          Passed: <b style='color:#00ff7f'>" + passedTests + "</b>\n" +
            "        </div>\n" +
            "        <div class='legend-item'>\n" +
            "          <span class='legend-dot' style='background:#ff4c4c'></span>\n" +
            "          Failed: <b style='color:#ff4c4c'>" + failedTests + "</b>\n" +
            "        </div>\n" +
            "        <div class='legend-item'>\n" +
            "          <span class='legend-dot' style='background:#00c8ff'></span>\n" +
            "          Total: <b style='color:#00c8ff'>" + totalTests + "</b>\n" +
            "        </div>\n" +
            "      </div>\n" +
            "    </div>\n" +
            "\n" +
            "    <!-- Overall Progress Bar -->\n" +
            "    <div class='progress-section'>\n" +
            "      <div class='progress-label'>Overall Pass / Fail Ratio</div>\n" +
            "      <div class='progress-bar'>\n" +
            "        <div class='progress-fill-pass' style='width:" + String.format("%.1f", passPercent) + "%'></div>\n" +
            "        <div class='progress-fill-fail' style='width:" + String.format("%.1f", failPercent) + "%'></div>\n" +
            "      </div>\n" +
            "    </div>\n" +
            "\n" +
            "    <!-- Feature-wise Table -->\n" +
            "    <h2 class='section-title'>Feature-wise Results</h2>\n" +
            "    <table class='feature-table'>\n" +
            "      <thead>\n" +
            "        <tr>\n" +
            "          <th>Feature</th>\n" +
            "          <th>Passed</th>\n" +
            "          <th>Failed</th>\n" +
            "          <th>Total</th>\n" +
            "          <th>Progress</th>\n" +
            "          <th>Status</th>\n" +
            "        </tr>\n" +
            "      </thead>\n" +
            "      <tbody>\n" +
            "        " + featureRowsHtml + "\n" +
            "      </tbody>\n" +
            "    </table>\n" +
            "\n" +
            "    <!-- CTA Button -->\n" +
            "    <div class='cta-section'>\n" +
            "      <a href='details/ExtentReport.html' class='cta-btn'>\n" +
            "        View Detailed Test Logs\n" +
            "      </a>\n" +
            "    </div>\n" +
            "\n" +
            "    <!-- Footer -->\n" +
            "    <div class='footer'>\n" +
            "      PathFinder UI Automation | Maersk Technology | Generated automatically\n" +
            "    </div>\n" +
            "\n" +
            "  </div>\n" +
            "\n" +
            "</body>\n" +
            "</html>";

        // Write index.html to run folder root
        try (PrintWriter pw = new PrintWriter(
                new FileWriter(runFolder + "/index.html"))) {
            pw.print(html);
            System.out.println("✅ Dashboard generated: " + runFolder + "/index.html");
        } catch (Exception e) {
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
