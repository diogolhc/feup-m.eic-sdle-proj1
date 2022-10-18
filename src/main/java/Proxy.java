import data.TopicServerMapping;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import protocol.MessageParser;
import protocol.ProtocolMessage;
import protocol.topics.*;
import protocol.topics.reply.ResponseStatus;
import protocol.topics.reply.StatusMessage;

public class Proxy {
    private final TopicServerMapping topicServerMapping;
    private final String ip;
    private final String port;

    public Proxy(String ip, String port) {
        topicServerMapping = new TopicServerMapping();
        this.ip = ip;
        this.port = port;
    }

    public StatusMessage makeStatus(ResponseStatus status) {
        return new StatusMessage(ip + ":" + port, status);
    }

    public void dispatchTopicMessage(ZContext context, ZMQ.Socket clientSocket, TopicsMessage message) {
        String serverId = topicServerMapping.getServer(message.getTopic());

        ZMQ.Socket serverSocket = context.createSocket(SocketType.REQ);
        if (!serverSocket.connect("tcp://" + serverId)) {
            this.makeStatus(ResponseStatus.SERVER_UNAVAILABLE).send(clientSocket);
            return;
        }

        message.send(serverSocket);
        new MessageParser(serverSocket.recv(0)).getMessage().send(clientSocket); // TODO we could send it without even bothering to parse (more efficient) but is it worth it?
    }

    public void listen() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.REP);
            socket.bind("tcp://*:" + this.port);

            while (!Thread.currentThread().isInterrupted()) {
                // receive request from client
                byte[] reply = socket.recv(0);
                ProtocolMessage message = (new MessageParser(new String(reply, ZMQ.CHARSET))).getMessage();
                if (message instanceof TopicsMessage) {
                    this.dispatchTopicMessage(context, socket, (TopicsMessage) message);
                } else {
                    System.out.println("Unexpected client request.");
                }

                // TODO send request to a server

                // TODO receive reply from the server

                // TODO reply to the client

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

        String[] ipPort = args[0].split(":");
        try {
            Integer.parseInt(ipPort[0]);
            Integer.parseInt(ipPort[1]);
        } catch (NumberFormatException exception) {
            System.out.println("Invalid <IP>:<PORT>: " + args[0]);
            return;
        }

        Proxy proxy = new Proxy(ipPort[0], ipPort[1]);
        proxy.listen();
    }
}
