package protocol.membership;

import protocol.ProtocolMessage;

// CONNECT_SERVER <ID> CRLF CRLF
public class ConnectServerMessage extends ProtocolMessage {
    public final static String TYPE = "CONNECT_SERVER";

    public ConnectServerMessage(String id) {
        super(id);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
