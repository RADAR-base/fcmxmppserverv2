package org.radarcns.xmppserver.service;

import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import org.apache.commons.dbcp.BasicDataSource;
import org.radarcns.xmppserver.ccs.CcsClientWrapper;
import org.radarcns.xmppserver.commandline.CommandLineArgs;
import org.radarcns.xmppserver.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

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
    private BasicDataSource basicDataSource;
    private OneTimeTask<Notification> notificationOneTimeTask;

    private boolean isRunning = false;

    private static final String TASK_NAME = "notification-one-time";
    private static final Logger logger = LoggerFactory.getLogger(DatabaseNotificationSchedulerService.class);

    DatabaseNotificationSchedulerService(String type) {
        this.basicDataSource = new BasicDataSource();
        this.basicDataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        this.basicDataSource.setUrl("jdbc:hsqldb:" + type
                + ":" + CommandLineArgs.dbPath);
        this.basicDataSource.setUsername(CommandLineArgs.dbUser);
        this.basicDataSource.setPassword(CommandLineArgs.dbPass);

    }

    @Override
    public void start() {

        if(! isRunning) {
            try {
                // Create table for db-scheduler
                basicDataSource.getConnection().createStatement()
                        .executeUpdate(
                                "create table if not exists scheduled_tasks (\n" +
                                        "  task_name varchar(40) not null,\n" +
                                        "  task_instance varchar(240) not null,\n" +
                                        "  task_data blob,\n" +
                                        "  execution_time timestamp(6) not null,\n" +
                                        "  picked BOOLEAN not null,\n" +
                                        "  picked_by varchar(50),\n" +
                                        "  last_success timestamp(6) null,\n" +
                                        "  last_failure timestamp(6) null,\n" +
                                        "  last_heartbeat timestamp(6) null,\n" +
                                        "  version BIGINT not null,\n" +
                                        "  PRIMARY KEY (task_name, task_instance)\n" +
                                        ")");

            } catch (SQLException e) {
                e.printStackTrace();
                stop();
            }
            notificationOneTimeTask = Tasks.oneTime(TASK_NAME, Notification.class).execute(
                    (inst, ctx) -> {
                        CcsClientWrapper.getInstance().sendNotification(inst.getData());
                    }
            );

            scheduler = Scheduler
                    .create(basicDataSource, notificationOneTimeTask)
                    .threads(5)
                    .build();

            scheduler.start();

            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

            isRunning = true;
        } else {
            logger.warn("Cannot start an instance of {} when it is already running.", this.getClass().getName());
        }

    }

    @Override
    public void stop() {

        if(isRunning) {
            scheduler.stop();

            try {
                basicDataSource.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            isRunning = false;
        } else {
            logger.warn("Cannot stop an instance of {} when it is not running.", this.getClass().getName());
        }
    }

    @Override
    public void schedule(String from, Map<String, String> payload) {
        // TODO add delimter in taskId and split for individual ids
        if(isRunning) {
            Notification notification = Notification.getNotification(from, payload);
            String taskId = notification.getRecepient() + notification.getSubjectId() + UUID.randomUUID();
            scheduler.schedule(notificationOneTimeTask.instance(taskId, notification), notification.getScheduledTime().toInstant());
            logger.info("Task scheduled for notification {}", notification);
        } else {
            logger.warn("Cannot schedule using an instance of {} when it is not running. Please start the service first.", this.getClass().getName());
        }
    }

    @Override
    public void cancelUsingFcmToken(String from) {
        cancelAllTasks(from);
    }

    @Override
    public void cancelUsingCustomId(String id) {
        cancelAllTasks(id);
    }

    private void cancelAllTasks(String partTaskId) {
        if(isRunning) {
            scheduler.getScheduledExecutions(taskScheduledExecution -> {
                if (taskScheduledExecution.getTaskInstance().getId().contains(partTaskId)) {
                    logger.info("Removing the scheduled task with id {}", taskScheduledExecution.getTaskInstance().getId());
                    scheduler.cancel(taskScheduledExecution.getTaskInstance());
                }
            });
        } else {
            logger.warn("Cannot cancelUsingFcmToken using an instance of {} when it is not running. Please start the service first.", this.getClass().getName());
        }
    }

    @Override
    public void updateToken(String oldToken, String newToken) {
        if(isRunning) {
            // TODO update token based on subject id
            scheduler.getScheduledExecutions(taskScheduledExecution -> {
                if (taskScheduledExecution.getTaskInstance().getId().contains(oldToken)) {
                    logger.info("Updating token on the scheduled task with id {}", taskScheduledExecution.getTaskInstance().getId());
                    Notification notification = (Notification) (taskScheduledExecution.getData());
                    if(notification.getRecepient().equals(oldToken)) {
                        notification.setRecepient(newToken);
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
}
