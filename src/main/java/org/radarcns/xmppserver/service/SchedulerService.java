package org.radarcns.xmppserver.service;

import java.util.Map;

public interface SchedulerService {

    void start();

    void stop();

    void schedule(String from, Map<String, String> payload);

    void cancel(String from);

    void updateToken(String oldToken, String newToken);

    boolean isRunning();

}
