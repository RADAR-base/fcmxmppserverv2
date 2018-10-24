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

    public <T extends Notification> void sendNotification(T t) {
        final String messageId = String.valueOf(t.hashCode());
        final CcsOutMessage outMessage = new CcsOutMessage(t.getRecepient(),
                messageId, t.toMap());

        Map<String, String> notifyMap = new HashMap<>();
        notifyMap.put("title", t.getTitle());
        notifyMap.put("body", t.getMessage());
        notifyMap.put("sound", "default");

        outMessage.setNotificationPayload(notifyMap);
        outMessage.setTimeToLive(t.getTtlSeconds());
        outMessage.setDeliveryReceiptRequested(true);

        final String jsonRequest = MessageMapper.toJsonString(outMessage);
        ccsClient.sendDownstreamMessage(messageId, jsonRequest);
    }
}
