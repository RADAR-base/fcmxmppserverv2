package org.radarcns.xmppserver.util;

import com.google.common.cache.*;
import org.radarcns.xmppserver.model.Data;
import org.radarcns.xmppserver.service.NotificationSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ScheduleCache implements RemovalListener<String, Data> {

    private Logger logger = LoggerFactory.getLogger(ScheduleCache.class);

    private Cache<String, Data> dataCache;
    private NotificationSchedulerService notificationSchedulerService;

    public ScheduleCache(int expiry, int maxSize, NotificationSchedulerService notificationSchedulerService) {
        dataCache = CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expiry, TimeUnit.SECONDS)
                .removalListener(this)
                .build();
        this.notificationSchedulerService = notificationSchedulerService;
    }

    @Override
    public void onRemoval(@Nonnull RemovalNotification<String, Data> notification) {
        logger.info("Removing data from cache due to : {}", notification.getCause());
        notificationSchedulerService.schedule(notification.getValue());
    }

    public void add(Data data) {
        logger.info("Adding data to cache...");
        dataCache.put(data.getFrom()+ UUID.randomUUID(), data);
    }
}



