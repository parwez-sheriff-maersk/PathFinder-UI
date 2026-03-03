package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DatabaseUtils {

    public static Connection getConnection(Properties prop) throws Exception {

        String url = prop.getProperty("db.url");
        String user = prop.getProperty("db.username");
        String pass = prop.getProperty("db.password");

        return DriverManager.getConnection(url, user, pass);
    }

    public static List<PlatformRecord> getLatestPlatformIdentifiers(Properties prop) {

        List<PlatformRecord> records = new ArrayList<>();

        try (Connection con = getConnection(prop);
             Statement stmt = con.createStatement()) {

            System.out.println("====================================");
            System.out.println("🔥 CONNECTED TO DB SUCCESSFULLY");
            System.out.println("====================================");

            // =========================
            // 5 AMPS
            // =========================
            String ampsQuery =
                    "SELECT platform_identifier, status, origin_system " +
                    "FROM path_finder_log " +
                    "WHERE platform_identifier IS NOT NULL " +
                    "AND origin_system = 'AMPS' " +
                    "ORDER BY log_created_time DESC " +
                    "LIMIT 1";

            ResultSet ampsRs = stmt.executeQuery(ampsQuery);

            while (ampsRs.next()) {

                records.add(new PlatformRecord(
                        ampsRs.getString("platform_identifier"),
                        ampsRs.getString("status"),
                        ampsRs.getString("origin_system")
                ));
            }

            // =========================
            // 5 SEEBURGER
            // =========================
            String seeburgerQuery =
                    "SELECT platform_identifier, status, origin_system " +
                    "FROM path_finder_log " +
                    "WHERE platform_identifier IS NOT NULL " +
                    "AND origin_system = 'SEEBURGER' " +
                   
                    "ORDER BY log_created_time DESC " +
                    "LIMIT 5";

            ResultSet seeRs = stmt.executeQuery(seeburgerQuery);

            while (seeRs.next()) {

                records.add(new PlatformRecord(
                        seeRs.getString("platform_identifier"),
                        seeRs.getString("status"),
                        seeRs.getString("origin_system")
                ));
            }

            System.out.println("====================================");
            System.out.println("✅ Total Records Fetched: " + records.size());
            System.out.println("====================================");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return records;
    }
}