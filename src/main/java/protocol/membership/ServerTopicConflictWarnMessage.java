package protocol.membership;

import protocol.ProtocolMessage;

// TOPIC_CONFLICT <ID> <TOPIC> <SERVER_CONFLICT_ID> CRLF CRLF
public class ServerTopicConflictWarnMessage extends ProtocolMessage {
    public final static String TYPE = "TOPIC_CONFLICT";

    private final String topic;
    private final String serverConflict;

    public ServerTopicConflictWarnMessage(String id, String topic, String serverConflict) {
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
}
