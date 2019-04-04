package com.example.kks.carpool.DriverClass;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kks.carpool.LoginSignup.Login;
import com.example.kks.carpool.R;
import com.example.kks.carpool.model.MyRoute;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.kks.carpool.LoginSignup.Result.USER_NAME;

public class MyRouteListPopUp extends Activity implements DriverMyRouteCallback {

    // 리사이클러뷰
    private RecyclerView myRoute_rv;
    private DriverMyRouteAdapter adapter;
    private ArrayList<MyRoute> mListData;
    private TextView noList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_my_route_list_pop_up);

        myRoute_rv = findViewById(R.id.myRoute_rv);
        noList = findViewById(R.id.noRoute);

        performGetMyRoute(USER_NAME);
    }

    // 나의 경로 받아오기
    private void performGetMyRoute(final String name) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new DriverMyRouteAdapter();
        mListData = new ArrayList<>();
        myRoute_rv.setLayoutManager(layoutManager);
        myRoute_rv.setAdapter(adapter);
        adapter.setCallback(this);

        Call<ArrayList<MyRoute>> call = Login.apiInterface.performGetMyRoute(name);

        call.enqueue(new Callback<ArrayList<MyRoute>>() {
            @Override
            public void onResponse(Call<ArrayList<MyRoute>> call, Response<ArrayList<MyRoute>> response) {

                ArrayList<MyRoute> list = response.body();

                if (list != null) {

                    for (int i = 0; i < list.size(); i++) {
                        String title = list.get(i).getTitle();
                        String s_route = list.get(i).getsPlace();
                        String e_route = list.get(i).getePlace();
                        double sLat = list.get(i).getsLat();
                        double sLon = list.get(i).getsLon();
                        double eLat = list.get(i).geteLat();
                        double eLon = list.get(i).geteLon();
                        int idx = list.get(i).getIdx();
                        String map = list.get(i).getgMap();

                        mListData.add(new MyRoute(name, title, s_route, e_route, sLat, sLon, eLat, eLon, idx, map));
                        Log.d("나의 경로 등록 정보:::", "" + mListData);
                    }
                    adapter.setData(mListData);
                    adapter.notifyDataSetChanged();
                    if (mListData == null || mListData.size() == 0) {
                        noList.setVisibility(View.VISIBLE);
                    } else {
                        myRoute_rv.setVisibility(View.VISIBLE);
                        noList.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<MyRoute>> call, Throwable t) {
                Toast.makeText(MyRouteListPopUp.this, "카풀 요청 실패", Toast.LENGTH_SHORT).show();
                Log.e("에러에러:::", "" + t.getMessage());
            }
        });
    }

    // 나의 경로 아이템 클릭 (토스트 메시지)
    @Override
    public void showToast(String place) {
        Toast.makeText(this, "나의 경로를 [" + place + "]으로 설정했습니다.", Toast.LENGTH_LONG).show();
    }

    // 나의 경로 아이템 클릭 (출발지 도착지 좌표, 장소명 가져온다)
    @Override
    public void myRouteClick(String title, String sPlace, String ePlace, double sLat, double sLon, double eLat, double eLon) {
        Log.e("나의 경로 리스트 팝업::","인텐트로 셋리절트 시작 부분");
        Intent intent = new Intent();
        intent.putExtra("title", title);
        intent.putExtra("startP", sPlace);
        intent.putExtra("endP", ePlace);
        intent.putExtra("sLat", sLat);
        intent.putExtra("sLon", sLon);
        intent.putExtra("eLat", eLat);
        intent.putExtra("eLon", eLon);
        setResult(RESULT_OK, intent);
        finish();
    }

    // 나의 경로 수정
    @Override
    public void myRouteEdit(String title, String sPlace, String ePlace, double sLat, double sLon, double eLat, double eLon, int idx) {
        Log.e("나의 경로 리스트 팝업::","인텐트로 셋리절트 시작 부분");
        Intent intent = new Intent();
        intent.putExtra("edit", 1);
        intent.putExtra("title", title);
        intent.putExtra("startP", sPlace);
        intent.putExtra("endP", ePlace);
        intent.putExtra("sLat", sLat);
        intent.putExtra("sLon", sLon);
        intent.putExtra("eLat", eLat);
        intent.putExtra("eLon", eLon);
        intent.putExtra("idx", idx);
        setResult(RESULT_OK, intent);
        finish();
    }

    // 나의 경로 삭제
    @Override
    public void myRouteDel(final String title, int idx) {
        Log.e("나의경로 팝업:::","~~"+idx);
        Call<MyRoute> call = Login.apiInterface.performInsertMyRouteDel(idx);

        call.enqueue(new Callback<MyRoute>() {
            @Override
            public void onResponse(Call<MyRoute> call, Response<MyRoute> response) {
                Log.e("나의 경로삭제:::", "성공");
                Toast.makeText(MyRouteListPopUp.this, "경로 ["+title+"] 삭제 하였습니다.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<MyRoute> call, Throwable t) {
                Log.e("나의 경로삭제 실패:::", "" + t.getMessage());
            }
        });
        performGetMyRoute(USER_NAME);
    }

}
