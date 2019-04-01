package com.example.kks.carpool;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.kks.carpool.driver.DrivingAdapter;
import com.example.kks.carpool.model.DrivingItem;
import com.example.kks.carpool.model.ShowRating;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.kks.carpool.Result.USER_NAME;

public class RiderCarpoolList extends AppCompatActivity {

    // recyclerView
    private RecyclerView request_rv;
    private DrivingAdapter adapter;
    private ArrayList<DrivingItem> mListData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_carpool_list);

        request_rv = findViewById(R.id.recyclerView_rider);

        performShowDriveList();
    }

    private void performShowDriveList() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new DrivingAdapter();
        mListData = new ArrayList<>();
        request_rv.setLayoutManager(layoutManager);
        request_rv.setAdapter(adapter);
//        adapter.setCallback(this);

        // 나의 운행내역 불러오기
        Call<ArrayList<ShowRating>> call = Login.apiInterface.performShowRating(USER_NAME);

        call.enqueue(new Callback<ArrayList<ShowRating>>() {
            @Override
            public void onResponse(Call<ArrayList<ShowRating>> call, Response<ArrayList<ShowRating>> response) {

                ArrayList<ShowRating> list = response.body();

                if (list != null) {

                    for (int i = 0; i < list.size(); i++) {
                        String name = list.get(i).getUser_name();
                        String targetID = list.get(i).getTarget_id();
                        String targetProfile = list.get(i).getTarget_profile();
                        String startPlace = list.get(i).getStart_place();
                        String endPlace = list.get(i).getEnd_place();
                        String dateTime = list.get(i).getDate_time();
                        String distanceTime = list.get(i).getDistance_time();
                        String rating = list.get(i).getRating();
                        String fare = list.get(i).getFare();
                        String idx = list.get(i).getIdx();

                        mListData.add(new DrivingItem(name, targetID, targetProfile, startPlace, endPlace, dateTime, distanceTime, rating, idx, fare));
                    }
                    adapter.setData(mListData);
                    adapter.notifyDataSetChanged();
                    if (mListData == null || mListData.size() == 0) {
//                        noList.setVisibility(View.VISIBLE);
                    } else {
                        request_rv.setVisibility(View.VISIBLE);
//                        noList.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<ShowRating>> call, Throwable t) {
                Toast.makeText(RiderCarpoolList.this, "운행리스트 불러오기 실패", Toast.LENGTH_SHORT).show();
                Log.e("에러에러:::", "" + t.getMessage());
            }
        });
    }
}
