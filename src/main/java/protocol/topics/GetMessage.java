package protocol.topics;

// GET <ID> <TOPIC> CRLF CRLF
public class GetMessage extends TopicsMessage {
    public GetMessage(String id, String topic) {
        super(id, topic);
    }

    @Override
    public String getType() {
        return "GET";
    }
}
