package data.server;

import data.PersistentStorage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Topic {
    public static final String SUBSCRIBERS_FILE = "subscribers";
    public static final String PUBLISHERS_FILE = "publishers";

    private final PersistentStorage storage;
    private final String name;
    private Map<String, Subscriber> subscribers;

    private final Map<String, Integer> clientMessagePutCounter;

    private Topic(PersistentStorage storage, String name) {
        this.storage = storage;
        this.name = name;
        this.subscribers = new HashMap<>();
        this.clientMessagePutCounter = new HashMap<>();
    }

    public String getName() {
        return this.name;
    }

    public List<Subscriber> getSubscribers() {
        return new LinkedList<>(subscribers.values());
    }

    public static Topic load(PersistentStorage storage, String name) throws IOException {
        Topic topic = new Topic(storage, name);

        storage.makeDirectory(name, Message.MESSAGES_FOLDER);
        Map<String, Message> messages = new HashMap<>();
        for (String messageFile : storage.listFiles(name, Message.MESSAGES_FOLDER)) {
            String messageId = messageFile;
            Message message = Message.load(storage, name, messageFile);
            messages.put(messageId, message);
        }

        topic.loadSubscribers(messages);
        topic.loadPublishersLastMessage();

        return topic;
    }

    private void loadSubscribers(Map<String, Message> messages) throws IOException {
        if (!this.storage.exists(this.name, SUBSCRIBERS_FILE)) {
            this.storage.write(this.name + File.separator + SUBSCRIBERS_FILE, "");
            return;
        }

        List<String> subscriberStrings = this.storage.readLines(this.name + File.separator + SUBSCRIBERS_FILE);
        for (String subscriberString : subscriberStrings) {
            Subscriber subscriber = Subscriber.load(subscriberString, messages);
            this.subscribers.put(subscriber.getId(), subscriber);
        }
    }

    private void loadPublishersLastMessage() throws IOException {
        if (!this.storage.exists(this.name, PUBLISHERS_FILE)) {
            this.storage.write(this.name + File.separator + PUBLISHERS_FILE, "");
            return;
        }

        List<String> publisherStrings = this.storage.readLines(this.name + File.separator + PUBLISHERS_FILE);
        for (String publisherString : publisherStrings) {
            String[] publisherLines = publisherString.split(" ");
            this.clientMessagePutCounter.put(publisherLines[0], Integer.valueOf(publisherLines[1]));
        }
    }

    private void updateSubscribers() throws IOException {
        try (FileWriter writer = this.storage.write(this.name + File.separator + SUBSCRIBERS_FILE)) {
            for (Subscriber subscriber : this.subscribers.values()) {
                writer.write(subscriber.toString());
                writer.write(System.lineSeparator());
            }
        }
    }

    private void updateLastPutMessageClient() throws IOException {
        try (FileWriter writer = this.storage.write(this.name + File.separator + PUBLISHERS_FILE)) {

            for (Map.Entry<String, Integer> entry : this.clientMessagePutCounter.entrySet()) {
                writer.write(entry.getKey() + " " + entry.getValue());
                writer.write(System.lineSeparator());
            }
        }
    }

    public void addSubscribersFromTransfer(List<Subscriber> subscribers) throws IOException {
        Map<String, Subscriber> oldSubs = new HashMap<>(this.subscribers);

        for (Subscriber subscriber: subscribers){
            this.subscribers.put(subscriber.getId(), subscriber);
        }

        try {
            this.updateSubscribers();
        } catch (IOException e) {
            this.subscribers = oldSubs;
            throw e;
        }
    }

    public void addSubscriber(String subscriberId) throws IOException {
        this.subscribers.put(subscriberId, new Subscriber(subscriberId));
        try {
            this.updateSubscribers();
        } catch (IOException e) {
            this.subscribers.remove(subscriberId);
            throw e;
        }
    }

    public void removeSubscriber(String subscriberId) throws IOException {
        Subscriber subscriber = this.subscribers.remove(subscriberId);
        try {
            this.updateSubscribers();
        } catch (IOException e) {
            this.subscribers.put(subscriberId, subscriber);
            throw e;
        }
    }

    public Boolean subscriberRepeatedLastCounter(String subscriberId, String lastCounter) {
        return this.subscribers.get(subscriberId).repeatedLastCounter(lastCounter);
    }

    public Message getMessage(String subscriberId, String lastCounter) {
        return this.subscribers.get(subscriberId).getMessage(lastCounter);
    }

    public void putMessage(String content, String publisher, Integer publisherCounter) throws IOException{
        Message message = new Message(publisher.replace(":", "_") + "-" + publisherCounter, content);
        message.save(this.storage, this.name);

        Integer counter = this.clientMessagePutCounter.get(publisher);
        if (counter == null) {
            this.clientMessagePutCounter.put(publisher, publisherCounter);
            this.updateLastPutMessageClient();
        } else {
            if (publisherCounter <= counter) {
                // Repeated message
                return;
            } else {
                this.clientMessagePutCounter.replace(publisher, publisherCounter);
                this.updateLastPutMessageClient();
            }
        }

        for (Subscriber subscriber : this.subscribers.values()) {
            subscriber.putMessage(message);
        }

        try {
            this.updateSubscribers();
        } catch (IOException e) {
            try {
                message.delete(this.storage, this.name);
            } catch (IOException e2) {
                e.addSuppressed(e2);
            }

            for (Subscriber subscriber : this.subscribers.values()) {
                subscriber.undoMessage();
            }
            System.out.println("HERE 4");
            throw e;
        }
    }

    public boolean isSubscribed(String subscriber) {
        return this.subscribers.containsKey(subscriber);
    }

    public boolean hasMessages(String clientId) {
        return this.subscribers.get(clientId).hasMessages();
    }

    @Override
    public String toString() {
        return this.name;
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

    public void deleteFromPersistence() throws IOException {
        this.storage.deleteRecursively(this.name);
    }
}
