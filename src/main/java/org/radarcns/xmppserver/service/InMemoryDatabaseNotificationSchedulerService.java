package org.radarcns.xmppserver.service;

public class InMemoryDatabaseNotificationSchedulerService extends DatabaseNotificationSchedulerService {

    private static final String TYPE = "mem";

    private static InMemoryDatabaseNotificationSchedulerService instance = null;

    private InMemoryDatabaseNotificationSchedulerService() {
        super(TYPE);
    }

    public static NotificationSchedulerService getInstance() {
        if (instance == null) {
            instance = new InMemoryDatabaseNotificationSchedulerService();
        }
        return instance;
    }
}
