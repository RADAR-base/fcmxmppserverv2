package org.radarcns.xmppserver.util;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.radarcns.xmppserver.commandline.CommandLineArgs;
import org.radarcns.xmppserver.config.Config;
import org.radarcns.xmppserver.database.NotificationDatabaseHelper;
import org.radarcns.xmppserver.factory.SchedulerServiceFactory;
import org.radarcns.xmppserver.service.NotificationSchedulerService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DatabaseLoadTest {

    private static final long CACHE_EXPIRY = 30;
    private static final long CACHE_CLEANUP_INTERVAL = 120;
    private static final String SCHEDULER_TYPE = Config.SCHEDULER_PERSISTENT;
    private static final NotificationSchedulerService notificationSchedulerService;
    private static NotificationDatabaseHelper databaseHelper;
    private static final ScheduleCache cache;
    private static MockDataProducer mockDataProducer;
    static {
        if(! SCHEDULER_TYPE.equals(Config.SCHEDULER_SIMPLE)
                && (CommandLineArgs.dbPath == null || CommandLineArgs.dbPath.isEmpty())) {
            switch (SCHEDULER_TYPE) {
                case Config.SCHEDULER_MEM:
                    CommandLineArgs.dbPath = "notificationDB";
                    break;

                case Config.SCHEDULER_PERSISTENT:
                    CommandLineArgs.dbPath = "/usr/local/hsql/notification";
                    break;
            }
        }

        notificationSchedulerService = SchedulerServiceFactory.getSchedulerService(SCHEDULER_TYPE);
        cache = new ScheduleCache(CACHE_EXPIRY, notificationSchedulerService);

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        dataSource.setUrl("jdbc:hsqldb:" + "file"
                + ":" + CommandLineArgs.dbPath.substring(0,
                CommandLineArgs.dbPath.lastIndexOf('/')) + "/status");
        dataSource.setUsername(CommandLineArgs.dbUser);
        dataSource.setPassword(CommandLineArgs.dbPass);
        databaseHelper = new NotificationDatabaseHelper(dataSource);
    }
    @BeforeClass
    public static void init() {
        notificationSchedulerService.start();
        cache.runCleanUpTillShutdown(CACHE_CLEANUP_INTERVAL);
    }

    @Test
    public void perSecond1000OneUser() {
        String subjectId = "1";
        String fcmToken = "1";

        assertTrue(true);
    }

    @Test
    public void perSecond10000OneUser() throws InterruptedException {
        int numOfTimes = 5;
        int numOfRecords = 10000;
        int numOfUsers = 1;
        mockDataProducer = new MockDataProducer(numOfTimes, numOfRecords, numOfUsers);

        mockDataProducer.generateDataToCache(cache);
        // Sleep 4 more minutes for scheduling to be completed.
        Thread.sleep(240_000);

        assertTrue(mockDataProducer.verifyData(databaseHelper));

        databaseHelper.removeAllNotifications(String.valueOf(1), String.valueOf(1));
    }

    @Test(timeout = 480_000)
    public void perSecond1000HundredUsers() throws InterruptedException {
        int numOfTimes = 5;
        int numOfRecords = 1000;
        int numOfUsers = 100;

        mockDataProducer = new MockDataProducer(numOfTimes, numOfRecords, numOfUsers);
        /**
         * Run for 5 seconds with ingesting 1000 records from different users per second
         */
        mockDataProducer.generateDataToCache(cache);
        // Sleep 4 more minutes for scheduling to be completed.
        Thread.sleep(240_000);

        assertEquals(numOfTimes * numOfRecords, notificationSchedulerService.getNumberOfScheduledNotifications());

        assertTrue(mockDataProducer.verifyData(databaseHelper));
    }

    @AfterClass
    public static void clean() {
        for(int i = 1; i <= 100; i++) {
            databaseHelper.removeAllNotifications(String.valueOf(i), String.valueOf(i));
        }
    }
}
