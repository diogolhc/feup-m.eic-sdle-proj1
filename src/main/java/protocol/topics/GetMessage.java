package protocol.topics;

import protocol.ProtocolMessage;

// GET <ID> <TOPIC> CRLF CRLF
public class GetMessage extends ProtocolMessage {
    final private static String TYPE = "GET";

    final private String topic;


    public GetMessage(String id, String topic) {
        super(id);
        this.topic = topic;
    }

    @Override
    public String toString() {
        return TYPE + " " + this.id + " " + this.topic + "\r\n\r\n";
    }
}
