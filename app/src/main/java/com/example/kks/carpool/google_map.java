package com.example.kks.carpool;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.kks.carpool.Pref.ViewAnimation;
import com.example.kks.carpool.autosearch.TMapSearchInfo;
import com.example.kks.carpool.autosearch.TmapRoute;
import com.example.kks.carpool.model.ClusterMarker;
import com.example.kks.carpool.model.PlaceInfo;
import com.example.kks.carpool.model.RequestCar;
import com.example.kks.carpool.retro.ApiInterface;
import com.example.kks.carpool.retro.TmapClient;
import com.example.kks.carpool.service.ExampleService;
import com.example.kks.carpool.service.RealService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.HTTP;

import static com.example.kks.carpool.AutoCompleteParse.TMAP_API_KEY;
import static com.example.kks.carpool.Result.USER_NAME;
import static com.example.kks.carpool.Result.USER_PROFILE;
import static com.example.kks.carpool.service.RealService.socketClient;

public class google_map extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private String TARGET_PROFILE, GET_MSG;

    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;
    private FloatingActionButton more, pay, myPoint;

    public static SimpleDateFormat datetimeFormat = new SimpleDateFormat("MM월 dd일 (E) HH:mm", Locale.KOREAN);
    public static SimpleDateFormat datetimeFormat_Date = new SimpleDateFormat("MM월 dd일 (EEE) HH:mm", Locale.KOREAN);
    public static SimpleDateFormat dbFormat_Date = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN);
    public static SimpleDateFormat dbFormat_Time = new SimpleDateFormat("HH:mm", Locale.KOREAN);
    private static final String TAG = "HTTP Tmap 좌표";

    private GoogleMap gMap;
    private FusedLocationProviderClient mFusedLocationClient;

    public static final float DEFAULT_ZOOM = 15f;

    // Http Tmap 경로 좌표 가져오기
    private ArrayList<LatLng> mCapturedLocations = new ArrayList<LatLng>();        //지나간 좌표 들을 저장하는 List
    private static final int PAGINATION_OVERLAP = 5;
    private static final int PAGE_SIZE_LIMIT = 100;
    private ArrayList<com.google.android.gms.maps.model.LatLng> mapPoints;

    // Retrofit Tmap 거리 요금 시간
    private ApiInterface apiInterface;
    private ConstraintLayout constraintLayout;
    private TextView total_time, total_distance, total_fare, total_taxi_fare;
    private Button time_set, people_count, request_car, cancel;
//    private Spinner people_count;

    // 위치 좌표
    double latitude;
    double longitude;
    public static String lat, lon, address;
    private double myLat, myLon;

    // setResult
    private double lat_start, lon_start, lat_end, lon_end;

    private Button start_btn, end_btn;
    private ImageButton mLocation;

    // Custom Marker
    private MarkerOptions e_markerOptions, s_markerOptions;
    private View marker_root_view;
    private TextView tv_marker;

    // 타임피커
    private TimePickerDialog timePickerDialog;
    private ConstraintLayout mMapContainer;
    private TextView setDay, setDate, setFare;
    private String[] listItems = {"월", "화", "수", "목", "금", "토", "일"};
    private boolean[] checkItems = new boolean[listItems.length];
    private ArrayList<Integer> userItems = new ArrayList<>();

    // Database 카풀요청
    private String time = "";
    private String date_c, date_form;
    private int people_c = 1;
    private String won;

    // 요청 진행
    private ProgressBar progressBar;
    private CountDownTimer countDownTimer;
    private int req_count = 0;
    public static int REQUEST_NUM;
    private String ttime = "";
    // chat
//    public static Socket socket;
    // 5사무실
    public static final String IP = "192.168.0.139";
    // 집
//    public static final String IP = "192.168.200.154";
    // 3사무실
//    public static final String IP = "192.168.0.81";

    private int port = 5000;
    private String TARGET_ID;

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
            GET_MSG = msg;
            Log.e("(탑승자)받았다 뭐냐::::", "요청화면:::" + GET_MSG);
            // 메세지가 왔다면.

            // 방번호, 보낸사람, 받는 사람, 메세지
            // 받을때는 보낸사람, 보낸내용, 보낸시간
            String[] filt1 = GET_MSG.split("@");
            TARGET_PROFILE = filt1[3];
            String target_lat = filt1[4];
            String target_lon = filt1[5];

            Toast.makeText(google_map.this, " 매칭 요청완료!! ", Toast.LENGTH_SHORT).show();
//            Log.e("받은 메세지 ", );

            TARGET_ID = filt1[0];
            Log.e("타겟 아이디 ", TARGET_ID);

            countDownTimer.cancel();
            progressBar.setProgress(0);
            time_set.setEnabled(true);
            people_count.setEnabled(true);
            cancel.setVisibility(View.VISIBLE);
            request_car.setText("카풀 요청");

            // 내정보 보내기
            sendMatch(REQUEST_NUM + "@" + USER_NAME + "@" + TARGET_ID + "@수락확인~!@" + USER_PROFILE + "@" + myLat + "@" + myLon);

            // 요청 리스트 삭제
            performDelRequestCar(REQUEST_NUM);

            Intent intent = new Intent(google_map.this, WaitingDriver.class);
            String start = s_markerOptions.getTitle();
            String end = e_markerOptions.getTitle();
            String dis = total_distance.getText().toString();
            String ttime = total_time.getText().toString();

            intent.putExtra("start", start);
            intent.putExtra("end", end);
            intent.putExtra("date", date_form);
            intent.putExtra("time", ttime);
            intent.putExtra("sTime", time);
            intent.putExtra("dis", dis);
            intent.putExtra("fare", won + " 원");
            intent.putExtra("people", people_c);
            intent.putExtra("driver", TARGET_ID);
            intent.putExtra("driver_profile", TARGET_PROFILE);
            intent.putExtra("roomNum", REQUEST_NUM);

            intent.putExtra("sLat", lat_start);
            intent.putExtra("sLon", lon_start);
            intent.putExtra("eLat", lat_end);
            intent.putExtra("eLon", lon_end);

            intent.putExtra("target_lat", target_lat);
            intent.putExtra("target_lon", target_lon);

            startActivity(intent);
            cancel.callOnClick();
        }

        @Override
        public void DriverRemoteCall(String msg) {

        }

        @Override
        public void VideoCall(String toDriver) {

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
        setContentView(R.layout.activity_google_map);

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        constraintLayout = findViewById(R.id.carpool_start);
        total_distance = findViewById(R.id.total_distance);
        total_time = findViewById(R.id.total_time);
        total_taxi_fare = findViewById(R.id.total_taxi_fare);
        total_fare = findViewById(R.id.total_fare);
        time_set = findViewById(R.id.time_set);
        people_count = findViewById(R.id.people_count);
        request_car = findViewById(R.id.request_carpool);
        cancel = findViewById(R.id.cancel_button);
        setDate = findViewById(R.id.setDate);
        setDay = findViewById(R.id.setDay);
        setFare = findViewById(R.id.setFare);

        start_btn = findViewById(R.id.start_point);
        end_btn = findViewById(R.id.end_point);
        mLocation = findViewById(R.id.my_located);

        mMapContainer = findViewById(R.id.gMapView);

        progressBar = findViewById(R.id.progress_bar);

//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.numbers, R.layout.support_simple_spinner_dropdown_item);
//        adapter.setDropDownViewResource(R.layout.activity_google_map);
//        people_count.setAdapter(adapter);
//        people_count.setOnItemSelectedListener(this);

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);

        more = (FloatingActionButton) findViewById(R.id.fab);
        pay = (FloatingActionButton) findViewById(R.id.fab1);
        myPoint = (FloatingActionButton) findViewById(R.id.fab2);

        more.setOnClickListener(this);
        pay.setOnClickListener(this);
        myPoint.setOnClickListener(this);
    }

    // 플로팅엑션버튼 클릭
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab:
                anim();
//                Toast.makeText(this, "Floating Action Button", Toast.LENGTH_SHORT).show();
                break;
            case R.id.fab1:
                anim();
                Toast.makeText(this, "이용내역", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(google_map.this, RiderCarpoolList.class);
                startActivity(intent);
                break;
            case R.id.fab2:
                anim();
                Toast.makeText(this, "평점", Toast.LENGTH_SHORT).show();
                Intent point = new Intent(google_map.this, RiderMyPoint.class);
                startActivity(point);
                break;
        }
    }

    // 플로팅엑션버튼 애니메이션
    public void anim() {
        if (isFabOpen) {
            pay.startAnimation(fab_close);
            myPoint.startAnimation(fab_close);
            pay.setClickable(false);
            myPoint.setClickable(false);
            isFabOpen = false;
        } else {
            pay.startAnimation(fab_open);
            myPoint.startAnimation(fab_open);
            pay.setClickable(true);
            myPoint.setClickable(true);
            isFabOpen = true;
        }
    }


    @Override
    public void onMapReady(final GoogleMap map) {
        gMap = map;

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
        gMap.setMyLocationEnabled(true);
        gMap.getUiSettings().setMyLocationButtonEnabled(false);
        setCustomMarkerView();

    }

    @Override
    public void onResume() {
        super.onResume();

        if (e_markerOptions != null && s_markerOptions != null) {
            Log.e("그려라", "마커");
//            gMap.addMarker(e_markerOptions);
//            gMap.addMarker(s_markerOptions);
        }

        // 출발지 버튼
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(google_map.this, MainActivity.class);
//                intent.putExtra("Lat", latitude);
//                intent.putExtra("Lon", longitude);
                intent.putExtra("btn", "출발지");
                intent.putExtra("now", address);
                startActivityForResult(intent, 111);
            }
        });

        // 도착지 버튼
        end_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(google_map.this, MainActivity.class);
                intent.putExtra("Lat", latitude);
                intent.putExtra("Lon", longitude);
                intent.putExtra("btn", "도착지");
                intent.putExtra("now", address);
                startActivityForResult(intent, 112);
            }
        });

        // 내 위치로
        mLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation();
            }
        });

        // 카풀 출발 시간 설정 (스피너 사용)
        time_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar currentDate = Calendar.getInstance();
                final Calendar date = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(google_map.this, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, final int dayOfMonth) {

                        date.set(year, monthOfYear, dayOfMonth);
                        timePickerDialog = new TimePickerDialog(google_map.this, AlertDialog.THEME_HOLO_LIGHT,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                        date.set(Calendar.MINUTE, minute);

                                        if (dayOfMonth == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
                                            if (hourOfDay < Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                                                Toast.makeText(google_map.this, "이전 시간은 선택할 수 없습니다.", Toast.LENGTH_SHORT).show();
                                            } else if (hourOfDay == Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                                                if (minute < Calendar.getInstance().get(Calendar.MINUTE)) {
                                                    Toast.makeText(google_map.this, "이전 시간은 선택할 수 없습니다.", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    time_set.setText(datetimeFormat_Date.format(date.getTime()));
                                                    date_c = dbFormat_Date.format(date.getTime());
                                                    time = dbFormat_Time.format(date.getTime());
                                                    date_form = datetimeFormat.format(date.getTime());
                                                }
                                            } else {
                                                time_set.setText(datetimeFormat_Date.format(date.getTime()));
                                                date_c = dbFormat_Date.format(date.getTime());
                                                time = dbFormat_Time.format(date.getTime());
                                                date_form = datetimeFormat.format(date.getTime());
                                            }
                                        } else {
                                            time_set.setText(datetimeFormat_Date.format(date.getTime()));
                                            date_c = dbFormat_Date.format(date.getTime());
                                            time = dbFormat_Time.format(date.getTime());
                                            date_form = datetimeFormat.format(date.getTime());
                                        }

                                        Log.e("출발시점:::", "형식: " + date.getTime());
                                        Log.e("출발시점:::", "형식: " + date);


//                                        // 요일선택 dialog
//                                        AlertDialog.Builder mBuilder = new AlertDialog.Builder(google_map.this);
//                                        mBuilder.setTitle("반복 요일선택");
//                                        mBuilder.setMultiChoiceItems(listItems, checkItems, new DialogInterface.OnMultiChoiceClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
//                                                if (isChecked) {
//                                                    if (!userItems.contains(position)){
//                                                        userItems.add(position);
//                                                    }
//                                                } else if (userItems.contains(position)) {
//                                                    userItems.remove((Integer) position);
//                                                }
//                                            }
//                                        });
//                                        mBuilder.setCancelable(false);
//                                        mBuilder.setPositiveButton("완료", new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialogInterface, int which) {
//                                                String item = "";
//                                                for (int i = 0; i < userItems.size(); i++) {
//                                                    item = item + listItems[userItems.get(i)];
//
//                                                    if (i != userItems.size() - 1) {
//                                                        item = item + ", ";
//                                                    }
//                                                }
//                                                setDay.setText(item);
//                                            }
//                                        });
//
//                                        AlertDialog mDialog = mBuilder.create();
//                                        mDialog.getWindow().setGravity(Gravity.BOTTOM);
//                                        mDialog.show();

                                    }
                                }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false);
                        timePickerDialog.setIcon(R.drawable.time);
                        timePickerDialog.setTitle("출발시간을 설정하세요");
                        timePickerDialog.getWindow().setGravity(Gravity.BOTTOM);
                        timePickerDialog.show();
                    }
                }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));

                Calendar minDate = Calendar.getInstance();
                Calendar maxDate = Calendar.getInstance();

                minDate.set(minDate.get(Calendar.YEAR), minDate.get(Calendar.MONTH), minDate.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(minDate.getTime().getTime());

                maxDate.set(maxDate.get(Calendar.YEAR), maxDate.get(Calendar.MONTH), maxDate.get(Calendar.DAY_OF_MONTH) + 30);
                datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

                Log.e("", "year" + maxDate.get(Calendar.YEAR));
                Log.e("", "month" + maxDate.get(Calendar.MONTH));
                Log.e("", "date" + maxDate.get(Calendar.DAY_OF_MONTH));

                datePickerDialog.show();

//                Calendar calendar = Calendar.getInstance();
//                Calendar minDate = Calendar.getInstance();
//                Calendar maxDate = Calendar.getInstance();
//
//                int year = calendar.get(Calendar.YEAR);
//                int month = calendar.get(Calendar.MONTH);
//                int day = calendar.get(Calendar.DAY_OF_MONTH);
//                String mDateStr = String.valueOf(year)+"-"+String.valueOf(month)+"-"+String.valueOf(day);          // 버튼값 mDateStr 에 넣기
//
//                Date date = new Date();
//
//                try {
//                    date = datetimeFormat_Date.parse(mDateStr);
//                } catch(Exception ex) {
//                    Log.d(TAG, "Exception in parsing date : " + date);
//                }
//                calendar.setTime(date);
//
//                DatePickerDialog datePickerDialog = new DatePickerDialog(
//                        google_map.this,
//                        new DatePickerDialog.OnDateSetListener() {
//                            @Override
//                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//
//                            }
//                        },
//                        calendar.get(Calendar.YEAR),
//                        calendar.get(Calendar.MONTH),
//                        calendar.get(Calendar.DAY_OF_MONTH)
//                );
//
//                minDate.set(year,month-1,day);
//                datePickerDialog.getDatePicker().setMinDate(minDate.getTime().getTime());
//
//                maxDate.set(year,month-1,day+30);
//                datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
//
//                datePickerDialog.getWindow().setGravity(Gravity.BOTTOM);
//                datePickerDialog.show();

                // 타임피커 시간설정 Listener
//                TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
//                    @Override
//                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
//
//                        Calendar now = Calendar.getInstance();
//                        int date = now.get(Calendar.DATE);
//                        int day = now.get(Calendar.DAY_OF_WEEK);
//                        int h = now.get(Calendar.HOUR_OF_DAY);
//                        int m = now.get(Calendar.MINUTE);
//
//                        if (hour < h) {
//                            Toast.makeText(google_map.this, "다음날로 설정하였습니다.", Toast.LENGTH_SHORT).show();
//                            date += 1;
//                            day += 1;
//
//                            String dayday = "요일";
//                            if (day == 1) { dayday = "일"; }
//                            else if (day == 2) { dayday = "월"; }
//                            else if (day == 3) { dayday = "화"; }
//                            else if (day == 4) { dayday = "수"; }
//                            else if (day == 5) { dayday = "목"; }
//                            else if (day == 6) { dayday = "금"; }
//                            else if (day == 7) { dayday = "토"; }
//                            else {dayday = "일";}
//
//                            // 시 --- 한자리 수 일때
//                            String hh = String.valueOf(hour);
//                            StringBuffer hhh = new StringBuffer();
//                            if (hh.length() <2) {
//                                hhh.append("0");
//                                hhh.append(hh);
//                            } else { hhh.append(hh); }
//                            // 분 --- 한자리 수 일때
//                            String min = String.valueOf(minute);
//                            StringBuffer mmm = new StringBuffer();
//                            if (min.length() < 2) {
//                                mmm.append("0");
//                                mmm.append(min);
//                            } else { mmm.append(min); }
//
//                            StringBuffer strBuf = new StringBuffer();
//                            strBuf.append(date);
//                            strBuf.append("일 ");
//                            strBuf.append("(");
//                            strBuf.append(dayday);
//                            strBuf.append(") ");
//                            strBuf.append(hhh);
//                            strBuf.append(":");
//                            strBuf.append(mmm);
//                            strBuf.append(" 출발");
//
//                            time_set.setText(strBuf.toString());
//                            date_c = date;
//                            time = hhh+":"+mmm;
//                        } else {
//                            if (h == hour && minute < m) {
//                                Toast.makeText(google_map.this, "다음날로 설정하였습니다.", Toast.LENGTH_SHORT).show();
//                                date += 1;
//                                day += 1;
//
//                                String dayday = "요일";
//                                if (day == 1) { dayday = "일"; }
//                                else if (day == 2) { dayday = "월"; }
//                                else if (day == 3) { dayday = "화"; }
//                                else if (day == 4) { dayday = "수"; }
//                                else if (day == 5) { dayday = "목"; }
//                                else if (day == 6) { dayday = "금"; }
//                                else if (day == 7) { dayday = "토"; }
//                                else {dayday = "일";}
//
//                                // 시 --- 한자리 수 일때
//                                String hh = String.valueOf(hour);
//                                StringBuffer hhh = new StringBuffer();
//                                if (hh.length() <2) {
//                                    hhh.append("0");
//                                    hhh.append(hh);
//                                } else { hhh.append(hh); }
//                                // 분 --- 한자리 수 일때
//                                String min = String.valueOf(minute);
//                                StringBuffer mmm = new StringBuffer();
//                                if (min.length() < 2) {
//                                    mmm.append("0");
//                                    mmm.append(min);
//                                } else { mmm.append(min); }
//
//                                StringBuffer strBuf = new StringBuffer();
//                                strBuf.append(date);
//                                strBuf.append("일 ");
//                                strBuf.append("(");
//                                strBuf.append(dayday);
//                                strBuf.append(") ");
//                                strBuf.append(hhh);
//                                strBuf.append(":");
//                                strBuf.append(mmm);
//                                strBuf.append(" 출발");
//
//                                time_set.setText(strBuf.toString());
//                                date_c = date;
//                                time = hhh+":"+mmm;
//                            } else {
//                                String dayday = "요일";
//                                if (day == 1) { dayday = "일"; }
//                                else if (day == 2) { dayday = "월"; }
//                                else if (day == 3) { dayday = "화"; }
//                                else if (day == 4) { dayday = "수"; }
//                                else if (day == 5) { dayday = "목"; }
//                                else if (day == 6) { dayday = "금"; }
//                                else if (day == 7) { dayday = "토"; }
//                                else {dayday = "일";}
//
//                                // 시 --- 한자리 수 일때
//                                String hh = String.valueOf(hour);
//                                StringBuffer hhh = new StringBuffer();
//                                if (hh.length() <2) {
//                                    hhh.append("0");
//                                    hhh.append(hh);
//                                } else { hhh.append(hh); }
//                                // 분 --- 한자리 수 일때
//                                String min = String.valueOf(minute);
//                                StringBuffer mmm = new StringBuffer();
//                                if (min.length() < 2) {
//                                    mmm.append("0");
//                                    mmm.append(min);
//                                } else { mmm.append(min); }
//
//                                StringBuffer strBuf = new StringBuffer();
//                                strBuf.append(date);
//                                strBuf.append("일 ");
//                                strBuf.append("(");
//                                strBuf.append(dayday);
//                                strBuf.append(") ");
//                                strBuf.append(hhh);
//                                strBuf.append(":");
//                                strBuf.append(mmm);
//                                strBuf.append(" 출발");
//
//                                time_set.setText(strBuf.toString());
//                                date_c = date;
//                                time = hhh+":"+mmm;
//                            }
//                        }
//                    }
//                };
//
//                Calendar now = Calendar.getInstance();
//                int hour = now.get(java.util.Calendar.HOUR_OF_DAY);
//                int minute = now.get(java.util.Calendar.MINUTE);
//
//                // Whether show time in 24 hour format or not.
//                boolean is24Hour = true;
//
//                timePickerDialog = new TimePickerDialog(google_map.this, AlertDialog.THEME_HOLO_LIGHT, onTimeSetListener, hour, minute, false);
//                timePickerDialog.setIcon(R.drawable.time);
//                timePickerDialog.setTitle("출발시간을 설정하세요");
//                timePickerDialog.getWindow().setGravity(Gravity.BOTTOM);
//
//                timePickerDialog.show();

            }
        });

        // 인원수 설정
        people_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String[] list = new String[]{"1인", "2인", "3인"};
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(google_map.this);
                mBuilder.setTitle("탑승인원을 선택하세요");
                mBuilder.setIcon(R.drawable.person_add);
                mBuilder.setSingleChoiceItems(list, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        people_count.setText(list[i]);
                        if (i == 0) {
                            people_c = 1;
                        } else if (i == 1) {
                            people_c = 2;
                        } else {
                            people_c = 3;
                        }
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog mDialog = mBuilder.create();
                WindowManager.LayoutParams params = mDialog.getWindow().getAttributes();

                Point pt = new Point();
                getWindowManager().getDefaultDisplay().getSize(pt);
                ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getSize(pt);
                int h = pt.x;
                int w = pt.y;
                params.width = 800;
                params.height = 500;
//                WindowManager.LayoutParams.WRAP_CONTENT;
                mDialog.getWindow().setAttributes(params);
                mDialog.getWindow().setGravity(Gravity.BOTTOM);

                mDialog.show();

            }
        });

        // 카풀 요청하기
        request_car.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (request_car.getText().toString().equals("카풀 요청")) {
                    Log.e("ggg", "::::: 카풀 요청");
//                Log.e("ggg", "lat:::"+tMapView.getCenterPointY());

                    Calendar now = Calendar.getInstance();
//                    String ttime = time;

                    if (time.equals("") || time.equals(ttime)) {
                        int h = now.get(Calendar.HOUR_OF_DAY);
                        int m = now.get(Calendar.MINUTE);

                        // 시 --- 한자리 수 일때
                        String hh = String.valueOf(h);
                        StringBuffer hhh = new StringBuffer();
                        if (hh.length() < 2) {
                            hhh.append("0");
                            hhh.append(hh);
                        } else {
                            hhh.append(hh);
                        }
                        // 분 --- 한자리 수 일때
                        String min = String.valueOf(m);
                        StringBuffer mmm = new StringBuffer();
                        if (min.length() < 2) {
                            mmm.append("0");
                            mmm.append(min);
                        } else {
                            mmm.append(min);
                        }

                        time = hhh + ":" + mmm;
                        ttime = time;
                    }

                    // 지금출발 (날짜)
                    if (date_c == null) {
                        date_c = dbFormat_Date.format(now.getTime());
                        date_form = datetimeFormat.format(now.getTime());
                    }

                    time_set.setEnabled(false);
                    people_count.setEnabled(false);
                    cancel.setVisibility(View.INVISIBLE);
                    performRequestCar(USER_NAME, lat_start, lon_start, lat_end, lon_end, date_c, time, people_c, won + " 원", 5);

                    // 요청 카운트 다운 프로그레스 바 ( 1분 설정 1000 = 1초 )
                    req_count = 0;
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(req_count);
                    request_car.setText("드라이버를 찾고 있어요!");
                    countDownTimer = new CountDownTimer(60000, 1000) {

                        @Override
                        public void onTick(long millisUntilFinished) {
                            Log.e("progressBar", "프로그레스 바 진행중..." + req_count + millisUntilFinished);
                            req_count++;
                            progressBar.setProgress(req_count * 100 / (60000 / 1000));
                        }

                        @Override
                        public void onFinish() {
                            Log.e("progressBar", "프로그레스 바 끝!!");
                            // 끝
                            req_count++;
                            time_set.setEnabled(true);
                            people_count.setEnabled(true);
                            cancel.setVisibility(View.VISIBLE);
                            progressBar.setProgress(0);
                            request_car.setText("카풀 요청");

                            // 매치 취소
                            sendMatch(REQUEST_NUM + "@" + USER_NAME + "@" + TARGET_ID + "@이미 완료된 요청입니다.@" + USER_PROFILE);
//                            sendMatch("이미 완료된 요청입니다.");
                            // 요청 리스트 삭제
                            performDelRequestCar(REQUEST_NUM);

                            // 다이얼로그 띄우고 확인 끝나면 프로그래스 바 다시 인비지블
                            AlertDialog.Builder mBuilder = new AlertDialog.Builder(google_map.this);
                            mBuilder.setTitle("ㅠ.ㅠ");
//                            mBuilder.setIcon(R.drawable.);
                            mBuilder.setMessage("매칭 가능한 드라이버를 찾지 못했습니다.");
                            mBuilder.setPositiveButton("재요청", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    Toast.makeText(google_map.this, "매칭 재요청.", Toast.LENGTH_SHORT).show();
                                    request_car.callOnClick();
                                    date_form = datetimeFormat.format(Calendar.getInstance().getTime());
                                }
                            });

                            mBuilder.setNegativeButton("요청 취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    dialogInterface.cancel();
                                }
                            });

                            AlertDialog mDialog = mBuilder.create();
                            mDialog.show();
                        }
                    };
                    countDownTimer.start();

                    // 알림 팝업
                    Intent intent = new Intent(google_map.this, PopUpMachingMessage.class);
                    startActivity(intent);

//                Intent intent = new Intent(google_map.this, WaitingDriver.class);
//                String start = s_markerOptions.getTitle();
//                String end = e_markerOptions.getTitle();
//                String dis = total_distance.getText().toString();
//                String time = total_time.getText().toString();
//
//                intent.putExtra("start", start);
//                intent.putExtra("end", end);
//                intent.putExtra("date", date_c);
//                intent.putExtra("time", time);
//                intent.putExtra("dis", dis);
//                intent.putExtra("fare", won+" 원");
//                intent.putExtra("people", people_c);
//                startActivity(intent);
                } else if (request_car.getText().toString().equals("드라이버를 찾고 있어요!")) {
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(google_map.this);
                    mBuilder.setTitle("매칭 요청을 취소 할까요?");
                    mBuilder.setIcon(R.drawable.cancel_private);
                    mBuilder.setPositiveButton("요청취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            countDownTimer.cancel();
                            progressBar.setProgress(0);
                            time_set.setEnabled(true);
                            people_count.setEnabled(true);
                            cancel.setVisibility(View.VISIBLE);
                            request_car.setText("카풀 요청");
                            Toast.makeText(google_map.this, "매칭 요청을 취소하였습니다.", Toast.LENGTH_SHORT).show();

                            // 요청 리스트 삭제
                            performDelRequestCar(REQUEST_NUM);
                            sendMatch(REQUEST_NUM + "@" + USER_NAME + "@" + TARGET_ID + "@탑승자가 요청을 취소했습니다.@" + USER_PROFILE);
//                            sendMatch("탑승자가 요청을 취소했습니다.");
                        }
                    });

                    mBuilder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });

                    AlertDialog mDialog = mBuilder.create();
                    mDialog.show();
                }
            }
        });

        // 장소 다시 설정하기
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 크기 원래대로 애니메이션
                expandMapAnimation();
                more.setVisibility(View.VISIBLE);
                start_btn.setVisibility(View.VISIBLE);
                end_btn.setVisibility(View.VISIBLE);
                mLocation.setVisibility(View.VISIBLE);
//                constraintLayout.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);

                people_count.setText("1인");
                time = "";
                time_set.setText("지금 출발");

                gMap.clear();
                getDeviceLocation();
            }
        });

//        // 서버로부터 수신한 메세지를 처리하는 곳  ( AsyncTesk를  써도됨 )
//        if (GET_MSG != null) {
//            Log.e("(탑승자)받았다 뭐냐::::", "요청화면:::" + GET_MSG);
//            // 메세지가 왔다면.
//
//            // 방번호, 보낸사람, 받는 사람, 메세지
//            // 받을때는 보낸사람, 보낸내용, 보낸시간
//            String[] filt1 = GET_MSG.split("@");
//            TARGET_PROFILE = filt1[3];
//
//            Toast.makeText(google_map.this, " 매칭 요청완료!! ", Toast.LENGTH_SHORT).show();
//            Log.e("받은 메세지 ", );
//
//            TARGET_ID = filt1[0];
//            Log.e("타겟 아이디 ", TARGET_ID);
//
//            countDownTimer.cancel();
//            progressBar.setProgress(0);
//            time_set.setEnabled(true);
//            people_count.setEnabled(true);
//            cancel.setVisibility(View.VISIBLE);
//            request_car.setText("카풀 요청");
//
//            // 내정보 보내기
//            sendMatch("매칭수락~!@" + USER_PROFILE);
//
//            // 요청 리스트 삭제
//            performDelRequestCar(REQUEST_NUM);
//
//            Intent intent = new Intent(google_map.this, WaitingDriver.class);
//            String start = s_markerOptions.getTitle();
//            String end = e_markerOptions.getTitle();
//            String dis = total_distance.getText().toString();
//            String time = total_time.getText().toString();
//
//            intent.putExtra("start", start);
//            intent.putExtra("end", end);
//            intent.putExtra("date", date_form);
//            intent.putExtra("time", time);
//            intent.putExtra("dis", dis);
//            intent.putExtra("fare", won + " 원");
//            intent.putExtra("people", people_c);
//            intent.putExtra("driver", TARGET_ID);
//            intent.putExtra("driver_profile", TARGET_PROFILE);
//            intent.putExtra("roomNum", REQUEST_NUM);
//            startActivity(intent);
//
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
//        } else if (msg.what == 1112) {
//            Log.e("탑승자) 요청대기부분:::", "채팅중 노티 띄워줄것?");
//        } else {
//            Log.e("msg.what::::", "~~~" + msg.what);
//        }

    }

    // 장소 설정 후 출발, 도착 마커 커스텀
    private void setCustomMarkerView() {
        marker_root_view = LayoutInflater.from(this).inflate(R.layout.custom_marker, null);
        tv_marker = (TextView) marker_root_view.findViewById(R.id.tv_marker);
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
                        Geocoder geocoder = new Geocoder(google_map.this);
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
                                start_btn.setText("해당되는 주소 정보는 없습니다.");
                            } else {
                                String ad = list.get(0).getSubLocality() + " " + list.get(0).getThoroughfare() + " " + list.get(0).getPostalCode();

                                latitude = currentLocation.getLatitude();
                                longitude = currentLocation.getLongitude();

                                address = ad;
                                start_btn.setHint("현위치 : " + ad);
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
                                    won = man + "," + beakWon;
                                    total_taxi_fare.setText("이용요금 : " + won + "원");
                                } else {
                                    total_taxi_fare.setText("이용요금 : " + taxiFare + "원");
                                }

                                total_distance.setText("이동거리 : " + dis + "km");
                                total_time.setText("소요시간 : 약 " + String.valueOf(time) + "분");
                                total_fare.setText("통행요금 : " + totalFare + "원");
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

    // Retrofit 사용 Tmap 에서 정보 가져오기
    private void getRetroTmapData(final LatLng start, final LatLng end) {

        // Retrofit을 사용해 거리, 소요시간, 요금 정보
        apiInterface = TmapClient.getApiClient().create(ApiInterface.class);
        Call<TmapRoute> call = apiInterface.performTmapInfo(
                "1", "application/json", TMAP_API_KEY,         // 버전, 콜백형태, 키값
                String.valueOf(lon_end), String.valueOf(lat_end),               // 도착지 좌표
                String.valueOf(lon_start), String.valueOf(lat_start),           // 출발지 좌표
                "2&tollgateFareOption=8&searchOption=10"             // 2: 시간,요금,거리 정보만
        );


        call.enqueue(new Callback<TmapRoute>() {
            @Override
            public void onResponse(Call<TmapRoute> call, Response<TmapRoute> response) {
                Log.d("레트로핏::::", "성공" + response.body());
                Log.d("레트로핏::::", "성공" + call);


                Log.e("레트로핏 성공인척 실패", "슬픔");

//                mListData.clear();
                TmapRoute tmapRoute = response.body();

                if (tmapRoute != null) {
                    ArrayList<TmapRoute.Features> features = (ArrayList<TmapRoute.Features>) tmapRoute.getFeatures();
                    Log.d("features:::", "(레트로핏)" + features);
                    Log.d("total info:::", "(레트로핏)" + features.get(0).getProperties());

                    String taxiFare = features.get(0).getProperties().getTaxiFare();
                    String totalFare = features.get(0).getProperties().getTotalFare();
                    String totalDistance = features.get(0).getProperties().getTotalDistance();
                    String totalTime = features.get(0).getProperties().getTotalTime();

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
                        String won = man + "," + beakWon;
                        total_taxi_fare.setText("이용요금 : " + won + "원");
                    } else {
                        total_taxi_fare.setText("이용요금 : " + taxiFare + "원");
                    }

                    total_distance.setText("이동거리 : " + dis + "km");
                    total_time.setText("소요시간 : 약 " + String.valueOf(time) + "분");
                    total_fare.setText("통행요금 : " + totalFare + "원");

                    if (!km.equals("")) {
                        if (Integer.valueOf(km) > 30) {
//                            tMapView.setZoomLevel(10);
//                            tMapView.setCenterPoint((lon_start + lon_end) / 2, (lat_start + lat_end) / 2 - 0.138);
                        } else if (Integer.valueOf(km) > 10) {
//                            tMapView.setZoomLevel(11);
//                            tMapView.setCenterPoint((lon_start + lon_end) / 2, (lat_start + lat_end) / 2 - 0.068);
                        }
                    }

//                    Log.e("좌표값 확인:::","LatLng--> "+ features.get(0).getGeometry().getCoordinates().get(0));
//                    Log.e("좌표값 확인:::","LatLng--> "+features.get(0).getGeometry().getCoordinates().get(0));

//                    for (int i = 0; i < features.get(i).getGeometry().getCoordinates().get(i).getLatlng().size(); i++) {
//                        gMap.addPolyline(new PolylineOptions()
//                                .add(new LatLng(Double.parseDouble(String.valueOf(features.get(i).getGeometry().getCoordinates().get(i).getLatlng().get(0))), Double.parseDouble(String.valueOf(features.get(i).getGeometry().getCoordinates().get(i).getLatlng().get(1)))))
//                                .add(new LatLng(Double.parseDouble(String.valueOf(features.get(i).getGeometry().getCoordinates().get(i+1).getLatlng().get(0))), Double.parseDouble(String.valueOf(features.get(i).getGeometry().getCoordinates().get(i+1).getLatlng().get(1)))))
//                                .width(20)
//                                .color(Color.LTGRAY)
//                        );
//                    }

                }
            }

            @Override
            public void onFailure(Call<TmapRoute> call, Throwable t) {
                Log.d("레트로핏::::", "실패");
                Log.d("레트로핏::::", "" + t.getMessage());

            }
        });

        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("start_lat", "" + lat_start);
                Log.e("start_lon", "" + lon_start);
                Log.e("end_lat", "" + lat_end);
                Log.e("end_lon", "" + lon_end);

//                TMapPoint startPoint = new TMapPoint(lat_start, lon_start);
//                TMapPoint endPoint = new TMapPoint(lat_end, lon_end);

                try {
//                    TMapPolyLine tMapPolyLine = new TMapData().findPathData(startPoint, endPoint);
//                    tMapPolyLine.setLineColor(Color.CYAN);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    tMapPolyLine.setLineColor(getColor(R.color.colorPrimaryDark));
//                }
//                    tMapPolyLine.setLineWidth(2);


                    Log.e("폴리라인 그렸다:::", "티맵");
                    // 구글로 어떻게 그리지??????
//                    tMapView.addTMapPolyLine("Line1", tMapPolyLine);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        th.start();
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "취소 하였습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (requestCode == 111) {                                                  //--------------------------------------------------- 출발지 버튼 클릭으로 갔다옴
            more.setVisibility(View.GONE);
            start_btn.setVisibility(View.GONE);
            end_btn.setVisibility(View.GONE);
            mLocation.setVisibility(View.GONE);
            constraintLayout.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.VISIBLE);

            lat_start = data.getDoubleExtra("lat_start", 0);
            lon_start = data.getDoubleExtra("lon_start", 0);
            lat_end = data.getDoubleExtra("lat_end", 0);
            lon_end = data.getDoubleExtra("lon_end", 0);
            String end_place = data.getStringExtra("address_end");
            String start_place = data.getStringExtra("address_start");

            ClusterMarker end = new ClusterMarker(lat_end, lon_end, end_place, "약 42분 소요");
            ClusterMarker start = new ClusterMarker(lat_start, lon_start, start_place, "");

            // 도착지 마커
            LatLng e_position = new LatLng(end.getLat(), end.getLon());
            String e_place = end.getPlace();
            tv_marker.setText(e_place);
            tv_marker.setBackgroundResource(R.drawable.end_picker);
            tv_marker.setTextColor(Color.WHITE);

            // 도착지 마커옵션 (찍을 위치좌표, 들어갈 텍스트 설정, 커스텀 추가)
            e_markerOptions = new MarkerOptions();
            e_markerOptions.title(e_place);
            e_markerOptions.position(e_position);
            e_markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker_root_view)));
            gMap.addMarker(e_markerOptions);

            // 출발지 마커
            LatLng s_position = new LatLng(start.getLat(), start.getLon());
            String s_place = start.getPlace();
            tv_marker.setText(s_place);
            tv_marker.setBackgroundResource(R.drawable.start_picker);
            tv_marker.setTextColor(Color.BLACK);

            // 출발지 마커옵션 (찍을 위치좌표, 들어갈 텍스트 설정, 커스텀 추가)
            s_markerOptions = new MarkerOptions();
            s_markerOptions.title(s_place);
            s_markerOptions.position(s_position);
            s_markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker_root_view)));
            gMap.addMarker(s_markerOptions);

//            getRetroTmapData(s_position, e_position);
            getJsonData(s_position, e_position);
            Log.e("출발 좌표값?", ":::" + mapPoints.get(0));
            Log.e("도착 좌표값?", ":::" + mapPoints.get(mapPoints.size() - 1));

            Polyline polyline = gMap.addPolyline(new PolylineOptions().addAll(mapPoints));
            polyline.setColor(Color.MAGENTA);
//            zoomRoute(polyline.getPoints());

            // 뷰 크기조절 애니메이션
            contractMapAnimation(polyline.getPoints());

//            if(mMapLayoutState == MAP_LAYOUT_STATE_CONTRACTED){
//                mMapLayoutState = MAP_LAYOUT_STATE_EXPANDED;
//                expandMapAnimation();
//                Log.e("출발::","1번 if 문");
//            }
//            else if(mMapLayoutState == MAP_LAYOUT_STATE_EXPANDED){
//                mMapLayoutState = MAP_LAYOUT_STATE_CONTRACTED;
//                contractMapAnimation();
//                Log.e("출발::","2번 else if 문");
//            }

//            for (int i = 0; i < mapPoints.size() - 1; i++) {
//                Polyline polyline = gMap.addPolyline(new PolylineOptions()
//                        .add(mapPoints.get(i))
//                        .add(mapPoints.get(i + 1))
//                        .width(15)
//                        .color(Color.MAGENTA)
//                );
//            }

        } else if (requestCode == 112) {                                                  //--------------------------------------------------- 도착지 버튼 클릭으로 갔다옴
            more.setVisibility(View.GONE);
            start_btn.setVisibility(View.GONE);
            end_btn.setVisibility(View.GONE);
            mLocation.setVisibility(View.GONE);
            constraintLayout.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.VISIBLE);

            lat_start = data.getDoubleExtra("lat_start", 0);
            lon_start = data.getDoubleExtra("lon_start", 0);
            lat_end = data.getDoubleExtra("lat_end", 0);
            lon_end = data.getDoubleExtra("lon_end", 0);
            String end_place = data.getStringExtra("address_end");
            String start_place = data.getStringExtra("address_start");

            ClusterMarker end = new ClusterMarker(lat_end, lon_end, end_place, "약 42분 소요");
            ClusterMarker start = new ClusterMarker(lat_start, lon_start, start_place, "");

            // 도착지 마커
            LatLng e_position = new LatLng(end.getLat(), end.getLon());
            String e_place = end.getPlace();
            tv_marker.setText(e_place);
            tv_marker.setBackgroundResource(R.drawable.end_picker);
            tv_marker.setTextColor(Color.WHITE);

            // 도착지 마커옵션 (찍을 위치좌표, 들어갈 텍스트 설정, 커스텀 추가)
            e_markerOptions = new MarkerOptions();
            e_markerOptions.title(e_place);
            e_markerOptions.position(e_position);
            e_markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker_root_view)));
            gMap.addMarker(e_markerOptions);

            // 출발지 마커
            LatLng s_position = new LatLng(start.getLat(), start.getLon());
            String s_place = start.getPlace();
            tv_marker.setText(s_place);
            tv_marker.setBackgroundResource(R.drawable.start_picker);
            tv_marker.setTextColor(Color.BLACK);

            // 출발지 마커옵션 (찍을 위치좌표, 들어갈 텍스트 설정, 커스텀 추가)
            s_markerOptions = new MarkerOptions();
            s_markerOptions.title(s_place);
            s_markerOptions.position(s_position);
            s_markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker_root_view)));
            gMap.addMarker(s_markerOptions);

//            getRetroTmapData(s_position, e_position);
            getJsonData(s_position, e_position);
            Log.e("출발 좌표값?", ":::" + mapPoints.get(0));
            Log.e("도착 좌표값?", ":::" + mapPoints.get(mapPoints.size() - 1));

            // 실제로 지도에 그리는 부분
            Polyline polyline = gMap.addPolyline(new PolylineOptions().addAll(mapPoints));
            polyline.setColor(Color.MAGENTA);
//            zoomRoute(polyline.getPoints());

            // 뷰 크기조절 애니메이션
            contractMapAnimation(polyline.getPoints());

//            if(mMapLayoutState == MAP_LAYOUT_STATE_CONTRACTED){
//                mMapLayoutState = MAP_LAYOUT_STATE_EXPANDED;
//                expandMapAnimation();
//                Log.e("도착::","1번 if 문");
//            }
//            else if(mMapLayoutState == MAP_LAYOUT_STATE_EXPANDED){
//                mMapLayoutState = MAP_LAYOUT_STATE_CONTRACTED;
//                contractMapAnimation();
//                Log.e("도착::","2번 else if 문");
//            }
        }
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

    // 맵 크기 확장, 원래대로
    private void expandMapAnimation() {
        Log.e("TTTTTTTTTTTTTTTTTTTTTTT", "맵 확장 하는 애니메이션");
        ViewAnimation mapAnimationWrapper = new ViewAnimation(mMapContainer);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                60,
                100);
        mapAnimation.setDuration(800);

        ViewAnimation recyclerAnimationWrapper = new ViewAnimation(constraintLayout);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                40,
                0);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();
    }

    // 요청확인 맵 크기 변경
    private void waitingMapAnimation() {
        Log.e("TTTTTTTTTTTTTTTTTTTTTTT", "요청 기다리는 맵 확장 하는 애니메이션");
        ViewAnimation mapAnimationWrapper = new ViewAnimation(mMapContainer);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                100,
                60);
        mapAnimation.setDuration(800);

        mapAnimation.start();
    }

    // 경로안내 축소
    private void contractMapAnimation(final List<LatLng> latlng) {
        Log.e("TTTTTTTTTTTTTTTTTTTTTTT", "맵 축소 하는 애니메이션");
        ViewAnimation mapAnimationWrapper = new ViewAnimation(mMapContainer);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                100,
                60);
        mapAnimation.setDuration(800);

        ViewAnimation recyclerAnimationWrapper = new ViewAnimation(constraintLayout);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                0,
                40);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();

        // 애니메이션 끝난 후 줌 아웃
        mapAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                Log.e("tttttttttttttttttttttt", "애니메이션 끝남");
                zoomRoute(latlng);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    // 레트로핏 데이터 넣기 (카풀 요청 정보)
    public void performRequestCar(String user, double sLat, double sLon, double eLat, double eLon, String date, String time, int people, String fare, double rating) {

        Log.e("요청할때 보내는 값::::", "이름: " + user);
        Log.e("요청할때 보내는 값::::", "sLat: " + sLat);
        Log.e("요청할때 보내는 값::::", "sLon: " + sLon);
        Log.e("요청할때 보내는 값::::", "eLat: " + eLat);
        Log.e("요청할때 보내는 값::::", "eLon: " + eLon);
        Log.e("요청할때 보내는 값::::", "날자: " + date);
        Log.e("요청할때 보내는 값::::", "시간: " + time);
        Log.e("요청할때 보내는 값::::", "인원: " + people);

        Call<RequestCar> call = Login.apiInterface.performRequestCar(user, sLat, sLon, eLat, eLon, date, time, people, fare, rating);

        call.enqueue(new Callback<RequestCar>() {
            @Override
            public void onResponse(Call<RequestCar> call, Response<RequestCar> response) {

                String i = response.body().getResponse();
                REQUEST_NUM = Integer.valueOf(i);
                Log.e("카풀 요청 성공", "인덱스 값 : " + i);
//                if (response.body().getResponse().equals("ok")) {
//                    Login.prefConfig.displayToast("카풀 요청 성공!!!");
//
//                } else {
//                    Login.prefConfig.displayToast("에러에러에러.....");
//                }
//                new SocketClient(i + "@" + USER_NAME).start();
                if (RealService.serviceIntent == null) {
                    serviceIntent = new Intent(google_map.this, RealService.class);
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
                    socketClient.roomAndUserData = REQUEST_NUM + "@" + USER_NAME;
                }
            }

            @Override
            public void onFailure(Call<RequestCar> call, Throwable t) {
                Toast.makeText(google_map.this, "카풀 요청 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 카풀 요청 취소 (삭제)
    public void performDelRequestCar(int idx) {
        Log.e("삭제할때 보내는 값::::", "idx : " + idx);

        Call<RequestCar> call = Login.apiInterface.performDelRequestCar(idx);

        call.enqueue(new Callback<RequestCar>() {
            @Override
            public void onResponse(Call<RequestCar> call, Response<RequestCar> response) {
                if (response.body().getResponse().equals("ok")) {
//                    Login.prefConfig.displayToast("요청 삭제 성공!!!");
                    Log.e("삭제한 요청 넘버:::", "종료할 서버 넘버:::" + REQUEST_NUM);
//                    new SocketClient(REQUEST_NUM + "@" + USER_NAME).destroy();
//                    sendMatch();
                } else {
                    Login.prefConfig.displayToast("카풀 요청 삭제 에러에러에러.....");
                }
            }

            @Override
            public void onFailure(Call<RequestCar> call, Throwable t) {
                Toast.makeText(google_map.this, "카풀 요청 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     **///-------  CHAT

//    // 내부클래스   ( 접속용 )
//    class SocketClient extends Thread {
//
//        DataInputStream in = null;
//        DataOutputStream out = null;
//        String roomAndUserData; // 방 정보 ( 방번호 /  접속자 아이디 )
//
//        public SocketClient(String roomAndUserData) {
//            this.roomAndUserData = roomAndUserData;
//        }
//
//        public void run() {
//            try {
//                // 채팅 서버에 접속 ( 연결 )  ( 서버쪽 ip와 포트 )
//                socket = new Socket(IP, port);
//
//                // 메세지를 서버에 전달 할 수 있는 통로 ( 만들기 )
//                out = new DataOutputStream(socket.getOutputStream());
//                in = new DataInputStream(socket.getInputStream());
//
//                // 서버에 초기 데이터 전송  ( 방번호와 접속자 아이디가 담겨서 간다 ) -  식별자 역할을 하게 될 거임.
//                out.writeUTF(roomAndUserData);
//
//                // (메세지 수신용 쓰레드 생성 ) 리시브 쓰레드 시작
//                recevie = new ReceiveThread(socket);
//                recevie.start();
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    } //SocketClient의 끝
//
//    // ( 메세지 수신용 )   -  서버로부터 받아서, 핸들러에서 처리하도록 할 거.
//    class ReceiveThread extends Thread {
//
//        Socket socket = null;
//        DataInputStream input = null;
//
//        public ReceiveThread(Socket socket) {
//            this.socket = socket;
//
//            try {
//                // 채팅 서버로부터 메세지를 받기 위한 스트림 생성.
//                input = new DataInputStream(socket.getInputStream());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void run() {
//            try {
//                while (input != null) {
//                    // 채팅 서버로 부터 받은 메세지
//                    String msg = input.readUTF();
//                    Log.e("(탑승자 화면)::: ", "채팅 서버로 부터 받은 메시지 : " + msg);
//
//                    // 방번호, 보낸사람, 받는 사람, 메세지
//                    // 받을때는 보낸사람, 보낸내용, 보낸시간
//                    String[] filt1 = msg.split("@");
//
//                    if (msg != null) {
//                        // 핸들러에게 전달할 메세지 객체
//                        Message hdmg = msgHandler.obtainMessage();
//
//                        if (filt1[1].equals("매칭수락~!")) { // 매칭수락
//                            Log.e("매칭수락 메시지 받는다","1111:::"+msg);
//                            // 핸들러에게 전달할 메세지의 식별자
//                            hdmg.what = 1111;
//                            // 메세지의 본문
//                            hdmg.obj = msg;
//                            // 핸들러에게 메세지 전달 ( 화면 처리 )
//                            msgHandler.sendMessage(hdmg);
//
//                        } else { // 채팅
//                            Log.e("채팅 메시지 받는다","1112:::"+msg);
//                            // 핸들러에게 전달할 메세지의 식별자
//                            hdmg.what = 1112;
//                            // 메세지의 본문
//                            hdmg.obj = msg;
//                            // 핸들러에게 메세지 전달 ( 화면 처리 )
//                            msgHandler.sendMessage(hdmg);
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
//
    // 내부 클래스  ( 메세지 전송용 )
    class SendThread extends Thread {
        Socket socket;
        String sendmsg;
        DataOutputStream output;


        public SendThread(Socket socket, String sendmsg) {
            this.socket = socket;
            this.sendmsg = sendmsg;
            try {
                // 채팅 서버로 메세지를 보내기 위한  스트림 생성.
                output = new DataOutputStream(socket.getOutputStream());

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // 서버로 메세지 전송 ( 이클립스 서버단에서 temp 로 전달이 된다.
        public void run() {
            try {
                if (output != null) {
                    if (sendmsg != null) {

                        // 여기서 방번호와 상대방 아이디 까지 해서 보내줘야 할거같다 .
                        // 서버로 메세지 전송하는 부분
                        output.writeUTF(String.valueOf(REQUEST_NUM) + "@" + USER_NAME + "@" + TARGET_ID + "@" + sendmsg);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 상대방에게 알림 보내는 메시지
    private void sendMatch(String msg) {
        // 매치 됬다 알려주기
        ms.myServiceFunc(msg);
//        send = new SendThread(socket, msg);
//        send.start();
    }

    // Stop 소켓 종료
    @Override
    protected void onStop() {
        super.onStop();

        Log.e("onStop", "stop");
//        unbindService(conn);
//        // 소켓종료
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


