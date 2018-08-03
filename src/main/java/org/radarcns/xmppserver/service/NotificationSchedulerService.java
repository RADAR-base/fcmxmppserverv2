package org.radarcns.xmppserver.service;

import org.radarcns.xmppserver.model.Data;

import java.util.List;
import java.util.Map;

public interface NotificationSchedulerService {

    void start();

    void stop();

    void schedule(String from, Map<String, String> payload);

    void schedule(List<Data> data);

    void cancelUsingFcmToken(String from);

    void cancelUsingCustomId(String id);

    void updateToken(String oldToken, String newToken);

    boolean isRunning();

}
