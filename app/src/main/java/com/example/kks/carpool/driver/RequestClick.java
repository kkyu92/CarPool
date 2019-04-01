package com.example.kks.carpool.driver;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kks.carpool.R;

import com.example.kks.carpool.google_map;
import com.example.kks.carpool.service.RealService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.kks.carpool.AutoCompleteParse.TMAP_API_KEY;
import static com.example.kks.carpool.Result.USER_NAME;
import static com.example.kks.carpool.Result.USER_PROFILE;
import static com.example.kks.carpool.driver.FragmentMyLoad.END_POINT;
import static com.example.kks.carpool.driver.FragmentMyLoad.START_POINT;
import static com.example.kks.carpool.google_map.DEFAULT_ZOOM;

public class RequestClick extends AppCompatActivity implements OnMapReadyCallback {

    private String TAG = "(카풀요청 클릭) 자동차 경로";
    private SimpleDateFormat dbFormat_Date = new SimpleDateFormat("HH:mm", Locale.KOREAN);
    public static SimpleDateFormat datetimeFormat_Date = new SimpleDateFormat("MM월 dd일 (E) HH:mm", Locale.KOREAN);

//    RouteThread routeThread;
//    private RouteAsyncTask routeAsyncTask;

    // 레이아웃
    private GoogleMap gMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private RatingBar ratingBar;
    private ImageButton cancel_btn;
    private TextView request_time, request_people, start_place, start_dis_time, end_place, end_dis_time, request_fare;
    private ConstraintLayout request_OK;
    private Button myRoute, requestRoute;

    // 커스텀 마커
    private Marker sMarker, eMarker;
    private MarkerOptions e_markerOptions, s_markerOptions;
    private View marker_root_view;
    private TextView tv_marker;

    // 자동차 경로정보
    private ArrayList<LatLng> mapPoints, requestPoints;
    private long toStart;
    private double allDistance;

    // 받아온 요청 출발 도착 좌표
    private String start, end;
    private LatLng getStart, getEnd;
    private Calendar calendar, calendarDriver;

    // 나의 경로 클릭
    private String arrive_time, arrive_clock, arrive_distance;

    private String TARGET_ID, TARGET_PROFILE;
    public static String D_REQUEST_NUM;

    private double myLat, myLon;
    private String sTime="";

    // 서비스 부분
    Intent serviceIntent;
    int a = 1;
    private RealService ms; // 서비스 객체
    boolean isService = false; // 서비스 중인 확인용

    private String arriveTime;

    // 서비스에서 받아옴 -- 노티 띄우기, 채팅아이템 추가하기
    public RealService.ICallback mCallback = new RealService.ICallback() {
        @Override
        public void RiderRemoteCall(String msg) {

        }

        @Override
        public void DriverRemoteCall(String msg) {
            // 메세지가 왔다면.
            //String getMSG = msg.obj.toString();
//            unbindService(conn);
            // 방번호, 보낸사람, 받는 사람, 메세지
            // 받을때는 보낸사람, 보낸내용, 보낸시간
            String[] filt1 = msg.split("@");

            if (filt1[1].equals("수락확인~!")) {
                Toast.makeText(RequestClick.this, filt1[1], Toast.LENGTH_SHORT).show();
                Log.d("받은 메세지 ", msg);
                TARGET_PROFILE = filt1[3];
                String target_lat = filt1[4];
                String target_lon = filt1[5];

                String datetime = datetimeFormat_Date.format(dateTime.getTime());
                int p;
                switch (request_people.getText().toString()) {
                    case "1 인":
                        p = 1;
                        break;
                    case "2 인":
                        p = 2;
                        break;
                    default:
                        p = 3;
                        break;
                }

                Intent intent = new Intent(RequestClick.this, ConnectRider.class);
                intent.putExtra("start", start);
                intent.putExtra("end", end);
                intent.putExtra("date", datetime);
                Log.e("날짜형식::::", "" + datetime);
                intent.putExtra("time", req_time);
                intent.putExtra("Atime", request_time.getText().toString());
                intent.putExtra("sTime", sTime);

                intent.putExtra("dis", req_dis);
                intent.putExtra("fare", request_fare.getText().toString());
                intent.putExtra("people", p);
                intent.putExtra("rider", TARGET_ID);
                intent.putExtra("rider_profile", TARGET_PROFILE);
                intent.putExtra("roomNum", Integer.valueOf(D_REQUEST_NUM));

                intent.putExtra("sLat", getStart.latitude);
                intent.putExtra("sLon", getStart.longitude);
                intent.putExtra("eLat", getEnd.latitude);
                intent.putExtra("eLon", getEnd.longitude);

                intent.putExtra("target_lat", target_lat);
                intent.putExtra("target_lon", target_lon);

                startActivity(intent);
                finish();
            } else {
                /** 수업중 수정 **/
//                        if (filt1[1].equals("요청이 종료되었습니다.")) {
//                            Log.e("요청종료된 사용자","요청종료 : "+filt1[1]);
//                        } else {
                Toast.makeText(RequestClick.this, filt1[1], Toast.LENGTH_SHORT).show();
                Log.d("받은 메세지 ", msg);
                setResult(RESULT_OK);
                finish();
//                        }
            }
        }

        @Override
        public void VideoCall(String toDriver) { // 요청수락하는 부분이라 영통 관련 없다

        }

        @Override
        public void LocationCall(float bearing, double lat, double lon) {

        }
    };

    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 서비스와 연결되었을 때 호출되는 메서드
            // 서비스 객체를 전역변수로 저장
            RealService.MyBinder mb = (RealService.MyBinder) service;
            ms = mb.getService(); // 서비스가 제공하는 메소드 호출하여
            ms.registerCallback(mCallback);
            ms.getREQUEST_NUM(D_REQUEST_NUM);
            // 서비스쪽 객체를 전달받을수 있슴
            isService = true;

//            request_OK.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Toast.makeText(RequestClick.this, "카풀요청 수락", Toast.LENGTH_SHORT).show();
//                    sendMatch("매칭수락~!");
//                }
//            });
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.e("바인드 서비스", "::::: 서비스 연결 끊김");
            // 서비스와 연결이 끊겼을 때 호출되는 메서드
            isService = false;
        }
    };

    //
    private Date dateTime;
    private String req_time, req_dis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_click);

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ratingBar = findViewById(R.id.rating);
        cancel_btn = findViewById(R.id.cancel_btn);
        request_time = findViewById(R.id.request_time);
        request_people = findViewById(R.id.request_people);
        start_place = findViewById(R.id.start_place);
        start_dis_time = findViewById(R.id.start_dis_time);
        end_place = findViewById(R.id.end_place);
        end_dis_time = findViewById(R.id.end_dis_time);
        request_fare = findViewById(R.id.request_fare);
        request_OK = findViewById(R.id.request_OK);
        myRoute = findViewById(R.id.my_route);
        requestRoute = findViewById(R.id.request_route);

        Intent intent = getIntent();
        double sLat = intent.getDoubleExtra("sLat", 0);
        double sLon = intent.getDoubleExtra("sLon", 0);
        double eLat = intent.getDoubleExtra("eLat", 0);
        double eLon = intent.getDoubleExtra("eLon", 0);
        String date = intent.getStringExtra("date");
        String time = intent.getStringExtra("time");
        String people = intent.getStringExtra("people");
        String fare = intent.getStringExtra("fare");
        double rating = intent.getFloatExtra("rating", 0);
        String name = intent.getStringExtra("name");
        String idx = intent.getStringExtra("idx");

        TARGET_ID = name;
        D_REQUEST_NUM = idx;

        getStart = new LatLng(sLat, sLon);
        getEnd = new LatLng(eLat, eLon);

        StringBuffer dateBuffer = new StringBuffer(date);
        int year = Integer.valueOf(dateBuffer.substring(0, 4));
        int month = Integer.valueOf(dateBuffer.substring(5, 7));
        int day = Integer.valueOf(dateBuffer.substring(8, 10));

        StringBuffer timeBuffer = new StringBuffer(time);
        int hour = Integer.valueOf(timeBuffer.substring(0, 2));
        int min = Integer.valueOf(timeBuffer.substring(3, 5));

        calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, min);

        calendarDriver = Calendar.getInstance();
        calendarDriver.set(year, month - 1, day, hour, min);

        dateTime = calendarDriver.getTime();
        Log.e("날짜 셋:::", "켈린더:::" + dbFormat_Date.format(calendar.getTime()));

        // 장소명 변환 (출발, 도착)
        start = setGeoCoder(sLat, sLon);
        end = setGeoCoder(eLat, eLon);

        // 경로정보 ( 1: 설정한 나의 위치에서 출발지 / 2: 출발지에서 도착지 / 3 : 도착지에서 나의 도착지 )
        mapPoints = new ArrayList<>();
        requestPoints = new ArrayList<>();

        ratingBar.setRating((float) rating);
        request_time.setText(date + " / " + time);
        sTime = time;
        request_people.setText(people);
        start_place.setText(start);
//        start_dis_time.setText();
        end_place.setText(end);
//        end_dis_time.setText();
        request_fare.setText(fare);

        Log.e("getIntent:::", "sLat : " + sLat);
        Log.e("getIntent:::", "sLon : " + sLon);
        Log.e("getIntent:::", "eLat : " + eLat);
        Log.e("getIntent:::", "eLon : " + eLon);
        Log.e("getIntent:::", "date : " + date);
        Log.e("getIntent:::", "time : " + time);
        Log.e("getIntent:::", "people : " + people);
        Log.e("getIntent:::", "fare : " + fare);
        Log.e("getIntent:::", "rating : " + rating);
        Log.e("getIntent:::", "name : " + name);
        Log.e("getIntent:::", "idx : " + idx);

        if (RealService.serviceIntent == null) {
            serviceIntent = new Intent(RequestClick.this, RealService.class);
            startService(serviceIntent);
            Log.e("int a = ", "::::" + a);
            if (a == 1) {
                bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
            }
        } else {
            serviceIntent = RealService.serviceIntent;//getInstance().getApplication();
            Toast.makeText(getApplicationContext(), "already", Toast.LENGTH_LONG).show();
            // 재실행 됬을때 바인드 다시해줘야함
//            bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        Log.e("onMapReady:::::", "onMapReady");

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
        gMap.getUiSettings().setMyLocationButtonEnabled(false);
        setCustomMarkerView();


        // 나의 출발 ~ 요청 출발
        getJsonData(START_POINT, getStart);
        // 요청 출발 ~ 요청 도착
        getJsonData(getStart, getEnd);
        // 요청 도착 ~ 나의 도착
        getJsonData(getEnd, END_POINT);

        Polyline myPolyline = gMap.addPolyline(new PolylineOptions().addAll(mapPoints));
        myPolyline.setWidth(15);
        myPolyline.setColor(Color.BLUE);

        Polyline requestPolyline = gMap.addPolyline(new PolylineOptions().addAll(requestPoints));
        requestPolyline.setWidth(8);
        requestPolyline.setColor(Color.MAGENTA);

        getDeviceLocation();

        setStartMarker(getStart.latitude, getStart.longitude, start);
        setEndMarker(getEnd.latitude, getEnd.longitude, end);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 취소버튼
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // 나의 경로 버튼
        myRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateMarker(getStart, START_POINT, sMarker);
                animateMarker(getEnd, END_POINT, eMarker);

                sMarker.setTitle("나의 경로");
                eMarker.setTitle("도착");
                eMarker.setSnippet(
                        "  총   이동거리 : " + arrive_distance + "km" + "\n" +
                                "  총   운행시간 : " + arrive_time + "분" + "\n" +
                                "예상 도착시간 : " + arrive_clock
                );
//                s_markerOptions.position(START_POINT);
//                e_markerOptions.position(END_POINT);
                // 나의경로 출발도착 타이틀 총 예상소요시간 / 총 예상도착시간
                gMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {

                        Context mContext = RequestClick.this;

                        LinearLayout info = new LinearLayout(mContext);
                        info.setOrientation(LinearLayout.VERTICAL);

                        TextView title = new TextView(mContext);
                        title.setTextColor(Color.BLACK);
                        title.setGravity(Gravity.CENTER);
                        title.setTypeface(null, Typeface.BOLD);
                        title.setText(marker.getTitle());

                        TextView snippet = new TextView(mContext);
                        snippet.setTextColor(Color.GRAY);
                        snippet.setText(marker.getSnippet());

                        info.addView(title);
                        info.addView(snippet);

                        return info;
                    }
                });
                zoomRoute(mapPoints);
            }
        });

        // 요청 경로 버튼
        requestRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateMarker(START_POINT, getStart, sMarker);
                animateMarker(END_POINT, getEnd, eMarker);
//                s_markerOptions.position(getStart);
                sMarker.setTitle(start);
//                e_markerOptions.position(getEnd);
                eMarker.setTitle(end);
                eMarker.setSnippet("");
                zoomRoute(requestPoints);
            }
        });

        // 카풀 요청 수락 버튼
        request_OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(RequestClick.this, "카풀요청 수락", Toast.LENGTH_SHORT).show();
                sendMatch("매칭수락~!");
            }
        });

        // 서버로부터 수신한 메세지를 처리하는 곳  ( AsyncTesk를  써도됨 )
//        msgHandler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                if (msg.what == 1112) {
//                    // 메세지가 왔다면.
//                    String getMSG = msg.obj.toString();
//
//                    // 방번호, 보낸사람, 받는 사람, 메세지
//                    // 받을때는 보낸사람, 보낸내용, 보낸시간
//                    String[] filt1 = getMSG.split("@");
//
//                    if (filt1[1].equals("매칭수락~!")) {
//                        Toast.makeText(RequestClick.this, filt1[1], Toast.LENGTH_SHORT).show();
//                        Log.d("받은 메세지 ", msg.obj.toString());
//                        TARGET_PROFILE = filt1[3];
//
//                        String datetime = datetimeFormat_Date.format(dateTime.getTime());
//                        int p;
//                        switch (request_people.getText().toString()) {
//                            case "1 인":
//                                p = 1;
//                                break;
//                            case "2 인":
//                                p = 2;
//                                break;
//                            default:
//                                p = 3;
//                                break;
//                        }
//
//                        Intent intent = new Intent(RequestClick.this, ConnectRider.class);
//                        intent.putExtra("start", start);
//                        intent.putExtra("end", end);
//                        intent.putExtra("date", datetime);
//                        Log.e("날짜형식::::", "" + datetime);
//                        intent.putExtra("time", req_time);
//                        intent.putExtra("dis", req_dis);
//                        intent.putExtra("fare", request_fare.getText().toString());
//                        intent.putExtra("people", p);
//                        intent.putExtra("rider", TARGET_ID);
//                        intent.putExtra("rider_profile", TARGET_PROFILE);
//                        intent.putExtra("roomNum", Integer.valueOf(REQUEST_NUM));
//                        startActivity(intent);
//                        finish();
//                    } else {
//                        /** 수업중 수정 **/
////                        if (filt1[1].equals("요청이 종료되었습니다.")) {
////                            Log.e("요청종료된 사용자","요청종료 : "+filt1[1]);
////                        } else {
//                            Toast.makeText(RequestClick.this, filt1[1], Toast.LENGTH_SHORT).show();
//                            Log.d("받은 메세지 ", msg.obj.toString());
//                            setResult(RESULT_OK);
//                            finish();
////                        }
//                    }
////                    TARGET_ID = msg.obj.toString();
////                    Log.d("타겟 아이디 ", TARGET_ID);
//
////                    msgFilter = msg.obj.toString().split("@");
////
////
////                    // 수신 1
////                    messageContent = new ChattingMessageContent(1, msgFilter[0], targetNickName, msgFilter[1], msgFilter[2]);
////
////                    messageData.add(messageContent);
////                    chattingRoomAdapter.notifyDataSetChanged();
//
//                } else if (msg.what == 1112) {
//                    Log.e("운전자) 요청확인부분:::", "채팅중 노티 띄워줄것?");
//                } else {
//                    Log.e("msg.what::::", "~~~" + msg.what);
//                }
//            }
//        };

    }

    // 내 위치 불러오기
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
                        myLat = currentLocation.getLatitude();
                        myLon = currentLocation.getLongitude();
                        zoomRoute(mapPoints);
                    } else {
                        Log.d("TAG", "실패");
                    }
                }
            });
        } catch (SecurityException e) {
            Log.d("TAG", "널포인트!");
        }
    }

    // 카메라이동
    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d("TAG", "카메라 이동 lat : " + latLng.latitude + ", lng : " + latLng.longitude);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!title.equals("내 위치")) {
            MarkerOptions options = new MarkerOptions().position(latLng).title(title);
            gMap.addMarker(options);
        }
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

    // 장소 설정 후 출발, 도착 마커 커스텀
    private void setCustomMarkerView() {
        marker_root_view = LayoutInflater.from(this).inflate(R.layout.custom_marker, null);
        tv_marker = marker_root_view.findViewById(R.id.tv_marker);
    }

    // 장소명 받기 (GeoCoder)
    private String setGeoCoder(double lat, double lon) {
        String geoAddr = null;
        Geocoder geocoder = new Geocoder(RequestClick.this);
        List<Address> list = null;
        try {
            list = geocoder.getFromLocation(lat, lon, 20);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (list != null) {
            if (list.size() == 0) {
                geoAddr = "해당되는 주소 정보는 없습니다.";
            } else {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getSubLocality() != null && list.get(0).getThoroughfare() != null) {
                        geoAddr = list.get(0).getSubLocality() + " " + list.get(0).getThoroughfare();
                        break;
                    } else {
                        geoAddr = "해당되는 주소 정보는 없습니다.";
                    }
                }
                Log.e("sub", ":::" + geoAddr);
            }
        }
        return geoAddr;
    }

    // Http Tmap 에서 길찾기 불러오기 + 거리, 소요시간, 요금 ---> 구글맵 폴리라인으로 그리기 (좌표 사용)
    public void getJsonData(final LatLng startPoint, final LatLng endPoint) {
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

                    InputStreamReader inputStreamReader = new InputStreamReader(acConn.getInputStream());
                    BufferedReader reader = new BufferedReader(inputStreamReader);

                    String line = reader.readLine();
                    if (line == null) {
                        Log.e("null 일때", "좌표값 초기화");
                        mapPoints.clear();
                        requestPoints.clear();
                    }
                    reader.close();
                    inputStreamReader.close();
//                    acConn.disconnect();

                    JSONObject jAr = null;
                    try {
                        jAr = new JSONObject(line);
                        JSONArray features = jAr.getJSONArray("features");
//                        mapPoints = new ArrayList<>();
//                        requestPoints = new ArrayList<>();

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

                                double distance;
                                if (totalDistance.length() >= 4) {
                                    String meter = totalDistance.substring(totalDistance.length() - 3, totalDistance.length());
                                    String km = totalDistance.substring(0, totalDistance.length() - 3);
                                    distance = Double.parseDouble(km + "." + meter);
//                                    double dis = Math.round(distance * 100d) / 100d;
                                } else {
                                    distance = Double.parseDouble("0." + totalDistance);
//                                    double dis = Math.round(distance * 100d) / 100d;
                                }
                                double dis = Math.round(distance * 100d) / 100d;

                                String beakWon, man, won = null;
                                StringBuffer sUnder = new StringBuffer();
                                StringBuffer eUnder = new StringBuffer();

                                if (START_POINT == startPoint) { // 나의 출발위치 ~~ 요청 출발까지
                                    sUnder.append(dis);
                                    allDistance += dis;
                                    sUnder.append("km / ");
                                    sUnder.append(String.valueOf(time));
                                    sUnder.append("분 소요예상");
                                    toStart += time;
                                    start_dis_time.setText(sUnder);
                                } else if (getStart == startPoint) { // 요청 출발 ~~ 요청 도착
                                    eUnder.append(dis);
                                    allDistance += dis;
                                    eUnder.append("km / ");
                                    toStart += time;
                                    int addtime = (int) time;
                                    calendar.add(Calendar.MINUTE, addtime);
                                    Log.e("도착시간 계산:::", addtime + "분 추가~~" + dbFormat_Date.format(calendar.getTime()));
                                    eUnder.append(dbFormat_Date.format(calendar.getTime()));
                                    eUnder.append(" 도착예상");
                                    end_dis_time.setText(eUnder);
                                    arriveTime = dbFormat_Date.format(calendar.getTime());

                                    req_time = String.valueOf(time);
                                    req_dis = String.format("%.2f", dis);
                                } else if (getEnd == startPoint) { // 요청 도착 ~~ 나의 도착
                                    allDistance += dis;
                                    toStart += time;
                                    int addtime = (int) time;
                                    calendar.add(Calendar.MINUTE, addtime);

                                    // 총 이동에 관한 정보
//                                    arrive_distance = Math.round((allDistance*100)/100.0);
                                    arrive_distance = String.format("%.2f", allDistance);
                                    arrive_clock = dbFormat_Date.format(calendar.getTime());
                                    arrive_time = String.valueOf(toStart);
                                    Log.e("총 움직일 거리:::", "" + arrive_distance);
                                    Log.e("도착할 시간:::", "" + arrive_clock);
                                    Log.e("총 운행할 시간:::", "" + toStart);
                                }
//                                if (taxiFare.length() > 2) {
//                                    beakWon = taxiFare.substring(taxiFare.length() - 3, taxiFare.length());
//                                    man = taxiFare.substring(0, taxiFare.length() - 3);
//                                    won = man + "," + beakWon;
//                                } else {
//
//                                }
//                                total_taxi_fare.setText("이용요금 : " + won + "원");
//                                total_taxi_fare.setText("이용요금 : " + taxiFare + "원");
//
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

//                                Log.d(TAG, "-");
//                                Log.d(TAG, lonJson + "," + latJson + "\n");
                                LatLng point = new LatLng(latJson, lonJson);

                                if (START_POINT == startPoint) { // 나의 출발위치 ~~ 요청 출발까지
                                    mapPoints.add(point);
                                } else if (getStart == startPoint) { // 요청 출발 ~~ 요청 도착
                                    mapPoints.add(point);
                                    requestPoints.add(point);
                                } else if (getEnd == startPoint) { // 요청 도착 ~~ 나의 도착
                                    mapPoints.add(point);
                                }
                            }
                            // 포인트 사이사이의 좌표
                            if (geoType.equals("LineString")) {
                                for (int j = 0; j < coordinates.length(); j++) {
                                    JSONArray JLinePoint = coordinates.getJSONArray(j);
                                    double lonJson = JLinePoint.getDouble(0);
                                    double latJson = JLinePoint.getDouble(1);

//                                    Log.d(TAG, "-");
//                                    Log.d(TAG, lonJson + "," + latJson + "\n");
                                    LatLng point = new LatLng(latJson, lonJson);

                                    if (START_POINT == startPoint) { // 나의 출발위치 ~~ 요청 출발까지
                                        mapPoints.add(point);
                                    } else if (getStart == startPoint) { // 요청 출발 ~~ 요청 도착
                                        mapPoints.add(point);
                                        requestPoints.add(point);
                                    } else if (getEnd == startPoint) { // 요청 도착 ~~ 나의 도착
                                        mapPoints.add(point);
                                    }
                                }
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "1\n");

//                    if (reader.ready() && inputStreamReader.ready()) {
//                        reader.close();
//                        inputStreamReader.close();
//                    }
//                    acConn.disconnect();
                } catch (IOException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                }

            }
        };
        thread.start();

        try {
            thread.join();
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        return mapPoints;
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

    // 출발 마커
    private void setStartMarker(double lat, double lon, String place) {
        // 출발지 마커
        LatLng s_position = new LatLng(lat, lon);
        tv_marker.setText("출발");
        tv_marker.setBackgroundResource(R.drawable.start_picker);
        tv_marker.setTextColor(Color.BLACK);

        // 출발지 마커옵션 (찍을 위치좌표, 들어갈 텍스트 설정, 커스텀 추가)
        s_markerOptions = new MarkerOptions();
        s_markerOptions.title(place);
        s_markerOptions.position(s_position);
        s_markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker_root_view)));
        sMarker = gMap.addMarker(s_markerOptions);
    }

    // 도착 마커
    private void setEndMarker(double lat, double lon, String place) {
        // 도착지 마커
        LatLng e_position = new LatLng(lat, lon);
        tv_marker.setText("도착");
        tv_marker.setBackgroundResource(R.drawable.end_picker);
        tv_marker.setTextColor(Color.WHITE);

        // 도착지 마커옵션 (찍을 위치좌표, 들어갈 텍스트 설정, 커스텀 추가)
        e_markerOptions = new MarkerOptions();
        e_markerOptions.title(place);
        e_markerOptions.position(e_position);
        e_markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker_root_view)));
        eMarker = gMap.addMarker(e_markerOptions);
    }

    // 마커 회전
    public void rotateMarker(final Marker marker, final float toRotation, final float st) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = st;
        final long duration = 1555;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                float rot = t * toRotation + (1 - t) * startRotation;

                marker.setRotation(-rot > 180 ? rot / 2 : rot);
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    // 마커이동
    public void animateMarker(final LatLng startPosition, final LatLng toPosition, final Marker marker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
//        Projection proj = gMap.getProjection();
//        Point startPoint = proj.toScreenLocation(m.getPosition());
//        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 600;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startPosition.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startPosition.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
//                    marker.setVisible(false);

                    marker.setVisible(true);
                }
            }
        });
    }

    /**
     * 레트로핏 3개 삽질
     **/


    private void sendMatch(String msg) {
//        int mode = 2;
//        String senderId = loginUserId;
//        String senderNick = loginUserNick;
//
//        // 현재 시간 받아오기
//        long mNow;
//        Date mDate;
//        mNow = System.currentTimeMillis();
//        mDate = new Date(mNow);
//
//        String time = mFormat.format(mDate);
//
//        messageContent = null;
//        messageContent = new ChattingMessageContent(mode, senderId, senderNick, message, time);
//
//        messageData.add(messageContent);
//        chattingRoomAdapter.notifyDataSetChanged();

//        // 메세지 보내주기
//        send = new SendThread(socket, message);
//        send.start();

        // 매치 됬다 알려주기
//        send = new SendThread(socket, "매칭수락~!");
//        send.start();
        ms.myServiceFunc(D_REQUEST_NUM + "@" + USER_NAME + "@" + TARGET_ID + "@" + msg+ "@" + USER_PROFILE + "@" + myLat + "@" + myLon);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unbindService(conn);
//        Thread SocketClose = new Thread() {
//            public void run() {
//                try {
//                    socket.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//        SocketClose.start();
    }
}
