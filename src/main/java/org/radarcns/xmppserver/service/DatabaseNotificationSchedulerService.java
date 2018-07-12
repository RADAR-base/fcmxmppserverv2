package org.radarcns.xmppserver.service;

import com.wedevol.xmpp.server.CcsClient;

import java.util.Map;

public class DatabaseNotificationSchedulerService implements SchedulerService {

    private CcsClient ccsClient;

    @Override
    public void start() {
        // TODO intitialize db
    }

    @Override
    public void stop() {
        // TODO close db
    }

    @Override
    public void schedule(String from, Map<String, String> payload) {
        // TODO add notifications to database and schedule
    }

    @Override
    public void cancel(String from) {
        // TODO remove notifications from database and cancel
    }

    @Override
    public void updateToken(String oldToken, String newToken) {
        
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    public DatabaseNotificationSchedulerService getInstance() {
        return null;
    }
}
