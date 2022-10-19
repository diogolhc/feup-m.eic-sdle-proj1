package protocol;

import org.zeromq.ZMQ;
import protocol.membership.ConnectServerMessage;
import protocol.membership.PeriodicServerMessage;
import protocol.topics.GetMessage;
import protocol.topics.PutMessage;
import protocol.topics.SubscribeMessage;
import protocol.topics.UnsubscribeMessage;
import protocol.status.ResponseStatus;
import protocol.status.StatusMessage;

import java.util.Arrays;

public class MessageParser {
    private final String message;

    public MessageParser(String message) {
        this.message = message;
    }

    public MessageParser(byte[] message) {
        this(new String(message, ZMQ.CHARSET));
    }

    public ProtocolMessage getMessage() {
        String[] headerAndBody = this.message.split("\r\n\r\n");
        if (headerAndBody.length < 1) throw new RuntimeException("Tried to parse an invalid message.");

        String[] headerFields = headerAndBody[0].split(" ");
        if (headerFields.length < 1) throw new RuntimeException("Tried to parse an invalid message.");

        String bodyMessage = headerAndBody.length == 2 ? headerAndBody[1].split("\r\n")[0] : null;

        switch (headerFields[0]) {
            case StatusMessage.TYPE:
                if (headerFields.length == 3) {
                    return new StatusMessage(headerFields[1], ResponseStatus.valueOf(headerFields[2]), bodyMessage);
                }
                break;
            case GetMessage.TYPE:
                if (headerFields.length == 3) {
                    return new GetMessage(headerFields[1], headerFields[2]);
                }
                break;
            case PutMessage.TYPE:
                if (headerFields.length == 3 && bodyMessage != null) {
                    return new PutMessage(headerFields[1], headerFields[2], bodyMessage);
                }
                break;
            case SubscribeMessage.TYPE:
                if (headerFields.length == 3) {
                    return new SubscribeMessage(headerFields[1], headerFields[2]);
                }
                break;
            case UnsubscribeMessage.TYPE:
                if (headerFields.length == 3) {
                    return new UnsubscribeMessage(headerFields[1], headerFields[2]);
                }
                break;
            case ConnectServerMessage.TYPE:
                if (headerFields.length == 2) {
                    return new ConnectServerMessage(headerFields[1]);
                }
                break;
            case PeriodicServerMessage.TYPE:
                if (headerFields.length == 2 && bodyMessage != null) {
                    return new PeriodicServerMessage(headerFields[1], Arrays.asList(bodyMessage.split(",").clone()));
                }
                break;
        }

        throw new RuntimeException("Tried to parse an invalid message.");
    }

}
