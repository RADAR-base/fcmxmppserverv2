package org.radarcns.xmppserver.service;

import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.wedevol.xmpp.server.CcsClient;
import org.apache.commons.dbcp.BasicDataSource;
import org.radarcns.xmppserver.ccs.CcsClientWrapper;
import org.radarcns.xmppserver.commandline.CommandLineArgs;
import org.radarcns.xmppserver.config.DbConfig;
import org.radarcns.xmppserver.model.Notification;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Database notification scheduler service class for the XMPP Server
 * This has functionality for both in-memory and persisent data bases.
 *
 * @author yatharthranjan
 */
public class DatabaseNotificationSchedulerService implements NotificationSchedulerService {

    private Scheduler scheduler;

    private BasicDataSource basicDataSource;

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
        // TODO intitialize db

        try {
            // Create table for db-scheduler
            basicDataSource.getConnection().createStatement()
                    .executeUpdate(
                            "create table if not exists scheduled_tasks (\n" +
                                    "  task_name varchar(40) not null,\n" +
                                    "  task_instance varchar(40) not null,\n" +
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

            // TODO Create a new table for storing FCM tokens and Foreign keys as tasks
            basicDataSource.getConnection().createStatement()
                    .executeUpdate(
                            ""
                    );

        } catch (SQLException e) {
            e.printStackTrace();
            stop();
        }
    }

    @Override
    public void stop() {
        // TODO close db

        try {
            basicDataSource.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void schedule(String from, Map<String, String> payload) {
        // TODO add notifications to database and schedule

        Notification notification = Notification.getNotification(from, payload);
        OneTimeTask<Notification> notificationOneTimeTask = Tasks.oneTime(notification.getSubjectId() + notification.getRecepient(), Notification.class).execute(
                (inst, ctx) -> {
                    CcsClientWrapper.getInstance().sendNotification(inst.getData());
                }
        );
    }

    @Override
    public void cancel(String from) {
        // TODO remove notifications from database and cancel
    }

    @Override
    public void updateToken(String oldToken, String newToken) {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    public DatabaseNotificationSchedulerService getInstance() {
        return null;
    }
}
