package org.radarcns.xmppserver.factory;

import org.radarcns.xmppserver.config.Config;
import org.radarcns.xmppserver.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class providing scheduler for the XMPP Server
 *
 * @author yatharthranjan
 */
public class SchedulerServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerServiceFactory.class);

    public static NotificationSchedulerService getSchedulerService(String type) {
        switch (type) {
            case Config.SCHEDULER_MEM:
                return InMemoryDatabaseNotificationSchedulerService.getInstance();

            case Config.SCHEDULER_PERSISTENT:
                return PersistentDatabaseNotificationSchedulerService.getInstance();

            case Config.SCHEDULER_SERVER:
                return ServerDatabaseNotificationSchedulerService.getInstance();

            case Config.SCHEDULER_SIMPLE:
                return SimpleNotificationSchedulerService.getInstance();

                default: logger.warn("No Scheduler Service for type : {}, Using a simple " +
                        "notification scheduler service", type);
                return SimpleNotificationSchedulerService.getInstance();
        }
    }
}
