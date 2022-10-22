package protocol.membership;

import protocol.ProtocolMessage;

import java.util.Set;

// PERIODIC_SERVER <ID> CRLF CRLF [<topic1> ...]
public class PeriodicServerMessage extends ProtocolMessage {
    public final static String TYPE = "PERIODIC_SERVER";

    private final Set<String> topics;

    public PeriodicServerMessage(String id, Set<String> topics) {
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
