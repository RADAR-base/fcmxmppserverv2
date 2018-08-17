package org.radarcns.xmppserver.model;


import java.io.Serializable;
import java.util.Map;

/**
 *
 * Model class for storing data payload of messages sent upstream.
 *
 * @author yatharth
 */
public class Data implements Serializable{

    private String from;
    private String messageId;
    private Map<String, String> payload;

    public Data(String from, Map<String, String> payload, String messageId) {
        this.from = from;
        this.payload = payload;
        this.messageId = messageId;
    }

    public String getFrom() {
        return from;
    }

    public Map<String, String> getPayload() {
        return payload;
    }

    public String getMessageId() {
        return messageId;
    }
}
