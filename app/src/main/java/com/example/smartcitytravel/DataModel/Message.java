package com.example.smartcitytravel.DataModel;

public class Message {

    private String id;
    private String senderID;
    private String senderName;
    private String message;
    private String date;
    private String time;

    public Message() {
    }

    public Message(String id, String senderID, String senderName, String message, String date, String time) {
        this.id = id;
        this.senderID = senderID;
        this.senderName = senderName;
        this.message = message;
        this.date = date;
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
