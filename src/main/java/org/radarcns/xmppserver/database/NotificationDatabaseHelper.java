package org.radarcns.xmppserver.database;

import org.radarcns.xmppserver.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class NotificationDatabaseHelper {

    private static final Logger logger = LoggerFactory.getLogger(NotificationDatabaseHelper.class);
    private JdbcTemplate jdbcTemplate;

    public NotificationDatabaseHelper(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    // TODO change the structure of the tables acc to 3rd Normal form.

    public void createTableforNotification() {
        this.jdbcTemplate.execute("create table if not exists notification_info (\n" +
                "  notification_task_uuid varchar(40),\n" +
                "  title varchar(100) not null,\n" +
                "  message varchar(100) not null,\n" +
                "  execution_time varchar(30) null,\n" +
                "  ttl_seconds BIGINT null,\n" +
                "  PRIMARY KEY (notification_task_uuid)\n" +
                ")");
        logger.debug("Created Table for notification_info");
    }

    public void createTableForStatus() {
        this.jdbcTemplate.execute("create table if not exists status_info (\n" +
                "  subject_id varchar(100) not null,\n" +
                "  fcm_token varchar(256) not null,\n" +
                "  notification_task_uuid varchar(40),\n" +
                "  fcm_message_id varchar(100) UNIQUE,\n" +
                "  delivered BOOLEAN null,\n" +
                "  PRIMARY KEY (subject_id, fcm_token, notification_task_uuid),\n" +
                "  FOREIGN KEY (notification_task_uuid) REFERENCES notification_info(notification_task_uuid)\n" +
                ")");
        logger.debug("Created Table for status_info");
    }

    /**
     * Finds if a particular notification exists in the database.
     * @param notification {@link Notification} pojo to compare
     * @return true if exists, otherwise false
     */
    public boolean checkIfNotificationExists(Notification notification) {

        List<Notification> notifications = this.jdbcTemplate.query("select status_info.subject_id, status_info.fcm_token," +
                        " status_info.notification_task_uuid, notification_info.title, notification_info.ttl_seconds," +
                        " notification_info.message, notification_info.execution_time from notification_info inner join status_info" +
                        " on notification_info.notification_task_uuid = status_info.notification_task_uuid where status_info.subject_id = ?" +
                        " and status_info.fcm_token = ?"
                , new Object[]{notification.getSubjectId(), notification.getRecepient()}
                , (rs, rowNum) -> new Notification.Builder().setTitle(rs.getString("title"))
                        .setMessage(rs.getString("message"))
                        .setScheduledTime(new Date(Long.parseLong(rs.getString("execution_time"))))
                        .setRecepient(rs.getString("fcm_token"))
                        .setSubjectId(rs.getString("subject_id"))
                        .setTtlSeconds(rs.getInt("ttl_seconds"))
                        .build());
        logger.debug("Notifications found: {}", Arrays.toString(notifications.toArray()));
        return notifications.contains(notification);
    }

    public void addNotification(Notification notification, String messageId, String taskId) {
        int result1 = this.jdbcTemplate.update("insert into notification_info values (?,?,?,?,?)",
                taskId,
                notification.getTitle(),
                notification.getMessage(),
                String.valueOf(notification.getScheduledTime().toInstant().toEpochMilli()),
                notification.getTtlSeconds()
        );
        int result2 = this.jdbcTemplate.update("insert into status_info values (?,?,?,?,?)",
                notification.getSubjectId(),
                notification.getRecepient(),
                taskId, messageId, false);
        if(result1 == 1 && result2 == 1)
            logger.debug("Added the notification : {}", notification);
    }

    public void updateDeliveryStatus(boolean status, String fcmToken, String messageId) {
        int result = this.jdbcTemplate.update("update status_info set delivered = ? where fcm_token = ? and fcm_message_id = ?",
                status, fcmToken, messageId);
        if(result == 1)
            logger.debug("Updated delivery status of message id {} and token {} to {}", messageId, fcmToken, status);
    }

    public void removeNotification(String taskId) {
        int result = this.jdbcTemplate.update("delete from status_info where notification_task_uuid = ?", taskId);
        result += this.jdbcTemplate.update("delete from notification_info where notification_task_uuid = ?", taskId);

        if(result == 2)
            logger.debug("Removed notification for task id {}", taskId);
    }

    public void removeNotification(String messageId, String fcmToken) {
        int result = this.jdbcTemplate.update("delete from status_info, notification_info from notification_info" +
                        " inner join status_info on notification_info.notification_task_uuid = status_info.notification_task_uuid" +
                        " where status_info.fcm_token = ? or status_info.fcm_message_id= ?", fcmToken, messageId);
        if(result == 1)
            logger.debug("Removed notification for Message Id {} and Token {}", messageId, fcmToken);
    }

    /**
     * Removes all the notifications for a particular subject and device token.
     * This follows when a cancel request is made.
     * //TODO Send delivery info to kafka first before removing them
     * @param subjectId subject ID to remove
     * @param fcmToken FCM Token to remove the notifications
     */
    public void removeAllNotifications(String subjectId, String fcmToken) {
        int result = this.jdbcTemplate.update("delete from status_info, notification_info from notification_info" +
                " inner join status_info on notification_info.notification_task_uuid = status_info.notification_task_uuid" +
                " where status_info.subject_id = ? or status_info.fcm_token = ?", subjectId, fcmToken);
        logger.debug("{} notifications removed from database for subject={} and token={}", result, subjectId, fcmToken);
    }
}
