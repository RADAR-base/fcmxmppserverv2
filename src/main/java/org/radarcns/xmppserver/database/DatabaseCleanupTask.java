package org.radarcns.xmppserver.database;

public interface DatabaseCleanupTask {
    public void startCleanup();

    public void stopCleanup();
}
