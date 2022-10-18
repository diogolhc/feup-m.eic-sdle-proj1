package protocol;

import org.zeromq.ZMQ;
import protocol.topics.SubscribeMessage;

public abstract class ProtocolMessage implements ProtocolMessageInterface {
    protected String id;

    public ProtocolMessage(String id) {
        this.id = id;
    }
}
