package data;

import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

public class Subscriber {
    private String id;
    private Map<Topic, Queue<Message>> messages;

    public Subscriber(String id) {
        this.id = id;
    }

    public void unsubscribeTopic(Topic topic) {
        this.messages.remove(topic);
    }

    public void addTopic(Topic topic) {
        this.messages.put(topic, new LinkedList<>());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscriber subscriber = (Subscriber) o;
        return id.equals(subscriber.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
