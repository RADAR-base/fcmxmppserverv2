package org.radarcns.xmppserver.model;

import java.util.Objects;

/**
 * Model class that uniquely identifies a particular user.
 */
public class User {
    private final String subjectId;

    private final String fcmToken;

    public User(String subjectId, String fcmToken) {
        this.subjectId = subjectId;
        this.fcmToken = fcmToken;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(getSubjectId(), user.getSubjectId()) &&
                Objects.equals(getFcmToken(), user.getFcmToken());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getSubjectId(), getFcmToken());
    }
}
