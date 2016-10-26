package edu.puc.iic3380.mg4.model;

import java.util.ArrayList;

/**
 * Created by quelves on 26/10/2016.
 */

public class ChatBinding {
    private String uid;
    private ArrayList<String> userIdList;

    public ChatBinding() {
    }

    public ChatBinding(String uid) {
        this.uid = uid;
    }

    public ChatBinding(String uid, ArrayList<String> userIdList) {
        this.uid = uid;
        this.userIdList = userIdList;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public ArrayList<String> getUserIdList() {
        return userIdList;
    }

    public void setUserIdList(ArrayList<String> userIdList) {
        this.userIdList = userIdList;
    }

    public void addUserId(String id) {
        if (userIdList == null) userIdList = new ArrayList<String>();
        userIdList.add(id);
    }


}
