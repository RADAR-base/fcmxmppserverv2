package org.radarcns.xmppserver.factory;

import com.wedevol.xmpp.server.CcsClient;
import org.radarcns.xmppserver.service.NotificationSchedulerService;
import org.radarcns.xmppserver.service.SchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerServiceFactory.class);

    public static SchedulerService getSchedulerService(String type, CcsClient ccsClient) {
        switch (type) {
            case "in-mem-db":
                //TODO get instance
                return null;

            case "persistent-db":
                //TODO get instance
                return null;

            case "simple":
                return NotificationSchedulerService.getINSTANCEForCcsClient(ccsClient);

                default: logger.warn("No Scheduler Service for type : {}, Using a simple " +
                        "notification scheduler service", type);
                return NotificationSchedulerService.getINSTANCEForCcsClient(ccsClient);
        }
    }
}
