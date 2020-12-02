package com.impervious.chatapp.Model;

public class Chats {

    private String sender, receiver, message;
    private boolean isSeen;

    public Chats(String sender, String receiver, String message, boolean isSeen) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isSeen = isSeen;
    }

    public Chats() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getIsSeen() { return isSeen; }

    public void setSeen(boolean seen) { isSeen = seen; }
}
