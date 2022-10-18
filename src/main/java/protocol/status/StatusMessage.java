package protocol.status;

import protocol.ProtocolMessage;

import java.util.List;

// STATUS <ID> <STATUS> CRLF CRLF
public class StatusMessage extends ProtocolMessage {
    private final ResponseStatus status;
    private final String message;

    public StatusMessage(String id, ResponseStatus status, String message) {
        super(id);
        this.status = status;
        this.message = message;
    }

    public StatusMessage(String id, ResponseStatus status) {
        this(id, status, null);
    }

    public ResponseStatus getStatus() {
        return status;
    }

    @Override
    public String getBody() {
        return this.message;
    }

    @Override
    public String getType() {
        return "STATUS";
    }

    @Override
    public List<String> getHeaderFields() {
        return List.of(this.status.toString());
    }
}
