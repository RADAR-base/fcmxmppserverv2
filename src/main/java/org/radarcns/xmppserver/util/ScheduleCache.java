package org.radarcns.xmppserver.util;

import org.radarcns.xmppserver.database.NotificationDatabaseHelper;
import org.radarcns.xmppserver.model.Data;
import org.radarcns.xmppserver.service.NotificationSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Class for caching incoming schedule requests.
 * This removes the requests from cache based on time.
 * This is required because the notifications come in bursts and
 * this helps harmonize them.
 */
public class ScheduleCache {

    private Logger logger = LoggerFactory.getLogger(ScheduleCache.class);
    private Set<Data> currentData;
    private  Temporal lastPush;
    private ScheduledFuture<?> scheduledFuture = null;

    private final Duration scheduleAfter;
    private final NotificationSchedulerService notificationSchedulerService;

    /**
     * Creates a time based cache that removes records based on an expiry time
     * and/or maximum size of the number of records.
     * @param expiry expiry time after which records to be removed from the cache in seconds
     * @param notificationSchedulerService the {@link NotificationSchedulerService} to use for scheduling
     *                                     the notification upon removal of records from the cache
     */
    public ScheduleCache(long expiry, NotificationSchedulerService notificationSchedulerService) {
        this.notificationSchedulerService = notificationSchedulerService;
        this.currentData = Collections.synchronizedSet(new HashSet<>());
        lastPush = Instant.MIN;
        scheduleAfter = Duration.ofSeconds(expiry);
    }

    public void add(Data data) {
        logger.info("Adding data to cache...");
        if(NotificationDatabaseHelper.isThresholdPassed(lastPush, scheduleAfter)
                && currentData.size() > 10) {
            logger.info("Scheduling all notifications after {} seconds", scheduleAfter);
            pushData();
        }
        currentData.add(data);
    }

    private synchronized void pushData() {
        notificationSchedulerService.schedule(currentData);
        currentData = Collections.synchronizedSet(new HashSet<>());
        lastPush = Instant.now();
    }

    /**
     * Clean Up the cache at fixed interval to ensure the values are removed even if there
     * are no operations (read or write) on the cache. Should only be run after the
     * {@link NotificationSchedulerService} has been started.
     * This stops when the {@link NotificationSchedulerService} is stopped
     * @param interval The fixed interval at which to clean up cache
     *
     */
    public void runCleanUpTillShutdown(long interval) {
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        // Only start the task if its not already running
        if (scheduledFuture == null || scheduledFuture.isCancelled() || scheduledFuture.isDone()) {
            scheduledFuture = executorService.scheduleAtFixedRate(() -> {
                if (notificationSchedulerService.isRunning()) {
                    logger.info("Running custom maintenance to evict values every {} mins", (interval / 60));
                    if (!currentData.isEmpty()) {
                        pushData();
                    }
                } else {
                    logger.warn("Closing the cache Clean Up thread");
                    executorService.shutdownNow();
                }
            }, interval, interval, TimeUnit.SECONDS);
        } else {
            logger.info("The cache clean up task is already running");
        }
    }
}



