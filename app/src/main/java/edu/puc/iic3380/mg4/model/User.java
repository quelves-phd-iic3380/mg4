package edu.puc.iic3380.mg4.model;


import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by quelves on 12/10/2016.
 */

public class User extends BaseObservable implements Parcelable {
    private String uid;
    private String username;
    private String phoneNumber;
    private String email;
    private String state;
    private String message;

    public User() {

    }

    public User(String uid, String email, String username, String phoneNumber) {
        this.uid = uid;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    protected User(Parcel in) {
        uid = in.readString();
        username = in.readString();
        phoneNumber = in.readString();
        email = in.readString();
        state = in.readString();
        message = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(username);
        dest.writeString(phoneNumber);
        dest.writeString(email);
        dest.writeString(state);
        dest.writeString(message);
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("username", username);
        result.put("email", email);
        result.put("phone", phoneNumber);
        result.put("estado", state);
        result.put("message", message);
        return result;
    }

    @Bindable
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Bindable
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Bindable
    public String getPhoneNumber() {
        return phoneNumber != null ? phoneNumber.replace(" ", ""):phoneNumber;
    }

    public void setPhone(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Bindable
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Bindable
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Bindable
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "User{" +
                ", uid='" + uid + '\'' +
                ", username='" + username + '\'' +
                ", phone='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", state='" + state + '\'' +
                ", message='" + message + '\'' +
                 '}';
    }


}
