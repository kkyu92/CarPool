package com.example.kks.carpool;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class KakaoPayment extends AppCompatActivity {

    private WebView mainWebView;
    private WebViewInterface mWebViewInterface;
    private final String APP_SCHEME = "iamportapp";
    private final Handler handler = new Handler();
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kakao_payment);

        mTextView = findViewById(R.id.mTextView);

        Intent getIntent = getIntent();
        String user = getIntent.getStringExtra("user");
        String fare = getIntent.getStringExtra("fare");
        String addr = getIntent.getStringExtra("addr");

        mainWebView = (WebView) findViewById(R.id.mainWebView); //웹뷰 객체
        mainWebView.setWebViewClient(new Payment(this));
        WebSettings settings = mainWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        //안드로이드와 자바스크립트 간의 통신 연결
        mainWebView.addJavascriptInterface(new AndroidBridge(), "android");

//        mWebViewInterface = new WebViewInterface(KakaoPayment.this, mainWebView); //JavascriptInterface 객체화
//        mainWebView.addJavascriptInterface(mWebViewInterface, "android"); //웹뷰에 JavascriptInterface를 연결

        try {
            KakaoPay(user, fare, addr);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        String url = intent.toString();

        if ( url.startsWith(APP_SCHEME) ) {
            // "iamportapp://https://pgcompany.com/foo/bar"와 같은 형태로 들어옴
            String redirectURL = url.substring(APP_SCHEME.length() + "://".length());
            mainWebView.loadUrl(redirectURL);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if ( intent != null ) {
            Uri intentData = intent.getData();

            if ( intentData != null ) {
                //카카오페이 인증 후 복귀했을 때 결제 후속조치
                String url = intentData.toString();
                Log.e("KakaoPay:::","인증 후 복귀했을 때 결제 후속조치");
                if ( url.startsWith(APP_SCHEME) ) {
                    String path = url.substring(APP_SCHEME.length());
                    if ( "process".equalsIgnoreCase(path) ) {
                        Log.e("KakaoPay:::","process");
                        mainWebView.loadUrl("javascript:IMP.m_redirect_url({result:'process'})");
                    } else {
                        Log.e("KakaoPay:::","cancel");
                        mainWebView.loadUrl("javascript:IMP.communicate({result:'cancel'})");
                    }
                }
            }
        }
    }
    
    private void KakaoPay(String user, String fare, String addr) throws UnsupportedEncodingException {
        String str = "user=" + URLEncoder.encode(user, "UTF-8")
                + "&fare=" + URLEncoder.encode(fare, "UTF-8")
                + "&addr=" + URLEncoder.encode(addr, "UTF-8");
        mainWebView.postUrl("http://54.180.95.149/Payment.php", str.getBytes());
    }

    private class AndroidBridge {
        @JavascriptInterface
        public void setMessage(final String arg) {
            handler.post(new Runnable() {
                public void run() {
                    Log.e("javascriptMessage", arg);
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        }
    }
}
