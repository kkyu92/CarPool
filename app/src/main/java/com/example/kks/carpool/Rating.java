package com.example.kks.carpool;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kks.carpool.LoginSignup.Login;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.kks.carpool.LoginSignup.Result.USER_NAME;
import static com.example.kks.carpool.LoginSignup.Result.USER_PROFILE;

public class Rating extends AppCompatActivity {

    // getIntent
    private String TARGET_ID, sPlace, ePlace, dateTime, distanceTime, fare, flag;

    // 레이아웃
    private TextView startText, endText, fareText, question1, question2, question3;
    private RadioGroup q1_Radio, q2_Radio, q3_Radio;
    private Button ratingFinish;

    // 점수계산
    private double point1, point2, point3 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        // getIntent
        Intent intent = getIntent();
        TARGET_ID = intent.getStringExtra("target");
        sPlace = intent.getStringExtra("start_place");
        ePlace = intent.getStringExtra("end_place");
        dateTime = intent.getStringExtra("date_time");
        distanceTime = intent.getStringExtra("distance_time");
        fare = intent.getStringExtra("fare");
        flag = intent.getStringExtra("flag");

        // 레이아웃
        startText = findViewById(R.id.start_P);
        endText = findViewById(R.id.end_P);
        fareText = findViewById(R.id.fareText);
        question1 = findViewById(R.id.question1);
        question2 = findViewById(R.id.question2);
        question3 = findViewById(R.id.question3);
        q1_Radio = findViewById(R.id.rating1);
        q2_Radio = findViewById(R.id.rating2);
        q3_Radio = findViewById(R.id.rating3);
        ratingFinish = findViewById(R.id.rating_finish);

        startText.setText(sPlace);
        endText.setText(ePlace);
        fareText.setText(fare);

        if (flag.equals("탑승자")) { // 탑승자일 때 질문
            question1.setText("운전자의 카풀매너는 어떠셨어요?");
            question2.setText("차량상태는 어떠셨어요?");
            question3.setText("약속시간과 장소는 잘 지켰나요?");
        } else { // 운전자일 때 질문
            question1.setText("탑승자의 카풀매너는 어떠셨어요?");
            question2.setText("차량을 깨끗히 이용했나요?");
            question3.setText("약속시간과 장소는 잘 지켰나요?");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 질문1번 라디오 그룹
        q1_Radio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkID) {
                switch (checkID) {
                    case R.id.Q1_point1:
                        point1 = 1.0;
                        break;
                    case R.id.Q1_point2:
                        point1 = 2.0;
                        break;
                    case R.id.Q1_point3:
                        point1 = 3.0;
                        break;
                    case R.id.Q1_point4:
                        point1 = 4.0;
                        break;
                    case R.id.Q1_point5:
                        point1 = 5.0;
                        break;
                }
                if (point1 != 0 && point2 != 0 && point3 != 0) {
                    ratingFinish.setBackgroundColor(R.color.colorPrimaryDark);
                }
            }
        });

        // 질문2번 라디오 그룹
        q2_Radio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkID) {
                switch (checkID) {
                    case R.id.Q2_point1:
                        point2 = 1.0;
                        break;
                    case R.id.Q2_point2:
                        point2 = 2.0;
                        break;
                    case R.id.Q2_point3:
                        point2 = 3.0;
                        break;
                    case R.id.Q2_point4:
                        point2 = 4.0;
                        break;
                    case R.id.Q2_point5:
                        point2 = 5.0;
                        break;
                }
                if (point1 != 0 && point2 != 0 && point3 != 0) {
                    ratingFinish.setBackgroundColor(R.color.colorPrimaryDark);
                }
            }
        });

        // 질문3번 라디오 그룹
        q3_Radio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkID) {
                switch (checkID) {
                    case R.id.Q3_point1:
                        point3 = 1.0;
                        break;
                    case R.id.Q3_point2:
                        point3 = 2.0;
                        break;
                    case R.id.Q3_point3:
                        point3 = 3.0;
                        break;
                    case R.id.Q1_point4:
                        point3 = 4.0;
                        break;
                    case R.id.Q3_point5:
                        point3 = 5.0;
                        break;
                }
                if (point1 != 0 && point2 != 0 && point3 != 0) {
                    ratingFinish.setBackgroundColor(R.color.colorPrimaryDark);
                }
            }
        });

        // 평가완료
        ratingFinish.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                if (point1 == 0 || point2 == 0 || point3 == 0) {
                    Toast.makeText(Rating.this, "평가를 완료해 주세요!", Toast.LENGTH_SHORT).show();
                } else {
                    double allPoint = (point1 + point2 + point3) / 3;
                    double point = Double.parseDouble(String.format("%.1f", allPoint));
                    Log.e("평가한 점수 :::: ", ""+point);
                    String time = distanceTime.replace("소요시간 : ", "");
                    // 데이터베이스 등록
                    Log.e("유저아이디 :::: ", ""+TARGET_ID);
                    Log.e("출발장소 :::: ", ""+sPlace);
                    Log.e("도착장소 :::: ", ""+ePlace);
                    Log.e("출발날짜 시간 :::: ", ""+dateTime);
                    Log.e("소요시간 :::: ", ""+time);
                    Log.e("평가한 점수 :::: ", ""+point);
                    Log.e("요금 :::: ", ""+fare);
                    Log.e("평가한 아이디 :::: ", ""+USER_NAME);
                    Log.e("평가한 프로필 :::: ", ""+USER_PROFILE);

                    addRating(TARGET_ID, sPlace, ePlace, dateTime, time, point, fare, USER_NAME, USER_PROFILE, point1, point2, point3);
                    Toast.makeText(Rating.this, "평가를 완료했습니다.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }

    private void addRating(String name, String start, String end, String datetime, String distancetime, double rating, String fare, String username, String profile, double rating1, double rating2, double rating3) {
        Call<Integer> call = Login.apiInterface.performAddRating(name, start, end, datetime, distancetime, rating, fare, username, profile, rating1, rating2, rating3);

        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.body() == 1) {
                    Log.e("평가등록 성공", "성공");
                } else {
                    Log.e("평가등록 실패", "실패");
                }
                Log.e("평가등록 성공", "인덱스 값 : " + response.body());
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
//                Toast.makeText(Rating.this, "평가등록 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
