package org.radarcns.xmppserver.model;


import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Model class for storing data payload of messages sent upstream.
 *
 * @author yatharth
 */
public class Data implements Serializable {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Data)) return false;
        Data data = (Data) o;
        return Objects.equals(getFrom(), data.getFrom()) &&
                Objects.equals(getMessageId(), data.getMessageId()) &&
                Objects.equals(getPayload(), data.getPayload());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getFrom(), getMessageId(), getPayload());
    }
}
