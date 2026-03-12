package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DatabaseUtils {

    // ============================================================
    // DB CONNECTION
    // ============================================================

    public static Connection getConnection(Properties prop) throws Exception {

        String url = prop.getProperty("db.url");
        String user = prop.getProperty("db.username");
        String pass = prop.getProperty("db.password");

        return DriverManager.getConnection(url, user, pass);
    }

    // ============================================================
    // LATEST 5 PLATFORM IDENTIFIERS (AMPS + SEEBURGER)
    // ============================================================

    public static List<PlatformRecord> getLatestPlatformIdentifiers(Properties prop) {

        List<PlatformRecord> records = new ArrayList<>();

        String ampsQuery =
                "SELECT platform_identifier, status, origin_system " +
                "FROM path_finder_log " +
                "WHERE platform_identifier IS NOT NULL " +
                "AND origin_system = 'AMPS' " +
                "ORDER BY log_created_time DESC " +
                "LIMIT 1";

        String seeburgerQuery =
                "SELECT platform_identifier, status, origin_system " +
                "FROM path_finder_log " +
                "WHERE platform_identifier IS NOT NULL " +
                "AND origin_system = 'SEEBURGER' " +
                "ORDER BY log_created_time DESC " +
                "LIMIT 5";

        try (Connection con = getConnection(prop)) {

            // -------- AMPS --------
            try (PreparedStatement ampsStmt = con.prepareStatement(ampsQuery);
                 ResultSet ampsRs = ampsStmt.executeQuery()) {

                while (ampsRs.next()) {
                    records.add(new PlatformRecord(
                            ampsRs.getString("platform_identifier"),
                            ampsRs.getString("status"),
                            ampsRs.getString("origin_system")
                    ));
                }
            }

            // -------- SEEBURGER --------
            try (PreparedStatement seeStmt = con.prepareStatement(seeburgerQuery);
                 ResultSet seeRs = seeStmt.executeQuery()) {

                while (seeRs.next()) {
                    records.add(new PlatformRecord(
                            seeRs.getString("platform_identifier"),
                            seeRs.getString("status"),
                            seeRs.getString("origin_system")
                    ));
                }
            }

            System.out.println("====================================");
            System.out.println("✅ Total Platform Records Fetched: " + records.size());
            System.out.println("====================================");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ DB Error while fetching Platform Identifiers");
        }

        return records;
    }

    // ============================================================
    // LATEST 5 HOUSE BILL OF LADING (SEEBURGER)
    // ============================================================

    public static List<PlatformRecord> getLatestHouseBillOfLadingSeeburger(Properties prop) {

        List<PlatformRecord> records = new ArrayList<>();

        String query =
                "SELECT bi ->> 'value' AS identifier_value, " +
                "       p.origin_system, " +
                "       p.status, " +
                "       p.log_created_time, " +
                "       p.trace_id " +
                "FROM path_finder_log p " +
                "CROSS JOIN LATERAL jsonb_array_elements(p.business_identifiers) AS bi " +
                "WHERE p.origin_system = 'SEEBURGER' " +
                "  AND bi ->> 'name' = 'HOUSE BILL OF LADING' " +
                "ORDER BY p.log_created_time DESC " +
                "LIMIT 5";

        try (Connection con = getConnection(prop);
             PreparedStatement pstmt = con.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {

                records.add(new PlatformRecord(
                        rs.getString("identifier_value"),
                        rs.getString("status"),
                        rs.getString("origin_system"),
                        rs.getString("trace_id")
                ));
            }

            System.out.println("====================================");
            System.out.println("✅ SEEBURGER HOUSE BOL Records: " + records.size());
            System.out.println("====================================");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ DB Error while fetching House Bill records");
        }

        return records;
    }

    // ============================================================
    // LATEST 5 BOOKING NUMBER (AMPS)
    // ============================================================

    public static List<PlatformRecord> getLatestBookingNumberAmps(Properties prop) {

        List<PlatformRecord> records = new ArrayList<>();

        String query =
                "SELECT bi ->> 'value' AS identifier_value, " +
                "       p.origin_system, " +
                "       p.status, " +
                "       p.log_created_time, " +
                "       p.trace_id " +
                "FROM path_finder_log p " +
                "CROSS JOIN LATERAL jsonb_array_elements(p.business_identifiers) AS bi " +
                "WHERE p.origin_system = 'AMPS' " +
                "  AND UPPER(bi ->> 'name') = 'BOOKING NUMBER' " +
                "ORDER BY p.log_created_time DESC " +
                "LIMIT 5";

        try (Connection con = getConnection(prop);
             PreparedStatement pstmt = con.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {

                records.add(new PlatformRecord(
                        rs.getString("identifier_value"),
                        rs.getString("status"),
                        rs.getString("origin_system")
                ));
            }

            System.out.println("====================================");
            System.out.println("✅ AMPS BOOKING NUMBER Records: " + records.size());
            System.out.println("====================================");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ DB Error while fetching Booking Number records");
        }

        return records;
    }
}