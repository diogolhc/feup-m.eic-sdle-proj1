import data.proxy.TopicServerMapping;
import exceptions.proxy.ProxyDoesNotKnowAnyServerException;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import protocol.MessageParser;
import protocol.ProtocolMessage;
import protocol.topics.*;
import protocol.status.ResponseStatus;
import protocol.status.StatusMessage;

public class Proxy extends Node {
    private final TopicServerMapping topicServerMapping;

    public Proxy(String address) {
        super(address);
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

    public void listen() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.REP);
            socket.bind("tcp://*:" + this.getPort());

            while (!Thread.currentThread().isInterrupted()) {
                // receive request from client
                byte[] reply = socket.recv(0);
                ProtocolMessage message = new MessageParser(reply).getMessage();
                System.out.println("Received " + message.getClass().getSimpleName());
                if (message instanceof TopicsMessage) {
                    this.dispatchTopicMessage(context, socket, (TopicsMessage) message);
                } else {
                    System.out.println("Unexpected client request.");
                }
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

        Proxy proxy = new Proxy(args[0]);
        proxy.listen();
    }
}
