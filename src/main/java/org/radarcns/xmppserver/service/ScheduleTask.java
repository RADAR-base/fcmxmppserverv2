package org.radarcns.xmppserver.service;

import org.radarcns.xmppserver.ccs.CcsClientWrapper;
import org.radarcns.xmppserver.model.Notification;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduleTask {

    public static ScheduledFuture<?> scheduleForDate(Notification notification){
        Date now = new Date();
        long delay = notification.getScheduledTime().getTime() - now.getTime();
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        return ses.schedule(() -> {

            SimpleNotificationSchedulerService.logger.info("Trying to send notification {}", notification);
            CcsClientWrapper.getInstance().sendNotification(notification);

            SimpleNotificationSchedulerService.logger.info("Sent downstream Scheduled message with title: {}", notification.getTitle());

        }, delay, TimeUnit.MILLISECONDS);
    }
}
