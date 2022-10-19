import data.Message;
import data.Subscriber;
import data.Topic;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import protocol.MessageParser;
import protocol.ProtocolMessage;
import protocol.status.ResponseStatus;
import protocol.status.StatusMessage;
import protocol.topics.*;

import java.util.*;

public class Server extends Node {
    private PersistentStorage storage;
    private final Map<String, Topic> topics;

    public Server(String address) {
        super(address);
        this.storage = new PersistentStorage(address);
        this.topics = new HashMap<>();
    }

    public void listen() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.REP);
            socket.bind("tcp://*:" + this.getPort());

            while (!Thread.currentThread().isInterrupted()) {
                // receive request from client
                byte[] reply = socket.recv(0);
                ProtocolMessage message = (new MessageParser(reply)).getMessage();
                if (message instanceof TopicsMessage) {
                    ResponseStatus responseStatus = this.handleTopicMessage((TopicsMessage) message);
                    // TODO respond status with server address or client address?
                    new StatusMessage(this.getAddress(), responseStatus).send(socket);
                } else {
                    System.out.println("Unexpected request.");
                }
            }
        }
    }

    public ResponseStatus handleTopicMessage(TopicsMessage message) {
        if (!this.topics.containsKey(message.getTopic())) {
            return ResponseStatus.WRONG_SERVER;
        }
        Topic topic = this.topics.get(message.getTopic());
        if (message instanceof SubscribeMessage) {
            SubscribeMessage subscribeMessage = (SubscribeMessage) message;
            if (topic.isSubscribed(subscribeMessage.getId())) {
                return ResponseStatus.ALREADY_SUBSCRIBED;
            }

            topic.addSubscriber(subscribeMessage.getId());
            // TODO storage (might make sense to encapsulate this inside addSubscriber)
            // storage.writeSync("subscribers.txt", subId + "\n");
        } else if (message instanceof UnsubscribeMessage) {
            UnsubscribeMessage unsubscribeMessage = (UnsubscribeMessage) message;
            if (!topic.isSubscribed(unsubscribeMessage.getId())) {
                return ResponseStatus.ALREADY_UNSUBSCRIBED;
            }

            topic.removeSubscriber(unsubscribeMessage.getId());
            // TODO storage (might make sense to encapsulate this inside addSubscriber)
            // storage.writeSync("subscribers.txt", subId + "\n");
        } else if (message instanceof GetMessage) {

        } else if (message instanceof PutMessage) {
            PutMessage putMessage = (PutMessage) message;

            // Write to file
            // TODO storage (might make sense to encapsulate useCounter and file writes)
            Message messageToPut = new Message(topic.useCounter(), putMessage.getBody());

            //storage.writeSync("topics/" + topic.getName() + "/counter.txt",
            //        topic.getCounter().toString());
            //storage.writeSync("topics/" + topic.getName() + "/" + messageToPut.getId() + ".txt",
            //        message.getBody() + "\n");

            // Update subscribers queues

            for (Subscriber subscriber: topic.getSubscribers()) {
                subscriber.putMessage(messageToPut);
            }
        }

        return ResponseStatus.OK;
    }

    private static void printUsage() {
        System.out.println("usage: java Server <IP>:<PORT>");
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            printUsage();
            return;
        }

        if (!Node.validateAddress(args[0])) {
            System.out.println("Invalid <IP>:<PORT>: " + args[0]);
            printUsage();
            return;
        }

        Server server = new Server(args[0]);
        server.listen();
    }
}
