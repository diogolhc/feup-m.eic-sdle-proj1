package protocol.topics;

// GET <ID> <TOPIC> CRLF CRLF
public class GetMessage extends TopicsMessage {
    public final static String TYPE = "GET";

    public GetMessage(String id, String topic) {
        super(id, topic);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
