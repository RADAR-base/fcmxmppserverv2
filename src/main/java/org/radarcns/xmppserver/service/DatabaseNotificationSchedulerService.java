package org.radarcns.xmppserver.service;

import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import org.apache.commons.dbcp.BasicDataSource;
import org.radarcns.xmppserver.ccs.CcsClientWrapper;
import org.radarcns.xmppserver.commandline.CommandLineArgs;
import org.radarcns.xmppserver.database.DataSourceWrapper;
import org.radarcns.xmppserver.database.NotificationDatabaseHelper;
import org.radarcns.xmppserver.model.Data;
import org.radarcns.xmppserver.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Database notification scheduler service class for the XMPP Server
 * This has functionality for both in-memory and persisent data bases.
 * Abstract class so has to instantiated through sub-classes. Look at
 * {@link InMemoryDatabaseNotificationSchedulerService} and
 * {@link PersistentDatabaseNotificationSchedulerService}
 *
 * @author yatharthranjan
 */
public abstract class DatabaseNotificationSchedulerService implements NotificationSchedulerService {

    private Scheduler scheduler;
    private final DataSourceWrapper scheduleDataSourceWrapper;
    private final NotificationDatabaseHelper databaseHelper;
    private OneTimeTask<Notification> notificationOneTimeTask;
    private boolean isRunning = false;

    private static final String TASK_NAME = "notification-one-time";
    private static final String TASK_ID_DELIMITER = "+";
    private static final Logger logger = LoggerFactory.getLogger(DatabaseNotificationSchedulerService.class);

    DatabaseNotificationSchedulerService(String type) {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        basicDataSource.setUrl("jdbc:hsqldb:" + type
                + ":" + CommandLineArgs.dbPath);
        basicDataSource.setUsername(CommandLineArgs.dbUser);
        basicDataSource.setPassword(CommandLineArgs.dbPass);
        this.scheduleDataSourceWrapper = new DataSourceWrapper(basicDataSource);

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        dataSource.setUrl("jdbc:hsqldb:" + type
                + ":" + CommandLineArgs.dbPath.substring(0,
                CommandLineArgs.dbPath.lastIndexOf('/')) + "/status");
        dataSource.setUsername(CommandLineArgs.dbUser);
        dataSource.setPassword(CommandLineArgs.dbPass);
        this.databaseHelper = new NotificationDatabaseHelper(dataSource);
    }

    @Override
    public void start() {

        if(! isRunning) {
            scheduleDataSourceWrapper.createTableForScheduler();
            databaseHelper.createTableForStatus();
            databaseHelper.createTableforNotification();

            notificationOneTimeTask = Tasks.oneTime(TASK_NAME, Notification.class).execute(
                    (inst, ctx) -> {
                        CcsClientWrapper.getInstance().sendNotification(inst.getData());
                    }
            );

            scheduler = Scheduler
                    .create(scheduleDataSourceWrapper.getDataSource(), notificationOneTimeTask)
                    .threads(5)
                    .build();

            scheduler.start();

            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
            migrateDataFromScheduler();

            isRunning = true;


        } else {
            logger.warn("Cannot start an instance of {} when it is already running.", this.getClass().getName());
        }
    }

    /**
     * Migrates data from the Scheduler database({@link CommandLineArgs#dbPath}) to the new Status database (/usr/hsql/status).
     * This is required to make sure that the two are synchronised and provide backward compatibility.
     */
    private void migrateDataFromScheduler() {
        scheduler.getScheduledExecutions(taskScheduledExecution -> {
            String[] ids = taskScheduledExecution.getTaskInstance().getId().split(Pattern.quote(TASK_ID_DELIMITER));
                Notification notification = (Notification) taskScheduledExecution.getData();
                if(!databaseHelper.checkIfNotificationExists(notification)) {
                    databaseHelper.addNotification(notification, String.valueOf(notification.hashCode()), ids[2]);
                }
        });
    }

    @Override
    public void stop() {

        if(isRunning) {
            isRunning = false;
            scheduler.stop();
            scheduleDataSourceWrapper.close();
        } else {
            logger.warn("Cannot stop an instance of {} when it is not running.", this.getClass().getName());
        }
    }

    @Override
    public synchronized void schedule(List<Data> data) {
        data.forEach(this::schedule);
    }

    /**
     * Schedules a single notification. This checks if a notification already exists for a particular subject and FCM token,
     * if it does not exist then schedules a task. The messageId for FCM is generate using Hashcode from the
     * {@link Notification} object as each notification is supposed to be unique.
     * @param data data object encapsulating the scheduling information
     */
    @Override
    public synchronized void schedule(Data data) {
        if(isRunning) {

            // TODO: Use a cache to get and add the scheduled notifications as most of them will be from same subject at a given burst of requests
            Notification notification = Notification.getNotification(data.getFrom(), data.getPayload());
            if(!databaseHelper.checkIfNotificationExists(notification)) {
                String taskUuid = UUID.randomUUID().toString();
                // Task id consists of 3 parts -- The FCM token, the subjectID (or any other custom ID)
                // and a UUID to make the task unique
                String taskId = notification.getRecepient() +
                        TASK_ID_DELIMITER +
                        notification.getSubjectId() +
                        TASK_ID_DELIMITER +
                        taskUuid;
                scheduler.schedule(notificationOneTimeTask.instance(taskId, notification), notification.getScheduledTime().toInstant());
                logger.info("Task scheduled for notification {}", notification);
                databaseHelper.addNotification(notification, String.valueOf(notification.hashCode()), taskUuid);
            } else {
                logger.debug("Notification already exists for subject {} : {}", notification.getSubjectId(), notification);
            }
        } else {
            logger.warn("Cannot schedule using an instance of {} when it is not running. Please start the service first.", this.getClass().getName());
        }
    }

    @Override
    public synchronized void cancelUsingFcmToken(String from) {
        if(isRunning) {
            scheduler.getScheduledExecutions(taskScheduledExecution -> {
                String[] ids = taskScheduledExecution.getTaskInstance().getId().split(Pattern.quote(TASK_ID_DELIMITER));
                if (ids.length == 3 && from.equals(ids[0])) {
                    logger.info("Removing the scheduled task using token {} from thread {} - {}", ids[0],
                            Thread.currentThread().getName(), Thread.currentThread().getId());
                    scheduler.cancel(taskScheduledExecution.getTaskInstance());
                }
            });
            databaseHelper.removeAllNotifications(null, from);
        } else {
            logger.warn("Cannot cancelUsingFcmToken using an instance of {} when it is not running. Please start the service first.", this.getClass().getName());
        }
    }

    @Override
    public synchronized void cancelUsingCustomId(String id) {
        if(isRunning) {
            scheduler.getScheduledExecutions(taskScheduledExecution -> {
                String[] ids = taskScheduledExecution.getTaskInstance().getId().split(Pattern.quote(TASK_ID_DELIMITER));
                if (ids.length == 3 && id.equals(ids[1])) {
                    logger.info("Removing the scheduled task using id {} from thread {} - {}", id,
                            Thread.currentThread().getName(), Thread.currentThread().getId());
                    scheduler.cancel(taskScheduledExecution.getTaskInstance());
                }
            });
            databaseHelper.removeAllNotifications(id, null);
        } else {
            logger.warn("Cannot cancelUsingFcmToken using an instance of {} when it is not running. Please start the service first.", this.getClass().getName());
        }
    }


    @Override
    public synchronized void updateToken(String oldToken, String newToken) {
        if(isRunning) {
            // TODO update token based on subject id
            scheduler.getScheduledExecutions(taskScheduledExecution -> {
                if (taskScheduledExecution.getTaskInstance().getId().contains(oldToken)) {
                    logger.info("Updating token on the scheduled task with id {} from thread {} - {}", taskScheduledExecution.getTaskInstance().getId(),
                            Thread.currentThread().getName(), Thread.currentThread().getId());
                    Notification notification = (Notification) (taskScheduledExecution.getData());
                    if(notification.getRecepient().equals(oldToken)) {
                        //notification.setRecepient(newToken);
                    }
                }
            });
        } else {
            logger.warn("Cannot update using an instance of {} when it is not running. Please start the service first.", this.getClass().getName());
        }
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void confirmDelivery(String messageId, String token) {
        // TODO: Uncomment so kafka sender can pick this up from the Database
        //databaseHelper.updateDeliveryStatus(true, token, messageId);

        // TODO: remove this and add in the Batched Kafka Sender
        databaseHelper.removeNotification(messageId, token);
        logger.info("Removed message from database after delivery: {} ", messageId);
    }
}
