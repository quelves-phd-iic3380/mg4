package edu.puc.iic3380.mg4.model;

/**
 * Created by quelves on 19/10/2016.
 */

public class Chat {
    private String uid;
    private String name;
    private String lastMessage;


    public Chat(String lastMessage, String name, String uid) {
        this.lastMessage = lastMessage;
        this.name = name;
        this.uid = uid;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
