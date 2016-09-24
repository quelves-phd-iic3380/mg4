package edu.puc.iic3380.mg4.model;

import java.util.UUID;

public class ChatMessage {
    private String mAuthor;
    private String mMessage;
    private String mUuid;
    private boolean isOwner;

    // Java objects used in firebase realtime database must declare an empty constructor
    public ChatMessage() {}

    public ChatMessage(String author, String message) {
        this(author, message, UUID.randomUUID().toString());
    }

    public ChatMessage(String author, String message, String uuid) {
        mAuthor = author;
        mMessage = message;
        mUuid = uuid;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

    public String getUuid() {
        return mUuid;
    }

    public void setUuid(String uuid) {
        mUuid = uuid;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }

    @Override
    public String toString() {
        return mMessage;
    }
}
