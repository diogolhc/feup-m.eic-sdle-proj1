package protocol.topics;

import protocol.ProtocolMessage;

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
        return List.of(this.topic);
    }
}
