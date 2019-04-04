package com.example.kks.carpool.DriverClass;

import android.widget.RatingBar;

public interface RequestCallback {
    void requestItemClick(double sLat, double sLon, double eLat, double eLon, String date, String time, String people, String fare, RatingBar rating, String name, String idx);
}
