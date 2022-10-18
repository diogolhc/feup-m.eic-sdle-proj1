package protocol.topics;

import protocol.ProtocolMessage;

// SUBSCRIBE <ID> <TOPIC> CRLF CRLF
public class SubscribeMessage extends ProtocolMessage implements TopicsMessage {
    final private static String TYPE = "SUBSCRIBE";

    final private String topic;


    public SubscribeMessage(String id, String topic) {
        super(id);
        this.topic = topic;
    }

    @Override
    public String getTopic() {
        return this.topic;
    }

    @Override
    public String toString() {
        return TYPE + " " + this.id + " " + this.topic + "\r\n\r\n";
    }
}
