package data.persistent;

import data.Message;

import java.io.IOException;

public class PersistentMessage {
    public static final String MESSAGES_FOLDER = "messages/";
    private PersistentStorage storage;
    private String topicPath;
    private Message message;

    private PersistentMessage(PersistentStorage storage, String topicPath, Message message) {
        this.storage = storage;
        this.topicPath = topicPath;
        this.message = message;
    }

    private static String getPath(String topicPath, int id) {
        return topicPath + "/" + MESSAGES_FOLDER + id;
    }

    public String getContent() {
        return this.message.getContent();
    }

    public int getId() {
        return this.message.getId();
    }

    public static PersistentMessage load(PersistentStorage storage, String topicPath, int id) throws IOException {
        Message message = new Message(id, storage.read(getPath(topicPath, id)));
        return new PersistentMessage(storage, topicPath, message);
    }

    public static PersistentMessage save(PersistentStorage storage, String topicPath, Message message) throws IOException {
        storage.write(getPath(topicPath, message.getId()), message.getContent());
        return new PersistentMessage(storage, topicPath, message);
    }

    public void delete() throws IOException {
        this.storage.delete(getPath(this.topicPath, this.getId()));
    }
}
