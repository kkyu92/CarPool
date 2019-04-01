package com.example.kks.carpool.driver;

import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.kks.carpool.Login;
import com.example.kks.carpool.R;
import com.example.kks.carpool.model.DrivingItem;
import com.example.kks.carpool.model.ShowRating;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.kks.carpool.Result.USER_NAME;

public class FragmentDriveList extends Fragment {

    private View view;

    // recyclerView
    private RecyclerView request_rv;
    private DrivingAdapter adapter;
    private ArrayList<DrivingItem> mListData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_drive_list, container, false);

        request_rv = view.findViewById(R.id.recyclerView);
        performShowDriveList();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void performShowDriveList() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
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
                Toast.makeText(getContext(), "운행리스트 불러오기 실패", Toast.LENGTH_SHORT).show();
                Log.e("에러에러:::", "" + t.getMessage());
            }
        });
    }
}
