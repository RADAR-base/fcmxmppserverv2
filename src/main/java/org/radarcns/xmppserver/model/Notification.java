package org.radarcns.xmppserver.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Notification model class for the XMPP Server
 *
 * @author yatharthranjan
 */
public class Notification implements Serializable {
    private static final long serialVersionUID = 3284965300642907379L;

    private final String title;
    private final String message;
    private final String subjectId;
    private final String recepient;
    private final Date scheduledTime;
    // time to live or expiry in seconds
    private int ttlSeconds;
    private static final Logger logger = LoggerFactory.getLogger(Notification.class);

    private Notification(Builder builder) {
        this.title = builder.title;
        this.message = builder.message;
        this.scheduledTime = builder.scheduledTime;
        this.recepient = builder.recepient;
        this.subjectId = builder.subjectId;
        this.ttlSeconds = builder.ttlSeconds;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public Date getScheduledTime() {
        return scheduledTime;
    }

    public String getRecepient() {
        return recepient;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public int getTtlSeconds() {
        return ttlSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;
        Notification that = (Notification) o;
        return Objects.equals(getTitle(), that.getTitle()) &&
                Objects.equals(getMessage(), that.getMessage()) &&
                Objects.equals(getRecepient(), that.getRecepient()) &&
                Objects.equals(getScheduledTime(), that.getScheduledTime()) &&
                Objects.equals(getSubjectId(), that.getSubjectId()) &&
                Objects.equals(getTtlSeconds(), that.getTtlSeconds());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getTitle(), getMessage(), getRecepient(), getScheduledTime(), getTtlSeconds());
    }

    @Override
    public String toString() {
        return "Notification{" +
                "title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", recepient='" + recepient + '\'' +
                ", scheduledTime=" + scheduledTime +
                ", subjectId=" + subjectId +
                ", ttlSeconds=" + ttlSeconds +
                '}';
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();

        map.put("title", title);
        map.put("message", message);
        map.put("recepient", recepient);
        map.put("subjectId", subjectId);
        map.put("scheduledTime", scheduledTime.toString());
        map.put("ttlSeconds", String.valueOf(ttlSeconds));

        return map;
    }

    public static Notification getNotification(String to, Map<String, String> payload) {
        String datetime = payload.get("time"); // epoch timestamp in milliseconds
        String notificationTitle = payload.get("notificationTitle");
        String notificationMessage = payload.get("notificationMessage");
        String subjectId = payload.get("subjectId") == null ? "null" : payload.get("subjectId");

        int ttlSeconds = 2_419_200;
        try {
            // Set to max of 28 days if not set
            ttlSeconds = payload.get("ttlSeconds") == null ? 2_419_200 : Integer.valueOf(payload.get("ttlSeconds"));
        } catch(NumberFormatException exc) {
            logger.error("TTL seconds value is invalid: ", exc);
        }

        Date date;
        try {
            // First parsed as Double to avoid any errors with decimal points
            date = new Date(Double.valueOf(datetime).longValue());
        } catch(NumberFormatException exc) {
            throw new IllegalArgumentException("The format for the Date Time is not correct.", exc);
        }

        return new Notification.Builder().setTitle(notificationTitle)
                .setMessage(notificationMessage).setScheduledTime(date).setRecepient(to)
                .setSubjectId(subjectId).setTtlSeconds(ttlSeconds).build();
    }


    public static class Builder {
        private String title;
        private String message;
        private String subjectId;
        private String recepient;
        private Date scheduledTime;
        // time to live or expiry in seconds
        private int ttlSeconds;

        public Builder() {
            // Do nothing
        }

        public Builder(Notification notification) {
            this.title = notification.title;
            this.message = notification.message;
            this.scheduledTime = notification.scheduledTime;
            this.recepient = notification.recepient;
            this.subjectId = notification.subjectId;
            this.ttlSeconds = notification.ttlSeconds;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setSubjectId(String subjectId) {
            this.subjectId = subjectId;
            return this;
        }

        public Builder setRecepient(String recepient) {
            this.recepient = recepient;
            return this;
        }

        public Builder setScheduledTime(Date scheduledTime) {
            this.scheduledTime = scheduledTime;
            return this;
        }

        public Builder setTtlSeconds(int ttlSeconds) {
            this.ttlSeconds = ttlSeconds;
            return this;
        }

        public Notification build() {
            return new Notification(this);
        }
    }
}
