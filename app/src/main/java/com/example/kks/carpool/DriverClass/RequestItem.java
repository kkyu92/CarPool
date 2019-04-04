package com.example.kks.carpool.DriverClass;

import android.widget.RatingBar;

public class RequestItem {

    private String start_distance;
    private String people_count;
    private String fare;
    private String start_point;
    private String end_point;
    private String start_date;
    private String start_time;
    private RatingBar rating;

    private double sLat;
    private double sLon;
    private double eLat;
    private double eLon;
    private String name;
    private String IDX;

    public RequestItem(String start_distance, String people_count, String fare, String start_point, String end_point, String start_date, String start_time, RatingBar rating, double sLat, double sLon, double eLat, double eLon, String name, String IDX) {
        this.start_distance = start_distance;
        this.people_count = people_count;
        this.fare = fare;
        this.start_point = start_point;
        this.end_point = end_point;
        this.start_date = start_date;
        this.start_time = start_time;
        this.rating = rating;
        this.sLat = sLat;
        this.sLon = sLon;
        this.eLat = eLat;
        this.eLon = eLon;
        this.name = name;
        this.IDX = IDX;
    }

    public String getStart_distance() {
        return start_distance;
    }

    public void setStart_distance(String start_distance) {
        this.start_distance = start_distance;
    }

    public String getPeople_count() {
        return people_count;
    }

    public void setPeople_count(String people_count) {
        this.people_count = people_count;
    }

    public String getFare() {
        return fare;
    }

    public void setFare(String fare) {
        this.fare = fare;
    }

    public String getStart_point() {
        return start_point;
    }

    public void setStart_point(String start_point) {
        this.start_point = start_point;
    }

    public String getEnd_point() {
        return end_point;
    }

    public void setEnd_point(String end_point) {
        this.end_point = end_point;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public RatingBar getRating() {
        return rating;
    }

    public void setRating(RatingBar rating) {
        this.rating = rating;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIDX() {
        return IDX;
    }

    public void setIDX(String IDX) {
        this.IDX = IDX;
    }
}
