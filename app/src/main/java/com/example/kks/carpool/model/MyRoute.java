package com.example.kks.carpool.model;

import com.google.gson.annotations.SerializedName;

public class MyRoute {

    @SerializedName("name")
    private String name;
    @SerializedName("sLat")
    private double sLat;
    @SerializedName("sLon")
    private double sLon;
    @SerializedName("eLat")
    private double eLat;
    @SerializedName("eLon")
    private double eLon;
    @SerializedName("title")
    private String title;
    @SerializedName("sPlace")
    private String sPlace;
    @SerializedName("ePlace")
    private String ePlace;
    @SerializedName("idx")
    private int idx;
    @SerializedName("gMap")
    private String gMap;

    public MyRoute(String name, String title, String sPlace, String ePlace, double sLat, double sLon, double eLat, double eLon, int idx, String gMap) {
        this.name = name;
        this.title = title;
        this.sPlace = sPlace;
        this.ePlace = ePlace;
        this.sLat = sLat;
        this.sLon = sLon;
        this.eLat = eLat;
        this.eLon = eLon;
        this.idx = idx;
        this.gMap = gMap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getsLat() {
        return sLat;
    }

    public void setsLat(double sLat) {
        this.sLat = sLat;
    }

    public double getsLon() {
        return sLon;
    }

    public void setsLon(double sLon) {
        this.sLon = sLon;
    }

    public double geteLat() {
        return eLat;
    }

    public void seteLat(double eLat) {
        this.eLat = eLat;
    }

    public double geteLon() {
        return eLon;
    }

    public void seteLon(double eLon) {
        this.eLon = eLon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getsPlace() {
        return sPlace;
    }

    public void setsPlace(String sPlace) {
        this.sPlace = sPlace;
    }

    public String getePlace() {
        return ePlace;
    }

    public void setePlace(String ePlace) {
        this.ePlace = ePlace;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public String getgMap() {
        return gMap;
    }

    public void setgMap(String gMap) {
        this.gMap = gMap;
    }
}
