package protocol.topics;

// UNSUBSCRIBE <ID> <TOPIC> CRLF CRLF
public class UnsubscribeMessage extends TopicsMessage {
    public final static String TYPE = "UNSUBSCRIBE";

    public UnsubscribeMessage(String id, String topic) {
        super(id, topic);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
