package protocol.membership;

import protocol.ProtocolMessage;

// TODO is this kind of message necessary?
// CONNECT_SERVER <ID> CRLF CRLF
public class ConnectServerMessage extends ProtocolMessage {
    public ConnectServerMessage(String id) {
        super(id);
    }

    @Override
    public String getType() {
        return "CONNECT_SERVER";
    }
}
