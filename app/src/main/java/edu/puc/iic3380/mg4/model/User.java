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

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", uid='" + uid + '\'' +
                ", username='" + username + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
