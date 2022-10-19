package data;

import java.util.*;

public class Topic {
    private final String name;
    private final Map<String, Subscriber> subscribers;
    private Integer counter;

    public Topic(String name) {
        this.name = name;
        this.subscribers = new HashMap<>();
        this.counter = 0;
    }

    public Integer useCounter(){
        return counter++;
    }

    public Integer getCounter(){
        return counter;
    }

    public void addSubscriber(Subscriber subscriber) {
        this.subscribers.put(subscriber.getId(), subscriber);
    }

    public void addSubscriber(String subscriber) {
        this.addSubscriber(new Subscriber(subscriber));
    }

    public void removeSubscriber(Subscriber subscriber) {
        this.removeSubscriber(subscriber.getId());
    }

    public void removeSubscriber(String subscriber) {
        this.subscribers.remove(subscriber);
    }

    public Collection<Subscriber> getSubscribers() {
        return this.subscribers.values();
    }

    public String getName() {
        return this.name;
    }

    public boolean isSubscribed(Subscriber subscriber) {
        return this.isSubscribed(subscriber.getId());
    }

    public boolean isSubscribed(String subscriber) {
        return this.subscribers.containsKey(subscriber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Topic topic = (Topic) o;
        return name.equals(topic.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
