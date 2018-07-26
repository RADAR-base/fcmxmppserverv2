package org.radarcns.xmppserver.service;

import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import org.apache.commons.dbcp.BasicDataSource;
import org.radarcns.xmppserver.ccs.CcsClientWrapper;
import org.radarcns.xmppserver.commandline.CommandLineArgs;
import org.radarcns.xmppserver.config.DbConfig;
import org.radarcns.xmppserver.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

/**
 * Database notification scheduler service class for the XMPP Server
 * This has functionality for both in-memory and persisent data bases.
 *
 * @author yatharthranjan
 */
public class DatabaseNotificationSchedulerService implements NotificationSchedulerService {

    private Scheduler scheduler;
    private BasicDataSource basicDataSource;
    private OneTimeTask<Notification> notificationOneTimeTask;

    private static final String TASK_NAME = "notification-one-time";
    private static final Logger logger = LoggerFactory.getLogger(DatabaseNotificationSchedulerService.class);

    private static DatabaseNotificationSchedulerService INSTANCE = null;

    private DatabaseNotificationSchedulerService(DbConfig dbConfig) {
        this.basicDataSource = new BasicDataSource();
        this.basicDataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        this.basicDataSource.setUrl("jdbc:hsqldb:" + dbConfig.getDbType()
                + DbConfig.DB_PATH_SEPARATOR + dbConfig.getDbPath());
        this.basicDataSource.setUsername(CommandLineArgs.dbUser);
        this.basicDataSource.setPassword(CommandLineArgs.dbPass);

    }

    public static NotificationSchedulerService getInstanceForCofig(DbConfig dbConfig) {
        if(INSTANCE == null) {
            INSTANCE = new DatabaseNotificationSchedulerService(dbConfig);
        }
        return INSTANCE;
    }

    @Override
    public void start() {

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

    }

    @Override
    public void stop() {
        scheduler.stop();

        try {
            basicDataSource.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void schedule(String from, Map<String, String> payload) {

        // TODO add subject ID in the task instance id
        Notification notification = Notification.getNotification(from, payload);
        scheduler.schedule(notificationOneTimeTask.instance(notification.getRecepient() + UUID.randomUUID(), notification), notification.getScheduledTime().toInstant());
        logger.info("Task scheduled for notification {}", notification);
    }

    @Override
    public void cancel(String from) {
        // TODO add the subject ID in the task instance id
        scheduler.getScheduledExecutions(taskScheduledExecution -> {
            if(taskScheduledExecution.getTaskInstance().getId().contains(from)) {
                logger.info("Removing the scheduled task with id {}", taskScheduledExecution.getTaskInstance().getId());
                scheduler.cancel(taskScheduledExecution.getTaskInstance());
            }
        });
    }

    @Override
    public void updateToken(String oldToken, String newToken) {
        scheduler.getScheduledExecutions(taskScheduledExecution -> {
            if(taskScheduledExecution.getTaskInstance().getId().contains(oldToken)) {
                logger.info("Updating token on the scheduled task with id {}", taskScheduledExecution.getTaskInstance().getId());
                Notification notification = (Notification)(taskScheduledExecution.getData());
                notification.setRecepient(newToken);
            }
        });
    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
