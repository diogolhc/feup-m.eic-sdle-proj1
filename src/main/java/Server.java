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
                    StatusMessage statusMessage = this.handleTopicMessage((TopicsMessage) message);
                    // TODO respond status with server address or client address?
                    statusMessage.send(socket);
                } else {
                    System.out.println("Unexpected request.");
                }
            }
        }
    }

    public StatusMessage handleTopicMessage(TopicsMessage message) {
        if (!this.topics.containsKey(message.getTopic())) {
            return new StatusMessage(this.getAddress(), ResponseStatus.WRONG_SERVER);
        }
        String clientId = message.getId();
        Topic topic = this.topics.get(message.getTopic());
        if (message instanceof SubscribeMessage) {
            if (topic.isSubscribed(clientId)) {
                return new StatusMessage(this.getAddress(), ResponseStatus.ALREADY_SUBSCRIBED);
            }

            topic.addSubscriber(clientId);
            // TODO storage (might make sense to encapsulate this inside addSubscriber)
            // storage.writeSync("subscribers.txt", subId + "\n");
        } else if (message instanceof UnsubscribeMessage) {
            if (!topic.isSubscribed(clientId)) {
                return new StatusMessage(this.getAddress(), ResponseStatus.NOT_SUBSCRIBED);
            }

            topic.removeSubscriber(clientId);
            // TODO storage (might make sense to encapsulate this inside addSubscriber)
            // storage.writeSync("subscribers.txt", subId + "\n");
        } else if (message instanceof GetMessage) {
            if (!topic.isSubscribed(clientId)) {
                return new StatusMessage(this.getAddress(), ResponseStatus.NOT_SUBSCRIBED);
            }

            Message messageToGet = topic.getSubscriber(clientId).getMessage();

            // TODO storage (might make sense to encapsulate file writes)
            return new StatusMessage(this.getAddress(), ResponseStatus.OK, messageToGet.getContent());
        } else if (message instanceof PutMessage) {
            Message messageToPut = new Message(topic.useCounter(), message.getBody());

            // TODO storage (might make sense to encapsulate useCounter and file writes)
            //storage.writeSync("topics/" + topic.getName() + "/counter.txt",
            //        topic.getCounter().toString());
            //storage.writeSync("topics/" + topic.getName() + "/" + messageToPut.getId() + ".txt",
            //        message.getBody() + "\n");

            for (Subscriber subscriber: topic.getSubscribers()) {
                subscriber.putMessage(messageToPut);
            }
        }

        return new StatusMessage(this.getAddress(), ResponseStatus.OK);
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
