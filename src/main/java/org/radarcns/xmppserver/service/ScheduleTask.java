package org.radarcns.xmppserver.service;

import com.wedevol.xmpp.bean.CcsOutMessage;
import com.wedevol.xmpp.server.CcsClient;
import com.wedevol.xmpp.util.MessageMapper;
import org.radarcns.xmppserver.model.Notification;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduleTask {

    public static ScheduledFuture<?> scheduleForDate(CcsClient ccsClient, Notification notification){
        Date now = new Date();
        long delay = notification.getScheduledTime().getTime() - now.getTime();
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        return ses.schedule(() -> {

            final String messageId = String.valueOf(notification.hashCode());
            final CcsOutMessage outMessage = new CcsOutMessage(notification.getRecepient(),
                    messageId, notification.toMap());

            String jsonNotification = "{\"title\":\""+ notification.getTitle() +"\"," +
                    "\"body\":\""+ notification.getMessage() +"\"," +
                    "\"click_action\":\"android.intent.action.MAIN\"}";

            Map<String, String> notifyMap = new HashMap<>();
            notifyMap.put("title", notification.getTitle());
            notifyMap.put("body", notification.getMessage());
            //notifyMap.put("click_action", "org.radarcns.action.OPEN_NOTIFICATION");

            outMessage.setNotificationPayload(notifyMap);

            final String jsonRequest = MessageMapper.toJsonString(outMessage);
            ccsClient.sendDownstreamMessage(messageId, jsonRequest);

            NotificationSchedulerService.logger.info("Sent downstream Scheduled message with title: {}", notification.getTitle());

        }, delay, TimeUnit.MILLISECONDS);
    }
}
