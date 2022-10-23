package protocol;

import data.server.Message;
import data.server.Subscriber;
import org.zeromq.ZMQ;
import protocol.membership.PeriodicServerMessage;
import protocol.membership.ServerGiveTopicMessage;
import protocol.membership.ServerTopicConflictWarnMessage;
import protocol.topics.GetMessage;
import protocol.topics.PutMessage;
import protocol.topics.SubscribeMessage;
import protocol.topics.UnsubscribeMessage;
import protocol.status.ResponseStatus;
import protocol.status.StatusMessage;

import java.util.*;

public class MessageParser {
    private final String message;

    public MessageParser(String message) {
        this.message = message;
    }

    public MessageParser(byte[] message) {
        this(new String(message, ZMQ.CHARSET));
    }

    public ProtocolMessage getMessage() {
        String bodyMessage = null;
        int bodyIndex = this.message.indexOf("\r\n\r\n");

        if (bodyIndex == -1) {
            throw new RuntimeException("Tried to parse an invalid message: no CRLF CRLF.");
        } else if (bodyIndex != this.message.length() - 4) {
            bodyMessage = this.message.substring(bodyIndex + 4);
        }

        String[] headerFields = this.message.substring(0, bodyIndex).split(" ");
        if (headerFields.length < 1) {
            throw new RuntimeException("Tried to parse an invalid message: no header fields.");
        }

        switch (headerFields[0]) {
            case StatusMessage.TYPE:
                if (headerFields.length == 3 && bodyMessage == null) {
                    return new StatusMessage(headerFields[1], ResponseStatus.valueOf(headerFields[2]));
                } else if (headerFields.length == 4 && bodyMessage != null) {
                    return new StatusMessage(headerFields[1], ResponseStatus.valueOf(headerFields[2]), headerFields[3], bodyMessage);
                }
                break;
            case GetMessage.TYPE:
                if (headerFields.length == 4) {
                    return new GetMessage(headerFields[1], headerFields[2], headerFields[3]);
                }
                break;
            case PutMessage.TYPE:
                try {
                    Integer counter = Integer.parseInt(headerFields[3]);
                    if (headerFields.length == 4 && bodyMessage != null) {
                        return new PutMessage(headerFields[1], headerFields[2], counter, bodyMessage);
                    }
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Tried to parse an invalid message (put counter).");
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
            case PeriodicServerMessage.TYPE:
                if (headerFields.length == 2) {
                    if (bodyMessage == null) {
                        return new PeriodicServerMessage(headerFields[1], new HashSet<>());
                    } else {
                        return new PeriodicServerMessage(headerFields[1], new HashSet<>(Arrays.asList(bodyMessage.split(" ").clone())));
                    }
                }
                break;
            case ServerGiveTopicMessage.TYPE:
                ServerGiveTopicMessage serverGiveTopicMessage = parseServerGiveTopicMessage(headerFields, bodyMessage);
                if (serverGiveTopicMessage != null) {
                    return serverGiveTopicMessage;
                }
                break;
            case ServerTopicConflictWarnMessage.TYPE:
                if (headerFields.length == 4) {
                    return new ServerTopicConflictWarnMessage(headerFields[1], headerFields[2], headerFields[3]);
                }
                break;
        }

        throw new RuntimeException("Tried to parse an invalid message of type: " + headerFields[0]);
    }

    private ServerGiveTopicMessage parseServerGiveTopicMessage(String[] headerFields, String bodyMessage) {
        if (headerFields.length != 3) {
            return null;
        }

        if (bodyMessage == null) {
            return new ServerGiveTopicMessage(headerFields[1], headerFields[2], new ArrayList<>());
        }


        String[] tokens = bodyMessage.split("\\*");
        if (tokens.length < 1) {
            return null;
        }

        String[] subs = tokens[0].split("\r\n");

        Map<String, Message> messages = new HashMap<>();
        for (int i = 1; i < tokens.length; i++) {
            int idIndex = tokens[i].indexOf("\r\n");

            String id = tokens[i].substring(0, idIndex);
            String stuffedContent = tokens[i].substring(idIndex + 2, tokens[i].length() - 2);

            // unStuffing by reversing the following:
            // *  -> /s
            // /* -> //s
            // /s -> /s
            // /  -> //
            // // -> ////
            StringBuilder content = new StringBuilder();
            for (int j = 0; j < stuffedContent.length(); j++) {
                if (stuffedContent.charAt(j) == '/') {
                    // no need to check if j+1 in string since its not possible
                    // to have an escape alone at the tail
                    if (stuffedContent.charAt(j + 1) == '/') {
                        j++;
                        content.append("/");
                    } else if (stuffedContent.charAt(j + 1) == 's') {
                        j++;
                        content.append("*");
                    } // no else
                } else {
                    content.append(stuffedContent.charAt(j));
                }
            }

            messages.put(id, new Message(id, content.toString()));
        }

        List<Subscriber> subscribers = new LinkedList<>();
        for (String sub : subs) {
            String[] subTokens = sub.split(" ");
            if (subTokens.length < 1) {
                return null;
            }

            String id = subTokens[0];
            Subscriber s = new Subscriber(id);
            for (int i = 1; i < subTokens.length; i++) {
                String messageId = subTokens[i];
                Message m = messages.get(messageId);
                if (m != null) {
                    s.putMessage(m);
                }
            }

            subscribers.add(s);
        }

        return new ServerGiveTopicMessage(headerFields[1], headerFields[2], subscribers);
    }

}
