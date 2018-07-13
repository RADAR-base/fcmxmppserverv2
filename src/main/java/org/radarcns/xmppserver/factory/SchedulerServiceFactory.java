package org.radarcns.xmppserver.factory;

import com.wedevol.xmpp.server.CcsClient;
import org.radarcns.xmppserver.config.DbConfig;
import org.radarcns.xmppserver.service.DatabaseNotificationSchedulerService;
import org.radarcns.xmppserver.service.NotificationSchedulerService;
import org.radarcns.xmppserver.service.SchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerServiceFactory.class);

    public static SchedulerService getSchedulerService(String type, CcsClient ccsClient) {
        switch (type) {
            case "in-mem-db":
                DbConfig dbConfig = new DbConfig("mem", "notificationDB");
                return DatabaseNotificationSchedulerService.getInstanceForCcsClientAndCofig(ccsClient, dbConfig);

            case "persistent-db":
                dbConfig = new DbConfig("file", "/usr/hsql/notification");
                return DatabaseNotificationSchedulerService.getInstanceForCcsClientAndCofig(ccsClient, dbConfig);

            case "simple":
                return NotificationSchedulerService.getINSTANCEForCcsClient(ccsClient);

                default: logger.warn("No Scheduler Service for type : {}, Using a simple " +
                        "notification scheduler service", type);
                return NotificationSchedulerService.getINSTANCEForCcsClient(ccsClient);
        }
    }
}
