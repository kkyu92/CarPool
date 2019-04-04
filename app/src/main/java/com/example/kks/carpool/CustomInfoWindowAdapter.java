//package com.example.kks.carpool;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.TextView;
//
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.model.Marker;
//
//public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
//
//    private final View mWindow;
//    private Context mContext;
//
//    public CustomInfoWindowAdapter(Context context) {
//        mContext = context;
//        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_window,null);
//    }
//
//    private void rendWindowText(Marker marker, View view) {
//
//        String Title = marker.getTitle();
//        TextView tvTitle = view.findViewById(R.id.TV_title);
//
//        if(!Title.equals("")) {
//            tvTitle.setText(Title);
//        }
//
//        String snipet = marker.getSnippet();
//        TextView tvSnipet = view.findViewById(R.id.TV_info);
//
//        if (!snipet.equals("")) {
//            tvSnipet.setText(snipet);
//        }
//    }
//
//    @Override
//    public View getInfoWindow(Marker marker) {
//        rendWindowText(marker, mWindow);
//        return mWindow;
//    }
//
//    @Override
//    public View getInfoContents(Marker marker) {
//        rendWindowText(marker, mWindow);
//        return mWindow;
//    }
//}
