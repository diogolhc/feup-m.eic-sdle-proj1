import data.server.Message;
import data.PersistentStorage;
import data.server.Topic;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import protocol.MessageParser;
import protocol.ProtocolMessage;
import protocol.membership.ServerTopicConflictWarnMessage;
import protocol.status.ResponseStatus;
import protocol.status.StatusMessage;
import protocol.topics.*;
import threads.ServerPeriodicThread;

import java.io.IOException;
import java.util.*;

public class Server extends Node {
    private final PersistentStorage storage;
    private final Map<String, Topic> topics;
    private final List<String> proxies;

    public Server(ZContext context, String address, List<String> proxies) {
        super(context, address);
        this.storage = new PersistentStorage(address.replace(":", "_"));
        this.topics = new HashMap<>();
        this.proxies = proxies;
    }

    public void start() {
        for (String topic: this.storage.listFiles()) {
            try {
                this.topics.put(topic, Topic.load(this.storage, topic));
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Could not load topic " + topic + ".");
            }
        }

        Thread serverPeriodicThread = new ServerPeriodicThread(this.getContext(), this.getAddress(), this.proxies, topics);
        serverPeriodicThread.start(); // TODO stop this thread (?)

        this.listen();
    }

    public void listen() {
        ZMQ.Socket socket = this.getContext().createSocket(SocketType.REP);
        socket.bind("tcp://*:" + this.getPort());

        while (!Thread.currentThread().isInterrupted()) {
            // receive request from client
            byte[] reply = socket.recv(0);
            ProtocolMessage message = (new MessageParser(reply)).getMessage();
            System.out.println("Received " + message.getClass().getSimpleName() + " from " + message.getId());
            if (message instanceof TopicsMessage) {
                StatusMessage statusMessage = this.handleTopicMessage((TopicsMessage) message);
                //TODO respond status with server address or client address?
                statusMessage.send(socket);
            } else if (message instanceof ServerTopicConflictWarnMessage) {
                new StatusMessage(this.getAddress(), ResponseStatus.OK).send(socket);
                // TODO send topic to other server...
            } else {
                System.out.println("Unexpected request.");
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

            String lastCounter = ((GetMessage) message).getCounter();

            if (topic.subscriberRepeatedLastCounter(clientId, lastCounter)){
                if (!topic.hasMessages(clientId)) {
                    return new StatusMessage(this.getAddress(), ResponseStatus.NO_MESSAGES);
                }
            }

            Message messageToGet = topic.getMessage(clientId, lastCounter);
            String getMessageCounter = Integer.toString(messageToGet.getId());
            return new StatusMessage(this.getAddress(), ResponseStatus.OK, getMessageCounter, messageToGet.getContent());
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

        List<String> proxies;
        try {
            proxies = Node.readProxyConfig();
        } catch (IOException e) {
            System.out.println("Failed to read proxy config: " + e.getMessage());
            return;
        }

        try (ZContext context = new ZContext()) {
            Server server = new Server(context, args[0], proxies);
            server.start();
        }
    }
}
