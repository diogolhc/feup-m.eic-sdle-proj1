import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public abstract class Node {
    public static final String PROXY_CONFIG_PATH = "./filesystems/proxy_config.txt";
    private final String ip;
    private final String port;

    public Node(String address) {
        String[] parts = address.split(":");
        this.ip = parts[0];
        this.port = parts[1];
    }

    public String getAddress() {
        return ip + ":" + port;
    }

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }

    public static boolean validateAddress(String address) {
        // TODO validate with regex, the try catch is wrong
        String[] ipPort = address.split(":");
        if (ipPort.length != 2) {
            return false;
        }
        try {
            //Integer.parseInt(ipPort[0]);
            Integer.parseInt(ipPort[1]);
        } catch (NumberFormatException exception) {
            return false;
        }

        return true;
    }

    public static List<String> readProxyConfig() throws IOException {
        List<String> proxies = new ArrayList<>();

        try (Stream<String> stream = Files.lines(Paths.get(PROXY_CONFIG_PATH))) {
            stream.forEach(proxies::add);
        }

        return proxies;
    }
}
