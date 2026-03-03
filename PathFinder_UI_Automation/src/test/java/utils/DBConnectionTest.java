package utils;

import utils.DatabaseUtils;
import java.util.Properties;

public class DBConnectionTest {

    public static void main(String[] args) throws Exception {

        Properties prop = new Properties();
        prop.setProperty("db.url",
            "jdbc:postgresql://pathfinderppdb-7h7nrlmimgepbaenojek3p.postgres.database.azure.com:5432/pathfinderppdb?sslmode=require");

        prop.setProperty("db.username",
            "pathfinderppdb");

        prop.setProperty("db.password",
            "JRtS8sl8ZH=PFTa");

        //DatabaseUtils.testConnection(prop);
    }
}