package utils;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class ExtentManager {

    private static ExtentReports extent;
    public static String runFolder;

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

            spark.config().setReportName("PathFinder Automation - Enterprise Report");
            spark.config().setDocumentTitle("Automation Execution Report");
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