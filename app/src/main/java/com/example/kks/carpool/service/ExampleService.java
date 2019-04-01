package com.example.kks.carpool.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.kks.carpool.Chatting;
import com.example.kks.carpool.R;
import com.example.kks.carpool.model.ChattingMessageItem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Date;

import static com.example.kks.carpool.Chatting.REQUEST_NUM;
import static com.example.kks.carpool.Chatting.inchat;
import static com.example.kks.carpool.Result.USER_NAME;
import static com.example.kks.carpool.Result.USER_PROFILE;
import static com.example.kks.carpool.service.App.CHANNEL_ID;

public class ExampleService extends Service {

    public static Socket socket;


    // 5사무실
    public static final String IP = "192.168.0.139";
    // 집
//    public static final String IP = "192.168.200.154";
    // 3사무실
//    public static final String IP = "192.168.0.81";


    private int port = 5000;
    // Thread
    private Handler msgHandler;
    private ReceiveThread recevie;

    // 받은 메시지
    private String[] msgFilter;

    private String TARGET_ID;

    /** 엑티비티와 연결 **/
    private final IBinder binder = new MyBinder();
    private ICallback mCallback;

    // declare callback function
    public interface ICallback {
        public void remoteCall();
    }

    // for registration in activity
    public void registerCallback(ICallback cb) {
        mCallback = cb;
    }

    // service contents
    public void myServiceFunc(String msg) {
        Log.e("BindService", "엑티비티에서 서비스로"+msg);
        // call callback in Activity
        mCallback.remoteCall();
    }


    public class MyBinder extends Binder {
        public ExampleService getService() { // 서비스 객체를 리턴
            return ExampleService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        new SocketClient(String.valueOf(REQUEST_NUM) + "@" + USER_NAME).start();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서버로부터 수신한 메세지를 처리하는 곳  ( AsyncTesk를  써도됨 )
        msgHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1112) {
                    // 메세지가 왔다면.
                    if (inchat) { // 현재 엑티비티에 들어온 상태다
                        Toast.makeText(ExampleService.this, "메세지 : " + msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        Log.d("EXAMPLE_SERVICE ", msg.obj.toString());

                        msgFilter = msg.obj.toString().split("@");
                        getMSG(msgFilter[1]);
                        // 수신 1
//                        messageContent = new ChattingMessageItem(1, msgFilter[0], TARGET_PROFILE, msgFilter[1], msgFilter[2]);
//
//                        messageData.add(messageContent);
//                        chattingRoomAdapter.setData(messageData, TARGET_ID);
//                        chat_rv.getRecycledViewPool().clear();
//                        chattingRoomAdapter.notifyDataSetChanged();
//                        if (messageData.size() > 3) {
//                            chat_rv.smoothScrollToPosition(messageData.size() - 1);
//                        }
                    } else { // 엑티비티 나간 상태다
                        Toast.makeText(ExampleService.this, "밖에서 받는 메세지 : " + msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        Log.d("EXAMPLE_SERVICE ", msg.obj.toString());
                        Intent notificationIntent = new Intent(ExampleService.this, Chatting.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(ExampleService.this, 0, notificationIntent, 0);

                        Notification notification = new NotificationCompat.Builder(ExampleService.this, CHANNEL_ID)
                                .setContentTitle(msgFilter[0])
                                .setContentText(msgFilter[1])
                                .setSmallIcon(R.drawable.carpool)
                                .setContentIntent(pendingIntent)
                                .build();

                        startForeground(1, notification);
                    }
                }
            }
        };
//        String input = intent.getStringExtra("inputExtra");
//        stopSelf();

        return START_NOT_STICKY;
    }

    String getMSG(String message) {
        return message;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }


    /** **/
    // 내부클래스   ( 접속용 )
    class SocketClient extends Thread {

        DataInputStream in = null;
        DataOutputStream out = null;
        String roomAndUserData; // 방 정보 ( 방번호 /  접속자 아이디 )

        public SocketClient(String roomAndUserData) {
            this.roomAndUserData = roomAndUserData;
        }

        public void run() {
            try {
                // 채팅 서버에 접속 ( 연결 )  ( 서버쪽 ip와 포트 )
                socket = new Socket(IP, port);

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
                while (input != null) {
                    // 채팅 서버로 부터 받은 메세지
                    String msg = input.readUTF();
                    Log.e("채팅방:::", "받은 메시지 : " + msg);
                    if (msg != null) {
                        // 핸들러에게 전달할 메세지 객체
                        Message hdmg = msgHandler.obtainMessage();

                        // 핸들러에게 전달할 메세지의 식별자
                        hdmg.what = 1112;

                        // 메세지의 본문
                        hdmg.obj = msg;

                        // 핸들러에게 메세지 전달 ( 화면 처리 )
                        msgHandler.sendMessage(hdmg);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 내부 클래스  ( 메세지 전송용 )
    public class SendThread extends Thread {
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

                        // 여기서 방번호와 상대방 아이디 까지 해서 보내줘야 할거같다 .
                        // 서버로 메세지 전송하는 부분
                        output.writeUTF(String.valueOf(REQUEST_NUM) + "@" + USER_NAME + "@" + TARGET_ID + "@" + sendmsg);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
