package utils;

public class StatusMapper {

    public static String mapDbToUiStatus(String dbStatus, String originSystem) {

        if (dbStatus == null) return "";

        dbStatus = dbStatus.trim().toUpperCase();
        originSystem = originSystem.trim().toUpperCase();

        // ==========================
        // COMMON MAPPING (AMPS + SEEBURGER)
        // ==========================
        switch (dbStatus) {

            case "CREATED":
            case "COMPLETED":
                return "SUCCESS";

            case "FAILED":
                return "ERROR";

            case "SUCCESS":
                return "SUCCESS";

            case "ERROR":
                return "ERROR";

            case "TERMINATED":
                return "TERMINATED";

            case "RUNNING":
                return "RUNNING";

            default:
                return dbStatus;
        }
    }
}