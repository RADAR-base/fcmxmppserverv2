package org.radarcns.xmppserver.util;

import org.radarcns.xmppserver.database.NotificationDatabaseHelper;
import org.radarcns.xmppserver.model.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class MockDataProducer {

    private int numOfTimes;
    private int numOfRecords;
    private int numOfUsers;

    public MockDataProducer(int numOfTimes, int numOfRecords, int numOfUsers) {
        this.numOfTimes = numOfTimes;
        this.numOfRecords = numOfRecords;
        this.numOfUsers = numOfUsers;
    }

    public void generateDataToCache(ScheduleCache cache) throws InterruptedException {
        int id = 0;
        String subjectId = "1";
        String fcmToken = "1";
        /**
         * Run for 5 seconds with ingesting 1000 records from different users per second
         */
        for(int j = 0; j < numOfTimes; j++) {
            for (int i = 0; i < numOfRecords; i++) {
                Map<String, String> dataPayload = new HashMap<>();
                if (i % (numOfRecords/numOfUsers) == 0) {
                    subjectId = String.valueOf(++id);
                    fcmToken = subjectId;
                }
                dataPayload.put("subjectId", subjectId);
                dataPayload.put("notificationTitle", "Schedule ID - " + i + "," + j);
                dataPayload.put("notificationMessage", "Everytime it is same");
                dataPayload.put("time", String.valueOf(Instant.now().plusSeconds(6000).toEpochMilli()));
                dataPayload.put("ttlSeconds", "600");

                Data data = new Data(fcmToken, dataPayload, "message - " + i + "," + j);
                cache.add(data);
            }
            Thread.sleep(1000);
            id = 0;
        }
    }

    public boolean verifyData(NotificationDatabaseHelper databaseHelper) {
        long numberOfRecordsPerUser = numOfTimes * (numOfRecords/numOfUsers);
        for(int i = 1; i <= numOfUsers; i++) {
            if(numberOfRecordsPerUser != databaseHelper.findNotifications(String.valueOf(i), String.valueOf(i)).size()) {
                return false;
            }
        }

        return true;
    }

    public void removeData(NotificationDatabaseHelper databaseHelper) {
        for(int i = 1; i <= numOfUsers; i++) {
            databaseHelper.removeAllNotifications(String.valueOf(i), String.valueOf(i));
        }
    }
}
