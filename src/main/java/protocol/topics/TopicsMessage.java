package protocol.topics;

import protocol.ProtocolMessage;

import java.util.LinkedList;
import java.util.List;

public abstract class TopicsMessage extends ProtocolMessage {
    private final String topic;

    public TopicsMessage(String id, String topic) {
        super(id);
        this.topic = topic;
    }

    public String getTopic() {
        return this.topic;
    }

    @Override
    public List<String> getHeaderFields() {
        List<String> fields = new LinkedList<>();
        fields.add(this.topic);
        return fields;
    }
}
