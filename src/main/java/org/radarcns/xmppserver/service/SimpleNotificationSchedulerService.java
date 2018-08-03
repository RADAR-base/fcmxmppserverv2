package org.radarcns.xmppserver.service;

import org.radarcns.xmppserver.model.Data;
import org.radarcns.xmppserver.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class SimpleNotificationSchedulerService implements NotificationSchedulerService{

    protected static final Logger logger = LoggerFactory.getLogger(SimpleNotificationSchedulerService.class);
    private HashMap<String, HashSet<ScheduleTask<Notification>>> scheduleTaskHashMap;

    // Singleton class. We only want one scheduler service running.
    private static SimpleNotificationSchedulerService instance = null;

    private boolean isRunning = false;

    private synchronized void scheduleNotificationForDate(String from, Map<String, String> payload) {
        Notification notification = Notification.getNotification(from, payload);
        ScheduleTask<Notification> notificationScheduleTask = new ScheduleTask<>(notification).scheduleForDate();

        logger.info(notification.toString());

        if (scheduleTaskHashMap.containsKey(from)) {
            if (! scheduleTaskHashMap.get(from).contains(notificationScheduleTask)) {
                scheduleTaskHashMap.get(from).add(notificationScheduleTask);
            }
        } else {
            HashSet<ScheduleTask<Notification>> newHashSet = new HashSet<>();
            newHashSet.add(notificationScheduleTask);

            scheduleTaskHashMap.put(from + notification.getSubjectId(), newHashSet);
        }
    }

    private synchronized void cancelAllNotifications(String partOfKey) {
        for(String key: scheduleTaskHashMap.keySet()) {
            if(key.contains(partOfKey)) {
                scheduleTaskHashMap.get(key).forEach(s -> s.getScheduledFuture().cancel(true));
                scheduleTaskHashMap.remove(key);
            }
        }
    }

    public static NotificationSchedulerService getInstance() {
        if(instance == null) {
            instance = new SimpleNotificationSchedulerService();
        }
        return instance;
    }

    @Override
    public void start() {
        if(!isRunning) {
            scheduleTaskHashMap = new HashMap<>();
            isRunning = true;
        } else {
            logger.warn("Cannot start an instance of {} when it is already running.", SimpleNotificationSchedulerService.class.getName());
        }
    }

    @Override
    public void stop() {
        if(!isRunning) {
            // Stop all scheduled tasks
            for (String key : scheduleTaskHashMap.keySet()) {
                scheduleTaskHashMap.get(key).forEach(s -> s.getScheduledFuture().cancel(true));
                scheduleTaskHashMap.remove(key);
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
    public void schedule(List<Data> data) {
        data.forEach(s -> schedule(s.getFrom(), s.getPayload()));
    }

    @Override
    public void cancelUsingFcmToken(String from) {
        cancelAllNotifications(from);
    }

    @Override
    public void cancelUsingCustomId(String id) {
        cancelAllNotifications(id);
    }

    @Override
    public void updateToken(String oldToken, String newToken) {
        //TODO Add update logic
        if (scheduleTaskHashMap.containsKey(oldToken)) {
            scheduleTaskHashMap.get(oldToken).forEach(s -> s.getData().setRecepient(newToken));
        }
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

}
