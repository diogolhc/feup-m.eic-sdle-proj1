package protocol.topics;

import protocol.ProtocolMessageInterface;

public interface TopicsMessage extends ProtocolMessageInterface {
    public String getTopic();
}
