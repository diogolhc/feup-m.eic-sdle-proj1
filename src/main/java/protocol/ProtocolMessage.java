package protocol;

public abstract class ProtocolMessage {
    protected String id;

    public ProtocolMessage(String id) {
        this.id = id;
    }

    public abstract String toString();
}
