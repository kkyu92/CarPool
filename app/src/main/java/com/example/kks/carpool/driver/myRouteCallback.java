package com.example.kks.carpool.driver;

public interface myRouteCallback {
    void showToast(String place);
    void myRouteClick(String title, String sPlace, String ePlace, double sLat, double sLon, double eLat, double eLon);
    void myRouteEdit(String title, String sPlace, String ePlace, double sLat, double sLon, double eLat, double eLon, int idx);
    void myRouteDel(String title, int idx);
}
