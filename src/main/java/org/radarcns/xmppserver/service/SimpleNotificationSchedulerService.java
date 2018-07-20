package org.radarcns.xmppserver.service;

import com.wedevol.xmpp.server.CcsClient;
import org.radarcns.xmppserver.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class SimpleNotificationSchedulerService implements NotificationSchedulerService{

    protected static final Logger logger = LoggerFactory.getLogger(SimpleNotificationSchedulerService.class);
    private HashMap<String, HashSet<ScheduledFuture<?>>> scheduledFutureHashMap;

    // Singleton class. We only want one scheduler service running.
    private static SimpleNotificationSchedulerService INSTANCE = null;

    private boolean isRunning = false;

    private void scheduleNotificationForDate(String from, Map<String, String> payload) {
        Notification notification = Notification.getNotification(from, payload);
        ScheduledFuture scheduledFuture = ScheduleTask.scheduleForDate(notification);

        logger.info(notification.toString());
        logger.info("" + scheduledFuture.getDelay(TimeUnit.MILLISECONDS));

        if (scheduledFutureHashMap.containsKey(from)) {
            if (! scheduledFutureHashMap.get(from).contains(scheduledFuture)) {
                scheduledFutureHashMap.get(from).add(scheduledFuture);
            }
        } else {
            HashSet<ScheduledFuture<?>> newHashSet = new HashSet<>();
            newHashSet.add(scheduledFuture);

            scheduledFutureHashMap.put(from, newHashSet);
        }
    }

    private void cancelAllNotificationsForToken(String token) {
        if (scheduledFutureHashMap.containsKey(token)) {
            scheduledFutureHashMap.get(token).forEach(s -> s.cancel(true));
        }
    }

    public static NotificationSchedulerService getINSTANCE() {
        if(INSTANCE == null) {
            INSTANCE = new SimpleNotificationSchedulerService();
        }
        return INSTANCE;
    }

    @Override
    public void start() {
        if(!isRunning) {
            scheduledFutureHashMap = new HashMap<>();
            isRunning = true;
        } else {
            logger.warn("Cannot start an instance of {} when it is already running.", SimpleNotificationSchedulerService.class.getName());
        }
    }

    @Override
    public void stop() {
        if(!isRunning) {
            // Stop all scheduled tasks
            for (String token : scheduledFutureHashMap.keySet()) {
                cancelAllNotificationsForToken(token);
            }
        } else {
            logger.warn("Cannot stop an instance of {} when it is not running.", SimpleNotificationSchedulerService.class.getName());
        }
    }

    @Override
    public void schedule(String token, Map<String, String> payload) {
        if(isRunning) {
            scheduleNotificationForDate(token, payload);
        } else {
            logger.warn("Cannot schedule using an instance of {} when it is not running.", SimpleNotificationSchedulerService.class.getName());
        }
    }

    @Override
    public void cancel(String from) {
        cancelAllNotificationsForToken(from);
    }

    @Override
    public void updateToken(String oldToken, String newToken) {
        //TODO Add update logic

    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

}
