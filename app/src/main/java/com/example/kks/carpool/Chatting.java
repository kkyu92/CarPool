package com.example.kks.carpool;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.kks.carpool.appRTC.PopUpVideoCall;
import com.example.kks.carpool.driver.RequestClick;
import com.example.kks.carpool.driver.myrouteAdapter;
import com.example.kks.carpool.model.ChattingAdapter;
import com.example.kks.carpool.model.ChattingMessageItem;
import com.example.kks.carpool.service.ExampleService;
import com.example.kks.carpool.service.RealService;
import com.example.kks.carpool.service.RunHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.kks.carpool.Result.USER_NAME;
import static com.example.kks.carpool.Result.USER_PROFILE;
import static com.example.kks.carpool.service.ExampleService.socket;

public class Chatting extends AppCompatActivity {

    public RequestManager mGlideRequestManager;
    public static boolean inchat, inApp;
    private String FLAG = "";

    public static SimpleDateFormat mFormat = new SimpleDateFormat("aa hh:mm", Locale.KOREAN);

    private TextView target_txt;
    private EditText chat_edit;
    private ImageView back_btn;
    private CircleImageView my_profile;
    private ImageButton send_btn;

    private RecyclerView chat_rv;
    // Item
    private ChattingMessageItem messageContent;
    private ArrayList<ChattingMessageItem> messageData;
    private ChattingAdapter chattingRoomAdapter;

    // getIntent
    public static int REQUEST_NUM;
    public static String TARGET_ID, TARGET_PROFILE;
//    private String TARGET_PROFILE;

    //    public static Socket socket;
    // 5사무실
    public static final String IP = "192.168.0.139";
    // 집
//    public static final String IP = "192.168.200.154";
    // 3사무실
//    public static final String IP = "192.168.0.81";

    // 받은 메시지
    private String[] msgFilter;

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
            ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

            List<ActivityManager.RunningTaskInfo> taskList = activityManager.getRunningTasks(10);

            if (!taskList.isEmpty()) {
                ActivityManager.RunningTaskInfo runningTaskInfo = taskList.get(0);
                if (runningTaskInfo.topActivity != null &&
                        !runningTaskInfo.topActivity.getClassName().contains(
                                "com.example.kks.carpool")) {
                    //You are App is being killed so here you can add some code
                    Log.e("받은 메세지(방금 추가한 수정) ", "App is not running" + msg);
                }
            }
            // 메세지가 왔다면.
            if (RunHelper.isAppRunning(Chatting.this, "com.example.kks.carpool")) {
                // App is running
                Log.e("받은 메세지 ", "App is running" + msg);
            } else {
                // App is not running
                Log.e("받은 메세지 ", "App is not running" + msg);
            }
            if (inchat) { // 현재 엑티비티에 들어온 상태다
//                Toast.makeText(Chatting.this, "메세지 : " + msg, Toast.LENGTH_SHORT).show();
                Log.d("받은 메세지 ", msg);

                msgFilter = msg.split("@");

                // 수신 1
                messageContent = new ChattingMessageItem(1, msgFilter[0], TARGET_PROFILE, msgFilter[1], msgFilter[2]);

                messageData.add(messageContent);
                chattingRoomAdapter.setData(messageData, TARGET_ID);
                chat_rv.getRecycledViewPool().clear();
                chattingRoomAdapter.notifyDataSetChanged();
                if (messageData.size() > 3) {
                    chat_rv.smoothScrollToPosition(messageData.size() - 1);
                }
            } else { // 엑티비티 나간 상태다
                Toast.makeText(Chatting.this, "밖에서 받는 메세지 : " + msg, Toast.LENGTH_SHORT).show();
                Log.d("받은 메세지 ", msg);
//                        startService(msgFilter[1]);
            }
        }

        @Override
        public void VideoCall(String roomNum) { // 방번호 받았다
            Log.e("Chatting", "체팅방에서 방번호 받는 부분 : " + roomNum);
            if (!roomNum.equals("상대방이 거절 하였습니다.")) {
                unbindService(conn);
                a = 2;
                Intent intent = new Intent(Chatting.this, PopUpVideoCall.class);
                intent.putExtra("tartget_id", TARGET_ID);
                intent.putExtra("tartget_profile", TARGET_PROFILE);
                intent.putExtra("rtc_room", roomNum);
                intent.putExtra("req", String.valueOf(REQUEST_NUM));
                startActivity(intent);
            }
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
//            ms.getREQUEST_NUM(D_REQUEST_NUM);
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
        setContentView(R.layout.activity_chatting);

        inchat = true;
        inApp = true;

        mGlideRequestManager = Glide.with(this);

        target_txt = findViewById(R.id.chat_target);
        back_btn = findViewById(R.id.chat_back_btn);
        my_profile = findViewById(R.id.chat_myProfile);
        chat_edit = findViewById(R.id.chat_Etext);
        send_btn = findViewById(R.id.chat_send_btn);

        chat_rv = findViewById(R.id.chat_rv);


        // getIntent
        Intent intent = getIntent();
        REQUEST_NUM = intent.getIntExtra("roomNum", 1);
        TARGET_ID = intent.getStringExtra("name");
        TARGET_PROFILE = intent.getStringExtra("profile");
        if (TARGET_ID == null) {
            loadData();
            Log.e("noti", "노티로 들어옴");
            FLAG = "noti";
            REQUEST_NUM = Integer.valueOf(intent.getStringExtra("request_num"));
            TARGET_ID = intent.getStringExtra("target_id");
            TARGET_PROFILE = intent.getStringExtra("target_profile");
            USER_NAME = intent.getStringExtra("user_id");
            USER_PROFILE = intent.getStringExtra("user_profile");
            String noti_chat = intent.getStringExtra("noti_chat");

            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<ChattingMessageItem>>() {
            }.getType();
            ArrayList<ChattingMessageItem> items = gson.fromJson(noti_chat, type);
/// ---- chattingmessageitemservice 서비스에서 돌고있는거 가져와서 넣어줘야함
            ArrayList<RealService.ChattingMessageItemService> serviceitems = gson.fromJson(noti_chat, type);
/** 확인해야하는 부분 **/
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//            // 채팅 어뎁터 이너 클래스로 만들어야 가능한부분
//            if (serviceitems.size() != 0) {
//                for (int i = 0; i < items.size(); i++) {
//                    RealService.ChattingMessageItemService item = new RealService.ChattingMessageItemService(
//                            items.get(i).getType(),
//                            items.get(i).getName(),
//                            items.get(i).getProfile(),
//                            items.get(i).getMessage(),
//                            items.get(i).getTime()
//                    );
//                    Log.e("noti", "노티로 들어온 메시지 : " + items.get(i).getType() +
//                            items.get(i).getName() +
//                            items.get(i).getProfile() +
//                            items.get(i).getMessage() +
//                            items.get(i).getTime());
//
//                    messageDataService.add(item);
//                }
//                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
//                notiChattingAdapter = new NotiChattingAdapter();
//                notiChattingAdapter.setData(messageDataService, TARGET_ID);
//                notiChattingAdapter.notifyDataSetChanged();
//                chat_rv.setLayoutManager(layoutManager);
//                chat_rv.setAdapter(notiChattingAdapter);
//                if (messageDataService.size() > 0) {
//                    chat_rv.smoothScrollToPosition(messageDataService.size() - 1);
//                }
//            } else {
//                for (int i = 0; i < items.size(); i++) {
//                    ChattingMessageItem item = new ChattingMessageItem(
//                            items.get(i).getType(),
//                            items.get(i).getName(),
//                            items.get(i).getProfile(),
//                            items.get(i).getMessage(),
//                            items.get(i).getTime()
//                    );
//                    Log.e("noti", "노티로 들어온 메시지 : " + items.get(i).getType() +
//                            items.get(i).getName() +
//                            items.get(i).getProfile() +
//                            items.get(i).getMessage() +
//                            items.get(i).getTime());
//
//                    messageData.add(item);
//                }
//                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
//                chattingRoomAdapter = new ChattingAdapter();
//                chattingRoomAdapter.setData(messageData, TARGET_ID);
//                chattingRoomAdapter.notifyDataSetChanged();
//                chat_rv.setLayoutManager(layoutManager);
//                chat_rv.setAdapter(chattingRoomAdapter);
//                if (messageData.size() > 0) {
//                    chat_rv.smoothScrollToPosition(messageData.size() - 1);
//                }
//            }
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//            for (int i = 0; i < items.size(); i++) {
//                ChattingMessageItem item = new ChattingMessageItem(
//                        items.get(i).getType(),
//                        items.get(i).getName(),
//                        items.get(i).getProfile(),
//                        items.get(i).getMessage(),
//                        items.get(i).getTime()
//                );
//                Log.e("noti", "노티로 들어온 메시지 : " + items.get(i).getType() +
//                        items.get(i).getName() +
//                        items.get(i).getProfile() +
//                        items.get(i).getMessage() +
//                        items.get(i).getTime());
//
//                messageData.add(item);
//            }
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            chattingRoomAdapter = new ChattingAdapter();
            chattingRoomAdapter.setData(messageData, TARGET_ID);
            chattingRoomAdapter.notifyDataSetChanged();
            chat_rv.setLayoutManager(layoutManager);
            chat_rv.setAdapter(chattingRoomAdapter);
            if (messageData.size() > 0) {
                chat_rv.smoothScrollToPosition(messageData.size() - 1);
            }
        } else {
            loadData();
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            chattingRoomAdapter = new ChattingAdapter();
            chattingRoomAdapter.setData(messageData, TARGET_ID);
            chattingRoomAdapter.notifyDataSetChanged();
            chat_rv.setLayoutManager(layoutManager);
            chat_rv.setAdapter(chattingRoomAdapter);
            if (messageData.size() > 0) {
                chat_rv.smoothScrollToPosition(messageData.size() - 1);
            }
        }

        // 상대방 프로필 표시
        target_txt.setText(TARGET_ID + "님과의 대화");

        // 사용자의 프로필 표시
        if (USER_PROFILE.contains("kakao")) { // 카카오 로그인
            Uri img = Uri.parse(USER_PROFILE);
            Glide.with(this).load(img).into(my_profile);
        } else if (USER_PROFILE.contains("http")) { // 페북 로그인
            Uri img = Uri.parse("http://graph.facebook.com/" + USER_PROFILE + "/picture?type=normal");
            Glide.with(this).load(img).into(my_profile);
        } else { // 앱 로그인
            Uri img = Uri.parse("http://54.180.95.149/uploadsMap/" + USER_PROFILE);
            Glide.with(this).load(img).into(my_profile);
        }


//        new SocketClient(String.valueOf(REQUEST_NUM) + "@" + USER_NAME).start();

        if (RealService.serviceIntent == null) {
            serviceIntent = new Intent(Chatting.this, RealService.class);
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

    protected void onResume() {
        super.onResume();

        if (a == 2) {
            bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
        }
        // 뒤로가기
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 채팅저장 + 종료
                if (FLAG.equals("noti")) { // 노티 클릭으로 들어옴
                    // 운전자 탑승자 구분 어떻게 하지??
                    Intent intent = new Intent(Chatting.this, WaitingDriver.class);
                    intent.putExtra("user_name", USER_NAME);
                    startActivity(intent);
                    finish();
                } else { // 그냥 뒤로가기
                    finish();
                }
            }
        });

        // 채팅 보내기 버튼
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSendBtn();
            }
        });

        // 서버로부터 수신한 메세지를 처리하는 곳  ( AsyncTesk를  써도됨 )
//        msgHandler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                if (msg.what == 1112) {
//                    // 메세지가 왔다면.
//                    if (inchat) { // 현재 엑티비티에 들어온 상태다
//                        Toast.makeText(Chatting.this, "메세지 : " + msg.obj.toString(), Toast.LENGTH_SHORT).show();
//                        Log.d("받은 메세지 ", msg.obj.toString());
//
//                        msgFilter = msg.obj.toString().split("@");
//
//                        // 수신 1
//                        messageContent = new ChattingMessageItem(1, msgFilter[0], TARGET_PROFILE, msgFilter[1], msgFilter[2]);
//
//                        messageData.add(messageContent);
//                        chattingRoomAdapter.setData(messageData, TARGET_ID);
//                        chat_rv.getRecycledViewPool().clear();
//                        chattingRoomAdapter.notifyDataSetChanged();
//                        if (messageData.size() > 3) {
//                            chat_rv.smoothScrollToPosition(messageData.size() - 1);
//                        }
//                    } else { // 엑티비티 나간 상태다
//                        Toast.makeText(Chatting.this, "밖에서 받는 메세지 : " + msg.obj.toString(), Toast.LENGTH_SHORT).show();
//                        Log.d("받은 메세지 ", msg.obj.toString());
////                        startService(msgFilter[1]);
//                    }
//                }
//            }
//        };
    }

    // 채팅 입력 이벤트
    private void setSendBtn() {

        String message = chat_edit.getText().toString();

        if (message == null || TextUtils.isEmpty(message) || message.equals("")) {
            Toast.makeText(Chatting.this, "메세지를 입력해주세요", Toast.LENGTH_SHORT).show();
        } else {
            int mode = 2;
            String senderId = USER_NAME;
            String senderProfile = USER_PROFILE;

            // 현재 시간 받아오기
            long mNow;
            Date mDate;
            mNow = System.currentTimeMillis();
            mDate = new Date(mNow);

            String time = mFormat.format(mDate);

            messageContent = null;
            messageContent = new ChattingMessageItem(mode, senderId, senderProfile, message, time);

            messageData.add(messageContent);
            chattingRoomAdapter.setData(messageData, TARGET_ID);
            chat_rv.getRecycledViewPool().clear();
            chattingRoomAdapter.notifyDataSetChanged();
            if (messageData.size() > 3) {
                chat_rv.smoothScrollToPosition(messageData.size() - 1);
            }

            // 메세지 보내주기
//            send = new SendThread(socket, message);
//            send.start();
            ms.myServiceFunc(REQUEST_NUM + "@" + USER_NAME + "@" + TARGET_ID + "@" + message);

            // 에디트 텍스트 비워주기
//                    enterRoomChattingEditText.setText(null);
            chat_edit.setText(null);

            // 키보드 내려주기
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(chat_edit.getWindowToken(), 0);

            Toast.makeText(Chatting.this, "전송", Toast.LENGTH_SHORT).show();

            chattingRoomAdapter.notifyDataSetChanged();
            if (messageData.size() > 3) {
                chat_rv.smoothScrollToPosition(messageData.size() - 1);
            }
        }
    }

//    // 내부클래스   ( 접속용 )
//    class SocketClient extends Thread {
//
//        DataInputStream in = null;
//        DataOutputStream out = null;
//        String roomAndUserData; // 방 정보 ( 방번호 /  접속자 아이디 )
//
//        public SocketClient(String roomAndUserData) {
//            this.roomAndUserData = roomAndUserData;
//        }
//
//        public void run() {
//            try {
//                // 채팅 서버에 접속 ( 연결 )  ( 서버쪽 ip와 포트 )
//                socket = new Socket(IP, port);
//
//                // 메세지를 서버에 전달 할 수 있는 통로 ( 만들기 )
//                out = new DataOutputStream(socket.getOutputStream());
//                in = new DataInputStream(socket.getInputStream());
//
//                // 서버에 초기 데이터 전송  ( 방번호와 접속자 아이디가 담겨서 간다 ) -  식별자 역할을 하게 될 거임.
//                out.writeUTF(roomAndUserData);
//
//                // (메세지 수신용 쓰레드 생성 ) 리시브 쓰레드 시작
//                recevie = new ReceiveThread(socket);
//                recevie.start();
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    } //SocketClient의 끝

//    // 내부 클래스  ( 메세지 전송용 )
//    class SendThread extends Thread {
//        Socket socket;
//        String sendmsg;
//        DataOutputStream output;
//
//
//        public SendThread(Socket socket, String sendmsg) {
//            this.socket = socket;
//            this.sendmsg = sendmsg;
//            try {
//                // 채팅 서버로 메세지를 보내기 위한  스트림 생성.
//                output = new DataOutputStream(socket.getOutputStream());
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//
//        // 서버로 메세지 전송 ( 이클립스 서버단에서 temp 로 전달이 된다.
//        public void run() {
//            try {
//                if (output != null) {
//                    if (sendmsg != null) {
//
//                        // 여기서 방번호와 상대방 아이디 까지 해서 보내줘야 할거같다 .
//                        // 서버로 메세지 전송하는 부분
//                        output.writeUTF(String.valueOf(REQUEST_NUM) + "@" + USER_NAME + "@" + TARGET_ID + "@" + sendmsg);
//                    }
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//
//    // ( 메세지 수신용 )   -  서버로부터 받아서, 핸들러에서 처리하도록 할 거.
//    class ReceiveThread extends Thread {
//
//        Socket socket = null;
//        DataInputStream input = null;
//
//        public ReceiveThread(Socket socket) {
//            this.socket = socket;
//
//            try {
//                // 채팅 서버로부터 메세지를 받기 위한 스트림 생성.
//                input = new DataInputStream(socket.getInputStream());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void run() {
//            try {
//                while (input != null) {
//                    // 채팅 서버로 부터 받은 메세지
//                    String msg = input.readUTF();
//                    Log.e("채팅방:::", "받은 메시지 : " + msg);
//                    if (msg != null) {
//                        // 핸들러에게 전달할 메세지 객체
//                        Message hdmg = msgHandler.obtainMessage();
//
//                        // 핸들러에게 전달할 메세지의 식별자
//                        hdmg.what = 1112;
//
//                        // 메세지의 본문
//                        hdmg.obj = msg;
//
//                        // 핸들러에게 메세지 전달 ( 화면 처리 )
//                        msgHandler.sendMessage(hdmg);
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//
//    // ( 노티용 ) - 채팅 온것을 서비스로 보냄
//    public static class ServiceThread extends Thread{
//        Socket socket = null;
//        Handler handler;
//        boolean isRun = true;
//        DataInputStream input = null;
//
//        public ServiceThread(Handler handler, Socket socket){
//            this.handler = handler;
//            this.socket = socket;
//
//            try {
//                // 채팅 서버로부터 메세지를 받기 위한 스트림 생성.
//                input = new DataInputStream(socket.getInputStream());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        public void stopForever(){
//            synchronized (this) {
//                this.isRun = false;
//            }
//        }
//
//        public void run(){
//            //반복적으로 수행할 작업을 한다.
//            try {
//                while (input != null) {
//                    // 채팅 서버로 부터 받은 메세지
//                    String msg = input.readUTF();
//                    Log.e("채팅방:::", "받은 메시지 : " + msg);
//                    if (msg != null) {
//                        // 핸들러에게 전달할 메세지 객체
//                        Message hdmg = handler.obtainMessage();
//
//                        // 핸들러에게 전달할 메세지의 식별자
//                        hdmg.what = 1112;
//
//                        // 메세지의 본문
//                        hdmg.obj = msg;
//
//                        // 핸들러에게 메세지 전달 ( 화면 처리 )
//                        handler.sendMessage(hdmg);
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.e("앱 상태(onPause) ", "App is running");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("앱 상태(onStop) ", "App is running");

//        unbindService(conn);
//        Intent service = new Intent(Chatting.this,ChatService.class);
//        startService(service);
//        if (inchat) { //
//            // App is running
//            Log.e("앱 상태(onDestroy) ", "App is running");
//            ms.myServiceFunc(REQUEST_NUM+"@"+USER_NAME+"@"+TARGET_ID+"@destroy");
//            stopService(serviceIntent);
//            serviceIntent = null;
//        } else {
//            // App is not running
//            Log.e("앱 상태(onDestroy) ", "App is not running");
//            ms.myServiceFunc(REQUEST_NUM+"@"+USER_NAME+"@"+TARGET_ID+"@destroy");
//            stopService(serviceIntent);
//            serviceIntent = null;
//            unbindService(conn);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("Chatting(onDestroy) ", "App is running? : " + inchat);
        unbindService(conn);
        // 대화내용 저장
        SharedPreferences sharedPreferences = getSharedPreferences("chatting" + USER_NAME + "499", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String saveChat = gson.toJson(messageData);
        editor.putString("saveChat", saveChat);
        editor.apply();
        if (FLAG.equals("noti")) {
            Log.e("앱 상태(onDestroy) ", "노티로 들어와 뒤로가기 버튼 클릭");
            inApp = false;
        } else {
            if (inchat) { // 강제종료 (앱 종료)
                // App is running
                Log.e("앱 상태(onDestroy) ", "App is not running");
                ms.myServiceFunc(REQUEST_NUM + "@" + USER_NAME + "@" + TARGET_ID + "@destroy");
                stopService(serviceIntent);
                serviceIntent = null;
            } else { // 뒤로가기 (앱 켜진상태)
                // App is not running
                Log.e("앱 상태(onDestroy) ", "App is running");
                inApp = false;
            }
        }
//        if (RunHelper.isAppRunning(Chatting.this, "com.example.kks.carpool")) {
//            // App is running
//            Log.e("앱 상태(onDestroy) ", "App is running");
//            ms.myServiceFunc(REQUEST_NUM+"@"+USER_NAME+"@"+TARGET_ID+"@destroy");
//            stopService(serviceIntent);
//            serviceIntent = null;
////            unbindService(conn);
//        } else {
//            // App is not running
//            Log.e("앱 상태(onDestroy) ", "App is not running");
//            ms.myServiceFunc(REQUEST_NUM+"@"+USER_NAME+"@"+TARGET_ID+"@destroy");
//            stopService(serviceIntent);
//            serviceIntent = null;
//            unbindService(conn);
//        }
    }

    public void loadData() {
        Gson gson = new Gson();
        Log.e("불러오기", "대화내역 불러오기");
        // 2 - 서비스 // 3 - 내 채팅방
        SharedPreferences sharedPreferences2 = getSharedPreferences("chatting" + USER_NAME + "488", Context.MODE_PRIVATE);
        SharedPreferences sharedPreferences3 = getSharedPreferences("chatting" + USER_NAME + "499", Context.MODE_PRIVATE);
        String savedChat = sharedPreferences2.getString("saveChat", null);
        String myChat = sharedPreferences3.getString("saveChat", null);
        Type type = new TypeToken<ArrayList<ChattingMessageItem>>() {
        }.getType();
        messageData = gson.fromJson(myChat, type);
        ArrayList<ChattingMessageItem> serviceChat = gson.fromJson(savedChat, type);

        if (messageData == null) {
            messageData = new ArrayList<>();
        }
        if (serviceChat != null) {
            for (int i = 0; i < serviceChat.size(); i++) {
                ChattingMessageItem item = new ChattingMessageItem(
                        serviceChat.get(i).getType(),
                        serviceChat.get(i).getName(),
                        serviceChat.get(i).getProfile(),
                        serviceChat.get(i).getMessage(),
                        serviceChat.get(i).getTime()
                );
                messageData.add(item);
            }
            serviceChat.clear();
            SharedPreferences.Editor editor = sharedPreferences2.edit();
            String clearChat = gson.toJson(serviceChat);
            editor.putString("saveChat", clearChat);
            editor.apply();
        }
    }

}
