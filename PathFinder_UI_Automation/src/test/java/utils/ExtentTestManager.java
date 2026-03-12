package utils;

import com.aventstack.extentreports.ExtentTest;

public class ExtentTestManager {

    private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    private static ThreadLocal<Boolean> screenshotTaken = new ThreadLocal<>();

    public static ExtentTest getTest() {
        return extentTest.get();
    }

    public static void setTest(ExtentTest test) {
        extentTest.set(test);
        screenshotTaken.set(false); // reset for each test
    }

    public static boolean isScreenshotTaken() {
        Boolean taken = screenshotTaken.get();
        return taken != null && taken;
    }

    public static void markScreenshotTaken() {
        screenshotTaken.set(true);
    }
}