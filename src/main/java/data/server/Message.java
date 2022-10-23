package data.server;

import data.PersistentStorage;

import java.io.IOException;
import java.util.Objects;

public class Message {
    public static final String MESSAGES_FOLDER = "messages/";

    private final String id;
    private final String content;

    public Message(String id, String content) {
        this.id = id;
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    public String getId() {
        return this.id;
    }

    private static String getPath(String topicPath, String id) {
        return topicPath + "/" + MESSAGES_FOLDER + id;
    }

    private String getPath(String topicPath) {
        return getPath(topicPath, this.id);
    }

    public static Message load(PersistentStorage storage, String topicPath, String id) throws IOException {
        return new Message(id, storage.read(Message.getPath(topicPath, id)));
    }

    public void save(PersistentStorage storage, String topicPath) throws IOException {
        storage.write(this.getPath(topicPath), this.content);
    }

    public void delete(PersistentStorage storage, String topicPath) throws IOException {
        storage.delete(this.getPath(topicPath));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return id == message.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
