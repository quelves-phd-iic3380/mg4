package edu.puc.iic3380.mg4.model;

import java.util.UUID;

/**
 * Created by quelves on 9/20/16.
 */

public class Contact {
    public final String uid;
    public final String name;
    public final String phoneNumber;

    private Contact(String name, String phoneNumber) {
        this(UUID.randomUUID().toString(), name, phoneNumber);
    }

    public Contact(String uid, String name, String phoneNumber) {
        this.name = name;
        this.uid = uid;
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
        return phoneNumber;
    }

    public String getUid() {
        return uid;
    }


}
