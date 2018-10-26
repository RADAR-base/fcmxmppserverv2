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
    private final long expiry;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final CleanupService cleanupService;

    /**
     * Initialise the cleanup thread.
     * @param intervalTimeUnit TimeUnit to consider for intervalValue to periodically run this thread.
     * @param intervalValue How often to run this thread. Specified in intervalTimeUnit.
     * @param expiry How old notifications are considered expired. Should be specified in days.
     */
    public DefaultDatabaseCleanupTask(TimeUnit intervalTimeUnit, long intervalValue, long expiry, CleanupService cleanupService) {
        this.interval = intervalValue;
        this.expiry = expiry;
        this.intervalTimeUnit = intervalTimeUnit;
        this.cleanupService = cleanupService;
    }

    /**
     * Starts the first cleanup at 3am from next day and then repeats every {interval} intervalTimeUnits.
     * Better to provide, this info in day intervals to ensure it always runs in the night (least traffic).
     */
    @Override
    public void startCleanup() {
        Long nightTime3am = LocalDateTime.now().until(LocalDate.now().plusDays(1).atStartOfDay().plus(Duration.ofHours(3)), ChronoUnit.MINUTES);
        scheduler.scheduleAtFixedRate(() -> {
            logger.info("Running Clean Up task to remove delivered notifications");
            cleanupService.removeNotifications(expiry);
        }, nightTime3am, interval, intervalTimeUnit);
    }

    @Override
    public void stopCleanup() {

    }

}
