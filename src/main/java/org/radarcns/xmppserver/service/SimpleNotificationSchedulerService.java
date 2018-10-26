package org.radarcns.xmppserver.service;

import org.radarcns.xmppserver.model.Data;
import org.radarcns.xmppserver.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


public class SimpleNotificationSchedulerService implements NotificationSchedulerService {

    protected static final Logger logger = LoggerFactory.getLogger(SimpleNotificationSchedulerService.class);
    private HashMap<String, HashSet<ScheduleTask<Notification>>> scheduleTaskHashMap;

    // Singleton class. We only want one scheduler service running.
    private static SimpleNotificationSchedulerService instance = null;

    private boolean isRunning = false;

    private synchronized void scheduleNotificationForDate(Data data) {
        Notification notification = Notification.getNotification(data.getFrom(), data.getPayload());
        ScheduleTask<Notification> notificationScheduleTask = new ScheduleTask<>(notification).scheduleForDate();

        logger.info(notification.toString());

        if (scheduleTaskHashMap.containsKey(data.getFrom())) {
            if (!scheduleTaskHashMap.get(data.getFrom()).contains(notificationScheduleTask)) {
                scheduleTaskHashMap.get(data.getFrom()).add(notificationScheduleTask);
            }
        } else {
            HashSet<ScheduleTask<Notification>> newHashSet = new HashSet<>();
            newHashSet.add(notificationScheduleTask);

            scheduleTaskHashMap.put(data.getFrom() + notification.getSubjectId(), newHashSet);
        }
    }

    private synchronized void cancelAllNotifications(String partOfKey) {
        for (String key : scheduleTaskHashMap.keySet()) {
            if (key.contains(partOfKey)) {
                scheduleTaskHashMap.get(key).forEach(s -> s.getScheduledFuture().cancel(true));
                scheduleTaskHashMap.remove(key);
            }
        }
    }

    public static NotificationSchedulerService getInstance() {
        if (instance == null) {
            instance = new SimpleNotificationSchedulerService();
        }
        return instance;
    }

    @Override
    public void start() {
        if (!isRunning) {
            scheduleTaskHashMap = new HashMap<>();
            isRunning = true;
        } else {
            logger.warn("Cannot start an instance of {} when it is already running.", SimpleNotificationSchedulerService.class.getName());
        }
    }

    @Override
    public void stop() {
        if (isRunning) {
            isRunning = false;
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
    public void schedule(Data data) {
        if (isRunning) {
            scheduleNotificationForDate(data);
        } else {
            logger.warn("Cannot schedule using an instance of {} when it is not running.", SimpleNotificationSchedulerService.class.getName());
        }
    }

    @Override
    public void schedule(Collection<Data> data) {
        data.forEach(this::schedule);
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
            scheduleTaskHashMap.get(oldToken).forEach(s -> s.setData(new Notification.Builder(s.getData()).setRecepient(newToken).build()));
        }
    }

    @Override
    public void confirmDelivery(String messageId, String token) {
        logger.info("Delivered Message with Message ID {} to Token {}", messageId, token);
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public long getNumberOfScheduledNotifications() {
        return scheduleTaskHashMap.values().stream().map(HashSet::size).mapToLong(Integer::longValue).sum();
    }
}
