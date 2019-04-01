package com.example.kks.carpool.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.kks.carpool.Chatting;
import com.example.kks.carpool.R;
import com.example.kks.carpool.appRTC.PopUpVideoCall;
import com.example.kks.carpool.google_map;
import com.example.kks.carpool.model.ChattingAdapter;
import com.example.kks.carpool.model.ChattingMessageItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.kks.carpool.Chatting.TARGET_ID;
import static com.example.kks.carpool.Chatting.TARGET_PROFILE;
import static com.example.kks.carpool.Chatting.inApp;
import static com.example.kks.carpool.Chatting.inchat;
import static com.example.kks.carpool.Result.USER_NAME;
import static com.example.kks.carpool.Result.USER_PROFILE;
import static com.example.kks.carpool.driver.RequestClick.D_REQUEST_NUM;
import static com.example.kks.carpool.google_map.REQUEST_NUM;

public class RealService extends Service {

    private NotificationManager manager;
    private String CHANNEL_ID = "FIREBASE_NOTI_CHANNEL";
    private String req_num;

    public static boolean isService = true;
    public static SimpleDateFormat mFormat = new SimpleDateFormat("aa hh:mm", Locale.KOREAN);

    // getIntent
//    private String roomNo = "11", targetId = "KKS", targetNickName = "TONICK", loginUserId = "TOID", loginUserNick = "KYU";

    private Handler msgHandler;
    private ReceiveThread recevie;
    private SendThread send;

    private Socket socket;
    public static SocketClient socketClient;

    // 5사무실
//    public static final String ip = "192.168.0.21";
//    public static final String ip = "192.168.0.139";
    // 집
    public static final String ip = "54.180.95.149";
//    public static final String ip = "192.168.200.128";
    // 3사무실
//    public static final String ip = "192.168.0.81";

    private int port = 5000;

    // 받은 메시지
    private String[] msgFilter;

    // Item
    private ChattingMessageItem messageContent;
    private ArrayList<ChattingMessageItem> messageData;
    private ChattingAdapter chattingRoomAdapter;

    // service용 아이템
    ChattingMessageItemService messageContentService;
    ArrayList<ChattingMessageItemService> messageDataService;

    // Service 확인자
    public static Intent serviceIntent = null;

    /**
     * 엑티비티와 연결
     **/
    private final IBinder binder = new MyBinder();
    private ICallback mCallback;
    private int a = 0;

    // 엑티비티로 보내는 곳
    public interface ICallback {
        public void RiderRemoteCall(String toRider);

        public void DriverRemoteCall(String toDriver);

        public void VideoCall(String toDriver);

        public void LocationCall(float bearing, double lat, double lon);
    }

    // for registration in activity
    public void registerCallback(ICallback cb) {
        mCallback = cb;
    }

    // service 에서 받는 곳
    public void myServiceFunc(String msg) {
        Log.e("BindService", "엑티비티에서 서비스로" + msg);
        setSendBtn(msg);
    }

    public void getREQUEST_NUM(String req) {
        Log.e("서비스시작전 방번호!", "방번호 받아옴:::" + req);
        if (REQUEST_NUM == 0) {
            REQUEST_NUM = Integer.valueOf(req);
        }
    }

    public void getVideoCall(String RTC_Room) {
        Log.e("RealService", "방번호 또는 영통 거절을 상대방에게 보낸다 : " + RTC_Room);
        // 방번호 보내주기
        setSendBtn(RTC_Room);
    }

    public void serviceTimeAlarm(String msg) {
        Log.e("RealService", "몇분 남았다는 노티 띄워주기 위함 : " + msg);
        sendTimeNotification(msg);
    }

    public class MyBinder extends Binder {
        public RealService getService() { // 서비스 객체를 리턴
            return RealService.this;
        }
    }

    public RealService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceIntent = intent;
        showToast(getApplication(), "Start Service");
        SharedPreferences preferences = getSharedPreferences("reService", Context.MODE_PRIVATE);
        req_num = preferences.getString("request_num", "0");
        String user_name = preferences.getString("user_name", "유저이름");
        Log.e("셰어드 저장된 값::::", "req === " + req_num + " :::: user_name === " + user_name);

        if (USER_NAME == null) { // 재실행 될때 저장된 값 부르기
            Log.e("실행되는 서비스 위치::::", "req === null 일떄");
            socketClient = new SocketClient(req_num + "@" + user_name);
            socketClient.start();
        } else if (REQUEST_NUM == 0) {
            Log.e("실행되는 서비스 위치::::", "req === 0 일떄");
            socketClient = new SocketClient(D_REQUEST_NUM + "@" + USER_NAME);
            socketClient.start();
        } else {
            Log.e("실행되는 서비스 위치::::", "최초실챙 일떄");
            socketClient = new SocketClient(String.valueOf(REQUEST_NUM) + "@" + USER_NAME);
            socketClient.start();
        }
        messageData = new ArrayList<>();
        messageDataService = new ArrayList<>();
        chattingRoomAdapter = new ChattingAdapter();

        // 서버로부터 수신한 메세지를 처리하는 곳  ( AsyncTesk를  써도됨 )
        msgHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                msgFilter = msg.obj.toString().split("@");

                if (msg.what == 1110) { // 운전자
                    // 받은 내용 필터로 구분하고 내용에 따라 구분하자
                    if (msgFilter[1].equals("수락확인~!")) { // 탑승자에게 매칭수락~!을 보낸후의 탑승자의 정보를 받아오는 부분
                        mCallback.DriverRemoteCall(msg.obj.toString());
                    } else { // 탑승자의 요청이 종료되거나 취소한 경우 받는 메시지
                        mCallback.DriverRemoteCall(msg.obj.toString());
                    }
                } else if (msg.what == 1111) { // 탑승자

                    if (msgFilter[1].equals("매칭수락~!")) { // 운전자로부터 요청수락한 메시지를 받는 부분
                        // 엑티비티에 알려준다
                        mCallback.RiderRemoteCall(msg.obj.toString());
                        Toast.makeText(RealService.this, " 매칭 요청완료!! ", Toast.LENGTH_SHORT).show();
                        Log.e("받은 메세지 ", msg.obj.toString());
                    } else {

                    }
                    // 메세지가 왔다면.
//                    Toast.makeText(RealService.this, "메세지 : " + msg.obj.toString(), Toast.LENGTH_SHORT).show();
//                    Log.d("받은 메세지 ", msg.obj.toString());

                    // 엑티비티로 전송
//                    mCallback.remoteCall(msgFilter[1]);


                } else if (msg.what == 1112) { // 채팅
                    if (USER_NAME == null) {
                        SharedPreferences preferences = getSharedPreferences("reService", Context.MODE_PRIVATE);
                        req_num = preferences.getString("request_num", "0");
                        USER_NAME = preferences.getString("user_name", "");
                        USER_PROFILE = preferences.getString("user_profile", "");
                        TARGET_ID = preferences.getString("target_id", "");
                        TARGET_PROFILE = preferences.getString("target_profile", "");
                    }
                    // 수신 1
                    messageContent = new ChattingMessageItem(1, msgFilter[0], TARGET_PROFILE, msgFilter[1], msgFilter[2]);

                    Log.e("RealService", "앱이 켜져있다 : " + inchat);
                    // 메세지가 왔다면.
//                    if (inApp) { // 앱이 켜져있다면
                    if (inchat) { // 채팅방이라면 or 강제종료
                        if (inApp) { // 채팅방이라면
                            // App is running
                            Log.e("앱 상태(RealService) ", "채팅방 안");
                            Toast.makeText(RealService.this, "메세지 : " + msg.obj.toString(), Toast.LENGTH_SHORT).show();
                            Log.d("받은 메세지 ", msg.obj.toString());
                            mCallback.DriverRemoteCall(msg.obj.toString());
                            messageData.clear();
                        } else { // 강제종료일 경우 or 채팅방입장 안함
                            Log.e("앱 상태(RealService) ", "강제종료");
                            Toast.makeText(RealService.this, "메세지 : " + msg.obj.toString(), Toast.LENGTH_SHORT).show();
                            Log.d("받은 메세지 ", msg.obj.toString());
                            sendNotification(msgFilter[0], msgFilter[1]);

                            messageContentService = new ChattingMessageItemService(1, msgFilter[0], TARGET_PROFILE, msgFilter[1], msgFilter[2]);
                            messageDataService.add(messageContentService);

                        }
                    } else { // 앱은 켜져있지만 채팅방은 아니다
                        // App is not running
                        Log.e("앱 상태(RealService) ", "App is running - 채팅방 밖");
                        Toast.makeText(RealService.this, "앱 종료됬을 때 : " + msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        Log.d("받은 메세지 ", msg.obj.toString());
                        sendNotification(msgFilter[0], msgFilter[1]);

//                        Gson gson = new Gson();
//                        SharedPreferences sharedPreferences3 = getSharedPreferences("chatting" + USER_NAME + "99", Context.MODE_PRIVATE);
//
//                        String myChat = sharedPreferences3.getString("saveChat", null);
//                        Type type = new TypeToken<ArrayList<ChattingMessageItem>>() {
//                        }.getType();
//                        messageData = gson.fromJson(myChat, type);
//
//                        if (messageData == null) {
//                            Log.e("확인해라", "저장되어있는게 널이다");
//                            messageData = new ArrayList<>();
//                        }

                        // 세어드 불러와서 추가해야지 멍청아
                        messageData.add(messageContent);
//                        chattingRoomAdapter.setData(messageData, TARGET_ID);
//                        chattingRoomAdapter.notifyDataSetChanged();

//                        messageContentService = new ChattingMessageItemService(1, msgFilter[0], TARGET_PROFILE, msgFilter[1], msgFilter[2]);
//                        messageDataService.add(messageContentService);

//                        chattingRoomAdapter.setData(messageData, TARGET_ID);
//                        chattingRoomAdapter.notifyDataSetChanged();
                        // 앱이 종료되어있어서 messageContent 생성이 안되서 그런듯 string[]으로 스플릿사용 넘어가서 만들어보기
                    }
                    SharedPreferences sharedPreferences = getSharedPreferences("chatting" + USER_NAME + "488", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    Gson gson = new Gson();
                    if (messageData == null || messageData.size() == 0) {
                        String saveChat = gson.toJson(messageDataService);
                        editor.putString("saveChatService", saveChat);
                        Log.e("RealService", "저장 service : " + saveChat);
                    } else {
                        String saveChat = gson.toJson(messageData);
                        editor.putString("saveChat", saveChat);
                        Log.e("RealService", "저장 : " + saveChat);
                    }

                    editor.apply();
//                    } else { // 앱이 꺼저있다.
//                        Log.e("앱 상태(RealService) ", "App is running - 채팅방 밖");
//                        Toast.makeText(RealService.this, "앱 종료됬을 때 : " + msg.obj.toString(), Toast.LENGTH_SHORT).show();
//                        Log.d("받은 메세지 ", msg.obj.toString());
//                    }
                } else if (msg.what == 1113) { // 영상통화
                    mCallback.VideoCall(msgFilter[3]);
//                    Intent intent = new Intent(RealService.this, PopUpVideoCall.class);
//                    intent.putExtra("roomN", msgFilter[3]);
//                    intent.putExtra("tartget_id", TARGET_ID);
//                    intent.putExtra("tartget_profile", TARGET_PROFILE);
//                    startActivity(intent);
                } else if (msg.what == 1114) { // 위치공유 // 탑승확인 유무
                    if (msgFilter[1].equals("xkqtmdgoTsmswlghkrdlsdban~!`")) { // 탑승유무 확인
                        if (a == 0) {
                            mCallback.LocationCall(999, 999, 999);
                            a++;
                        } else if (a == 1) { // 결제 완료
                            mCallback.LocationCall(888, 888, 888);
                            a++;
                        }
                    } else { // 위치공유 전송
                        mCallback.LocationCall(Float.parseFloat(msgFilter[3]), Double.parseDouble(msgFilter[4]), Double.parseDouble(msgFilter[5]));
                    }

                }
            }
        };

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        SharedPreferences preferences = getSharedPreferences("reService", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("request_num", String.valueOf(REQUEST_NUM));
        editor.putString("user_name", USER_NAME);
        editor.putString("user_profile", USER_PROFILE);
        editor.putString("target_id", TARGET_ID);
        editor.putString("target_profile", TARGET_PROFILE);
        editor.apply();
        serviceIntent = null;
        setAlarmTimer();
//        Thread.currentThread().interrupt();

    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        Log.e("onTaskRemoved", "");
    }

    public void showToast(final Application application, final String msg) {
        Handler h = new Handler(application.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(application, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void setAlarmTimer() {
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(Calendar.SECOND, 1);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);

        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
    }

    // 상대방의 시간 정보 띄워줌 ( 10, 5, 3, 1{곧 도착} )
    private void sendTimeNotification(String msg) {
        Log.e("realService", "시간 노티 불리는부분 + 시간 : " + msg);
        if (msg.equals("목적지에 곧 도착합니다.")) {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.carpool))
                    .setSmallIcon(R.drawable.carpool)
                    .setContentTitle("목적지 도착 정보")
                    .setContentText("목적지에 곧 도착합니다.")
                    .setAutoCancel(true);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(888 /* ID of notification */, notificationBuilder.build());
        } else if (msg.equals("요금결제를 진행해 주세요.")) {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.carpool))
                    .setSmallIcon(R.drawable.carpool)
                    .setContentTitle("요금결제")
                    .setContentText("요금결제를 진행해 주세요.")
                    .setAutoCancel(true);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(888 /* ID of notification */, notificationBuilder.build());
        } else {
            if (msg.equals("0")) {
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.carpool))
                        .setSmallIcon(R.drawable.carpool)
                        .setContentTitle("카풀 도착 정보")
                        .setContentText("곧 도착할 예정입니다.")
                        .setAutoCancel(true);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(888 /* ID of notification */, notificationBuilder.build());
            } else {
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.carpool))
                        .setSmallIcon(R.drawable.carpool)
                        .setContentTitle("카풀 도착 정보")
                        .setContentText("약 " + msg + " 분 후 도착할 예정입니다.")
                        .setAutoCancel(true);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(888 /* ID of notification */, notificationBuilder.build());
            }
        }
    }

    // 채팅 노티 띄워줌
    private void sendNotification(String senderID, String message) {
        Log.e("변화탐지 서비스", "sendNotification");

        SharedPreferences preferences = getSharedPreferences("reService", Context.MODE_PRIVATE);
        req_num = preferences.getString("request_num", "0");
        String noti_user_id = preferences.getString("user_name", "");
        String noti_user_profile = preferences.getString("user_profile", "");
        String noti_target_id = preferences.getString("target_id", "");
        String noti_target_profile = preferences.getString("target_profile", "");

        Gson gson = new Gson();
        String saveChat = gson.toJson(messageData);
        Log.e("변화탐지 서비스", "" + saveChat);
//        String serviceChat = gson.toJson(messageDataService);
//        Log.e("변화탐지 서비스", ""+serviceChat);

        // 아이디가 운전자인지 탑승자인지 구분하여 화면전환을 한다
        Intent intent = new Intent(this, Chatting.class);
        intent.putExtra("user_id", noti_user_id);
        intent.putExtra("user_profile", noti_user_profile);
        intent.putExtra("target_id", noti_target_id);
        intent.putExtra("target_profile", noti_target_profile);
        intent.putExtra("request_num", req_num);
//        if (serviceChat != null) {
//            intent.putExtra("noti_chat", serviceChat);
//            Log.e("변화탐지 서비스", ""+serviceChat);
//        } else {
        intent.putExtra("noti_chat", saveChat);
        Log.e("변화탐지 서비스", "" + saveChat);
//        }

        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.carpool))
                .setSmallIcon(R.drawable.carpool)
                .setContentTitle("새로운 메세지가 도착했습니다.")
                .setContentText(senderID + "님의 메세지 :  " + message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(999 /* ID of notification */, notificationBuilder.build());


    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "CHATTING_CHANNEL",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }


    // 채팅 입력 이벤트
    public void setSendBtn(String getMSG) {
//        Button enterRoomChattingSend = (Button) findViewById(R.id.send_btn);

        String message = getMSG;

        if (message == null || TextUtils.isEmpty(message) || message.equals("")) {
            Toast.makeText(RealService.this, "메세지를 입력해주세요", Toast.LENGTH_SHORT).show();
        } else {
            int mode = 2;
            String senderId = USER_NAME;
//            String senderNick = loginUserNick;

            // 현재 시간 받아오기
            long mNow;
            Date mDate;
            mNow = System.currentTimeMillis();
            mDate = new Date(mNow);

            String time = mFormat.format(mDate);

//            messageContent = null;
//            messageContent = new ChattingMessageItem(mode, senderId, senderNick, message, time);
//
//            messageData.add(messageContent);
//            chattingRoomAdapter.notifyDataSetChanged();

            // 메세지 보내주기
            send = new SendThread(socket, message);
            send.start();

            // 에디트 텍스트 비워주기
//                    enterRoomChattingEditText.setText(null);
//            editText.setText(null);

            // 키보드 내려주기
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
//            Toast.makeText(RealService.this, "전송", Toast.LENGTH_SHORT).show();
        }

    }

    public static class ChattingMessageItemService {

        public int type;
        public String name;
        public String profile;
        public String message;
        public String time;

        public ChattingMessageItemService(int type, String name, String profile, String message, String time) {
            this.type = type;
            this.name = name;
            this.profile = profile;
            this.message = message;
            this.time = time;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getProfile() {
            return profile;
        }

        public void setProfile(String profile) {
            this.profile = profile;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }

    /**
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     **/
    // 내부클래스   ( 접속용 )
    public class SocketClient extends Thread {

        DataInputStream in = null;
        DataOutputStream out = null;
        public String roomAndUserData; // 방 정보 ( 방번호 /  접속자 아이디 )

        public SocketClient(String roomAndUserData) {
            this.roomAndUserData = roomAndUserData;
        }

        public void run() {
            try {
                // 채팅 서버에 접속 ( 연결 )  ( 서버쪽 ip와 포트 )
                socket = new Socket(ip, port);

                // 메세지를 서버에 전달 할 수 있는 통로 ( 만들기 )
                out = new DataOutputStream(socket.getOutputStream());
                in = new DataInputStream(socket.getInputStream());

                // 서버에 초기 데이터 전송  ( 방번호와 접속자 아이디가 담겨서 간다 ) -  식별자 역할을 하게 될 거임.
                out.writeUTF(roomAndUserData);

                // (메세지 수신용 쓰레드 생성 ) 리시브 쓰레드 시작
                recevie = new ReceiveThread(socket);
                recevie.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    } //SocketClient의 끝

    // 내부 클래스  ( 메세지 전송용 )
    class SendThread extends Thread {
        Socket socket;
        String sendmsg;
        DataOutputStream output;


        public SendThread(Socket socket, String sendmsg) {
            this.socket = socket;
            this.sendmsg = sendmsg;
            try {
                // 채팅 서버로 메세지를 보내기 위한  스트림 생성.
                output = new DataOutputStream(socket.getOutputStream());

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // 서버로 메세지 전송 ( 이클립스 서버단에서 temp 로 전달이 된다.
        public void run() {
            try {
                if (output != null) {
                    if (sendmsg != null) {
                        Log.e("이상하게 받아오냐???", ":::" + sendmsg);
                        String[] filter = sendmsg.split("@");
                        if (filter[3].equals("탑승자가 요청을 취소했습니다.") || filter[3].equals("이미 완료된 요청입니다.") || filter[3].equals("Destroy")) {
                            output.writeUTF(sendmsg);
//                            socket.close();
//                            serviceIntent = null;
//                            recevie.interrupt();
//                            recevie = null;
//                            this.interrupt();
                        } else if (filter[3].equals("매칭수락~!")) { // 운전자가 탑승자 요청을 수락한다 보내는 곳
                            output.writeUTF(sendmsg);
                        } else {
                            // 여기서 방번호와 상대방 아이디 까지 해서 보내줘야 할거같다 .
                            // 서버로 메세지 전송하는 부분
                            output.writeUTF(sendmsg);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // ( 메세지 수신용 )   -  서버로부터 받아서, 핸들러에서 처리하도록 할 거.
    class ReceiveThread extends Thread {

        Socket socket = null;
        DataInputStream input = null;

        public ReceiveThread(Socket socket) {
            this.socket = socket;

            try {
                // 채팅 서버로부터 메세지를 받기 위한 스트림 생성.
                input = new DataInputStream(socket.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                while (input != null && serviceIntent != null) {
                    // 채팅 서버로 부터 받은 메세지
                    String msg = input.readUTF();

                    // 방번호, 보낸사람, 받는 사람, 메세지
                    // 받을때는 보낸사람, 보낸내용, 보낸시간
                    String[] filt = msg.split("@");

                    if (msg != null) {
                        // 핸들러에게 전달할 메세지 객체
                        Message hdmg = msgHandler.obtainMessage();

                        /** what = 받는 클라
                         *  1110 = 운전자
                         *  1111 = 탑승자
                         *  1112 = 채팅
                         **/
                        if (filt[1].equals("이미 완료된 요청입니다.") || filt[1].equals("탑승자가 요청을 취소했습니다.") || filt[1].equals("요청이 종료되었습니다.") || filt[1].equals("수락확인~!")) { // 매칭 취소
                            // 핸들러에게 전달할 메세지의 식별자
                            hdmg.what = 1110;
                            // 메세지의 본문
                            hdmg.obj = msg;
                            // 핸들러에게 메세지 전달 ( 화면 처리 )
                            msgHandler.sendMessage(hdmg);

                        } else if (filt[1].equals("매칭수락~!")) { // 연결확인 돌려받기 요청자 정보 받는다
                            // 핸들러에게 전달할 메세지의 식별자
                            hdmg.what = 1111;
                            // 메세지의 본문
                            hdmg.obj = msg;
                            // 핸들러에게 메세지 전달 ( 화면 처리 )
                            msgHandler.sendMessage(hdmg);
                        } else if (filt[1].equals("dudxhddhkTek~!`") || filt[1].equals("dudtkdxhdghkrjwjf~!`")) { // 영상통화 왔다 // 영상통화 거절
                            // 핸들러에게 전달할 메세지의 식별자
                            hdmg.what = 1113;
                            // 메세지의 본문
                            hdmg.obj = msg;
                            // 핸들러에게 메세지 전달 ( 화면 처리 )
                            msgHandler.sendMessage(hdmg);
                        } else if (filt[1].equals("GPSLocationServiceLatitude : ~!")) { // 위치 변화 받는 부분
                            // 핸들러에게 전달할 메세지의 식별자
                            hdmg.what = 1114;
                            // 메세지의 본문
                            hdmg.obj = msg;
                            // 핸들러에게 메세지 전달 ( 화면 처리 )
                            msgHandler.sendMessage(hdmg);
                        } else if (filt[1].equals("xkqtmdgoTsmswlghkrdlsdban~!`")) { // 탑승확인 부분
                            Log.e("리시브 쓰레드 :: ", "탑승확인 여부 묻는다(탑승자에게)");
                            // 핸들러에게 전달할 메세지의 식별자
                            hdmg.what = 1114;
                            // 메세지의 본문
                            hdmg.obj = msg;
                            // 핸들러에게 메세지 전달 ( 화면 처리 )
                            msgHandler.sendMessage(hdmg);
                        } else { // 채팅
                            Log.e("리시브 쓰레드 :: ", "메시지 받았다(채팅)");
                            // 핸들러에게 전달할 메세지의 식별자
                            hdmg.what = 1112;
                            // 메세지의 본문
                            hdmg.obj = msg;
                            // 핸들러에게 메세지 전달 ( 화면 처리 )
                            msgHandler.sendMessage(hdmg);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}

