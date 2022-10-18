package protocol;

import org.zeromq.ZMQ;

public class MessageParser {
    private final String message;

    public MessageParser(String message) {
        this.message = message;
    }

    public MessageParser(byte[] message) {
        this(new String(message, ZMQ.CHARSET));
    }

    public ProtocolMessage getMessage() {
        // TODO
        return null;
    }
}
