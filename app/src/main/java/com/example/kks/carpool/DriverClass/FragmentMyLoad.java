package com.example.kks.carpool.DriverClass;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kks.carpool.LoginSignup.Login;
import com.example.kks.carpool.R;
import com.example.kks.carpool.model.MyRoute;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static com.example.kks.carpool.RiderClass.AutoCompleteParse.TMAP_API_KEY;
import static com.example.kks.carpool.LoginSignup.Result.USER_NAME;
import static com.example.kks.carpool.DriverClass.start_Driver.driver_txt;
import static com.example.kks.carpool.DriverClass.start_Driver.myload;

public class FragmentMyLoad extends Fragment implements DriverMyRouteCallback {

    private View view;
    private String TAG = "Fragment 나의경로 자동차 길찾기";

    MapView mapView;
    private GoogleMap gMap;
    private FusedLocationProviderClient mFusedLocationClient;
    public static final float DEFAULT_ZOOM = 12f;

    // 레이아웃
    private TextView start_txt, end_txt;
    private Button setOK, setEdit, setCancel;

    // 현재 위치 좌표
    double latitude;
    double longitude;

    // 마커 리스트
    private ArrayList<LatLng> allPoints;
    // 마커 커스텀
    private View marker_root_view;
    private TextView tv_marker;
    private MarkerOptions sMarkerOptions, eMarkerOptions;

    // 자동차경로
    private ArrayList<LatLng> mapPoints;

    private ImageView test;
    private Bitmap bitmap;
    private File file;

    // 완료된 나의 출발지 도착지
    public static LatLng START_POINT;
    public static LatLng END_POINT;
    private int EDIT = 0;
    private int EDIT_IDX = 0;
    private String TITLE, S_TITLE, E_TITLE;
    private String mapIMG;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_my_load, container, false);

        start_txt = view.findViewById(R.id.start_txt);
        end_txt = view.findViewById(R.id.end_txt);
        setOK = view.findViewById(R.id.setOK);
        setEdit = view.findViewById(R.id.setEdit);
        setCancel = view.findViewById(R.id.setCancel);

        mapView = (MapView) view.findViewById(R.id.google_map);
        mapView.onCreate(savedInstanceState);

        mapView.onResume(); // needed to get the map to display immediately
//        test = view.findViewById(R.id.test);
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        allPoints = new ArrayList<>();

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                gMap = mMap;
                setCustomMarkerView();
                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

                // 저장 불러오기
                myRouteLoad();

                gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng point) {

                        String ad = placeGetName(point.latitude, point.longitude);

                        if (allPoints.size() == 0) {
                            allPoints.add(point);
                            driver_txt.setText("새로운 나의 경로");

                            tv_marker.setText("출발");
                            tv_marker.setBackgroundResource(R.drawable.start_picker);
                            tv_marker.setTextColor(Color.BLACK);

                            sMarkerOptions = new MarkerOptions();
                            sMarkerOptions.title(ad);
                            sMarkerOptions.position(point);
                            sMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getContext(), marker_root_view)));
                            gMap.addMarker(sMarkerOptions);

                            start_txt.setText(ad);

                            if (EDIT == 0) { // 경로 선택 (클릭)
                                setOK.setVisibility(View.VISIBLE);
                                setEdit.setVisibility(View.INVISIBLE);
                                setCancel.setVisibility(View.INVISIBLE);
                            } else { // 경로 수정하기 (클릭)
                                setOK.setVisibility(View.INVISIBLE);
                                setEdit.setVisibility(View.INVISIBLE);
                                setCancel.setVisibility(View.VISIBLE);
                            }
                        } else if (allPoints.size() == 1) {
                            allPoints.add(point);

                            tv_marker.setText("도착");
                            tv_marker.setBackgroundResource(R.drawable.end_picker);
                            tv_marker.setTextColor(Color.WHITE);

                            eMarkerOptions = new MarkerOptions();
                            eMarkerOptions.title(ad);
                            eMarkerOptions.position(point);
                            eMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getContext(), marker_root_view)));
                            gMap.addMarker(eMarkerOptions);

                            end_txt.setText(ad);

                            // 경로 그려주기
                            getJsonData(allPoints.get(0), point);
                            Log.e("출발 좌표값?", ":::" + allPoints.get(0));
                            Log.e("도착 좌표값?", ":::" + point);

                            Polyline polyline = gMap.addPolyline(new PolylineOptions().addAll(mapPoints));
                            polyline.setColor(Color.MAGENTA);
                            zoomRoute(polyline.getPoints());

                            if (EDIT == 0) { // 경로 선택 (클릭)
                                setOK.setVisibility(View.VISIBLE);
                                setEdit.setVisibility(View.INVISIBLE);
                                setCancel.setVisibility(View.INVISIBLE);
                            } else { // 경로 수정하기 (클릭)
                                setOK.setVisibility(View.INVISIBLE);
                                setEdit.setVisibility(View.VISIBLE);
                                setCancel.setVisibility(View.VISIBLE);
                            }
                        } else {
                            gMap.clear();
                            allPoints.clear();
                            // 경로 만든거 지우기
                            end_txt.setText("도착지");
                            allPoints.add(point);

                            tv_marker.setText("출발");
                            tv_marker.setBackgroundResource(R.drawable.start_picker);
                            tv_marker.setTextColor(Color.BLACK);

                            sMarkerOptions = new MarkerOptions();
                            sMarkerOptions.title(ad);
                            sMarkerOptions.position(point);
                            sMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getContext(), marker_root_view)));
                            gMap.addMarker(sMarkerOptions);

                            start_txt.setText(ad);

                            if (EDIT == 0) { // 경로 선택 (클릭)
                                setOK.setVisibility(View.VISIBLE);
                                setEdit.setVisibility(View.INVISIBLE);
                                setCancel.setVisibility(View.INVISIBLE);
                            } else { // 경로 수정하기 (클릭)
                                setOK.setVisibility(View.INVISIBLE);
                                setEdit.setVisibility(View.INVISIBLE);
                                setCancel.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
            }
        });

        return view;
    }

    public void onResume() {
        super.onResume();

        // 경로등록 버튼
        setOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (allPoints.size() == 0 && !start_txt.getText().toString().equals("출발지")) {
                    Toast.makeText(getContext(), "출발지를 등록해 주세요", Toast.LENGTH_SHORT).show();
                } else if (allPoints.size() == 1 && !end_txt.getText().toString().equals("도착지")) {
                    Toast.makeText(getContext(), "도착지를 등록해 주세요", Toast.LENGTH_SHORT).show();
                } else {
                    LatLng sL = new LatLng(allPoints.get(0).latitude, allPoints.get(0).longitude);
                    LatLng eL = new LatLng(allPoints.get(1).latitude, allPoints.get(1).longitude);

                    // 경로등록
                    Intent intent = new Intent(getContext(), DriveRoutePopUp.class);
                    intent.putExtra("s_latlng", sL);
                    intent.putExtra("e_latlng", eL);
                    intent.putExtra("startPlace", start_txt.getText().toString());
                    intent.putExtra("endPlace", end_txt.getText().toString());
                    startActivityForResult(intent, 1116);
                }
            }
        });

        // 경로수정 버튼
        setEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (allPoints.size() == 1) {
                    Toast.makeText(getContext(), "도착지를 등록해 주세요", Toast.LENGTH_SHORT).show();
                } else {
                    LatLng sL = new LatLng(allPoints.get(0).latitude, allPoints.get(0).longitude);
                    LatLng eL = new LatLng(allPoints.get(1).latitude, allPoints.get(1).longitude);

                    // 경로등록
                    Intent intent = new Intent(getContext(), DriveRoutePopUp.class);
                    intent.putExtra("title", TITLE);
                    intent.putExtra("s_latlng", sL);
                    intent.putExtra("e_latlng", eL);
                    intent.putExtra("startPlace", start_txt.getText().toString());
                    intent.putExtra("endPlace", end_txt.getText().toString());
                    intent.putExtra("idx", EDIT_IDX);
                    startActivityForResult(intent, 1117);
                }
            }
        });

        // 수정취소 버튼
        setCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setOK.setVisibility(View.VISIBLE);
                setEdit.setVisibility(View.INVISIBLE);
                setCancel.setVisibility(View.INVISIBLE);

                Toast.makeText(getContext(), "경로수정을 취소했습니다.", Toast.LENGTH_SHORT).show();

                EDIT = 0;
                EDIT_IDX = 0;

                gMap.clear();
                allPoints.clear();
                myRouteLoad();
            }
        });

        // 나의 경로 선택
        myload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "경로를 선택하세요", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), MyRouteListPopUp.class);
                startActivityForResult(intent, 1115);
            }
        });
    }

    // Http Tmap 에서 길찾기 불러오기 + 거리, 소요시간, 요금 ---> 구글맵 폴리라인으로 그리기 (좌표 사용)
    public ArrayList<com.google.android.gms.maps.model.LatLng> getJsonData(final LatLng startPoint, final LatLng endPoint) {
        Thread thread = new Thread() {
            @Override
            public void run() {

                try {
                    URL acUrl = new URL(
                            "https://api2.sktelecom.com/tmap/routes?callback=application/json" +
                                    "&appKey=" + TMAP_API_KEY + "&version=1&totalValue=1&tollgateFareOption=8&searchOption=10" +
                                    "&endX=" + endPoint.longitude + "&endY=" + endPoint.latitude +
                                    "&startX=" + startPoint.longitude + "&startY=" + startPoint.latitude

                    );

                    HttpURLConnection acConn = (HttpURLConnection) acUrl.openConnection();
                    acConn.setRequestProperty("Accept", "application/json");
                    acConn.setRequestProperty("appKey", TMAP_API_KEY);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            acConn.getInputStream()));

                    String line = reader.readLine();
                    if (line == null) {
                        mapPoints.clear();
                    }
                    reader.close();

                    JSONObject jAr = null;
                    try {
                        jAr = new JSONObject(line);
                        JSONArray features = jAr.getJSONArray("features");
                        mapPoints = new ArrayList<>();

                        for (int i = 0; i < features.length(); i++) {
                            JSONObject featuresJSONObject = features.getJSONObject(i);

                            if (i == 0) {
                                JSONObject properties = featuresJSONObject.getJSONObject("properties");

                                String taxiFare = properties.getString("taxiFare");
                                String totalFare = properties.getString("totalFare");
                                String totalDistance = properties.getString("totalDistance");
                                String totalTime = properties.getString("totalTime");

                                Log.d("getTotal 택시:::::", "" + taxiFare);
                                Log.d("getTotal 요금:::::", "" + totalFare);
                                Log.d("getTotal 거리:::::", "" + totalDistance);
                                Log.d("getTotal 시간:::::", "" + totalTime);

                                double t_time = Integer.valueOf(totalTime) / 60;
                                long time = Math.round(t_time);
                                String meter = totalDistance.substring(totalDistance.length() - 3, totalDistance.length());
                                String km = totalDistance.substring(0, totalDistance.length() - 3);
                                double distance = Double.parseDouble(km + "." + meter);
                                double dis = Math.round(distance * 100d) / 100d;

                                if (taxiFare.length() > 2) {
                                    String beakWon = taxiFare.substring(taxiFare.length() - 3, taxiFare.length());
                                    String man = taxiFare.substring(0, taxiFare.length() - 3);
//                                    won = man + "," + beakWon;
//                                    total_taxi_fare.setText("이용요금 : " + won + "원");
                                } else {
//                                    total_taxi_fare.setText("이용요금 : " + taxiFare + "원");
                                }

//                                total_distance.setText("총 거리 : " + dis + "km");
//                                total_time.setText("소요시간 : 약 " + String.valueOf(time) + "분");
//                                total_fare.setText("통행료 : " + totalFare + "원");
                            }

                            JSONObject geometry = featuresJSONObject.getJSONObject("geometry");
                            JSONArray coordinates = geometry.getJSONArray("coordinates");


                            String geoType = geometry.getString("type");
                            // 꺽이는?? 특정 포인트 좌표
                            if (geoType.equals("Point")) {
                                double lonJson = coordinates.getDouble(0);
                                double latJson = coordinates.getDouble(1);

                                Log.d(TAG, "-");
                                Log.d(TAG, lonJson + "," + latJson + "\n");
                                com.google.android.gms.maps.model.LatLng point = new com.google.android.gms.maps.model.LatLng(latJson, lonJson);
                                mapPoints.add(point);

                            }
                            // 포인트 사이사이의 좌표
                            if (geoType.equals("LineString")) {
                                for (int j = 0; j < coordinates.length(); j++) {
                                    JSONArray JLinePoint = coordinates.getJSONArray(j);
                                    double lonJson = JLinePoint.getDouble(0);
                                    double latJson = JLinePoint.getDouble(1);

                                    Log.d(TAG, "-");
                                    Log.d(TAG, lonJson + "," + latJson + "\n");
                                    com.google.android.gms.maps.model.LatLng point = new com.google.android.gms.maps.model.LatLng(latJson, lonJson);

                                    mapPoints.add(point);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.d(TAG, "1\n");

                } catch (IOException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                }

            }
        };
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mapPoints;
    }

    // 장소 설정 후 출발, 도착 마커 커스텀
    private void setCustomMarkerView() {
        marker_root_view = LayoutInflater.from(getContext()).inflate(R.layout.custom_marker, null);
        tv_marker = (TextView) marker_root_view.findViewById(R.id.tv_marker);
    }

    // View를 Bitmap으로 변환
    private Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    // 자동 줌 설정 (경로안내)
    public void zoomRoute(List<LatLng> lstLatLngRoute) {
        Log.e("TTTTTTTTTTTTTTTTTTTTTTT", "자동 줌 설정 시작");
        if (gMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build();

        gMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
        Log.e("TTTTTTTTTTTTTTTTTTTTTTT", "자동 줌 설정 종료");
    }

    // 내 위치 불러오기
    private void getDeviceLocation() {
        Log.d("TAG", "내 위치");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getContext()));

        try {
            Task location = mFusedLocationClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful() && task.getResult() != null) {

                        Log.d("TAG", "성공");
                        Location currentLocation = (Location) task.getResult();

                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "내 위치");

                        Geocoder geocoder = new Geocoder(getContext());
                        List<Address> list = null;
                        try {
                            double lat = currentLocation.getLatitude();
                            double lon = currentLocation.getLongitude();
                            list = geocoder.getFromLocation(lat, lon, 10);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (list != null) {
                            if (list.size() == 0) {
                                start_txt.setText("해당되는 주소 정보는 없습니다.");
                            } else {
                                String ad = list.get(0).getSubLocality() + " " + list.get(0).getThoroughfare() + " " + list.get(0).getPostalCode();

                                latitude = currentLocation.getLatitude();
                                longitude = currentLocation.getLongitude();

                                start_txt.setText(ad);
//                                address = ad;
//                                start_btn.setHint("현위치 : " + ad);
                                Log.e("sub", ":::" + ad);
                            }
                        }
                    } else {
                        Log.d("TAG", "실패");
                    }
                }
            });

        } catch (SecurityException e) {
            Log.d("TAG", "널포인트!");
        }
    }

    // 주소 가져오기
    private String placeGetName(double lat, double lon) {

        Geocoder geocoder = new Geocoder(getContext());
        List<Address> list = null;
        try {
            list = geocoder.getFromLocation(lat, lon, 5);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String ad = null;
        if (list != null) {
            if (list.size() == 0) {
                ad = "해당되는 주소 정보는 없습니다.";
            } else {
                if (list.get(0).getSubLocality() == null) {
                    ad = list.get(0).getThoroughfare() + " " + list.get(0).getPostalCode();
                } else {
                    ad = list.get(0).getSubLocality() + " " + list.get(0).getThoroughfare() + " " + list.get(0).getPostalCode();
                }
                Log.e("sub", ":::" + ad);
            }
        }
        return ad;
    }

    // 카메라이동
    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d("TAG", "카메라 이동 lat : " + latLng.latitude + ", lng : " + latLng.longitude);
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!title.equals("내 위치")) {
            MarkerOptions options = new MarkerOptions().position(latLng).title(title);
            gMap.addMarker(options);
        }
        hideSoftKeyboard();
    }

    // 키보드 숨기기
    private void hideSoftKeyboard() {
//        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            Toast.makeText(getContext(), "등록을 취소 하였습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (requestCode == 1116) {
            Log.e("setResult( 1116):::", "경로설정 완료하고 돌아옴");
            int change = data.getIntExtra("change", 0);
            String title = data.getStringExtra("title");
            TITLE = title;
            // 출발 도착 좌표와 장소 이름
            LatLng sPosition = sMarkerOptions.getPosition();
            LatLng ePosition = eMarkerOptions.getPosition();
            String stitle = sMarkerOptions.getTitle();
            String etitle = eMarkerOptions.getTitle();

            S_TITLE = stitle;
            E_TITLE = etitle;

            if (change != 0) {
                // 맵 어떻게 받아서 넣을까
                captureScreen("추가변경");

                START_POINT = ePosition;
                END_POINT = sPosition;
                myRouteSave(title, etitle, stitle, String.valueOf(ePosition.latitude), String.valueOf(ePosition.longitude), String.valueOf(sPosition.latitude), String.valueOf(sPosition.longitude));
                Toast.makeText(getContext(), "출발지와 도착지를 변경했습니다.", Toast.LENGTH_SHORT).show();

                String st = start_txt.getText().toString();
                String et = end_txt.getText().toString();

                start_txt.setText(et);
                end_txt.setText(st);

                // 마커 바꿔주기 ( title, position )
                gMap.clear();
                sMarkerOptions.position(ePosition);
                eMarkerOptions.position(sPosition);
                sMarkerOptions.title(etitle);
                eMarkerOptions.title(stitle);
                gMap.addMarker(sMarkerOptions);
                gMap.addMarker(eMarkerOptions);

                // 경로 그려주기
                getJsonData(ePosition, sPosition);
                Log.e("바뀐 출발 좌표값", ":::" + ePosition);
                Log.e("바뀐 도착 좌표값", ":::" + sPosition);

                Polyline polyline = gMap.addPolyline(new PolylineOptions().addAll(mapPoints));
                polyline.setColor(Color.MAGENTA);
                zoomRoute(polyline.getPoints());
                start_Driver.driver_txt.setText("나의 경로 : " + title);

                Toast.makeText(getContext(), "경로가 등록되었습니다.", Toast.LENGTH_SHORT).show();

            } else {
                captureScreen("추가");

                START_POINT = sPosition;
                END_POINT = ePosition;
                myRouteSave(title, stitle, etitle, String.valueOf(sPosition.latitude), String.valueOf(sPosition.longitude), String.valueOf(ePosition.latitude), String.valueOf(ePosition.longitude));
                start_Driver.driver_txt.setText("나의 경로 : " + title);

                Toast.makeText(getContext(), "경로가 등록되었습니다.", Toast.LENGTH_SHORT).show();
//                gMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
//                    @Override
//                    public void onMapLoaded() {
//                        snapShot();
//                    }
//                });
            }
        } else if (requestCode == 1115) { // 나의 경로 아이템 클릭 갔다옴
            Log.e("갔다옴:::", "1115");
            EDIT = data.getIntExtra("edit", 0);
            String title = data.getStringExtra("title");
            TITLE = title;
            String sPlace = data.getStringExtra("startP");
            String ePlace = data.getStringExtra("endP");
            double sLat = data.getDoubleExtra("sLat", 0);
            double sLon = data.getDoubleExtra("sLon", 0);
            double eLat = data.getDoubleExtra("eLat", 0);
            double eLon = data.getDoubleExtra("eLon", 0);
            int idx = data.getIntExtra("idx", 0);

            Log.e("수정이냐 클릭이냐::", "숫자 뭐냐" + EDIT);
            Log.e("idx:::", "숫자 뭐냐~~" + idx);

            if (EDIT == 0) { // 경로 클릭
                showToast(title);
                myRouteClick(title, sPlace, ePlace, sLat, sLon, eLat, eLon);
            } else { // 경로 수정
                Toast.makeText(getContext(), "[" + title + "] 경로를 수정합니다.", Toast.LENGTH_SHORT).show();
                myRouteEdit(title, sPlace, ePlace, sLat, sLon, eLat, eLon, idx);
                EDIT_IDX = idx;
            }
        } else if (requestCode == 1117) { // 경로수정 완료 하고 돌아옴

            Log.e("setResult( 1117):::", "경로수정 완료하고 돌아옴");
            int change = data.getIntExtra("change", 0);
            int idx = data.getIntExtra("idx", 0);
            String title = data.getStringExtra("title");
            TITLE = title;
            // 출발 도착 좌표와 장소 이름
            LatLng sPosition = sMarkerOptions.getPosition();
            LatLng ePosition = eMarkerOptions.getPosition();
            String stitle = sMarkerOptions.getTitle();
            String etitle = eMarkerOptions.getTitle();

            S_TITLE = stitle;
            E_TITLE = etitle;
            EDIT_IDX = idx;

            if (change != 0) {
                // 맵 어떻게 받아서 넣을까
                captureScreen("수정변경");

                START_POINT = ePosition;
                END_POINT = sPosition;
                myRouteSave(title, etitle, stitle, String.valueOf(ePosition.latitude), String.valueOf(ePosition.longitude), String.valueOf(sPosition.latitude), String.valueOf(sPosition.longitude));
//                Toast.makeText(getContext(), "출발지와 도착지를 변경했습니다.", Toast.LENGTH_SHORT).show();

                String st = start_txt.getText().toString();
                String et = end_txt.getText().toString();

                start_txt.setText(et);
                end_txt.setText(st);

                // 마커 바꿔주기 ( title, position )
                gMap.clear();
                sMarkerOptions.position(ePosition);
                eMarkerOptions.position(sPosition);
                sMarkerOptions.title(etitle);
                eMarkerOptions.title(stitle);
                gMap.addMarker(sMarkerOptions);
                gMap.addMarker(eMarkerOptions);

                // 경로 그려주기
                getJsonData(ePosition, sPosition);
                Log.e("바뀐 출발 좌표값", ":::" + ePosition);
                Log.e("바뀐 도착 좌표값", ":::" + sPosition);

                Polyline polyline = gMap.addPolyline(new PolylineOptions().addAll(mapPoints));
                polyline.setColor(Color.MAGENTA);
                zoomRoute(polyline.getPoints());
                start_Driver.driver_txt.setText("나의 경로 : " + title);

                Toast.makeText(getContext(), "경로를 수정했습니다.", Toast.LENGTH_SHORT).show();

            } else {
                captureScreen("수정");

                START_POINT = sPosition;
                END_POINT = ePosition;
                myRouteSave(title, stitle, etitle, String.valueOf(sPosition.latitude), String.valueOf(sPosition.longitude), String.valueOf(ePosition.latitude), String.valueOf(ePosition.longitude));
                start_Driver.driver_txt.setText("나의 경로 : " + title);

                Toast.makeText(getContext(), "경로를 수정했습니다.", Toast.LENGTH_SHORT).show();
//                gMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
//                    @Override
//                    public void onMapLoaded() {
//                        snapShot();
//                    }
//                });

            }
            setOK.setVisibility(View.VISIBLE);
            setEdit.setVisibility(View.INVISIBLE);
            setCancel.setVisibility(View.INVISIBLE);
        }
    }

    // 나의 경로 등록
    private void performMyRoute(String name, double slat, double slon, double elat, double elon, String tit, String sPlace, String ePlace, String map) {

        Call<MyRoute> call = Login.apiInterface.performInsertMyRoute(name, slat, slon, elat, elon, tit, sPlace, ePlace, map);

        call.enqueue(new Callback<MyRoute>() {
            @Override
            public void onResponse(Call<MyRoute> call, Response<MyRoute> response) {
                Log.e("나의 경로등록:::", "성공");
            }

            @Override
            public void onFailure(Call<MyRoute> call, Throwable t) {
                Log.e("나의 경로등록:::", "" + t.getMessage());
            }
        });
    }

    // 나의 경로 수정
    private void performMyRouteEdit(String name, double slat, double slon, double elat, double elon, String tit, String sPlace, String ePlace, int idx, String map) {

        Call<MyRoute> call = Login.apiInterface.performInsertMyRouteEdit(name, slat, slon, elat, elon, tit, sPlace, ePlace, idx, map);

        call.enqueue(new Callback<MyRoute>() {
            @Override
            public void onResponse(Call<MyRoute> call, Response<MyRoute> response) {
                Log.e("나의 경로수정:::", "성공");
            }

            @Override
            public void onFailure(Call<MyRoute> call, Throwable t) {
                Log.e("나의 경로수정 실패:::", "" + t.getMessage());
            }
        });
    }

    // 구글맵 캡쳐
    public void captureScreen(final String activityResult) {
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                // TODO Auto-generated method stub
                bitmap = snapshot;

                OutputStream fout = null;

                String filePath = System.currentTimeMillis() + ".jpeg";
                Log.e("캡쳐 이미지 경로:::", ""+filePath);
                if (activityResult.equals("추가")) {
                    performMyRoute(USER_NAME, START_POINT.latitude, START_POINT.longitude, END_POINT.latitude, END_POINT.longitude, TITLE, S_TITLE, E_TITLE, filePath);
                } else if (activityResult.equals("추가변경")) {
                    performMyRoute(USER_NAME, END_POINT.latitude, END_POINT.longitude, START_POINT.latitude, START_POINT.longitude, TITLE, E_TITLE, S_TITLE, filePath);
                } else if (activityResult.equals("수정")) {
                    performMyRouteEdit(USER_NAME, START_POINT.latitude, START_POINT.longitude, END_POINT.latitude, END_POINT.longitude, TITLE, S_TITLE, E_TITLE, EDIT_IDX, filePath);
                } else if (activityResult.equals("수정변경")) {
                    performMyRouteEdit(USER_NAME, END_POINT.latitude, END_POINT.longitude, START_POINT.latitude, START_POINT.longitude, TITLE, E_TITLE, S_TITLE, EDIT_IDX, filePath);
                }

                try {
                    fout = getContext().openFileOutput(filePath, getContext().MODE_PRIVATE);

                    // Write the string to the file
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
//                    test.setVisibility(View.VISIBLE);
//                    test.setImageBitmap(bitmap);
                    fout.flush();
                    fout.close();
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    Log.d("ImageCapture", "FileNotFoundException");
                    Log.d("ImageCapture", e.getMessage());
                    filePath = "";
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.d("ImageCapture", "IOException");
                    Log.d("ImageCapture", e.getMessage());
                    filePath = "";
                }
                openShareImageDialog(filePath);
            }
        };
        gMap.snapshot(callback);
//        gMap.clear();
//        start_txt.setText("출발지");
//        end_txt.setText("도착지");

    }

    // 캡쳐한 이미지 처리
    public void openShareImageDialog(String filePath) {
        File file = getContext().getFileStreamPath(filePath);

        if (!filePath.equals("")) {
            final ContentValues values = new ContentValues(2);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            final Uri contentUriFile = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Log.e("Uri 값:::", "" + contentUriFile);
            String path = getRealPathFromURI(contentUriFile);

            File upload_file = new File(path);
            // Parsing any Media type file
            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), upload_file);
            MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("fileToUpload", upload_file.getName(), requestBody);

//            RequestBody description = RequestBody.create(MultipartBody.FORM, driver_txt.getText().toString());
//
//            RequestBody filePart = RequestBody.create(MediaType.parse(getContext().getContentResolver().getType(contentUriFile)), FileUtils.getFile);

            Call<ResponseBody> call = Login.apiInterface.uploadImage(requestBody, fileToUpload);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
//                        System.out.println("Image Url: " + response.body().getImage() + " " + response.body().getDishes_name());
                        Log.e("리스폰 성공","성공: " + response.body());
                    } else {
                        Log.e("리스폰 성공","onFailure: " + response.raw());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("리스폰 실패","onFailure: " + t.getMessage());
                }
            });
            Log.e("retrofit:", "지나간다");
        } else {
            //This is a custom class I use to show dialogs...simply replace this with whatever you want to show an error message, Toast, etc.
            Toast.makeText(getContext(), "EEEEERRRRRRRR", Toast.LENGTH_SHORT).show();
        }

    }

    // uri --> 절대경로
    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContext().getContentResolver().query(contentUri, proj, null, null, null);
        assert cursor != null;
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
        Log.e("절대경로 값:::", "" + path);
        Uri uri = Uri.fromFile(new File(path));
        Log.e(TAG, "getRealPathFromURI(), path : " + uri.toString());
        cursor.close();
        return path;
    }

    // 경로 제목 바꿔주기
    @Override
    public void showToast(String place) {
        driver_txt.setText("나의 경로 : " + place);
    }

    // 나의 경로 아이템 클릭 (운전자의 출발 도착 좌표 저장하기)
    @Override
    public void myRouteClick(String title, String sPlace, String ePlace, double sLat, double sLon, double eLat, double eLon) {
        Log.e("나의경로 프레그먼트", "아이템클릭??");
        gMap.clear();
        allPoints.clear();
        TITLE = title;
        setOK.setVisibility(View.INVISIBLE);
        setEdit.setVisibility(View.INVISIBLE);
        setCancel.setVisibility(View.INVISIBLE);

        start_txt.setText(sPlace);
        end_txt.setText(ePlace);

        // 출발 도착 좌표와 장소 이름
        LatLng sPosition = new LatLng(sLat, sLon);
        LatLng ePosition = new LatLng(eLat, eLon);
        allPoints.add(sPosition);
        allPoints.add(ePosition);
        START_POINT = sPosition;
        END_POINT = ePosition;
        myRouteSave(title, sPlace, ePlace, String.valueOf(sLat), String.valueOf(sLon), String.valueOf(eLat), String.valueOf(eLon));

        // 출발 마커
        tv_marker.setText("출발");
        tv_marker.setBackgroundResource(R.drawable.start_picker);
        tv_marker.setTextColor(Color.BLACK);

        sMarkerOptions = new MarkerOptions();
        sMarkerOptions.title(sPlace);
        sMarkerOptions.position(sPosition);
        sMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getContext(), marker_root_view)));
        gMap.addMarker(sMarkerOptions);

        // 도착 마커
        tv_marker.setText("도착");
        tv_marker.setBackgroundResource(R.drawable.end_picker);
        tv_marker.setTextColor(Color.WHITE);

        eMarkerOptions = new MarkerOptions();
        eMarkerOptions.title(ePlace);
        eMarkerOptions.position(ePosition);
        eMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getContext(), marker_root_view)));
        gMap.addMarker(eMarkerOptions);

        // 경로 그려주기
        getJsonData(sPosition, ePosition);
        Polyline polyline = gMap.addPolyline(new PolylineOptions().addAll(mapPoints));
        polyline.setColor(Color.MAGENTA);
        zoomRoute(polyline.getPoints());
    }

    // 경로 수정
    @Override
    public void myRouteEdit(String title, String sPlace, String ePlace, double sLat, double sLon, double eLat, double eLon, int idx) {
        Log.e("나의경로 프레그먼트", "아이템클릭??");
        gMap.clear();
        allPoints.clear();
        TITLE = title;
        driver_txt.setText("나의 경로 : " + title);
        setOK.setVisibility(View.INVISIBLE);
        setEdit.setVisibility(View.VISIBLE);
        setCancel.setVisibility(View.VISIBLE);

        start_txt.setText(sPlace);
        end_txt.setText(ePlace);

        // 출발 도착 좌표와 장소 이름
        LatLng sPosition = new LatLng(sLat, sLon);
        LatLng ePosition = new LatLng(eLat, eLon);
        allPoints.add(sPosition);
        allPoints.add(ePosition);
        START_POINT = sPosition;
        END_POINT = ePosition;
        myRouteSave(title, sPlace, ePlace, String.valueOf(sLat), String.valueOf(sLon), String.valueOf(eLat), String.valueOf(eLon));

        // 출발 마커
        tv_marker.setText("출발");
        tv_marker.setBackgroundResource(R.drawable.start_picker);
        tv_marker.setTextColor(Color.BLACK);

        sMarkerOptions = new MarkerOptions();
        sMarkerOptions.title(sPlace);
        sMarkerOptions.position(sPosition);
        sMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getContext(), marker_root_view)));
        gMap.addMarker(sMarkerOptions);

        // 도착 마커
        tv_marker.setText("도착");
        tv_marker.setBackgroundResource(R.drawable.end_picker);
        tv_marker.setTextColor(Color.WHITE);

        eMarkerOptions = new MarkerOptions();
        eMarkerOptions.title(ePlace);
        eMarkerOptions.position(ePosition);
        eMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getContext(), marker_root_view)));
        gMap.addMarker(eMarkerOptions);

        // 경로 그려주기
        getJsonData(sPosition, ePosition);
        Polyline polyline = gMap.addPolyline(new PolylineOptions().addAll(mapPoints));
        polyline.setColor(Color.MAGENTA);
        zoomRoute(polyline.getPoints());
    }

    // 경로 삭제
    @Override
    public void myRouteDel(String title, int idx) {

    }

    // 운전자 경로 저장
    private void myRouteSave(String title, String sPlace, String ePlace, String sLat, String sLon, String eLat, String eLon) {
        SharedPreferences preferences = getContext().getSharedPreferences("myRoute", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("title", title);
        editor.putString("sPlace", sPlace);
        editor.putString("ePlace", ePlace);
        editor.putString("sLat", sLat);
        editor.putString("sLon", sLon);
        editor.putString("eLat", eLat);
        editor.putString("eLon", eLon);
        editor.apply();
    }

    // 운전자 경로 불러오기
    private void myRouteLoad() {
        SharedPreferences preferences = getContext().getSharedPreferences("myRoute", Context.MODE_PRIVATE);

        String title = preferences.getString("title", "나의 경로");
        TITLE = title;
        String sPlace = preferences.getString("sPlace", "출발지");
        String ePlace = preferences.getString("ePlace", "도착지");
        String sLatitude = preferences.getString("sLat", "0");
        String sLongitude = preferences.getString("sLon", "0");
        String eLatitude = preferences.getString("eLat", "0");
        String eLongitude = preferences.getString("eLon", "0");
        double sLat = Double.parseDouble(sLatitude);
        double sLon = Double.parseDouble(sLongitude);
        double eLat = Double.parseDouble(eLatitude);
        double eLon = Double.parseDouble(eLongitude);
        START_POINT = new LatLng(sLat, sLon);
        END_POINT = new LatLng(eLat, eLon);

        if (sLatitude.equals("0") && sLongitude.equals("0") && eLatitude.equals("0") && eLongitude.equals("0")) {
            Toast.makeText(getContext(), "운행할 경로를 등록해 주세요", Toast.LENGTH_SHORT).show();
            getDeviceLocation();
        } else {
            driver_txt.setText("나의 경로 : " + title);
            start_txt.setText(sPlace);
            end_txt.setText(ePlace);

            // 출발 도착 좌표와 장소 이름
            LatLng sPosition = new LatLng(sLat, sLon);
            LatLng ePosition = new LatLng(eLat, eLon);
            allPoints.clear();
            allPoints.add(sPosition);
            allPoints.add(ePosition);
            START_POINT = sPosition;
            END_POINT = ePosition;
            myRouteSave(title, sPlace, ePlace, String.valueOf(sLat), String.valueOf(sLon), String.valueOf(eLat), String.valueOf(eLon));

            // 출발 마커
            tv_marker.setText("출발");
            tv_marker.setBackgroundResource(R.drawable.start_picker);
            tv_marker.setTextColor(Color.BLACK);

            sMarkerOptions = new MarkerOptions();
            sMarkerOptions.title(sPlace);
            sMarkerOptions.position(sPosition);
            sMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getContext(), marker_root_view)));
            gMap.addMarker(sMarkerOptions);

            // 도착 마커
            tv_marker.setText("도착");
            tv_marker.setBackgroundResource(R.drawable.end_picker);
            tv_marker.setTextColor(Color.WHITE);

            eMarkerOptions = new MarkerOptions();
            eMarkerOptions.title(ePlace);
            eMarkerOptions.position(ePosition);
            eMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getContext(), marker_root_view)));
            gMap.addMarker(eMarkerOptions);

            // 경로 그려주기
            getJsonData(sPosition, ePosition);
            Polyline polyline = gMap.addPolyline(new PolylineOptions().addAll(mapPoints));
            polyline.setColor(Color.MAGENTA);
            zoomRoute(polyline.getPoints());
        }
    }

}
