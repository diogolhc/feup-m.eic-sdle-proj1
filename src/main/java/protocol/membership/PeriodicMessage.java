package protocol.membership;

import protocol.ProtocolMessage;

import java.util.Set;

// PERIODIC <ID> CRLF CRLF [<topic1> ...]
public class PeriodicMessage extends ProtocolMessage {
    public final static String TYPE = "PERIODIC";

    private final Set<String> topics;

    public PeriodicMessage(String id, Set<String> topics) {
        super(id);
        this.topics = topics;
    }

    public Set<String> getTopics() {
        return this.topics;
    }

    @Override
    public String getBody() {
        return String.join(" ", this.topics);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
