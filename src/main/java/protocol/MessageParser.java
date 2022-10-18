package protocol;

import org.zeromq.ZMQ;
import protocol.membership.ConnectServerMessage;
import protocol.topics.GetMessage;
import protocol.topics.PutMessage;
import protocol.topics.SubscribeMessage;
import protocol.topics.UnsubscribeMessage;
import protocol.topics.reply.GetResponseMessage;
import protocol.topics.reply.ResponseStatus;
import protocol.topics.reply.StatusMessage;

public class MessageParser {
    private final String message;

    public MessageParser(String message) {
        this.message = message;
    }

    public MessageParser(byte[] message) {
        this(new String(message, ZMQ.CHARSET));
    }

    public ProtocolMessage getMessage() {
        String[] headerAndBody = message.split("\r\n\r\n");
        if (headerAndBody.length < 1) return null;

        String[] headerFields = headerAndBody[0].split(" ");
        if (headerFields.length < 1) return null;

        String bodyMessage = headerAndBody.length == 2 ? headerAndBody[1].split("\r\n")[0] : null;

        switch (headerFields[0]) {
            case "STATUS":
                if (headerFields.length == 2 && bodyMessage != null) {
                    return new GetResponseMessage(headerFields[1], bodyMessage);
                } else if (headerFields.length == 3) {
                    return new StatusMessage(headerFields[1], ResponseStatus.valueOf(headerFields[2]));
                }
                break;
            case "GET":
                if (headerFields.length == 3) {
                    return new GetMessage(headerFields[1], headerFields[2]);
                }
                break;
            case "PUT":
                if (headerFields.length == 3 && bodyMessage != null) {
                    return new PutMessage(headerFields[1], headerFields[2], bodyMessage);
                }
                break;
            case "SUBSCRIBE":
                if (headerFields.length == 3) {
                    return new SubscribeMessage(headerFields[1], headerFields[2]);
                }
                break;
            case "UNSUBSCRIBE":
                if (headerFields.length == 3) {
                    return new UnsubscribeMessage(headerFields[1], headerFields[2]);
                }
                break;
            case "CONNECT_SERVER":
                if (headerFields.length == 2) {
                    return new ConnectServerMessage(headerFields[1]);
                }
                break;
        }

        throw new RuntimeException("Tried to parse an invalid message.");
    }

}
