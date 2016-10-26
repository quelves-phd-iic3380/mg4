package edu.puc.iic3380.mg4.model;


import com.google.firebase.database.Exclude;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by quelves on 12/10/2016.
 */

public class User {
    private String uid;
    private String username;
    private String phone;
    private String email;
    private String state;
    private String message;
    private String chatkey;


    private ArrayList<Chat> chats;
    private ArrayList<Contact> contacts;
    private ArrayList<Invite> invites;

    public User() {

    }

    public User(String uid, String email, String username, String phone) {
        this.uid = uid;
        this.username = username;
        this.phone = phone;
        this.email = email;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("username", username);
        result.put("email", email);
        result.put("phone", phone);
        result.put("estado", state);
        result.put("message", message);
        return result;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getChatkey() {
        return chatkey;
    }

    public void setChatkey(String chatkey) {
        this.chatkey = chatkey;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "User{" +
                "chatkey='" + chatkey + '\'' +
                ", uid='" + uid + '\'' +
                ", username='" + username + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", state='" + state + '\'' +
                ", message='" + message + '\'' +
                 '}';
    }
}
