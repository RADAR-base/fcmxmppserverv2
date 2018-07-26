package org.radarcns.xmppserver.ccs;

import com.wedevol.xmpp.bean.CcsOutMessage;
import com.wedevol.xmpp.server.CcsClient;
import com.wedevol.xmpp.util.MessageMapper;
import org.radarcns.xmppserver.model.Notification;

import java.util.HashMap;
import java.util.Map;

public class CcsClientWrapper {

    private CcsClient ccsClient;

    private static CcsClientWrapper CcsClientWrapperInstance = null;

    private CcsClientWrapper(CcsClient ccsClient) {
        this.ccsClient = ccsClient;
    }

    public static CcsClientWrapper getInstance() {
        if(CcsClientWrapperInstance == null) {
            CcsClientWrapperInstance = new CcsClientWrapper(CcsClient.getInstance());
        }
        return CcsClientWrapperInstance;
    }

    public void sendNotification(Notification notification) {
        final String messageId = String.valueOf(notification.hashCode());
        final CcsOutMessage outMessage = new CcsOutMessage(notification.getRecepient(),
                messageId, notification.toMap());

        Map<String, String> notifyMap = new HashMap<>();
        notifyMap.put("title", notification.getTitle());
        notifyMap.put("body", notification.getMessage());

        outMessage.setNotificationPayload(notifyMap);

        final String jsonRequest = MessageMapper.toJsonString(outMessage);
        ccsClient.sendDownstreamMessage(messageId, jsonRequest);
    }
}
