package org.radarcns.xmppserver.model;


import java.util.Map;

/**
 *
 * Model class for storing data payload of messages sent upstream.
 *
 * @author yatharth
 */
public class Data {
    // TODO add data fields

    String from;
    Map<String, String> payload;

    public String getFrom() {
        return from;
    }

    public Map<String, String> getPayload() {
        return payload;
    }
}
