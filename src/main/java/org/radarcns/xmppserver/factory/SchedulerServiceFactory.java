package org.radarcns.xmppserver.factory;

import com.wedevol.xmpp.server.CcsClient;
import org.radarcns.xmppserver.config.Config;
import org.radarcns.xmppserver.config.DbConfig;
import org.radarcns.xmppserver.service.DatabaseNotificationSchedulerService;
import org.radarcns.xmppserver.service.NotificationSchedulerService;
import org.radarcns.xmppserver.service.SimpleNotificationSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factroy class providing scheduler for the XMPP Server
 *
 * @author yatharthranjan
 */
public class SchedulerServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerServiceFactory.class);

    public static NotificationSchedulerService getSchedulerService(String type, CcsClient ccsClient) {
        switch (type) {
            case Config.SCHEDULER_MEM:
                DbConfig dbConfig = new DbConfig("mem", "notificationDB");
                return DatabaseNotificationSchedulerService.getInstanceForCcsClientAndCofig(ccsClient, dbConfig);

            case Config.SCHEDULER_PERSISTENT:
                dbConfig = new DbConfig("file", "/usr/hsql/notification");
                return DatabaseNotificationSchedulerService.getInstanceForCcsClientAndCofig(ccsClient, dbConfig);

            case Config.SCHEDULER_SIMPLE:
                return SimpleNotificationSchedulerService.getINSTANCEForCcsClient(ccsClient);

                default: logger.warn("No Scheduler Service for type : {}, Using a simple " +
                        "notification scheduler service", type);
                return SimpleNotificationSchedulerService.getINSTANCEForCcsClient(ccsClient);
        }
    }
}
