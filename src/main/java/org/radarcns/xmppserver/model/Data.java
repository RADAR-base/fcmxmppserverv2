package org.radarcns.xmppserver.model;


import java.util.Map;

/**
 *
 * Model class for storing data payload of messages sent upstream.
 *
 * @author yatharth
 */
public class Data {

    private String from;
    private Map<String, String> payload;

    public Data(String from, Map<String, String> payload) {
        this.from = from;
        this.payload = payload;
    }

    public String getFrom() {
        return from;
    }

    public Map<String, String> getPayload() {
        return payload;
    }
}
