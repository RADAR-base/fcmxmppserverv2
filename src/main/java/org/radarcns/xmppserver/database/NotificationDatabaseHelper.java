package org.radarcns.xmppserver.database;

import org.radarcns.xmppserver.model.ExpandedNotification;
import org.radarcns.xmppserver.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.List;


// TODO add periodic eviction of expired notifications from the database based on the execution_time
public class NotificationDatabaseHelper {

    private static final Logger logger = LoggerFactory.getLogger(NotificationDatabaseHelper.class);
    private final JdbcTemplate jdbcTemplate;

    public NotificationDatabaseHelper(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // TODO change the structure of the tables acc to 3rd Normal form.
    // i.e - One table for notification which also contains delivery and message id
    // and the other table will only contain subject_id, fcm_token. This will greatly reduce the number of duplicates
    // and hence increase the performance.

    public void createTableforNotification() {
        this.jdbcTemplate.execute("create table if not exists notification_info (\n" +
                "  notification_task_uuid varchar(40),\n" +
                "  title varchar(100) not null,\n" +
                "  message varchar(100) not null,\n" +
                "  execution_time varchar(30) not null,\n" +
                "  ttl_seconds BIGINT,\n" +
                "  PRIMARY KEY (notification_task_uuid),\n" +
                "  FOREIGN KEY (notification_task_uuid) REFERENCES status_info(notification_task_uuid) ON DELETE CASCADE\n" +
                ")");
        logger.debug("Created Table for notification_info");
    }

    public void createTableForStatus() {
        this.jdbcTemplate.execute("create table if not exists status_info (\n" +
                "  subject_id varchar(100) not null,\n" +
                "  fcm_token varchar(256) not null,\n" +
                "  notification_task_uuid varchar(40) not null,\n" +
                "  fcm_message_id varchar(100) not null,\n" +
                "  delivered BOOLEAN,\n" +
                "  UNIQUE (notification_task_uuid),\n" +
                "  UNIQUE (fcm_message_id),\n" +
                "  PRIMARY KEY (subject_id, fcm_token, notification_task_uuid)\n" +
                ")");
        logger.debug("Created Table for status_info");
    }

    /**
     * Finds if a particular notification exists in the database.
     * see {@link Notification#equals(Object)} to see the criteria of equality.
     *
     * @param notification {@link Notification} POJO to compare
     * @return true if exists, otherwise false
     */
    public boolean checkIfNotificationExists(Notification notification) {
        return findNotifications(notification.getSubjectId(), notification.getRecepient())
                .contains(notification);
    }

    /**
     * Finds undelivered notifications for a particular subject from the database.
     *
     * @param subjectId the subject Id of the subject
     * @param fcmToken  the FCM token for the device
     * @return {@link List} of {@link Notification}
     */
    public List<Notification> findNotifications(String subjectId, String fcmToken) {

        List<Notification> notifications = this.jdbcTemplate.query("select status_info.subject_id, status_info.fcm_token," +
                        " status_info.notification_task_uuid, notification_info.title, notification_info.ttl_seconds," +
                        " notification_info.message, notification_info.execution_time from notification_info inner join status_info" +
                        " on notification_info.notification_task_uuid = status_info.notification_task_uuid where status_info.subject_id = ?" +
                        " and status_info.fcm_token = ? and status_info.delivered = ?"
                , new Object[]{subjectId, fcmToken, false}, new NotificationRowMapper());

        return notifications;
    }

    /**
     * Finds notifications for a all the subjects from the databse.
     *
     * @return {@link List} of {@link Notification}
     */
    public List<Notification> findAllNotifications() {

        List<Notification> notifications = this.jdbcTemplate.query("select status_info.subject_id, status_info.fcm_token," +
                        " status_info.notification_task_uuid, notification_info.title, notification_info.ttl_seconds," +
                        " notification_info.message, notification_info.execution_time from notification_info inner join status_info" +
                        " on notification_info.notification_task_uuid = status_info.notification_task_uuid"
                , new NotificationRowMapper());

        return notifications;
    }


    /**
     * Finds delivered notifications for all the subjects from the databse.
     *
     * @return {@link List} of {@link Notification}
     */
    public List<ExpandedNotification> findAllDeliveredNotifications() {

        List<ExpandedNotification> notifications = this.jdbcTemplate.query("select status_info.subject_id, status_info.fcm_token," +
                        " status_info.notification_task_uuid, status_info.fcm_message_id, status_info.delivered, notification_info.title, notification_info.ttl_seconds," +
                        " notification_info.message, notification_info.execution_time from notification_info inner join status_info" +
                        " on notification_info.notification_task_uuid = status_info.notification_task_uuid where status_info.delivered = ?", new Object[]{true}
                , new ExpandedNotificationRowMapper());

        return notifications;
    }

    public void addNotification(Notification notification, String messageId, String taskId) {
        int result1 = this.jdbcTemplate.update("insert into status_info values (?,?,?,?,?)",
                notification.getSubjectId(),
                notification.getRecepient(),
                taskId, messageId, false);

        int result2 = this.jdbcTemplate.update("insert into notification_info values (?,?,?,?,?)",
                taskId,
                notification.getTitle(),
                notification.getMessage(),
                String.valueOf(notification.getScheduledTime().toInstant().toEpochMilli()),
                notification.getTtlSeconds()
        );

        if (result1 == 1 && result2 == 1)
            logger.debug("Added the notification : {}", notification);
    }

    public void updateDeliveryStatus(boolean status, String fcmToken, String messageId) {
        int result = this.jdbcTemplate.update("update status_info set delivered = ? where fcm_token = ? and fcm_message_id = ?",
                status, fcmToken, messageId);
        if (result == 1)
            logger.debug("Updated delivery status of message id {} and token {} to {}", messageId, fcmToken, status);
    }

    public void removeNotification(String taskId) {
        int result = this.jdbcTemplate.update("delete from status_info where notification_task_uuid = ?", taskId);

        if (result == 2)
            logger.debug("Removed notification for task id {}", taskId);
    }

    /**
     * Deletes a notification from both tables as ON DELETE CASCADE is specified.
     *
     * @param messageId Message Id of the particular notification
     * @param fcmToken  Fcm Token of the device that received/requested the notification
     */
    public void removeNotification(String messageId, String fcmToken) {
        int result = this.jdbcTemplate.update("delete from status_info" +
                " where fcm_token = ? and fcm_message_id = ?", fcmToken, messageId);
        if (result == 2)
            logger.debug("Removed notification for Message Id {} and Token {}", messageId, fcmToken);
    }

    /**
     * Removes all the notifications for a particular subject or device token.
     * This follows when a cancel request is made.
     * //TODO Send delivery info to kafka first before removing them
     *
     * @param subjectId subject ID to remove
     * @param fcmToken  FCM Token to remove the notifications
     */
    public void removeAllNotifications(String subjectId, String fcmToken) {
        int result = this.jdbcTemplate.update("delete from status_info where" +
                " subject_id = ? or fcm_token = ?", subjectId, fcmToken);
        logger.debug("{} notifications removed from database for subject={} and token={}"
                , result, subjectId, fcmToken);
    }

    /**
     * Removes all the undelivered notifications for a particular subject or device token.
     * This follows when a cancel request is made.
     *
     * @param subjectId subject ID to remove
     * @param fcmToken  FCM Token to remove the notifications
     */
    public void removeAllUndeliveredNotifications(String subjectId, String fcmToken) {
        int result = this.jdbcTemplate.update("delete from status_info where" +
                " delivered = ? and (subject_id = ? or fcm_token = ?)", false, subjectId, fcmToken);
        logger.debug("{} notifications removed from database for subject={} and token={}"
                , result, subjectId, fcmToken);
    }

    public void removeDeliveredNotificationsOlderThan(long olderThanMillis) {

    }

    /**
     * Whether a given temporal threshold is passed, compared to given time.
     */
    public static boolean isThresholdPassed(Temporal time, Duration duration) {
        return Duration.between(time, Instant.now()).compareTo(duration) > 0;
    }

    private class NotificationRowMapper implements RowMapper<Notification> {

        @Override
        public Notification mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Notification.Builder().setTitle(rs.getString("title"))
                    .setMessage(rs.getString("message"))
                    .setScheduledTime(new Date(Long.parseLong(rs.getString("execution_time"))))
                    .setRecepient(rs.getString("fcm_token"))
                    .setSubjectId(rs.getString("subject_id"))
                    .setTtlSeconds(rs.getInt("ttl_seconds"))
                    .build();
        }
    }

    private class ExpandedNotificationRowMapper implements RowMapper<ExpandedNotification> {
        @Override
        public ExpandedNotification mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ExpandedNotification.Builder(new Notification.Builder().setTitle(rs.getString("title"))
                    .setMessage(rs.getString("message"))
                    .setScheduledTime(new Date(Long.parseLong(rs.getString("execution_time"))))
                    .setRecepient(rs.getString("fcm_token"))
                    .setSubjectId(rs.getString("subject_id"))
                    .setTtlSeconds(rs.getInt("ttl_seconds"))
                    .build())
                    .delivered(rs.getBoolean("delivered"))
                    .fcmMessageId(rs.getString("fcm_message_id"))
                    .notificationTaskUuid(rs.getString("notification_task_uuid"))
                    .build();
        }
    }
}
