package utils;

import com.aventstack.extentreports.ExtentTest;

public class DbReportUtil {

    public static void attachDbValidationDetails(String platformName,
                                                 String identifierValue,
                                                 String traceId,
                                                 String dbRawStatus,
                                                 String expectedStatus) {

        ExtentTest test = ExtentTestManager.getTest();

        if (test == null) return;

        String tableHtml =
                "<div style='margin-top:10px'>" +
                "<b>Database Validation Details</b>" +
                "<table border='1' style='border-collapse:collapse; width:60%; margin-top:8px'>" +
                "<tr><td style='padding:6px'><b>Platform</b></td><td style='padding:6px'>" + platformName + "</td></tr>" +
                "<tr><td style='padding:6px'><b>Identifier</b></td><td style='padding:6px'>" + identifierValue + "</td></tr>" +
                "<tr><td style='padding:6px'><b>Trace ID</b></td><td style='padding:6px'>" + traceId + "</td></tr>" +
                "<tr><td style='padding:6px'><b>DB Raw Status</b></td><td style='padding:6px'>" + dbRawStatus + "</td></tr>" +
                "<tr><td style='padding:6px'><b>Expected UI Status</b></td><td style='padding:6px'>" + expectedStatus + "</td></tr>" +
                "</table></div>";

        test.info(tableHtml);
    }
}