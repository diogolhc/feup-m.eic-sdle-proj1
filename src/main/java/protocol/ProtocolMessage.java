package protocol;

import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.List;

public abstract class ProtocolMessage {
    private final String id;

    public abstract String getType();
    public List<String> getHeaderFields() {
        return new ArrayList<>();
    }
    public String getBody() {
        return "";
    }

    public ProtocolMessage(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getType());
        sb.append(" ");
        sb.append(this.id);
        for (String field : this.getHeaderFields()) {
            sb.append(" ");
            sb.append(field);
        }
        sb.append("\r\n\r\n");
        if (this.getBody() != null) {
            sb.append(this.getBody());
            sb.append("\r\n");
        }
        return sb.toString();
    }

    public void send(ZMQ.Socket socket) {
        socket.send(this.toString().getBytes(ZMQ.CHARSET), 0);
    }
}
