package edu.puc.iic3380.mg4.model;

import android.text.style.TtsSpan;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;


public class ChatMessage {
    private String uid;
    private String senderId;
    private String receiverId;
    private String message;
    private Date messageDate;

    // Java objects used in firebase realtime database must declare an empty constructor
    public ChatMessage() {
        UUID.randomUUID().toString();


    }

    public ChatMessage(String senderId, String message) {
        this.senderId = senderId;
        this.message = message;
    }

    public ChatMessage(String message, Date messageDate, String receiverId, String senderId) {
        this();
        this.message = message;
        this.messageDate = messageDate;
        this.receiverId = receiverId;
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(Date messageDate) {
        this.messageDate = messageDate;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return message;
    }


}
