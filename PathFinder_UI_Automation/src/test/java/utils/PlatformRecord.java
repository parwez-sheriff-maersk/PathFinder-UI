package utils;

public class PlatformRecord {

    private String platformId;
    private String dbStatus;
    private String originSystem;

    public PlatformRecord(String platformId, String dbStatus, String originSystem) {
        this.platformId = platformId;
        this.dbStatus = dbStatus;
        this.originSystem = originSystem;
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
}