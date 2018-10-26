package org.radarcns.xmppserver.service;

import org.radarcns.xmppserver.database.NotificationDatabaseHelper;
import org.radarcns.xmppserver.model.ExpandedNotification;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class DatabaseCleanupService implements CleanupService {

    private NotificationDatabaseHelper notificationDatabaseHelper;

    public DatabaseCleanupService(NotificationDatabaseHelper notificationDatabaseHelper) {
        this.notificationDatabaseHelper = notificationDatabaseHelper;
    }

    @Override
    public synchronized void removeNotifications(long expiry) {
        long millisToCompare = new Date().toInstant().toEpochMilli() - Duration.ofDays(expiry).toMillis();

        Set<ExpandedNotification> notificationSet = Collections.synchronizedSet(
                new HashSet<>(notificationDatabaseHelper.findAllDeliveredNotifications()));

        notificationSet.stream()
                .filter(n -> n.getNotification().getScheduledTime().toInstant().toEpochMilli() < millisToCompare)
                .forEach(n -> notificationDatabaseHelper.removeNotification(n.getNotificationTaskUuid()));
    }
}
