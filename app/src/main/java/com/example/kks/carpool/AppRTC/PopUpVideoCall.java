package com.example.kks.carpool.AppRTC;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kks.carpool.R;
import com.example.kks.carpool.Service.RealService;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.kks.carpool.LoginSignup.Result.USER_NAME;

public class PopUpVideoCall extends Activity {

    private String TAG = "PupUpVideoCall";

    private TextView name, msg_txt;
    private Button cancel, ok;
    private CircleImageView profile;

    private String TARGET_ID, TARGET_PROFILE, ROOM, REQUEST_NUM;

    // 서비스 부분
    Intent serviceIntent;
    int a = 1;
    RealService ms; // 서비스 객체
    boolean isService = false; // 서비스 중인 확인용

    // 서비스에서 받아옴 -- 노티 띄우기, 채팅아이템 추가하기
    public RealService.ICallback mCallback = new RealService.ICallback() {
        @Override
        public void RiderRemoteCall(String msg) {

        }

        @Override
        public void DriverRemoteCall(String msg) {
            // 메세지가 왔다면.
            //String getMSG = msg.obj.toString();
//            unbindService(conn);
            // 방번호, 보낸사람, 받는 사람, 메세지
            // 받을때는 보낸사람, 보낸내용, 보낸시간
        }

        @Override
        public void VideoCall(String toDriver) {

        }

        @Override
        public void LocationCall(float bearing, double lat, double lon) {

        }

    };

    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 서비스와 연결되었을 때 호출되는 메서드
            // 서비스 객체를 전역변수로 저장
            RealService.MyBinder mb = (RealService.MyBinder) service;
            ms = mb.getService(); // 서비스가 제공하는 메소드 호출하여
            ms.registerCallback(mCallback);
            // 서비스쪽 객체를 전달받을수 있슴
            isService = true;

        }

        public void onServiceDisconnected(ComponentName name) {
            Log.e("바인드 서비스", "::::: 서비스 연결 끊김");
            // 서비스와 연결이 끊겼을 때 호출되는 메서드
            isService = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_pop_up_video_call);

        profile = findViewById(R.id.profile_videoCall);
        name = findViewById(R.id.name_videoCall);
        msg_txt = findViewById(R.id.msg_txt);
        cancel = findViewById(R.id.cancel_videoCall);
        ok = findViewById(R.id.ok_videoCall);

        // getIntent
        Intent get = getIntent();
        ROOM = get.getStringExtra("rtc_room");
        Log.e("팝업 떳을 때",""+ROOM);
        TARGET_ID = get.getStringExtra("tartget_id");
        TARGET_PROFILE = get.getStringExtra("tartget_profile");
        REQUEST_NUM = get.getStringExtra("req");

        name.setText(TARGET_ID);
        msg_txt.setText(TARGET_ID + "님으로 부터\n영상통화가 왔습니다.");

        // 상대방 프로필 표시
        if (TARGET_PROFILE.contains("kakao")) { // 카카오 로그인
            Uri img = Uri.parse(TARGET_PROFILE);
            Glide.with(this).load(img).into(profile);
        } else if (!TARGET_PROFILE.contains(".jpg")) { // 페북 로그인
            Uri img = Uri.parse("http://graph.facebook.com/" + TARGET_PROFILE + "/picture?type=normal");
            Glide.with(this).load(img).into(profile);
        } else { // 앱 로그인
            Uri img = Uri.parse("http://54.180.95.149/uploadsMap/" + TARGET_PROFILE);
            Glide.with(this).load(img).into(profile);
        }

        // 서비스 연결
        if (RealService.serviceIntent == null) {
            serviceIntent = new Intent(PopUpVideoCall.this, RealService.class);
            startService(serviceIntent);
            Log.e("int a = ", "::::" + a);
            if (a == 1) {
                bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
            }
        } else {
            serviceIntent = RealService.serviceIntent;//getInstance().getApplication();
            Toast.makeText(getApplicationContext(), "already", Toast.LENGTH_LONG).show();
            // 재실행 됬을때 바인드 다시해줘야함
            bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 영상통화 취소
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 상대방에게 취소한것을 알림
                sendCancel();
                unbindService(conn);
                a = 999;
                finish();
            }
        });

        // 영상통화 수락
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PopUpVideoCall.this, ConnectActivity.class);
                intent.putExtra("req_num", REQUEST_NUM);
                intent.putExtra("tar_id", TARGET_ID);
                intent.putExtra("rtc_room", ROOM);
                Log.e("수락버튼 눌렀을때",""+ROOM);
                startActivity(intent);
                unbindService(conn);
                a = 0;
                finish();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (a == 1) {
            sendCancel();
            unbindService(conn);
        }
    }

    private void sendCancel() {
        ms.getVideoCall(REQUEST_NUM + "@" + USER_NAME + "@" + TARGET_ID + "@dudtkdxhdghkrjwjf~!`@상대방이 거절 하였습니다.");
    }
}
