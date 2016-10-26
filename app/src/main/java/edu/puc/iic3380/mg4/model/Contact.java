package edu.puc.iic3380.mg4.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by quelves on 9/20/16.
 */

public class Contact {
    private String uid;
    private String name;
    private String phoneNumber;
    private String email;
    private String chatRef;

    public Contact() {
    }

    private Contact(String name, String phoneNumber) {
        this(UUID.randomUUID().toString(), name, phoneNumber);
    }

    public Contact(String uid, String name, String phoneNumber) {
        this.name = name;
        this.uid = uid;
        this.phoneNumber = phoneNumber;
    }

    public Contact(String uid, String name, String phoneNumber, String email) {
        this.email = email;
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public static class Builder {
        private String name;
        private String phoneNumber;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Contact build() {
            return new Contact(name, phoneNumber);
        }

    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber != null ? phoneNumber.replace(" ", ""):phoneNumber;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getChatRef() {
        return chatRef;
    }

    public void setChatRef(String chatRef) {
        this.chatRef = chatRef;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("name", name);
        result.put("email", email);
        result.put("phoneNumber", phoneNumber);
        result.put("chatRef", chatRef);

        return result;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "chatRef='" + chatRef + '\'' +
                ", uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
