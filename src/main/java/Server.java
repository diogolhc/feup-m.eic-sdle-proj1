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

    private final Map<String, Topic> topics;
    private final Map<String, Subscriber> subscribers;

    public Server(String address) {
        super(address);
        this.topics = new HashMap<>();
        this.subscribers = new HashMap<>();
    }

    public void run() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.REP);
            socket.bind("tcp://*:" + this.getPort());

            while (!Thread.currentThread().isInterrupted()) {
                // receive request from client
                byte[] reply = socket.recv(0);
                ProtocolMessage message = (new MessageParser(new String(reply, ZMQ.CHARSET))).getMessage();
                if (message instanceof TopicsMessage) {

                    if (message instanceof SubscribeMessage) {
                        Topic currentTopic = this.topics.get(((SubscribeMessage) message).getTopic());

                        String subId = message.getId();

                        if (!this.subscribers.containsKey(subId)) {
                            this.subscribers.put(subId, new Subscriber(subId));
                        }

                        StatusMessage statusMessage;

                        if (this.subscribers.get(subId).containsTopic(currentTopic)) {
                            statusMessage = new StatusMessage(this.getAddress(), ResponseStatus.ALREADY_SUBSCRIBED);
                        } else {
                            this.subscribers.get(subId).addTopic(currentTopic);
                            currentTopic.addSub(this.subscribers.get(subId));
                            statusMessage = new StatusMessage(this.getAddress(), ResponseStatus.OK);
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
                            }
                        }

                        statusMessage.send(socket);

                    } else if (message instanceof GetMessage) {

                    } else if (message instanceof PutMessage) {

                    }

                } else {
                    System.out.println("Unexpected client request.");
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Server myServer = new Server("127.0.0.1:4001");
        myServer.run();
    }
}
