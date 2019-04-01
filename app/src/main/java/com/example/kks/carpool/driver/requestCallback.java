package com.example.kks.carpool.driver;

import android.widget.RatingBar;

public interface requestCallback {
    void requestItemClick(double sLat, double sLon, double eLat, double eLon, String date, String time, String people, String fare, RatingBar rating, String name, String idx);
}
