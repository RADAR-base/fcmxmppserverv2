package org.radarcns.xmppserver.database;

import org.radarcns.xmppserver.service.CleanupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DefaultDatabaseCleanupTask implements DatabaseCleanupTask{

    private final static Logger logger = LoggerFactory.getLogger(DefaultDatabaseCleanupTask.class);
    private final TimeUnit intervalTimeUnit;
    private final long interval;
    private final TimeUnit expiryTimeUnit;
    private final long expiry;
    private final long startTime;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final CleanupService cleanupService;

    /**
     * Initialise the cleanup thread.
     * @param startTime The milliseconds from now, when to start the cleanup thread.
     * @param intervalTimeUnit TimeUnit to consider for intervalValue to periodically run this thread.
     * @param intervalValue How often to run this thread. Specified in intervalTimeUnit.
     * @param expiry How old notifications are considered expired. Should be specified in days.
     * @param expiryTimeUnit TimeUnit to consider for expiry
     *
     */
    public DefaultDatabaseCleanupTask(long startTime, TimeUnit intervalTimeUnit, long intervalValue, TimeUnit expiryTimeUnit, long expiry, CleanupService cleanupService) {
        this.interval = intervalValue;
        this.expiry = expiry;
        this.intervalTimeUnit = intervalTimeUnit;
        this.cleanupService = cleanupService;
        this.expiryTimeUnit = expiryTimeUnit;
        this.startTime = startTime;
    }

    /**
     * Starts the first cleanup at 3am from next day and then repeats every {interval} intervalTimeUnits.
     * Better to provide, this info in day intervals to ensure it always runs in the night (least traffic).
     */
    @Override
    public void startCleanup() {
        scheduler.scheduleAtFixedRate(() -> {
            logger.info("Running Clean Up task to remove delivered notifications");
            cleanupService.removeNotifications(expiryTimeUnit, expiry);
        }, startTime, interval, intervalTimeUnit);
    }

    @Override
    public void stopCleanup() {
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            logger.error("Database Cleanup Thread interrupted during shutdown", ex);
        }
    }

}
