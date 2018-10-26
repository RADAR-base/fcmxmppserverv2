package org.radarcns.xmppserver.service;

import org.radarcns.xmppserver.database.NotificationDatabaseHelper;
import org.radarcns.xmppserver.model.ExpandedNotification;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DatabaseCleanupService implements CleanupService {

    private NotificationDatabaseHelper notificationDatabaseHelper;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DatabaseCleanupService.class);

    DatabaseCleanupService(NotificationDatabaseHelper notificationDatabaseHelper) {
        this.notificationDatabaseHelper = notificationDatabaseHelper;
    }

    @Override
    public synchronized void removeNotifications(TimeUnit expiryTimeUnit, long expiry) {
        long millisToCompare = new Date().toInstant().toEpochMilli() - expiryTimeUnit.toMillis(expiry);

        Set<ExpandedNotification> notificationSet = Collections.synchronizedSet(
                new HashSet<>(notificationDatabaseHelper.findAllDeliveredNotifications()));

        logger.debug("Delivered Notifications:" + notificationSet);

        notificationSet.stream()
                .filter(n -> n.getNotification().getScheduledTime().toInstant().toEpochMilli() < millisToCompare)
                .forEach(n -> notificationDatabaseHelper.removeNotification(n.getNotificationTaskUuid()));

        logger.info("Delivered Notifications removed!");
    }
}
