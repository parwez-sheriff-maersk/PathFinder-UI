package utils;

public class AdvancedSearchRecord {

    private final String transactionId;
    private final String platformId;
    private final String dbStatus;
    private final String originSystem;

    public AdvancedSearchRecord(String transactionId, String platformId,
                                String dbStatus, String originSystem) {
        this.transactionId  = transactionId;
        this.platformId     = platformId;
        this.dbStatus       = dbStatus;
        this.originSystem   = originSystem;
    }

    public String getTransactionId()  { return transactionId; }
    public String getPlatformId()     { return platformId; }
    public String getDbStatus()       { return dbStatus; }
    public String getOriginSystem()   { return originSystem; }

    @Override
    public String toString() {
        return "AdvancedSearchRecord{" +
                "transactionId='" + transactionId + '\'' +
                ", platformId='" + platformId + '\'' +
                ", dbStatus='" + dbStatus + '\'' +
                ", originSystem='" + originSystem + '\'' +
                '}';
    }
}
