package edu.puc.iic3380.mg4.model;

import java.util.UUID;

/**
 * Created by quelves on 19/10/2016.
 */

public class Chat {
    private String mUuid;
    private ChatMessage chatMessage;

    public Chat() {
    }

    public Chat(ChatMessage chatMessage, String mUuid) {
        this.chatMessage = chatMessage;
        this.mUuid = mUuid;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }

    public String getmUuid() {
        return mUuid;
    }

    public void setmUuid(String mUuid) {
        this.mUuid = mUuid;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "chatMessage=" + chatMessage +
                ", mUuid='" + mUuid + '\'' +
                '}';
    }
}
