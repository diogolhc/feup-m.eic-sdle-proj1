import data.server.Message;
import data.PersistentStorage;
import data.server.Topic;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import protocol.MessageParser;
import protocol.ProtocolMessage;
import protocol.status.ResponseStatus;
import protocol.status.StatusMessage;
import protocol.topics.*;

import java.io.IOException;
import java.util.*;

public class Server extends Node {
    private PersistentStorage storage;
    private final Map<String, Topic> topics;

    public Server(String address) {
        super(address);
        this.storage = new PersistentStorage(address.replace(":", "_"));
        this.topics = new HashMap<>();
    }

    public void start() {
        // TODO thread que periodicamente envia uma PeriodicServerMessage a cada proxy
        for (String topic: this.storage.listFiles()) {
            try {
                this.topics.put(topic, Topic.load(this.storage, topic));
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Could not load topic " + topic + ".");
            }
        }
        this.listen();
    }

    public void listen() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.REP);
            socket.bind("tcp://*:" + this.getPort());

            while (!Thread.currentThread().isInterrupted()) {
                // receive request from client
                byte[] reply = socket.recv(0);
                ProtocolMessage message = (new MessageParser(reply)).getMessage();
                System.out.println("Received " + message.getClass().getSimpleName());
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
            try {
                this.topics.put(message.getTopic(), Topic.load(storage, message.getTopic()));
            } catch (IOException e) {
                return new StatusMessage(this.getAddress(), ResponseStatus.INTERNAL_ERROR);
            }
        }
        String clientId = message.getId();
        Topic topic = this.topics.get(message.getTopic());
        if (message instanceof SubscribeMessage) {
            if (topic.isSubscribed(clientId)) {
                return new StatusMessage(this.getAddress(), ResponseStatus.ALREADY_SUBSCRIBED);
            }

            try {
                topic.addSubscriber(clientId);
            } catch (Exception e) {
                return new StatusMessage(this.getAddress(), ResponseStatus.INTERNAL_ERROR);
            }
        } else if (message instanceof UnsubscribeMessage) {
            if (!topic.isSubscribed(clientId)) {
                return new StatusMessage(this.getAddress(), ResponseStatus.NOT_SUBSCRIBED);
            }

            try {
                topic.removeSubscriber(clientId);
            } catch (Exception e) {
                return new StatusMessage(this.getAddress(), ResponseStatus.INTERNAL_ERROR);
            }
        } else if (message instanceof GetMessage) {
            if (!topic.isSubscribed(clientId)) {
                return new StatusMessage(this.getAddress(), ResponseStatus.NOT_SUBSCRIBED);
            }

            if (!topic.hasMessages(clientId)) {
                return new StatusMessage(this.getAddress(), ResponseStatus.NO_MESSAGES);
            }

            Message messageToGet = topic.getMessage(clientId);
            return new StatusMessage(this.getAddress(), ResponseStatus.OK, messageToGet.getContent());
        } else if (message instanceof PutMessage) {
            try {
                // TODO if no subscribers, NOOP? probably not, since there may be subscribers and this is the wrong server
                //      but still, we need some kind of 'garbage collection' or 'reference counting' for messages
                //      so that we don't store messages forever once everyone read them
                //      or maybe yes, since that would be easier and its a rare case that we don't need to take into account

                topic.putMessage(message.getBody(), clientId, ((PutMessage) message).getCounter());
            } catch (Exception e) {
                return new StatusMessage(this.getAddress(), ResponseStatus.INTERNAL_ERROR);
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

        server.start();
    }
}
