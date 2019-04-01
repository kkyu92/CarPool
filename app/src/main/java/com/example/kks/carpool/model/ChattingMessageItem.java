package com.example.kks.carpool.model;

public class ChattingMessageItem {

    int type;
    String name;
    String profile;
    String message;
    String time;

    public ChattingMessageItem(int type, String name, String profile, String message, String time) {
        this.type = type;
        this.name = name;
        this.profile = profile;
        this.message = message;
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
