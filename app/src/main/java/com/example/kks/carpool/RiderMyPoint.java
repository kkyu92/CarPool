package com.example.kks.carpool;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.kks.carpool.Result.USER_NAME;
import static com.example.kks.carpool.Result.USER_PROFILE;

public class RiderMyPoint extends AppCompatActivity {

    // 레이아웃
    private TextView userName, ratingAll, useCarpool, rating1, rating2, rating3;
    private ImageView userProfile, rating1_img, rating2_img, rating3_img;

    // 평점
    private String avg1, avg2, avg3, avg, using;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_my_point);

        userName = findViewById(R.id.user);
        userProfile = findViewById(R.id.userProfile);
        ratingAll = findViewById(R.id.rating_all);
        useCarpool = findViewById(R.id.useCarpool);
        rating1_img = findViewById(R.id.question1_img);
        rating1 = findViewById(R.id.question1_txt);
        rating2_img = findViewById(R.id.question2_img);
        rating2 = findViewById(R.id.question2_txt);
        rating3_img = findViewById(R.id.question3_img);
        rating3 = findViewById(R.id.question3_txt);

        getMyPoint();

        userName.setText(USER_NAME);
        Glide.with(this).load(USER_PROFILE).apply(new RequestOptions().circleCrop()).into(userProfile);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void getMyPoint() {
        Call<String> call = Login.apiInterface.performGetMyPoint(USER_NAME);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.e("점수 불러오기 :::", "받아온값 잘라쓰기 : " + response.body());
                if (response.body() != null) {
                    String getString = response.body();
                    String[] filt = getString.split("@");
                    avg1 = filt[0];
                    avg2 = filt[1];
                    avg3 = filt[2];
                    avg = filt[3];
                    using = filt[4];
                    Log.e("점수 불러오기 성공", "성공" + getString);

                    ratingAll.setText(avg);
                    useCarpool.setText("총 이용횟수 : " + using);
                    rating1.setText(avg1);
                    rating2.setText(avg2);
                    rating3.setText(avg3);

                    double rat1 = Double.parseDouble(avg1);
                    double rat2 = Double.parseDouble(avg2);
                    double rat3 = Double.parseDouble(avg3);

                    Log.e("double::", " " + rat1);
                    Log.e("double::", " " + rat2);
                    Log.e("double::", " " + rat3);

                    if (Double.compare(5.0, rat1) < 1.0) { // 5점대
                        rating1_img.setImageResource(R.drawable.point5_color);
                    } else if (Double.compare(4.0, rat1) < 1.0) { // 4점대
                        rating1_img.setImageResource(R.drawable.point4_color);
                    } else if (Double.compare(3.0, rat1) < 1.0) { // 3점대
                        rating1_img.setImageResource(R.drawable.point3_color);
                    } else if (Double.compare(2.0, rat1) < 1.0) { // 2점대
                        rating1_img.setImageResource(R.drawable.point2_color);
                    } else if (Double.compare(1.0, rat1) < 1.0) { // 1점대
                        rating1_img.setImageResource(R.drawable.point1_color);
                    }

                    if (Double.compare(5.0, rat2) < 1.0) { // 5점대
                        rating2_img.setImageResource(R.drawable.point5_color);
                    } else if (Double.compare(4.0, rat2) < 1.0) { // 4점대
                        rating2_img.setImageResource(R.drawable.point4_color);
                    } else if (Double.compare(3.0, rat2) < 1.0) { // 3점대
                        rating2_img.setImageResource(R.drawable.point3_color);
                    } else if (Double.compare(2.0, rat2) < 1.0) { // 2점대
                        rating2_img.setImageResource(R.drawable.point2_color);
                    } else if (Double.compare(1.0, rat2) < 1.0) { // 1점대
                        rating2_img.setImageResource(R.drawable.point1_color);
                    }

                    if (Double.compare(5.0, rat3) < 1.0) { // 5점대
                        rating3_img.setImageResource(R.drawable.point5_color);
                    } else if (Double.compare(4.0, rat3) < 1.0) { // 4점대
                        rating3_img.setImageResource(R.drawable.point4_color);
                    } else if (Double.compare(3.0, rat3) < 1.0) { // 3점대
                        rating3_img.setImageResource(R.drawable.point3_color);
                    } else if (Double.compare(2.0, rat3) < 1.0) { // 2점대
                        rating3_img.setImageResource(R.drawable.point2_color);
                    } else if (Double.compare(1.0, rat3) < 1.0) { // 1점대
                        rating3_img.setImageResource(R.drawable.point1_color);
                    }
                } else {
                    Log.e("점수 불러오기 실패", "실패");
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
//                Toast.makeText(Rating.this, "평가등록 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
