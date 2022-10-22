import org.zeromq.ZContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public abstract class Node {
    public static final String PROXY_CONFIG_PATH = "./filesystems/proxy_config.txt";
    private final String ip;
    private final String port;
    private final ZContext context;

    public Node(ZContext context, String address) {
        String[] parts = address.split(":");
        this.ip = parts[0];
        this.port = parts[1];
        this.context = context;
    }

    public String getAddress() {
        return this.ip + ":" + this.port;
    }

    public String getIp() {
        return this.ip;
    }

    public String getPort() {
        return this.port;
    }

    public ZContext getContext() {
        return this.context;
    }

    public static boolean validateAddress(String address) {
        String[] ipPort = address.split(":");
        if (ipPort.length != 2) {
            return false;
        }
        try {
            Integer.parseInt(ipPort[1]);
            String[] groups = ipPort[0].split("\\.");

            if (groups.length != 4) {
                return false;
            }
            return Arrays.stream(groups)
                    .filter(s -> !(s.length() > 1 && s.startsWith("0")))
                    .map(Integer::parseInt)
                    .filter(i -> (i >= 0 && i <= 255))
                    .count() == 4;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static List<String> readProxyConfig() throws IOException {
        List<String> proxies = new ArrayList<>();

        try (Stream<String> stream = Files.lines(Paths.get(PROXY_CONFIG_PATH))) {
            stream.forEach(proxies::add);
        }

        return proxies;
    }
}
