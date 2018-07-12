package org.radarcns.xmppserver.model;

import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

public class ScheduleTaskDetails {

    private ScheduledFuture<?> scheduledFuture;

    private String token;

    public ScheduleTaskDetails(ScheduledFuture<?> scheduledFuture, String token) {
        this.scheduledFuture = scheduledFuture;
        this.token = token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScheduleTaskDetails)) return false;
        ScheduleTaskDetails that = (ScheduleTaskDetails) o;
        return Objects.equals(scheduledFuture, that.scheduledFuture) &&
                Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {

        return Objects.hash(scheduledFuture, token);
    }

    @Override
    public String toString() {
        return "ScheduleTaskDetails{" +
                "scheduledFuture=" + scheduledFuture +
                ", token='" + token + '\'' +
                '}';
    }

    public ScheduledFuture<?> getScheduledFuture() {
        return scheduledFuture;
    }

    public String getToken() {
        return token;
    }
}
