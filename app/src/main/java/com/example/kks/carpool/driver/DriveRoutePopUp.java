package com.example.kks.carpool.driver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kks.carpool.R;
import com.google.android.gms.maps.model.LatLng;

public class DriveRoutePopUp extends Activity {

    // 레이아웃
    private EditText route_name;
    private TextView start_txt, end_txt;
    private ImageButton change_route;
    private Button cancel_btn, routeOK_btn, routeEDIT_btn;

    // 출발 도착 정보
    private String start_p, end_p, title;
    private LatLng sL, eL, start_L, end_L;
    private int idx;

    // 출발 도착 바꾸기
    private int change = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_drive_route_pop_up);

        route_name = findViewById(R.id.route_name);
        start_txt = findViewById(R.id.startplace_txt);
        end_txt = findViewById(R.id.endplace_txt);
        change_route = findViewById(R.id.change_route);
        cancel_btn = findViewById(R.id.cancel_btn);
        routeOK_btn = findViewById(R.id.driving_route_set);
        routeEDIT_btn = findViewById(R.id.driving_route_edit);

        // getIntent
        Intent getintent = getIntent();
        title = getintent.getStringExtra("title");
        start_p = getintent.getStringExtra("startPlace");
        end_p = getintent.getStringExtra("endPlace");
        sL = getintent.getParcelableExtra("s_latlng");
        eL = getintent.getParcelableExtra("e_latlng");
        idx = getintent.getIntExtra("idx",0);
        start_L = sL;
        end_L = eL;

        // 받아온 값 레이아웃에 표시
        start_txt.setText(start_p);
        end_txt.setText(end_p);

        // 수정할때 경로 제목 넣어주기
        if (title == null) {
            route_name.setText("");
        } else if (!title.equals("")) { route_name.setText(title); }

        if (idx != 0) { // 경로수정으로 들어왔을 때
            routeOK_btn.setVisibility(View.INVISIBLE);
            routeEDIT_btn.setVisibility(View.VISIBLE);
        } else { // 경로추가로 들어왔을 때
            routeOK_btn.setVisibility(View.VISIBLE);
            routeEDIT_btn.setVisibility(View.INVISIBLE);
        }
        Log.e("idx:::","~~"+idx);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 출발 도착 바꾸기
        change_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String st = start_txt.getText().toString();
                String et = end_txt.getText().toString();
                Log.e("출도착 바꾸기:::","st"+st);
                Log.e("출도착 바꾸기:::","et"+et);

                if (change == 0) { // 출발 도착 바꾸기
                    start_L = eL;
                    end_L = sL;
                    change = 1;
                } else if (change == 1) { // 원래대로 돌리기
                    start_L = sL;
                    end_L = eL;
                    change = 0;
                }

                start_txt.setText(et);
                end_txt.setText(st);
            }
        });

        // 취소버튼
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(DriveRoutePopUp.this, "등록을 취소하였습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // 등록완료 버튼
        routeOK_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 경로이름 미지정
                if (route_name.getText().toString().equals("")) {
//                    Toast.makeText(DriveRoutePopUp.this, "제목 빈칸 나의경로1 로 등록", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("change", change);
                    intent.putExtra("title", "나의 경로");
                    setResult(RESULT_OK, intent);
                    finish();
                } else { // 경로이름 지정
//                    Toast.makeText(DriveRoutePopUp.this, route_name.getText().toString(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("change", change);
                    intent.putExtra("title", route_name.getText().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        // 수정완료 버튼
        routeEDIT_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 경로이름 미지정
                if (route_name.getText().toString().equals("")) {
//                    Toast.makeText(DriveRoutePopUp.this, "제목 빈칸 나의경로1 로 등록", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("change", change);
                    intent.putExtra("title", "나의 경로");
                    intent.putExtra("idx", idx);
                    setResult(RESULT_OK, intent);
                    finish();
                } else { // 경로이름 지정
//                    Toast.makeText(DriveRoutePopUp.this, route_name.getText().toString(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("change", change);
                    intent.putExtra("title", route_name.getText().toString());
                    intent.putExtra("idx", idx);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }
}
