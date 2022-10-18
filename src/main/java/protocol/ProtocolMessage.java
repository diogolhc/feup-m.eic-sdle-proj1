package protocol;

public abstract class ProtocolMessage implements ProtocolMessageInterface {
    protected String id;

    public ProtocolMessage(String id) {
        this.id = id;
    }
}
