package edu.puc.iic3380.mg4.model;

import java.util.Date;

/**
 * Created by quelves on 21/10/2016.
 */

public class Invite {
    private boolean accepted;
    private Date inviteDate;
    private Date acceptDate;
    private String senderId;



    public Invite() {
    }

    public Invite(Date acceptDate, boolean accepted, Date inviteDate, String senderId) {
        this.acceptDate = acceptDate;
        this.accepted = accepted;
        this.inviteDate = inviteDate;
        this.senderId = senderId;
    }

    public Date getAcceptDate() {
        return acceptDate;
    }

    public void setAcceptDate(Date acceptDate) {
        this.acceptDate = acceptDate;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public Date getInviteDate() {
        return inviteDate;
    }

    public void setInviteDate(Date inviteDate) {
        this.inviteDate = inviteDate;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
}
