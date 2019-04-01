package com.example.kks.carpool;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.example.kks.carpool.Pref.PrefConfig;
import com.example.kks.carpool.model.User;
import com.example.kks.carpool.retro.ApiClient;
import com.example.kks.carpool.retro.ApiInterface;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.kks.carpool.Result.USER_PROFILE;
import static com.kakao.util.helper.Utility.getPackageInfo;


public class Login extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    // Retrofit
    public static PrefConfig prefConfig;
    public static ApiInterface apiInterface;
    private TextView RegText;
    private EditText userEmail, userPassword;
    private Button login_btn;
    // 보류
    OnLoginFormActivityListener loginFormActivityListener;

    // 페이스 북
    private static final String PERMISSION = "public_profile";
    private LoginButton fb_login_btn;
    private com.shaishavgandhi.loginbuttons.FacebookButton facebook_fake_btn;
    private CallbackManager callbackManager;

    private String fb_name, fb_img;

    // 구글
    private SignInButton google_btn;
    private com.shaishavgandhi.loginbuttons.GooglePlusButton google_fake_btn;
    static GoogleApiClient mGoogleApiClient;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 10;
    private String TAG = "Login Activity";

    private FirebaseAuth mAuth;
    private android.app.AlertDialog waiting_dialog;

    // 카카오
    private SessionCallback callback;
    private com.kakao.usermgmt.LoginButton kakao_login_btn;
    private Button kakao_fake_btn;

    private AQuery aQuery;


    //
    // 1. TCP 서버의 IP와 PORT를 상수로 할당
    // 실제로는 서버의 IP보다는 도메인을 작성하는 것이 좋다.
//    private static final String SERVER_IP = "192.168.56.1";
//    private static final int SERVER_PORT = 5000;
//    private static String ip = "127.0.0.1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.e("GET KEY HASH :::: ", getKeyHash(Login.this));

        // Retrofit
        prefConfig = new PrefConfig(this);
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        userEmail = findViewById(R.id.email_txt);
        userPassword = findViewById(R.id.pass_txt);
        RegText = findViewById(R.id.signup_btn);
        login_btn = findViewById(R.id.login_btn);

        if (prefConfig.readLoginStatus()) {
            Log.e("로그인 유지 부분 ::: ",""+prefConfig.readLoginStatus());
            Intent intent = new Intent(Login.this, Result.class);
            startActivity(intent);
        } else {

        }

        // 페이스북
        callbackManager = CallbackManager.Factory.create();

        fb_login_btn = findViewById(R.id.btn_facebook);
        facebook_fake_btn = findViewById(R.id.fake_facebook);
        fb_login_btn.setReadPermissions(Arrays.asList(PERMISSION));


        // 구글
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        google_btn = findViewById(R.id.btn_google);
        google_fake_btn = findViewById(R.id.fake_google);

        mAuth = FirebaseAuth.getInstance();

        waiting_dialog = new SpotsDialog.Builder().setContext(this)
                .setMessage("잠시만 기다려주세요...")
                .setCancelable(false)
                .build();

        // 카카오
        aQuery = new AQuery(this);
        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);
//        Session.getCurrentSession().checkAndImplicitOpen();

        kakao_login_btn = findViewById(R.id.btn_kakao);
        kakao_fake_btn = findViewById(R.id.fake_kakao);

//        Session.getCurrentSession().removeCallback(callback);
        if (Session.getCurrentSession().isOpened()) { // 바로 로그인 되어버림 하지만 정보가 없어서 널포인트 에러!!!!!!!!!!!!!!!
            requestMe();
        } else {
            Toast.makeText(this, "세션 로그인 ㄴㄴ", Toast.LENGTH_SHORT).show();
        }
    }

    protected void onResume() {
        super.onResume();

        // 회원가입하러 가기
        RegText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Signup.class);
                startActivity(intent);
//                loginFormActivityListener.performRegister();
            }
        });

        // 로그인 버튼
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performLogin();
            }
        });

        // 페이스북
        facebook_fake_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fb_login_btn.performClick();
            }
        });

        fb_login_btn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                AccessToken accessToken = AccessToken.getCurrentAccessToken();

                if (!accessToken.isExpired()) {
                    GraphRequest request = GraphRequest.newMeRequest(accessToken, mGraphCallBack);
                    Bundle params = new Bundle();
                    params.putString("fields", "id, name, picture.height(600)");
                    request.setParameters(params);
                    request.executeAsync();
                }
            }
            @Override
            public void onCancel() {
                Toast.makeText(Login.this, "cancel", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(FacebookException error) {
                Toast.makeText(Login.this, "error", Toast.LENGTH_SHORT).show();
            }
        });

        // 구글
        google_fake_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                google_btn.performClick();
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        google_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//                startActivityForResult(signInIntent, RC_SIGN_IN);
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        // 카카오
        kakao_fake_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                kakao_login_btn.performClick();
            }
        });
    }

    // 로그인 버튼
    private void performLogin() {
        final String useremail = userEmail.getText().toString();
        String userpassword = userPassword.getText().toString();

        Call<User> call = apiInterface.performUserLogin(useremail, userpassword);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.body().getResponse().equals("ok")) {
                    prefConfig.writeLoginStatus(true);
                    //prefConfig.writeName();
                    String profile = response.body().getProfile();
                    USER_PROFILE = profile;
                    Log.e("프로필 경로", ""+profile);
                    Intent intent = new Intent(Login.this, Result.class);
                    //이름 넘겨줘야해
                    intent.putExtra("email", useremail);
                    intent.putExtra("name", response.body().getName());
                    intent.putExtra("photo_uri", profile);
                    startActivity(intent);
//                    loginFormActivityListener.performLogin(response.body().getName());
                } else if (response.body().getResponse().equals("failed")) {
                    prefConfig.displayToast("이메일 또는 비밀번호를 확인하세요!");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });

//        userEmail.setText("");
        userPassword.setText("");
    }

    public static String getKeyHash(final Context context) {
        PackageInfo packageInfo = getPackageInfo(context, PackageManager.GET_SIGNATURES);
        if (packageInfo == null)
            return null;

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                return Base64.encodeToString(md.digest(), Base64.NO_WRAP);
            } catch (NoSuchAlgorithmException e) {
                Log.e("d", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
        return null;
    }

    public interface OnLoginFormActivityListener {
        public void performRegister();
        public void performLogin(String name);
    }

    protected GraphRequest.GraphJSONObjectCallback mGraphCallBack = new GraphRequest.GraphJSONObjectCallback() {
        @Override
        public void onCompleted(JSONObject object, GraphResponse response) {

            try {
                // 페북 아이디
                fb_name = object.getString("name");
//                nametxt.setText("Welcome : " + object.getString("name"));
                // 페북 프로필 이미지
                fb_img = object.getString("id");
//                profileImage.setProfileId(object.getString("id"));
                if (fb_name != null || fb_img != null) {
                    Intent intent = new Intent(Login.this, Result.class);
                    intent.putExtra("name", fb_name);
//                intent.putExtra("phone_number", object.getString(""));
                    intent.putExtra("photo_uri", fb_img);
                    startActivity(intent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);

        /// 페이스북
        if (requestCode == RC_SIGN_IN) {
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            try {
//                // Google Sign In was successful, authenticate with Firebase
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//                firebaseAuthWithGoogle(account);
//            } catch (ApiException e) {
//                // Google Sign In failed, update UI appropriately
//                Log.w(TAG, "Google sign in failed", e);
//                // ...
//            }
            waiting_dialog.show();

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                waiting_dialog.dismiss();

                GoogleSignInAccount account = result.getSignInAccount();
                String idToken = account.getIdToken();

                AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
                firebaseAuthWithGoogle(credential);
            } else {
                waiting_dialog.dismiss();
                Log.e("EDMT_ERROR", "Login failed");
//                Log.e("EDMT_ERROR", result.getStatus().getStatusMessage());
            }
        } else if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) { // 카카오
            requestMe();
        } else { // 구글
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(AuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Intent intent = new Intent(Login.this, Result.class);
                        intent.putExtra("email", authResult.getUser().getEmail());
                        intent.putExtra("name", authResult.getUser().getDisplayName());
                        intent.putExtra("phone_number", authResult.getUser().getPhoneNumber());
                        intent.putExtra("photo_uri", authResult.getUser().getPhotoUrl());
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Login.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, ""+connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }

    // 카카오톡
    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {
//            redirectSignupActivity();  // 세션 연결성공 시 redirectSignupActivity() 호출

            //access token을 성공적으로 발급 받아 valid access token을 가지고 있는 상태. 일반적으로 로그인 후의 다음 activity로 이동한다.
            if(Session.getCurrentSession().isOpened()){ // 한 번더 세션을 체크해주었습니다.
                requestMe();
            }
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            if(exception != null) {
                Logger.e(exception);
            }
//            setContentView(R.layout.activity_login); // 세션 연결이 실패했을때

            Toast.makeText(Login.this, "세션연결 실패", Toast.LENGTH_SHORT).show();
        }                                            // 로그인화면을 다시 불러옴
    }

//    protected void redirectSignupActivity() {       //세션 연결 성공 시 SignupActivity로 넘김
//        final Intent intent = new Intent(this, KakaoSignupActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//        startActivity(intent);
//        finish();
//    }

    private void requestMe() {
        UserManagement.getInstance().requestMe(new MeResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Log.e("onFailure", errorResult + "");
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Log.e("onSessionClosed",errorResult + "");
            }

            @Override
            public void onSuccess(UserProfile userProfile) {
                Log.e("onSuccess",userProfile.toString());

                Intent intent = new Intent(Login.this, Result.class);
                intent.putExtra("email", userProfile.getEmail());
                intent.putExtra("name", userProfile.getNickname());
                intent.putExtra("photo_uri", userProfile.getProfileImagePath());
                startActivity(intent);
//                aQuery.id(user_img).image(userProfile.getThumbnailImagePath()); // <- 프로필 작은 이미지 , userProfile.getProfileImagePath() <- 큰 이미지
            }

            @Override
            public void onNotSignedUp() {
                Log.e("onNotSignedUp","onNotSignedUp");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 카카오톡
        Session.getCurrentSession().removeCallback(callback);
    }

}
