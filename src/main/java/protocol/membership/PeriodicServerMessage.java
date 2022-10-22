package protocol.membership;

import protocol.ProtocolMessage;

import java.util.List;

// PERIODIC_SERVER <ID> CRLF CRLF [<topic1> ...]
public class PeriodicServerMessage extends ProtocolMessage {
    public final static String TYPE = "PERIODIC_SERVER";

    private final List<String> topics;

    public PeriodicServerMessage(String id, List<String> topics) {
        super(id);
        this.topics = topics;
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
