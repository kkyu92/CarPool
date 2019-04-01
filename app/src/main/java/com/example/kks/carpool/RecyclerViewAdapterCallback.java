package com.example.kks.carpool;

/**
 * Created by KJH on 2017-11-07.
 */

public interface RecyclerViewAdapterCallback {
    void showToast(String place);
    void startPlace(String place, String lat, String lon);
    void endPlace(String place, String lat, String lon);
}
