package protocol.topics;

import protocol.ProtocolMessage;

// PUT <ID> <TOPIC> CRLF <MESSAGE> CRLF CRLF
public class PutMessage extends ProtocolMessage implements TopicsMessage {
    final private static String TYPE = "PUT";

    final private String topic;
    final private String message;


    public PutMessage(String id, String topic, String message) {
        super(id);
        this.topic = topic;
        this.message = message;
    }

    @Override
    public String toString() {
        return TYPE + " " + this.id + " " + this.topic + "\r\n" + this.message + "\r\n\r\n";
    }

    @Override
    public String getTopic() {
        return this.topic;
    }
}
