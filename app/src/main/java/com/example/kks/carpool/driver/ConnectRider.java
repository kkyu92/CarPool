package com.example.kks.carpool.driver;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kks.carpool.Chatting;
import com.example.kks.carpool.MainActivity;
import com.example.kks.carpool.PopUpRequestInfo;
import com.example.kks.carpool.R;
import com.example.kks.carpool.Rating;
import com.example.kks.carpool.WaitingDriver;
import com.example.kks.carpool.appRTC.ConnectActivity;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.kks.carpool.AutoCompleteParse.TMAP_API_KEY;
import static com.example.kks.carpool.Chatting.inchat;
import static com.example.kks.carpool.Result.USER_NAME;
import static com.example.kks.carpool.Result.USER_PROFILE;
import static com.example.kks.carpool.driver.RequestClick.D_REQUEST_NUM;
import static com.example.kks.carpool.google_map.DEFAULT_ZOOM;

public class ConnectRider extends AppCompatActivity implements OnMapReadyCallback {

    private String TAG = "ConnectRider";

    // 레이아웃
    private LinearLayout gMapContainer;
    private TextView textMessage, start_txt, fare_txt, driver_name, driver_email;
    private CircleImageView driver_img;
    private Button requestInfo, cancel_btn, chat_btn, call_btn, cancel_map, show_map, call112, show_route, care_message, move_location, move_location_cancel;
    private GoogleMap gMap;
    private FusedLocationProviderClient mFusedLocationClient;

    // getIntent
    private String start;
    private String end;
    private String date;
    private String time;
    private String atime;
    private String sTime;
    private String dis;
    private String fare;
    private int people;
    private String TARGET_ID;
    private String TARGET_PROFILE;
    private int REQUEST_NUM;
    private double sLat, sLon, eLat, eLon, myLat, myLon, target_lat, target_lon;

    // 커스텀 마커
    private Marker sMarker, eMarker, dMarker, rMarker;
    private MarkerOptions e_markerOptions, s_markerOptions, driver_options, rider_options;
    private View marker_root_view, driver_marker_view, rider_marker_view;
    private TextView tv_marker, driver_marker, rider_marker;
    private ArrayList<LatLng> zoomPoint;
    private float S_bearing = 1;
    private Location preLoc;
    private float rotate = 0;
    private Handler mHandler;
    private long driver_time;
    private int timeFlag10 = 100;
    private int timeFlag5 = 100;
    private int timeFlag3 = 100;
    private int timeFlag1 = 100;
    private int driverToRider = 0;
    private int driverToEndPoint = 0;
    private int rideStart = 0;
    private String autoMove = "";

    // 안심메시지


    //
    private int cancel_reason;

    // 받은 메시지
    private String[] msgFilter;

    // 서비스 부분
    Intent serviceIntent;
    int a = 1;
    RealService ms; // 서비스 객체
    boolean isService = false; // 서비스 중인 확인용

    // 서비스에서 받아옴 -- 노티 띄우기, 채팅아이템 추가하기
    public RealService.ICallback mCallback = new RealService.ICallback() {
        @Override
        public void RiderRemoteCall(String msg) {
            Log.e("Activity", "서비스에서 엑티비티로 받아옴:::" + msg);
        }

        @Override
        public void DriverRemoteCall(String toDriver) {
            Log.e("서비스에서 받아옴", ":::" + toDriver);

            // 방번호, 보낸사람, 받는 사람, 메세지
            // 받을때는 보낸사람, 보낸내용, 보낸시간
            String[] filt1 = toDriver.split("@");
//                    TARGET_PROFILE = filt1[3];

            Toast.makeText(ConnectRider.this, "메세지 : " + toDriver, Toast.LENGTH_SHORT).show();
            Log.d("받은 메세지 ", toDriver);

            msgFilter = toDriver.split("@");
        }

        @Override
        public void VideoCall(String toDriver) { // 영통 받는 부분 방번호를 받았다

        }

        @Override
        public void LocationCall(float bearing, double lat, double lon) { // 위치변화감지
            if (bearing == 999 && lat == 999 && lon == 999) { // 탑승유무 확인완료
                Toast.makeText(ConnectRider.this, "탑승 확인을 완료했습니다.", Toast.LENGTH_LONG).show();
                // 레이아웃 변화 주기
                rideStart = 1;
                driver_email.setText("탑승중입니다.");
                // 줌설정 (출발 도착지)
                zoomPoint.clear();
                zoomPoint.add(new LatLng(sLat, sLon));
                zoomPoint.add(new LatLng(eLat, eLon));
                zoomRoute(zoomPoint);
                // invisible 처리
                cancel_map.setVisibility(View.GONE);
                cancel_btn.setVisibility(View.GONE);
                chat_btn.setVisibility(View.GONE);
                call_btn.setVisibility(View.GONE);
                show_map.setVisibility(View.GONE);
                requestInfo.setVisibility(View.GONE);
                // visible
                move_location.setVisibility(View.VISIBLE);
                call112.setVisibility(View.VISIBLE);
                show_route.setVisibility(View.VISIBLE);
                care_message.setVisibility(View.VISIBLE);
                gMapContainer.setVisibility(View.VISIBLE);

                // 안심메시지 보내기 창 띄워주기
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(ConnectRider.this);
                mBuilder.setTitle("안심메시지 보내기");
                mBuilder.setIcon(R.drawable.car_map);
                mBuilder.setMessage("가족이나 친구에게 안심메시지를 보내세요");
                mBuilder.setPositiveButton("문자", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        String message = USER_NAME + "님이 카풀 운행을 시작했습니다.\n" + "출발지 : " + start + "\n"
                                + "도착지 : " + end + "\n\n" + "출발정보 : " + atime + "\n" + "예상소요시간 : 약" + time + "분";
                        Intent smsMsgAppVar = new Intent(Intent.ACTION_VIEW);
                        smsMsgAppVar.setData(Uri.parse("sms:" + ""));
                        smsMsgAppVar.putExtra("sms_body", message);
                        startActivity(smsMsgAppVar);
                    }
                });
                mBuilder.setNegativeButton("카카오톡", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        String message = USER_NAME + "님이 카풀 운행을 시작했습니다.\n" + "출발지 : " + start + "\n"
                                + "도착지 : " + end + "\n\n" + "출발정보 : " + atime + "\n" + "예상소요시간 : 약" + time + "분";
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, message);
                        intent.setPackage("com.kakao.talk");
                        startActivity(intent);
                    }
                });
                AlertDialog mDialog = mBuilder.create();
                mDialog.show();

            } else if (bearing == 888 && lat == 888 && lon == 888) { // 결제진행 완료
                finish();
                Log.e("결제진행 완료", "평가페이지로 이동하기");
                Toast.makeText(ConnectRider.this, "평가페이지로 이동하기", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(ConnectRider.this, Rating.class);
                intent.putExtra("target", TARGET_ID);
                intent.putExtra("start_place", start);
                intent.putExtra("end_place", end);
                intent.putExtra("date_time", atime);
                intent.putExtra("distance_time", "소요시간 : 약 "+time+"분");
                String money = fare.replace("약 ", "");
                intent.putExtra("fare", money);
                intent.putExtra("flag", "운전자");
                startActivity(intent);
            } else { // 상대방의 위치변화 감지
                Log.e(TAG, "LocationCall : 상대방의 위치 변화 감지");
                LatLng target = new LatLng(target_lat, target_lon);
                animateMarker(target, new LatLng(lat, lon), rMarker);
                rotateMarker(rMarker, bearing, S_bearing);
                Log.e("End_bearing : ", "" + bearing);
                Log.e("Start_bearing : ", "" + S_bearing);
                target_lat = lat;
                target_lon = lon;
                S_bearing = bearing;
                if (driver_email.getText().toString().equals("탑승중입니다.")) { // 탑승후 상태창 변화 및 알림
                    getJsonDataToEndPoint(new LatLng(myLat, myLon), new LatLng(eLat, eLon));
                } else { // 탑승전 까지의 상태창 변화 및 알림
                    getJsonData(new LatLng(target_lat, target_lon), new LatLng(sLat, sLon));
                }
                if (rideStart == 1) { // 탑승확인 후 출발하는 동안 이동할때 좌표 튀는 것 방지
//                    animateMarker(new LatLng(target_lat, target_lon), new LatLng(myLat, myLon), rMarker);
                }
            }
        }
    };

    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 서비스와 연결되었을 때 호출되는 메서드
            // 서비스 객체를 전역변수로 저장
            RealService.MyBinder mb = (RealService.MyBinder) service;
            ms = mb.getService(); // 서비스가 제공하는 메소드 호출하여
            ms.registerCallback(mCallback);
            // 서비스쪽 객체를 전달받을수 있슴
            isService = true;
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.e("바인드 서비스", "::::: 서비스 연결 끊김");
            // 서비스와 연결이 끊겼을 때 호출되는 메서드
            isService = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_rider);

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager
                .findFragmentById(R.id.connect_map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        gMapContainer = findViewById(R.id.connect_map_container);

        textMessage = findViewById(R.id.text_message_ConnectRider);
        start_txt = findViewById(R.id.start_place_ConnectRider);
        fare_txt = findViewById(R.id.fare);
        driver_name = findViewById(R.id.rider_name);
        driver_email = findViewById(R.id.rider_email);
        driver_img = findViewById(R.id.circleImageView);
        requestInfo = findViewById(R.id.request_info);
        cancel_btn = findViewById(R.id.cancel_btn);
        chat_btn = findViewById(R.id.chat_btn);
        call_btn = findViewById(R.id.call_btn);
        cancel_map = findViewById(R.id.cancel_map);
        show_map = findViewById(R.id.show_map);
        move_location = findViewById(R.id.my_location);
        move_location_cancel = findViewById(R.id.my_location_cancel);

        call112 = findViewById(R.id.cancel_btn2);
        show_route = findViewById(R.id.chat_btn2);
        care_message = findViewById(R.id.call_btn2);

        // getIntent
        Intent get = getIntent();
        start = get.getStringExtra("start");
        end = get.getStringExtra("end");
        date = get.getStringExtra("date");
        time = get.getStringExtra("time");
        atime = get.getStringExtra("Atime");
        sTime = get.getStringExtra("sTime");
        Log.e("확인해라", ""+sTime);
        dis = get.getStringExtra("dis");
        fare = get.getStringExtra("fare");
        TARGET_ID = get.getStringExtra("rider");
        TARGET_PROFILE = get.getStringExtra("rider_profile");
        people = get.getIntExtra("people", 1);
        REQUEST_NUM = get.getIntExtra("roomNum", 1);

        sLat = get.getDoubleExtra("sLat", 0);
        sLon = get.getDoubleExtra("sLon", 0);
        eLat = get.getDoubleExtra("eLat", 0);
        eLon = get.getDoubleExtra("eLon", 0);

        target_lat = Double.parseDouble((get.getStringExtra("target_lat")));
        target_lon = Double.parseDouble((get.getStringExtra("target_lon")));

        if (TARGET_PROFILE == null) {
            USER_NAME = get.getStringExtra("user_name");
            SharedPreferences sharedPreferences = getSharedPreferences("matching" + USER_NAME, Context.MODE_PRIVATE);
            start = sharedPreferences.getString("start", null);
            end = sharedPreferences.getString("end", null);
            date = sharedPreferences.getString("date", null);
            time = sharedPreferences.getString("time", null);
            atime = sharedPreferences.getString("Atime", null);
            sTime = sharedPreferences.getString("sTime", null);
            dis = sharedPreferences.getString("dis", null);
            fare = sharedPreferences.getString("fare", null);
            TARGET_ID = sharedPreferences.getString("TARGET_ID", null);
            TARGET_PROFILE = sharedPreferences.getString("TARGET_PROFILE", null);
            people = sharedPreferences.getInt("people", 1);
            REQUEST_NUM = sharedPreferences.getInt("REQUEST_NUM", 1);
            sLat = sharedPreferences.getInt("sLat", 0);
            sLon = sharedPreferences.getInt("sLon", 0);
            eLat = sharedPreferences.getInt("eLat", 0);
            eLon = sharedPreferences.getInt("eLon", 0);
            target_lat = sharedPreferences.getInt("target_lat", 0);
            target_lon = sharedPreferences.getInt("target_lon", 0);
        }

        textMessage.setText(date + "까지 \n 출발지로 이동하세요");
        start_txt.setText("출발 | " + start);
        fare_txt.setText(fare);
        driver_name.setText(TARGET_ID);

        // 상대방 프로필 표시
        if (TARGET_PROFILE.contains("kakao")) { // 카카오 로그인
            Uri img = Uri.parse(TARGET_PROFILE);
            Glide.with(this).load(img).into(driver_img);
        } else if (!TARGET_PROFILE.contains(".jpg")) { // 페북 로그인
            Uri img = Uri.parse("http://graph.facebook.com/" + TARGET_PROFILE + "/picture?type=normal");
            Glide.with(this).load(img).into(driver_img);
        } else { // 앱 로그인
            Uri img = Uri.parse("http://54.180.95.149/uploadsMap/" + TARGET_PROFILE);
            Glide.with(this).load(img).into(driver_img);
        }

        if (RealService.serviceIntent == null) {
            serviceIntent = new Intent(ConnectRider.this, RealService.class);
            startService(serviceIntent);
            Log.e("int a = ", "::::" + a);
            if (a == 1) {
                bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
            }
        } else {
            serviceIntent = RealService.serviceIntent;//getInstance().getApplication();
            Toast.makeText(getApplicationContext(), "already", Toast.LENGTH_LONG).show();
            // 재실행 됬을때 바인드 다시해줘야함
            bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
        }
//        mHandler = new Handler();
//        getJsonData(new LatLng(target_lat, target_lon), new LatLng(sLat, sLon));
    }

    protected void onResume() {
        super.onResume();

        // 10분남았을때 맵 보여주기
        // 요청 받았을때 구글맵과 운전자의 정보를 불러와서 띄워준다 + 운전자의 위치를 표시해준다 서로 공유
//        gMapContainer.setVisibility(View.VISIBLE);

        // 매칭 취소버튼
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] list = new String[]{"긴급한 일이 생겼어요", "다른 교통수단을 이용 할래요", "드라이버가 너무 멀리 있어요", "기타"};
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(ConnectRider.this);
                mBuilder.setTitle("취소하는 이유를 선택해 주세요.");
                mBuilder.setIcon(R.drawable.cancel_private);
                mBuilder.setSingleChoiceItems(list, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mBuilder.setMessage(list[i]);
                        if (i == 0) {
                            cancel_reason = 0;
                        } else if (i == 1) {
                            cancel_reason = 1;
                        } else if (i == 2) {
                            cancel_reason = 2;
                        } else {
                            cancel_reason = 3;
                        }
                    }
                });
                mBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Toast.makeText(ConnectRider.this, "카풀 매칭을 취소했습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                mBuilder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog mDialog = mBuilder.create();
                mDialog.show();
            }
        });

        // 채팅버튼
        chat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConnectRider.this, Chatting.class);
                intent.putExtra("name", TARGET_ID);
                intent.putExtra("profile", TARGET_PROFILE);
                intent.putExtra("roomNum", REQUEST_NUM);
                startActivityForResult(intent, 1933);
            }
        });

        // 영상통화 버튼
        call_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConnectRider.this, ConnectActivity.class);
                intent.putExtra("req_num", String.valueOf(REQUEST_NUM));
                intent.putExtra("tar_id", TARGET_ID);
                intent.putExtra("rtc_room", "");
                startActivity(intent);
            }
        });
        // 요청정보 확인
        requestInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ConnectRider.this, "요청정보 상세보기", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ConnectRider.this, PopUpRequestInfo.class);
                intent.putExtra("start", start);
                intent.putExtra("end", end);
                intent.putExtra("date", date);
                intent.putExtra("time", time);
                intent.putExtra("dis", dis);
                intent.putExtra("fare", fare);
                intent.putExtra("people", people);
                startActivity(intent);
            }
        });

        // 지도 열기
        show_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gMapContainer.setVisibility(View.VISIBLE);
                cancel_map.setVisibility(View.VISIBLE);
                move_location.setVisibility(View.VISIBLE);

                show_map.setVisibility(View.INVISIBLE);
                requestInfo.setVisibility(View.INVISIBLE);
                move_location_cancel.setVisibility(View.INVISIBLE);
            }
        });

        // 지도 닫기
        cancel_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gMapContainer.setVisibility(View.INVISIBLE);
                cancel_map.setVisibility(View.INVISIBLE);
                move_location.setVisibility(View.INVISIBLE);

                show_map.setVisibility(View.VISIBLE);
                requestInfo.setVisibility(View.VISIBLE);
            }
        });

        // 내 위치 포커스 자동이동
        move_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                move_location.setVisibility(View.INVISIBLE);
                move_location_cancel.setVisibility(View.VISIBLE);
//                getDeviceLocation();
                autoMove = "자동이동";
            }
        });

        // 내 위치 포커스 자동이동 취소
        move_location_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                move_location.setVisibility(View.VISIBLE);
                move_location_cancel.setVisibility(View.INVISIBLE);
                autoMove = "";
            }
        });

        // get manager instance
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // set listener
        GPSListener gpsListener = new GPSListener();
        long minTime = 2000;
        float minDistance = 5;

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
        manager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime,
                minDistance,
                gpsListener);

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 222) { // 상대방의 소요시간 변화
                    if (driver_time <= 1) {
                        if (!driver_email.getText().toString().equals("탑승중입니다.") || !driver_email.getText().toString().equals("탑승을 확인중입니다.") || !driver_email.getText().toString().equals("곧 도착합니다.") || !driver_email.getText().toString().equals("목적지에 곧 도착합니다.") || !driver_email.getText().toString().equals("요금결제를 진행중입니다.")) {
                            if (timeFlag1 == 100) {
                                driver_email.setText("곧 도착합니다.");
                            }
                        }
                    } else {
                        if (!driver_email.getText().toString().equals("탑승중입니다.") || !driver_email.getText().toString().equals("탑승을 확인중입니다.") || !driver_email.getText().toString().equals("곧 도착합니다.") || !driver_email.getText().toString().equals("목적지에 곧 도착합니다.") || !driver_email.getText().toString().equals("요금결제를 진행중입니다.")) {
                            if (timeFlag1 == 100) {
                                driver_email.setText("출발지까지 예상 소요시간은 \n (도보) " + String.valueOf(driver_time) + "분 입니다.");
                            }
                        }
                    }

                    if (driver_time == 10 && timeFlag10 == 100) {
                        ms.serviceTimeAlarm(String.valueOf(driver_time));
                        timeFlag10++;
                    } else if (driver_time == 5 && timeFlag5 == 100) {
                        ms.serviceTimeAlarm(String.valueOf(driver_time));
                        timeFlag5++;
                    } else if (driver_time == 3 && timeFlag3 == 100) {
                        ms.serviceTimeAlarm(String.valueOf(driver_time));
                        timeFlag3++;
                    } else if (driver_time <= 1 && timeFlag1 == 100) {
                        ms.serviceTimeAlarm(String.valueOf(driver_time));
                        timeFlag1++;
                    }
                } else if (msg.what == 223) { // 서로의 거리가 100m 이내
                    final AlertDialog.Builder mBuilder = new AlertDialog.Builder(ConnectRider.this);
                    mBuilder.setTitle("탑승자의 탑승유무 확인");
                    mBuilder.setIcon(R.drawable.car_map);
                    mBuilder.setMessage("탑승자가 탑승하게 되면 확인 버튼을 눌러주세요!");
                    mBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            Toast.makeText(ConnectRider.this, "탑승을 확인중입니다..", Toast.LENGTH_LONG).show();
                            driver_email.setText("탑승을 확인중입니다.");
                            // 탑승자에게 알림보내기
                            ms.myServiceFunc(D_REQUEST_NUM + "@" + USER_NAME + "@" + TARGET_ID + "@xkqtmdgoTsmswlghkrdlsdban~!`");
                        }
                    });
                    AlertDialog mDialog = mBuilder.create();
                    mDialog.show();
                } else if (msg.what == 224) { // 200m 도착지까지 남은거리 (알림 + 상태창 변화)
                    driver_email.setText("목적지에 곧 도착합니다.");
                    ms.serviceTimeAlarm("목적지에 곧 도착합니다.");
                } else if (msg.what == 225) { // 100m 도착지까지 남은거리 (상태창 변화) 결제진행중
                    driver_email.setText("요금결제를 진행중입니다.");
                }
            }
        };

        // 신고하기
        call112.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 112 전화 걸기
                Intent smsMsgAppVar = new Intent(Intent.ACTION_CALL);
                smsMsgAppVar.setData(Uri.parse("tel:112"));
                if (ActivityCompat.checkSelfPermission(ConnectRider.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(smsMsgAppVar);
            }
        });

        // 운행경로확인
        show_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConnectRider.this, PopUpRequestInfo.class);
                intent.putExtra("start", start);
                intent.putExtra("end", end);
                intent.putExtra("date", date);
                intent.putExtra("time", time);
                intent.putExtra("dis", dis);
                intent.putExtra("fare", fare);
                intent.putExtra("people", people);
                startActivity(intent);
            }
        });

        // 안심메시지 보내기
        care_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 카톡 // 문자
                // 안심메시지 보내기 창 띄워주기
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(ConnectRider.this);
                mBuilder.setTitle("안심메시지 보내기");
                mBuilder.setIcon(R.drawable.car_map);
                mBuilder.setMessage("가족이나 친구에게 안심메시지를 보내세요");
                mBuilder.setPositiveButton("문자", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        String message = USER_NAME + "님이 카풀 운행을 시작했습니다.\n" + "출발지 : " + start + "\n"
                                + "도착지 : " + end + "\n\n" + "출발정보 : " + atime + "\n" + "예상소요시간 : 약" + time + "분";
                        Intent smsMsgAppVar = new Intent(Intent.ACTION_VIEW);
                        smsMsgAppVar.setData(Uri.parse("sms:" + ""));
                        smsMsgAppVar.putExtra("sms_body", message);
                        startActivity(smsMsgAppVar);
                    }
                });
                mBuilder.setNegativeButton("카카오톡", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        String message = USER_NAME + "님이 카풀 운행을 시작했습니다.\n" + "출발지 : " + start + "\n"
                                + "도착지 : " + end + "\n\n" + "출발정보 : " + atime + "\n" + "예상소요시간 : 약" + time + "분";
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, message);
                        intent.setPackage("com.kakao.talk");
                        startActivity(intent);
                    }
                });
                AlertDialog mDialog = mBuilder.create();
                mDialog.show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 매칭된 상대 및 요청경로 저장
        SharedPreferences sharedPreferences = getSharedPreferences("matching" + USER_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("start", start);
        editor.putString("end", end);
        editor.putString("date", date);
        editor.putString("time", time);
        editor.putString("Atime", atime);
        editor.putString("sTime", sTime);
        editor.putString("dis", dis);
        editor.putString("fare", fare);
        editor.putString("TARGET_ID", TARGET_ID);
        editor.putString("TARGET_PROFILE", TARGET_PROFILE);
        editor.putInt("people", people);
        editor.putInt("REQUEST_NUM", REQUEST_NUM);
        editor.putInt("sLat", (int) sLat);
        editor.putInt("sLon", (int) sLon);
        editor.putInt("eLat", (int) eLat);
        editor.putInt("eLon", (int) eLon);
        editor.putInt("target_lat", (int) target_lat);
        editor.putInt("target_lon", (int) target_lon);
        editor.apply();

//        unbindService(conn);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        Log.e("onMapReady:::::", "onMapReady");
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
        gMap.setMyLocationEnabled(false);
        gMap.getUiSettings().setMyLocationButtonEnabled(false);
        setCustomMarkerView();
        setDriverMarkerView();
        setRiderMarkerView();

        setStartMarker(sLat, sLon, start);
        setEndMarker(eLat, eLon, end);
        setDriverMarker(myLat, myLon);
        Log.e(TAG, "운전자의 좌표 : " + myLat + " / " + myLon);
        setRiderMarker(76.889322, -67.884277);

//        LatLng myLoction = new LatLng(myLat,myLon);
        LatLng targetLocation = new LatLng(target_lat, target_lon);
        LatLng startLocation = new LatLng(sLat, sLon);
        zoomPoint = new ArrayList<>();
//        zoomPoint.add(myLoction);
        zoomPoint.add(targetLocation);
        zoomPoint.add(startLocation);

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
                        zoomRoute(zoomPoint);
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
        hideSoftKeyboard();
    }

    // 키보드 숨기기
    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    // 장소 설정 후 출발, 도착 마커 커스텀
    private void setCustomMarkerView() {
        marker_root_view = LayoutInflater.from(this).inflate(R.layout.custom_marker, null);
        tv_marker = (TextView) marker_root_view.findViewById(R.id.tv_marker);
    }

    // 운전자 마커 커스텀
    private void setDriverMarkerView() {
        driver_marker_view = LayoutInflater.from(this).inflate(R.layout.driver_marker, null);
        driver_marker = (TextView) driver_marker_view.findViewById(R.id.car_marker);
    }

    // 탑승자 마커 커스텀
    private void setRiderMarkerView() {
        rider_marker_view = LayoutInflater.from(this).inflate(R.layout.rider_marker, null);
        rider_marker = (TextView) rider_marker_view.findViewById(R.id.rider_marker);
    }


    /**
     * 탑승자와 운전자의 연결
     **/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            Log.e("운전자의 연결화면:::", "setResult( RESULT_OK ) 리절트오케");
            Log.e(TAG, "onActivityResult");
            inchat = false;
//            Toast.makeText(getContext(), "등록을 취소 하였습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (requestCode == 1933) {
            Log.e("setResult( 1933 ):::", "(운전자) 채팅에서 톨아옴");

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
        tv_marker.setText(place);
        tv_marker.setBackgroundResource(R.drawable.start_picker);
        tv_marker.setTextColor(Color.BLACK);

        // 출발지 마커옵션 (찍을 위치좌표, 들어갈 텍스트 설정, 커스텀 추가)
        s_markerOptions = new MarkerOptions();
        s_markerOptions.title("출발");
        s_markerOptions.position(s_position);
        s_markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker_root_view)));
        sMarker = gMap.addMarker(s_markerOptions);
    }

    // 도착 마커
    private void setEndMarker(double lat, double lon, String place) {
        // 도착지 마커
        LatLng e_position = new LatLng(lat, lon);
        tv_marker.setText(place);
        tv_marker.setBackgroundResource(R.drawable.end_picker);
        tv_marker.setTextColor(Color.WHITE);

        // 도착지 마커옵션 (찍을 위치좌표, 들어갈 텍스트 설정, 커스텀 추가)
        e_markerOptions = new MarkerOptions();
        e_markerOptions.title("도착");
        e_markerOptions.position(e_position);
        e_markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker_root_view)));
        eMarker = gMap.addMarker(e_markerOptions);
    }

    // 운전자 마커
    private void setDriverMarker(double lat, double lon) {
        LatLng driver = new LatLng(lat, lon);
        driver_marker.setBackgroundResource(R.drawable.car_map);

        driver_options = new MarkerOptions();
        driver_options.position(driver);
        driver_options.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, driver_marker_view)));
//        rider_options.anchor(0.5f, 0.5f);
//        rider_options.flat(true);
        dMarker = gMap.addMarker(driver_options.anchor(0.5f, 0.5f).flat(true));
    }

    // 탑승자 마커
    private void setRiderMarker(double lat, double lon) {
        LatLng rider = new LatLng(lat, lon);
        rider_marker.setBackgroundResource(R.drawable.radio_red);

        rider_options = new MarkerOptions();
        rider_options.position(rider);
        rider_options.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, rider_marker_view)));
//        rider_options.anchor(0.5f, 0.5f);
//        rider_options.flat(true);
        rMarker = gMap.addMarker(rider_options.anchor(0.5f, 0.5f).flat(true));
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
//                S_bearing = -rot > 180 ? rot / 2 : rot;
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

    // 소요시간 알아낸다
    public void getJsonData(final LatLng startPoint, final LatLng endPoint) {
        Thread thread = new Thread() {
            @Override
            public void run() {

                try {
                    URL acUrl = new URL(
                            "https://api2.sktelecom.com/tmap/routes/pedestrian?version=1&callback=application/json" +
                                    "&appKey=" + TMAP_API_KEY + "&startName=출발지&endName=도착지" +
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
//                        mapPoints.clear();
                    }
                    reader.close();

                    JSONObject jAr = null;
                    try {
                        jAr = new JSONObject(line);
                        JSONArray features = jAr.getJSONArray("features");
//                        mapPoints = new ArrayList<>();


                        for (int i = 0; i < features.length(); i++) {
                            JSONObject featuresJSONObject = features.getJSONObject(i);

                            if (i == 0) {
                                JSONObject properties = featuresJSONObject.getJSONObject("properties");

//                                String taxiFare = properties.getString("taxiFare");
//                                String totalFare = properties.getString("totalFare");
                                String totalDistance = properties.getString("totalDistance");
//                                String totalTime = properties.getString("totalTime");
                                String totalTime = properties.getString("totalTime");

//                                Log.d("getTotal 택시:::::", "" + taxiFare);
//                                Log.d("getTotal 요금:::::", "" + totalFare);
//                                Log.d("getTotal 거리:::::", "" + totalDistance);
                                Log.d("getTotal 시간:::::", "" + totalTime);

                                double t_time = Integer.valueOf(totalTime) / 60;
                                long time = Math.round(t_time);
//                                String meter = totalDistance.substring(totalDistance.length() - 3, totalDistance.length());
//                                String km = totalDistance.substring(0, totalDistance.length() - 3);
//                                double distance = Double.parseDouble(km + "." + meter);
//                                double dis = Math.round(distance * 100d) / 100d;

                                // 도착지와 가까워 졌다 100m 이내이다.
                                if (Integer.valueOf(totalDistance) <= 100) {
                                    Log.e("도착지와의 거리가 100m 이내", "상대방과의 거리를 비교 시작 : " + totalDistance);
                                    // 나와의 거리비교를 시작한다
                                    getJsonDataToTarget(new LatLng(myLat, myLon), new LatLng(target_lat, target_lon));

                                }
//                                if (taxiFare.length() > 2) {
//                                    String beakWon = taxiFare.substring(taxiFare.length() - 3, taxiFare.length());
//                                    String man = taxiFare.substring(0, taxiFare.length() - 3);
//                                    won = man + "," + beakWon;
//                                    total_taxi_fare.setText("이용요금 : " + won + "원");
//                                } else {
//                                    total_taxi_fare.setText("이용요금 : " + taxiFare + "원");
//                                }
                                driver_time = time;
                                Thread time_thread = new Thread() {
                                    public void run() {
                                        Message msg = mHandler.obtainMessage();
                                        msg.what = 222;
                                        mHandler.sendMessage(msg);
                                    }
                                };
                                time_thread.start();

//                                if (driver_time == 0) {
//                                    time_thread.interrupt();
//                                    Log.i(TAG, "상대방의 소요시간 정보 쓰레드 중지");
//                                }
//                                driver_email.setText("출발지까지 예상 소요시간은 \n"+ String.valueOf(time) +"분 입니다.");
//                                total_distance.setText("이동거리 : " + dis + "km");
//                                total_time.setText("소요시간 : 약 " + String.valueOf(time) + "분");
//                                total_fare.setText("통행요금 : " + totalFare + "원");
                            }

//                            JSONObject geometry = featuresJSONObject.getJSONObject("geometry");
//                            JSONArray coordinates = geometry.getJSONArray("coordinates");


//                            String geoType = geometry.getString("type");
                            // 꺽이는?? 특정 포인트 좌표
//                            if (geoType.equals("Point")) {
//                                double lonJson = coordinates.getDouble(0);
//                                double latJson = coordinates.getDouble(1);
//
//                                Log.d(TAG, "-");
//                                Log.d(TAG, lonJson + "," + latJson + "\n");
//                                com.google.android.gms.maps.model.LatLng point = new com.google.android.gms.maps.model.LatLng(latJson, lonJson);
//                                mapPoints.add(point);
//
//                            }
                            // 포인트 사이사이의 좌표
//                            if (geoType.equals("LineString")) {
//                                for (int j = 0; j < coordinates.length(); j++) {
//                                    JSONArray JLinePoint = coordinates.getJSONArray(j);
//                                    double lonJson = JLinePoint.getDouble(0);
//                                    double latJson = JLinePoint.getDouble(1);
//
//                                    Log.d(TAG, "-");
//                                    Log.d(TAG, lonJson + "," + latJson + "\n");
//                                    com.google.android.gms.maps.model.LatLng point = new com.google.android.gms.maps.model.LatLng(latJson, lonJson);
//
//                                    mapPoints.add(point);
//                                }
//                            }
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
    }

    // 상대방과 나의 거리비교
    public void getJsonDataToTarget(final LatLng startPoint, final LatLng endPoint) {
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
//                        mapPoints.clear();
                    }
                    reader.close();

                    JSONObject jAr = null;
                    try {
                        jAr = new JSONObject(line);
                        JSONArray features = jAr.getJSONArray("features");
//                        mapPoints = new ArrayList<>();


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

//                                double t_time = Integer.valueOf(totalTime) / 60;
//                                long time = Math.round(t_time);
//                                if (totalDistance.length() >= 4) {
//                                    meter = totalDistance.substring(totalDistance.length() - 3, totalDistance.length());
//                                    String km = totalDistance.substring(0, totalDistance.length() - 3);
//                                    double distance = Double.parseDouble(km + "." + meter);
//                                    double dis = Math.round(distance * 100d) / 100d;
//                                } else {
//                                    meter = totalDistance;
//                                    String km = "0";
//                                    double distance = Double.parseDouble(km + "." + meter);
//                                    double dis = Math.round(distance * 100d) / 100d;
//                                }
                                // 서로 100m 이내이다.
                                if (Integer.valueOf(totalDistance) <= 100 && driverToRider == 0) {
                                    Log.e("운전자 화면", "서로의 거리가100m 이다");
                                    driverToRider++;
                                    // 다이얼로그로 확인 띄우기 확인 눌렀을 경우 화면 구성 변화 시키고 + 탑승자 화면에 안심메시지 보내는 창 띄워주기
                                    Thread time_thread = new Thread() {
                                        public void run() {
                                            Message msg = mHandler.obtainMessage();
                                            msg.what = 223;
                                            mHandler.sendMessage(msg);
                                        }
                                    };
                                    time_thread.start();
                                }
                            }

                            JSONObject geometry = featuresJSONObject.getJSONObject("geometry");
                            JSONArray coordinates = geometry.getJSONArray("coordinates");


                            String geoType = geometry.getString("type");

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
    }

    // 도착지와 나의 거리비교
    public void getJsonDataToEndPoint(final LatLng startPoint, final LatLng endPoint) {
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
//                        mapPoints.clear();
                    }
                    reader.close();

                    JSONObject jAr = null;
                    try {
                        jAr = new JSONObject(line);
                        JSONArray features = jAr.getJSONArray("features");
//                        mapPoints = new ArrayList<>();


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

//                                double t_time = Integer.valueOf(totalTime) / 60;
//                                long time = Math.round(t_time);
//                                if (totalDistance.length() >= 4) {
//                                    meter = totalDistance.substring(totalDistance.length() - 3, totalDistance.length());
//                                    String km = totalDistance.substring(0, totalDistance.length() - 3);
//                                    double distance = Double.parseDouble(km + "." + meter);
//                                    double dis = Math.round(distance * 100d) / 100d;
//                                } else {
//                                    meter = totalDistance;
//                                    String km = "0";
//                                    double distance = Double.parseDouble(km + "." + meter);
//                                    double dis = Math.round(distance * 100d) / 100d;
//                                }
                                // 서로 100m 이내이다.
                                if (Integer.valueOf(totalDistance) <= 200 && driverToEndPoint == 0) {
                                    Log.e("운전자 화면", "도착지 까지의 거리가 200m 이다");
                                    driverToEndPoint++;
                                    // 곧 도착한다는 알림과 상태창 변화 시키기
                                    Thread time_thread = new Thread() {
                                        public void run() {
                                            Message msg = mHandler.obtainMessage();
                                            msg.what = 224;
                                            mHandler.sendMessage(msg);
                                        }
                                    };
                                    time_thread.start();
                                } else if (Integer.valueOf(totalDistance) <= 100 && driverToEndPoint == 1) {
                                    driverToEndPoint++;
                                    Log.e("운전자 화면", "도착지까지의 거리가 100m 이다");
                                    // 운전자 화면 결제 진행중으로 상태창 변화
                                    Thread time_thread = new Thread() {
                                        public void run() {
                                            Message msg = mHandler.obtainMessage();
                                            msg.what = 225;
                                            mHandler.sendMessage(msg);
                                        }
                                    };
                                    time_thread.start();
                                }
                            }

                            JSONObject geometry = featuresJSONObject.getJSONObject("geometry");
                            JSONArray coordinates = geometry.getJSONArray("coordinates");


                            String geoType = geometry.getString("type");

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
    }

    // 위치변화 감지
    private class GPSListener implements LocationListener {

        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged 나의 위치변화 감지");

            //capture location data sent by current provider
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            String msg = "Latitude : " + latitude + "\nLongitude:" + longitude;
            Log.i("GPSLocationService", msg);

            LatLng myLoction = new LatLng(latitude, longitude);
            zoomPoint.add(myLoction);
            if (a == 1) {
                zoomRoute(zoomPoint);
            }
            a++;

            LatLng nowDriver = new LatLng(myLat, myLon);
            LatLng moveDriver = new LatLng(latitude, longitude);
            animateMarker(nowDriver, moveDriver, dMarker);

            if (preLoc != null) {
                rotate = preLoc.bearingTo(location);
                dMarker.setRotation(rotate);
                Log.e("나의 위치변화감지 (이전) : ", "" + preLoc.getBearing());
                Log.e("나의 위치변화감지 (현재) : ", "" + location.getBearing());
                Log.e("나의 위치변화감지 (결과값) : ", "" + rotate);
            }
//            rotateMarker(dMarker, location.getBearing(), S_bearing);
            myLat = latitude;
            myLon = longitude;
            preLoc = location;
//            S_bearing = location.getBearing();

            // 10분전 적용부분
            countTen(rotate, myLat, myLon);

            if (autoMove.equals("자동이동")) {
                moveCamera(myLoction, DEFAULT_ZOOM, "내 위치");
            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    // 출발 10분전 위치확인
    private void countTen(float bearing, double lat, double lon) {

        Date now = new Date();

        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMdd HH:mm");

            Date today = new Date();
            // 오늘 날짜를 yyyy-MM-dd 형태로 변환하여 반환한다.
            String result = timeFormat.format(today);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(result);
            stringBuffer.append(" ");
            stringBuffer.append(sTime);

            String dateTime = stringBuffer.toString();
            Log.e(TAG, "출발시간 : " + dateTime);
            Date stime = timeFormat.parse(dateTime);

            Calendar c = Calendar.getInstance();
            c.setTime(stime);
            c.add(Calendar.MINUTE, -10);
            stime = c.getTime();

            Log.e(TAG, "현재시간 : "+now.getTime());
            Log.e(TAG, "출발시간 -10 : "+stime.getTime());
            if (now.getTime() >= stime.getTime()) {
                Log.e(TAG, "10분전 적용완료");
                sendMatch(bearing, lat, lon);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void sendMatch(float bearing, double myLat, double myLon) {
        ms.myServiceFunc(REQUEST_NUM + "@" + USER_NAME + "@" + TARGET_ID + "@GPSLocationServiceLatitude : ~!@" + bearing + "@" + myLat + "@" + myLon);
    }
}
