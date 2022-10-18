package protocol.topics.reply;

import protocol.ProtocolMessage;

// STATUS <ID> <STATUS> CRLF CRLF
public class StatusMessage extends ProtocolMessage {
    final private static String TYPE = "STATUS";

    final private ResponseStatus status;

    public StatusMessage(String id, ResponseStatus status) {
        super(id);
        this.status = status;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return TYPE + " " + this.id + " " + this.status + " \r\n\r\n";
    }
}
