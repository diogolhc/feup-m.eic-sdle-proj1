package protocol.topics;

import java.util.List;

// PUT <ID> <TOPIC> <COUNTER> CRLF CRLF <MESSAGE>
public class PutMessage extends TopicsMessage {
    public final static String TYPE = "PUT";
    private final String message;
    private final Integer counter;

    public PutMessage(String id, String topic, Integer counter, String message) {
        super(id, topic);
        this.counter = counter;
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

    public Integer getCounter() {
        return this.counter;
    }

    @Override
    public List<String> getHeaderFields() {
        List<String> fields = super.getHeaderFields();
        fields.add(Integer.toString(this.counter));
        return fields;
    }
}
