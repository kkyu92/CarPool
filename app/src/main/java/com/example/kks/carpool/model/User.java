package com.example.kks.carpool.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("response")
    private String Response;

    @SerializedName("name")
    private String Name;

    @SerializedName("profile")
    private String profile;

    public String getResponse() {
        return Response;
    }

    public String getName() {
        return Name;
    }

    public String getProfile() {
        return profile;
    }
}
