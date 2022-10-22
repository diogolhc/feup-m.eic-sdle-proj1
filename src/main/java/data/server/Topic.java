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
    private final Map<String, Subscriber> subscribers;
    private Integer messageCounter;

    private final Map<String, Integer> clientMessagePutCounter;

    private Topic(PersistentStorage storage, String name) {
        this.storage = storage;
        this.name = name;
        this.subscribers = new HashMap<>();
        this.messageCounter = 0;
        this.clientMessagePutCounter = new HashMap<>();
    }

    public String getName() {
        return this.name;
    }

    public static Topic load(PersistentStorage storage, String name) throws IOException {
        Topic topic = new Topic(storage, name);

        storage.makeDirectory(name, Message.MESSAGES_FOLDER);
        Map<Integer, Message> messages = new HashMap<>();
        for (String messageFile : storage.listFiles(name, Message.MESSAGES_FOLDER)) {
            int messageId = Integer.parseInt(messageFile);
            Message message = Message.load(storage, name, messageId);
            if (messageId > topic.messageCounter) {
                topic.messageCounter = messageId;
            }
            messages.put(messageId, message);
        }

        topic.loadSubscribers(messages);
        topic.loadPublishersLastMessage(messages);

        return topic;
    }

    private void loadSubscribers(Map<Integer, Message> messages) throws IOException {
        if (!this.storage.exists(this.name, SUBSCRIBERS_FILE)) {
            this.storage.write(this.name + File.pathSeparator + SUBSCRIBERS_FILE, "");
            return;
        }

        List<String> subscriberStrings = this.storage.readLines(this.name + File.pathSeparator + SUBSCRIBERS_FILE);
        for (String subscriberString : subscriberStrings) {
            Subscriber subscriber = Subscriber.load(subscriberString, messages);
            this.subscribers.put(subscriber.getId(), subscriber);
        }
    }

    private void loadPublishersLastMessage(Map<Integer, Message> messages) throws IOException {
        if (!this.storage.exists(this.name, PUBLISHERS_FILE)) {
            this.storage.write(this.name + File.pathSeparator + PUBLISHERS_FILE, "");
            return;
        }

        List<String> publisherStrings = this.storage.readLines(this.name + File.pathSeparator + PUBLISHERS_FILE);
        for (String publisherString : publisherStrings) {
            String[] publisherLines = publisherString.split(" ");
            this.clientMessagePutCounter.put(publisherLines[0], Integer.valueOf(publisherLines[1]));
        }
    }

    private void updateSubscribers() throws IOException {
        try (FileWriter writer = this.storage.write(this.name + File.pathSeparator + SUBSCRIBERS_FILE)) {
            for (Subscriber subscriber : this.subscribers.values()) {
                writer.write(subscriber.toString());
                writer.write(System.lineSeparator());
            }
        }
    }

    private void updateLastPutMessageClient() throws IOException {
        try (FileWriter writer = this.storage.write(this.name + File.pathSeparator + PUBLISHERS_FILE)) {

            for (Map.Entry<String, Integer> entry : this.clientMessagePutCounter.entrySet()) {
                writer.write(entry.getKey() + " " + Integer.toString(Integer.parseInt(entry.getKey())));
                writer.write(System.lineSeparator());
            }
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

    public Message getMessage(String subscriberId) {
        // TODO this is not deleting the message from the subscriber because that should only be done when the subscriber confirms it
        //  received the message. As such, exactly once fault tolerance should be implemented for that.
        //  This is also not deleting the message from the topic because that should only be done when it has been deleted from all subscribers.
        return this.subscribers.get(subscriberId).getMessage();
    }

    public void putMessage(String content, String publisher, Integer publisherCounter) throws IOException {
        Message message = new Message(messageCounter, content);
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
                this.clientMessagePutCounter.replace(publisher, messageCounter);
                this.updateLastPutMessageClient();
            }
        }

        for (Subscriber subscriber : this.subscribers.values()) {
            subscriber.putMessage(message);
        }

        try {
            this.updateSubscribers();
            messageCounter += 1;
        } catch (IOException e) {
            try {
                message.delete(this.storage, this.name);
            } catch (IOException e2) {
                e.addSuppressed(e2);
            }

            for (Subscriber subscriber : this.subscribers.values()) {
                subscriber.undoMessage();
            }

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
}
