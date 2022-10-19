package data;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Topic {
    private final String name;
    private final Set<Subscriber> subscribers;
    private Integer counter;

    public Topic(String name) {
        this.name = name;
        this.subscribers = new HashSet<>();
        this.counter = 0;
    }

    public Integer useCounter(){
        return counter++;
    }

    public Integer getCounter(){
        return counter;
    }

    public void addSub(Subscriber subscriber) {
        this.subscribers.add(subscriber);
    }

    public void removeSub(Subscriber subscriber) {
        subscriber.unsubscribeTopic(this);
        this.subscribers.remove(subscriber);
    }

    public Set<Subscriber> getSubscribers() { //TODO this is probably a code smell
        return this.subscribers;
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
