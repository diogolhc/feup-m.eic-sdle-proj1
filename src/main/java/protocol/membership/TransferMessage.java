package protocol.membership;

import data.server.Message;
import data.server.Subscriber;
import protocol.ProtocolMessage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
127.0.0.1:8051 1 3 4 5
127.0.0.1:8052 1 3 4 5
127.0.0.1:8053 1 3 4 5
127.0.0.1:8054 1 3 4 5
*1
msgsgsrhrdh1\smsgis\\gfjsigshjrgisr
*3
gakgjhaigihsurjgvoisjgv
aggag
fgsg
*4
fagagoajivs0ivjsgjr0gvskrg
 */
// /s == *

// TRANSFER <ID> <TOPIC> CRLF CRLF [[<sub1.getString()> CRLF ...] [*ID CRLF CONTENT ...]]
public class TransferMessage extends ProtocolMessage {
    public final static String TYPE = "TRANSFER";

    private final List<Subscriber> subscribers;
    private final String topic;

    public TransferMessage(String id, String topic, List<Subscriber> subscribers) {
        super(id);
        this.topic = topic;
        this.subscribers = subscribers;
    }

    public List<Subscriber> getSubscribers() {
        return subscribers;
    }

    public String getTopic() {
        return topic;
    }

    @Override
    public String getBody() { // with stuffing
        StringBuilder sb = new StringBuilder();

        Set<Message> messages = new HashSet<>();
        for (Subscriber sub : subscribers) {
            sb.append(sub).append("\r\n");
            messages.addAll(sub.getMessages());
        }

        for (Message message : messages) {
            sb.append("*").append(message.getId()).append("\r\n")
              .append(message.getContent()
                      .replace("/", "//")
                      .replace("*", "/s")
              ).append("\r\n");

        }

        return sb.toString();
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public List<String> getHeaderFields() {
        return List.of(topic);
    }
}
