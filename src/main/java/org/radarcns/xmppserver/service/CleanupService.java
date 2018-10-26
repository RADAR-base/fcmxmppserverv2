package org.radarcns.xmppserver.service;

import java.util.concurrent.TimeUnit;

public interface CleanupService {
    void removeNotifications(TimeUnit expiryTimeUnit, long expiry);
}
