package data;

import data.persistent.PersistentStorage;

import java.io.IOException;

public class Message {
    private final int id;
    private final String content;

    public Message(int id, String content) {
        this.id = id;
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    public int getId() {
        return this.id;
    }
}
