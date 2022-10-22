package protocol.status;

import protocol.ProtocolMessage;

import java.util.List;

// STATUS <ID> <STATUS> [COUNTER] CRLF CRLF [MESSAGE]
public class StatusMessage extends ProtocolMessage {
    public final static String TYPE = "STATUS";

    private final ResponseStatus status;
    private final String message;
    private String counter = null;

    public StatusMessage(String id, ResponseStatus status, String message) {
        super(id);
        this.status = status;
        this.message = message;
    }

    public StatusMessage(String id, ResponseStatus status, String counter, String message) {
        super(id);
        this.status = status;
        this.message = message;
        this.counter = counter;
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
        return TYPE;
    }

    public String getCounter() { return this.counter; }

    @Override
    public List<String> getHeaderFields() {
        List<String> headerList = new java.util.ArrayList<>(List.of(this.status.toString()));
        if (this.counter != null){
            headerList.add(this.counter);
        }
        return headerList;
    }
}
