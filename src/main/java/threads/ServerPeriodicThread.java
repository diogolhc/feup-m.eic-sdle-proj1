package threads;

import data.server.Topic;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import protocol.membership.PeriodicMessage;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class ServerPeriodicThread extends Thread {
    final static int PERIOD_MS = 5000;
    private final ZContext context;
    private final String address;
    private final List<String> proxies;
    private final Map<String, Topic> topics;

    public ServerPeriodicThread(ZContext context, String address, List<String> proxies, Map<String, Topic> topics) {
        this.context = context;
        this.address = address;
        this.proxies = proxies;
        this.topics = topics;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            for (String proxy: this.proxies) {
                ZMQ.Socket socket = this.context.createSocket(SocketType.REQ);
                if (!socket.connect("tcp://" + proxy)) {
                    System.out.println("Could not connect to " + proxy + ".");
                    continue;
                }

                Set<String> topicsNames = topics.values().stream().map(Topic::getName).collect(Collectors.toSet());
                PeriodicMessage message = new PeriodicMessage(this.address, topicsNames);
                message.send(socket);
                System.out.println("Sending " + message.getClass().getSimpleName() + " to " + proxy);
            }

            try {
                // TODO avoid busy waiting?
                // don't know if good idea to put this thread and the main one into a threadpool...
                sleep(PERIOD_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
