package protocol.topics;

// PUT <ID> <TOPIC> CRLF CRLF <MESSAGE>
public class PutMessage extends TopicsMessage {
    public final static String TYPE = "PUT";
    private final String message;
    private final Integer counter;

    public PutMessage(String id, String topic, String message, Integer counter) {
        super(id, topic);
        this.message = message;
        this.counter = counter;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getBody() {
        return this.message;
    }

    public Integer getCounter() {
        return this.counter;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\r\n\r\n");
        sb.append(this.counter);

        return super.toString() + sb.toString();
    }
}
