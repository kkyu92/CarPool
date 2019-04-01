package com.example.kks.carpool;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class PopUpRequestInfo extends Activity {

    // 레이아웃
    private TextView start_txt, end_txt, time_txt, dis_txt, fare_txt, people_txt;
    private ImageButton cancel_btn;

    // getIntent
    private String start;
    private String end;
    private String time;
    private String dis;
    private String fare;
    private int people;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_pop_up_request_info);

        start_txt = findViewById(R.id.start);
        end_txt = findViewById(R.id.end);
        time_txt = findViewById(R.id.time);
        dis_txt = findViewById(R.id.distance);
        fare_txt = findViewById(R.id.fare);
        people_txt = findViewById(R.id.people);
        cancel_btn = findViewById(R.id.cancel_btn);

        // getIntent
        Intent get = getIntent();
        start = get.getStringExtra("start");
        end = get.getStringExtra("end");
        time = get.getStringExtra("time");
        dis = get.getStringExtra("dis");
        fare = get.getStringExtra("fare");
        people = get.getIntExtra("people", 1);

        start_txt.setText(start);
        end_txt.setText(end);
        if (dis.length() < 7) {
            time_txt.setText("소요시간 : 약 "+time+"분");
            dis_txt.setText("이동거리 : 약 "+dis+"km");
        } else {
            time_txt.setText(time);
            dis_txt.setText(dis);
        }
        fare_txt.setText("이용요금 : "+fare);
        people_txt.setText("이용인원 : "+people+"인");
    }

    protected void onResume() {
        super.onResume();

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
