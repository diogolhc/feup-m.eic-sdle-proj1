//
//  Hello World server in Java
//  Binds REP socket to tcp://*:5555
//  Expects "Hello" from client, replies with "World"
//

import data.Subscriber;
import data.Topic;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import protocol.MessageParser;
import protocol.ProtocolMessage;
import protocol.topics.*;
import protocol.topics.reply.ResponseStatus;
import protocol.topics.reply.StatusMessage;

import java.util.*;

public class Server extends Node
{

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

                        Topic currentTopic = topics.get(((SubscribeMessage) message).getTopic());

                        String subId = message.getId();

                        if (!subscribers.containsKey(subId)) {
                            subscribers.put(subId, new Subscriber(subId));
                        }

                        StatusMessage statusMessage ;

                        if (subscribers.get(subId).containsTopic(currentTopic)) {
                            statusMessage = new StatusMessage(this.getAddress(), ResponseStatus.ALREADY_SUBSCRIBED);
                        } else {
                            subscribers.get(subId).addTopic(currentTopic);
                            currentTopic.addSub(subscribers.get(subId));
                            statusMessage = new StatusMessage(this.getAddress(), ResponseStatus.OK);
                        }

                        statusMessage.send(socket);

                    } else if (message instanceof UnsubscribeMessage) {

                    } else if (message instanceof GetMessage) {

                    } else if (message instanceof PutMessage) {

                    }

                } else {
                    System.out.println("Unexpected client request.");
                }
            }
        }
    }

    public static void main(String[] args) throws Exception
    {
        Server myServer = new Server("127.0.0.1:4001");
        myServer.run();
    }
}
