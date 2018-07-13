package org.radarcns.xmppserver.service;

import com.wedevol.xmpp.server.CcsClient;
import org.radarcns.xmppserver.config.DbConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
/**
 * Database notification scheduler service class for the XMPP Server
 * This has functionality for both in-memory and persisent data bases.
 *
 * @author yatharthranjan
 */
public class DatabaseNotificationSchedulerService implements NotificationSchedulerService {

    private CcsClient ccsClient;

    private Connection sqlConnection;

    private static DatabaseNotificationSchedulerService INSTANCE = null;

    private DatabaseNotificationSchedulerService(CcsClient ccsClient, DbConfig dbConfig) {
        this.ccsClient = ccsClient;
        try {
            this.sqlConnection = DriverManager.getConnection("jdbc:hsqldb:" + dbConfig.getDbType()
                    + DbConfig.DB_PATH_SEPARATOR + dbConfig.getDbPath(), "", "");
        } catch (SQLException exc) {
            throw new RuntimeException("Cannot get connection to database with config: {}" + dbConfig.toString(), exc);
        }
    }

    public static NotificationSchedulerService getInstanceForCcsClientAndCofig(CcsClient ccsClient, DbConfig dbConfig) {
        if(INSTANCE == null) {
            INSTANCE = new DatabaseNotificationSchedulerService(ccsClient, dbConfig);
        }
        return INSTANCE;
    }

    @Override
    public void start() {
        // TODO intitialize db
    }

    @Override
    public void stop() {
        // TODO close db

        try {sqlConnection.commit();
            sqlConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void schedule(String from, Map<String, String> payload) {
        // TODO add notifications to database and schedule
    }

    @Override
    public void cancel(String from) {
        // TODO remove notifications from database and cancel
    }

    @Override
    public void updateToken(String oldToken, String newToken) {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    public DatabaseNotificationSchedulerService getInstance() {
        return null;
    }
}
