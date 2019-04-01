package com.example.kks.carpool;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener;
import com.google.android.gms.maps.GoogleMap.OnCameraMoveListener;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;

import java.io.IOException;
import java.util.List;

import static com.example.kks.carpool.google_map.DEFAULT_ZOOM;

public class google_picker extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gMap;
    private FusedLocationProviderClient mFusedLocationClient;

    private String pick, address;
    private double lat, lon;

    private Button start_pick, end_pick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_picker);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(google_picker.this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        start_pick = findViewById(R.id.start_pick);
        end_pick = findViewById(R.id.end_pick);

        Intent get = getIntent();
        pick = get.getStringExtra("picker");

        if (pick.equals("출발")) {
            start_pick.setVisibility(View.VISIBLE);
            end_pick.setVisibility(View.INVISIBLE);
        } else {
            end_pick.setVisibility(View.VISIBLE);
            start_pick.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        getDeviceLocation();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        gMap.setMyLocationEnabled(true);
        gMap.getUiSettings().setMyLocationButtonEnabled(true);
        LatLng center = gMap.getCameraPosition().target;
        Log.e("Center", "::::" + center);

        // 화면 중앙 좌표값에 해당되는 장소의 주소를 알아낸다!! (Geocoder)----변환
        gMap.setOnCameraIdleListener(new OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                CameraPosition cp = gMap.getCameraPosition();
                final LatLng current_center = cp.target;
                Log.e("중앙:::", "::" + current_center);

                Geocoder geocoder = new Geocoder(google_picker.this);
                List<Address> list = null;
                try {
                    lat = current_center.latitude;
                    lon = current_center.longitude;
                    list = geocoder.getFromLocation(lat, lon, 10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (list != null) {
                    if (list.size() == 0) {
                        start_pick.setText("해당되는 주소 정보는 없습니다.");
                        end_pick.setText("해당되는 주소 정보는 없습니다.");
                    } else {
                        if (pick.equals("출발")) {
                            String ad = list.get(0).getSubLocality() + " " + list.get(0).getThoroughfare() + " " + list.get(0).getPostalCode();
                            String result = ad.substring(5, ad.length());
                            address = ad;
                            start_pick.setText("<출발지> " + ad);
                        } else {
                            String ad = list.get(0).getSubLocality() + " " + list.get(0).getThoroughfare() + " " + list.get(0).getPostalCode();
                            String result = ad.substring(5, ad.length());
                            address = ad;
                            end_pick.setText("<도착지> " + ad);
                        }
                    }
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        start_pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent go = new Intent();
                go.putExtra("address", address);
                go.putExtra("lat_start", lat);
                go.putExtra("lon_start", lon);
                setResult(RESULT_OK, go);
                finish();
            }
        });

        end_pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent go = new Intent();
                go.putExtra("address", address);
                go.putExtra("lat_end", lat);
                go.putExtra("lon_end", lon);
                setResult(RESULT_OK, go);
                finish();
            }
        });

    }

    private void getDeviceLocation() {
        Log.d("TAG", "내 위치");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            Task location = mFusedLocationClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful() && task.getResult() != null) {

                        Log.d("TAG", "성공");
                        Location currentLocation = (Location) task.getResult();

                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "내 위치");
                    } else {
                        Log.d("TAG", "실패");
                    }
                }
            });

        } catch (SecurityException e) {
            Log.d("TAG", "널포인트!");
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d("TAG", "카메라 이동 lat : " + latLng.latitude + ", lng : " + latLng.longitude);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!title.equals("내 위치")) {
            MarkerOptions options = new MarkerOptions().position(latLng).title(title);
            gMap.addMarker(options);
        }
        hideSoftKeyboard();
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

//    private void configureCameraIdle() {
//        onCameraIdleListener = new GoogleMap.OnCameraIdleListener() {
//            @Override
//            public void onCameraIdle() {
//
//                LatLng latLng = gMap.getCameraPosition().target;
//                Geocoder geocoder = new Geocoder(google_picker.this);
//                Log.e("center", "::::"+latLng);
//                try {
//                    List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
//                    if (addressList != null && addressList.size() > 0) {
//                        String locality = addressList.get(0).getAddressLine(0);
//                        String country = addressList.get(0).getCountryName();
//                        if (!locality.isEmpty() && !country.isEmpty())
//                            start_pick.setVisibility(View.VISIBLE);
//                            start_pick.setText(locality + "  " + country);
//                            Log.e("center", "::::"+latLng);
//                    }
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Log.e("center", "::::에러러러러");
//                }
//
//            }
//        };
//    }

}
