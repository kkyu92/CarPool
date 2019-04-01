package com.example.kks.carpool.driver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kks.carpool.Login;
import com.example.kks.carpool.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static com.example.kks.carpool.driver.FragmentMyLoad.END_POINT;
import static com.example.kks.carpool.driver.FragmentMyLoad.START_POINT;
import static com.example.kks.carpool.driver.start_Driver.driver_txt;
import static com.example.kks.carpool.google_map.dbFormat_Date;
import static com.example.kks.carpool.google_map.dbFormat_Time;

public class FragmentRequestList extends Fragment implements requestCallback, SwipeRefreshLayout.OnRefreshListener {

    private View view;

    //RecyclerView
    private int type = 1;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView request_rv;
    private TextView noList;
    private requestAdapter adapter;
    private ArrayList<requestItem> mListData;

    private FusedLocationProviderClient mFusedLocationClient;
    private Location currentLocation;

    // Calender
    Calendar now;
    String date, date_s, time;

    // 필터 거친 값
    private String sDate, eDate, setTime;
    private int people, sDistance, eDistance;
    private Bundle filter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_request_list, container, false);

        // 저장 불러오기
        myRouteLoad();

        // 리사이클러뷰 카풀 요청리스트
        refreshLayout = view.findViewById(R.id.swipe_refresh);
        request_rv = view.findViewById(R.id.request_rv);
        noList = view.findViewById(R.id.noList);

        now = Calendar.getInstance();
        int month = now.get(Calendar.MONTH);
        now.set(now.get(Calendar.YEAR), month, now.get(Calendar.DAY_OF_MONTH));
        date = dbFormat_Date.format(now.getTime());
        time = dbFormat_Time.format(now.getTime());
//        time = now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE);
        Log.e("getDate:::", "" + date);
        Log.e("getTime:::", "" + time);

        // 값 받는 부분
        filter = this.getArguments();
        if (filter != null) {
            sDate = filter.getString("startDate");
            eDate = filter.getString("endDate");
            setTime = filter.getString("setTime");
            people = filter.getInt("setPeople", 0);
            sDistance = filter.getInt("startD", 5);
            eDistance = filter.getInt("endD", 5);

            Log.e("번들 받았다:::", "~~~~~~~~~~~~~~~~");
            Log.e("시작날짜:::", "" + sDate);
            Log.e("종료날짜:::", "" + eDate);
            Log.e("시간:::", "" + setTime);
            Log.e("탑승인원:::", "" + people);
            Log.e("출발지와의 거리:::", "" + sDistance);
            Log.e("도착지와의 거리:::", "" + eDistance);
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getContext()));

        setRefresh();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // 새로고침

        getDeviceLocation();
        // 사용자 출도착 좌표 설정 안하면 설정하라고 텍스트에 넣기


    }

    // 카플 요청 리스트 기본 값
    private void performRequestDriver(int type, final double mLat, final double mLon) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        adapter = new requestAdapter();
        mListData = new ArrayList<>();
        request_rv.setLayoutManager(layoutManager);
        request_rv.setAdapter(adapter);
        adapter.setCallback(this);

        Call<ArrayList<requestParse>> call = Login.apiInterface.performRequestDriver(type, date, time);

        call.enqueue(new Callback<ArrayList<requestParse>>() {
            @Override
            public void onResponse(Call<ArrayList<requestParse>> call, Response<ArrayList<requestParse>> response) {

                ArrayList<requestParse> list = response.body();

                if (list != null) {

                    for (int i = 0; i < list.size(); i++) {
                        String name = list.get(i).getUser();
                        double sLat = list.get(i).getsLat();
                        double sLon = list.get(i).getsLon();
                        double eLat = list.get(i).geteLat();
                        double eLon = list.get(i).geteLon();
                        String date = list.get(i).getDate();
                        String time = list.get(i).getTime();
                        String people = list.get(i).getPeople();
                        String fare = list.get(i).getFare();
                        String rat = list.get(i).getRating();
                        String idx = list.get(i).getIdx();

                        RatingBar ratingBar = new RatingBar(getContext());
                        if (rat.contains("회원")) {
                            ratingBar.setRating(5);
                        } else {
                            float rating = Float.valueOf(rat);
                            ratingBar.setRating(rating);
                        }
                        // 거리비교
                        Location myLocation = new Location("locationA");
                        myLocation.setLatitude(mLat);
                        myLocation.setLongitude(mLon);
                        Location startLocation = new Location("locationB");
                        startLocation.setLatitude(sLat);
                        startLocation.setLongitude(sLon);
                        double distance = myLocation.distanceTo(startLocation) * 0.001;
                        double dis = Math.round(distance * 10d) / 10d;

                        // 좌표로 주소 받아오기
                        Geocoder geocoder = new Geocoder(getContext());
                        List<Address> add_start = null;
                        List<Address> add_end = null;
                        try {
                            add_start = geocoder.getFromLocation(sLat, sLon, 1);
                            add_end = geocoder.getFromLocation(eLat, eLon, 1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String startAddr = add_start.get(0).getSubLocality() + " " + add_start.get(0).getThoroughfare();
                        String endAddr = add_end.get(0).getSubLocality() + " " + add_end.get(0).getThoroughfare();

                        // 출발지 거리 비교하기
//                        float dis = Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), sLat, sLon, );

                        mListData.add(new requestItem("출발지까지 " + dis + "Km", people + " 인", "약 " + fare, startAddr, endAddr, String.valueOf(date), time, ratingBar, sLat, sLon, eLat, eLon, name, idx));
                        Log.d("start_ListData:::", "" + mListData);
                    }
                    adapter.setData(mListData);
                    adapter.notifyDataSetChanged();
                    if (START_POINT == null || END_POINT == null) {
                        noList.setText("운전자의 경로를 등록해주세요!!!");
                        noList.setVisibility(View.VISIBLE);
                    } else if (mListData == null || mListData.size() == 0) {
                        noList.setText("카풀 요청 정보가 없습니다.");
                        noList.setVisibility(View.VISIBLE);
                    } else {
                        request_rv.setVisibility(View.VISIBLE);
                        noList.setText("카풀 요청 정보가 없습니다.");
                        noList.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<requestParse>> call, Throwable t) {
                Toast.makeText(getContext(), "카풀 요청 실패", Toast.LENGTH_SHORT).show();
                Log.e("에러에러:::", "" + t.getMessage());
            }
        });
    }

    // 카풀 요청 리스트 필터 ( 정렬옵션 부분 )
    private void performRequestFilter(int type, final double mLat, final double mLon, String sDate, String eDate, String settime, int people, final int sDistance, final int eDistance) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        adapter = new requestAdapter();
        mListData = new ArrayList<>();
        request_rv.setLayoutManager(layoutManager);
        request_rv.setAdapter(adapter);
        adapter.setCallback(this);

        // 현재날짜 시간 ===> date / time
        Call<ArrayList<requestParse>> call = Login.apiInterface.performRequestDriverFilter(type, sDate, eDate, settime, people, sDistance, eDistance);

        call.enqueue(new Callback<ArrayList<requestParse>>() {
            @Override
            public void onResponse(Call<ArrayList<requestParse>> call, Response<ArrayList<requestParse>> response) {

                ArrayList<requestParse> list = response.body();

                if (list != null) {

                    for (int i = 0; i < list.size(); i++) {
                        String name = list.get(i).getUser();
                        double sLat = list.get(i).getsLat();
                        double sLon = list.get(i).getsLon();
                        double eLat = list.get(i).geteLat();
                        double eLon = list.get(i).geteLon();
                        String date = list.get(i).getDate();
                        String time = list.get(i).getTime();
                        String people = list.get(i).getPeople();
                        String fare = list.get(i).getFare();
                        String rat = list.get(i).getRating();
                        String idx = list.get(i).getIdx();

                        RatingBar ratingBar = new RatingBar(getContext());
                        if (rat.contains("회원")) {
                            ratingBar.setRating(5);
                        } else {
                            float rating = Float.valueOf(rat);
                            ratingBar.setRating(rating);
                        }
                        // 거리비교
                        Location myLocation = new Location("locationA");
                        myLocation.setLatitude(mLat);
                        myLocation.setLongitude(mLon);
                        Location startLocation = new Location("locationB");
                        startLocation.setLatitude(sLat);
                        startLocation.setLongitude(sLon);
                        double distance = myLocation.distanceTo(startLocation) * 0.001;
                        double dis = Math.round(distance * 10d) / 10d;

                        Location myLocationEnd = new Location("locationC");
                        myLocationEnd.setLatitude(END_POINT.latitude);
                        myLocationEnd.setLongitude(END_POINT.longitude);
                        Location endLocation = new Location("locationD");
                        endLocation.setLatitude(eLat);
                        endLocation.setLongitude(eLon);
                        double distance2 = myLocationEnd.distanceTo(endLocation) * 0.001;
                        double dis2 = Math.round(distance2 * 10d) / 10d;

                        if (sDistance == 0) {
                            // 좌표로 주소 받아오기
                            Geocoder geocoder = new Geocoder(getContext());
                            List<Address> add_start = null;
                            List<Address> add_end = null;
                            String startAddr = null;
                            String endAddr = null;

                            if (eDistance == 0) {
                                try {
                                    add_start = geocoder.getFromLocation(sLat, sLon, 1);
                                    add_end = geocoder.getFromLocation(eLat, eLon, 1);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (add_start.get(0).getSubLocality() == null) {
                                    startAddr = add_start.get(0).getThoroughfare();
                                } else {
                                    startAddr = add_start.get(0).getSubLocality() + " " + add_start.get(0).getThoroughfare();
                                }
                                if (add_end.get(0).getSubLocality() == null) {
                                    endAddr = add_end.get(0).getThoroughfare();
                                } else {
                                    endAddr = add_end.get(0).getSubLocality() + " " + add_end.get(0).getThoroughfare();
                                }
                                mListData.add(new requestItem("출발지까지 " + dis + "Km", people + " 인", "약 " + fare, startAddr, endAddr, String.valueOf(date), time, ratingBar, sLat, sLon, eLat, eLon, name, idx));
                                Log.d("start_ListData:::", "" + mListData);

                            } else if (eDistance >= dis2) { // 도착지 거리 조건 비교 { 1, 5, 10, 0}
                                try {
                                    add_start = geocoder.getFromLocation(sLat, sLon, 1);
                                    add_end = geocoder.getFromLocation(eLat, eLon, 1);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (add_start.get(0).getSubLocality() == null) {
                                    startAddr = add_start.get(0).getThoroughfare();
                                } else {
                                    startAddr = add_start.get(0).getSubLocality() + " " + add_start.get(0).getThoroughfare();
                                }
                                if (add_end.get(0).getSubLocality() == null) {
                                    endAddr = add_end.get(0).getThoroughfare();
                                } else {
                                    endAddr = add_end.get(0).getSubLocality() + " " + add_end.get(0).getThoroughfare();
                                }
                                mListData.add(new requestItem("출발지까지 " + dis + "Km", people + " 인", "약 " + fare, startAddr, endAddr, String.valueOf(date), time, ratingBar, sLat, sLon, eLat, eLon, name, idx));
                                Log.d("start_ListData:::", "" + mListData);

                            }
                        } else if (sDistance >= dis) { // 출발지 거리 조건 비교 { 1, 5, 10, 0}
                            // 좌표로 주소 받아오기
                            Geocoder geocoder = new Geocoder(getContext());
                            List<Address> add_start = null;
                            List<Address> add_end = null;
                            String startAddr = null;
                            String endAddr = null;

                            if (eDistance == 0) {
                                try {
                                    add_start = geocoder.getFromLocation(sLat, sLon, 1);
                                    add_end = geocoder.getFromLocation(eLat, eLon, 1);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (add_start.get(0).getSubLocality() == null) {
                                    startAddr = add_start.get(0).getThoroughfare();
                                } else {
                                    startAddr = add_start.get(0).getSubLocality() + " " + add_start.get(0).getThoroughfare();
                                }
                                if (add_end.get(0).getSubLocality() == null) {
                                    endAddr = add_end.get(0).getThoroughfare();
                                } else {
                                    endAddr = add_end.get(0).getSubLocality() + " " + add_end.get(0).getThoroughfare();
                                }
                                mListData.add(new requestItem("출발지까지 " + dis + "Km", people + " 인", "약 " + fare, startAddr, endAddr, String.valueOf(date), time, ratingBar, sLat, sLon, eLat, eLon, name, idx));
                                Log.d("start_ListData:::", "" + mListData);

                            } else if (eDistance >= dis2) { // 도착지 거리 조건 비교 { 1, 5, 10, 0}
                                try {
                                    add_start = geocoder.getFromLocation(sLat, sLon, 1);
                                    add_end = geocoder.getFromLocation(eLat, eLon, 1);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (add_start.get(0).getSubLocality() == null) {
                                    startAddr = add_start.get(0).getThoroughfare();
                                } else {
                                    startAddr = add_start.get(0).getSubLocality() + " " + add_start.get(0).getThoroughfare();
                                }
                                if (add_end.get(0).getSubLocality() == null) {
                                    endAddr = add_end.get(0).getThoroughfare();
                                } else {
                                    endAddr = add_end.get(0).getSubLocality() + " " + add_end.get(0).getThoroughfare();
                                }
                                mListData.add(new requestItem("출발지까지 " + dis + "Km", people + " 인", "약 " + fare, startAddr, endAddr, String.valueOf(date), time, ratingBar, sLat, sLon, eLat, eLon, name, idx));
                                Log.d("start_ListData:::", "" + mListData);
                            }

                        }
                    }
                    adapter.setData(mListData);
                    adapter.notifyDataSetChanged();
                    if (mListData == null || mListData.size() == 0) {
                        noList.setVisibility(View.VISIBLE);
                    } else {
                        request_rv.setVisibility(View.VISIBLE);
                        noList.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<requestParse>> call, Throwable t) {
                Toast.makeText(getContext(), "카풀 요청 실패", Toast.LENGTH_SHORT).show();
                Log.e("에러에러:::", "" + t.getMessage());
            }
        });
    }

    // 내 위치 불러오기
    private void getDeviceLocation() {
        Log.d("TAG", "내 위치");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        try {
            Task location = mFusedLocationClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful() && task.getResult() != null) {

                        Log.d("TAG", "성공");
                        currentLocation = (Location) task.getResult();

                        // 내 좌표 받아서 비교하고 띄워준다 리사이클러뷰
                        if (currentLocation != null && filter == null) {
                            Log.e("TAG", "기본설정 값");
                            performRequestDriver(type, START_POINT.latitude, START_POINT.longitude);
//                            performRequestDriver(type, currentLocation.getLatitude(), currentLocation.getLongitude(), sDate, eDate, setTime, people, sDistance, eDistance);
                        } else if (filter != null) {
                            Log.e("TAG", "필터적용 값");
                            performRequestFilter(type, START_POINT.latitude, START_POINT.longitude, sDate, eDate, setTime, people, sDistance, eDistance);
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

    public void request_set(String sD, String eD, String sT, int peo, int sDis, int eDis) {

        sDate = sD;
        eDate = eD;
        setTime = sT;
        people = peo;
        sDistance = sDis;
        eDistance = eDis;

        Log.e("시작날짜:::", "" + sDate);
        Log.e("종료날짜:::", "" + eDate);
        Log.e("시간:::", "" + setTime);
        Log.e("탑승인원:::", "" + people);
        Log.e("출발지와의 거리:::", "" + sDistance);
        Log.e("도착지와의 거리:::", "" + eDistance);

        performRequestFilter(type, currentLocation.getLatitude(), currentLocation.getLongitude(), sDate, eDate, setTime, people, sDistance, eDistance);
    }


    // 운전자 경로 불러오기
    private void myRouteLoad() {
        SharedPreferences preferences = getContext().getSharedPreferences("myRoute", Context.MODE_PRIVATE);

        String title = preferences.getString("title", "나의 경로");
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
        driver_txt.setText("출발 : [" + sPlace + "]");
    }

    // 요청리스트 아이템 클릭
    @Override
    public void requestItemClick(double sLat, double sLon, double eLat, double eLon, String date, String time, String people, String fare, RatingBar rating, String name, String idx) {
        Intent intent = new Intent(getContext(), RequestClick.class);
        intent.putExtra("sLat", sLat);
        intent.putExtra("sLon", sLon);
        intent.putExtra("eLat", eLat);
        intent.putExtra("eLon", eLon);
        intent.putExtra("date", date);
        intent.putExtra("time", time);
        intent.putExtra("people", people);
        intent.putExtra("fare", fare);
        intent.putExtra("rating", rating.getRating());
        intent.putExtra("name", name);
        intent.putExtra("idx", idx);
        startActivityForResult(intent, 1127);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            Log.e("setResult( RESULT_OK )", "리절트오케");
//            Toast.makeText(getContext(), "등록을 취소 하였습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (requestCode == 1127) {
            Log.e("setResult( 1127):::", "탑승자가 요청을 취소해서 돌아옴");

        }
    }

    @Override
    public void onRefresh() {
        this.request_rv.postDelayed(new Runnable() {
            @Override
            public void run() {
                performRequestFilter(type, START_POINT.latitude, START_POINT.longitude, sDate, eDate, setTime, people, sDistance, eDistance);
                Snackbar.make(request_rv,"새로고침완료",Snackbar.LENGTH_SHORT).show();
                refreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

    private void setRefresh(){
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_blue_dark,
                android.R.color.holo_green_dark,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_dark

        );
    }

}
