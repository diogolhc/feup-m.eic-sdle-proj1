package protocol.topics.reply;

import protocol.ProtocolMessage;

// STATUS <ID> <STATUS> CRLF CRLF
public class StatusMessage extends ProtocolMessage {
    final private static String TYPE = "STATUS";
    public enum Status {
        OK,
        ALREADY_SUBSCRIBED
    }

    final private Status status;

    public StatusMessage(String id, Status status) {
        super(id);
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return TYPE + " " + this.id + " " + this.status + " \r\n\r\n";
    }
}
