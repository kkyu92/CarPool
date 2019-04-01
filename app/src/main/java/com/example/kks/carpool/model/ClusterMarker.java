package com.example.kks.carpool.model;

public class ClusterMarker{

    private double lat;
    private double lon;
    private String place;
    private String time;

    public ClusterMarker(double lat, double lon, String place, String time) {
        this.lat = lat;
        this.lon = lon;
        this.place = place;
        this.time = time;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
