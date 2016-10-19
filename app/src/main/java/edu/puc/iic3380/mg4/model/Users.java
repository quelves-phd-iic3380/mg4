package edu.puc.iic3380.mg4.model;

import java.util.ArrayList;

/**
 * Created by quelves on 21/10/2016.
 */

public class Users {
    private ArrayList<User> users;

    public Users() {
        users = new ArrayList<User>();
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public void add(User user) {
        users.add(user);
    }


}
