package protocol.membership;

import protocol.ProtocolMessage;

// TODO is this kind of message necessary?
// CONNECT_SERVER <ID> CRLF CRLF
public class ConnectServerMessage extends ProtocolMessage {
    final private static String TYPE = "CONNECT_SERVER";

    public ConnectServerMessage(String id) {
        super(id);
    }

    @Override
    public String toString() {
        return TYPE + " " + this.id + "\r\n\r\n";
    }
}
