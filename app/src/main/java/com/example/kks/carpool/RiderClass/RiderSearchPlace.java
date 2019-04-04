package com.example.kks.carpool.RiderClass;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kks.carpool.R;
import com.example.kks.carpool.model.PlaceInfo;
import com.facebook.stetho.Stetho;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.Marker;

public class RiderSearchPlace extends AppCompatActivity implements PlaceSearchCallback, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "RiderSearchPlace";

    private EditText startText, endText;
    private RecyclerView recyclerView_start, recyclerView_end;
    public static PlaceSearchAdapter adapter;
    //private RVAdapterEndPlace end_adapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable workRunnable;
    private final long DELAY = 500;

    private ImageButton start_clear, end_clear;
    private String start_btn, now_point;
    private double start_lat, start_lon, end_lat, end_lon;

    private GoogleApiClient mGoogleApiClient;
    private PlaceInfo mPlace;
    private Marker mMarker;
    private String location_name;
    private TextView mPlacePicker;

    // getIntent
    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e(TAG, "onCreate");

        Stetho.initializeWithDefaults(this);
        // 출발 도착 장소 텍스트
        startText = findViewById(R.id.start_text);
        endText = findViewById(R.id.end_text);
        // 장소 띄우기 (리사이클러뷰)
        recyclerView_start = findViewById(R.id.rv_listview_start);
        recyclerView_end = findViewById(R.id.rv_listview_end);
        // X 버튼 생기기 (텍스트 삭제용)
        start_clear = findViewById(R.id.start_clear);
        end_clear = findViewById(R.id.end_clear);
        // 구글 플레이스로 이동
        mPlacePicker = findViewById(R.id.google_place);

        // 출발 || 도착 분류 (버튼 텍스트)
        Intent get = getIntent();
        start_lat = get.getDoubleExtra("Lat",0);
        start_lon = get.getDoubleExtra("Lon",0);
        start_btn = get.getStringExtra("btn");
        now_point = get.getStringExtra("now");

        if (start_btn.equals("출발지")) {
            startText.setText(now_point);
            mPlacePicker.setText("지도로 지정하기 (출발지)");
        } else if (start_btn.equals("도착지")) {
            startText.setHint("현위치 : " + now_point);
            endText.requestFocus();
            mPlacePicker.setText("지도로 지정하기 (도착지)");
        }

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void onResume() {
        super.onResume();
        layoutInit_start();
        layoutInit_end();

        startText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.e("포커스:::::", "시작장소");
                adapter.clear();
                mPlacePicker.setText("지도로 지정하기 (출발지)");
                if (start_btn.equals("도착지")) {
                    startText.setText(now_point);
                }
                if (startText.getText().toString().length() > 0) {
                    start_clear.setVisibility(View.VISIBLE);
                    end_clear.setVisibility(View.INVISIBLE);
                } else {
                    start_clear.setVisibility(View.INVISIBLE);
                    end_clear.setVisibility(View.INVISIBLE);
                }
//                recyclerView_start.setVisibility(View.VISIBLE);
//                recyclerView_end.setVisibility(View.INVISIBLE);
                return false;
            }
        });

        endText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.e("포커스:::::", "도착장소");
                adapter.clear();
                mPlacePicker.setText("지도로 지정하기 (도착지)");
//                recyclerView_start.setVisibility(View.INVISIBLE);
//                recyclerView_end.setVisibility(View.VISIBLE);
                if (endText.getText().toString().length() > 0) {
                    start_clear.setVisibility(View.INVISIBLE);
                    end_clear.setVisibility(View.VISIBLE);
                } else {
                    start_clear.setVisibility(View.INVISIBLE);
                    end_clear.setVisibility(View.INVISIBLE);
                }
                return false;
            }
        });

        start_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startText.setText("");
                adapter.clear();

            }
        });

        end_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endText.setText("");
                adapter.clear();
            }
        });

        mPlacePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPlacePicker.getText().toString().contains("출발")) {
                    Log.e("출발이냐도착이냐::::", "출발이다");
                    Intent intent = new Intent(RiderSearchPlace.this, RiderPickPlace.class);
                    intent.putExtra("picker", "출발");
                    startActivityForResult(intent, 113);
                } else {
                    Log.e("출발이냐도착이냐:::", "도착이다");
                    Intent intent = new Intent(RiderSearchPlace.this, RiderPickPlace.class);
                    intent.putExtra("picker", "도착");
                    startActivityForResult(intent, 114);
                }
            }
        });

        hideSoftKeyboard();

    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void showToast(String place) {
        Toast.makeText(this, place + " 상세보기.", Toast.LENGTH_SHORT).show();
    }

    // 출발지 설정 완료 버튼
    @Override
    public void startPlace(String place, String lat, String lon) {
        startText.setText(place);
        startText.setSelection(startText.getText().length());

        // 출발지 좌표 설정완료
        start_lat = Double.parseDouble(lat);
        start_lon = Double.parseDouble(lon);

        if (endText.getText().toString().length() < 3) {
            adapter.clear();
            endText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            mPlacePicker.setText("지도로 지정하기 (도착지)");
        } else {
            if (start_lat != 0 && start_lon != 0 && end_lat != 0 && end_lon != 0) {
                Toast.makeText(this, "장소설정완료!", Toast.LENGTH_SHORT).show();
                Intent go = new Intent();
                go.putExtra("lat_start", start_lat);
                go.putExtra("lon_start", start_lon);
                go.putExtra("lat_end", end_lat);
                go.putExtra("lon_end", end_lon);
                go.putExtra("address_end", endText.getText().toString());
                go.putExtra("address_start", startText.getText().toString());
                setResult(RESULT_OK, go);
                finish();
            } else {
                Toast.makeText(this, "장소설정뭔가 안됨", Toast.LENGTH_SHORT).show();
                Log.e("start_lat", "" + start_lat);
                Log.e("start_lon", "" + start_lon);
                Log.e("end_lat", "" + end_lat);
                Log.e("end_lon", "" + end_lon);
            }
        }
    }

    // 도착지 설정 완료 버트
    @Override
    public void endPlace(String place, String lat, String lon) {
        endText.setText(place);
        endText.setSelection(endText.getText().length());

        // 도착지 좌표 설정 완료
        end_lat = Double.parseDouble(lat);
        end_lon = Double.parseDouble(lon);

        if (startText.getText().toString().length() < 3) {
            adapter.clear();
            startText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            mPlacePicker.setText("지도로 지정하기 (출발지)");
        } else {
            if (start_lat != 0 && start_lon != 0 && end_lat != 0 && end_lon != 0) {
                Toast.makeText(this, "장소설정완료!", Toast.LENGTH_SHORT).show();
                Intent go = new Intent();
                go.putExtra("lat_start", start_lat);
                go.putExtra("lon_start", start_lon);
                go.putExtra("lat_end", end_lat);
                go.putExtra("lon_end", end_lon);
                go.putExtra("address_end", endText.getText().toString());
                go.putExtra("address_start", startText.getText().toString());
                setResult(RESULT_OK, go);
                finish();
            } else {
                Toast.makeText(this, "장소설정뭔가 안됨", Toast.LENGTH_SHORT).show();
                Log.e("start_lat", "" + start_lat);
                Log.e("start_lon", "" + start_lon);
                Log.e("end_lat", "" + end_lat);
                Log.e("end_lon", "" + end_lon);
            }
        }
    }

    private void layoutInit_start() {

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new PlaceSearchAdapter();
        recyclerView_start.setLayoutManager(layoutManager);
        recyclerView_start.setAdapter(adapter);
        adapter.setCallback(this);

        startText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                final String keyword = s.toString();

                if (keyword.length() > 1) {
                    adapter.filter(keyword, 1);
                    adapter.notifyDataSetChanged();
                }
                if (keyword.length() > 0) {
                    start_clear.setVisibility(View.VISIBLE);
                    end_clear.setVisibility(View.INVISIBLE);
                } else {
                    start_clear.setVisibility(View.INVISIBLE);
                    end_clear.setVisibility(View.INVISIBLE);
                }
//                handler.removeCallbacks(workRunnable);
//                workRunnable = new Runnable() {
//                    @Override
//                    public void run() {
//                        adapter.filter(keyword, 1);
//                    }
//                };
//                handler.postDelayed(workRunnable, DELAY);
            }
        });
    }

    private void layoutInit_end() {

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new PlaceSearchAdapter();
        recyclerView_end.setLayoutManager(layoutManager);
        recyclerView_end.setAdapter(adapter);
        adapter.setCallback(this);

        endText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {


                final String keyword = s.toString();

//                handler.removeCallbacks(workRunnable);
//                workRunnable = new Runnable() {
//                    @Override
//                    public void run() {
                if (keyword.length() > 1) {
                    adapter.filter(keyword, 2);
//                    }
//                };
//                handler.postDelayed(workRunnable, DELAY);
                    adapter.notifyDataSetChanged();
                }
                if (keyword.length() > 0) {
                    start_clear.setVisibility(View.INVISIBLE);
                    end_clear.setVisibility(View.VISIBLE);
                } else {
                    start_clear.setVisibility(View.INVISIBLE);
                    end_clear.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

//    private void moveCamera(LatLng latLng, float zoom, PlaceInfo placeInfo)  {
//        Log.d("TAG", "카메라 이동 lat : "+ latLng.latitude +", lng : "+latLng.longitude);
//        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
//
//        gMap.clear();
//
//        gMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(RiderStartMap.this));
//
//        if (placeInfo != null) {
//            try {
//                String snippet = "주소 : " + placeInfo.getAddress() +"\n";
//                MarkerOptions options = new MarkerOptions().position(latLng).title(placeInfo.getName()).snippet(snippet);
//
//                mMarker = gMap.addMarker(options);
//
//            } catch (NullPointerException e) {
//                Log.e("TAG", "NULLLLLLLLLLLL!!!!!!!!!!!");
//            }
//        }  else   {
//            gMap.addMarker(new MarkerOptions().position(latLng));
//        }
//
//        hideSoftKeyboard();
//    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "취소 하였습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (requestCode == 113) {
            address = data.getStringExtra("address");
            start_lat = data.getDoubleExtra("lat_start", 0);
            start_lon = data.getDoubleExtra("lon_start", 0);
            startText.setText(address);

            if (start_lat != 0 && start_lon != 0 && end_lat != 0 && end_lon != 0) {
                Toast.makeText(this, "장소설정완료!", Toast.LENGTH_SHORT).show();
                Intent go = new Intent();
                go.putExtra("lat_start", start_lat);
                go.putExtra("lon_start", start_lon);
                go.putExtra("lat_end", end_lat);
                go.putExtra("lon_end", end_lon);
                go.putExtra("address_end", endText.getText().toString());
                go.putExtra("address_start", startText.getText().toString());
                setResult(RESULT_OK, go);
                finish();
            } else {
                Log.e("start_lat", "" + start_lat);
                Log.e("start_lon", "" + start_lon);
                Log.e("end_lat", "" + end_lat);
                Log.e("end_lon", "" + end_lon);
            }
        } else if (requestCode == 114) {
            address = data.getStringExtra("address");
            end_lat = data.getDoubleExtra("lat_end", 0);
            end_lon = data.getDoubleExtra("lon_end", 0);
            endText.setText(address);

            if (start_lat != 0 && start_lon != 0 && end_lat != 0 && end_lon != 0) {
                Toast.makeText(this, "장소설정완료!", Toast.LENGTH_SHORT).show();
                Intent go = new Intent();
                go.putExtra("lat_start", start_lat);
                go.putExtra("lon_start", start_lon);
                go.putExtra("lat_end", end_lat);
                go.putExtra("lon_end", end_lon);
                go.putExtra("address_end", endText.getText().toString());
                if (startText.getText().toString().equals("")) {
                    go.putExtra("address_start", startText.getHint().toString());
                } else {
                    go.putExtra("address_start", startText.getText().toString());
                }
                setResult(RESULT_OK, go);
                finish();
            } else {
                Log.e("start_lat", "" + start_lat);
                Log.e("start_lon", "" + start_lon);
                Log.e("end_lat", "" + end_lat);
                Log.e("end_lon", "" + end_lon);
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "연결실패!!!!!!!!!!!!!!!!!!!!!!!", Toast.LENGTH_SHORT).show();
    }
}
