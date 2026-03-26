package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class DatabaseUtils {

    private static final Logger logger = Logger.getLogger(DatabaseUtils.class.getName());

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
    // LATEST PLATFORM IDENTIFIERS (AMPS + SEEBURGER)
    // ============================================================

    public static List<PlatformRecord> getLatestPlatformIdentifiers(Properties prop) {

        List<PlatformRecord> records = new ArrayList<>();

        // Fetch more rows, then dedup by platform_identifier keeping latest (id DESC)
        String ampsQuery =
                "SELECT platform_identifier, status, origin_system " +
                "FROM path_finder_log " +
                "WHERE platform_identifier IS NOT NULL " +
                "AND TRIM(platform_identifier) != '' " +
                "AND origin_system = 'AMPS' " +
                "ORDER BY log_created_time DESC, id DESC " +
                "LIMIT 20";

        String seeburgerQuery =
                "SELECT platform_identifier, status, origin_system " +
                "FROM path_finder_log " +
                "WHERE platform_identifier IS NOT NULL " +
                "AND TRIM(platform_identifier) != '' " +
                "AND origin_system = 'SEEBURGER' " +
                "ORDER BY log_created_time DESC, id DESC " +
                "LIMIT 20";

        try (Connection con = getConnection(prop)) {

            // -------- AMPS --------
            try (PreparedStatement ampsStmt = con.prepareStatement(ampsQuery);
                 ResultSet ampsRs = ampsStmt.executeQuery()) {

                List<PlatformRecord> ampsAll = new ArrayList<>();
                while (ampsRs.next()) {
                    ampsAll.add(new PlatformRecord(
                            ampsRs.getString("platform_identifier"),
                            ampsRs.getString("status"),
                            ampsRs.getString("origin_system")
                    ));
                }
                // Dedup: keep only FIRST (latest) per platform_identifier
                Map<String, PlatformRecord> ampsSeen = new LinkedHashMap<>();
                for (PlatformRecord r : ampsAll) {
                    if (!ampsSeen.containsKey(r.getPlatformId())) {
                        ampsSeen.put(r.getPlatformId(), r);
                    }
                }
                int count = 0;
                for (PlatformRecord r : ampsSeen.values()) {
                    if (count++ >= 5) break;
                    records.add(r);
                }
            }

            // -------- SEEBURGER --------
            try (PreparedStatement seeStmt = con.prepareStatement(seeburgerQuery);
                 ResultSet seeRs = seeStmt.executeQuery()) {

                List<PlatformRecord> seeAll = new ArrayList<>();
                while (seeRs.next()) {
                    seeAll.add(new PlatformRecord(
                            seeRs.getString("platform_identifier"),
                            seeRs.getString("status"),
                            seeRs.getString("origin_system")
                    ));
                }
                // Dedup: keep only FIRST (latest) per platform_identifier
                Map<String, PlatformRecord> seeSeen = new LinkedHashMap<>();
                for (PlatformRecord r : seeAll) {
                    if (!seeSeen.containsKey(r.getPlatformId())) {
                        seeSeen.put(r.getPlatformId(), r);
                    }
                }
                int count = 0;
                for (PlatformRecord r : seeSeen.values()) {
                    if (count++ >= 5) break;
                    records.add(r);
                }
            }

            System.out.println("====================================");
            System.out.println("✅ Total Platform Records Fetched (deduped): " + records.size());
            System.out.println("====================================");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ DB Error while fetching Platform Identifiers");
        }

        return records;
    }

    // ============================================================
    // LATEST HOUSE BILL OF LADING (SEEBURGER)
    // ============================================================

    public static List<PlatformRecord> getLatestHouseBillOfLadingSeeburger(Properties prop) {

        List<PlatformRecord> records = new ArrayList<>();

        // Fetch more rows, then dedup by identifier_value keeping latest (id DESC)
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
                "  AND TRIM(bi ->> 'value') != '' " +
                "  AND p.trace_id IS NOT NULL " +
                "  AND TRIM(p.trace_id) != '' " +
                "ORDER BY p.log_created_time DESC, p.id DESC " +
                "LIMIT 20";

        try (Connection con = getConnection(prop);
             PreparedStatement pstmt = con.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            List<PlatformRecord> allRows = new ArrayList<>();
            while (rs.next()) {
                allRows.add(new PlatformRecord(
                        rs.getString("identifier_value"),
                        rs.getString("status"),
                        rs.getString("origin_system"),
                        rs.getString("trace_id")
                ));
            }

            // Dedup: keep only FIRST (latest) per identifier_value
            Map<String, PlatformRecord> seen = new LinkedHashMap<>();
            for (PlatformRecord r : allRows) {
                if (!seen.containsKey(r.getPlatformId())) {
                    seen.put(r.getPlatformId(), r);
                }
            }
            int count = 0;
            for (PlatformRecord r : seen.values()) {
                if (count++ >= 5) break;
                records.add(r);
            }

            System.out.println("====================================");
            System.out.println("✅ SEEBURGER House Bill Records: " + allRows.size()
                    + " rows → " + records.size() + " unique (deduped)");
            System.out.println("====================================");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ DB Error while fetching House Bill record");
        }

        return records;
    }

    // ============================================================
    // LATEST BOOKING NUMBER (AMPS)
    // ============================================================

    public static List<PlatformRecord> getLatestBookingNumberAmps(Properties prop) {

        List<PlatformRecord> records = new ArrayList<>();

        // Fetch more rows, then dedup by identifier_value keeping latest (id DESC)
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
                "  AND TRIM(bi ->> 'value') != '' " +
                "  AND p.trace_id IS NOT NULL " +
                "  AND TRIM(p.trace_id) != '' " +
                "ORDER BY p.log_created_time DESC, p.id DESC " +
                "LIMIT 20";

        try (Connection con = getConnection(prop);
             PreparedStatement pstmt = con.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            List<PlatformRecord> allRows = new ArrayList<>();
            while (rs.next()) {
                allRows.add(new PlatformRecord(
                        rs.getString("identifier_value"),
                        rs.getString("status"),
                        rs.getString("origin_system"),
                        rs.getString("trace_id")
                ));
            }

            // Dedup: keep only FIRST (latest) per identifier_value
            Map<String, PlatformRecord> seen = new LinkedHashMap<>();
            for (PlatformRecord r : allRows) {
                if (!seen.containsKey(r.getPlatformId())) {
                    seen.put(r.getPlatformId(), r);
                }
            }
            int count = 0;
            for (PlatformRecord r : seen.values()) {
                if (count++ >= 5) break;
                records.add(r);
            }

            System.out.println("====================================");
            System.out.println("✅ AMPS BOOKING NUMBER Records: " + allRows.size()
                    + " rows → " + records.size() + " unique (deduped)");
            System.out.println("====================================");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ DB Error while fetching Booking Number records");
        }

        return records;
    }

    // ============================================================
    // ADVANCED SEARCH RECORDS (TRANSACTION ID + PLATFORM ID)
    // ============================================================

    public static List<AdvancedSearchRecord> getAdvancedSearchRecords(Properties prop) {

        List<AdvancedSearchRecord> records = new ArrayList<>();

        // Fetch recent records ordered by log_created_time DESC, id DESC
        // so the FIRST row per (trace_id, platform_identifier) is the LATEST status.
        // This matches the UI "last row decides" pattern.
        String query =
                "SELECT trace_id, platform_identifier, status, origin_system " +
                "FROM path_finder_log " +
                "WHERE trace_id IS NOT NULL " +
                "AND TRIM(trace_id) != '' " +
                "AND platform_identifier IS NOT NULL " +
                "AND TRIM(platform_identifier) != '' " +
                "ORDER BY log_created_time DESC, id DESC " +
                "LIMIT 20";

        try (Connection con = getConnection(prop);
             PreparedStatement pstmt = con.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            List<AdvancedSearchRecord> allRows = new ArrayList<>();
            while (rs.next()) {
                allRows.add(new AdvancedSearchRecord(
                        rs.getString("trace_id"),
                        rs.getString("platform_identifier"),
                        rs.getString("status"),
                        rs.getString("origin_system")
                ));
            }

            // Deduplicate: keep only the FIRST (latest) row per (traceId + platformId)
            Map<String, AdvancedSearchRecord> seen = new LinkedHashMap<>();
            for (AdvancedSearchRecord r : allRows) {
                String key = r.getTransactionId() + "|" + r.getPlatformId();
                if (!seen.containsKey(key)) {
                    seen.put(key, r);
                }
            }
            records.addAll(seen.values());

            // Keep only top 5 unique records
            if (records.size() > 5) {
                records = new ArrayList<>(records.subList(0, 5));
            }

            logger.info("====================================");
            logger.info("✅ Advanced Search Records Fetched: " + allRows.size()
                    + " rows → " + records.size() + " unique (trace_id + platform_id)");
            records.forEach(r -> logger.info("  → " + r));
            logger.info("====================================");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ DB Error while fetching Advanced Search records");
        }

        return records;
    }
}