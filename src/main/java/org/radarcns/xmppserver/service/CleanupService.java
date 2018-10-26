package org.radarcns.xmppserver.service;

public interface CleanupService {
    void removeNotifications(long expiry);
}
