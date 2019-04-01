package com.example.kks.carpool;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.bumptech.glide.Glide;
import com.example.kks.carpool.driver.start_Driver;
import com.example.kks.carpool.service.ExampleService;
import com.example.kks.carpool.service.RealService;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

import java.net.URL;
import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.kks.carpool.Login.mGoogleApiClient;
import static com.example.kks.carpool.Login.prefConfig;

public class Result extends AppCompatActivity {

    public static String USER_NAME, USER_PROFILE;

    private TextView email_txt, name_txt, phone_txt, profile;
    private String image_path, email, name, phone;
    //    private URL profile;
    // 페북 프로필
    private ProfilePictureView profilePictureView;
    private LoginButton facebook_logout;
    private Button fake_facebook;
    // 구글 프로필
    private FirebaseAuth mAuth;
    private Button google_logout;
    // 카카오 프로필
    private AQuery aQuery;
    private CircleImageView kakaoProfile;
    private Button kakao_logout;
    // 이메일 로그인
    private Button email_logout;

    private Button start_btn;
    private Button rider_btn;

    public interface OnLogoutListener {
        public void logoutPerformed();
    }

    // 결제 테스트
    Button pay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        pay = findViewById(R.id.pay);

        email_txt = findViewById(R.id.txt_email);
        name_txt = findViewById(R.id.txt_name);
        phone_txt = findViewById(R.id.txt_phone);
        profile = findViewById(R.id.txt_profile);
        profilePictureView = findViewById(R.id.profile_img);

        // 페이스북
        fake_facebook = findViewById(R.id.logout_facebook_fake);
        facebook_logout = findViewById(R.id.logout_facebook);
        facebook_logout.setReadPermissions(Arrays.asList("public_profile"));

        // 구글
        mAuth = FirebaseAuth.getInstance();
        google_logout = findViewById(R.id.logout_google);

        // 카카오
        kakaoProfile = findViewById(R.id.profile_kakao);
        kakao_logout = findViewById(R.id.logout_kakao);
        aQuery = new AQuery(this);

        // 이메일
        email_logout = findViewById(R.id.logout_email);
        start_btn = findViewById(R.id.start_btn);
        rider_btn = findViewById(R.id.rider_btn);

        // GetIntent (로그인 해서 받아온 정보)
        image_path = getIntent().getStringExtra("photo_uri");
        email = getIntent().getStringExtra("email");
        name = getIntent().getStringExtra("name");
        phone = getIntent().getStringExtra("phone_number");
        USER_NAME = name;
        if (getIntent() != null) {

            email_txt.setText(email);
            name_txt.setText(name);
            phone_txt.setText(phone);
//            profile.setText(getIntent().getStringExtra("photo_uri"));
//            Log.e("전화번호", getIntent().getStringExtra("phone_number"));
//            Log.e("이미지 uri", getIntent().getStringExtra("photo_uri"));

            if (image_path != null) {
                if (image_path.contains("kakao")) {
                    Log.e("이미지 경로표시 :: ", "" + image_path);
                    USER_PROFILE = image_path;
                    aQuery.id(kakaoProfile).image(image_path); // <-  userProfile.getProfileImagePath() <- 큰 이미지
                    profilePictureView.setVisibility(View.INVISIBLE);
                    kakao_logout.setVisibility(View.VISIBLE);
                } else if (email != null && email.contains("gmail.com")) {
                    google_logout.setVisibility(View.VISIBLE);
                } else if (!image_path.contains(".jpg")) {
                    Log.e("이미지 경로표시 :: ", "" + image_path);
                    USER_PROFILE = image_path;
                    profilePictureView.setProfileId(image_path);
                    kakaoProfile.setVisibility(View.INVISIBLE);
                    fake_facebook.setVisibility(View.VISIBLE);
                } else {
                    kakaoProfile.setVisibility(View.VISIBLE);
                    profilePictureView.setVisibility(View.INVISIBLE);
                    Uri img = Uri.parse("http://54.180.95.149/uploadsMap/" + USER_PROFILE);
                    Glide.with(this).load(img).into(kakaoProfile);
                    image_path = "http://54.180.95.149/uploadsMap/" + USER_PROFILE;
                    USER_PROFILE = image_path;
                }
            } else { // 프로필 이미지 = null
                if (prefConfig.readLoginStatus()) {
                    email_logout.setVisibility(View.VISIBLE);
                } else if (email.contains("gmail.com")) { // 구글
                    google_logout.setVisibility(View.VISIBLE);
//                } else if () {

                } else {

                }
            }
        }
    }

    protected void onResume() {
        super.onResume();

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Result.this, KakaoPayment.class);
                intent.putExtra("user", "김규식");
                intent.putExtra("fare", "100");
                intent.putExtra("addr", "도착장소");
                startActivity(intent);
            }
        });

        // 페이스북
        fake_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                facebook_logout.performClick();
                facebookLogOutEvent();
            }
        });

        // 구글
        google_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleLogOutEvent();
            }
        });

        // 카카오
        kakao_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                kakaoLogOutEvent();
            }
        });

        // 이메일
        email_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefConfig.writeLoginStatus(false);
                prefConfig.writeName("User");
                Intent intent = new Intent(Result.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

        // 지도보러가자 (탑승객)
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Result.this, google_map.class);
                intent.putExtra("profile", image_path);
                intent.putExtra("email", email);
                intent.putExtra("name", name);
                startActivity(intent);
            }
        });

        // 라이더 (운전자)
        rider_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Result.this, start_Driver.class);
                intent.putExtra("profile", image_path);
                intent.putExtra("email", email);
                intent.putExtra("name", name);
                startActivity(intent);
            }
        });

    }

    public void facebookLogOutEvent() {
        LoginManager.getInstance().logOut();
        finish();
    }

    public void googleLogOutEvent() {
        mGoogleApiClient.connect();
        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                mAuth.signOut();
                if (mGoogleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                Log.e("알림", "로그아웃 성공");
                                setResult(1);
                            } else {
                                setResult(0);
                            }
                            finish();
                        }
                    });
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                Log.v("알림", "Google API Client Connection Suspended");
                setResult(-1);
                finish();
            }
        });
    }

    public void kakaoLogOutEvent() {
        UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                Intent intent = new Intent(Result.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
    }

}
