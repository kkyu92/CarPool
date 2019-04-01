package com.example.kks.carpool.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

public class RequestCar {
    @SerializedName("response")
    private String Response;

    @SerializedName("user")
    private String user;
    @SerializedName("sLat")
    private double sLat;
    @SerializedName("sLon")
    private double sLon;
    @SerializedName("eLat")
    private double eLat;
    @SerializedName("eLon")
    private double eLon;
    @SerializedName("time")
    private String time;
    @SerializedName("date")
    private int date;
    @SerializedName("people")
    private int people;
    @SerializedName("fare")
    private String fare;
    @SerializedName("rating")
    private String rating;

    public String getResponse() {
        return Response;
    }

    public String getUser() {
        return user;
    }

    public double getsLat() {
        return sLat;
    }

    public double getsLon() {
        return sLon;
    }

    public double geteLat() {
        return eLat;
    }

    public double geteLon() {
        return eLon;
    }

    public int getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public int getPeople() {
        return people;
    }

    public String getFare() {
        return fare;
    }

    public String getRating() {
        return rating;
    }
}
