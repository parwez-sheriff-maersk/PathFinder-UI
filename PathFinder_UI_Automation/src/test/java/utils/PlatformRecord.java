package utils;

public class PlatformRecord {

    private String platformId;
    private String dbStatus;
    private String originSystem;
    private String traceId;

    // 🔹 Old Constructor (keeps existing code working)
    public PlatformRecord(String platformId, String dbStatus, String originSystem) {
        this.platformId = platformId;
        this.dbStatus = dbStatus;
        this.originSystem = originSystem;
        this.traceId = null; // default
    }

    // 🔹 New Constructor (with traceId)
    public PlatformRecord(String platformId, String dbStatus, String originSystem, String traceId) {
        this.platformId = platformId;
        this.dbStatus = dbStatus;
        this.originSystem = originSystem;
        this.traceId = traceId;
    }

    public String getPlatformId() {
        return platformId;
    }

    public String getDbStatus() {
        return dbStatus;
    }

    public String getOriginSystem() {
        return originSystem;
    }

    public String getTraceId() {
        return traceId;
    }

    @Override
    public String toString() {
        return "PlatformRecord{" +
                "platformId='" + platformId + '\'' +
                ", dbStatus='" + dbStatus + '\'' +
                ", originSystem='" + originSystem + '\'' +
                ", traceId='" + traceId + '\'' +
                '}';
    }
}