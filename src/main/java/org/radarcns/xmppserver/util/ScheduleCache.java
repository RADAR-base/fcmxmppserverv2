package org.radarcns.xmppserver.util;

import com.google.common.cache.*;
import org.radarcns.xmppserver.model.Data;
import org.radarcns.xmppserver.service.NotificationSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Class for caching incoming schedule requests.
 * This removes the requests from cache based on time and size.
 * This is required because the notifications come in bursts and
 * this helps harmonize them.
 */
public class ScheduleCache implements RemovalListener<String, Data> {

    private Logger logger = LoggerFactory.getLogger(ScheduleCache.class);

    private final Cache<String, Data> dataCache;
    private final NotificationSchedulerService notificationSchedulerService;

    /**
     * Creates a time based cache that removes records based on an expiry time
     * and/or maximum size of the number of records. All the removed records are
     * passed to the {@link #onRemoval(RemovalNotification)} where they are scheduled.
     * @param expiry expiry time after which records to be removed from the cache in ms
     * @param maxSize the maximum size to maintain after which records are removed from the cache
     * @param notificationSchedulerService the {@link NotificationSchedulerService} to use for scheduling
     *                                     the notification upon removal of records from the cache
     */
    public ScheduleCache(long expiry, long maxSize, NotificationSchedulerService notificationSchedulerService) {
        dataCache = CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expiry, TimeUnit.MILLISECONDS)
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

    /**
     * Clean Up the cache at fixed interval to ensure the values are removed even if there
     * are no operations (read or write) on the cache.
     * This stops when the {@link NotificationSchedulerService} is stopped
     * @param intervalMs The fixed interval at which to clean up cache
     *
     */
    public void runCleanUpTillShutdown(long intervalMs) {
        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

        executorService.scheduleAtFixedRate(() -> {
            if(notificationSchedulerService.isRunning()) {
                logger.info("Running custom maintenance to evict values every {} mins", (intervalMs/(60*1000)));
                dataCache.cleanUp();
            } else {
                logger.warn("Closing the cache Clean Up thread");
                executorService.shutdownNow();
            }
        }, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
    }
}



