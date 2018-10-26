package org.radarcns.xmppserver.service;

import org.radarcns.xmppserver.model.Data;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface NotificationSchedulerService {

    void start();

    void stop();

    void schedule(Data data);

    void schedule(Collection<Data> data);

    void cancelUsingFcmToken(String from);

    void cancelUsingCustomId(String id);

    void updateToken(String oldToken, String newToken);

    void confirmDelivery(String messageId, String token);

    boolean isRunning();

    long getNumberOfScheduledNotifications();

}
