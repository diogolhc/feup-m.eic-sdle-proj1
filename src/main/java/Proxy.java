import data.proxy.TopicServerMapping;
import exceptions.proxy.ProxyDoesNotKnowAnyServerException;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import protocol.MessageParser;
import protocol.ProtocolMessage;
import protocol.membership.PeriodicServerMessage;
import protocol.membership.ServerTopicConflictWarnMessage;
import protocol.topics.*;
import protocol.status.ResponseStatus;
import protocol.status.StatusMessage;

import java.util.Map;

public class Proxy extends Node {
    private final TopicServerMapping topicServerMapping;

    public Proxy(ZContext context, String address) {
        super(context, address);
        this.topicServerMapping = new TopicServerMapping();
    }

    public void dispatchTopicMessage(ZContext context, ZMQ.Socket clientSocket, TopicsMessage message) {
        try {
            String serverId = this.topicServerMapping.getServer(message.getTopic());
            ZMQ.Socket serverSocket = context.createSocket(SocketType.REQ);
            if (!serverSocket.connect("tcp://" + serverId)) {
                // TODO proxy address or client address?
                new StatusMessage(this.getAddress(), ResponseStatus.SERVER_UNAVAILABLE).send(clientSocket);
                return;
            }
            message.send(serverSocket);
            new MessageParser(serverSocket.recv(0)).getMessage().send(clientSocket); // TODO we could send it without even bothering to parse (more efficient) but is it worth it?
        } catch (ProxyDoesNotKnowAnyServerException e) {
            new StatusMessage(this.getAddress(), ResponseStatus.PROXY_DOES_NOT_KNOW_ANY_SERVER).send(clientSocket);
        }
    }

    public void updateServers(PeriodicServerMessage message) {
        Map<String, String> serversWithSameTopic = this.topicServerMapping.updateServers(message.getId(), message.getTopics());

        for (Map.Entry<String,String> entry : serversWithSameTopic.entrySet()) {
            String topicConflict = entry.getKey();
            String serverConflict = entry.getValue();

            String serverToSend;
            ServerTopicConflictWarnMessage serverTopicConflictWarnMessage;
            if (serverConflict.compareTo(message.getId()) < 0) {
                serverTopicConflictWarnMessage = new ServerTopicConflictWarnMessage(this.getAddress(), topicConflict, message.getId());
                serverToSend = serverConflict;
            } else {
                serverTopicConflictWarnMessage = new ServerTopicConflictWarnMessage(this.getAddress(), topicConflict, serverConflict);
                serverToSend = message.getId();
            }

            // TODO do not create this socket (?) use one saved?
            ZMQ.Socket serverSocket = this.getContext().createSocket(SocketType.REQ);
            // TODO try several times, or not?
            serverSocket.connect("tcp://" + serverToSend);
            serverTopicConflictWarnMessage.send(serverSocket);
        }
    }

    public void listen() {
        ZMQ.Socket socket = this.getContext().createSocket(SocketType.REP);
        socket.bind("tcp://*:" + this.getPort());

        while (!Thread.currentThread().isInterrupted()) {
            // receive request from client
            byte[] reply = socket.recv(0);
            ProtocolMessage message = new MessageParser(reply).getMessage();
            System.out.println("Received " + message.getClass().getSimpleName() + " from " + message.getId());
            if (message instanceof TopicsMessage) {
                this.dispatchTopicMessage(this.getContext(), socket, (TopicsMessage) message);
            } else if (message instanceof PeriodicServerMessage) {
                this.updateServers((PeriodicServerMessage) message);
            } else {
                System.out.println("Unexpected client request.");
            }
        }
    }

    private static void printUsage() {
        System.out.println("usage: java Proxy <IP>:<PORT>");
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

        try (ZContext context = new ZContext()) {
            Proxy proxy = new Proxy(context, args[0]);
            proxy.listen();
        }
    }
}
