package org.radarcns.xmppserver.model;

public class ExpandedNotification {

    private String notificationTaskUuid;
    private boolean delivered;
    private String fcmMessageId;
    private Notification notification;

    public String getNotificationTaskUuid() {
        return notificationTaskUuid;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public String getFcmMessageId() {
        return fcmMessageId;
    }

    public Notification getNotification() {
        return notification;
    }

    private ExpandedNotification(Builder builder) {
        this.notificationTaskUuid = builder.notificationTaskUuid;
        this.delivered = builder.delivered;
        this.fcmMessageId = builder.fcmMessageId;
        this.notification = builder.notification;
    }

    public static class Builder {
        private String notificationTaskUuid;
        private boolean delivered;
        private String fcmMessageId;
        private Notification notification;

        public Builder(Notification notification) {
            this.notification = notification;
        }

        public Builder delivered(boolean delivered) {
            this.delivered = delivered;
            return this;
        }

        public Builder notificationTaskUuid(String notificationTaskUuid) {
            this.notificationTaskUuid = notificationTaskUuid;
            return this;
        }

        public Builder fcmMessageId(String fcmMessageId) {
            this.fcmMessageId = fcmMessageId;
            return this;
        }

        public ExpandedNotification build() {
            return new ExpandedNotification(this);
        }
    }

    @Override
    public String toString() {
        return "ExpandedNotification{" +
                "notificationTaskUuid='" + notificationTaskUuid + '\'' +
                ", delivered=" + delivered +
                ", fcmMessageId='" + fcmMessageId + '\'' +
                ", notification=" + notification +
                '}';
    }
}
