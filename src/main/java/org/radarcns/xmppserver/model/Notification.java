package org.radarcns.xmppserver.model;

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
public class Notification implements Serializable{

    private String title;
    private String message;
    private String subjectId;
    private String recepient;
    private Date scheduledTime;
    // time to live or expiry in seconds
    private int ttlSeconds;

    public Notification(String title, String message, Date date, String recepient,
                        String subjectId, int ttlSeconds){
        this.title = title;
        this.message = message;
        this.scheduledTime = date;
        this.recepient = recepient;
        this.subjectId = subjectId;
        this.ttlSeconds = ttlSeconds;
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

    public void setRecepient(String recepient) {
        this.recepient = recepient;
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

    public static Notification getNotification(Data data) {
        String datetime = data.getPayload().get("time"); // epoch timestamp in milliseconds
        String notificationTitle = data.getPayload().get("notificationTitle");
        String notificationMessage = data.getPayload().get("notificationMessage");
        String subjectId = data.getPayload().get("subjectId") == null ? "null" : data.getPayload().get("subjectId");

        // Set to max of 28 days if not set
        int ttlSeconds = data.getPayload().get("ttlSeconds") == null ? 2_419_200 : Integer.valueOf(data.getPayload().get("ttlSeconds"));

        Date date = new Date(Long.parseLong(datetime));


        return new Notification(notificationTitle, notificationMessage, date, data.getFrom(), subjectId, ttlSeconds);
    }
}
