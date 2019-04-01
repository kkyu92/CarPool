package com.example.kks.carpool;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kks.carpool.autosearch.Poi;
import com.example.kks.carpool.autosearch.TMapSearchInfo;
import com.example.kks.carpool.autosearch.TmapRoute;
import com.example.kks.carpool.retro.ApiInterface;
import com.example.kks.carpool.retro.TmapClient;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.example.kks.carpool.AutoCompleteParse.TMAP_API_KEY;

public class TmapStart extends AppCompatActivity {

    // Tmap
    private TMapView tMapView;
    LinearLayout tMap;

    // Retrofit Tmap 거리 요금 시간
    private ApiInterface apiInterface;
    private ConstraintLayout constraintLayout;
    private TextView total_time, total_distance, total_fare, total_taxi_fare;
    private Button time_set, people_count, request_car;

    // 위치 좌표
    double latitude;
    double longitude;
    public static String lat, lon, address;
    // 현위치 잡아주는 handler
    private Handler handler;

    // setResult
    double lat_start, lon_start, lat_end, lon_end;

    // 출발, 도착
    private Button start_btn, end_btn;
    private ImageButton mLocation;

    // 계속 현재 위치로 포커스 잡는 문제
    int start_focus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tmap_start);

        handler = new Handler();
        constraintLayout = findViewById(R.id.carpool_start);
        total_distance = findViewById(R.id.total_distance);
        total_time = findViewById(R.id.total_time);
        total_taxi_fare = findViewById(R.id.total_taxi_fare);
        total_fare = findViewById(R.id.total_fare);
        time_set = findViewById(R.id.time_set);
        people_count = findViewById(R.id.people_count);
        request_car = findViewById(R.id.request_carpool);

        tMap = findViewById(R.id.tMapView);
        tMapView = new TMapView(this);
        tMapView.setSKTMapApiKey("e01eeb5d-4c77-47c3-abe4-57a995dc41ce");
        tMap.addView(tMapView);

        tMapView.setIconVisibility(true);//현재위치로 표시될 아이콘을 표시할지 여부를 설정합니다.

        start_btn = findViewById(R.id.start_point);
        end_btn = findViewById(R.id.end_point);
        mLocation = findViewById(R.id.my_located);

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{ACCESS_FINE_LOCATION}, 1); //위치권한 탐색 허용 관련 내용
            }
            return;
        }
        setGps();

    }

    protected void onResume() {
        super.onResume();

        // 현위치 handler
//        startRepeating();
//        if (start_focus == 6) {
//            stopRepeating();
//        }

        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TmapStart.this, MainActivity.class);
//                intent.putExtra("Lat", latitude);
//                intent.putExtra("Lon", longitude);
                intent.putExtra("btn", "출발지");
                intent.putExtra("now", address);
                startActivityForResult(intent, 111);
            }
        });

        end_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TmapStart.this, MainActivity.class);
//                intent.putExtra("Lat", latitude);
//                intent.putExtra("Lon", longitude);
                intent.putExtra("btn", "도착지");
                intent.putExtra("now", address);
                startActivityForResult(intent, 112);
            }
        });

        // 내 위치로
        mLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tMapView.setLocationPoint(longitude, latitude);
                tMapView.setCenterPoint(longitude, latitude);
                tMapView.setZoomLevel(15);
            }
        });

        // 카풀 출발 시간 설정 (스피너 사용)
        time_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // 인원수 설정
        people_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // 카풀 요청하기
        request_car.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("ggg", "lon:::"+tMapView.getCenterPoint());
                Log.e("ggg", "lat:::"+tMapView.getCenterPointY());
            }
        });
    }

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            //현재위치의 좌표를 알수있는 부분
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                // static 좌표
                lat = String.valueOf(latitude);
                lon = String.valueOf(longitude);
                tMapView.setLocationPoint(longitude, latitude);
                // 처음 시작할 때만 포커스 잡아라
                Log.d("조건 확인 부분:::", "" + start_focus);
                if (start_focus < 1) {
                    tMapView.setCenterPoint(longitude, latitude);
                }
                Log.d("test", longitude + "," + latitude);
                Log.d("zoomlevel:::", "" + tMapView.getZoomLevel());

                TMapData tMapData = new TMapData();
                TMapPoint myPoint = new TMapPoint(latitude, longitude);

                try {

                    Log.d("TmapTest", "" + myPoint.getLatitude());
                    Log.d("TmapTest", "" + myPoint.getLongitude());

                    tMapData.convertGpsToAddress(myPoint.getLatitude(), myPoint.getLongitude(), new TMapData.ConvertGPSToAddressListenerCallback() {

                        @Override
                        public void onConvertToGPSToAddress(String addr) {
                            Log.d("TmapTest", "*** updatePositionInfo - addr: " + addr);
                            start_btn.setHint("현위치 : " + addr);
                            address = addr;
                        }
                    });

                } catch (Exception e) {
                    Log.d("error", "*** Exception: " + e.getLocalizedMessage());
                    e.printStackTrace();
                }

            }
            start_focus++;
            Log.d("증가한 값:::", "" + start_focus);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    public void setGps() {
        final LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}, 1);
        }
        assert lm != null;
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자(실내에선 NETWORK_PROVIDER 권장)
                1000, // 통지사이의 최소 시간간격 (miliSecond)
                (float) 0.1, // 통지사이의 최소 변경거리 (m)
                mLocationListener);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "취소 하였습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (requestCode == 111) {                                                  //--------------------------------------------------- 출발지 버튼 클릭으로 갔다옴

            start_btn.setVisibility(View.GONE);
            end_btn.setVisibility(View.GONE);
            mLocation.setVisibility(View.GONE);
            constraintLayout.setVisibility(View.VISIBLE);

            lat_start = data.getDoubleExtra("lat_start", 0);
            lon_start = data.getDoubleExtra("lon_start", 0);
            lat_end = data.getDoubleExtra("lat_end", 0);
            lon_end = data.getDoubleExtra("lon_end", 0);

            TMapMarkerItem startMarker = new TMapMarkerItem(); // 시작 마커 생성
            TMapMarkerItem endMarker = new TMapMarkerItem(); // 도착 마커 생성

            TMapPoint startPoint = new TMapPoint(lat_start, lon_start); // 시작 마커 좌표
            TMapPoint endPoint = new TMapPoint(lat_end, lon_end); // 도착 마커 좌표

            // 마커 아이콘 설정
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            // 시작 마커에 넣을 이미지
            Bitmap startBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.color_button, options);
            // 시작 마커 설정
            startMarker.setIcon(startBitmap); // 마커 아이콘 지정
            startMarker.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
            startMarker.setTMapPoint(startPoint); // 마커의 좌표 지정
            startMarker.setName("시작점"); // 마커의 타이틀 지정
            tMapView.addMarkerItem("startMarker", startMarker); // 지도에 마커 추가

            // 도착 마커에 넣을 이미지
            Bitmap endBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.gray_edit_text);
            // 도착 마커 설정
            endMarker.setIcon(endBitmap); // 마커 아이콘 지정
            endMarker.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
            endMarker.setTMapPoint(endPoint); // 마커의 좌표 지정
            endMarker.setName("도착점"); // 마커의 타이틀 지정
            tMapView.addMarkerItem("endMarker", endMarker); // 지도에 마커 추가

            tMapView.setCenterPoint((lon_start + lon_end) / 2, (lat_start + lat_end) / 2 - 0.038);
            tMapView.setZoomLevel(12);
            Log.e("ggg", "lon:::"+tMapView.getCenterPoint());
            Log.e("ggg", "lat:::"+tMapView.getCenterPointY());

            // Retrofit을 사용해 거리, 소요시간, 요금 정보
            apiInterface = TmapClient.getApiClient().create(ApiInterface.class);
            Call<TmapRoute> call = apiInterface.performTmapInfo("1", "application/json", TMAP_API_KEY, String.valueOf(lon_end), String.valueOf(lat_end), String.valueOf(lon_start), String.valueOf(lat_start), "2");
//        Log.e("encoded::::", ""+encodeWord);
//            Log.e("!encoded::::", "" + word);
            call.enqueue(new Callback<TmapRoute>() {
                @Override
                public void onResponse(Call<TmapRoute> call, Response<TmapRoute> response) {
                    Log.d("레트로핏::::", "성공" + response.body());
                    Log.d("레트로핏::::", "성공" + call);
//                mListData.clear();
                    TmapRoute tmapRoute = response.body();

                    if (tmapRoute != null) {
                        ArrayList<TmapRoute.Features> features = tmapRoute.getFeatures();
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

                        double t_time = Integer.valueOf(totalTime)/60;
                        long time = Math.round(t_time);
                        String meter = totalDistance.substring(totalDistance.length()-3, totalDistance.length());
                        String km = totalDistance.substring(0,totalDistance.length()-3);
                        double distance = Double.parseDouble(km+"."+meter);
                        double dis = Math.round(distance*100d) / 100d;

                        if (taxiFare.length() > 2) {
                            String beakWon = taxiFare.substring(taxiFare.length()-3, taxiFare.length());
                            String man = taxiFare.substring(0, taxiFare.length()-3);
                            String won = man+","+beakWon;
                            total_taxi_fare.setText("이용요금 : "+won+"원");
                        } else {
                            total_taxi_fare.setText("이용요금 : "+taxiFare+"원");
                        }

                        total_distance.setText("총 거리 : "+dis+"km");
                        total_time.setText("소요시간 : 약 "+String.valueOf(time)+"분");
                        total_fare.setText("통행료 : "+totalFare+"원");

                        if (!km.equals("")) {
                            if (Integer.valueOf(km) > 30) {
                                tMapView.setZoomLevel(10);
                                tMapView.setCenterPoint((lon_start + lon_end) / 2, (lat_start + lat_end) / 2 - 0.138);
                            } else if (Integer.valueOf(km) > 10) {
                                tMapView.setZoomLevel(11);
                            }
                        }

                    }
                }

                @Override
                public void onFailure(Call<TmapRoute> call, Throwable t) {
                    Log.d("레트로핏::::", "실패");
                }
            });

            Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.e("start_lat", "" + lat_start);
                    Log.e("start_lon", "" + lon_start);
                    Log.e("end_lat", "" + lat_end);
                    Log.e("end_lon", "" + lon_end);

                    TMapPoint startPoint = new TMapPoint(lat_start, lon_start);
                    TMapPoint endPoint = new TMapPoint(lat_end, lon_end);

                    try {
                        TMapPolyLine tMapPolyLine = new TMapData().findPathData(startPoint, endPoint);
                        tMapPolyLine.setLineColor(Color.CYAN);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    tMapPolyLine.setLineColor(getColor(R.color.colorPrimaryDark));
//                }
                        tMapPolyLine.setLineWidth(2);
                        tMapView.addTMapPolyLine("Line1", tMapPolyLine);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            th.start();

        } else if (requestCode == 112) {                                                  //--------------------------------------------------- 도착지 버튼 클릭으로 갔다옴

            start_btn.setVisibility(View.GONE);
            end_btn.setVisibility(View.GONE);
            mLocation.setVisibility(View.GONE);
            constraintLayout.setVisibility(View.VISIBLE);

            lat_start = data.getDoubleExtra("lat_start", 0);
            lon_start = data.getDoubleExtra("lon_start", 0);
            lat_end = data.getDoubleExtra("lat_end", 0);
            lon_end = data.getDoubleExtra("lon_end", 0);

            TMapMarkerItem startMarker = new TMapMarkerItem(); // 시작 마커 생성
            TMapMarkerItem endMarker = new TMapMarkerItem(); // 도착 마커 생성

            TMapPoint startPoint = new TMapPoint(lat_start, lon_start); // 시작 마커 좌표
            TMapPoint endPoint = new TMapPoint(lat_end, lon_end); // 도착 마커 좌표

            // 마커 아이콘 설정
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            // 시작 마커에 넣을 이미지
            Bitmap startBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.color_button, options);
            // 시작 마커 설정
            startMarker.setIcon(startBitmap); // 마커 아이콘 지정
            startMarker.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
            startMarker.setTMapPoint(startPoint); // 마커의 좌표 지정
            startMarker.setName("시작점"); // 마커의 타이틀 지정
            tMapView.addMarkerItem("startMarker", startMarker); // 지도에 마커 추가

            // 도착 마커에 넣을 이미지
            Bitmap endBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.gray_edit_text);
            // 도착 마커 설정
            endMarker.setIcon(endBitmap); // 마커 아이콘 지정
            endMarker.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
            endMarker.setTMapPoint(endPoint); // 마커의 좌표 지정
            endMarker.setName("도착점"); // 마커의 타이틀 지정
            tMapView.addMarkerItem("endMarker", endMarker); // 지도에 마커 추가

            tMapView.setCenterPoint((lon_start + lon_end) / 2, (lat_start + lat_end) / 2 - 0.038);
            tMapView.setZoomLevel(12);


            // Retrofit을 사용해 거리, 소요시간, 요금 정보
            apiInterface = TmapClient.getApiClient().create(ApiInterface.class);
            Call<TmapRoute> call = apiInterface.performTmapInfo("1", "application/json", TMAP_API_KEY, String.valueOf(lon_end), String.valueOf(lat_end), String.valueOf(lon_start), String.valueOf(lat_start), "2");
//        Log.e("encoded::::", ""+encodeWord);
//            Log.e("!encoded::::", "" + word);
            call.enqueue(new Callback<TmapRoute>() {
                @Override
                public void onResponse(Call<TmapRoute> call, Response<TmapRoute> response) {
                    Log.d("레트로핏::::", "성공" + response.body());
                    Log.d("레트로핏::::", "성공" + call);
//                mListData.clear();
                    TmapRoute tmapRoute = response.body();

                    if (tmapRoute != null) {
                        ArrayList<TmapRoute.Features> features = tmapRoute.getFeatures();
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

                        double t_time = Integer.valueOf(totalTime)/60;
                        long time = Math.round(t_time);
                        String meter = totalDistance.substring(totalDistance.length()-3, totalDistance.length());
                        String km = totalDistance.substring(0,totalDistance.length()-3);
                        double distance = Double.parseDouble(km+"."+meter);
                        double dis = Math.round(distance*100d) / 100d;

                        if (taxiFare.length() > 2) {
                            String beakWon = taxiFare.substring(taxiFare.length()-3, taxiFare.length());
                            String man = taxiFare.substring(0, taxiFare.length()-3);
                            String won = man+","+beakWon;
                            total_taxi_fare.setText("이용요금 : "+won+"원");
                        } else {
                            total_taxi_fare.setText("이용요금 : "+taxiFare+"원");
                        }

                        total_distance.setText("총 거리 : "+dis+"km");
                        total_time.setText("소요시간 : 약 "+String.valueOf(time)+"분");
                        total_fare.setText("통행료 : "+totalFare+"원");

                        if (!km.equals("")) {
                            if (Integer.valueOf(km) > 30) {
                                tMapView.setZoomLevel(10);
                                tMapView.setCenterPoint((lon_start + lon_end) / 2, (lat_start + lat_end) / 2 - 0.138);
                            } else if (Integer.valueOf(km) > 10) {
                                tMapView.setZoomLevel(11);
                                tMapView.setCenterPoint((lon_start + lon_end) / 2, (lat_start + lat_end) / 2 - 0.068);
                            }
                        }

                    }
                }

                @Override
                public void onFailure(Call<TmapRoute> call, Throwable t) {
                    Log.d("레트로핏::::", "실패");
                }
            });

            Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.e("start_lat", "" + lat_start);
                    Log.e("start_lon", "" + lon_start);
                    Log.e("end_lat", "" + lat_end);
                    Log.e("end_lon", "" + lon_end);

                    TMapPoint startPoint = new TMapPoint(lat_start, lon_start);
                    TMapPoint endPoint = new TMapPoint(lat_end, lon_end);

                    try {
                        TMapPolyLine tMapPolyLine = new TMapData().findPathData(startPoint, endPoint);
                        tMapPolyLine.setLineColor(Color.CYAN);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    tMapPolyLine.setLineColor(getColor(R.color.colorPrimaryDark));
//                }
                        tMapPolyLine.setLineWidth(2);
                        tMapView.addTMapPolyLine("Line1", tMapPolyLine);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            th.start();
        }
    }

//    public void startRepeating() {
//        //mHandler.postDelayed(mChangeRunnable, 5000);
//        mChangeRunnable.run();
//    }
//
//    public void stopRepeating() {
//        handler.removeCallbacks(mChangeRunnable);
//    }
//
//    private Runnable mChangeRunnable = new Runnable() {
//
//        @Override
//        public void run() {
//            if (start_focus == 0) {
//                start_btn.setHint("현위치 : " + address);
//                handler.postDelayed(this, 5000);
//            } else if (start_focus == 1) {
//                start_btn.setHint("현위치 : " + address);
//                handler.postDelayed(this, 5000);
//            } else if (start_focus == 2) {
//                start_btn.setHint("현위치 : " + address);
//                handler.postDelayed(this, 5000);
//            } else if (start_focus == 3) {
//                start_btn.setHint("현위치 : " + address);
//                handler.postDelayed(this, 5000);
//            } else if (start_focus == 4) {
//                start_btn.setHint("현위치 : " + address);
//                handler.postDelayed(this, 5000);
//            } else {
//                start_btn.setHint("출발지를 입력하세요");
//                handler.postDelayed(this, 5000);
//            }
//        }
//    };

}

