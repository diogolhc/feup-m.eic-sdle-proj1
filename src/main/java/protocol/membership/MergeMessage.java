package protocol.membership;

import protocol.ProtocolMessage;

import java.util.LinkedList;
import java.util.List;

// MERGE <ID> <TOPIC> <SERVER_CONFLICT_ID> CRLF CRLF
public class MergeMessage extends ProtocolMessage {
    public final static String TYPE = "MERGE";

    private final String topic;
    private final String serverConflict;

    public MergeMessage(String id, String topic, String serverConflict) {
        super(id);
        this.topic = topic;
        this.serverConflict = serverConflict;
    }

    public String getTopic() {
        return this.topic;
    }

    public String getServerConflict() {
        return this.serverConflict;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public List<String> getHeaderFields() {
        List<String> headerFields = new LinkedList<>();
        headerFields.add(this.topic);
        headerFields.add(this.serverConflict);
        return headerFields;
    }
}
