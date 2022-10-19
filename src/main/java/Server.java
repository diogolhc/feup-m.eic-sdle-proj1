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

import java.io.IOException;
import java.util.*;

public class Server extends Node {

    private PersistentStorage storage;
    private final Map<String, Topic> topics;
    private final Map<String, Subscriber> subscribers;

    public Server(String address) {
        super(address);
        this.storage = new PersistentStorage(address);
        this.topics = new HashMap<>();
        this.subscribers = new HashMap<>();
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
                    this.handleTopicMessage(socket, (TopicsMessage) message);
                } else {
                    System.out.println("Unexpected request.");
                }
            }
        }
    }

    public void handleTopicMessage(ZMQ.Socket socket, TopicsMessage message) {
        if (!this.topics.containsKey(message.getTopic())) {
            // TODO handle wrong_server
            return;
        }
        Topic topic = this.topics.get(message.getTopic());
        if (message instanceof SubscribeMessage) {
            SubscribeMessage subscribeMessage = (SubscribeMessage) message;

            String subId = message.getId();

            if (!this.subscribers.containsKey(subId)) {
                this.subscribers.put(subId, new Subscriber(subId));
            }

            StatusMessage statusMessage;

            if (this.subscribers.get(subId).containsTopic(topic)) {
                statusMessage = new StatusMessage(this.getAddress(), ResponseStatus.ALREADY_SUBSCRIBED);
            } else {
                this.subscribers.get(subId).addTopic(topic);
                topic.addSub(this.subscribers.get(subId));
                statusMessage = new StatusMessage(this.getAddress(), ResponseStatus.OK);

                storage.writeSync("subscribers.txt", subId + "\n");
            }

            statusMessage.send(socket);

        } else if (message instanceof UnsubscribeMessage) {
            String topicName = ((UnsubscribeMessage) message).getTopic();
            Topic currentTopic = this.topics.get(topicName);

            String unsubId = message.getId();

            StatusMessage statusMessage;

            if (!this.topics.containsKey(topicName)) {
                statusMessage = new StatusMessage(this.getAddress(), ResponseStatus.WRONG_SERVER);
            } else if (!this.subscribers.get(unsubId).containsTopic(currentTopic)) {
                statusMessage = new StatusMessage(this.getAddress(), ResponseStatus.ALREADY_UNSUBSCRIBED);
            } else {
                currentTopic.removeSub(this.subscribers.get(unsubId));
                statusMessage = new StatusMessage(this.getAddress(), ResponseStatus.OK);

                if (this.subscribers.get(unsubId).isEmpty()) {
                    this.subscribers.remove(unsubId);
                    // TODO Remove from "subscribers.txt" file
                }
            }

            statusMessage.send(socket);

        } else if (message instanceof GetMessage) {

        } else if (message instanceof PutMessage) {

            String topicName = ((PutMessage) message).getTopic();

            StatusMessage statusMessage;

            if (!this.topics.containsKey(topicName)) {
                statusMessage = new StatusMessage(this.getAddress(), ResponseStatus.WRONG_SERVER);
            } else {
                // Write to file

                String newMessageId = topicName + topics.get(topicName).useCounter();
                storage.writeSync("topics/" + topicName + "/counter.txt",
                        topics.get(topicName).getCounter().toString());
                storage.writeSync("topics/" + topicName + "/" + newMessageId + ".txt",
                        message.getBody() + "\n");

                // Update subscribers queues

                for (Subscriber subscriber : topics.get(topicName).getSubscribers()) {
                    subscriber.addMessageToTopic(new Message(message.getBody()), topicName);
                }

                statusMessage = new StatusMessage(this.getAddress(), ResponseStatus.OK);
            }

            statusMessage.send(socket);
        }
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
