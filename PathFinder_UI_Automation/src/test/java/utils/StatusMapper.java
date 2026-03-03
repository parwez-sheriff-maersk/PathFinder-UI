package utils;

public class StatusMapper {

    public static String mapDbToUiStatus(String dbStatus, String originSystem) {

        if (dbStatus == null) return "";

        dbStatus = dbStatus.trim().toUpperCase();
        originSystem = originSystem.trim().toUpperCase();

        // ==========================
        // AMPS MAPPING
        // ==========================
        if (originSystem.equals("AMPS")) {

            switch (dbStatus) {

                case "CREATED":
                case "COMPLETED":
                case "RUNNING":
                    return "SUCCESS";

                case "FAILED":
                    return "ERROR";

                default:
                    return dbStatus;
            }
        }

        // ==========================
        // SEEBURGER MAPPING
        // ==========================
        if (originSystem.equals("SEEBURGER")) {

            switch (dbStatus) {

                case "COMPLETED":
                case "SUCCESS":
                case "RUNNING":
                    return "SUCCESS";

                case "ERROR":
                    return "ERROR";

                case "TERMINATED":
                    return "TERMINATED";

                default:
                    return dbStatus;
            }
        }

        return dbStatus;
    }
}