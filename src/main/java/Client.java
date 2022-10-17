import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import protocol.MessageParser;
import protocol.topics.SubscribeMessage;
import protocol.topics.reply.StatusMessage;

import java.util.ArrayList;
import java.util.List;

public class Client {
    private final List<String> proxies;
    private final String ip;
    private final String port;

    public Client(String ip, String port) {
        this.ip = ip;
        this.port = port;
        this.proxies = new ArrayList<>();
        // TODO read from filesystems folder
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

                String request = new SubscribeMessage(ip + ":" + port, topic).toString();
                socket.send(request.getBytes(ZMQ.CHARSET), 0);

                byte[] reply = socket.recv(0);

                StatusMessage replyMessage = (StatusMessage)new MessageParser(new String(reply, ZMQ.CHARSET)).getMessage();
                if (replyMessage.getStatus().equals(StatusMessage.Status.OK)) {
                    System.out.println("Topic \"" + topic + "\" subscribed.");
                } else if (replyMessage.getStatus().equals(StatusMessage.Status.ALREADY_SUBSCRIBED)) {
                    System.out.println("Topic \"" + topic + "\" already subscribed.");
                } else {
                    System.out.println("Unknown server response: " + replyMessage.getStatus());
                }

                return;
            }
        }
    }

    public void unsubscribe(String topic) {

    }





    private static void printUsage() {
        System.out.println("usage: java Client <OP> <IP>:<PORT> <TOPIC> [MESSAGE_PATH]");
    }

    public static void main(String[] args) {
        if (args.length != 3 && args.length != 4) {
            printUsage();
            return;
        }

        String operation = args[0];
        String[] ipPort = args[1].split(":");
        try {
            Integer.parseInt(ipPort[0]);
            Integer.parseInt(ipPort[1]);
        } catch (NumberFormatException exception) {
            System.out.println("Invalid <IP>:<PORT>: " + args[0]);
            printUsage();
            return;
        }

        String topic = args[2];
        String message = null;
        if (args.length == 4) {
            String path = args[3];

            // TODO read message from path (async?)
            message = "msg toda pimpona";
        }

        Client client = new Client(ipPort[0], ipPort[1]);
        switch (operation) {
            case "PUT" -> client.put(topic, message);
            case "GET" -> client.get(topic);
            case "SUBSCRIBE" -> client.subscribe(topic);
            case "UNSUBSCRIBE" -> client.unsubscribe(topic);
            default -> printUsage();
        }

    }
}
