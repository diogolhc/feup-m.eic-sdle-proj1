package protocol.topics.reply;

import protocol.ProtocolMessage;

// STATUS <ID> CRLF <MESSAGE> CRLF CRLF
public class GetResponseMessage extends ProtocolMessage {
    final private static String TYPE = "STATUS";
    final private String message;

    public GetResponseMessage(String id, String message) {
        super(id);
        this.message = message;
    }

    @Override
    public String toString() {
        return TYPE + " " + this.id + "\r\n" + this.message + "\r\n\r\n";
    }
}
