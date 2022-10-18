package protocol.topics;

import protocol.ProtocolMessage;

// UNSUBSCRIBE <ID> <TOPIC> CRLF CRLF
public class UnsubscribeMessage extends ProtocolMessage implements TopicsMessage {
    final private static String TYPE = "UNSUBSCRIBE";

    final private String topic;


    public UnsubscribeMessage(String id, String topic) {
        super(id);
        this.topic = topic;
    }

    @Override
    public String toString() {
        return TYPE + " " + this.id + " " + this.topic + "\r\n\r\n";
    }

    @Override
    public String getTopic() {
        return this.topic;
    }
}
