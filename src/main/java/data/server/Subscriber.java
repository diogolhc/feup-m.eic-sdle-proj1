package data.server;

import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;

public class Subscriber {
    private final String id;
    // Removing last is useful for undoing operations during error handling
    private final Deque<Message> messages;

    public Subscriber(String id) {
        this.id = id;
        this.messages = new LinkedBlockingDeque<>();
    }

    public static Subscriber load(String data, Map<Integer, Message> messages) {
        String[] subscriberData = data.split(" ");
        String subscriberId = subscriberData[0];
        Subscriber subscriber = new Subscriber(subscriberId);

        for (int i = 1; i < subscriberData.length; i++) {
            int messageId = Integer.parseInt(subscriberData[i]);
            subscriber.messages.add(messages.get(messageId));
        }

        return subscriber;
    }

    public String getId() {
        return this.id;
    }

    public boolean hasMessages() {
        return !this.messages.isEmpty();
    }

    public void putMessage(Message message) {
        this.messages.add(message);
    }

    public boolean repeatedLastCounter(String lastCounter) {
        if (lastCounter.equals(Integer.toString(this.messages.peek().getId()))) {
            this.messages.remove();
            return true;
        }
        return false;
    }

    public Message getMessage(String lastCounter) {
        return this.messages.peek();
    }

    public void removeMessage() {
        this.messages.remove();
    }

    public void undoMessage() {
        this.messages.removeLast();
    }

    public Deque<Message> getMessages() {
        return this.messages;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append(' ');
        for (Message message: this.messages) {
            sb.append(message.getId()).append(' ');
        }
        return sb.toString();
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
