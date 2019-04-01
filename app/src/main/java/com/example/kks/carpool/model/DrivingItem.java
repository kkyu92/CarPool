package com.example.kks.carpool.model;

public class DrivingItem {

    private String user_name;
    private String target_name;
    private String target_profile;
    private String start_place;
    private String end_place;
    private String date_time;
    private String distance_time;
    private String rating;
    private String idx;
    private String fare;

    public DrivingItem(String user_name, String target_name, String target_profile, String start_place, String end_place, String date_time, String distance_time, String rating, String idx, String fare) {
        this.user_name = user_name;
        this.target_name = target_name;
        this.target_profile = target_profile;
        this.start_place = start_place;
        this.end_place = end_place;
        this.date_time = date_time;
        this.distance_time = distance_time;
        this.rating = rating;
        this.idx = idx;
        this.fare = fare;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getTarget_name() {
        return target_name;
    }

    public void setTarget_name(String target_name) {
        this.target_name = target_name;
    }

    public String getTarget_profile() {
        return target_profile;
    }

    public void setTarget_profile(String target_profile) {
        this.target_profile = target_profile;
    }

    public String getStart_place() {
        return start_place;
    }

    public void setStart_place(String start_place) {
        this.start_place = start_place;
    }

    public String getEnd_place() {
        return end_place;
    }

    public void setEnd_place(String end_place) {
        this.end_place = end_place;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    public String getDistance_time() {
        return distance_time;
    }

    public void setDistance_time(String distance_time) {
        this.distance_time = distance_time;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getIdx() {
        return idx;
    }

    public void setIdx(String idx) {
        this.idx = idx;
    }

    public String getFare() {
        return fare;
    }

    public void setFare(String fare) {
        this.fare = fare;
    }
}
