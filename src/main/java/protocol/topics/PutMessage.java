package protocol.topics;

// PUT <ID> <TOPIC> CRLF CRLF <MESSAGE> CRLF
public class PutMessage extends TopicsMessage {
    private final String message;

    public PutMessage(String id, String topic, String message) {
        super(id, topic);
        this.message = message;
    }

    @Override
    public String getType() {
        return "PUT";
    }

    @Override
    public String getBody() {
        return this.message;
    }
}
