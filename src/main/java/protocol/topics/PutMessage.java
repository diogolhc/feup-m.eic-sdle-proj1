package protocol.topics;

// PUT <ID> <TOPIC> CRLF CRLF <MESSAGE>
public class PutMessage extends TopicsMessage {
    public final static String TYPE = "PUT";
    private final String message;

    public PutMessage(String id, String topic, String message) {
        super(id, topic);
        this.message = message;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getBody() {
        return this.message;
    }
}
