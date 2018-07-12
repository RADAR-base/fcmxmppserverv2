package org.radarcns.xmppserver.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Notification {

    private String title;
    private String message;
    private String recepient;

    private Date scheduledTime;

    public Notification(String title, String message, Date date, String recepient){
        this.title = title;
        this.message = message;
        this.scheduledTime = date;
        this.recepient = recepient;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;
        Notification that = (Notification) o;
        return Objects.equals(getTitle(), that.getTitle()) &&
                Objects.equals(getMessage(), that.getMessage()) &&
                Objects.equals(getRecepient(), that.getRecepient()) &&
                Objects.equals(getScheduledTime(), that.getScheduledTime());
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
                '}';
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();

        map.put("title", title);
        map.put("message", message);
        map.put("recepient", recepient);
        map.put("scheduledTime", scheduledTime.toString());

        return map;
    }
}
