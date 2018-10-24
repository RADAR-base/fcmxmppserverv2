package org.radarcns.xmppserver.service;

public class ServerDatabaseNotificationSchedulerService extends DatabaseNotificationSchedulerService {

    private static final String TYPE = "hsql";

    private static ServerDatabaseNotificationSchedulerService instance = null;

    private ServerDatabaseNotificationSchedulerService() {
        super(TYPE);
    }

    public static NotificationSchedulerService getInstance() {
        if(instance == null) {
            instance = new ServerDatabaseNotificationSchedulerService();
        }

        return instance;
    }

}
