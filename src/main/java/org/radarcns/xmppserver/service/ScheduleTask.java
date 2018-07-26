package org.radarcns.xmppserver.service;

import org.radarcns.xmppserver.ccs.CcsClientWrapper;
import org.radarcns.xmppserver.model.Notification;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduleTask<E extends Notification>{

    private E data;

    private ScheduledFuture<?> scheduledFuture;

    ScheduleTask(E data){
        this.data = data;
    }

    public ScheduleTask<E> scheduleForDate(){
        Date now = new Date();
        long delay = data.getScheduledTime().getTime() - now.getTime();
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        scheduledFuture = ses.schedule(() -> {

            SimpleNotificationSchedulerService.logger.info("Trying to send notification {}", data);
            CcsClientWrapper.getInstance().sendNotification(data);

            SimpleNotificationSchedulerService.logger.info("Sent downstream Scheduled message with title: {}", data.getTitle());

        }, delay, TimeUnit.MILLISECONDS);

        return this;
    }


    public E getData() {
        return data;
    }

    public void setData(E data) {
        this.data = data;
    }

    public ScheduledFuture<?> getScheduledFuture() {
        return scheduledFuture;
    }

    public void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

}
