package org.radarcns.xmppserver.service;

import com.wedevol.xmpp.server.CcsClient;
import org.radarcns.xmppserver.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ScheduledFuture;


public class NotificationSchedulerService implements SchedulerService{

    private CcsClient ccsClient;
    protected static final Logger logger = LoggerFactory.getLogger(NotificationSchedulerService.class);
    private HashMap<String, HashSet<ScheduledFuture<?>>> scheduledFutureHashMap;

    // Singleton class. We only want one scheduler service running.
    private static NotificationSchedulerService INSTANCE = null;

    private boolean isRunning = false;

    private NotificationSchedulerService(CcsClient ccsClient) {
        this.ccsClient = ccsClient;
    }

    private void scheduleNotificationForDate(String from, Map<String, String> payload) {
        String datetime = payload.get("time"); // epoch timestamp in milliseconds
        String notificationTitle = payload.get("notificationTitle");
        String notificationMessage = payload.get("notificationMessage");

        Date date = new Date(Long.parseLong(datetime));


        Notification notification = new Notification(notificationTitle, notificationMessage, date, from);
        ScheduledFuture scheduledFuture = ScheduleTask.scheduleForDate(ccsClient, notification);

        logger.info(notification.toString());

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

    public void cancelAllNotificationsForToken(String token) {
        if (scheduledFutureHashMap.containsKey(token)) {
            scheduledFutureHashMap.get(token).forEach(s -> s.cancel(true));
        }
    }

    public static NotificationSchedulerService getINSTANCEForCcsClient(CcsClient ccsClient) {
        if(INSTANCE == null) {
            INSTANCE = new NotificationSchedulerService(ccsClient);
        }
        return INSTANCE;
    }

    @Override
    public void start() {
        if(!isRunning) {
            scheduledFutureHashMap = new HashMap<>();
            isRunning = true;
        } else {
            logger.warn("Cannot start an instance of {} when it is already running.", NotificationSchedulerService.class.getName());
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
            logger.warn("Cannot stop an instance of {} when it is not running.", NotificationSchedulerService.class.getName());
        }
    }

    @Override
    public void schedule(String token, Map<String, String> payload) {
        if(isRunning) {
            scheduleNotificationForDate(token, payload);
        } else {
            logger.warn("Cannot schedule using an instance of {} when it is not running.", NotificationSchedulerService.class.getName());
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
