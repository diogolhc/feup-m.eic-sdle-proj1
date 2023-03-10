package protocol.topics;

// SUBSCRIBE <ID> <TOPIC> CRLF CRLF
public class SubscribeMessage extends TopicsMessage {
    public final static String TYPE = "SUBSCRIBE";

    public SubscribeMessage(String id, String topic) {
        super(id, topic);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
