import data.PersistentStorage;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import protocol.MessageParser;
import protocol.ProtocolMessage;
import protocol.topics.GetMessage;
import protocol.topics.PutMessage;
import protocol.topics.SubscribeMessage;
import protocol.topics.UnsubscribeMessage;
import protocol.status.ResponseStatus;
import protocol.status.StatusMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public class Client extends Node {
    public static final String LAST_ID_FILE = "last_id";
    private final List<String> proxies;
    private final PersistentStorage storage;

    public Client(ZContext context, String address, List<String> proxies) {
        super(context, address);
        this.proxies = proxies;
        this.storage = new PersistentStorage(address.replace(":", "_"));
    }

    public StatusMessage send(ProtocolMessage message) {
        for (String proxy: this.proxies) {
            System.out.println("sending...");
            ZMQ.Socket socket = this.getContext().createSocket(SocketType.REQ);
            if (!socket.connect("tcp://" + proxy)) {
                System.out.println("Could not connect to " + proxy + ".");
                continue;
            }

            message.send(socket);

            ProtocolMessage response = new MessageParser(socket.recv(0)).getMessage();

            if (response instanceof StatusMessage) {
                return (StatusMessage) response;
            } else {
                System.out.println("Unexpected server response.");
                return null;
            }
        }

        System.out.println("Connection failed.");
        return null;
    }

    public void get(String topic) {
        StatusMessage replyMessage = this.send(new GetMessage(this.getAddress(), topic));
        if (replyMessage == null) return;

        ResponseStatus status = replyMessage.getStatus();

        if (status.equals(ResponseStatus.OK) && replyMessage.getBody() != null) {
            System.out.println("Message received from \"" + topic + "\".");
            System.out.println("==================================================");
            System.out.println(replyMessage.getBody());

        } else {
            System.out.println("Unknown server response: " + replyMessage.getStatus());
        }
    }

    public void put(String topic, String message) {
        StatusMessage replyMessage = this.send(new PutMessage(this.getAddress(), topic, message));
        if (replyMessage == null) return;

        ResponseStatus status = replyMessage.getStatus();

        if (status.equals(ResponseStatus.OK)) {
            System.out.println("Message sent to \"" + topic + "\".");
        } else {
            System.out.println("Unknown server response: " + replyMessage.getStatus());
        }
    }

    public void subscribe(String topic) throws IOException {
        StatusMessage replyMessage = this.send(new SubscribeMessage(this.getAddress(), topic));
        if (replyMessage == null) return;

        ResponseStatus status = replyMessage.getStatus();

        if (status.equals(ResponseStatus.OK)) {
            System.out.println("Topic \"" + topic + "\" subscribed.");
            this.storage.write(topic + File.separator + LAST_ID_FILE, "-1");
        } else if (status.equals(ResponseStatus.ALREADY_SUBSCRIBED)) {
            System.out.println("Topic \"" + topic + "\" already subscribed.");
        } else {
            System.out.println("Unknown server response: " + replyMessage.getStatus());
        }
    }

    public void unsubscribe(String topic) {
        StatusMessage replyMessage = this.send(new UnsubscribeMessage(this.getAddress(), topic));
        if (replyMessage == null) return;

        ResponseStatus status = replyMessage.getStatus();

        if (status.equals(ResponseStatus.OK)) {
            System.out.println("Topic \"" + topic + "\" unsubscribed.");
        } else if (status.equals(ResponseStatus.NOT_SUBSCRIBED)) {
            System.out.println("Topic \"" + topic + "\" already unsubscribed.");
        } else {
            System.out.println("Unknown server response: " + replyMessage.getStatus());
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

        // TODO timeouts on requests?
        try (ZContext context = new ZContext()) {
            Client client = new Client(context, args[1], proxies);
            switch (operation) {
                case "put": client.put(topic, message); break;
                case "get": client.get(topic); break;
                case "subscribe": client.subscribe(topic); break;
                case "unsubscribe": client.unsubscribe(topic); break;
                default: printUsage();
            }
        } catch (IOException e) {
            throw new RuntimeException(e); //TODO deal with exception
        }
    }
}
