package data.proxy;

import exceptions.proxy.ProxyDoesNotKnowAnyServerException;

import java.util.*;

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
            } else if (size == count) {
                if (serverIdLess == null || entry.getKey().compareTo(serverIdLess) < 0) {
                    serverIdLess = entry.getKey();
                }
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

    public Map<String, String> updateServers(String serverId, Set<String> topics) {
        // topic -> server (!= serverId)
        Map<String, String> serversWithSameTopic = new HashMap<>();

        this.topicsPerServer.put(serverId, topics);
        for (String topic : topics) {
            String topicLocation = topicsLocations.get(topic);
            if (topicLocation == null) {
                this.topicsLocations.put(topic, serverId);
            } else if (!topicLocation.equals(serverId)) {
                if (topicLocation.compareTo(serverId) < 0) this.topicsLocations.put(topic, serverId);
                System.out.println("Detected topic conflict on: \"" + topic + "\" with servers: " + "\"" + serverId + "\" & \"" + topicLocation + "\"");
            }
        }

        return serversWithSameTopic;
    }

}
