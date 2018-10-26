package org.radarcns.xmppserver.service;

public class PersistentDatabaseNotificationSchedulerService extends DatabaseNotificationSchedulerService {

    private static final String TYPE = "file";

    private static PersistentDatabaseNotificationSchedulerService instance = null;

    private PersistentDatabaseNotificationSchedulerService() {
        super(TYPE);
    }

    public static NotificationSchedulerService getInstance() {
        if (instance == null) {
            instance = new PersistentDatabaseNotificationSchedulerService();
        }
        return instance;
    }

}
