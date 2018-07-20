package org.radarcns.xmppserver.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
/**
 * Notification model class for the XMPP Server
 *
 * @author yatharthranjan
 */
public class Notification {

    private String title;
    private String message;
    private String subjectId;
    private String recepient;
    private Date scheduledTime;

    public Notification(String title, String message, Date date, String recepient, String subjectId){
        this.title = title;
        this.message = message;
        this.scheduledTime = date;
        this.recepient = recepient;
        this.subjectId = subjectId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;
        Notification that = (Notification) o;
        return Objects.equals(getTitle(), that.getTitle()) &&
                Objects.equals(getMessage(), that.getMessage()) &&
                Objects.equals(getRecepient(), that.getRecepient()) &&
                Objects.equals(getScheduledTime(), that.getScheduledTime()) &&
                Objects.equals(getSubjectId(), that.getSubjectId());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getTitle(), getMessage(), getRecepient(), getScheduledTime());
    }

    @Override
    public String toString() {
        return "Notification{" +
                "title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", recepient='" + recepient + '\'' +
                ", scheduledTime=" + scheduledTime +
                ", subjectId=" + subjectId +
                '}';
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();

        map.put("title", title);
        map.put("message", message);
        map.put("recepient", recepient);
        map.put("subjectId", subjectId);
        map.put("scheduledTime", scheduledTime.toString());

        return map;
    }

    public static Notification getNotification(String to, Map<String, String> payload) {
        String datetime = payload.get("time"); // epoch timestamp in milliseconds
        String notificationTitle = payload.get("notificationTitle");
        String notificationMessage = payload.get("notificationMessage");
        String subjectId = payload.get("subjectId") == null ? "test" : payload.get("subjectId");

        Date date = new Date(Long.parseLong(datetime));


        return new Notification(notificationTitle, notificationMessage, date, to, subjectId);
    }
}
