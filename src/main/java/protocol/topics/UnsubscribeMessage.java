package protocol.topics;

// UNSUBSCRIBE <ID> <TOPIC> CRLF CRLF
public class UnsubscribeMessage extends TopicsMessage {
    public UnsubscribeMessage(String id, String topic) {
        super(id, topic);
    }

    @Override
    public String getType() {
        return "UNSUBSCRIBE";
    }
}
