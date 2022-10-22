package data.proxy;

import exceptions.proxy.ProxyDoesNotKnowAnyServerException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TopicServerMapping {
    // topic -> serverId
    private final Map<String, String> topicsLocations;
    // serverId -> set(topic)
    private final Map<String, Set<String>> topicsPerServer;


    public TopicServerMapping() {
        this.topicsLocations = new HashMap<>();
        this.topicsPerServer = new HashMap<>();
    }

    public void addServer(String serverId) {
        this.topicsPerServer.put(serverId, new HashSet<>());
    }

    private String addTopic(String topic) throws ProxyDoesNotKnowAnyServerException {
        String serverIdLess = null;
        int count = Integer.MAX_VALUE;
        for (Map.Entry<String, Set<String>> entry : this.topicsPerServer.entrySet()) {
            int size = entry.getValue().size();
            if (size < count) {
                serverIdLess = entry.getKey();
                count = size;
            }
        }

        if (serverIdLess == null) {
            throw new ProxyDoesNotKnowAnyServerException("");
        }

        this.topicsLocations.put(topic, serverIdLess);
        this.topicsPerServer.get(serverIdLess).add(topic);

        return serverIdLess;
    }

    public String getServer(String topic) throws ProxyDoesNotKnowAnyServerException {
        String serverId = this.topicsLocations.get(topic);
        if (serverId == null) {
            serverId = addTopic(topic);
        }

        return serverId;
    }

}
