package org.radarcns.xmppserver.config;


/**
 * Database config class for the XMPP Server
 *
 * @author yatharthranjan
 */
public class DbConfig {

    public static final String DB_PATH_SEPARATOR = ":";

    String dbType;

    String dbPath;

    public DbConfig(String dbType, String dbPath) {
        this.dbType = dbType;
        this.dbPath = dbPath;
    }

    public String getDbType() {
        return dbType;
    }

    public String getDbPath() {
        return dbPath;
    }

    @Override
    public String toString() {
        return "DbConfig{" +
                "dbType='" + dbType + '\'' +
                ", dbPath='" + dbPath + '\'' +
                '}';
    }
}
