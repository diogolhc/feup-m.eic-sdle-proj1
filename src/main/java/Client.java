import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import protocol.MessageParser;
import protocol.topics.SubscribeMessage;
import protocol.topics.UnsubscribeMessage;
import protocol.topics.reply.ResponseStatus;
import protocol.topics.reply.StatusMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Client extends Node {
    private final List<String> proxies;


    public Client(String address, List<String> proxies) {
        super(address);
        this.proxies = proxies;
    }

    public void get(String topic) {

    }

    public void put(String topic, String message) {

    }

    public void subscribe(String topic) {
        try (ZContext context = new ZContext()) {
            for (String proxy: proxies) {
                ZMQ.Socket socket = context.createSocket(SocketType.REQ);
                if (!socket.connect("tcp://" + proxy)) {
                    continue;
                }

                new SubscribeMessage(this.getAddress(), topic).send(socket);

                String request = new SubscribeMessage(this.getAddress(), topic).toString();
                socket.send(request.getBytes(ZMQ.CHARSET), 0);

                byte[] reply = socket.recv(0);

                StatusMessage replyMessage = (StatusMessage)new MessageParser(new String(reply, ZMQ.CHARSET)).getMessage();
                if (replyMessage.getStatus().equals(ResponseStatus.OK)) {
                    System.out.println("Topic \"" + topic + "\" subscribed.");
                } else if (replyMessage.getStatus().equals(ResponseStatus.ALREADY_SUBSCRIBED)) {
                    System.out.println("Topic \"" + topic + "\" already subscribed.");
                } else {
                    System.out.println("Unknown server response: " + replyMessage.getStatus());
                }

                return;
            }
        }
    }

    public void unsubscribe(String topic) {
        try (ZContext context = new ZContext()) {
            for (String proxy : proxies) {
                ZMQ.Socket socket = context.createSocket(SocketType.REQ);
                if (!socket.connect("tcp://" + proxy)) {
                    continue;
                }

                String request = new UnsubscribeMessage(ip + ":" + port, topic).toString();
                socket.send(request.getBytes(ZMQ.CHARSET), 0);

                byte[] reply = socket.recv(0);

                StatusMessage replyMessage = (StatusMessage) new MessageParser(new String(reply, ZMQ.CHARSET)).getMessage();
                if (replyMessage.getStatus().equals(ResponseStatus.OK)) {
                    System.out.println("Topic \"" + topic + "\" unsubscribed.");
                } else {
                    System.out.println("Unknown server response: " + replyMessage.getStatus());
                }

                return;
            }
        }

    }

    private static void printUsage() {
        System.out.println("usage:");
        System.out.println("java Client subscribe <IP>:<PORT> <TOPIC>");
        System.out.println("java Client unsubscribe <IP>:<PORT> <TOPIC>");
        System.out.println("java Client put <IP>:<PORT> <TOPIC> MESSAGE_PATH");
        System.out.println("java Client get <IP>:<PORT> <TOPIC>");
    }

    public static void main(String[] args) {
        if (args.length != 3 && args.length != 4) {
            printUsage();
            return;
        }

        if (!Node.validateAddress(args[1])) {
            System.out.println("Invalid <IP>:<PORT>: " + args[1]);
            printUsage();
            return;
        }

        String operation = args[0];
        String topic = args[2];
        String message = null;
        if (args.length == 4) {
            if (!Objects.equals(operation, "put")) {
                System.out.println("MESSAGE_PATH is only valid for put operation.");
                printUsage();
                return;
            }

            if (!Files.exists(Paths.get(args[3]))) {
                System.out.println("Invalid message path: " + args[3]);
                printUsage();
                return;
            }
            String path = args[3];
            try {
                message = new String(Files.readAllBytes(Paths.get(path)));
            } catch (IOException e) {
                System.out.println("Failed to read from " + path + ": " + e.getMessage());
                return;
            }
        }

        List<String> proxies;
        try {
            proxies = Node.readProxyConfig();
        } catch (IOException e) {
            System.out.println("Failed to read proxy config: " + e.getMessage());
            return;
        }

        Client client = new Client(args[1], proxies);
        switch (operation) {
            case "put" -> client.put(topic, message);
            case "get" -> client.get(topic);
            case "subscribe" -> client.subscribe(topic);
            case "unsubscribe" -> client.unsubscribe(topic);
            default -> printUsage();
        }
    }
}
