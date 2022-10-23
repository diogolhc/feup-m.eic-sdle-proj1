import data.proxy.TopicServerMapping;
import exceptions.proxy.ProxyDoesNotKnowAnyServerException;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import protocol.MessageParser;
import protocol.ProtocolMessage;
import protocol.membership.PeriodicMessage;
import protocol.membership.MergeMessage;
import protocol.topics.*;
import protocol.status.ResponseStatus;
import protocol.status.StatusMessage;

import java.util.Map;

public class Proxy extends Node {
    private static final Integer MAX_TRIES = 3;
    private static final Integer TIMEOUT_MS = 1000;

    private final TopicServerMapping topicServerMapping;

    public Proxy(ZContext context, String address) {
        super(context, address);
        this.topicServerMapping = new TopicServerMapping();
    }

    public void dispatchTopicMessage(ZMQ.Socket clientSocket, TopicsMessage message) {
        try {
            String serverId = this.topicServerMapping.getServer(message.getTopic());
            ZMQ.Socket serverSocket = this.getContext().createSocket(SocketType.REQ);
            if (!serverSocket.connect("tcp://" + serverId)) {
                // TODO proxy address or client address?
                new StatusMessage(this.getAddress(), ResponseStatus.SERVER_UNAVAILABLE).send(clientSocket);
                return;
            }
            ProtocolMessage response = message.sendWithRetriesAndTimeoutAndGetResponse(this.getContext(), serverId, serverSocket, MAX_TRIES, TIMEOUT_MS);
            if (response != null) {
                response.send(clientSocket);
            }

        } catch (ProxyDoesNotKnowAnyServerException e) {
            new StatusMessage(this.getAddress(), ResponseStatus.PROXY_DOES_NOT_KNOW_ANY_SERVER).send(clientSocket);
        }
    }

    public void updateServers(PeriodicMessage message) {
        Map<String, String> serversWithSameTopic = this.topicServerMapping.updateServers(message.getId(), message.getTopics());

        for (Map.Entry<String,String> entry : serversWithSameTopic.entrySet()) {
            String topicConflict = entry.getKey();
            String serverConflict = entry.getValue();

            String serverToSend;
            MergeMessage mergeMessage;
            if (serverConflict.compareTo(message.getId()) < 0) {
                mergeMessage = new MergeMessage(this.getAddress(), topicConflict, message.getId());
                serverToSend = serverConflict;
            } else {
                mergeMessage = new MergeMessage(this.getAddress(), topicConflict, serverConflict);
                serverToSend = message.getId();
            }

            // TODO do not create this socket (?) use one saved?
            ZMQ.Socket serverSocket = this.getContext().createSocket(SocketType.REQ);
            serverSocket.connect("tcp://" + serverToSend);
            mergeMessage.sendWithRetriesAndTimeoutAndGetResponse(this.getContext(), serverToSend, serverSocket, MAX_TRIES, TIMEOUT_MS);
        }
    }

    public void listen() {
        ZMQ.Socket socket = this.getContext().createSocket(SocketType.REP);
        socket.bind("tcp://*:" + this.getPort());

        while (!Thread.currentThread().isInterrupted()) {
            // receive request from client or periodic from server
            byte[] reply = socket.recv(0);
            ProtocolMessage message = new MessageParser(reply).getMessage();
            System.out.println("Received " + message.getClass().getSimpleName() + " from " + message.getId());
            if (message instanceof TopicsMessage) {
                this.dispatchTopicMessage(socket, (TopicsMessage) message);
            } else if (message instanceof PeriodicMessage) {
                this.updateServers((PeriodicMessage) message);
                new StatusMessage(this.getAddress(), ResponseStatus.OK).send(socket);
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
